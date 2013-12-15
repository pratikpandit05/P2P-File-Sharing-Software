
package PeerProcess;

import ConfigurationParsers.CommonFileParser;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Logger;
import Communicators.CommunicationHandler;

public class DataFile
{
    final String m_FileName;    
    final String m_WorkingDir;
    
    final int m_PeerId;
    final int m_TotalRequiredPieces;
    
    Map<Integer, byte[]> m_PeerIdToPieceBMap;
    Map<Integer, AtomicInteger> m_PeerIdToPieceDownloadCount;
    Set<Integer> m_RequestedPieces;
    
    FileHandler m_FileHandler;
    
    byte[] m_FBitmap;  //my final bitmap
    
    CommunicationHandler m_CommHandler;
    
    public DataFile (int peerNum, CommonFileParser commonFileParser, Set<Integer> peerConfigIDs, 
            boolean IsFilePresent, CommunicationHandler CommunicationHandler) throws IOException
    {
        m_PeerId = peerNum;
        
        m_CommHandler = CommunicationHandler;
        m_FileName = commonFileParser.getFileName();
        m_PeerIdToPieceBMap = new ConcurrentHashMap<Integer, byte[]>();
        
        m_PeerIdToPieceDownloadCount = new ConcurrentHashMap<Integer, AtomicInteger>();
        m_RequestedPieces = new ConcurrentSkipListSet<Integer>();
        m_TotalRequiredPieces = (int)Math.ceil((double)commonFileParser.getFileSize() / commonFileParser.getPieceSize());
        
        int totalBytesRequiredForPieces = (int)Math.ceil((double)m_TotalRequiredPieces / 8);
        
        for(Integer aPeerID : peerConfigIDs)
        {
            m_PeerIdToPieceBMap.put(aPeerID, new byte[totalBytesRequiredForPieces]);
            m_PeerIdToPieceDownloadCount.put(aPeerID, new AtomicInteger(0));
        }
        
        m_WorkingDir = System.getProperty ("user.dir") + "/peer_" + peerNum;
        
        if (
                !FileHandler.createDirectoryIfNotExists (m_WorkingDir)) {
            
        }
        
        m_FileHandler = new FileHandler (m_WorkingDir + "/" + commonFileParser.getFileName(),
                commonFileParser.getFileSize(), commonFileParser.getPieceSize());
        
        
        m_FBitmap = getFinalBitmap(m_TotalRequiredPieces);
        
        if(!IsFilePresent) {
            
            m_FileHandler.createDummyFile();
        
        } else {
            
            
                File tempFile = new File(m_WorkingDir + "/" + commonFileParser.getFileName());
                if (tempFile.exists() == false) {
                    
                    CommunicationHandler.logMessage("ERROR: File not found by " + peerNum);
                    System.exit(0);
                }
            
            if (tempFile.length() != commonFileParser.getFileSize())
                        {
                CommunicationHandler.logMessage("ERROR: File size is not correct, as found by " + peerNum + "\n reuired " + tempFile.length());
                                System.exit(0);
                        }
            
            m_PeerIdToPieceBMap.put(peerNum, m_FBitmap); 
        }
    }
    
    private byte[] getFinalBitmap (int piecesRequired) {
        
        int len = (int)Math.ceil((double)piecesRequired/8);
        byte[] fBitMap = new byte[len];
        
        for(int i = 0; i < len; i++)
        {
            fBitMap[i] = (byte)0xFF;
        }
        
        int lastBytePieces = piecesRequired & 7;  
        
        if(lastBytePieces > 0) 
        {
            fBitMap[len - 1] = (byte)(fBitMap[len - 1]&0xFF >>> (8 - lastBytePieces));
        }
        
        return fBitMap;
    }

    
    public void reportPieceReceived(int pieceID, byte[] pieceData) throws IOException
    
    {
    
        m_FileHandler.writePieceToFile (pieceID, pieceData);
        
        synchronized (this)
        {
            byte[] myFileBitmap = m_PeerIdToPieceBMap.get(m_PeerId);
            updateBitmapWithPiece(myFileBitmap, pieceID);
            m_PeerIdToPieceDownloadCount.get(m_PeerId).addAndGet(1);   
        }
                
        printBitmap();
        
        if(IsQuittingPossible())
        {
         
            m_CommHandler.QuitInterrupt(); // tell everyone to quit
        }
    
    }

    private void printBitmap()
    {
        StringBuilder builder = new StringBuilder("Peer " + m_PeerId + ": Bitmap Info--");
        
        for(Integer pids : m_PeerIdToPieceBMap.keySet())
        {
            builder.append(pids + ": " + m_PeerIdToPieceBMap.get(pids)[0] + "\n");
        }
        
    }

    public byte[] getMyDataFileBitmap()
    {
        return m_PeerIdToPieceBMap.get(m_PeerId);
    }

    public int getTotalPieceCount()
    {
        return m_TotalRequiredPieces;
    }
    
    
    public void reportPeerPieceAvailablity(int pId, int pieceIndex)
    {
   
        synchronized(this)
        {
            byte[] pFileBMap = m_PeerIdToPieceBMap.get (pId);
            updateBitmapWithPiece (pFileBMap, pieceIndex);
            m_PeerIdToPieceDownloadCount.get(pId).addAndGet(1);
        }
        
        printBitmap();
        
        if(IsQuittingPossible()) {
            
            m_CommHandler.QuitInterrupt();
        }
        
    }
    
    
    public int getDownloadedPieceCount (int pId)
    {
        return m_PeerIdToPieceDownloadCount.get(pId).get();
    }
    
    public boolean IsQuittingPossible()
    {
        for ( byte[] tempBitMap : m_PeerIdToPieceBMap.values())
        {
            if(isBitmapFinal(tempBitMap) == false)
            {
                return false;
            }
        }
        
        return true;
    }
    
    
    private boolean isBitmapFinal (byte[] tempBitMap)
    {
        
        for(int i = 0; i < tempBitMap.length; i++)
        {
            if((tempBitMap[i] ^ m_FBitmap[i]) != 0)
            {
                return false;
            }
        }
        return true;
    }

    public boolean doIHavePiece (int pieceIndex) {
        
        int pieceOffset = pieceIndex / 8;
        int bitOffset = pieceIndex & 7; 
        byte[] myBitMap = m_PeerIdToPieceBMap.get(m_PeerId);
        
        if((myBitMap [pieceOffset] & (1 << bitOffset)) != 0)  {
            return true;
        }
        
        return false;
    
    }

    private void updateBitmapWithPiece (byte[] peerFileBitmap, int pieceIndex) {
        
        int pieceOffset = pieceIndex / 8;
        int bitOffset = pieceIndex & 7;   
        peerFileBitmap [pieceOffset] |= (1 << bitOffset);
    
    }

    public byte[] getPeerBitmap(int peerID) {
        return m_PeerIdToPieceBMap.get(peerID);
    }
    
    public synchronized void setPeerBitmap (int peerID, byte[] bitmap)
    {
        m_PeerIdToPieceBMap.put(peerID, bitmap);
    }
    
    public String getFileName()
    {
        return m_FileName;
    }

    public boolean doIHaveAnyPiece()
    {
        byte[] myBitMap = m_PeerIdToPieceBMap.get(m_PeerId);
        final int mask = 0x000000FF;
        final int len = myBitMap.length;
        for(int i = 0; i < len; i++)
        {
            if((myBitMap[i] & mask) != 0)
            {
                return true;
            }
        }
        return false;
    }

    public boolean hasInterestingPiece(int peerId)
    {
        byte[] myBitMap = m_PeerIdToPieceBMap.get(m_PeerId);
        final int len = myBitMap.length;
        
        byte[] peerFileBitmap = m_PeerIdToPieceBMap.get(peerId); 

        for(int i = 0; i < len; i++)
        {
            if((0xFF&(int)(myBitMap[i] | peerFileBitmap[i])) > (0xFF&(int)myBitMap[i]))
            {
                return true;
            }
        }
        
        return false;
    }

    public int getRequiredPieceIndexFromPeer (int PeersId)
    {
        byte[] myBitMap = m_PeerIdToPieceBMap.get(m_PeerId);
        
        int desiredPieceID = -1;        
        
        byte[] peerBitmap = m_PeerIdToPieceBMap.get(PeersId);
        final int len = myBitMap.length;
        List<Integer> PiecesPermutation = new ArrayList<Integer>();
        
        for(int i = 0; i < len; i++)
        {
            if((0xFFFF &(long)(myBitMap[i] | peerBitmap[i])) > (0xFFFF&(long)myBitMap[i]))
            
            {
                for(int j = 0; j < 8; j++)
                {        
                    if((myBitMap[i] & (1 << j)) == 0 && (peerBitmap[i] & (1 << j)) != 0)
                    {
                        int attemptedPieceIndex = i*8 + j;
                        desiredPieceID = findAndLogRequestedPiece(attemptedPieceIndex);
                        if (desiredPieceID != -1)
                                PiecesPermutation.add(desiredPieceID);
                    }
                }
            }
        }
        
        if (PiecesPermutation.size() != 0) {
            
            Random rand = new Random();
            int idx = rand.nextInt(PiecesPermutation.size());                
            int pieceIndex = PiecesPermutation.get(idx); 
            m_RequestedPieces.add(pieceIndex);
            return pieceIndex;
        
        }
        
        return -1;
    
    }

    private synchronized int findAndLogRequestedPiece(int pieceIndex)
    
    {
        if(m_RequestedPieces.contains(pieceIndex))
        {
            return -1;
        }
        
        return pieceIndex;
    }

    
    public byte[] getPieceData(int pieceIndex) throws IOException
    {
        return m_FileHandler.getPieceFromFile(pieceIndex);
    }
    
}

