/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ui.UIManager
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

package org.openmicroscopy.shoola.agents.chainbuilder.ui;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainLoader;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainExecutionLoader;
import org.openmicroscopy.shoola.agents.chainbuilder.data.layout.LayoutChainData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ModuleLoader;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ModulesData;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.TopWindowGroup;
import org.openmicroscopy.shoola.util.data.ContentGroup;
import org.openmicroscopy.shoola.util.data.ContentGroupSubscriber;


/** 
 * Creates and controls the {@link ModulePaletteWindow}.
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * after code by 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class UIManager implements ContentGroupSubscriber
{

	/** Strore the data manager */
	private ChainDataManager manager;
	/**
	 * Inherits from {@link TopWindow}, so it's automatically linked to the
	 * {@link TaskBar}.
	 */
	private ModulePaletteWindow			mainWindow;
	
	/** The chain library window */
	private ChainPaletteWindow chainWindow=null;
	
	/** Command mapping */
	private CmdTable cmdTable;
	
	private int chainFrameCount = 0;
	
	/** registry */
	private Registry config;
		
	/* loader */
	private ModuleLoader modLoader;
	/** 
	 * Manages all the {@link ChainFrame}s that we've created and not
	 * destroyed yet.
	 */
	private TopWindowGroup		chainGroup;
	
	/** is this the first time the windows are being shown?*/
	private boolean firstShowing = true;
	
	/** icon factory */
	private IconFactory icons;
	
	/** the current chain frame */
	private ChainFrame currentChainFrame;
	/**
	 * Creates a new instance.
	 * 
	 * @param config	A reference to this agent's registry.
	 */
	public UIManager(ChainDataManager  manager)
	{	
		this.manager = manager;
		cmdTable = new CmdTable(this);
		mainWindow = new ModulePaletteWindow(this,manager,cmdTable);
		chainWindow = new ChainPaletteWindow(manager);
		config = manager.getRegistry();
		IconFactory icons = manager.getIconFactory();
		chainGroup = new TopWindowGroup("chains",icons.getIcon("chains.png"), config.getTaskBar());
		
		ContentGroup group = new ContentGroup(this);
		modLoader = new ModuleLoader(manager,group);
		ChainExecutionLoader execLoader = new ChainExecutionLoader(manager,group);
		ChainLoader chainLoader = new ChainLoader(manager,group);
		
		group.setAllLoadersAdded();
	}
		
	/**
	 * Releases all UI resources currently in use and returns them to the OS.
	 */
	public void disposeUI()
	{
		mainWindow.dispose();
		chainGroup.removeAll(true);
	}
	
	public void newChain() {
		ChainFrame frame = new ChainFrame(++chainFrameCount,manager,cmdTable,this);
		chainGroup.add(frame,frame.getTitle(),null);
	}
	
	public void showWindows() {
		setWindowsVisibility(true);
		// the first time the windows come up, 
		// have the center contents.
		if (firstShowing == true) {
			chainWindow.focusOnPalette();
			mainWindow.focusOnPalette();
			firstShowing = false;
		}
	}
	
	public void closeWindows() {
		setWindowsVisibility(false);
	}
	
	private void setWindowsVisibility(boolean v) {
		if (chainWindow != null)
			chainWindow.setVisible(v);
	}
	
	public void contentComplete() {
		ModulesData modData = (ModulesData) modLoader.getContents();
		mainWindow.buildGUI(modData);
		chainWindow.buildGUI();
	}
	
	public void setCurrentChainFrame(ChainFrame frame) {
		currentChainFrame = frame;
	}
	
	public void saveCurrentChainFrame() {
		if (currentChainFrame == null)
			return;
		System.err.println("saving frame..."+currentChainFrame);
		currentChainFrame.save();
	}
	
	public void updateChainPalette(LayoutChainData chain) {
		if (chainWindow != null) 
			chainWindow.displayNewChain(chain);
	}
}
