/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.io.IOException;

public class MessageBitField extends MessagePayload
{
    private static final long serialVersionUID = 12L;

    public MessageBitField(byte[] bitField) throws IOException, InterruptedException
    {
        super(MessageType.bitfield, bitField);
    }
};