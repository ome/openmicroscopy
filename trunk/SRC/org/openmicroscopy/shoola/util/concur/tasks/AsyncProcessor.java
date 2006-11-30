/*
 * org.openmicroscopy.shoola.util.concur.tasks.AsyncProcessor
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * A concrete {@link CmdProcessor} that supports a thread-per-service
 * model. 
 * Each service is executed in its own thread which is created when the 
 * service object is handed off to one of the <code>exec</code> methods
 * and disposed when the service exits.
 * <p>In this model services are not queued up for execution, so it's not
 * possible that dependencies among services can cause deadlocks &#151; at
 * least until threads can keep on being created at the same pace of the
 * arrival rate of requests to execute services, that is, invocations of
 * the <code>exec</code> methods.</p>
 * <p>On the other hand, you might have to face the possibility of resource
 * exhaustion, depending on arrival rate and duration of services &#151; this
 * could be something to consider very carefully if your JVM doesn't enforce
 * any internal thread pooling.  Also take into account that having too many
 * threads kicking around at the same time could make the context switching
 * and scheduling overhead reach peaks which could account for too a high
 * percentage of the actual service execution.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class AsyncProcessor
    extends CmdProcessor
{

//TODO: extract CancellableTask I/F from ExecCommand.  This I/F will
//extend Runnable and add the cancel method.  This I/F will substitute
//Runnable in the role of Abstract Command.  When done, get rid of all
//occurrences of ExecCommand in this file and replace them with this
//new I/F.
    
    /**
     * The run loop we use to execute commands.
     * We run the command, trap any uncaught exceptions (and call the
     * <code>uncaughtExcHandler</code>, if one was provided), and then
     * notify the enclosing <code>AsyncProcessor</code> upon exit.
     */
    private class Runner
        implements Runnable
    {
        ExecCommand cmd;
        Runner(ExecCommand cmd) { this.cmd = cmd; }
        public void run()
        {
            try {
                cmd.run();
            } catch (Throwable t) {
                if (uncaughtExcHandler != null) uncaughtExcHandler.handle(t); 
                else t.printStackTrace();
            } finally {  //Make sure we notify in any case.
                notifyExit(this);
            }
        }
    }
    
    /** Maps a <code>Runner</code> onto the thread that is running it. */
    private final Map   threadsMap;
    
    /** Custom handler used in the case of uncaught exceptions. */
    private final UncaughtExcHandler uncaughtExcHandler;
    
    /** 
     * Tells whether the processor will accept and run new commands. 
     * Initially set to <code>false</code> to mean the processor will
     * accept and run new commands transferred via the 
     * {@link #doExec(Runnable) doExec} method.  Latches to <code>true</code>
     * when the {@link #terminate(long) terminate} method is called.  From
     * that point on, new commands will be cancelled and discarded.
     */
    private boolean     terminated;
    

    /**
     * Factory method to create a thread that will run <code>cmd</code>.
     * The <code>cmd</code> object is wrapped into a <code>Runner</code>,
     * which is then actually passed to the new thread.  
     * The {@link #threadsMap} is updated accordingly.
     * This method will do nothing and return <code>null</code>, if the
     * {@link #terminated} flag is <code>true</code>.
     * 
     * @param cmd   The command to run.
     * @return A new thread to run <code>cmd</code> or <code>null</code> if
     *          the {@link #terminated} flag is <code>true</code>.
     */
    private synchronized Thread createRunner(ExecCommand cmd)
    {
        if (terminated) return null;
        Runner r = new Runner(cmd);
        Thread t = new Thread(r);
        threadsMap.put(r, t);
        return t;
    }
    
    /**
     * Callback used by a <code>Runner</code> when exiting the run loop.
     * It just updates the {@link #threadsMap} by removing the 
     * <code>Runner</code> and its associated thread.
     * 
     * @param r The <code>Runner</code> that exited.
     */
    private synchronized void notifyExit(Runner r)
    {
        threadsMap.remove(r);
    }
    
    /** 
     * Transfers a command for execution.
     * 
     * @param cmd The command to run.
     * @see CmdProcessor#doExec(java.lang.Runnable)
     */
    protected void doExec(Runnable cmd)
    {
        ExecCommand srv = (ExecCommand) cmd;
        Thread t = createRunner(srv);
        if (t == null) srv.cancel();
        else t.start();
    }
    
    /**
     * Creates a new instance.
     */
    public AsyncProcessor()
    {
        threadsMap = new HashMap();
        uncaughtExcHandler = null;
        terminated = false;
    }
    
    /**
     * Creates a new instance.
     * Registers the passed <code>handler</code> to handle all uncaught
     * exceptions that occurred during the execution of a service.
     * 
     * @param handler   Handles uncaught exceptions.  Mustn't be 
     *                  <code>null</code>.
     * @see UncaughtExcHandler
     */
    public AsyncProcessor(UncaughtExcHandler handler)
    {
        if (handler == null) throw new NullPointerException("No handler.");
        threadsMap = new HashMap();
        uncaughtExcHandler = handler;
        terminated = false;
    }
    
    /**
     * Cancels execution of all currently running services.
     * This is equivalent to calling the 
     * {@link ExecHandle#cancelExecution() cancelExecution} method on each
     * {@link ExecHandle} of the currently executing services.
     */
    public void cancelAll()
    {
        Runner[] runners;
        synchronized (this) {  //Take snapshot.
            Set keys = threadsMap.keySet();
            runners = new Runner[keys.size()];
            keys.toArray(runners);
        }
        //From now on new runners can be added to the threadsMap and/or
        //existing ones can exit and thus be removed from the map.  This
        //is not a concern.
        
        for (int i = 0; i < runners.length; ++i)
            runners[i].cmd.cancel();  //Won't hurt if no longer a runner.
    }
    
    /**
     * Cancels execution of all currently running services and disallows
     * execution of new ones.
     * Waits at most <code>maxWait</code> milliseconds for each running
     * thread to exit and retuns <code>true</code> only if all waited for
     * threads have died.  After this method has been called, the processor
     * is disabled, meaning that invocations of any of the <code>exec</code>
     * methods will result in the service being cancelled and discarded. 
     * Ideally you would call this method after all the client threads that
     * are using this processor have joined and so no call to any of the 
     * <code>exec</code> methods is possible after this method is invoked.
     * 
     * @param maxWait Maximum amount of milliseconds to wait for each running
     *                  thread to exit.
     * @return <code>true</code> no service is still running, 
     *          <code>false</code> otherwise.
     */
    public boolean terminate(long maxWait)
    {
        Thread[] runners;
        synchronized (this) { 
            terminated = true;  //Disable processor forever.
            cancelAll();  //...running services (synch, so re-entrant).
            
            //Take snapshot.
            Collection values = threadsMap.values();
            runners = new Thread[values.size()];
            values.toArray(runners);
        }
        //From now on existing runners can exit and thus be removed from
        //the threadsMap, but no new runner can be added to the map.  In
        //fact, the terminated flag is true, so the createRunner method
        //will do nothing.  So the runners array is the set of all threads
        //that are currently running or have exited since the snapshot was
        //taken.
        
        boolean anyThreadStillRunning = false;
        for (int i = 0; i < runners.length; ++i) {
            try {
                runners[i].join(maxWait);
            } catch (InterruptedException ie) {
                //Ignore.  This whole loop is a bounded wait.
            }
            if (runners[i].isAlive()) anyThreadStillRunning = true; //see NOTE.
        }
        return !anyThreadStillRunning;    
    }
    /* NOTE: Some JVM implementations could return true even if the thread
     * is not completely defunct yet.
     */

}
