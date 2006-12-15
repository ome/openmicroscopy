/*
 * ome.util.mem.SimpleCopiableArray
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.mem;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * Supports unit tests for the {@link CopiableArray} class.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: 1.1 $ $Date:
 *          2005/06/25 18:09:08 $) </small>
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
