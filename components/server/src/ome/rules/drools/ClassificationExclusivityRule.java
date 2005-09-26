/*
 * ome.rules.drools.ClassificationExclusivityRule
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.rules.drools;

//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Classification;
import ome.model.Image;
import ome.util.ContextFilter;
import ome.util.Filterable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.spi.KnowledgeHelper;
import org.drools.spring.metadata.annotation.java.Condition;
import org.drools.spring.metadata.annotation.java.Consequence;
import org.drools.spring.metadata.annotation.java.Data;
import org.drools.spring.metadata.annotation.java.Fact;
import org.drools.spring.metadata.annotation.java.Rule;

//Application-internal dependencies

/**
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 3.0
 */
@Rule
public class ClassificationExclusivityRule {

    private static Log log = LogFactory.getLog(ClassificationExclusivityRule.class);
    
    @Condition
    public boolean classification(Classification cla) {
    	Set<Image> images = new HashSet<Image>();
    	Image i = cla.getImage();
    	Category c = cla.getCategory();
    	CategoryGroup cg = c.getCategoryGroup();
    	
    	Set<Category> cs = cg.getCategories();
    	for (Category _c : cs) {
    		if (_c != c) {
    			Set<Classification> clas = _c.getClassifications();
    			for (Classification _cla : clas) {
    				images.add(_cla.getImage());
    			}
    		}
    	}
    	
    	return images.contains(i);
	}

	@Consequence
	public void die(){
		throw new RuntimeException("Having the same image in TWO categories of the same category group is not allowed.");
	}
}