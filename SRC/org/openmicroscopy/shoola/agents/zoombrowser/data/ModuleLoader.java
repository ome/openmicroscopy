/*
 * org.openmicroscopy.shoola.agents.zoombrowser.data.ModuleLoader
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

package org.openmicroscopy.shoola.agents.zoombrowser.data;

//Java imports
import java.util.List;
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.zoombrowser.DataManager;

import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.env.data.model.FormalParameterData;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;



/** 
 * A {@link ComponentContentLoader} subclass for loading projects.
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
public class ModuleLoader extends ContentLoader
{
	
	public ModuleLoader(final DataManager dataManager,
			final ContentGroup group) {
		super(dataManager,group);
		start();
	}	
	
	/**
	 * Do the work
	 */
	public List getContents() {
		List res = dataManager.getModules();
		List cats = dataManager.getModuleCategories();
		System.err.println(res.size()+" modules loaded");
		if (cats != null)
			System.err.println(cats.size()+" module categories");
		return res;
	}
	
	public void completeInitialization() {
	
		List res = getContents();	
		dumpModules(res);
	}
	
	private void dumpModules(List res) {
		Iterator iter = res.iterator();
		while (iter.hasNext()) {
			ModuleData mod = (ModuleData) iter.next();
			dumpModule(mod);
		}
	}
	
	private void dumpModule(ModuleData mod) {
		System.err.println("Module: "+mod.getID()+": "+mod.getName());
		ModuleCategoryData cd = mod.getModuleCategory();
		if (cd != null)
			System.err.println("Category: "+cd.getID()+": "+cd.getName());
		System.err.println("Inputs: ");
		List params = mod.getFormalInputs();
		dumpParams(params);
		System.err.println("Outputs: ");
		params = mod.getFormalOutputs();
		dumpParams(params);
	}
	
	private void dumpParams(List params) {
		Iterator iter = params.iterator();
		while (iter.hasNext()) {
			FormalParameterData f = (FormalParameterData) iter.next();
			dumpParam(f);
		}
	}
	
	private void dumpParam(FormalParameterData param) {
		System.err.println(param.getID()+": "+param.getName());
		SemanticTypeData st = param.getSemanticType();
		if (st != null) {
			System.err.println("Type: "+st.getID()+": "+st.getName());
		}
	}
}