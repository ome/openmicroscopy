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

public class JArray extends omero.RArray {

    public JArray(RType...rtypes) {
        super(Arrays.asList(rtypes));
    }

    public JArray(List<RType> l) {
        super(l);
    }

    public JArray(Collection<RType> s) {
        super(new ArrayList(s));
    }


}