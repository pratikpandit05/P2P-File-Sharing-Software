/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.io.IOException;

public class MessageNotInterested extends MessageActual
{
    private static final long serialVersionUID = 8L;

    public MessageNotInterested() throws IOException, InterruptedException
    {
        super(MessageType.notInterested);
    }
}