/*
 * Created on Feb 14, 2005
*/
package org.ome.srv.db.jena;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ome.model.Factory;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.interfaces.GenericService;
import org.ome.model.FollowGroup;
import org.ome.srv.db.GenericStore;
import org.ome.srv.db.NamedQuery;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdql.Query;
import com.hp.hpl.jena.rdql.QueryEngine;
import com.hp.hpl.jena.rdql.QueryResults;
import com.hp.hpl.jena.rdql.ResultBinding;
import com.hp.hpl.jena.rdql.ResultBindingImpl;

/**
 * @author josh
 */
public class JenaGenericStore implements GenericService, GenericStore {

	public List evaluateNamedQuery(NamedQuery nq) {

		Model m = JenaModelFactory.getModel();
		Map map = parseMap(m, nq); 
		QueryResults results = queryWithBinding(m, nq.getQueryString(), map);

		List l = lsObjectFromQueryResult(results, nq.getTarget());
		results.close();
		return l;
	}

	private Map parseMap(Model m, NamedQuery nq) {
		Map map = new HashMap();

		for (Iterator iter = nq.getBindingMap().keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			map.put(key,m.getResource(((LSID)nq.getBindingMap().get(key)).getURI()));//FIXME Assumption also need literals! (Values!)
			//TODO file:///home/josh/lib/jena/doc/ontology/examples/describe-class/DescribeClass.java.html
		}
		return map;
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#setLSOjbect(org.ome.LSObject)
	 */
	public void setLSOjbect(LSObject obj) {
//		 TODO Auto-generated method stub
		throw new RuntimeException("implement me");

	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#updateLSObject(org.ome.LSObject)
	 */
	public void updateLSObject(LSObject obj) {
//		 TODO Auto-generated method stub
		throw new RuntimeException("implement me");

	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObject(org.ome.LSID)
	 */
	public LSObject getLSObject(LSID lsid) {
		LSObject lsobj = null;
		Model m = JenaModelFactory.getModel();
		
		lsobj = Factory.make( lsid.getURI() );
		
		Resource subj = m.getResource(lsid.getURI());
		
		if (! m.contains(subj,null)){
			return null;
		}
		
		for (StmtIterator iter = subj.listProperties(); iter.hasNext();) {
			Property pred = iter.nextStatement().getPredicate();
			List values = new ArrayList();
			for (StmtIterator iter2 = subj.listProperties(pred); iter2.hasNext();) {
				RDFNode obj = iter2.nextStatement().getObject();
				//FIXME needs to be well-defined if List or functional or null accepted!
				//This code needs to be kept insync with ome3-gen! (perhaps utilities somewhere!)
				//undefined what happens if there are multilple values for a functional property
				Object value = null;
				
				if (obj.canAs(Resource.class)){
					LSObject child = null;
					child = Factory.make( ((Resource)obj).getURI() );
					value = child;
				} else if (obj.canAs(Literal.class)){
					value = ((Literal)obj).getValue();
				}
				
				values.add(value);
			}
			
			//FIXME Tricky; refactor out to utils
			if (pred.canAs(OntProperty.class)){
				OntProperty prop = (OntProperty)pred.as(OntProperty.class);
				if (prop.isFunctionalProperty()) { // TODO use myOntPred class and use min max (need utils)
					if (values.size() > 0){
						lsobj.put(pred.getURI(),values.get(0));
					} 
				} else {
				    lsobj.put(pred.getURI(),values);
				}
			} else {
				if (values.size() > 1){
					lsobj.put(pred.getURI(),values);
				} else if (values.size()==1){
					lsobj.put(pred.getURI(),values.get(0));
				}
			}

			
			
		}
		return lsobj;
		 
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObject(org.ome.LSID, org.ome.texen.srv.PredicateGroup)
	 */
	public LSObject getLSObject(LSID lsid, FollowGroup fg) {
//		 TODO Auto-generated method stub
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObjectWithFollowGroup(org.ome.model.LSID, org.ome.model.FollowGroup)
	 */
	public LSObject getLSObjectWithFollowGroup(LSID arg0, FollowGroup arg1) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObjectsByLSIDType(org.ome.model.LSID)
	 */
	public List getLSObjectsByLSIDType(LSID arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObjectsByClassType(java.lang.Class)
	 */
	public List getLSObjectsByClassType(Class arg0) throws RemoteException {
		// TODO Auto-generated method stub
		/* return null; */
		throw new RuntimeException("implement me");
	}
	

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
