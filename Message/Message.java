/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Message;

import java.io.Serializable;

public abstract class Message implements Serializable {
        protected byte[] message;
        protected static final long serialVersionUID = 1L;
        
        /**
         * default constructor to enable subclasses to compute message before setting bytes
         */
        public Message(){}
        
        public Message(byte[] msg)
        {
                this.message = msg;
        }
        
        public byte[] getBytes()
        {
                return message;
        }
}
