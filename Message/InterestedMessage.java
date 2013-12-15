/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.io.IOException;

public class InterestedMessage extends MessageActual
{
    private static final long serialVersionUID = 7L;

    public InterestedMessage() throws IOException, InterruptedException
    {
        super(MessageType.interested);
    }

}