/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero;

public class JLong extends omero.RLong {

    public JLong() {
        super(true,0L);
    }

    public JLong(long l) {
        super(false,l);
    }

}