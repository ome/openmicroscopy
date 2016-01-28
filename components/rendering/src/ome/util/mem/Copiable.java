/*
 * ome.util.mem.Copiable
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.mem;

/**
 * Requires an implementing class to provide <i>deep</i> copies of its
 * instances. More precisely, an invocation of the {@link #copy() copy} method,
 * such as <code>y = x.copy()</code>, is required to return an object
 * <code>y</code> for which the following conditions hold true:
 * <ul>
 * <li>The state of <code>y</code> is the same as the state of <code>x</code>.</li>
 * <li>The state of any object <code>w</code> referenced by <code>y</code>
 * is the same as the corresponding object referenced by <code>x</code>, and
 * so on recursively for every object referenced by <code>w</code>.</li>
 * <li>Any subsequent state change in <code>y</code> or, recursively, in any
 * other object referenced by <code>y</code>, is not going to affect
 * <code>x</code> or recursively, any other object referenced by
 * <code>x</code>.</li>
 * </ul>
 * Note that a deep copy would satisfy the above conditions. However, a
 * non-complete deep copy might comply with the above as well &#151; for example
 * immutable objects don't need to be copied.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision$ $Date:
 *          2005/06/09 15:01:57 $) </small>
 * @since OME2.2
 */
public interface Copiable {

    /**
     * Makes a copy of this object. The implementation is required to stick to
     * the copy semantics defined by this interface.
     * 
     * @return The new object.
     */
    public Object copy();

}
