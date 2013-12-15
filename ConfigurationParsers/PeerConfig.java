/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ConfigurationParsers;

public class PeerConfig
{
    private String hostName;
    private int listeningPort;
    private boolean hasCompleteFile;

    public PeerConfig()
    {
    } 

    public PeerConfig(String hostName, int listeningPort, boolean hasFile)
    {
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasCompleteFile = hasFile;
    }

    public String getHostName()
    {
        return hostName;
    }

    public int getListeningPort()
    {
        return listeningPort;
    }

    public boolean hasCompleteFile()
    {
        return hasCompleteFile;
    }

    protected void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    protected void setListeningPort(int listeningPort)
    {
        this.listeningPort = listeningPort;
    }

    protected void setHasFile(boolean hasFile)
    {
        this.hasCompleteFile = hasFile;
    }
}