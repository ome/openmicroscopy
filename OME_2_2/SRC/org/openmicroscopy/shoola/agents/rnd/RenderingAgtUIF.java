/*
 * org.openmicroscopy.shoola.agents.rnd.RenderingAgtUIF
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

package org.openmicroscopy.shoola.agents.rnd;

//Java imports
import java.awt.BorderLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.controls.ToolBar;
import org.openmicroscopy.shoola.agents.rnd.model.ModelPane;
import org.openmicroscopy.shoola.agents.rnd.pane.QuantumPane;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.TopWindow;

/** 
 * 
 *
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
class RenderingAgtUIF 
	extends TopWindow
{
	
	/** index to position Mapping component in the tabbedPane. */
	static final int				POS_MAPPING = 0 ;
	
	/** index to position Model component in the tabbedPane. */
	static final int				POS_MODEL = 1 ;
	
	/** index to position Codomain component in the tabbedPane. */
	static final int				POS_CD = 2;
	
	/** Reference to the regisry. */
	private Registry				registry;
	
	/** Reference to the control. */
	private RenderingAgtCtrl		control;
	
	private QuantumPane				quantumPane;
	
	private ModelPane				modelPane;
	
	private JTabbedPane				tabs;
	
	private JPanel					mappingPanel;
	
	private IconManager				im;
	
	/**
	 * Create a new instance.
	 * 
	 * @param control		Reference to the controller {@link RenderingAgtCtrl}
	 *  					of this agent.
	 * @param registry		Reference to the {@link Registry registry}.
	 * @param name			Name of the current image displayed.
	 */
	RenderingAgtUIF(RenderingAgtCtrl control, Registry registry, String name)
	{
		super("Rendering "+name);
		this.registry = registry;
		this.control = control;
		im = IconManager.getInstance(registry); 
		setJMenuBar(createMenuBar());
		initPanes();
		buildMappingPanel();
		buildGUI();
		pack();
	}
	
	QuantumPane getQuantumPane() { return quantumPane; }
	
	JTabbedPane getTabs() { return tabs; }
	
	/** Set the selected model. */
	void setModelPane(ModelPane pane, boolean b)
	{
		tabs.remove(POS_MODEL);
		modelPane.removeAll();
		modelPane = pane;
		modelPane.buildComponent();
		tabs.insertTab(control.getModelType()+" Model", control.getModelIcon(), 
						modelPane, null, POS_MODEL);
		if (b) tabs.setSelectedIndex(POS_MODEL);	
	}

	/** Set the mapping pane when a new wavelength is selected. */
	void setMappingPane()
	{
		tabs.remove(POS_MAPPING);
		mappingPanel.removeAll();
		buildMappingPanel();
		tabs.insertTab("Mapping", im.getIcon(IconManager.MAPPING), mappingPanel,
						null, POS_MAPPING);
		tabs.setSelectedIndex(POS_MAPPING);	
	}
	
	/** Reset the default values for the GUI. */
	void resetGUI(ModelPane pane)
	{
		setModelPane(pane, false);
		setMappingPane();
	}
	
	/** Initialize the components. */
	private void initPanes()
	{
		quantumPane = new QuantumPane(control);
		modelPane = control.getModelPane();
		modelPane.buildComponent();
		mappingPanel = new JPanel();
		mappingPanel.setLayout(new BorderLayout(0, 10));
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{	
		//create and initialize the tabs
		tabs = new JTabbedPane(JTabbedPane.TOP, 
												JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setAlignmentX(LEFT_ALIGNMENT);
		tabs.insertTab("Mapping", im.getIcon(IconManager.MAPPING), mappingPanel,
						 null, POS_MAPPING);
		tabs.insertTab(control.getModelType()+" Model", 
							im.getIcon(IconManager.GREYSCALE), modelPane, null, 
							POS_MODEL);
		tabs.insertTab("Options", im.getIcon(IconManager.CODOMAIN), 
							quantumPane.getCodomainPane(), null, POS_CD);
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(new ToolBar(control, registry), 
							BorderLayout.NORTH);
		getContentPane().add(tabs, BorderLayout.CENTER);
		//setIconImage(IconManager.getOMEImageIcon());		
	}
	
	/** Build a panel with graphics and control. */
	private void buildMappingPanel()
	{
		mappingPanel.setLayout(new BorderLayout(0, 0));
		mappingPanel.add(quantumPane.getGRPane(), BorderLayout.NORTH);
		mappingPanel.add(quantumPane.getDomainPane(), BorderLayout.CENTER);
	}
	
	/** Creates an internal menu. */
	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar(); 
		menuBar.add(createModelMenu());
		menuBar.add(createMenu());
		return menuBar;
	}

	private JMenu createMenu()
	{
		JMenu menu = new JMenu("Controls");
		/*
		JMenuItem menuItem = new JMenuItem("Save", 
									im.getIcon(IconManager.SAVE_SETTINGS));
		control.setMenuItemListener(menuItem, RenderingAgtCtrl.SAVE);
		menu.add(menuItem);
		*/
		JMenuItem menuItem = new JMenuItem("Reset defaults", 
								im.getIcon(IconManager.RESET_DEFAULTS));
		control.setMenuItemListener(menuItem, RenderingAgtCtrl.RESET_DEFAULTS);
		menu.add(menuItem);
		return menu;
	}
	
	/** Creates the <code>color model</code>. */
	private JMenu createModelMenu()
	{
		JMenu menu = new JMenu("Model");
		JMenuItem menuItem = new JMenuItem("GreyScale", 
								im.getIcon(IconManager.GREYSCALE));
		control.setMenuItemListener(menuItem, RenderingAgtCtrl.GREY);
		menu.add(menuItem);
		menuItem = new JMenuItem("RGB", im.getIcon(IconManager.RGB));
		control.setMenuItemListener(menuItem, RenderingAgtCtrl.RGB);
		menu.add(menuItem);
		menuItem = new JMenuItem("HSB", im.getIcon(IconManager.HSB));
		control.setMenuItemListener(menuItem, RenderingAgtCtrl.HSB);
		menu.add(menuItem);
		
		return menu;
	}
	
}
