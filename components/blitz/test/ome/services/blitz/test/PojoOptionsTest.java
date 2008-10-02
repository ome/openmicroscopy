/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test;

import junit.framework.TestCase;
import omero.RLong;
import omero.sys.PojoOptions;

import org.testng.annotations.Test;

public class PojoOptionsTest extends TestCase {

    @Test
    public void testBasics() throws Exception {
        PojoOptions po = new PojoOptions();
        po.exp(new RLong(1));
    }
}
