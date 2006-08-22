/*
 * org.openmicroscopy.shoola.util.concur.tasks.ListAssembler
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * Collects all partial results of a computation in a list.
 * The order in which the partial results were {@link #add(Object) added} is
 * preserved.  Thus, the <code>List</code> returned by the 
 * {@link #assemble() assemble} method can be traversed in that same order.
 * Also, <code>null</code> values are allowed.  That is, if a <code>null</code>
 * partial results is {@link #add(Object) added}, then the returned
 * <code>List</code> will contain a <code>null</code> value in the corresponding
 * position.
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
public class ListAssembler
    implements ResultAssembler
{

    /** Collects all partial results. */
    private List   result;
    
    
    /**
     * Creates a new instance.
     */
    public ListAssembler()
    {
        result = new ArrayList();
    }

    /**
     * Implemented as specified by interface.
     * @see ResultAssembler#add(Object)
     */
    public void add(Object partialResult)
    {   
        result.add(partialResult);  //Can add null.
    }

    /**
     * Implemented as specified by interface.
     * The returned object is a <code>List</code>, which contains the partial
     * results in the same order in which they were {@link #add(Object) added}.
     * Notice that <code>null</code> values are allowed within the list.  
     * The returned list is never <code>null</code>, although it may be empty.
     *   
     * @return See above.
     * @see ResultAssembler#assemble()
     */
    public Object assemble()
    {
        return result;
    }

}
