/*
 * ome.util.CBlock
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util;

import ome.model.IObject;
import ome.model.core.Image;

/**
 * Block template used to "C"ollect the results of some function called on each
 * {@link ome.model.IObject IObject} in a collection. The {@link CBlock} can be
 * used to "map" {@link IObject} inputs to arbitrary outputs. All collection
 * valued fields on model objects have a method that will scan the collection
 * and apply the block of code. For example, {@link Image#collectPixels(CBlock)}
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 3.0
 */
public interface CBlock<E> {

    /**
     * invoke this block.
     * 
     * @param object
     *            An IObject (possibly null) which should be considered for
     *            mapping.
     * @return A possibly null value which is under some interpretation "mapped"
     *         to the object argument
     */
    E call(IObject object);

}
