/*
 * org.openmicroscopy.shoola.agents.chainbuilder.data.ModuleLoader
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
import org.openmicroscopy.shoola.agents.chainbuilder.ChainBuilderAgent;
import org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.env.data.model.FormalParameterData;
import org.openmicroscopy.shoola.env.data.model.SemanticTypeData;
import org.openmicroscopy.shoola.util.data.ContentGroup;
import org.openmicroscopy.shoola.util.data.ContentLoader;



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
	private ModulesData modData;
	
	public ModuleLoader(final ChainDataManager dataManager,
			final ContentGroup group) {
		super(dataManager,group);
		start();
	}	
	
	/**
	 * Do the work
	 */
	public Object getContents() {
		long modstart = System.currentTimeMillis();
		if (ChainBuilderAgent.DEBUG_TIMING)
			modstart = System.currentTimeMillis();
		ChainDataManager chainDataManager = (ChainDataManager) dataManager;
		if (modData == null) {
			modData = new ModulesData();
			long loadStart = System.currentTimeMillis();
			// IT'S tempting to think here that one can grab the modules and work
			// back to infer the category tree, but that doesn't work. Since 
			// categories need not contain modules, this approach might miss 
			// thoese categories that only contain other categories.
			// It might be possible to just get the categories and work from their
			// down to the individual modules, but that would require painful
			// and ugly rewrite of the module category mapper, so I'll punt on
			// it for now, and hope a more robust solution is developed.
			Collection mods = chainDataManager.getModules();
			Collection cats = chainDataManager.getModuleCategories();
			long loadEnd;
			if (ChainBuilderAgent.DEBUG_TIMING) {
				loadEnd= System.currentTimeMillis()-loadStart;
				System.err.println("time for get modules and get categories.."+loadEnd);
			}
			
			// find uncategorized modules
			Iterator iter = mods.iterator();
			while (iter.hasNext()) {
				ChainModuleData m = (ChainModuleData) iter.next();
				if (m.getModuleCategory()== null)
					modData.addUncategorizedModule(m);
			}
			
			// find root categories
			if (cats != null) {
				iter = cats.iterator();
				while (iter.hasNext()) {
					ModuleCategoryData c = (ModuleCategoryData) iter.next();
					if (c.getParentCategory() == null) 
						modData.addRootModuleCategory(c);
					findCategoryModules(c,mods);
					findCategoryChildren(c,cats);
				}
			}
		}
		if (ChainBuilderAgent.DEBUG_TIMING) {
			long modend = System.currentTimeMillis()-modstart;
			if (modend > 0)
				System.err.println("total time spent in module get contents.."+modend);
		}
		return modData;
	}
	
	/**
	 *  The categories don't come with their modules associated. So, 
	 *  we need to take the list of modules that we found, 
	 *  find all of those in this category, and associate the resulting 
	 *  list with the category
	 * @param cat
	 * @param mods
	 */
	private void findCategoryModules(ModuleCategoryData cat,Collection mods) {
		
		int id  = cat.getID();
		ModuleCategoryData modCat;
		ChainModuleData mod;
		Iterator iter = mods.iterator();
		Vector modules = new Vector();
		while (iter.hasNext()) {
			mod = (ChainModuleData) iter.next();
			modCat = mod.getModuleCategory();
			if (modCat != null && id == modCat.getID()) 
				modules.add(mod); 
		}
		cat.setModules(modules);
	}
	
	/**
	 * Similarly, we have to build up the list of children for each category
	 * @param cat the category for which we're building up the children
	 * @param cats the list of catgegories
	 */
	public void findCategoryChildren(ModuleCategoryData cat,Collection cats) {
		int id = cat.getID();
		ModuleCategoryData modCat;
		ModuleCategoryData parent;
		Iterator iter = cats.iterator();
		Vector children = new Vector();
		while (iter.hasNext()) {
			modCat = (ModuleCategoryData) iter.next();
			parent = modCat.getParentCategory();
			if (parent != null && parent.getID() == id)
				children.add(modCat); 
		}
		cat.setChildCategories(children);
	}
	
		
	private void dumpModules() {
		if (modData == null) return;
		
		System.err.println("uncategorized modules....");
		System.err.println("=========================");
		Collection uncats = modData.uncategorizedModules();
		Iterator iter = uncats.iterator();
		ChainModuleData mod;
		while (iter.hasNext()) {
			mod = (ChainModuleData) iter.next();
			dumpModule(mod);
		}
		
		System.err.println("Root Categories...");
		System.err.println("==================");
		
		iter = modData.rootCategories().iterator();
		while (iter.hasNext()) {
			ModuleCategoryData m = (ModuleCategoryData) iter.next();
			dumpCategory(m);
			Collection children = m.getChildCategories();
			if (children != null && children.size() > 0)
				dumpChildCategories(children);
		}
	}
	
	private void dumpModule(ChainModuleData mod) {
		System.err.println("Module: "+mod.getID()+": "+mod.getName());
		/*System.err.println("Inputs: ");
		List params = mod.getFormalInputs();
		dumpParams(params);
		System.err.println("Outputs: ");
		params = mod.getFormalOutputs();
		dumpParams(params);*/
	}
	
	private void dumpParams(Collection params) {
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
	
	private void dumpCategory(ModuleCategoryData m) {
		System.err.println("\nCategory..."+m.getName());
		ChainModuleData mod;
		Collection mods = m.getModules();
		if (mods != null) {
			Iterator iter2 = mods.iterator();
			while (iter2.hasNext()) {
				mod = (ChainModuleData) iter2.next();
				dumpModule(mod);
			}
		} 	
	}
	
	private void dumpChildCategories(Collection children) {
		System.err.println("\nchild categories...");
		Iterator iter = children.iterator();
		ModuleCategoryData m;
		while (iter.hasNext()) {
			m = (ModuleCategoryData) iter.next();
			dumpCategory(m);
		}
	}
}