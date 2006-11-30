/*
 * org.openmicroscopy.shoola.util.concur.tasks.ResultAssembler
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

/** 
 * Defines a contract between the {@link CmdProcessor} and a class that
 * provides a specific way to assemble computatation results from partial
 * results.
 * <p>You might want to implement this interface when you break down a
 * computation into steps (by using an {@link Invocation} chain or a
 * {@link MultiStepTask}) and need to implement a custom algorithm to
 * assemble the partial results of each step into the final result of the
 * computation.</p>
 * <p>The {@link CmdProcessor} calls the {@link #add(Object) add} method at
 * the end of each step, passing the partial result computed by that step.
 * The order of the calls to the {@link #add(Object) add} method is the same
 * as the order in which the partial results are produced.  After the last
 * step has been performed, the {@link CmdProcessor} calls the 
 * {@link #assemble() assemble} method, which has to return the final result
 * of the computation &#151; however, this method is never called in the case
 * an exception is raised during the computation or the computation is
 * cancelled.</p>
 * <p>Calls to the {@link #add(Object) add}/{@link #assemble() assemble} method
 * are dispatched <i>within the same thread that executes the computation</i>,
 * so they can never overlap.  However, keep in mind that, in general, the
 * computation is executed in a thread different from the one that triggered
 * execution by invoking one of the <code>exec</code> methods.
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
public interface ResultAssembler
{
    
    /**
     * Called at the end of each step in the computation, passing the 
     * <code>partialResult</code> computed by that step.
     * 
     * @param partialResult The partial result just computed. 
     */
    public void add(Object partialResult);
    
    /**
     * Called after the last step in the computation has been performed, 
     * this method has to return the final result of the computation.
     * This is usually done by assembling all partial results into a
     * single object.
     * 
     * @return  The result of the computation.
     */
    public Object assemble();
    
}
