
package Communicators;

import java.io.IOException;
import java.util.*;

public class OptimisticNeighborManager implements Runnable {
    
    private CommunicationHandler m_CommunicationHandler;
    
    public OptimisticNeighborManager(CommunicationHandler handler)
    {
        m_CommunicationHandler = handler;
    }

    @Override
    public void run()
    {
        try
        {
            selectOptimisticallyUnchokedPeer();
        } catch (IOException e)
        {            
            e.printStackTrace();
        } catch (InterruptedException e)
        {         
            e.printStackTrace();
        }
        
    }

    private synchronized void selectOptimisticallyUnchokedPeer() throws IOException, InterruptedException
    {
        
        Integer prevOCPeer = m_CommunicationHandler.getPrevOptUnchokedPeer();
        
        if (prevOCPeer != -1)
                m_CommunicationHandler.reportChokedPeer(m_CommunicationHandler.getPrevOptUnchokedPeer());
                
        Set<Integer> chokedPeersSet = m_CommunicationHandler.getChokedPeers();
        List<Integer> interestedAndChoked = new LinkedList<Integer>();
        interestedAndChoked.addAll(m_CommunicationHandler.getInterestedNeighbours());
        
        interestedAndChoked.retainAll(chokedPeersSet);
        if(interestedAndChoked.size() > 0)
        {
            Random rand = new Random();
            int selectedPeer = interestedAndChoked.get(rand.nextInt(interestedAndChoked.size()));
            
            m_CommunicationHandler.reportUnchokedPeer(selectedPeer);
            
            m_CommunicationHandler.setPrevOptUnchokedPeer(selectedPeer);
            
            m_CommunicationHandler.logMessage("Peer " + m_CommunicationHandler.getMyPeerID() + " has optimistically unchoked neighbor " + selectedPeer);
        }
    }

}
