/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Communicators;
import Message.MessageUnchoke;
import Message.MessageChoke;
import java.util.logging.*;
import java.io.*;
import ConfigurationParsers.CommonFileParser;
import ConfigurationParsers.PeerConfig;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import PeerProcess.DataFile;

public class CommunicationHandler
{
    final int NUM_PREFERRED_NEIGHBORS;
    private final int UNCHOKING_INTERVAL;
    private final int OPTIMISTIC_UNCHOKING_INTERVAL;
    public Map<Integer, PeerConfig> peerInfoMap;
    private int myPeerID;
    private String myHostName;
    private int myListenerPort;
    private final int pieceSize;
    private AtomicInteger prevOptUnchokedPeer;
    private ConcurrentHashMap<Integer, Client> peerConnectionMap;
    private Map<Integer, Double> peerDownloadRate; // peer id --> download rate
    private DataFile myTorrentFile;
    private Set<Integer> interestedNeighbours;
    private List<Integer> allPeerIDList;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private Set<Integer> chokedPeersSet = new ConcurrentSkipListSet<Integer>();
    private CommonFileParser myCommonConfig;
    private static final Logger debugLogger = Logger.getLogger("A");
    private static final Logger eventLogger = Logger.getLogger("B");
    FileHandler handler = null;

    public CommunicationHandler(CommonFileParser myCommonConfig, Map<Integer, PeerConfig> peerMap, int myPeerID) throws IOException 
    {
        this.myCommonConfig = myCommonConfig;
        PeerConfig myConfig = peerMap.get(myPeerID);
        this.myHostName = myConfig.getHostName();
        this.myListenerPort = myConfig.getListeningPort();
        this.pieceSize = myCommonConfig.getPieceSize();
        this.peerInfoMap = peerMap;
        this.myPeerID = myPeerID;
        this.NUM_PREFERRED_NEIGHBORS = myCommonConfig.getNumPreferredNeighbours();
        this.UNCHOKING_INTERVAL = myCommonConfig.getUnchokingInterval();
        this.OPTIMISTIC_UNCHOKING_INTERVAL = myCommonConfig.getOptimisticUnchokingInterval();
        this.peerConnectionMap = new ConcurrentHashMap<Integer, Client>();
        this.peerDownloadRate = new HashMap<Integer, Double>();
        this.interestedNeighbours = new ConcurrentSkipListSet<Integer>();
        allPeerIDList = new ArrayList<Integer>();
        //add all the peerIDs in allPeerIDList
        allPeerIDList.addAll(this.peerInfoMap.keySet());
        
        // Initialize this to -1
        prevOptUnchokedPeer = new AtomicInteger(-1); 
        
        // Make the log file name for this peer
        String peerLogFileName = System.getProperty("user.dir") + "/log_peer_" + myPeerID + ".log";
        // Start logger for this peer
        System.setProperty("peer.logfile", peerLogFileName);

	this.handler = new FileHandler(peerLogFileName);
	debugLogger.addHandler(this.handler);
        eventLogger.addHandler(this.handler);
        handler.setFormatter(new SimpleFormatter());
        
    }
    
    public void start() throws SocketTimeoutException, IOException, InterruptedException
    {
        Thread serverThread = new Thread(new Server(myHostName, myListenerPort, this));
        serverThread.start();
        eventLogger.info("Started the server on port " + myListenerPort);
	//eventLogger.log(Level.INFO,"Started the server on port " + myListenerPort);
        this.myTorrentFile = new DataFile(myPeerID, myCommonConfig, peerInfoMap.keySet(), 
                this.peerInfoMap.get(myPeerID).hasCompleteFile(), this);
        this.processPeerInfoMap();
        scheduler.scheduleAtFixedRate(new SchedulerPreferredPeersManager(this), 0, UNCHOKING_INTERVAL, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new OptimisticNeighborManager(this), 0, OPTIMISTIC_UNCHOKING_INTERVAL, TimeUnit.SECONDS);
//        serverThread.join();
    }
    
    
    private void processPeerInfoMap() throws SocketTimeoutException, IOException
    {
        for(Integer aPeerID : this.peerInfoMap.keySet())
        {
            if(aPeerID < myPeerID)
            {
                //peer has already been started, try to make a connection
                Client newClient = new Client (this.peerInfoMap.get(aPeerID).getHostName(),
                        this.peerInfoMap.get(aPeerID).getListeningPort(), this, aPeerID);
                //now make an EventHandler (algorithm) for this client
                PeerProcessCommunicator anEventManager = new PeerProcessCommunicator(newClient, this);
                //start event manager before client starts any activity
                (new Thread(anEventManager)).start();
                this.peerConnectionMap.put(aPeerID, newClient);
                eventLogger.info("Started Client for peerID = " + aPeerID);
		//eventLogger.log(Level.INFO,"Started Client for peerID = " + aPeerID);
                eventLogger.info("Peer " + myPeerID + " makes a connection to Peer " + aPeerID);
		//eventLogger.log(Level.INFO,"Peer " + myPeerID + " makes a connection to Peer " + aPeerID);
            }
        }
    }
    
    public void sendMessageToGroup(List<Integer> peerIDList, byte[] data) throws IOException
    {
        for (Integer aPeerID : peerIDList)
        {
            if(this.peerConnectionMap.containsKey(aPeerID))
            {
                this.peerConnectionMap.get(aPeerID).send(data);
            }
            else
            {
//                debugLogger.warn(this.myPeerID + " : No client found for peer " + aPeerID);
            }
        }
    }
    
    public void sendMessageToPeer(int peerID, byte[] data) throws IOException
    {
        if(this.peerConnectionMap.containsKey(peerID))
        {
            this.peerConnectionMap.get(peerID).send(data);
        }
        else
        {
//            debugLogger.warn(this.myPeerID + " : No client found for peer " + peerID);
        }
    }

    public DataFile getTorrentFile()
    {
        return this.myTorrentFile;
    }

    public PeerConfig getMyPeerConfig() 
    {
        return (this.peerInfoMap.get (this.myPeerID));
    }
    
    public int getMyPeerID()
    {
        return this.myPeerID;
    }
    
    public void logMessage(String msg)
    {
        eventLogger.info(msg);
	//eventLogger.log(Level.INFO,msg);
    }
    
    public void reportInterestedPeer(int peerID)
    {
        this.interestedNeighbours.add(new Integer(peerID));

    }
    
    public void reportNotInterestedPeer(int peerID)
    {
        this.interestedNeighbours.remove(new Integer(peerID));

    }

    public List<Integer> getAllPeerIDList()
    {
        return allPeerIDList;
    }

    public List<Integer> computeAndGetWastePeersList()
    {
        List<Integer> wastePeersList = new ArrayList<Integer>();
        for(Integer peerID : allPeerIDList)
        {
            if(!myTorrentFile.hasInterestingPiece(peerID))
            {
                wastePeersList.add(peerID);
            }
        }
        return wastePeersList;
    }

    public synchronized void addOrUpdatePeerDownloadRate(Integer peerId, long elapsedTime)
    {   
                
        double downloadRate = (double) (elapsedTime / this.pieceSize);
        
        peerDownloadRate.put(peerId, downloadRate);
        
        
    }

    public synchronized void resetPeerDownloadRate(Integer peerId)
    {          
        peerDownloadRate.put(peerId, 0.0);
    }
    
    public double getDownloadRate(Integer peerId)
    {
        if(this.peerDownloadRate.containsKey (peerId))
            return peerDownloadRate.get (peerId);
        else
            System.out.println ("Unable to find... peerId '" + peerId + "'");
            return -1;
    }
    
    public Set<Integer> getInterestedNeighbours()
    {
        return this.interestedNeighbours;
    }

    public void reportChokedPeer(Integer peerID) throws IOException, InterruptedException
    {
        //send choke message to this peer
        this.sendMessageToPeer(peerID, new MessageChoke().getBytes());

        synchronized (this.chokedPeersSet) {
            this.chokedPeersSet.add(peerID);
        }
    }

    public void reportUnchokedPeer(int peerID) throws IOException, InterruptedException
    {
        //send unchoke message to this peer
        this.sendMessageToPeer(peerID, new MessageUnchoke().getBytes());
        
        synchronized (this.chokedPeersSet) {
            this.chokedPeersSet.remove(peerID);
        }
        
    }

    public Set<Integer> getChokedPeers()
    {
        return this.chokedPeersSet;
    }
    
    public int getPrevOptUnchokedPeer()
    {
        return prevOptUnchokedPeer.get();
    }
    
    public void setPrevOptUnchokedPeer(int peerId)
    {
        this.prevOptUnchokedPeer.set(peerId);
    }
    
    public void reportNewClientConnection(int clientID, Client aClient)
    {
        logMessage ("Peer" + getMyPeerID() + " is connected to Peer " + clientID );
        
        this.peerConnectionMap.put(clientID, aClient);
        
    }

    public void QuitInterrupt()
    {
       debugLogger.info("Peer " + myPeerID + " : Transceiver signalled to quit.");
        scheduler.shutdownNow();
    }
}