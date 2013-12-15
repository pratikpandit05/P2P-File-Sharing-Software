/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import Utilities.Utilities;

public class MessagePiece extends MessagePayload
{
    private static final long serialVersionUID = 10L;

    public MessagePiece(int pieceIndex, byte[] data) throws IOException, InterruptedException
    {
        //the length in super(...) call indicates length of pieceIndex + data
        super(data.length + 4, MessageType.piece);
        ByteArrayOutputStream baos = Utilities.getStreamHandle();
        baos.write(super.message);
        baos.write(Utilities.getBytes(pieceIndex));
        baos.write(data);
        super.message = baos.toByteArray();
        Utilities.returnStreamHandle();
    }
        
};
