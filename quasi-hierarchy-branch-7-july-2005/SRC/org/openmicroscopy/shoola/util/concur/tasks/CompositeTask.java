/*
 * org.openmicroscopy.shoola.util.concur.tasks.CompositeTask
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * Aggregates computations in a computation tree.
 * <p>Each leaf node is a computation to carry out and can be an instance of 
 * {@link java.lang.Runnable}, {@link Invocation}, or {@link MultiStepTask}.
 * The root node and all internal nodes are <code>CompositeTask</code> objects
 * which are used to {@link #add(MultiStepTask) build} the tree.</p>
 * <p>After building the tree, pass the root node to the {@link CmdProcessor}
 * for execution.  This class enforces an execution algorithm which consists in
 * running all computations in the leaf nodes <i>sequentially</i> and in the
 * order imposed by the depth-first algorithm &#151; the children of every node
 * are kept in the same order as they were {@link #add(MultiStepTask) added} to
 * the node.  For example, say you create a root node <i>R</i>, an internal node
 * <i>I</i> and {@link #add(MultiStepTask) add} <i>I</i> to <i>R</i>.  You then
 * create a {@link MultiStepTask} <i>L1</i> and {@link #add(MultiStepTask) add}
 * it to <i>R</i>.  Finally, you create two {@link Invocation} objects <i>L2, 
 * L3</i> and add them to <i>I</i>.  Here's what your tree looks like then:</p>
 * <pre>
 *     R
 *     |--- I
 *     |    |--- L2
 *     |    |--- L3
 *     |
 *     |--- L1
 * </pre>
 * <p>The resulting execution order is <i>L2, L3, L1</i>.</p>
 * <p>You can add a new node while the tree is being computed &#151; however, be
 * careful about concurrency issues when doing so.  If <i>not all</i> children
 * of the parent node to which you're adding the new node have already been
 * visited by the execution algorithm, then the leaf nodes of the new sub-tree
 * will be executed.  Otherwise, an exception will be thrown.<br>
 * Back to the previous example, you could add a new node <i>II</i> containing
 * a {@link java.lang.Runnable} <i>L4</i> to <i>I</i>.  So your tree would now
 * be:</p>
 * <pre>
 *     R
 *     |--- I
 *     |    |--- L2
 *     |    |--- L3
 *     |    |--- II
 *     |          |--- L4
 *     |--- L1
 * </pre>
 * <p>If you added <i>before L3</i> has finished executing, then <i>L4</i> would
 * be executed (after <i>L3</i> in this case).  If you didn't, you would get an
 * exception.</p>
 * <p>A <code>CompositeTask</code> object can't take part in more than one
 * execution.  This implies that the root node and any internal node can't be
 * reused after the first execution and have to be discarded.  Another 
 * implication is that you shouldn't share the same <code>CompositeTask</code>
 * object across any two trees that are executed concurrently.  Probably the
 * best strategy to avoid this altogether is to follow an hand-off protocol:
 * create a new tree, pass it to the {@link CmdProcessor}, dereference the root
 * node and any other internal node.</p> 
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
public class CompositeTask
    implements MultiStepTask
{
    
    /**
     * Flag to denote the <i>Adding</i> state.
     * When in this state, the tree is being built.  The {@link #curIndex} is
     * <code>0</code> and {@link #curChild} is <code>null</code>.  It is
     * possible to {@link #add(MultiStepTask) add} new nodes, but attempting to
     * call {@link #doStep()} will result in an exception.  A call to 
     * {@link #isDone()} will result in the object transitioning to the 
     * {@link #ITERATING} state if some nodes have been added and not all of
     * them are done (that is, there exists at least one child for which its 
     * <code>isDone</code> method will return <code>false</code>).
     * Otherwise, the object will transition to the {@link #DONE} state.
     */
    public static final int  ADDING = 0;
    
    /**
     * Flag to denote the <i>Iterating</i> state.
     * When in this state, the tree is being executed by the
     * {@link CmdProcessor} and the execution algorithm is processing this node.
     * The {@link #curChild} is not <code>null</code> and {@link #curIndex} is
     * in <code>[0, sz)</code> &#151; <code>sz</code> being the size of
     * {@link #children}.  Moreover, <nobr><code>
     * {@link #curChild} == {@link #children}.get({@link #curIndex})
     * </code></nobr>.  While in this state, it is still possible to 
     * {@link #add(MultiStepTask) add} new nodes.  The iteration will proceed
     * until all children are done (that is, until the <code>isDone</code>
     * method of every child will return <code>true</code>).  At which point the
     * object will transition to the {@link #DONE} state.
     */
    public static final int  ITERATING = 1;
    
    /**
     * Flag to denote the <i>Done</i> state.
     * When in this state, this node has already been executed &#151; this
     * implies the {@link #isDone() isDone} method has been called at least
     * once.  The {@link #curChild} is <code>null</code> and {@link #curIndex}
     * equals the number of children (may be <code>0</code>).  Any attempt to
     * {@link #add(MultiStepTask) add} new nodes or to call {@link #doStep()}
     * will result in an exception.  The {@link #isDone() isDone} method will
     * always return <code>true</code> from now on.
     */
    public static final int  DONE = 2;

    
    /** Keeps track of the current state. */
    private int             state;
    
    /** 
     * Stores the children nodes of this node.
     * Although this field is never <code>null</code>, the list may be empty.
     * If not empty, the list contains instances of {@link MultiStepTask} 
     * &#151; <code>null</code>s are not allowed. 
     */
    private List            children;
    
    /** 
     * The index of {@link #curChild} in {@link #children}.
     * Only relevant to the {@link #ITERATING} state.
     */
    private int             curIndex;
    
    /** 
     * The child currently processed by the execution algorithm.
     * Only relevant to the {@link #ITERATING} state.
     */
    private MultiStepTask   curChild;
    
    
    /**
     * Finds the first child which is not done, starting from {@link #curChild}.
     * That is, the first element <code>c</code> in {@link #children} whose
     * <code>isDone</code> method will return <code>false</code>.  If found,
     * {@link #curChild} will be set to <code>c</code> and {@link #curIndex} to
     * the index of <code>c</code> in {@link #children}.  Otherwise, 
     * {@link #curChild} will be set to <code>null</code> and {@link #curIndex}
     * to the size of {@link #children}.
     * 
     * @return The new value of {@link #curIndex}.
     */
    private int next()
    {
        int m = children.size();
        curChild = null;  //Discard previous one, if any.
        for (int k = curIndex; k < m; ++k) {
            curChild = (MultiStepTask) children.get(k);
            if (!curChild.isDone()) {  //Found, could well be old curChild.
                m = k;
                break;
            }
            //Else it is either a CompositeTask with no children or a
            //task with no steps to execute.  Both cases are probably
            //symptoms of bugs. (*)
        }
        curIndex = m;
        return m;
    }
    //(*) NOTE: We might want to revisit this and throw an exception instead.
    //However in the case of a CompositeTask, we can't throw an exception from
    //the add method b/c the client may first add an empty node and then
    //populate it.  So exceptions will have to be thrown during execution.
    //This is inconvenient b/c execution doesn't happen in the client's thread.
    
    /**
     * Creates a new instance which could serve either as a root or internal
     * node.
     */
    public CompositeTask()
    { 
        children = new ArrayList();
        state = ADDING;
    }
    
    /**
     * Just forwards the call to the child currently processed by the execution
     * algorithm.
     * @throws IllegalStateException  If not invoked in the {@link #ITERATING}
     *                                state.
     * @see MultiStepTask#doStep()
     */
    public Object doStep()
        throws Exception
    {
        if (state != ITERATING) throw new IllegalStateException();
        return curChild.doStep();
    }

    /** 
     * Tells whether all children within this node have been processed.
     * If the current child has been processed, then this method advances
     * to the next child.
     * @see MultiStepTask#isDone()
     */
    public boolean isDone()
    {
        if (state == DONE) return true;
        //Else Active state, either ADDING or ITERATING
        
        int m = next();  //Update the curChild and curIndex.
        if (m == children.size())  //sz=0 or all children are done.
            state = DONE;
        else  //m<sz and curChild is first child not done.
            if (state == ADDING) state = ITERATING;
            //Else we're already iterating, do nothing.
            
        return (state == DONE);
    }
    
    /**
     * Adds a new child node to this node.
     * The child node will be treated as a leaf node unless it is an instance
     * of <code>CompositeTask</code>.  In which case, the new node will just
     * be an internal node.
     * 
     * @param mst  The child node.  Mustn't be <code>null</code>.
     * @throws IllegalStateException  If invoked in the {@link #DONE} state.
     */
    public void add(MultiStepTask mst)
    {
        if (state == DONE) throw new IllegalStateException();
        if (mst == null) throw new NullPointerException("No task.");
        children.add(mst);
    }

    /**
     * Adds a new leaf node to this node.
     * 
     * @param task  The leaf node.  Mustn't be <code>null</code>.
     * @throws IllegalStateException  If invoked in the {@link #DONE} state.
     */
    public void add(Runnable task)
    {
        add(new TaskAdapter(task));  
        //TaskAdapter checks for null and throws NPE.
    }
    
    /**
     * Adds a new leaf node to this node.
     * 
     * @param call  The leaf node.  Mustn't be <code>null</code>.
     * @throws IllegalStateException  If invoked in the {@link #DONE} state.
     */
    public void add(Invocation call)
    {
        add(new InvocationAdapter(call));  
        //InvocationAdapter checks for null and throws NPE.
    }
    
    /**
     * Returns a <i>read-only</i> view of the children nodes. 
     * Attempts to modify the contents of the returned list will fail and an 
     * exception will be thrown.  The list contains no <code>null</code>
     * elements; however its elements may differ from the objects that were
     * initially added.  In fact, whenever you add a {@link Runnable} or 
     * {@link Invokation} object, this is turned into a {@link MultiStepTask}
     * object before adding it to the list.  {@link MultiStepTask} objects
     * remains untouched though.
     * 
     * @return  See above.
     */
    public List getChildren()
    {
        return Collections.unmodifiableList(children);
    }
    
    /**
     * Returns the child node that is currently processed by the execution
     * algorithm.  The returned object may differ from the object that was
     * originally added.  In fact, whenever you add a {@link Runnable} or 
     * {@link Invokation} object, this is turned into a {@link MultiStepTask}
     * object before adding it to the list.  {@link MultiStepTask} objects
     * remains untouched though.
     * 
     * @return  See above.
     */
    public MultiStepTask getCurChild()
    {
        if (isDone()) return null;
        return curChild;
    }
    
    /**
     * Returns a flag denoting the current state.
     * 
     * @return One of the state flags defined by this class.
     */
    public int getState() { return state; }
    
}
