/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero;

public class JDouble extends omero.RDouble {

    public JDouble() {
        super(true,0.0);
    }

    public JDouble(double d) {
        super(false,d);
    }

    public JDouble(Double d) {
        super(d==null,d);
    }

}