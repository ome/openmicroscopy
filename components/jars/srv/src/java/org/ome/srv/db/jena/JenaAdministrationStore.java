/*
 * Created on Feb 12, 2005
 */
package org.ome.srv.db.jena;

import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ome.Factory;
import org.ome.ILSObject;
import org.ome.LSID;
import org.ome.LSObject;
import org.ome.interfaces.AdministrationService;

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
public class JenaAdministrationStore implements AdministrationService {

	com.hp.hpl.jena.rdf.model.RDFNode r;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ome.interfaces.RemoteAdministrationService#retrieveProjectsByExperimenter(int)
	 */
	public List retrieveProjectsByExperimenter(LSID experimenterId)
			throws RemoteException {
		List l = new ArrayList();
		Model m = JenaModelFactory.getModel();

		Query query = new Query(JenaQueries.getProjectsByExperimenterQueryString());

		// Need to set the source if the query does not.
		query.setSource(m);
		QueryEngine qe = new QueryEngine(query);

		ResultBindingImpl binding = new ResultBindingImpl();
		binding.add("exp",m.getResource(experimenterId.getURI()));

		QueryResults results = qe.exec(binding);
		
		for (Iterator iter = results; iter.hasNext();) {
			ResultBinding res = (ResultBinding) iter.next();
			Resource p = (Resource) res.get("project");
			if (null != p) {
				try {
					ILSObject obj = Factory.make(p.getURI());
					l.add(obj);
				} catch (URISyntaxException e) {
					System.err.println("Error constructing LSID "
							+ "\n" + e.getMessage());
				}
			}
		}
		results.close();

		//		for (StmtIterator i = m.listStatements(
		//		    new SimpleSelector(null, m.getProperty(Vocabulary.owner),
		// m.getResource(experimenterId.getURI()))); i.hasNext();) {
		//			String subject = i.nextStatement().getResource().toString();
		//			l.add(subject);
		//		}

		return l;
	}
}