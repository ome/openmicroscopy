/*
 * Created on Feb 14, 2005
*/
package org.ome.srv.db.jena;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ome.ILSObject;
import org.ome.LSID;
import org.ome.LSObject;
import org.ome.interfaces.GenericService;
import org.ome.texen.srv.PredicateGroup;

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
public class JenaGenericService implements GenericService {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#setLSOjbect(org.ome.LSObject)
	 */
	public void setLSOjbect(ILSObject obj) {
		throw new RuntimeException("implement me");//FIXME

	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#updateLSObject(org.ome.LSObject)
	 */
	public void updateLSObject(ILSObject obj) {
		throw new RuntimeException("implement me");//FIXME

	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObject(org.ome.LSID)
	 */
	public ILSObject getLSObject(LSID lsid) {
		ILSObject lsobj = null;
		Model m = JenaModelFactory.getModel();
		
		lsobj = new LSObject(lsid);
		
		Resource subj = m.getResource(lsid.getURI());
		
		if (! m.contains(subj,null)){
			return null;
		}
		
		for (StmtIterator iter = subj.listProperties(); iter.hasNext();) {
			Property pred = (Property) iter.nextStatement().getPredicate();
			for (StmtIterator iter2 = subj.listProperties(pred); iter2.hasNext();) {
				RDFNode obj = (RDFNode) iter2.nextStatement().getObject();
				
				if (obj.canAs(Resource.class)){
					ILSObject child = null;
					try {
						child = new LSObject(new LSID(((Resource)obj).getURI()));
						lsobj.put(pred.getURI(),child);
					} catch (URISyntaxException e) {
						// TODO what exactly is the condition here
						e.printStackTrace();
					}
				} else if (obj.canAs(Literal.class)){
					lsobj.put(pred.getURI(),((Literal)obj).getValue());	
				}
				
				
			}
			
		}
		return lsobj;
		 
	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#getLSObject(org.ome.LSID, org.ome.texen.srv.PredicateGroup)
	 */
	public ILSObject getLSObject(LSID lsid, PredicateGroup pg) {
		throw new RuntimeException("implement me");//FIXME	}
	}
}
