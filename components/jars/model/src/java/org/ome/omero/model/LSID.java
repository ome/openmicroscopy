/*
 * Created on Feb 12, 2005
 */
package org.ome.model;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/** a reference mechanism used throughout the OME code. Stores
 * an java.net.URI internally for validation of new LSIDs.
 * @author josh
 */
public class LSID implements Serializable {

	protected String uri; //URI uri;
	
	/** create an LSID based on a String version of URI. While creating the java.net.URI an exception may be thrown. */
	public LSID(String uri) throws URISyntaxException{
		this.uri = uri ; //new URI(uri); FIXME
	    
	}

	/**
	 * @return
	 */
	public String getURI() {
		return uri.toString();
	}
	
	public String toString(){
		return this.getURI();
	}
}
