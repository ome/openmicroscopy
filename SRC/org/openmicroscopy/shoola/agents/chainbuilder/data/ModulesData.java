/*
 * org.openmicroscopy.shoola.agents.chainbuilder.data.ModulesData
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

package org.openmicroscopy.shoola.agents.chainbuilder.data;

//Java imports
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;



/** 
 * A container class for information about which modules have no
 * categories, and which categories have no parents
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ModulesData
{
	private Collection uncategorizedModules = new Vector();
	private Collection rootModuleCategories = new Vector();
	
	public ModulesData() {
	}
	
	public void addUncategorizedModule(ChainModuleData module) {
		uncategorizedModules.add(module);	
	}
	
	public void addRootModuleCategory(ModuleCategoryData category) {
		rootModuleCategories.add(category);
	}
	
	public Iterator uncategorizedModulesIterator() {
		return uncategorizedModules.iterator();
	}
	
	public Iterator rootCategoriesIterator() {
		return rootModuleCategories.iterator();
	}
	
	public int getUncategorizedCount() {
		return uncategorizedModules.size();
	}
}