/*
 * Created on Feb 12, 2005
 */
package org.ome.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ome.exceptions.LSIDObjectException;

/** storage mechanism for an RDF graph of LSID-referenced objects.
 * Currently stores Java Object Primitives and other LSObjects. // FIXME  
 * @author josh
*/
public abstract class LSObject implements Serializable, ILSObject{

	protected LSID lsid;
	Map map = new HashMap();
	
	/** creates an LSObject with a given lsid. The LSID is immutable. */
	public LSObject(LSID lsid){
		this.lsid = lsid;
	}
	
	public boolean put(String predicate, Object object){ // FIXME lsid and runtime checks on object 
		if (object instanceof LSObject ||
				object instanceof Float ||
				object instanceof Double ||
				object instanceof String ||
				object instanceof Integer) {
				map.put(predicate,object);
			return true;
		} else {
			return false;
		}

	}
	
	public Object get(String predicate){
		return map.get(predicate);
	}
	
	public LSID getLSID(){
		return lsid;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append(lsid);
		sb.append("\n");
		for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			String value;
			
			Object o = map.get(key);
			if (o instanceof ILSObject){
				value = ((ILSObject)o).getLSID().getURI();
			} else {
				value = o.toString();
			}
			
			sb.append("\t"+key+" --> "+value+"\n");
		}
		
		return sb.toString();
	}
	
}
