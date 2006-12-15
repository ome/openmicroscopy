/*
 * ome.rules.drools.GraphRule
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

//Java imports
import java.util.Set;

//Third-party libraries
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
 * walks a graph of objects {@see ome.util.ContextFilter} and asserts all objects for further testing.
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since OMERO 3.0
 */
@Rule(salience=Integer.MAX_VALUE)
public class GraphRule {

    private static Log log = LogFactory.getLog(GraphRule.class);
    
    @Condition
    public boolean notYetParsed(@Data("cache") Set dataCache, @Fact("node") Filterable node) {
    	return ! dataCache.contains(node);
	}

	@Consequence
	public void filter(@Data("cache") Set dataCache, @Fact("node") Filterable node, KnowledgeHelper kh){
		Filter f = new Filter(dataCache);
		f.filter("Top-level node",node);
		assertGraph(f.getCache(),kh);
	}
	
	protected void assertGraph(Set s, KnowledgeHelper kh){
		if (s != null){
			try {
				for (Object o : s){
					kh.assertObject(o);
				}
			} catch (Exception e){
				throw new RuntimeException("Error on assert graph objects",e);
			}
		}
	}
}

class Filter extends ContextFilter {
	private Set data_cache;
	public Filter(Set data){
		super();
		data_cache = data;
	}
	@Override
	public Filterable filter(String fieldId, Filterable f) {
		data_cache.add(f);
		return super.filter(fieldId,f);
	}
	@Override
	public Object filter(String fieldId, Object o) {
		addSeen(o);
		return super.filter(fieldId,o);
	}
	public Set getCache(){
		return this._cache.keySet();
	}
}