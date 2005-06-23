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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.sql.DataSource;

//Third-party libraries
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

//Application-internal dependencies

/** 
 * abstract data container for testing. Sub-classes can set whatever values
 * it would like in <code>init()</code>. After the OMEData instance is inserted into 
 * the test class by Spring, it SHOULD not be changed, but this is a matter
 * of opionon. Setting the same <code>seed</code> value for two independent
 * Data instances is also assumed to create identical values. 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public abstract class OMEData {

    private static Log log = LogFactory.getLog(OMEData.class);
    
    boolean initialized = false;

    DataSource ds;

    public void setDataSource(DataSource dataSource) {
        this.ds = dataSource;
    }

    public OMEData() {
    }

    final static String emptyColl = "collections may not be empty";
    long seed = (new Random()).nextLong();
    Random rnd = new Random(seed);

    // Test data : calculated before to not change times.
    public Set allUsers;
    public Set allImgs;
    public Set allDss;
    public Set allPrjs;
    public Set allCgs;
    public Set allCs;
    public int userId;
    public int prjId;
    public int dsId;
    public int cgId;
    public int cId;
    public Set imgsPDI;
    public Set imgsCGCI;
    public Set imgsAnn1;
    public Set imgsAnn2;
    public Set dsAnn1;
    public Set dsAnn2;

    public OMEData init() {
        //      Perhaps generalize on type HOW OFTEN IS THIS USED! Each ONCE ? TODO
        if (!initialized) {
            allUsers = getAllIds("experimenters", "attribute_id");
            allImgs = getAllIds("images", "image_id");
            allDss = getAllIds("datasets", "dataset_id");
            allPrjs = getAllIds("projects", "project_id");
            allCgs = getAllIds("category_groups", "attribute_id");
            allCs = getAllIds("categories", "attribute_id");

            userId = getOneFromCollection(allUsers); // Perhaps generalize on type HOW OFTEN IS THIS USED! Each ONCE ?
            prjId = getOneFromCollection(allPrjs);
            dsId = getOneFromCollection(allDss);
            cgId = getOneFromCollection(allCgs);
            cId = getOneFromCollection(allCs);
            /*
             * for example. should be done in sub-classes
             * 
             *imgsPDI = getPercentOfCollection(allImgs, percent);
             *imgsCGCI = getPercentOfCollection(allImgs, percent);
             *imgsAnn1 = getPercentOfCollection(allImgs, percent);
             *imgsAnn2 = getPercentOfCollection(allImgs, percent);
             *dsAnn1 = getPercentOfCollection(allDss, percent);
             *dsAnn2 = getPercentOfCollection(allDss, percent);
             *
             */
        }
        return this;
    }

    Set getAllIds(String table, String field) {
        JdbcTemplate jt = new JdbcTemplate(ds);
        List rows = jt.queryForList("select " + field + " from " + table);
        Set result = new HashSet();
        for (Iterator i = rows.iterator(); i.hasNext();) {
            Map element = (Map) i.next();
            result.add(element.get(field));
        }
        return result;
    }

    int getOneFromCollection(final Collection ids) {

        if (ids.size() == 0) {
            throw new IllegalArgumentException(emptyColl);
        }

        List ordered = new ArrayList(ids);
        int choice = randomChoice(ids.size());
        return ((Integer) ordered.get(choice)).intValue();
    }

    Set getPercentOfCollection(final Set ids, double percent) {

        if (ids.size() == 0) {
            throw new IllegalArgumentException(emptyColl);
        }

        List ordered = new ArrayList(ids);
        Set result = new HashSet();
       
        while (ordered.size() >0 && result.size() < ids.size() * percent) {
            int choice = randomChoice(ordered.size());
            result.add(ordered.remove(choice));
        }

        return result;
    }

    int randomChoice(int size) {
        double value = (size - 1) * rnd.nextDouble();
        return (new Double(value)).intValue();
    }
   
    public String toString(){
    return new ToStringBuilder(this).
    	append("seed",seed ).
    	append("userId",userId).
    	append("prjId",prjId).
    	append("dsId",dsId).
    	append("cgId",cgId).
    	append("cId",cId).
    	append("imgsPDI",imgsPDI).
    	append("imgsCGCI",imgsCGCI).
    	append("imgsAnn1",imgsAnn1).
    	append("imgsAnn2",imgsAnn2).
    	append("dsAnn1",dsAnn1).
    	append("dsAnn2",dsAnn2).
    	toString();
	}
}
