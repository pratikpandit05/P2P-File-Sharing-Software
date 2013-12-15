
package Communicators;

import java.io.IOException;
import java.util.*;

public class SchedulerPreferredPeersManager implements Runnable {
    
    private CommunicationHandler m_CommunicationHandler;
    private Set<Integer> m_PreferredNeighbourIds;

    public SchedulerPreferredPeersManager (CommunicationHandler handler) {
        m_PreferredNeighbourIds = new TreeSet<Integer>();
        m_CommunicationHandler = handler;
        
    }

    public void run()
    {
        try {
            ChoosePreferredNeighbours();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
            
    }
        
    private synchronized void ChoosePreferredNeighbours() throws IOException, InterruptedException {
        
        List<double[]> ListPeerDownloadRates = new ArrayList<double[]>();
        Set<Integer> interestedNeighborsList = m_CommunicationHandler.getInterestedNeighbours();
        
        for (Integer PeerId : interestedNeighborsList) {
        
            double[] peerIDAndRatePair = new double[2];
            peerIDAndRatePair[0] = (double)PeerId;
            
            if (m_CommunicationHandler.getMyPeerConfig().hasCompleteFile()) {
                
                peerIDAndRatePair[1] = Math.random() * m_CommunicationHandler.NUM_PREFERRED_NEIGHBORS;
            
            } else {
                
                peerIDAndRatePair[1] = m_CommunicationHandler.getDownloadRate(PeerId);
                
            }
            
            // :) :) :) 
            if (peerIDAndRatePair[1] != -1) {
                ListPeerDownloadRates.add (peerIDAndRatePair);
            }
        }
        
        Collections.sort(ListPeerDownloadRates, new Comparator<double[]>() {
            
            @Override
            public int compare(double[] rate1, double[] rate2) {
                
                if(rate1[1] > rate2[1])
                    return 1;
                else if(rate1[1] < rate2[1])
                    return -1;
                else 
                    return 0;
            
            }
        
        });
        
        Set<Integer> newPreferredPeersSet = new TreeSet<Integer>();
        
        int count = ListPeerDownloadRates.size() < m_CommunicationHandler.NUM_PREFERRED_NEIGHBORS ? 
                ListPeerDownloadRates.size() : m_CommunicationHandler.NUM_PREFERRED_NEIGHBORS;
                
        for(int i = 0; i < count; i++) {
            
            int peerID = (int)ListPeerDownloadRates.get(i)[0];
            
            if(!m_PreferredNeighbourIds.contains(peerID)) {
            
                m_CommunicationHandler.reportUnchokedPeer(peerID);
            }
            
            newPreferredPeersSet.add(peerID);
        }
        
        for(Integer peerID : m_PreferredNeighbourIds)
        {
            if (!newPreferredPeersSet.contains(peerID)) {
                m_CommunicationHandler.reportChokedPeer(peerID);
            }
        }
                
        m_PreferredNeighbourIds = newPreferredPeersSet;
        
        if (m_PreferredNeighbourIds.size() > 0) {
        
            String CommaList = "";
            
            for(Integer peerID : m_PreferredNeighbourIds) {
                
                CommaList += peerID;
                CommaList += ",";
            }
            
            String betterCommaSepString = CommaList.substring(0, CommaList.length()-1);
            
            m_CommunicationHandler.logMessage ("Peer " + m_CommunicationHandler.getMyPeerID() + " has the preferred neighbors " + betterCommaSepString);        
        
        }
        
    }
}
