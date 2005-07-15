/*
 * org.openmicroscopy.omero.tests
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

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

    private static Log log = LogFactory.getLog(OMEPerformanceData.class);
    
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
            imgsAnn1 = getImageAnnotations();
            imgsAnn2 = getImageAnnotations();
            dsAnn1 = getDatasetAnnotations();
            dsAnn2 = getDatasetAnnotations();
        }
        return this;
    }
    
    private Set getImageAnnotations(){
        String id = "image_id";
        String query = "select i."+id +" from images i, image_annotations a where i.image_id = a.image_id";
        return getAnnotations(allImgs.size(),id,query);
    }

    private Set getDatasetAnnotations(){
        String id = "dataset_id";
        String query = "select d."+id +" from datasets d, dataset_annotations a where d.dataset_id = a.dataset_id";
        return getAnnotations(allDss.size(),id, query);
    }
    
    private Set getAnnotations(int size, String id, String query){
        JdbcTemplate jt = new JdbcTemplate(ds);
        List rows = jt.queryForList(query);
        Set result = new HashSet();
        for (Iterator i = rows.iterator(); i.hasNext();) {
            Map element = (Map) i.next();
            result.add(element.get(id));
        }
        double newPercent = ( percent * size ) / result.size();
        return getPercentOfCollection(result,newPercent);
    }
    
    
    
}
