/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.io.IOException;
import Utilities.Utilities;

public class MessageRequest extends MessagePayload
{
    private static final long serialVersionUID = 11L;

    public MessageRequest(int requestedPiece) throws IOException, InterruptedException
    {
        super(MessageType.request, Utilities.getBytes(requestedPiece));
    }
};
