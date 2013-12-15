/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import Utilities.Utilities;



public class MessageActual extends Message
{
        protected static final long serialVersionUID = 2L;
        private MessageType msgType = null;
    private int msgLength;
        
        public MessageActual(MessageType msgType) throws InterruptedException, IOException
        {
                this(MessageType.getMessageTypeLength(), msgType);
        }
        
        public MessageActual(int msgLength, MessageType msgType) throws InterruptedException, IOException
        {
            this.msgType = msgType;
        this.msgLength = msgLength;
        ByteArrayOutputStream baos = Utilities.getStreamHandle();
        baos.write(Utilities.getBytes(this.msgLength));
        baos.write(msgType.getMessageType());
        super.message = baos.toByteArray();
        Utilities.returnStreamHandle();
        }

    public MessageType getMsgType()
    {
        return msgType;
    }
    
    public int getMessageLength()
    {
        return this.msgLength;
    }
}
