/*
 * org.openmicroscopy.shoola.env.data.views.BatchCallTree
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

package org.openmicroscopy.shoola.env.data.views;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor;
import org.openmicroscopy.shoola.util.concur.tasks.ExecHandle;
import org.openmicroscopy.shoola.util.concur.tasks.ExecMonitor;

/** 
 * Maintains and executes a {@link BatchCall} tree.
 * <p>This class is abstract, it requires subclasses to actually 
 * {@link #buildTree() build} the tree and provide the result of the computation
 * after the tree has been {@link #exec(AgentEventListener) executed}.  A tree
 * represents a coarse grained task to carry out within the data services and
 * usually maps to a single call in a data services view.</p>  
 * <p>A <code>BatchCallTree</code> object can't be 
 * {@link #exec(AgentEventListener) executed} more than once &#151; any such
 * attempt would result in an exception being thrown.  So you have to discard
 * the object after execution.</p>
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
abstract class BatchCallTree
{

    /**
     * Tells whether the {@link #exec(AgentEventListener) exec} method has
     * already been executed.
     * It latches to <code>true</code> after the first execution.  Don't
     * access this field directly, use {@link #hasExecuted()} instead. 
     */
    private boolean    executed; 
    
    /** The root of the call tree. */
    private CompositeBatchCall  root;
    
    /** Subclasses use it to gain access to the container's services. */
    protected final Registry    context;
    
    
    /**
     * Creates a new instance.
     */
    protected BatchCallTree() 
    { 
        root = new CompositeBatchCall();
        context = DataViewsFactory.getContext();
    }
    
    /**
     * Queries the current {@link #executed execution} state and then
     * marks it as executed.
     * This method will return <code>false</code> (not executed) only the first
     * time it is invoked and then <code>true</code> (executed) thereafter.
     * 
     * @return The current {@link #executed execution} state.
     */
    private synchronized boolean hasExecuted()
    {
        boolean currentState = executed;
        executed = true;
        return currentState;
    }
    //NOTE: This method is sync in order to avoid problems in the case the UI
    //spawns a separate thread to call a method w/in a view and at the same
    //time the same method is being called by the UI thread.  This condition
    //can never arise if the UI avoids to spawn threads altogether.
    
    /**
     * Counts the actual calls.
     * That is, this method counts all the leaf nodes in the tree.
     *  
     * @return The count.
     */
    int countCalls() { return root.countCalls(); }
    
    /**
     * Returns the leaf call that is currently processed by the execution
     * algorithm visiting the call tree.  
     * 
     * @return See above.
     * @see CompositeBatchCall#getCurCall()
     */
    BatchCall getCurCall() { return root.getCurCall(); }
    
    /**
     * Asynchronoulsy executes this task.
     * All leaf {@link BatchCall}s within the tree will be executed 
     * <i>sequentially</i>, respecting the order in which nodes were added to
     * the tree.
     * The specified observer is notified of the execution progress and of the
     * eventual outcome of the computation.  In particular if the computation
     * is not cancelled or no exception is raised, then the result of the
     * whole computation, as returned by the {@link #getResult() getResult}
     * method, is dispatched to the observer. 
     * 
     * @param observer Monitors progress and gets the outcome of the execution.
     *                  Mustn't be <code>null</code>.
     * @return A handle to the computation.  Can be used to cancel execution.
     * @see BatchCallMonitor
     */
    CallHandle exec(AgentEventListener observer)
    {
        if (hasExecuted()) throw new IllegalStateException();
        
        //Only one thread will ever make it here b/c hasExecuted is sync.
        buildTree();
        ExecHandle handle = getProcessor().exec(root, getMonitor(observer));
        return new CallHandle(handle);
    }
    
    /**
     * Returns a concrete {@link CmdProcessor} to 
     * {@link #exec(AgentEventListener) execute} the call tree.
     * 
     * @return See above.
     */
    protected CmdProcessor getProcessor()
    {
        return (CmdProcessor) context.lookup(LookupNames.CMD_PROCESSOR);
    }
    
    /**
     * Returns an implementation of {@link ExecMonitor} that works as an 
     * adapter to notify the specified <code>observer</code> of execution
     * events. 
     * Specifically, the returned adapter will notify the <code>observer</code>
     * of the tree's execution progress and of the eventual outcome of the 
     * computation.
     * 
     * @param observer The adaptee.
     * @return The adapter.
     * @see BatchCallMonitor
     */
    protected ExecMonitor getMonitor(AgentEventListener observer)
    {
        return new BatchCallMonitor(this, observer);
    }
    
    /**
     * Adds a new child node to the root.
     * The root node maintained by <code>BatchCallTree</code> is an instance
     * of {@link CompositeBatchCall}, so refer to that for the semantics of 
     * adding and the resulting execution order.
     * 
     * @param bc  The child node.  Mustn't be <code>null</code>.
     * @see CompositeBatchCall#add(BatchCall)
     */
    protected void add(BatchCall bc) { root.add(bc); }
    
    /**
     * Builds the call tree.
     * The root of the tree is owned by <code>BatchCallTree</code>, you use
     * the {@link #add(BatchCall) add} method to add nodes to the root.
     * The call tree could be as easy as one call; for example you could set
     * it to be:
     * <pre><code>
     * add(new BatchCall("Loading some data.") {
     *         void doCall() { loadSomeData(); }
     *     });
     * </code></pre>
     * <p>Where <code>loadSomeData</code> is a call that retrieves some data 
     * from the data services and stores the result somewhere within this object
     * &#151; the call description (<code>"Loading some data"</code>) will be
     * used to provide feedback to the execution observer.</p>
     * <p>However, you would typically set up an execution sequence, by adding
     * several leaf {@link BatchCall}s to the root node.  Further composition is
     * also possible because {@link BatchCall}s can be aggregated into sub-trees
     * and then these trees can be {@link #add(BatchCall) added} to the root.
     * Even dynamic composition (as the tree is executed) is available, should
     * you ever need it.</p>
     * <p>As hinted above, call results have to be stored in a place accessible
     * to this object.  This is because a concrete <code>BatchCallTree</code> is
     * responsible for providing the final result of the computation.</p>
     * 
     * @see BatchCall
     * @see CompositeBatchCall
     * @see #getResult()
     * @see #exec(AgentEventListener)
     */
    protected abstract void buildTree();
    
    /**
     * Provides the final result of the computation.
     * This method is called after the call tree built by {@link #buildTree()}
     * has been {@link #exec(AgentEventListener) executed} to provide the
     * result of the whole execution.  
     * 
     * @return The final result of the computation.
     */
    protected abstract Object getResult();
    
}
