/*
 * org.openmicroscopy.shoola.env.config.OMEROInfo
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.config;

/** 
 * Holds the configuration information for the <i>OMERO</i> entry in the
 * container's configuration file.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class OMEROInfo
{

    /** The value of the <code>port</code> sub-tag. */ 
    private int        port;
    
    /** The value of the <code>portSSL</code> sub-tag. */ 
    private int        portSSL;
    
    /** The value of the <code>hostName</code> sub-tag. */ 
    private String     hostName;
    
    /** The value of the <code>encrypted</code> sub-tag. */ 
    private boolean     encrypted;
    
    /** 
     * Flag indicating  if the host name can be modified by the user,
     * or not.
     */
    private boolean hostNameConfigurable;
    
    /** 
     * Flag indicating  if the encryption of the data transfer
     * can be modified by the user or not.
     */
    private boolean encryptedConfigurable;
    
    /**
     * Parses the specified string into an integer.
     * 
     * @param value The string holding the value to parse.
     * @return See above.
     * @throws ConfigException If <code>value</code> is not a well-formed 
     *                         integer.
     */
    private int parseInt(String value)
        throws ConfigException
    {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            throw new ConfigException("Malformed integer value: "+value+".");
        }
    }
    
    /**
     * Creates a new instance.
     * This is the only constructor and should have package visibility because 
     * instances of this class can only be created (meaningfully) within this
     * package. However, we made it public to ease testing.
     *
     * @param port The value of the <code>port</code> sub-tag.
     * @param portSSL The value of the <code>portSSL</code> sub-tag.
     * @param hostName The value of the <code>hostname</code> sub-tag.
     * @param encrypted The value of the <code>encrypted</code> sub-tag.
     * @throws ConfigException If <code>port</code> can't be parsed into an 
     *                          integer.
     */
    public OMEROInfo(String port, String portSSL, String hostName,
    		String encrypted)
        throws ConfigException
    {
        this.portSSL = parseInt(portSSL);
        this.port = parseInt(port);
        this.hostName = hostName;
        hostNameConfigurable = true;
        encryptedConfigurable = true;
        if (encrypted == null) this.encrypted = false; 
		else {
			encrypted = encrypted.toLowerCase();
			if (AgentInfo.TRUE.equals(encrypted) ||
				AgentInfo.TRUE.equals(encrypted))
				this.encrypted = true;
			else if (AgentInfo.FALSE.equals(encrypted) ||
				AgentInfo.FALSE_SHORT.equals(encrypted))
				this.encrypted = false;
			else this.encrypted = false;
		}
    }
    
    /**
     * Returns the value of the <code>portSSL</code> sub-tag.
     * 
     * @return See above.
     */
    public int getPortSSL() { return portSSL; }
    
    /**
     * Returns the value of the <code>port</code> sub-tag.
     * 
     * @return See above.
     */
    public int getPort() { return port; }
    
    /**
     * Returns the value of the <code>hostName</code> sub-tag.
     * 
     * @return See above.
     */
    public String getHostName() { return hostName; }
    
    /**
     * Returns the value of the <code>encrypted</code> sub-tag.
     * 
     * @return See above.
     */
    public boolean isEncrypted() { return encrypted; }

    /**
     * Returns <code>true</code> if the host name can be modified by the user,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isHostNameConfigurable() { return hostNameConfigurable; }
    
    /**
     * Returns <code>true</code> if the encryption of the data transfer
     * can be modified by the user, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isEncryptedConfigurable() { return encryptedConfigurable; }
    
    /**
     * Sets to <code>true</code> if the host name can be modified by the user,
     * to <code>false</code> otherwise.
     * 
     * @param hostNameConfigurable See above.
     */
    public void setHostNameConfigurable(boolean hostNameConfigurable)
    { 
    	this.hostNameConfigurable = hostNameConfigurable;
    }
    
    /**
     * Sets to <code>true</code> if the encryption of the data transfer
     * can be modified by the user, to <code>false</code> otherwise.
     * 
     * @param encryptedConfigurable See above.
     */
    public void setEncryptedConfigurable(boolean encryptedConfigurable)
    {
    	this.encryptedConfigurable = encryptedConfigurable;
    }
    
}
