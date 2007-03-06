/*
 *   $Id$
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 * 
 */

package omero;

public class JInt extends omero.RInt {

    public JInt() {
        super(true,0);
    }
    
    public JInt(int i) {
        super(false,i);
    }
    
}