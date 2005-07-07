/*
 * org.openmicroscopy.shoola.env.data.views.CompositeBatchCall
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
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.concur.tasks.CompositeTask;
import org.openmicroscopy.shoola.util.concur.tasks.MultiStepTask;

/** 
 * Aggregates calls to the data services in a computation tree.
 * <p>Each leaf node is a call to the data services and is an instance of a
 * concrete {@link BatchCall}.  The root node and all internal nodes are 
 * <code>CompositeBatchCall</code> objects which are used to 
 * {@link #add(BatchCall) build} the tree.  
 * After the tree has been completely assembled, the root node is passed to a 
 * {@link org.openmicroscopy.shoola.util.concur.tasks.CmdProcessor} for 
 * asynchronous execution.</p>
 * <p>This class behaves just like a {@link CompositeTask}.  In fact, this 
 * class extends (through delegation) the functionality of {@link CompositeTask}
 * so that it can play nicely with composition of {@link BatchCall}s.</p>
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
class CompositeBatchCall
    extends BatchCall
{

    /**
     * Allows this object to work just like a {@link CompositeTask}.
     * However, the tree is constrained to only contain instances of
     * {@link BatchCall}.  This way we can play nicely with composition
     * of {@link BatchCall}s.
     */
    private CompositeTask   delegate;
    
    
    /**
     * Creates a new instance which could serve either as a root or internal
     * node.
     */
    CompositeBatchCall() { delegate = new CompositeTask(); }
    
    /**
     * Adds a new child node to this node.
     * 
     * @param bc  The child node.  Mustn't be <code>null</code>.
     * @see CompositeTask#add(MultiStepTask)
     */
    void add(BatchCall bc) { delegate.add(bc); }
    
    /**
     * Counts the actual calls, if any.
     * That is, this method counts the leaf nodes connected to this node.
     *  
     * @return The count.
     */
    int countCalls() 
    { 
        int count = 0;
        Iterator children = delegate.getChildren().iterator();
        BatchCall child;
        while (children.hasNext()) {
            child = (BatchCall) children.next();
            count += child.countCalls();  //Use recursion and aggregate result.
        }
        return count; 
    }
    
    /**
     * Returns the leaf call that is currently processed by the execution
     * algorithm visiting the call tree.  
     * 
     * @return See above.
     * @see CompositeTask#getCurChild()
     */
    BatchCall getCurCall() 
    { 
        BatchCall bc = (BatchCall) delegate.getCurChild();
        if (bc != null) bc = bc.getCurCall();  //Use recursion.
        return bc; 
    }
    
    /**
     * Forwards the call to its {@link CompositeTask} delegate.
     * @see CompositeTask#doStep()
     */
    public Object doStep()
        throws Exception
    {
        return delegate.doStep();
    }
    
    /**
     * Forwards the call to its {@link CompositeTask} delegate.
     * @see CompositeTask#isDone()
     */
    public boolean isDone() { return delegate.isDone(); }
    
}
