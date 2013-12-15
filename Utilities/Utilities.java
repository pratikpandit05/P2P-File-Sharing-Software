/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Utilities
{
    private static ByteArrayOutputStream streamHandle = new ByteArrayOutputStream();
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition borrowedStream = lock.newCondition();
    private static boolean isStreamInUse = false;
    
    public static byte[] getBytes(int i)
    {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i);

        return result;
    }
    
    public static synchronized ByteArrayOutputStream getStreamHandle() throws InterruptedException
    {
        lock.lock();
        try
        {
            if(isStreamInUse)
            {
                borrowedStream.await();
            }
            isStreamInUse = true;
            streamHandle.reset();
            return streamHandle;
        }
        finally
        {
            lock.unlock();
        }
    }
    
    public static void returnStreamHandle()
    {
        lock.lock();
        try
        {
            borrowedStream.signal();
            isStreamInUse = false;
        }
        finally
        {
            lock.unlock();
        }
    }

    public static int getIntegerFromByteArray(byte[] array, int startIndex)
    {
        ByteBuffer buffer = ByteBuffer.wrap(array, startIndex, 4);
        return buffer.getInt();
    }
}