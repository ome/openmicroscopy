/*
 * org.openmicroscopy.shoola.util.concur.ProducerLoop
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
import org.openmicroscopy.shoola.util.concur.tasks.ExecMonitor;
import org.openmicroscopy.shoola.util.concur.tasks.MultiStepTask;

/** 
 * The asynchronous producer loop of an {@link AsyncByteBuffer}.
 * Implements a {@link MultiStepTask} service to have the producer (an instance
 * of {@link ByteBufferFiller}, handed off by the {@link AsyncByteBuffer} at
 * creation time) fill up the {@link AsyncByteBuffer}'s internal buffer.  Also
 * keeps track of the loop state so that the {@link AsyncByteBuffer} can query
 * it in order to find out whether a given data segment is available (that is,
 * it has been written to the buffer) and possibly wait until it becomes 
 * available or the data is discarded &#151; because of an exception thrown by
 * the producer or because the loop is cancelled by the {@link AsyncByteBuffer}
 * itself.
 *
 * @see org.openmicroscopy.shoola.util.concur.AsyncByteBuffer 
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
class ProducerLoop
    implements MultiStepTask, ExecMonitor
{
    
    /**
     * State flag to denote the <i>Filling</i> state.
     * In this state the producer thread runs the <code>doStep</code> loop to
     * fill up the {@link AsyncByteBuffer}'s internal buffer.  This state is
     * characterized by the following constraints: 
     * <code>bytesWritten &lt; {@link #PAYLOAD}</code> and
     * <code>exc = null</code>.
     */
    static final int               FILLING = 0;
    
    /**
     * State flag to denote the <i>Done</i> state.
     * This state is reached when the producer thread has finished running the 
     * <code>doStep</code> loop and {@link #PAYLOAD} bytes have been written to
     * the {@link AsyncByteBuffer}'s internal buffer.  This state is 
     * characterized by the following constraints: 
     * <code>bytesWritten = {@link #PAYLOAD}</code> and
     * <code>exc = null</code>.
     */
    static final int                DONE = 1;
    
    /**
     * State flag to denote the <i>Data Discarded</i> state.
     * This state is reached if the producer thread exits abnormally the 
     * <code>doStep</code> loop.  This can happen because of an exception
     * thrown by the {@link #producer} object, the loop is cancelled, or
     * an amount of bytes different from {@link #PAYLOAD} have been written
     * to the {@link AsyncByteBuffer}'s internal buffer.  This state is 
     * characterized by the following constraint: <code>exc != null</code>.
     */
    static final int                 DATA_DISCARDED = 2;
    
    /** 
     * The object that actually writes bytes into the {@link AsyncByteBuffer}'s 
     * internal buffer.
     */
    private final ByteBufferFiller    producer;
    
    /**
     * Link back to the {@link AsyncByteBuffer} this instance is working with. 
     */
    private final AsyncByteBuffer    buffer;
    
    /**
     * Total amount of bytes that can ever be produced by the {@link #producer}.
     * This is the value returned by {@link ByteBufferFiller#getTotalLength()},
     * which we grab only once at creation time so to avoid problems if a 
     * buggy producer returns different values in different calls to the
     * <code>getTotalLength</code> method.  
     */
    private final int            PAYLOAD;
    
    //State written by producer thread and queried by consumer threads.
    /**
     * The amount of bytes that have currently been written to the 
     * {@link AsyncByteBuffer}'s internal buffer.  This field is written 
     * by the producer thread and indirectly queried by consumer threads
     * via the {@link AsyncByteBuffer}.
     */
    private int                  bytesWritten;
    
    /**
     * Details why data was discarded if the loop transitions to the 
     * {@link #DATA_DISCARDED} state.  This field is written by the 
     * producer thread and indirectly queried by consumer threads via
     * the {@link AsyncByteBuffer}.
     */
    private BufferWriteException discardCause;
    
    /**
     * Keeps track of the loop state.  This field is written by the 
     * producer thread and indirectly queried by consumer threads via
     * the {@link AsyncByteBuffer}.
     */
    private int                  state;
    
    /**
     * Tells whether {@link #doStep()} should be invoked.
     * Only used by the producer thread, it is initialized to <code>false</code>
     * and then latches to <code>true</code> when {@link #doStep()} has
     * written the last chunk of bytes. 
     */
    private boolean              done;
    
    
    /**
     * Creates a new instance.
     * 
     * @param buffer    The {@link AsyncByteBuffer} object that this new
     *                  <code>ProducerLoop</code> has to work for.
     *                  Mustn't be <code>null</code>.
     * @param producer  The object that will actually write to the buffer.
     *                  Mustn't be <code>null</code>.
     */
    ProducerLoop(AsyncByteBuffer buffer, ByteBufferFiller producer)
    {
        //Links.
        if (buffer == null) throw new NullPointerException("No buffer.");
        if (producer == null) throw new NullPointerException("No producer.");
        this.buffer = buffer; 
        this.producer = producer;
        
        //Grab the total lenght of the byte stream now to avoid problems
        //later if the producer is buggy.  Don't bother to check that is
        //greater than 0 -- if not, doStep will fail t the first call. 
        PAYLOAD = producer.getTotalLength(); 
        if (PAYLOAD < 1) 
            throw new IllegalArgumentException(
                "producer.getTotalLength() didn't return a positive value: "+
                PAYLOAD+".");
        
        //Initialize state (just for the sake of clarity).
        bytesWritten = 0;  
        discardCause = null;
        done = false;
        
        //Set logical state.
        state = FILLING;
    }
    
    /**
     * Increases the {@link #bytesWritten} field by the amount of bytes written
     * by the last call to {@link #doStep()}.
     * Note that this method acquires this object's lock so to force a flush
     * of the working memory and make the new field value available to 
     * consumer threads.  It also notifies any consumer thread waiting for
     * a data segment.
     * 
     * @param writeLength  The amount of bytes lastly written by 
     *                     {@link #doStep()}.
     */
    private synchronized void updateBytesWritten(int writeLength) 
    { 
        if (flowObs != null) flowObs.update(LOCK_ACQUIRED);
        bytesWritten += writeLength;
        notifyAll();  //B/c there might be more than 1 consumer waiting.
    }
    
    /**
     * Verifies that <code>[offset, length]</code> is a valid interval and
     * falls within <code>[0, {@link #PAYLOAD}]</code>.  Failing that, an 
     * {@link IllegalArgumentException} is thrown.
     *  
     * @param offset    The start of the data segment.
     * @param length    The length of the data segment.
     */
    private void checkInterval(int offset, int length)
    {
        if (offset < 0 || length < 0 || PAYLOAD < offset+length)
        throw new IllegalArgumentException(
            "Illegal data segment: [offset="+offset+", offset+length="+
            (offset+length)+"] not in [0, PAYLOAD="+PAYLOAD+"].");
    }
    
    /**
     * Checks if the specified data segment is available and optionally waits
     * <code>timeout</code> milliseconds to evaluate said condition again if
     * the state is {@link #FILLING}.
     * If the state is {@link #DATA_DISCARDED}, then there's no point in
     * checking and {@link #discardCause} is thrown instead.  If the state
     * is {@link #DONE}, then there's no point in waiting as no more bytes 
     * will ever be written, and we just verify the above mentioned condition
     * &#151; which should always evaluate to <code>true</code> in this case.
     * This method assumes the caller already owns the object's monitor &#151;
     * that is, call this method only within a <code>synchronized</code>
     * block.  Another precondition which is assumed to hold <code>true</code>
     * is that <code>[offset, length]</code> is a valid interval within
     * <code>[0, {@link #PAYLOAD}]</code>.
     * 
     * @param offset    The start of the data segment.
     * @param length    The length of the data segment.
     * @param timeout   The amount of milliseconds the calling thread should be
     *              suspended if the data segment hasn't yet been retrieved 
     *              &#151; possible if the state is {@link #FILLING}.
     *              Pass <code>0</code> for an unbounded wait or <code>-1</code>
     *              to not wait at all.
     * @return <code>true</code> if <code>[offset, length]</code> falls within
     *          <code>[0, {@link #PAYLOAD}]</code>, <code>false</code> 
     *          otherwise.
     * @throws BufferWriteException If the data has been discarded.
     * @throws InterruptedException If the current thread has been interrupted
     *                              while waiting for the condition to become
     *                              <code>true</code>.
     */
    private boolean isAvailable(int offset, int length, long timeout)
        throws BufferWriteException, InterruptedException
    { //We own the object's monitor and [off,len] in [0,PAYLOAD]. 
        boolean available = (offset+length <= bytesWritten);
        switch (state) {
            case DATA_DISCARDED: throw discardCause;
            case DONE:  //[off,len] in [0,PAYLOAD] and bytesWritten==PAYLOAD.
                return available;  //So this must be true.
            case FILLING:
                if (available) return true;  //Don't wait if already true.
                if (0 <= timeout) wait(timeout); //Wait for unbounded time if 0.
                //Else don't wait at all if timeout < 0.
        }
        //State is FILLING (see above switch).  Re-evaluate condition.
        return (offset+length <= bytesWritten);
    }
    
    /**
     * Atomically checks if the specified data segment is available and 
     * optionally waits <code>timeout</code> milliseconds for said condition
     * to be satisfied.
     * 
     * @param offset    The start of the data segment.
     * @param length    The length of the data segment.
     * @param timeout   The amount of milliseconds the calling thread should be
     *              suspended if the data segment hasn't yet been retrieved.
     *              If the passed value is not positive, then the caller is not
     *              suspended at all.
     * @return <code>true</code> if <code>[offset, length]</code> falls within
     *          <code>[0, {@link #PAYLOAD}]</code>, <code>false</code> 
     *          otherwise.
     * @throws BufferWriteException If the data has been discarded.
     * @throws InterruptedException If the current thread has been interrupted
     *                              while waiting for the condition to become
     *                              <code>true</code>.
     * @throws IllegalArgumentException If <code>[offset, length]</code> is not
     *                                  a valid interval or doesn't fall within
     *                                  <code>[0, {@link #PAYLOAD}]</code>. 
     */
    boolean waitForData(int offset, int length, long timeout)
        throws BufferWriteException, InterruptedException
    {   
        //Don't acquire the lock if the current thread has been interrupted.
        //Pointless to have this thread compete to acquire the lock and then
        //possibly suspend when it should stop its activity instead.
        if (Thread.interrupted()) throw new InterruptedException();
        
        checkInterval(offset, length);  //Make sure [off,len] in [0,PAYLOAD].
        
        //Get the lock.
        synchronized (this) {
            if (flowObs != null) flowObs.update(LOCK_ACQUIRED);
            if (isAvailable(offset, length, -1))  //Check without waiting. 
                return true;
            if (timeout <= 0)  //No wait.  Data not available so return false. 
                return false;
            
            //Release lock and suspend until allowed to proceed or timed-out. 
            long start = System.currentTimeMillis(), delta = timeout;
            while (true) {
                if (isAvailable(offset, length, delta)) return true;
                delta = (start+timeout) - System.currentTimeMillis();
                if (delta <= 0) return false;
            }
        } 
    }
    
    /**
     * Atomically checks if the specified data segment is available and, if
     * not, waits for said condition to be satisfied.
     * 
     * @param offset    The start of the data segment.
     * @param length    The length of the data segment.
     * @return <code>true</code>.
     * @throws BufferWriteException If the data has been discarded.
     * @throws InterruptedException If the current thread has been interrupted
     *                              while waiting for the condition to become
     *                              <code>true</code>.
     * @throws IllegalArgumentException If <code>[offset, length]</code> is not
     *                                  a valid interval or doesn't fall within
     *                                  <code>[0, {@link #PAYLOAD}]</code>. 
     */
    boolean waitForData(int offset, int length)
        throws BufferWriteException, InterruptedException
    {
        //Don't acquire the lock if the current thread has been interrupted.
        //Pointless to have this thread compete to acquire the lock and then
        //possibly suspend when it should stop its activity instead.
        if (Thread.interrupted()) throw new InterruptedException();
        
        checkInterval(offset, length);  //Make sure [off,len] in [0,PAYLOAD].
        
        //Get the lock, but release it and suspend if not allowed to proceed.
        synchronized (this) {
            if (flowObs != null) flowObs.update(LOCK_ACQUIRED);
            
            //Wait until data is available or aborted/cancelled.
            while (!isAvailable(offset, length, 0)) ;  
        }
        return true;
    }
    
    /**
     * Run in the producer thread to fill up the {@link AsyncByteBuffer}'s 
     * internal buffer.  Each call writes a chunk of bytes up to a value
     * specified by {@link AsyncByteBuffer} and then updates the 
     * {@link #bytesWritten} field.  If more than {@link #PAYLOAD} bytes
     * have been written, then a {@link BufferWriteException} is thrown.
     * 
     * @see MultiStepTask#doStep()
     */
    public Object doStep()
        throws Exception
    {
        if (PAYLOAD < bytesWritten) 
            throw new BufferWriteException("Overflow: PAYLOAD="+PAYLOAD+
                    " shouldn't be exceeded ("+bytesWritten+
                    " bytes written so far).");
        int writeLength = buffer.writeToBuffer(producer, bytesWritten);
        if (writeLength != -1) updateBytesWritten(writeLength);
        else done = true;
        return null;  //Result written directly into buffer.
    }
    
    /**
     * Implemented as specified by the {@link MultiStepTask} interface.
     * @see MultiStepTask#isDone()
     */
    public boolean isDone() { return done; }
    
    
    /**
     * No-op implementation.
     * Required by the {@link ExecMonitor} interface but not actually needed.
     * @see ExecMonitor#onStart()
     */
    public void onStart() {}
    
    /**
     * No-op implementation.
     * Required by the {@link ExecMonitor} interface but not actually needed.
     * Replaced by {@link #updateBytesWritten(int)}.
     * @see ExecMonitor#update(int)
     */
    public void update(int step) {}
    
    /**
     * Just calls {@link #onAbort(Throwable)} with a 
     * {@link BufferWriteException} argument to specifiy that the 
     * producer loop was cancelled.
     * @see ExecMonitor#onCancel()
     */
    public void onCancel() 
    {   
        onAbort(new BufferWriteException("Data retrieval cancelled."));
        /* NOTE 1: onAbort and onCancel are mutually exclusive, either one
         * is invoked by the service execution workflow.
         * NOTE 2: PAYLOAD bytes could have been written to the buffer if
         * cancellation is detected after the last call to doStep().
         */
    }
    
    /**
     * Transitions this object to the {@link #DATA_DISCARDED} state.
     * Note that this method acquires this object's lock so to force a flush
     * of the working memory and make the exception available to consumer 
     * threads.  It also notifies any consumer thread waiting for a data 
     * segment.
     * 
     * @param cause Details why the data retrieval was aborted.  If not an
     *              instance of {@link BufferWriteException}, then it is
     *              wrapped with an instance of said exception.
     * @see ExecMonitor#onAbort(Throwable)
     */
    public synchronized void onAbort(Throwable cause) 
    {
        if (flowObs != null) flowObs.update(LOCK_ACQUIRED);
        if (cause instanceof BufferWriteException)
            discardCause = (BufferWriteException) cause;
        else  //Can only be RuntimeException.
            discardCause = new BufferWriteException(  
                "Unexpected runtime exception.", cause);
        state = DATA_DISCARDED;
        notifyAll();  //B/c there might be more than 1 consumer waiting.
    }
    
    /**
     * Transitions this object to the {@link #DONE} or {@link #DATA_DISCARDED}
     * state.  If {@link #PAYLOAD} equals {@link #bytesWritten} then we go to
     * the {@link #DONE} state, otherwise to the {@link #DATA_DISCARDED} state. 
     * Note that this method acquires this object's lock so to force a flush
     * of the working memory and make the new state available to consumer 
     * threads.  It also notifies any consumer thread waiting for a data 
     * segment.
     * @see ExecMonitor#onEnd(Object)
     */
    public synchronized void onEnd(Object result) 
    {
        if (flowObs != null) flowObs.update(LOCK_ACQUIRED);
        if (bytesWritten == PAYLOAD) state = DONE;
        else { 
        //bytesWritten < PAYLOAD.  In fact, if we're here then doStep must have
        //set done to true.  This impies PAYLOAD <= bytesWritten (otherwise an
        //exception would have been thrown and onAbort called instead).
            String msg = "Underflow: PAYLOAD="+PAYLOAD+" hasn't been reached "+
                            "("+bytesWritten+" bytes written in total).";
            discardCause = new BufferWriteException(msg);
            state = DATA_DISCARDED;
        }
        notifyAll();  //B/c there might be more than 1 consumer waiting.
    }

    
/* 
 * ==============================================================
 *              Follows code to enable testing.
 * ==============================================================
 */  
    
    static final int LOCK_ACQUIRED = 100;
    
    private ControlFlowObserver flowObs;
    
    void register(ControlFlowObserver obs) { flowObs = obs; }
    int getPayload() { return PAYLOAD; }
    synchronized int getState() { return state; }
    synchronized int getBytesWritten() { return bytesWritten; }
    synchronized BufferWriteException getDiscardCause() { return discardCause; }
    
}
