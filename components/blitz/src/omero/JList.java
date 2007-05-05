/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class JList extends omero.RList {

    public JList() {
        super(true,null);
    }

    public JList(RType...rtypes) {
        super(false,Arrays.asList(rtypes));
    }

    public JList(List<RType> l) {
        super(false,l);
    }

    public JList(Collection<RType> s) {
        super(false,new ArrayList(s));
    }


}