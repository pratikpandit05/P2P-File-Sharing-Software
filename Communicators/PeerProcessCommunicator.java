/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Communicators;

import PeerProcess.DataFile;
import Message.MessageHandshake;
import Message.Message;
import Message.MessageActual;
import Message.MessagePiece;
import Message.MessageType;
import Message.InterestedMessage;
import Message.MessageHave;
import Message.MessageNotInterested;
import Message.MessageRequest;
import Message.MessageBitField;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;

import java.util.Arrays;
import java.util.logging.*;
import Utilities.Utilities;

public class PeerProcessCommunicator implements Runnable
{
    private DataFile myTorrentFile;
    private Client myClient = null;
    private DataInputStream dis = null;
    private int myOwnID;
    private int myPeersID = -1;
    private boolean amIchoked = true;
    private CommunicationHandler myTransceiver;
    private static final Logger debugLogger = Logger.getLogger("A");
    private final String debugHeader;
    private long downloadStartTime;
    private long downloadStopTime;
    private volatile boolean requestSenderStarted = false;
    
    public PeerProcessCommunicator(Client aClient, CommunicationHandler myTransceiver) throws IOException
    {
        this.myTransceiver = myTransceiver;
        this.myTorrentFile = myTransceiver.getTorrentFile();
        this.myClient = aClient;
        this.dis = new DataInputStream(new PipedInputStream(aClient.getPipedOutputStream()));
        this.myOwnID = myTransceiver.getMyPeerID();       
        debugHeader = "Peer " + this.myOwnID + " : ";
        
    }
    
    @Override
    public void run()
    {
	
        try
        {
            
            sendHandshake(myOwnID);
            
            this.processHandshake(this.receiveHandshake());
                     
            myClient.setSoTimeout();            
            (new Thread(myClient)).start();
                   
            this.readDataAndTakeAction();
        }

        catch (IOException e)
        {
         //   debugLogger.fatal("IOEx", e);
        } catch (InterruptedException e)
        {
           // debugLogger.fatal("InterruptedEx", e);
        }
    }
    
    private MessageHandshake receiveHandshake() throws IOException
    {
        myClient.receive(32);
        byte[] handshakeMsg = new byte[32];
        dis.readFully(handshakeMsg);
        return new MessageHandshake(handshakeMsg);
    }

    private void sendHandshake(int myPeerID) throws IOException, InterruptedException
    {
        Message msg = new MessageHandshake(myPeerID);
        myClient.send(msg.getBytes());
        
    }

    private void readDataAndTakeAction() throws IOException, InterruptedException
    {        
        //now always receive bytes and take action
        while(true)
        {
            
            MessageActual msg = getNextMessage();
            
            try {
            
                takeAction(msg);
            
            } catch (Exception ex) {
                
            }
            
        }
    }

    private void takeAction(MessageActual msg) throws IOException, InterruptedException
    {
        
        if (msg == null) {
            return;
        }
        
        int payloadLength = msg.getMessageLength() - MessageType.getMessageTypeLength();  //removing  the size of message type
        
        
        switch(msg.getMsgType())
        {
        case choke:
            takeActionForChokeMessage();
            break;
        case unchoke:
            takeActionForUnchokeMessage();
            break;
        case interested:
            takeActionForInterestedMessage();
            break;
        case notInterested:
            takeActionForNotInterestedMessage();
            break;
        case have:
            takeActionForHaveMessage(payloadLength);
            break;
        case request:
            takeActionForRequestMessage();
            break;
        case piece:
            takeActionForPieceMessage(payloadLength);
            break;
        case bitfield:
            takeActionForBitFieldMessage(payloadLength);
        }
    }

    private void takeActionForBitFieldMessage(int msgLength) throws IOException, InterruptedException
    {
        byte[] bitmap = new byte[msgLength];
        dis.readFully(bitmap);
        myTorrentFile.setPeerBitmap(myPeersID, bitmap);
        if(myTorrentFile.hasInterestingPiece(myPeersID))
        {
            myClient.send((new InterestedMessage()).getBytes());
        }
        else
        {
            myClient.send((new MessageNotInterested()).getBytes());
        }
    }

    private void takeActionForPieceMessage(int msgLength) throws IOException, InterruptedException
    {
        byte[] pieceIndexBuffer = new byte[4];
        
        dis.readFully(pieceIndexBuffer);
        int pieceIndex = Utilities.getIntegerFromByteArray(pieceIndexBuffer, 0);
        byte[] pieceData = new byte[msgLength - 4];  //subtracting the length of pieceIndex
        dis.readFully(pieceData);
        
        downloadStopTime = System.currentTimeMillis();
        myTransceiver.addOrUpdatePeerDownloadRate(myPeersID, (downloadStopTime - downloadStartTime));
        
        myTorrentFile.reportPieceReceived(pieceIndex, pieceData);
            
        myTransceiver.logMessage("Peer " + myOwnID + " has downloaded the piece " + pieceIndex + " from " + myPeersID 
                                                         + ". Now the number of pieces it has is " + myTorrentFile.getDownloadedPieceCount(myOwnID)
                                                         + ".");
        
        
        // check if all pieces have been downloaded
        if (myTorrentFile.getDownloadedPieceCount (myOwnID) == myTorrentFile.getTotalPieceCount())
        {
                myTransceiver.logMessage("Peer " + myOwnID + " has downloaded the complete file.");
                        // Send not interested to everybody because I now have the complete file.
                        myTransceiver.sendMessageToGroup(myTransceiver.getAllPeerIDList(), new MessageNotInterested().getBytes());
        }
        
        // If not choked, request for more pieces
        if(!amIchoked)
        {
            //send request for another piece
            int desiredPiece = myTorrentFile.getRequiredPieceIndexFromPeer(myPeersID);
            if(desiredPiece != -1)
            {
                myClient.send((new MessageRequest(desiredPiece)).getBytes());
            }
        }
        //send have message to every one to notify about this piece received
        myTransceiver.sendMessageToGroup(myTransceiver.getAllPeerIDList(), (new MessageHave(pieceIndex)).getBytes());
        
        //after receiving the piece check if you need to send not-interested message to any of the peers
        myTransceiver.sendMessageToGroup(myTransceiver.computeAndGetWastePeersList(), new MessageNotInterested().getBytes());
    }

    private void takeActionForRequestMessage() throws IOException, InterruptedException
    {        
        //1. we know that requested piece index is an integer
        byte[] indexBuffer = new byte[4];
        dis.readFully(indexBuffer);
        int pieceIndex = Utilities.getIntegerFromByteArray(indexBuffer, 0);
        //2. now read this data from file.
        byte[] dataForPiece = myTorrentFile.getPieceData(pieceIndex);
        //3. send this data as piece packet to peer
        myClient.send((new MessagePiece(pieceIndex, dataForPiece)).getBytes());
    }

    private void takeActionForHaveMessage(int msgLength) throws IOException, InterruptedException
    {
        //read payload from pipe
        byte[] payload = new byte[msgLength];
        dis.readFully(payload);
        //as we know that this payload is piece index, convert it and pass to torrent file
        int pieceIndex = Utilities.getIntegerFromByteArray(payload, 0);
        
        myTransceiver.logMessage("Peer " + myOwnID + " received the 'have' message from " + myPeersID + " for the piece " + pieceIndex);
        myTorrentFile.reportPeerPieceAvailablity(myPeersID, pieceIndex);
        if(!myTorrentFile.doIHavePiece(pieceIndex))
        {
            //report that neighbor is interesting
            myTransceiver.reportInterestedPeer(myPeersID);
            myClient.send((new InterestedMessage()).getBytes());
        }
    }

    private void takeActionForNotInterestedMessage()
    {
        myTransceiver.logMessage("Peer " + myOwnID + " received the 'not interested' message from " + myPeersID);
        //remove from interested neighbours list
        myTransceiver.reportNotInterestedPeer(myPeersID);
    }

    private void takeActionForInterestedMessage()
    {
        myTransceiver.logMessage("Peer " + myOwnID + " received the 'interested' message from " + myPeersID);
        //add to interested neighbors list
        myTransceiver.reportInterestedPeer(myPeersID);
    }

    private void takeActionForUnchokeMessage() throws IOException, InterruptedException
    {
        myTransceiver.logMessage("Peer " + myOwnID + " is unchoked by " + myPeersID);
        //first of all set the status that I am now unchoked
        this.amIchoked = false;
        
        if(!requestSenderStarted)
        {
            (new Thread(new RequestMessageManager())).start();
            this.requestSenderStarted  = true;
        }
        
        //select any piece which my peer has but I don't have and I have not already requested
        int pieceIndex = myTorrentFile.getRequiredPieceIndexFromPeer(myPeersID);
        if(pieceIndex != -1)
        {
            myClient.send((new MessageRequest(pieceIndex)).getBytes());
        }
    }

    private void takeActionForChokeMessage()
    {
        myTransceiver.logMessage("Peer " + myOwnID + " is choked by " + myPeersID); 
        //set the status that I am choked now
        this.amIchoked = true;
        myTransceiver.resetPeerDownloadRate(myPeersID);
        
    }

    private void processHandshake(MessageHandshake handshakeMsg) throws IOException, InterruptedException
    {
        byte [] msgBytes = handshakeMsg.getBytes();
        byte [] msgHeader = new byte[18];
        System.arraycopy(msgBytes, 0, msgHeader, 0, 18);
        if (Arrays.equals(msgBytes, "CEN5501C2008SPRING".getBytes())==false)
        {
        }
        
        this.myPeersID = handshakeMsg.getPeerID();
        //report this peer connection to Transceiver
        this.myTransceiver.reportNewClientConnection(this.myPeersID, myClient);
             
        
        myTransceiver.logMessage("Peer " + myOwnID + " is connected from Peer " + myPeersID);
        
        //send my bitmap to others only if I have some piece
//        if(myTorrentFile.doIHaveAnyPiece())
        {
            myClient.send((new MessageBitField(myTorrentFile.getMyDataFileBitmap())).getBytes());
        }
    }
    
    private MessageActual getNextMessage() throws IOException, InterruptedException
    {
        //rule, always read first 4 bytes (or an int) and 
        byte[] lengthBuffer = new byte[4];
        dis.readFully(lengthBuffer);
        int msgLength = Utilities.getIntegerFromByteArray(lengthBuffer, 0);
        //then read the message type
        byte[] msgType = new byte[1];
        dis.readFully(msgType);
        
        if (MessageType.getMessageType(msgType[0]) == null) {
            return (null);
        }
        
        return new MessageActual(msgLength, MessageType.getMessageType(msgType[0]));
    }

    class RequestMessageManager implements Runnable
    {
        private void sendRequestMessage() throws IOException, InterruptedException
        {
            //send request for another piece
            int desiredPiece = myTorrentFile.getRequiredPieceIndexFromPeer (myPeersID);
            if(desiredPiece != -1)
            {
                myClient.send((new MessageRequest(desiredPiece)).getBytes());
            }
        }

        @Override
        public void run()
        {
            while(! myTorrentFile.IsQuittingPossible())
                        {
            try
            {
                this.sendRequestMessage();
                
                Thread.sleep (6);
                                
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
                        
        }
	
        }
    }
}
