/*
 * org.openmicroscopy.shoola.agents.viewer.ViewerUIF
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

package org.openmicroscopy.shoola.agents.viewer;


//Java imports
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.canvas.ImageCanvas;
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
public class ViewerUIF
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
	
	/** Canvas to display the currently selected 2D image. */
	private ImageCanvas             canvas;
	
	/** Menu specific to this agent. */
	private JMenu					internalMenu;
	
	private ViewerCtrl 				control;
	
	private Registry				registry;
	
	ViewerUIF(ViewerCtrl control, Registry registry)
	{
		//name, resizable, closable, maximizable, iconifiable.
		super("Viewer", true, true, true, true);
		this.control = control;
		this.registry = registry;
		setJMenuBar(createMenuBar());
		buildGUI();
		//set the size and position the window.
		setBounds(X_LOCATION, Y_LOCATION, WIN_WIDTH, WIN_HEIGHT);
	}
	
	/** 
	 * Menu item to add to the 
	 * {@link org.openmicroscopy.shoola.env.ui.TopFrame} menu bar.
	 */
	JMenuItem getViewMenuItem()
	{
		JMenuItem menuItem = new JMenuItem("Viewer2D");
		control.setMenuItemListener(menuItem, ViewerCtrl.V_VISIBLE);
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
	
	/**
	 * Displays the image in the viewer.
	 * 
	 * @param img
	 */
	 void setImage(BufferedImage img)
	 {
		canvas.display(img);
		revalidate();
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
		internalMenu = new JMenu("Controls");
		JMenuItem menuItem = new JMenuItem("Control");
		control.setMenuItemListener(menuItem, ViewerCtrl.CONTROL);
		internalMenu.add(menuItem);
		menuItem = new JMenuItem("SAVE AS...");
		control.setMenuItemListener(menuItem, ViewerCtrl.SAVE_AS);
		internalMenu.add(menuItem);
	}	
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		canvas = new ImageCanvas(this, getContentPane());
		JScrollPane scrollPane = new JScrollPane(canvas);
		getContentPane().add(scrollPane);
		IconManager im = IconManager.getInstance(registry);
		Icon icon = im.getIcon(IconManager.OME);
		setFrameIcon(icon);
	}
	
}
