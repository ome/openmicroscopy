/*
 * org.openmicroscopy.shoola.agents.datamng.PopupMenu
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

package org.openmicroscopy.shoola.agents.datamng;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DataObject;

/** 
 * The UI of the context pop-up menu used within this agent's UI. 
 * Provides buttons for accessing the properties of an object (a project, 
 * dataset or image), viewing an image, browsing a dataset, and reloading
 * data from the DB.
 * The background motif is a repeated pattern made of three lines.
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
class TreePopupMenu
	extends JPopupMenu
{

	//colors used by drawRow1()
	private static final Color    	ROW1_0_20 = new Color(255, 250, 255);
	private static final Color    	ROW1_21 = new Color(251, 249, 252);
	private static final Color    	ROW1_22 = new Color(245, 243, 246);
	private static final Color    	ROW1_23_26 = new Color(244, 242, 245);
	private static final Color    	ROW1_27_END = new Color(244, 244, 244);
	//colors used by drawRow2()
	private static final Color    	ROW2 = new Color(255, 250, 250);
	//colors used by drawRow3()
	private static final Color    	ROW3_0_31 = new Color(195, 220, 214);
	private static final Color    	ROW3_32_35 = new Color(209, 215, 215);
	private static final Color    	ROW3_36 = new Color(224, 225, 227);
	private static final Color    	ROW3_37 = new Color(242, 243, 245);
	private static final Color    	ROW3_38_END = new Color(255, 255, 255);
    
    
	/** This UI component's controller and model. */
	private TreePopupMenuManager	manager;
	
	/** Holds the configuration entries. */
	private Registry				config;
	
	/** 
	 * Button to bring up the property sheet of an object (project, dataset 
	 * or image).
	 */
	JMenuItem   					properties;
	
	/** Button to view an image. */
	JMenuItem   					view;
	
	/** Button to browse a dataset. */
	JMenuItem   					browse;
	
	/** Button to reload data from the DB. */
	JMenuItem   					refresh;

	/** 
	 * Creates a new instance.
	 *
	 *@param    agentCtrl   The agent's control component.
	 */
	TreePopupMenu(DataManagerCtrl agentCtrl, Registry r) 
	{
		this.config = r;
		initProperties();
		initView();
		initBrowse();
		initRefresh();
		manager = new TreePopupMenuManager(this, agentCtrl);
		buildGUI() ;
	}

	/** 
	 * Sets the object (project, dataset or image) the menu is going to 
	 * operate on. 
	 * The view button will be enabled only if the passed object is
	 * an image summary.
	 * The browse button will be enabled only if the passed object is
	 * a dataset summary.
	 *
	 * @param   t  The object for which the menu has to be brought up.
	 */
	void setTarget(DataObject t) { manager.setTarget(t); }
    
	/**  Overrides parent to paint the backgroud motif. */
	public void paintComponent(Graphics g)
	{
		Dimension d = getSize();
		for (int y = 0; y < d.height; y += 3) {
			drawRow1(g, y, d.width);
			drawRow2(g, y+1, d.width);
			drawRow3(g, y+2, d.width);
		} 
	}
    
	/** 
	 * Draws line 1 of background pattern.
	 *
	 * @param g		The graphics context.
	 * @param y		The row to draw.
	 * @param width	The component's width.   
	 */
	private void drawRow1(Graphics g, int y, int width) 
	{
		g.setColor(ROW1_0_20);
		g.drawLine(0, y, 20, y);
		g.setColor(ROW1_21);
		g.drawLine(21, y, 21, y);
		g.setColor(ROW1_22);
		g.drawLine(22, y, 22, y);
		g.setColor(ROW1_23_26);
		g.drawLine(23, y, 26, y);
		if (26 < width) {
			g.setColor(ROW1_27_END);
			g.drawLine(27, y, width, y);
		}
	}
    
	/** 
	 * Draws line 2 of background pattern.
	 *
	 * @param g		The graphics context.
	 * @param y		The row to draw.
	 * @param width	The component's width.   
	 */
	private void drawRow2(Graphics g, int y, int width)
	{
		g.setColor(ROW2);
		g.drawLine(0, y, width, y);
	}
    
	/** 
	 * Draws line 3 of background pattern.
	 *
	 * @param g		The graphics context.
	 * @param y		The row to draw.
	 * @param width	The component's width.   
	 */
	private void drawRow3(Graphics g, int y, int width)
	{
		g.setColor(ROW3_0_31);
		g.drawLine(0, y, 31, y);
		g.setColor(ROW3_32_35);
		g.drawLine(32, y, 35, y);
		g.setColor(ROW3_36);
		g.drawLine(36, y, 36, y);
		g.setColor(ROW3_37);
		g.drawLine(37, y, 37, y);
		if (37 < width) {
			g.setColor(ROW3_38_END);
			g.drawLine(38, y, width, y);
		}
	}
	
	/** Creates and initializes the properties button. */
	private void initProperties() 
	{
		IconManager icons = IconManager.getInstance(config);
		properties = new JMenuItem("Properties", 
									icons.getIcon(IconManager.PROPERTIES));
		properties.setOpaque(false);
		properties.setBorder(null);
		properties.setFont((Font) config.lookup("/resources/fonts/Labels"));
		properties.setForeground(DataManager.STEELBLUE);  
	}

	/** Creates and initializes the view button. */
	private void initView() 
	{
		IconManager icons = IconManager.getInstance(config);
		view = new JMenuItem("View", icons.getIcon(IconManager.VIEWER));
		view.setOpaque(false);
		view.setBorder(null);
		view.setFont((Font) config.lookup("/resources/fonts/Labels"));
		view.setForeground(DataManager.STEELBLUE); 
		view.setEnabled(false);  
	}
	
	/** Creates and initializes the browse button. */
	private void initBrowse() 
	{
		IconManager icons = IconManager.getInstance(config);
		browse = new JMenuItem("Browse", icons.getIcon(IconManager.BROWSER));
		browse.setOpaque(false);
		browse.setBorder(null);
		browse.setFont((Font) config.lookup("/resources/fonts/Labels"));
		browse.setForeground(DataManager.STEELBLUE); 
		browse.setEnabled(false);  
	}
   
	/** Creates and initializes the refresh button. */
	private void initRefresh() 
	{
		IconManager icons = IconManager.getInstance(config);
		refresh = new JMenuItem("Refresh", icons.getIcon(IconManager.REFRESH));
		refresh.setOpaque(false);
		refresh.setBorder(null);
		refresh.setFont((Font) config.lookup("/resources/fonts/Labels"));
		refresh.setForeground(DataManager.STEELBLUE); 
	}

	/** Builds and lays out the GUI. */
	private void buildGUI() 
	{
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		add(properties);
		add(view);
		add(browse);
		add(refresh);
	}

}
