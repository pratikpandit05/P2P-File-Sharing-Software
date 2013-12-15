
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

public class StartAllClients {
    
    

    public static void main (String[] args) throws IOException {
        
        for (int index = 1000; index <= 1005; index++) {
        
            Runtime.getRuntime().exec (new String[] { "cmd.exe", "/c", "start java Peer " + index + ""});
            
            try { 
                Thread.sleep (1000);
            } catch (InterruptedException ex) {
                
            }
            
        }

        
    }
    
}
