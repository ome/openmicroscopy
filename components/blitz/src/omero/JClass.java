/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero;

public class JClass extends omero.RClass {

    public JClass() {
        super(true,"");
    }

    public JClass(String s) {
        super(s==null,s);
    }

    public JClass(Class k) {
        super(k==null,k==null?"":k.getName());
    }

}