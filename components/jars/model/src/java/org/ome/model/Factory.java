/*
 * Created on Feb 16, 2005 
*/
package org.ome.model;


/**
 * @author josh
 */
public class Factory {

	public static ILSObject make (String str){
		LSID lsid = null;
		try {
			lsid = new LSID(str);
		} catch (Exception e){
			return null;
		}

		return new OMEObject(lsid);
	}

}
