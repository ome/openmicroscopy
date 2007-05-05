/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero;

public class JFloat extends omero.RFloat {

    public JFloat() {
        super(true,0f);
    }

    public JFloat(float f) {
        super(false,f);
    }

}