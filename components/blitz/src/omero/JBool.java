/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero;

public class JBool extends omero.RBool {

    public JBool() {
        super(true,false);
    }

    public JBool(Boolean b) {
        super(b==null,b==null?false:b);
    }

}