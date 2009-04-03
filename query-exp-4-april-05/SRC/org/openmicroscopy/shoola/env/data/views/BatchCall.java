/*
 * org.openmicroscopy.shoola.env.data.views.BatchCall
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
import org.openmicroscopy.shoola.util.concur.tasks.MultiStepTask;

/** 
 * An asynchronous call to the data services.
 * These kind of calls are usually aggregated in a {@link BatchCallTree} in
 * order to perform multiple operations on (possibly) large portions of a
 * view's data model in the background.
 * <p>This class defines a component interface that allows for composition.
 * Leaf <code>BatchCall</code>s can be composed into {@link CompositeBatchCall}s
 * which, in turn, make up the internal nodes of a {@link BatchCallTree}.</p>
 * <p>A leaf <code>BatchCall</code> is a concrete subclass that overrides the
 * {@link #doCall() doCall} method to carry out some operation on the data
 * services.  Because we use the <code>util.concur</code> packages for 
 * asynchronous execution, asynchronous calls have to implement the   
 * {@link MultiStepTask} interface.  So this class also works as an adapter to 
 * map said interface onto the {@link #doCall() doCall} method.  For this reason
 * , subclasses can decide to override the methods from {@link MultiStepTask} if
 * they need to take advantage of the extra flexibility offered by the 
 * {@link MultiStepTask} interface.</p> 
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
abstract class BatchCall
    implements MultiStepTask
{

    /** A textual description of what this call does. */
    private String      description;
    
    /** 
     * Tells whether or not this call has been completey performed.
     * Latches to <code>true</code> after the first call to {@link #doStep()}
     * &#151; which, in turns, invokes {@link #doCall()}.  If a subclass decides
     * to implement the {@link MultiStepTask} interface, this semantics is 
     * likely to require changes.
     */
    protected boolean   done;
    
    
    /**
     * Creates a new instance with no textual description.
     */
    protected BatchCall() {}
    
    /**
     * Creates a new instance with the specified textual description.
     * 
     * @param description A textual description of what this call does.
     */
    protected BatchCall(String description) { this.description = description; }
    
    /**
     * Returns a textual description of what this call does.
     * 
     * @return See above.
     */
    String getDescription() { return description; }
    
    /**
     * Sets the call's description.
     * This is a textual description of what this call does.
     * 
     * @param d The description.
     */
    void setDescription(String d) { description = d; }
    
    /**
     * Counts the children calls, if any.
     * This method is for the benefit of {@link CompositeBatchCall} which
     * overrides it to return the count of its children.  
     * A leaf <code>BatchCall</code> has to return <code>1</code> and so
     * doesn't need to override this method.
     *  
     * @return <code>1</code>.
     */
    int countCalls() { return 1; }
    
    /**
     * Returns the leaf call that is currently processed by the execution
     * algorithm visiting the call tree.  
     * This method is for the benefit of {@link CompositeBatchCall} which
     * overrides it to return the currently processed child.
     * A leaf <code>BatchCall</code> has to return <code>this</code> and so
     * doesn't need to override this method.
     * 
     * @return <code>this</code>.
     */
    BatchCall getCurCall() { return this; }
    
    /**
     * A leaf <code>BatchCall</code> overrides this method to carry out
     * some operation on the data services.
     * 
     * @throws Exception If an error occurs.
     */
    void doCall() throws Exception {}
    
    /**
     * Forwards the call to the {@link #doCall() doCall} method.
     * @see MultiStepTask#doStep()
     */
    public Object doStep()
        throws Exception
    {
        if (!done) doCall();
        done = true;
        return null;
    }
    
    /**
     * Tells whether or not the {@link #doStep() doStep} method has been
     * invoked.
     * @see MultiStepTask#isDone()
     */
    public boolean isDone() { return done; }
    
}
