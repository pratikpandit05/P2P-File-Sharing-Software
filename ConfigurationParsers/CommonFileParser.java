
package ConfigurationParsers;

public class CommonFileParser {

    int m_FileSize;
    int m_PieceSize;

    int m_PreferredNeighbours;
    
    int m_UnchokingInterval;
    int m_OptimisticUnchokingInterval;
    
    String m_FileName;

    public CommonFileParser() {
        
    }
    
    public CommonFileParser (int numPNeighbours, int ucInterval, int oUCInterval, String fname, int size,
                             int pieceSize) {
        
        m_PreferredNeighbours = numPNeighbours;
        m_UnchokingInterval = ucInterval;
        m_OptimisticUnchokingInterval = oUCInterval;
        m_FileName = fname;
        m_FileSize = size;
        m_PieceSize = pieceSize;
    
    }
    
    protected void setFileName (String fName) {
        m_FileName = fName;
    }
    
    protected void setNumPreferredNeighbours (int numPNeighbours) {
        m_PreferredNeighbours = numPNeighbours;
    }

    protected void setUnchokingInterval (int interval) {
        m_UnchokingInterval = interval;
    }

    protected void setOptimisticUnchokingInterval (int interval) {
        m_OptimisticUnchokingInterval = interval;
    }

    protected void setFSize (int size) {
        m_FileSize = size;
    }

    protected void setPieceSize (int pieceSize) {
        m_PieceSize = pieceSize;
    }

    public int getNumPreferredNeighbours() {
        return m_PreferredNeighbours;
    }

    public int getUnchokingInterval() {
        return m_UnchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        return m_OptimisticUnchokingInterval;
    }

    public String getFileName() {
        return m_FileName;
    }

    public int getFileSize() {
        return m_FileSize;
    }

    public int getPieceSize() {
        return m_PieceSize;
    }
    
}