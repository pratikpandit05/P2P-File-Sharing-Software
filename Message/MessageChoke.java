/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.io.IOException;

public class MessageChoke extends MessageActual
{
    private static final long serialVersionUID = 5L;

    public MessageChoke() throws IOException, InterruptedException
    {
        super(MessageType.choke);
    }
}