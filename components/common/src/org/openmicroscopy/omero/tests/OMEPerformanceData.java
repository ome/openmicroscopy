/*
 * org.openmicroscopy.omero.tests
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.omero.tests;

//Java imports
import java.util.Random;

//Third-party libraries

//Application-internal dependencies

/** 
 * <code>OMEData</code> subclass which adds logic for choosing
 * cardinality of random data by percent. Setting the same 
 * <code>seed</code> value for two independent <code>OMEPerformanceData</code>
 * instances is also assumed to create identical values. 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class OMEPerformanceData extends OMEData {

    // Main field
    public double percent = 0.001;

    public OMEPerformanceData() {
    }

    public OMEPerformanceData(double percent) {
        this.percent = percent;
    }

    public OMEPerformanceData(double percent, long seed) {
        this.percent = percent;
        this.seed = seed;
        this.rnd = new Random(seed);
    }

    public OMEData init() {
        // Test data : calculated before to not change times.
        if (!initialized) {
            super.init();
            imgsPDI = getPercentOfCollection(allImgs, percent);
            imgsCGCI = getPercentOfCollection(allImgs, percent);
            imgsAnn1 = getPercentOfCollection(allImgs, percent);
            imgsAnn2 = getPercentOfCollection(allImgs, percent);
            dsAnn1 = getPercentOfCollection(allDss, percent);
            dsAnn2 = getPercentOfCollection(allDss, percent);
        }
        return this;
    }
}
