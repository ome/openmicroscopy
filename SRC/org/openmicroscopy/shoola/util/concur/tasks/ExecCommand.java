/*
 * org.openmicroscopy.shoola.util.concur.tasks.ExecCommand
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

package org.openmicroscopy.shoola.util.concur.tasks;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.concur.ControlFlowObserver;

/** 
 * Encapsulates the service workflow.
 * The execution workflow is reduced to the same set of activities, regardless
 * of how the service was modeled (as a {@link Runnable}, {@link Invocation}, 
 * {@link Invocation} chain, or {@link MultiStepTask}):
 * <ul>
 *   <li>Perform each step in the computation &#151; by delegating to the 
 *   service object (an instance of {@link MultiStepTask}).</li>
 *   <li>Assemble partial results &#151; using a {@link ResultAssembler} object,
 *   and store the computation result into a {@link Future} object.</li>
 *   <li>Notify the observer (an instance of {@link ExecMonitor}) about the 
 *   computation state.</li>
 *   <li>Respond to cancellation.</li> 
 * </ul>
 * <p>The various <code>exec</code> methods of the {@link CmdProcessor} perform
 * adaptation as needed so that an <code>ExecCommand</code> is ultimately linked
 * to exactly one instance of {@link MultiStepTask} (service object), 
 * {@link ResultAssembler}, {@link Future}, and {@link ExecMonitor} (observer).
 * </p>
 * <p>Because an <code>ExecCommand</code> encapsulates the execution workflow,
 * all concrete {@link CmdProcessor}s have to do in order to execute a service
 * is to call the {@link #run() run} method.  However, no attempt should ever
 * be made to execute an <code>ExecCommand</code> twice or by more than one
 * thread &#151; an {@link Error} would be thrown.  A concrete 
 * {@link CmdProcessor} should also check the interrupted status of the
 * executing thread after the {@link #run() run} method returns as we never
 * clear the interrupted status &#151; which is set in the case of cancellation.
 * </p>
 *
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
class ExecCommand
    implements Runnable
{

    /** State flag to indicate that the command is ready to be executed. */
    static final int    READY = 0;
    
    /** 
     * State flag to indicate that the command is being executed.
     * That is, some thread is in the {@link #run() run} method. 
     */
    static final int    EXECUTING = 1;
    
    /** State flag to indicate that execution terminated. */
    static final int    FINISHED = 2;
    
    /** 
     * State flag to indicate that execution terminated because of 
     * cancellation. 
     */
    static final int    CANCELLED = 3;
    
    
    /** Keeps track of the current state. */
    private int     state;
    
    /** 
     * The thread that is currently executing the {@link #run() run} method.
     * This field is a valid reference only in the {@link #EXECUTING} state
     * and is set to <code>null</code> in all other states.
     */
    private Thread  executor;
    
    //NOTE: the above two fields need to be protected against concurrent 
    //access.  The following fields do not, as they are immutable.
    
    /** The service object. */
    private final MultiStepTask   task;
    
    /** 
     * Collects partial results and then assembles them into the final
     * result of the computation.
     */
    private final ResultAssembler assembler;
    
    /**
     * Stores the result of the computation (or any exception) and sends
     * cancellation signals.
     */
    private final Future          future;
    
    /** Notified about the progress and outcome of the computation. */
    private final ExecMonitor     observer;
    
    
    /**
     * Creates a new instance.
     * All passed arguments mustn't be <code>null</code>.
     * 
     * @param t The service object.
     * @param ra Collects partial results and assembles the final result.
     * @param f Stores the result of the computation (or any exception) and 
     *          sends cancellation signals.
     * @param em Observes the progress and outcome of the computation.
     */
    ExecCommand(MultiStepTask t, ResultAssembler ra, Future f, ExecMonitor em)
    {
        if (t == null) throw new NullPointerException("No task.");
        if (ra == null) throw new NullPointerException("No result assembler.");
        if (f == null) throw new NullPointerException("No future.");
        if (em == null) throw new NullPointerException("No execution monitor.");
        task = t;
        assembler = ra;
        future = f;
        observer = em;
        
        state = READY;
    }
    
    /**
     * Performs the execution workflow activities.
     */
    private void exec()
    {
        int step = 0;
        Object result = null, partialResult;
        Throwable abortCause = null;
        try {
            observer.onStart();
            while (!task.isDone()) {
                if (executor.isInterrupted()) throw new InterruptedException(); 
                partialResult = task.doStep();
                if (executor.isInterrupted()) throw new InterruptedException();
                assembler.add(partialResult);
                observer.update(++step);
            }
            result = assembler.assemble();
            observer.onEnd(result);
        } catch (InterruptedException ie) { 
            observer.onCancel();
        } catch (Throwable t) {
            abortCause = t;
            observer.onAbort(t);
        } finally {
            if (abortCause != null) future.setException(abortCause);
            else future.setResult(result);
        }
    }
    
    /**
     * Action dispatched right after the {@link #run() run} method is called.
     * If the state is {@link #READY} we set the {@link #executor} field
     * to the current thread and transition to {@link #EXECUTING}.
     * An {@link Error} will be thrown if the state is either {@link #EXECUTING}
     * or {@link #FINISHED}, as we do nothing if the state is 
     * {@link #CANCELLED}.  
     * 
     * @return <code>true</code> if the {@link #run() run} method should should
     *          proceed, <code>false</code> otherwise &#151; this happens when
     *          the state is {@link #CANCELLED}.
     */
    private synchronized boolean enterExecuting()
    {
        if (flowObs != null) flowObs.update(LOCK_ACQUIRED_BY_ENTER_EXECUTING);
        boolean enter = true;
        switch (state) {
            case READY:
                executor = Thread.currentThread();
                state = EXECUTING;
                break;
            case EXECUTING: throw new Error("Command is being executed.");
            case FINISHED: throw new Error("Can't re-execute a command.");
            case CANCELLED: enter = false;
        }
        return enter;
    }
    
    /**
     * Action dispatched just before the {@link #run() run} method returns.
     * Transitions to the {@link #FINISHED} state if no cancellation was
     * detected.  Otherwise the state will be {@link #CANCELLED}.
     */
    private synchronized void leaveExecuting()
    {
        if (flowObs != null) flowObs.update(LOCK_ACQUIRED_BY_LEAVE_EXECUTING);
        //Never clear interrupted status of executor.
        boolean cancelled = executor.isInterrupted();  
        executor = null;
        state = (cancelled ? CANCELLED : FINISHED);
    }
    
    /**
     * Cancels execution.
     * If the state is {@link #READY} then we just transition to 
     * {@link #CANCELLED} and notify any caller blocked on the future as well
     * as any observer.  If the state is already {@link #CANCELLED} or 
     * {@link #FINISHED}, we do nothing.
     * If the state is {@link #EXECUTING}, then we set the interrupted status
     * of the thread that is executing the {@link #run() run} method.  Whether
     * the interrupted status will be detected depends on the state of the
     * execution loop at the time when this state becomes visible to the 
     * executing thread.  We force a flush of the caller's thread working 
     * memory and a refresh of the executor's one the next time the interrupted
     * status is checked.  The executor checks the interrupted status at the 
     * beginning of every step in the computation &#151; however, in general, 
     * it's not possible to state the relative order of this method call and
     * the last check of the interrupted status in the executor thread; this is
     * the reason why cancellation could have no effect.    
     * That said, if the interrupted status is detected, then execution will
     * stop and the state will be transitioned to {@link #CANCELLED}.
     * Otherwise the computation proceeds to its natural ending, at which point
     * the state will be set to {@link #FINISHED}.  
     */
    synchronized void cancel()
    {
        if (flowObs != null) flowObs.update(LOCK_ACQUIRED_BY_CANCEL);
        switch (state) {
            case READY:
                state = CANCELLED;
                future.setResult(null);
                observer.onCancel();  //Trail call to avoid problems if exc.
                break;
            case EXECUTING:
                executor.interrupt();
                //Depending on current state of run loop the above may
                //either result in a transition to CANCELLED or FINISHED.
                break;
            case FINISHED:  //Do nothing.
            case CANCELLED:  //Do nothing.
        }
    }
    
    /**
     * Executes the service.
     */
    public void run()
    {
        if (!enterExecuting())  //Transition to EXECUTING or error/ignore.
            return;  //State is CANCELLED, run() should do nothing.
        try {
            exec();  //Perform workflow activities.
        } finally {  //Dispatch this action in any case.
            leaveExecuting();  //Transition to FINISHED or CANCELLED. 
        }
    }
    
    
/* 
 * ==============================================================
 *              Follows code to enable testing.
 * ==============================================================
 */
    
    static final int LOCK_ACQUIRED_BY_ENTER_EXECUTING = 100;
    static final int LOCK_ACQUIRED_BY_LEAVE_EXECUTING = 101;
    static final int LOCK_ACQUIRED_BY_CANCEL = 102;
    
    private ControlFlowObserver flowObs;
    
    void register(ControlFlowObserver obs) { flowObs = obs; }
    synchronized int getState() { return state; }
    MultiStepTask getTask() { return task; }
    ResultAssembler getAssembler() { return assembler; }
    Future getFuture() { return future; }
    ExecMonitor getObserver() { return observer; }
    
}
