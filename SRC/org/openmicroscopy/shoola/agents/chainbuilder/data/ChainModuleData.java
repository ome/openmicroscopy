/*
 * org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData
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
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.data.MatchMapper;
import org.openmicroscopy.shoola.env.data.model.DataObject;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModuleView;
import org.openmicroscopy.shoola.agents.chainbuilder.ui.ModulePaletteWindow;

/** 
 * An extension of 
 * {@link org.openmicroscopy.shoola.env.data.model.ModuleData}, 
 * adding some state to track {@link ModuleView} visual representations
 * of each module
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
public class ChainModuleData  extends ModuleData
{
	/** 
	 * A hash for associating {@link ModuleView} instances with 
	 * ChainModuleData objects 
	 * 
	 */
	private static MatchMapper moduleNodeMap = new MatchMapper();
	 
	private static ModulePaletteWindow mainWindow = null;

	private static String longestName = null;

	
	public static void setMainWindow(ModulePaletteWindow main) {
		mainWindow=main;
	}
	
	public static String getLongestName() {
		return longestName;
	}
	
	public ChainModuleData() {}
	
	
	/** Required by the DataObject interface. */
	public DataObject makeNew() { return new ChainModuleData(); }

		
	public void addModuleNode(ModuleView mod) {
		moduleNodeMap.addMatch(this.getID(),mod);
	}
	
	public List getModuleNodes() {
		return moduleNodeMap.getMatches(getID());
	}
	
	public void removeModuleNode(ModuleView mod) {
		moduleNodeMap.removeMatch(getID(),mod);
	}
	

	/**
	 * Set modules to be highlighted. Note that this might be better and 
	 * more generally handled by a selection listener model.
	 * 
	 * @param v true if highlighted, else false
	 */
	public void setModulesHighlighted(boolean v) {
		ModuleView m;
		List widgets = getModuleNodes();
		
		
		for  (int i = 0; i < widgets.size(); i++) {
			m = (ModuleView) widgets.get(i);
			m.setHighlighted(v);
			if (v == false)
				m.setParamsHighlighted(v);
		}
		if (mainWindow != null) {
			if (v == true)
				mainWindow.setTreeSelection(this);
			else
				mainWindow.clearTreeSelection();
		}
	}
	
	public void setName(String name) {
		super.setName(name);
		if (longestName == null || name.length() > longestName.length())
			longestName =name;
	}
}
