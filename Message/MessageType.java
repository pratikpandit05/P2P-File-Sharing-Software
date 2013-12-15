/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

/**
 *
 * @author Prajakta
 */
import java.io.Serializable;

public enum MessageType implements Serializable
{
    choke(0), 
    unchoke(1), 
    interested(2), 
    notInterested(3), 
    have(4), 
    bitfield(5), 
    request(6), 
    piece(7);
    
    private byte msgType;

    private MessageType(int msgType)
    {
        this.msgType = (byte)msgType;
    }
    
    public byte getMessageType()
    {
        return this.msgType;
    }
    
    public static MessageType getMessageType(byte value)
    {
        if(value == 0)
            return MessageType.choke;
        if (value == 1)
            return MessageType.unchoke;
        if(value == 2)
            return MessageType.interested;
        if(value == 3)
            return MessageType.notInterested;
        if(value == 4)
            return MessageType.have;
        if(value == 5)
            return MessageType.bitfield;
        if(value == 6)
            return MessageType.request;
        if(value == 7)
            return MessageType.piece;
        
        return null;
    }
    
    public static int getMessageTypeLength()
    {
        return 1;
    }
}
