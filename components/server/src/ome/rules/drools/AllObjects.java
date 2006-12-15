/*
 * ome.rules.drools.AllObjectsRule
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.rules.drools;

import org.drools.spi.KnowledgeHelper;
import org.drools.spring.metadata.annotation.java.Condition;
import org.drools.spring.metadata.annotation.java.Consequence;
import org.drools.spring.metadata.annotation.java.Fact;
import org.drools.spring.metadata.annotation.java.Rule;

@Rule
public class AllObjects {
	
	    @Condition
	    public boolean objectAsserted(@Fact("object") Object obj) {
	        return true;
	    }
	    
	    @Consequence
	    public void consequence(@Fact("object") Object obj, KnowledgeHelper kh) {
	    	System.out.println("here i am"+obj);//TODO walk the graph!
	    }
	}
