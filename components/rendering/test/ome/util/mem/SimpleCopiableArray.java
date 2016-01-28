/*
 * ome.util.mem.SimpleCopiableArray
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.mem;

/**
 * Supports unit tests for the {@link CopiableArray} class.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @since OME2.2
 */
class SimpleCopiableArray extends CopiableArray {

    SimpleCopiableArray(int size) {
        super(size);
    }

    @Override
    protected CopiableArray makeNew(int size) {
        return new SimpleCopiableArray(size);
    }

}
