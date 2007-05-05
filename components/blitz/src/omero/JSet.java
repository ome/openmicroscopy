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

public class JSet extends omero.RSet {

    public JSet() {
        super(true,null);

    }

    public JSet(RType...rtypes) {
        super(false,Arrays.asList(rtypes));
    }

    public JSet(List<RType> l) {
        super(false,l);
    }

    public JSet(Collection<RType> s) {
        super(false,new ArrayList(s));
    }


}