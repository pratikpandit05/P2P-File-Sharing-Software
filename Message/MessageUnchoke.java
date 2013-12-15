/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.io.IOException;

public class MessageUnchoke extends MessageActual
{
    private static final long serialVersionUID = 6L;

    public MessageUnchoke() throws IOException, InterruptedException
    {
        super(MessageType.unchoke);
    }
}

