/*
 * trunk.components.common.src.ome.api.IScript
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package ome.api;

// Java imports
import java.util.List;
import java.util.Map;
// Third-party libraries

// Application-internal dependencies
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;

/*
 * Developer notes: The two annotations below are activated by setting
 * subversion properties on this class file. These values can then be accessed
 * via ome.system.Version
 */
//@RevisionDate("$Date: 2008-02-12 $")
//@RevisionNumber("$Revision: 1 $")
public interface IScript 
	extends ServiceInterface 
{
	/**
	* 
	*
	*/
	List<String> getScripts() throws ApiUsageException, SecurityViolation;
	long getScriptID(String scriptName) throws ApiUsageException, SecurityViolation;
	long uploadScript(String script) throws ApiUsageException, SecurityViolation;
	String getScript(String name) throws ApiUsageException;
	Map getParams(String script) throws ApiUsageException;
	Map runScript(String script, Map paramMap) throws ApiUsageException, SecurityViolation;
}
