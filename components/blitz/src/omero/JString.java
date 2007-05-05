/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero;

import java.util.ArrayList;

public class JString extends omero.RString {

    public JString() {
        super(true,null);
    }

    public JString(String s) {
        super(s==null,s);
    }

    public static JList asList(String...strings) {
        JList list = new JList();
        list._null = false;
        list.val = new ArrayList<RType>();
        for (String string : strings) {
            list.val.add( new JString(string));
        }
        return list;
    }

}