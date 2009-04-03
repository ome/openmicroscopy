/*
 * org.openmicroscopy.shoola.util.concur.ThreadSupport
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

/** 
 * Helps to simulate concurrent access to an object in order to verify 
 * synchronization and state dependence.
 * <p>This class allows you to {@link #pauseMainFlow() pause} the main flow (the
 * thread that creates an instance of this class, it should be the JUnit thread)
 * and, meanwhile, have an alternate thread perform some other action &#151; by
 * calling {@link #startAltFlow()}.  The main flow should then 
 * {@link #awaitAltFlow() wait} for the alternate flow to terminate so that no 
 * alternate thread is kicking around when JUnit runs the next test &#151; this
 * avoids the possibility of unplanned concurrent access.<br>
 * Also static methods are provided to run a task in a separate thread but
 * synchronously with respect to the calling thread &#151; that is, the caller
 * will wait until the task has finished running.</p>
 * <p>This class factors out some test code common to all test cases in 
 * <code>concur</code> and its sub-packages.  Tests that verify synchronization
 * mainly use this class to have the JUnit thread acquire an object's lock and 
 * then wait for the alternate thread to attempt to acquire the same lock &#151;
 * so that we can simulate concurrent access and test synchronized blocks.  
 * Those tests are written against the methods of the target class (the class 
 * under test) that perform atomic state transitions by acquiring the target's 
 * lock (target is an instance of the target class), modifying the target's 
 * state, and then releasing the lock.  Here is the common pattern used in those
 * test cases (see {@link Semaphore} and {@link TestSemaphoreSync} for example):
 * </p>
 * <ul>
 *   <li>The target class allows to register a {@link ControlFlowObserver} and 
 *   notifies it whenever entering a method that performs an atomic state 
 *   transition.  The {@link ControlFlowObserver#update(int) update} method is 
 *   called after the lock is acquired but before the target's state is modified
 *   and the lock is released.</li>
 *   <li>The test case registers a {@link ControlFlowObserver} with the target.
 *   The implementation of the {@link ControlFlowObserver#update(int) update}
 *   method <i>first</i> starts the alternate flow and <i>then</i> pauses the 
 *   main flow.</li>
 *   <li>The alternate flow attempts to get the target's state.  The target
 *   protects access to its state with the same lock.</li>
 *   <li>The test consists in calling a target's method <code>m</code> that
 *   enforces an atomic state transition, waiting for the alternate flow to
 *   terminate, and then checking that the state read by the alternate flow is
 *   the state expected at the end of the transition performed by
 *   <code>m()</code> in the main flow.</li>
 * </ul>
 * <p>Note the sequence of events: <code>m()</code> is called in the main flow
 * (JUnit), <code>update()</code> is called in the main flow too just after 
 * <code>m()</code> has acquired the lock, within <code>update()</code> the
 * alternate flow is started and then the main flow is paused.  Before it can
 * access the target's state, the alternate flow has to wait until the main flow
 * releases the lock.  So if the state that we get is not the state in which the
 * target should be after <code>m()</code>, then we can conclude that the
 * alternate flow was allowed to read the state while it was modified by 
 * <code>m()</code> &#151; so <code>m()</code> is not acquiring the lock
 * correctly.  Sadly enough, even if we get the expected state, we can't assert 
 * <code>m()</code> is acquiring the lock properly.  In fact, the alternate flow
 * could have accessed the state after <code>m()</code> returned and no
 * concurrent access to the target could have taken place at all.  To mitigate 
 * this state of affairs, we use a reasonably long {@link #PAUSE_DELAY pause} 
 * delay for the main thread so that, in practice, the alternate flow is
 * extremely likely to run while the main flow is paused and still holding the
 * lock.  Thus, it's <i>reasonable</i> (but not logically sound) to assume that 
 * <code>m()</code> was implemented correctly if we get the expected state.</p>
 * <p>Tests that verify the correct behavior of state dependent actions also 
 * follow a common pattern.  Those tests are written against the methods of the
 * target class that perform actions depending on the current target's state. 
 * These methods are usually structured as follows: the target's lock is
 * acquired, the state is checked and if some condition is satisfied the method
 * proceeds (reading or modifying the state) otherwise the caller is suspended
 * (by calling the {@link Object#wait() wait} method, which releases the lock)
 * until that condition is satisfied.  Here is the common pattern used in those 
 * test cases (see {@link Semaphore} and {@link TestSemaphoreStateDep} for 
 * example):</p>
 * <ul>
 *   <li>The target class allows to register a {@link ControlFlowObserver} and
 *   notifies it whenever entering a state dependent method.  The 
 *   {@link ControlFlowObserver#update(int) update} method is called after the
 *   lock is acquired but before the {@link Object#wait() wait} method is
 *   called.</li>
 *   <li>The test case registers a {@link ControlFlowObserver} with the target.
 *   The implementation of the {@link ControlFlowObserver#update(int) update}
 *   method starts the alternate flow.</li>
 *   <li>The alternate flow has the target transition to a state in which a
 *   given state dependent method <code>m</code> can proceed.  Note that the 
 *   target's methods invoked by the alternate flow have to acquire the target's
 *   lock and issue notifications of state change.</li>
 *   <li>The test consists transitioning the target to a state in which 
 *   <code>m()</code> can't proceed, invoking <code>m()</code>, waiting for the
 *   alternate flow to terminate, and finally verifying that <code>m()</code>
 *   operated on the right state.</li>
 * </ul>
 * <p>Note the sequence of events: <code>m()</code> is called in the main flow
 * (JUnit), <code>update()</code> is called in the main flow too just after 
 * <code>m()</code> has acquired the lock, within <code>update()</code> the
 * alternate flow is started.  Before it can access the target's state, the 
 * alternate flow has to wait until the main flow releases the lock.  Now if
 * <code>m()</code> proceeds with its action and then releases the lock, it will
 * have operated on the wrong state and we fail the test.  Instead, if
 * <code>m()</code> calls <code>wait()</code> then the lock is released, the
 * alternate flow transitions the object to a suitable state, and then a
 * notification is issued so that the main flow wakes up and <code>m()</code>
 * proceeds with its action, this time operating on the correct state &#151; in
 * this case the test succeeds.  Note that if the methods invoked by the
 * alternate flow fail to send the wake up signal, we get a deadlock.  This is a
 * failure as well, but can't be detected by JUint and we'll have to stop
 * execution manually.</p>
 * <p>Finally, tests that verify how an object reacts to interruption follow a
 * common pattern too (see {@link Semaphore} and {@link TestSemaphoreInt} for 
 * example).  Each test spawns an ad-hoc thread (using the static methods 
 * provided by this class) which is interrupted to verify how a given object
 * reacts to interruption.  The result of the test (usually an instance of 
 * {@link InterruptedException}) is passed back into the main flow (JUnit) so
 * to perform the needed assertions.<br>  
 * Note that we never interrupt JUnit.  Spawning another thread might seem 
 * unecessary when we could just interrupt JUnit and then clear the interrupted
 * status after the test.  However implementation characteristics of 
 * interruption-based methods are quite uncertain, so we prefer using a 
 * separate thread (which is simply discarded at the end of the test) rather
 * than interrupting JUnit and making thus room for possible side effects.
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
public class ThreadSupport
{

    /** The delay used to pause the main thread. */
    public static final int     PAUSE_DELAY = 2000;
    
    
    /**
     * Runs <code>task</code> <i>synchronously</i> but in a separate thread.
     * This means that the main flow (the thread in which this method is
     * invoked, should be JUnit) will wait until the <code>task</code> has
     * finished running.  Interrupting the main flow will buy you a nice
     * {@link Error}. 
     * 
     * @param task  The task to run.  Mustn't be <code>null</code>.
     */
    public static void runInNewThread(Runnable task)
    {
        if (task == null) throw new NullPointerException("No task.");
        Thread t = new Thread(task);
        t.start();
        try {
            t.join();
        } catch (InterruptedException ie) {
            throw new Error("Main flow thread was interrupted.");
        }
    }
    
    /**
     * Runs <code>task</code> <i>synchronously</i> but in a separate thread, 
     * which is interrupted just before calling <code>task.run()</code>.
     * The main flow (the thread in which this method is invoked, should be
     * JUnit) will wait until the <code>task</code> has finished running.  
     * Interrupting the main flow will buy you a nice {@link Error}. 
     * 
     * @param task  The task to run.  Mustn't be <code>null</code>.
     */
    public static void runInNewInterruptedThread(final Runnable task)
    {
        if (task == null) throw new NullPointerException("No task.");
        Thread t = new Thread(new Runnable() {
            public void run() {
                Thread.currentThread().interrupt();
                task.run();
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException ie) {
            throw new Error("Main flow thread was interrupted.");
        }
    }
    
    
    /** The thread that creates this object. */
    private final Thread  mainFlow;
    
    /** The thread that we spawn to perform the alternate flow. */
    private final Thread  altFlow;
    
    
    /**
     * Creates a new object.
     * The main flow is assumed to be the thread in which this constructor is
     * invoked, so you should create this object within the JUnit thread
     * (usually within the <code>setUp</code> method).  The passed argument
     * specifies the actions to be carried out by the alternate flow.  This
     * thread is automatically created for you, but only started when you
     * call {@link #startAltFlow()}.
     * 
     * @param altFlow   The activity to be performed by the alternate thread.
     */
    public ThreadSupport(Runnable altFlow)
    {
        mainFlow = Thread.currentThread();  //Should be JUnit.
        this.altFlow = new Thread(altFlow);
    }
    
    /**
     * Starts the alternate thread.
     */
    public void startAltFlow()
    {
        altFlow.start();
    }
    
    /**
     * Puts the main flow to sleep for {@link #PAUSE_DELAY} milliseconds.
     * You must invoke this method within the main flow, otherwise an
     * {@link Error} will be thrown.  You get the same treat, if you interrupt
     * the main flow while it's sleeping.
     */
    public void pauseMainFlow()
    {
        if (Thread.currentThread() != mainFlow)
            throw new Error("This method must be invoked in the main flow.");
        try {
            Thread.sleep(PAUSE_DELAY);
        } catch (InterruptedException ie) {
            throw new Error("Main flow thread was interrupted.");
        }
    }
    
    /**
     * Tells whether the current thread is the main flow.
     * 
     * @return <code>true</code> if main flow, <code>false</code> otherwise.
     */
    public boolean isMainFlow()
    {
        return Thread.currentThread() == mainFlow;
    }
    
    /**
     * Tells whether the current thread is the alternate flow.
     * 
     * @return <code>true</code> if alternate flow, 
     *          <code>false</code> otherwise.
     */
    public boolean isAltFlow()
    {
        return Thread.currentThread() == altFlow;
    }
    
    /**
     * Interrupts the alternate flow.
     */
    public void interruptAltFlow()
    {
        altFlow.interrupt();
    }
    
    /**
     * Waits for the alternate flow to terminate.
     * Obviously, you're not going to call this from the alternate flow &#151;
     * or you get a friendly {@link Error}.
     */
    public void awaitAltFlow()
    {
        if (Thread.currentThread() == altFlow)
            throw new Error("This method mustn't be invoked in the alt flow.");
        try {
            altFlow.join();
        } catch (InterruptedException ie) {
            throw new Error("Interrupted wait for the alt flow to join.");
        }
    }
    
}
