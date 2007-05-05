/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero;

public class JTime extends omero.RTime {

    public JTime() {
        super(true,null);
    }

    public JTime(long l) {
        super(false,null);
        val = new Time(l);
    }

    public JTime(Time t) {
        super(t==null,t);
    }
}