/*
 * ome.api.IConfig
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.api;

//Java imports
import java.util.Date;

//Third-party libraries

//Application-internal dependencies
import ome.annotations.NotNull;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.SecurityViolation;

/** 
 * Access to server configuration. These methods provide access to the state
 * and configuration of the server and its components (e.g. the database). 
 * However, it should not be assumed that two subsequent calls to a proxy for
 * this service will go to the same server due to clustering. 
 * 
 * Not all possible server configuration is available through this API. Some
 * values (such as DB connection info, ports, etc.) must naturally be set 
 * before this service is accessible.
 * 
 * Also used as the main developer example for developing (stateless) ome.api
 * interfaces. See source code documentation for more.
 *
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since   3.0-M3
 */
/* Developer notes:
 * ---------------
 * The two annotations below are activated by setting subversion properties
 * on this class file. These values can then be accessed via ome.system.Version
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public interface IConfig extends ServiceInterface
{

	/* Developer notes:
	 * ---------------
	 * Simple almost hello-world call. There should be almost nothing that 
	 * causes this to throw an exception (except perhaps a Java security policy
	 * file which disallows "new Date()"). Therefore we don't add a throws 
	 * clause here. Anything that is thrown will be wrapped in an 
	 * InternalException
	 * see http://cvs.openmicroscopy.org.uk/tiki/tiki-index.php?page=Omero+Exception+Handling
	 */
	/** checks the current server for it's time. This value may be variant 
	 * depending on whether the service is clustered or not.
	 * 
	 * @return Non-null {@link Date} representation of the server's own time.
	 */
	Date getServerTime();

	/* Developer notes:
	 * ---------------
	 * This call hits the database through JDBC (not our own Hibernate 
	 * infrastructure) and therefore it is more likely that an exception can 
	 * occur. An InternalException will also be thrown (though this may change
	 * as more exceptions are created). We mark it here for general consumption;
	 * readers of the API will want to know why.
	 */
	/** checks the database for it's time using a SELECT statement. 
	 * 
	 * @return Non-null {@link Date} representation of the database's time.
	 * @throws InternalException though any call can throw an InternalException
	 * 	it is more likely that this can occur while contacting the DB. An 
	 *  exception here most likely means (A) a temporary issue with the DB or 
	 *  (B) a SQL dialect issue which must be corrected by the Omero team. 
	 */
	Date getDatabaseTime() throws InternalException;
	
	/* Developer notes:
	 * --------------- 
	 * The @NotNull annotation on the key parameter will cause all managed
	 * method calls on any implementation of this interface to be checked by
	 * ome.annotations.ApiConstraintChecker. This is done before any access to 
	 * the Hibernate session is performed and so balances its own overhead
	 * somewhat.
	 */
	/** retrieve a configuration value from the backend store. Permissions 
	 * applied to the configuration value may cause a {@link SecurityViolation} 
	 * to be thrown.
	 * 
	 * @param key The non-null name of the desired configuration value 
	 * @return The {@link String} value linked to this key, possibly null if not set.
	 * @throws ApiUsageException if the key is null or invalid.
	 * @throws SecurityViolation if the value for the key is not readable.
	 */
	String getConfigValue( @NotNull String key ) 
		throws ApiUsageException, SecurityViolation;
    
	/** set a configuration value in the backend store. Permissions applied to
	 * the configuration value may cause a {@link SecurityViolation} to be thrown.
	 * 
	 * @param key The non-null name of the desired configuration value 
	 * @param value The {@link String} value to assign to the given key. 
	 * @throws ApiUsageException if the key or value is null or invalid.
	 * @throws SecurityViolation if the value is not writable.
	 */
	void setConfigValue( @NotNull String key, @NotNull String value )
		throws ApiUsageException, SecurityViolation;
}
