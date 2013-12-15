/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;


import java.io.IOException;
import Utilities.Utilities;


public class MessageHave extends MessagePayload
{
    private static final long serialVersionUID = 9L;

    public MessageHave(int pieceIndex) throws IOException, InterruptedException
    {
        super(MessageType.have, Utilities.getBytes(pieceIndex));
    }
};