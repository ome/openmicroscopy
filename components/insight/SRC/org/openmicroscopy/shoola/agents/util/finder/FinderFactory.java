/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.util.finder;

import java.util.Collection;

import javax.swing.JFrame;

import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import omero.gateway.SecurityContext;

import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Factory to create {@link Finder}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class FinderFactory
{
	
	/** The sole instance. */
    private static final FinderFactory  singleton = new FinderFactory();
    
	/**
	 * Creates or recycles an advanced search.
	 * 
	 * @param reg Reference to the registry. Mustn't be <code>null</code>.
	 * @param groups The available groups.
	 * @return See above.
	 */
	public static AdvancedFinder getAdvancedFinder(Registry reg,
			Collection<GroupData> groups)
	{
		return FinderFactory.getAdvancedFinder(reg, groups, null);
	}
	
	/**
	 * Creates or recycles an advanced search.
	 * 
	 * @param reg Reference to the registry. Mustn't be <code>null</code>.
	 * @param groups The available groups.
	 * @param refObject	Object of reference. The search is limited to that 
	 * 					object.
	 * @return See above.
	 */
	public static AdvancedFinder getAdvancedFinder(Registry reg,
			Collection<GroupData> groups, DataObject refObject)
	{
		if (singleton.registry == null) singleton.registry = reg;
		return (AdvancedFinder) singleton.createFinder(groups, refObject);
	}
	
	/**
	 * Creates or recycles an advanced search.
	 * 
	 * @param ctx	Reference to the registry. Mustn't be <code>null</code>.
	 * @return See above.
	 */
	public static QuickFinder getQuickFinder(Registry ctx)
	{
		return FinderFactory.getQuickFinder(ctx, null);
	}
	
	/**
	 * Creates or recycles an advanced search.
	 * 
	 * @param ctx		Reference to the registry. Mustn't be <code>null</code>.
	 * @param refObject	Object of reference. The search is limited to that 
	 * 					object.
	 * @return See above.
	 */
	public static QuickFinder getQuickFinder(Registry ctx, DataObject refObject)
	{
		if (singleton.registry == null) singleton.registry = ctx;
		return (QuickFinder) singleton.createQuickFinder(refObject);
	}
	
	 /**
     * Helper method. 
     * 
     * @return A reference to the {@link Registry}.
     */
    public static Registry getRegistry() { return singleton.registry; }
    
    /**
	 * Returns the task bar frame.
	 * 
	 * @return See above.
	 */
	public static JFrame getRefFrame()
	{
		return singleton.registry.getTaskBar().getFrame();
	}
	
    /**
	 * Helper method returning the current user's details.
	 * 
	 * @return See above.
	 */
	public static ExperimenterData getUserDetails()
	{ 
		return (ExperimenterData) singleton.registry.lookup(
								LookupNames.CURRENT_USER_DETAILS);
	}
	
	
	 /** Reference to the registry. */
    private Registry	registry;
    
    /** The tracked component. */
    private Finder		finder;
    
    /** Creates a new instance. */
	private FinderFactory()
	{
		//finder = null;
	}
	
	/**
	 * Creates the finder.
	 * 
	 * @param groups The available groups.
	 * @param refObject	Object of reference. The search is limited to that 
	 * 					object.
	 * @return See above.
	 */
	private Finder createFinder(Collection<GroupData> groups,
			DataObject refObject)
	{
		if (finder != null)
			return finder;
		finder = new AdvancedFinder(groups);
		return finder;
	}
	
	/**
	 * Creates the finder.
	 * 
	 * @param refObject	Object of reference. The search is limited to that 
	 * 					object.
	 * @return See above.
	 */
	private Finder createQuickFinder(DataObject refObject)
	{
		//if (advancedFinder != null) {
			//advancedFinder.setFocusOnSearch();
			//advancedFinder.set
		//	return advancedFinder;
		//}
		//finder = new 
		return new QuickFinder();
	}
	
}
