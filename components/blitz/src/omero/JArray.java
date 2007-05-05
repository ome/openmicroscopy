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

    public JArray() {
        super(true,null);

    }

    public JArray(RType...rtypes) {
        super(false,Arrays.asList(rtypes));
    }

    public JArray(List<RType> l) {
        super(false,l);
    }

    public JArray(Collection<RType> s) {
        super(false,new ArrayList(s));
    }


}