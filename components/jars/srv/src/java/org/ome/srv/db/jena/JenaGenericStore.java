/*
 * Created on Feb 14, 2005
*/
package org.ome.srv.db.jena;

import java.rmi.RemoteException;
import java.util.List;

import org.ome.model.Factory;
import org.ome.model.LSID;
import org.ome.model.LSObject;
import org.ome.interfaces.GenericService;
import org.ome.model.FollowGroup;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author josh
 */
public class JenaGenericStore implements GenericService {

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#setLSOjbect(org.ome.LSObject)
	 */
	public void setLSOjbect(LSObject obj) {
		throw new RuntimeException("implement me");//FIXME

	}

	/* (non-Javadoc)
	 * @see org.ome.interfaces.GenericService#updateLSObject(org.ome.LSObject)
	 */
	public void updateLSObject(LSObject obj) {
		throw new RuntimeException("implement me");//FIXME

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
			Property pred = (Property) iter.nextStatement().getPredicate();
			for (StmtIterator iter2 = subj.listProperties(pred); iter2.hasNext();) {
				RDFNode obj = (RDFNode) iter2.nextStatement().getObject();
				
				if (obj.canAs(Resource.class)){
					LSObject child = null;
					child = Factory.make( ((Resource)obj).getURI() );
					lsobj.put(pred.getURI(),child);
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
	public LSObject getLSObject(LSID lsid, FollowGroup fg) {
		throw new RuntimeException("implement me");//FIXME	}
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
}
