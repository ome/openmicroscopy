/*
 * org.openmicroscopy.shoola.util.concur.AsyncByteBuffer
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.concur;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.concur.tasks.AsyncProcessor;
import org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor;
import org.openmicroscopy.shoola.util.concur.tasks.ExecException;
import org.openmicroscopy.shoola.util.concur.tasks.Future;
import org.openmicroscopy.shoola.util.mem.ReadOnlyByteArray;

/** 
 * A specialized byte buffer with an embedded asynchronous producer.
 * <p>The producer, which has to implement {@link ByteBufferFiller}, is run in
 * a separate thread so that the buffer can be filled up asynchronously with
 * respect to the threads that attempt to read from the buffer.  Ideally, the
 * producer object would be passed to the buffer and then de-referenced right
 * afterward &#151; this hand-off protocol ensures that, at any given time, the
 * buffer's internal thread is the only one to access that object.  If this is
 * not possible or desirable, then care must be taken in order to protect from
 * concurrent access to the producer's state &#151; which is possible if the
 * object is referenced within other threads as well.</p>
 * <p>This buffer will take care of concurrent read access to the underlying
 * data.  Threads that have a reference to this buffer can safely call its
 * public methods at any given time.  Note that threads that read from the
 * buffer can't be considered consumers as a read operation leaves the
 * underlying data untouched.  However, it's possible to refill the buffer 
 * with new data by specifying a new producer to the
 * {@link #setProducer(ByteBufferFiller) setProducer} method.  Because the 
 * read methods return objects that access the internal buffer directly (data
 * is not copied), after a call to the 
 * {@link #setProducer(ByteBufferFiller) setProducer} method, any
 * {@link ReadOnlyByteArray} object previously obtained from one of the read
 * methods should be considered no longer valid and de-referenced.  Moreover,
 * any pending <code>read</code> should be considered invalid.  In fact, the
 * new producer might write some data to the internal buffer before the slice
 * requested by such a <code>read</code> is constructed &#151; so the caller
 * could be getting the wrong data.  For this reason, it's important that all 
 * pending <code>reads</code> join back before calling the
 * {@link #setProducer(ByteBufferFiller) setProducer} method.</p>
 * <p>A final note.  This class will check the amount of data written by a
 * producer when it declares that the end of the stream has been reached.
 * An exception will be thrown if that amount is not the same as the one
 * declared by the producer via its 
 * {@link ByteBufferFiller#getTotalLength() getTotalLength} method.</p>
 *
 * @see org.openmicroscopy.shoola.util.concur.ByteBufferFiller
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class AsyncByteBuffer
{

    /** The internal buffer. */
    private final byte[]        buffer;
    
    /** 
     * The maximum amount of bytes that the producer will be asked to write
     * at a time during the filling loop.
     */
    private final int           BLOCK_SIZE;
    
    /** Provides a thread to run the producer loop. */
    private final CmdProcessor  cmdProcessor;
    
    /** 
     * Run in a separate thread to loop on a {@link ByteBufferFiller}
     * in order to write (at most) {@link #BLOCK_SIZE} bytes at a time
     * to {@link #buffer}. 
     */
    private ProducerLoop        producer;
    
    /**
     * Returned by {@link #cmdProcessor} when posting a request to 
     * execute the {@link #producer} loop.
     * We use it to cancel execution as well as rendezvous with the 
     * {@link #producer}.
     */
    private Future              producerHandle;
    
    
    /**
     * Encapsulates and parameterizes the algorithm common to all read methods.
     * 
     * @param offset    The starting position.
     * @param length    The length of the data segment.
     * @param timeout   The amount of milliseconds the calling thread should be
     *                  suspended if the specified data segment is not yet
     *                  available.  Pass <code>-1</code> for an unbounded wait 
     *                  or <code>0</code> to not wait at all. 
     * @return A read-only slice of the internal data buffer.
     * @throws BufferWriteException If an error ocurred while the producer wrote
     *                              data into the internal buffer.
     * @throws InterruptedException If the calling thread was interrupted.
     */
    private ReadOnlyByteArray doRead(int offset, int length, long timeout) 
        throws BufferWriteException, InterruptedException
    {
        ReadOnlyByteArray roba = null;
        ProducerLoop currentProducer;
        
        //Acquire this object's lock and get a reference to the current
        //producer.  We needto acquire the lock in order to avoid any
        //overlapping with setProducer().  However, we release it right
        //after grabbing the reference so to allow a setProducer() call
        //to proceed if we have to wait for data.
        synchronized (this) {
            currentProducer = producer;
        }
        
        if (currentProducer != null) {
            boolean available = false;
            
            //Check to see if currentProducer has written [off, len] yet.
            //If we suspend, no deadlock is possible b/c we've already
            //released this object's lock -- so any blocking is not going
            //to affect setProducer's callers.
            if (timeout < 0)  //Unbounded wait.  IllArgExc if bad [off, len].
                available = currentProducer.waitForData(offset, length);
            else  //No wait if timeout=0 or bounded wait otherwise.
                available =  //IllArgExc if bad [off, len].
                    currentProducer.waitForData(offset, length, timeout);
            
            //If a new producer's been set in the mean time and this new
            //producer has already written some data, then it's possible 
            //that the returned buffer slice contain the wrong data --
            //the caller is expecting data coming from currentProducer.
            //For this reason, it's important that all pending reads join
            //back before calling setProducer() -- this is the caller's
            //responsibility though.
            if (available)
                roba = readFromBuffer(offset, length);
        }
        return roba;
    }
    
    /**
     * Encapsulates read access to {@link #buffer} to make sure that the
     * most recently written data is read in.
     * 
     * @param offset    The starting position.
     * @param length    The length of the data segment.
     * @return A read-only slice of the internal data buffer.
     */
    private ReadOnlyByteArray readFromBuffer(int offset, int length)
    {
        synchronized (buffer) {
            //Refresh consumer thread's working memory.  The values 
            //last written by the producer will now be visible.
            return new ReadOnlyByteArray(buffer, offset, length);
        }
    }
    
    /**
     * Encapsulates write access to {@link #buffer} to make sure that the
     * written data is flushed from the producer thread's working memory.
     * 
     * @param producer  The producer that will be asked to write 
     *                  {@link #BLOCK_SIZE} bytes into {@link #buffer}.
     * @param offset    The starting position.
     * @return  The write length as returned by <code>producer</code>.
     * @throws BufferWriteException Thrown by <code>producer</code>.
     */
    int writeToBuffer(ByteBufferFiller producer, int offset) 
        throws BufferWriteException
    {
        synchronized (buffer) {
            //Flush producer thread's working memory.  Next time this lock
            //will be acquired, the results of the following write will be
            //visible to the thread acquiring the lock.
            return producer.write(buffer, offset, BLOCK_SIZE);
        }
    }
    
    /**
     * Creates a new instance.
     * Use the {@link #setProducer(ByteBufferFiller) setProducer} method to
     * specify a producer and start an asynchronous data retrieval.
     * The <code>size</code> parameter specifies the size of the internal byte
     * buffer.  That is, how much space will be available to any producer.  
     * This is not to be confused with the amount of data that a producer 
     * can write into the buffer, that is the value returned by its
     * {@link ByteBufferFiller#getTotalLength() getTotalLength} method.
     * 
     * @param size  The the size of the internal byte buffer.  Must be positive. 
     * @param blockSize  The maximum amount of bytes that the producer will
     *                   be asked to write at a time during the filling loop.
     *                   Must be positive and no greater than <code>size</code>.
     */
    public AsyncByteBuffer(int size, int blockSize)
    {
        if (size < 1)
            throw new IllegalArgumentException("Non-positive size: "+size+".");
        if (blockSize < 1 || size < blockSize)
            throw new IllegalArgumentException(
                "Invalid blockSize: "+blockSize+".");
        buffer = new byte[size];
        BLOCK_SIZE = blockSize;
        cmdProcessor = new AsyncProcessor();
    }
    
    /**
     * Creates a new instance.
     * Use the {@link #setProducer(ByteBufferFiller) setProducer} method to
     * specify a producer and start an asynchronous data retrieval.
     * The <code>size</code> parameter specifies the size of the internal byte
     * buffer.  That is, how much space will be available to any producer.  
     * This is not to be confused with the amount of data that a producer 
     * can write into the buffer, that is the value returned by its
     * {@link ByteBufferFiller#getTotalLength() getTotalLength} method.
     * 
     * @param size  The the size of the internal byte buffer.  Must be positive. 
     * @param blockSize  The maximum amount of bytes that the producer will
     *                   be asked to write at a time during the filling loop.
     *                   Must be positive and no greater than <code>size</code>.
     * @param cp    Will be used to get a thread to run the producer's loop.
     *              Mustn't be <code>null</code>.
     */
    public AsyncByteBuffer(int size, int blockSize, CmdProcessor cp)
    {
        if (size < 1)
            throw new IllegalArgumentException("Non-positive size: "+size+".");
        if (blockSize < 1 || size < blockSize)
            throw new IllegalArgumentException(
                "Invalid blockSize: "+blockSize+".");
        if (cp == null) throw new NullPointerException("No command processor.");
        buffer = new byte[size];
        BLOCK_SIZE = blockSize;
        cmdProcessor = cp;
    }
    
    /**
     * Creates and start a new loop to allow <code>p</code> to write its
     * payload into the internal buffer.
     * The loop is run in a separate thread.  
     * If there's already a producer running, then this method will cancel its
     * loop and wait for it to terminate.  Then, if the passed producer is not
     * <code>null</code>, a new loop is created and started for it.  Otherwise,
     * this method just returns after clearing the previous producer's loop. 
     * Note that if there was already a producer, invoking this method will
     * cause the internal buffer to be refilled with new data.  Because the
     * read methods return objects that access the internal buffer directly 
     * (data is not copied), after calling this method, any 
     * {@link ReadOnlyByteArray} object previously obtained from one of the
     * read methods should be considered no longer valid and de-referenced.
     * 
     * @param p The producer that will write data into the buffer.
     *          The amount of data that the producer can write into
     *          the buffer, that is the value returned by 
     *          {@link ByteBufferFiller#getTotalLength()}, mustn't 
     *          exceed the size returned by {@link #getSize()}.
     * @throws InterruptedException If this thread was interrupted.
     */
    public void setProducer(ByteBufferFiller p)
        throws InterruptedException
    {
        synchronized (this) {  //Serialize calls.
            if (producer != null) {  //Then we need to clean up. 
                //First cancel execution.  Any pending read will throw an
                //BufferWriteException if this succeeds while the read is
                //waiting for data.
                producerHandle.cancelExecution();
                
                //Then rendezvous with the producer.  We have to wait for the
                //producer to terminate before we can possibly start a new
                //one -- otherwise two threads would be writing to the buffer.
                try {
                    producerHandle.getResult();
                } catch (ExecException ee) {  
                    //Ignore, we're going to discard this producer anyway.
                }
                producer = null;  //Get rid of prevoius producer.
            }
            
            //If p is null, then we're done.  The caller just wanted to get
            //rid of the previous producer, if any.
            if (p == null) return;  
            
            //We've got a new producer p, so start it's loop.
            producer = new ProducerLoop(this, p);
            producerHandle = cmdProcessor.exec(producer, producer);
        }
    }
    
    /**
     * Attempts to read <code>length</code> bytes from the buffer, starting from
     * the <code>offset</code> position.
     * This method will block if the requested data is not available.  So the
     * caller is suspended until the data becomes available or an error is
     * detected. 
     * The passed arguments must define an interval 
     * <code>[offset, offset+length]</code> in <code>[0, limit]</code>, 
     * <code>limit</code> being the amount of data that the producer can write 
     * into the buffer, that is the value returned by its  
     * {@link ByteBufferFiller#getTotalLength() getTotalLength} method.
     * 
     * @param offset    The starting position.
     * @param length    The length of the data segment.
     * @return A read-only slice of the internal data buffer.
     * @throws BufferWriteException If an error ocurred while the producer wrote
     *                              data into the internal buffer.
     * @throws InterruptedException If this thread was interrupted.
     */
    public ReadOnlyByteArray read(int offset, int length) 
        throws BufferWriteException, InterruptedException
    {
        return doRead(offset, length, -1);  //-1 for unbounded wait.
    }
    
    /**
     * Attempts to read <code>length</code> bytes from the buffer, starting from
     * the <code>offset</code> position and waiting at most <code>timeout</code>
     * milliseconds for the data to become available.
     * This method doesn't block forever.  If the requested data is not yet
     * available after the <code>timeout</code> elapses, then <code>null</code>
     * is returned instead.
     * The passed arguments must define an interval 
     * <code>[offset, offset+length]</code> in <code>[0, limit]</code>, 
     * <code>limit</code> being the amount of data that the producer can write 
     * into the buffer, that is the value returned by 
     * {@link ByteBufferFiller#getTotalLength()}.
     * 
     * @param offset    The starting position.
     * @param length    The length of the data segment.
     * @param timeout   The amount of milliseconds the calling thread should be
     *                  suspended if the specified data segment is not yet
     *                  available.  If the passed value is not positive, then 
     *                  the caller won't be suspended at all. 
     * @return A read-only slice of the internal data buffer.
     * @throws BufferWriteException If an error ocurred while the producer wrote
     *                              data into the internal buffer.
     * @throws InterruptedException If this thread was interrupted.
     */
    public ReadOnlyByteArray read(int offset, int length, long timeout) 
        throws BufferWriteException, InterruptedException
    {
        if (timeout <= 0) timeout = 0;  //<0 reserved for unbounded wait.
        return doRead(offset, length, timeout);  
    }
    
    /**
     * Returns the capacity of this buffer.
     * That is, the size of the internal byte buffer.  This is not to be
     * confused with the amount of data that the producer can write into
     * the buffer, that is the value returned by its
     * {@link ByteBufferFiller#getTotalLength() getTotalLength} method.
     * 
     * @return  The buffer's size.
     */
    public int getSize()
    {
        return buffer.length;
    }
    
}
