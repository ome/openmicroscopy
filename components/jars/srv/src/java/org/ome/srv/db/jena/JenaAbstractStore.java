/*
 * Created on Feb 19, 2005
*/
package org.ome.srv.db.jena;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ome.model.Factory;
import org.ome.model.LSObject;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdql.Query;
import com.hp.hpl.jena.rdql.QueryEngine;
import com.hp.hpl.jena.rdql.QueryResults;
import com.hp.hpl.jena.rdql.ResultBinding;
import com.hp.hpl.jena.rdql.ResultBindingImpl;

/**
 * @author josh
 */
public class JenaAbstractStore {

	protected List lsObjectFromQueryResult(QueryResults results, String variable) {
		List l = new ArrayList();
		for (Iterator iter = results; iter.hasNext();) {
			ResultBinding res = (ResultBinding) iter.next();
			Resource p = (Resource) res.get(variable);
			if (null != p) {
				LSObject obj = Factory.make(p.getURI());
				l.add(obj);
			}
		}
		return l;
	}

	protected QueryResults queryWithBinding(Model m, String queryString, Map map) {

		Query query = new Query(queryString);

		// Need to set the source if the query does not.
		query.setSource(m);
		QueryEngine qe = new QueryEngine(query);

		ResultBindingImpl binding = new ResultBindingImpl();
		for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			binding.add(key, (Resource) map.get(key));
		}
		QueryResults results = qe.exec(binding);
		return results;
	}
	
}
