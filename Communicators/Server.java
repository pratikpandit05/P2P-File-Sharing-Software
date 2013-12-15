/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Communicators;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class Server implements Runnable
{
    private ServerSocket serverSocket;
    private CommunicationHandler myTransceiver;
    private static final Logger acitivityLogger = Logger.getLogger("A");
    private static final int ACCEPT_TIMEOUT = 1000;
    
    public Server(String hostName, int port, CommunicationHandler myTransceiver) throws UnknownHostException, IOException
    {
        this.serverSocket = new ServerSocket(port, 0, InetAddress.getByName(hostName));
        this.serverSocket.setSoTimeout(ACCEPT_TIMEOUT);
        this.myTransceiver = myTransceiver;
        Server.acitivityLogger.info("Server is ready to listen now at address : " 
                + this.serverSocket.getLocalSocketAddress().toString() + " and port : " + this.serverSocket.getLocalPort());
    }

    @Override
    public void run()
    {
        while(true)
        {
            Socket aClientSocket = null;
            // wait for connections forever
            try
            {
                aClientSocket = this.serverSocket.accept();
                
                Server.acitivityLogger.info("Server received a client request!");                
                //start an Event manager in another thread for further communication
                (new Thread (new PeerProcessCommunicator (new Client (aClientSocket, myTransceiver), myTransceiver))).start();
            }
            catch(InterruptedIOException iioex)
            {
                if(myTransceiver.getTorrentFile().IsQuittingPossible())
                {
                    try
                    {
                        this.serverSocket.close();
                        break;
                    } catch (IOException e){
                        
                        e.printStackTrace();
                        
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}