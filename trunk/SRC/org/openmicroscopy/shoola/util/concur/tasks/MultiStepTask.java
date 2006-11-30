/*
 * org.openmicroscopy.shoola.util.concur.tasks.MultiStepTask
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
 * Models a computation that is broken down into subsequent steps.
 * Each step is carried out by the {@link #doStep() doStep} method and supplies
 * a partial result of the computation.  The computation result has to be
 * assembled out of those partial results.  The {@link #isDone() isDone} method
 * tells when the last step in the computation has been performed.  Thus, a
 * <code>MultiStepTask</code> has to be executed in a loop that calls the
 * {@link #doStep() doStep} method until the {@link #isDone() isDone} method
 * returns <code>true</code>.
 * <p>The {@link CmdProcessor} makes provisions for executing a 
 * <code>MultiStepTask</code> asynchronously and retrieving the result of
 * the computation (or any exception) via a {@link Future}.</p>
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
public interface MultiStepTask
{

    /**
     * Carries out one step in the computation.
     * 
     * @return  The partial result computed by this step. 
     * @throws Exception    If any error occurs.
     */
    public Object doStep() throws Exception;
    
    /**
     * Tells when the last step in the computation has been performed.
     * 
     * @return  <code>true</code> if the last step has been performed, 
     *          <code>false</code> otherwise.
     */
    public boolean isDone();
    
}
