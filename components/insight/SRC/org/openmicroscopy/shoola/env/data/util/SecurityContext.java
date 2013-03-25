/*
 * org.openmicroscopy.shoola.env.data.util.SecurityContext 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.util;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Hosts information required to access correct connector.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class SecurityContext
{

	/** The identifier of the group.*/
	private long groupID;
	
	/** The experimenterID if required.*/
	private long experimenterID;
	
	/** The name of the server.*/
	private String host;
	
	/** The port to use.*/
	private int port;
	
	/** The compression level. */
	private float compression;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param groupID The identifier of the group.
	 */
	public SecurityContext(long groupID)
	{
		this.groupID = groupID;
		experimenterID = -1;
	}
	
	/**
	 * Sets the experimenter ID.
	 * 
	 * @param experimenterID The id of the experimenter.
	 */
	public void setExperimenter(long experimenterID)
	{
		this.experimenterID = experimenterID;
	}
	
	/**
	 * Returns the id of the experimenter.
	 * 
	 * @return See above.
	 */
	public long getExperimenter() { return experimenterID; }
	
	/**
	 * Sets the information used to connect to the correct server.
	 * 
	 * @param host The name of the server.
	 * @param port The port to use.
	 */
	public void setServerInformation(String host, int port)
	{
		this.host = host;
		this.port = port;
	}
	
	/**
	 * Sets the information used to connect to the correct server.
	 * 
	 * @param host The name of the server.
	 */
	public void setServerInformation(String host)
	{
		this.host = host;
	}
	
	/** 
	 * Returns the hostname.
	 * 
	 * @return See above.
	 */
	public String getHostName() { return host; }
	
	/**
	 * Sets the compression level.
	 * 
	 * @param compression The value to set.
	 */
	public void setCompression(float compression)
	{
		this.compression = compression;
	}
	
	/**
	 * Returns the compression.
	 * 
	 * @return See above.
	 */
	public float getCompression() { return compression; }
	
	/**
	 * Returns the identifier of the group.
	 * 
	 * @return See above.
	 */
	public long getGroupID() { return groupID; }
	
	
	/**
	 * Returns a copy of the security context.
	 * 
	 * @return See above.
	 */
	public SecurityContext copy()
	{
		SecurityContext ctx = new SecurityContext(groupID);
		ctx.setCompression(this.compression);
		ctx.setExperimenter(this.experimenterID);
		ctx.setServerInformation(this.host, this.port);
		return ctx;
	}
}
