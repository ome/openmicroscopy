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
import java.awt.Font;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.model.ModelPane;
import org.openmicroscopy.shoola.agents.rnd.pane.QuantumPane;
import org.openmicroscopy.shoola.env.config.Registry;

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
	extends JInternalFrame
{
	/** Width of the widget. */
	private static final int 		WIN_WIDTH = 350;
	
	/** Height of the widget. */
	private static final int 		WIN_HEIGHT = 350;
	
	/** Location x-coordinate. */
	private static final int		X_LOCATION = 0;
	
	/** Location y-coordinate. */
	private static final int		Y_LOCATION = 0;
	
	/** index to position Graphic component in the tabbedPane. */
	private static final int		POS_GR = 0 ;
			
	/** index to position Domain component in the tabbedPane. */
	private static final int		POS_D = 1 ;
	
	/** index to position Codomain component in the tabbedPane. */
	private static final int		POS_CD = 2 ;
	
	/** index to position Model component in the tabbedPane. */
	private static final int		POS_M = 3 ;
	
	/** Reference to the regisry. */
	private Registry				registry;
	
	/** Reference to the regisry. */
	private RenderingAgtCtrl		control;
	
	private QuantumPane				quantumPane;
	
	private ModelPane				modelPane;
	
	private JTabbedPane 			tabs;
	
	/** Menu specific to this agent. */
	private JMenu					internalMenu;
	
	RenderingAgtUIF(RenderingAgtCtrl control, Registry registry)
	{
		//name, resizable, closable, maximizable, iconifiable.
		super("Rendering", true, true, true, true);
		this.registry = registry;
		this.control = control;
		setJMenuBar(createMenuBar());
		initPanes();
		buildGUI();
		//set the size and position the window.
		setBounds(X_LOCATION, Y_LOCATION, WIN_WIDTH, WIN_HEIGHT);
	}
	
	/** 
	 * Menu item to add to the 
	 * {@link org.openmicroscopy.shoola.env.ui.TopFrame} menu bar. */
	JMenuItem getViewMenuItem()
	{
		JMenuItem menuItem = new JMenuItem("Rendering");
		control.setMenuItemListener(menuItem, RenderingAgtCtrl.R_VISIBLE);
		return menuItem;
	}
	
	/** 
	 * Sizes, centers and brings up the specified editor dialog.
	 *
	 * @param   editor	The editor dialog.
	 */
	void showDialog(JDialog editor)
	{
		JFrame topFrame = (JFrame) registry.getTopFrame().getFrame();
		Rectangle tfB = topFrame.getBounds(), psB = editor.getBounds();
		int offsetX = (tfB.width-psB.width)/2, 
			offsetY = (tfB.height-psB.height)/2;
		if (offsetX < 0) offsetX = 0;
		if (offsetY < 0) offsetY = 0;
		editor.setLocation(tfB.x+offsetX, tfB.y+offsetY);
		editor.setVisible(true);
	}
	
	/** Set the selected model. */
	void setModelPane(ModelPane pane)
	{
		tabs.remove(POS_M);
		modelPane.removeAll();
		modelPane = pane;
		modelPane.buildComponent();
		tabs.insertTab("Model", null, modelPane, null, POS_M);	
	}
	
	/** Initialize the components. */
	private void initPanes()
	{
		quantumPane = new QuantumPane(control);
		modelPane = control.getModelPane();
		modelPane.buildComponent();
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		Font font = (Font) registry.lookup("/resources/fonts/Titles");
		IconManager im = IconManager.getInstance(registry);
		Icon icon = im.getIcon(IconManager.OME);
		
		//create and initialize the tabs
		tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
		
  		tabs.setAlignmentX(LEFT_ALIGNMENT);
		tabs.setFont(font);
		
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.add(quantumPane.getLayeredPane());
		tabs.insertTab("Graphic", null, p, null, POS_GR);
		tabs.insertTab("Domain", null, quantumPane.getDomainPane(), null,
						POS_D);
		tabs.insertTab("Codomain", null, quantumPane.getCodomainPane(), null, 
						POS_CD);
		tabs.insertTab("Model", null, modelPane, null, POS_M);
  		//set layout and add components
  		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tabs, BorderLayout.CENTER);
		
		setFrameIcon(icon);		
	}
	
	/** Creates an internal menu. */
	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar(); 
		createNewMenu();
		menuBar.add(internalMenu);
	
		return menuBar;
	}

	/** Creates the <code>newMenu</code>. */
	private void createNewMenu()
	{
		internalMenu = new JMenu("Model");
		JMenuItem menuItem = new JMenuItem("GreyScale");
		control.setMenuItemListener(menuItem, RenderingAgtCtrl.GREY);
		internalMenu.add(menuItem);
		menuItem = new JMenuItem("RGB");
		control.setMenuItemListener(menuItem, RenderingAgtCtrl.RGB);
		internalMenu.add(menuItem);
		menuItem = new JMenuItem("HSB");
		control.setMenuItemListener(menuItem, RenderingAgtCtrl.HSB);
		internalMenu.add(menuItem);
		menuItem = new JMenuItem("SAVE");
		control.setMenuItemListener(menuItem, RenderingAgtCtrl.SAVE);
		internalMenu.add(menuItem);
	}
	
}
