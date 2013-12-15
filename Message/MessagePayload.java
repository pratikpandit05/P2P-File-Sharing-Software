
package Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


import Utilities.Utilities;

public class MessagePayload extends MessageActual
{
    private static final long serialVersionUID = 4L;

    public MessagePayload(MessageType msgType, byte[] payload) throws IOException, InterruptedException
    {
        super(payload.length + MessageType.getMessageTypeLength(), msgType);  //size should be the final one which includes type length and payload length
        ByteArrayOutputStream baos = Utilities.getStreamHandle();
        baos.write(super.message);
        baos.write(payload);
        super.message = baos.toByteArray();
        Utilities.returnStreamHandle();
    }
    
    public MessagePayload(int msgLength, MessageType msgType) throws IOException, InterruptedException
    {
        super(msgLength + MessageType.getMessageTypeLength(), msgType);
    }
    
}
