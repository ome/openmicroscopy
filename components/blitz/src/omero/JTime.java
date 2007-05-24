/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero;

public class JTime extends omero.RTime {

    public JTime(long l) {
        super(l);
    }

    public JTime(java.util.Date d) {
        super(d.getTime());
    }
    
}