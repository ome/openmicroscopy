/*
 * org.openmicroscopy.shoola.agents.chainbuilder.ModulePaletteWindow
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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainBuilderAgent;
import org.openmicroscopy.shoola.agents.chainbuilder.ChainDataManager;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainExecutionLoader;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainLoader;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ChainModuleData;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ModuleLoader;
import org.openmicroscopy.shoola.agents.chainbuilder.data.ModulesData;
import 
	org.openmicroscopy.shoola.agents.chainbuilder.piccolo.ModulePaletteCanvas;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.data.model.ModuleCategoryData;
import org.openmicroscopy.shoola.env.ui.TopWindow;
import org.openmicroscopy.shoola.env.ui.TopWindowManager;
import org.openmicroscopy.shoola.util.data.ContentGroup;
import org.openmicroscopy.shoola.util.data.ContentGroupSubscriber;

import org.openmicroscopy.shoola.util.ui.Constants;


/** 
 * A top level window containing analysis modules that might be used in a chain
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </smalbl>
 * @since OME2.2
 */
public class ModulePaletteWindow 
	extends TopWindow implements TreeSelectionListener, ComponentListener, 
		ContentGroupSubscriber
{
	
	public static int SIDE=200;
	
	/** the ui manager */
	private final UIManager uiManager;
	
	/** the data manager for this instance */
	private ChainDataManager dataManager;
	
	/** Cached reference to access the icons. */
	private IconFactory icons;
			
	/* module data loader */
	private ModuleLoader modLoader;
	
	/** The split pane in the window. */
	private JSplitPane split;
	
	/** The command table */
	private CmdTable cmdTable;
	
	/** The module moduleCanvas */
	private ModulePaletteCanvas moduleCanvas =null;

	/** The tree control */
	private JTree tree;	
	

	/** 
	 * A flag to prevent re-entrant UI event calls on the JTRee
	 */
	private boolean lockTreeChange = false;
	
	/** the last chain that was higlighted */
	private ChainModuleData lastModule;
	
	
	/** the top window manager */
	private TopWindowManager topWindowManager;
	
	/* has the data been loaded ?*/
	private int dataState=0;
	
	private final static int NOT_LOADED=0;
	private final static int LOADING=1;
	private final static int LOADED=2;
	
	
	/**
	 * Creates a new instance.
	 */
	public ModulePaletteWindow(final UIManager uiManager,
			ChainDataManager dataManager,CmdTable cmdTable)
	{
		//We have to specify the title of the window to the superclass
		//constructor and pass a reference to the TaskBar, which we get
		//from the Registry.
		super("Chain Builder Preview: Module Palette", dataManager.getTaskBar());
		this.uiManager = uiManager;
		this.cmdTable = cmdTable;
		this.dataManager = dataManager;
		icons = dataManager.getIconFactory();
		
		configureDisplayButtons();
		enableButtons(true);
		
	}
	
	/**
	 * Specifies names, icons, and tooltips for the quick-launch button and the
	 * window menu entry in the task bar.
	 */
	private void configureDisplayButtons()
	{
		Icon chainIcon = icons.getIcon("chains.png");
		configureQuickLaunchBtn(chainIcon, "Chain Builder");
		configureWinMenuEntry("Chain Builder", icons.getIcon("chains.png"));
	}
	
	/** Builds and lays out this window. */
	public void buildGUI(ModulesData modData)
	{
		Container content = getContentPane();
		content.setLayout(new BorderLayout());
		
		// what is the window that the chain modules are in?
		ChainModuleData.setMainWindow(this);
		JToolBar  tb = new JToolBar();
		Icon smallChain = icons.getIcon("chains-small.png");
		JButton newChain = new JButton(smallChain);
		newChain.setToolTipText("Create a new chain");
		newChain.addActionListener(cmdTable.lookupActionListener("new chain"));
		tb.setFloatable(false);
		tb.add(newChain);
		content.add(tb,BorderLayout.NORTH);
		moduleCanvas = new ModulePaletteCanvas(this);
		moduleCanvas.setContents(modData);
		long start;
		if (ChainBuilderAgent.DEBUG_TIMING)
			start = System.currentTimeMillis();
		moduleCanvas.layoutContents();
		if (ChainBuilderAgent.DEBUG_TIMING) {
			long end = System.currentTimeMillis()-start;
			System.err.println("time to layout module palette.."+end);
		}
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,null,moduleCanvas);
		split.setPreferredSize(new Dimension(2*SIDE,SIDE));
		configureTreeNode();
		content.add(split,BorderLayout.CENTER); 
		
		addComponentListener(this);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				uiManager.closeWindows();
				setVisible(false);
			}
		});
	}
	
	
	public void setDividerLocation(int h) {
		if (split != null)
			split.setDividerLocation(h);
	}
	
	
	public void configureTreeNode() {
		ModuleTreeNode node = moduleCanvas.getModuleTreeNode();
		
		JScrollPane treePanel = null;

		if (node != null) {
			tree = new JTree(node);
			tree.setCellRenderer(new ModuleTreeCellRenderer());
			tree.setBackground(Constants.CANVAS_BACKGROUND_COLOR);
			tree.setRootVisible(false);
			tree.setEditable(false);
			tree.setExpandsSelectedPaths(true);
			tree.getSelectionModel().
				setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(this);
			treePanel = new JScrollPane(tree);
			treePanel.setBackground(Constants.CANVAS_BACKGROUND_COLOR);
			split.setLeftComponent(treePanel);
			split.setDividerLocation(0);
			split.setOneTouchExpandable(true);
			split.setResizeWeight(0.25);
		}
	}

	private class ModuleTreeCellRenderer extends DefaultTreeCellRenderer {
		
		ModuleTreeCellRenderer() {
			setBackgroundNonSelectionColor(Constants.CANVAS_BACKGROUND_COLOR);
		}
		
		public Component getTreeCellRendererComponent(JTree tree,Object value,
				boolean sel, boolean expanded,boolean leaf,int row,
				boolean hasFocus) {
			super.getTreeCellRendererComponent(tree,value,sel,expanded,
					leaf,row,hasFocus);
			setBackground(Constants.CANVAS_BACKGROUND_COLOR);
			return this;
		}
	}
	
	/**
	 * A Listener for the {@JTree} of module names and categories
	 */
	public void valueChanged(TreeSelectionEvent e) {
	
		// If this change is occurring because a
		if (lockTreeChange == true)
			return;
		
		
		ModuleTreeNode node = 
			(ModuleTreeNode) tree.getLastSelectedPathComponent();
		if (node == null) 
			return;
		
		if (lastModule != null) {
			moduleCanvas.unhighlightModules(lastModule);
		}
		if (node.isLeaf()) { // it's a module
			lastModule = (ChainModuleData) node.getObject();
			moduleCanvas.highlightModule(lastModule);
		}
		else if (node.getObject() instanceof ModuleCategoryData) {
			lastModule = null;
			ModuleCategoryData mc = (ModuleCategoryData) node.getObject();
			moduleCanvas.highlightCategory(mc);
		
		}
		// clicked on uncategorized
		else if (node.getName() != null && 
				node.getName().compareTo(ModuleTreeNode.UNCAT_NAME) ==0)
			moduleCanvas.highlightCategory(null);
		else
			moduleCanvas.unhighlightModules();
	}

	public void clearTreeSelection() {
		if (tree != null)
			tree.clearSelection();
	}

	public void setTreeSelection(ChainModuleData mod) {
	
		if (tree == null) 
			return;
		int rowCount = tree.getRowCount();
		for (int i =0; i < rowCount; i++) {
			TreePath path = tree.getPathForRow(i);
			Object obj = path.getLastPathComponent();
			if (obj instanceof ModuleTreeNode) {
				ModuleTreeNode modNode = (ModuleTreeNode) obj;
				//if (modNode.isLeaf() && modNode.getID() == mod.getID())  {
				if (modNode.getObject() == mod) {
					// We don't want the valueChanged call to get executed here.
					lockTreeChange= true;
					tree.setSelectionPath(path);
					lockTreeChange = false;
					return;
				}
			}
		}
		tree.clearSelection();
	}
	
	public void componentHidden(ComponentEvent e) {
	}
	
	public void componentMoved(ComponentEvent e) {
	}
	
	public void componentResized(ComponentEvent e) {
		if (moduleCanvas != null)
			moduleCanvas.scaleToResize();
	}
	
	public void componentShown(ComponentEvent e) {
		uiManager.showWindows();
	}
	
	long totalTime = 0;
	long start;
	public void preHandleDisplay(TopWindowManager manager) {
		if (dataState == NOT_LOADED) {
			start = System.currentTimeMillis();
			topWindowManager = manager;
			ContentGroup group = new ContentGroup(this);
			modLoader = new ModuleLoader(dataManager,group);
			ChainExecutionLoader execLoader = new ChainExecutionLoader(dataManager,group);
			ChainLoader chainLoader = new ChainLoader(dataManager,group);
			
			group.setAllLoadersAdded();
			dataState = LOADING;
		}
		else if (dataState == LOADED)
			topWindowManager.continueHandleDisplay();
	}
	
	public void contentComplete() {
		if (dataManager.getChains() != null || dataManager.getModules() != null) {
			long guiStart =System.currentTimeMillis();
			buildGUI((ModulesData) modLoader.getContents());
			if (ChainBuilderAgent.DEBUG_TIMING) {
				long guiTime =System.currentTimeMillis()-guiStart;
				System.err.println("time spent on module palette .."+guiTime);
			}
			uiManager.contentComplete();
			topWindowManager.continueHandleDisplay();
			if (ChainBuilderAgent.DEBUG_TIMING) {
				totalTime = System.currentTimeMillis()-start;
				System.err.println("time for chainbuilder start..."+totalTime);
			}
			dataState = LOADED;
		}
	}
	
}
