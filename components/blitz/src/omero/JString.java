/*
 *   $Id$
 * 
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 * 
 */

package omero;

public class JString extends omero.RString {

    public JString() {
        super(true,null);
    }
    
    public JString(String s) {
        super(s==null,s);
    }
    
}