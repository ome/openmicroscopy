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

    public JList(RType...rtypes) {
        super(Arrays.asList(rtypes));
    }

    public JList(List<RType> l) {
        super(l);
    }

    public JList(Collection<RType> s) {
        super(new ArrayList(s));
    }


}