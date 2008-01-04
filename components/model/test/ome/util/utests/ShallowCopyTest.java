/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.util.utests;

import junit.framework.TestCase;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.util.ShallowCopy;

import org.testng.annotations.Test;

public class ShallowCopyTest extends TestCase {

    @Test
    public void testNoNulls() throws Exception {
        Pixels pix = new Pixels();
        pix.setId(1L);
        pix.setSizeC(1);
        pix.setImage(new Image());

        Pixels test = new ShallowCopy().copy(pix);
        assertNotNull(test.getId());
        assertNotNull(test.getDetails());
        assertNotNull(test.getSizeC());
        assertNotNull(test.getImage());
    }

}
