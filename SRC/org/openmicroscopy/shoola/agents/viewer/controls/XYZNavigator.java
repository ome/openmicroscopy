/*
 * org.openmicroscopy.shoola.agents.viewer.controls.XYZNavigator
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

package org.openmicroscopy.shoola.agents.viewer.controls;



//Java imports
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.viewer.IconManager;
import org.openmicroscopy.shoola.agents.viewer.ViewerCtrl;

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
class XYZNavigator
	extends JPanel
{
	/** Displays the spatial dimensions (XYZ) of the image. */
	private JLabel          		dimsInfo;
	/** The slider used to move across the Z stack. */
	private JSlider         		zSlider;
	/** Text field to allow user to specify a Z point. */
	private JTextField      		zField;
	/** Displays the current position of the cursor within the image. */    
	private JLabel          		xyInfo;
	
	/** The current and maximum Z. */
	private int             curZ, maxZ;
	
	private XYZNavigatorManager		manager;
	
	private IconManager				im;
	
	//TODO: retrieve real data.
	XYZNavigator(ViewerCtrl eventManager)
	{
		manager = new XYZNavigatorManager(this, eventManager);
		im = IconManager.getInstance(eventManager.getRegistry());
		initSlider(100);
		initTextField(0);
		initXYInfo();
		manager.attachListeners();
		curZ = 0;
		maxZ = 0;
		buildGUI(0, 0);
	}
	
	public JTextField getZField()
	{
		return zField;
	}

	public JSlider getZSlider()
	{
		return zSlider;
	}

	/** 
	* Instantiates and initializes to <code>curZ</code> the Z slider.
	* 
	* @param max     Total number of ticks.
	*/
	private void initSlider(int max)
	{
		zSlider = new JSlider(JSlider.HORIZONTAL, 0, max, curZ);
		zSlider.setToolTipText("Move the slider to navigate across Z stack");
	}
    
	/** 
	* Instantiates and initializes to <code>curZ</code> the Z text field.
	*
	* @param max     Total number of planes in the Z-stack. 
	*/
	private void initTextField(int max)
	{
		zField = new JTextField(""+curZ, (""+max).length());
		zField.setForeground(NavigationPalette.STEELBLUE);
		zField.setToolTipText("Enter a Z point");
	}
    
	/** 
	* Instantiates and initializes the label containing info about 
	* the current position of the cursor within the image displayed by 
	* the viewer. 
	*/
	private void initXYInfo()
	{
		xyInfo = new JLabel("x: ");
		setXY(0, 0);
	}
	
	/** 
	* Sets the label containing info about the current position of 
	* the cursor within the image displayed by the viewer. 
	*
	* @param x   The X-position.
 	* @param y   The Y-position.
 	*/
	private void setXY(int x, int y)
	{
		String  html = "<html><table colspan=0 rowspan=0 border=0><tr>";
		html += "<td>x:<br>y:</td>";
		html += "<td align=right>"+x+"<br>"+y+"</td>";
		html += "</tr></table><html>";
		xyInfo.setText(html);
	}
	
	/** 
	* Resets the dimensions label on the new spatial dimensions.
	*
	* @param sizeX	Total number of pixels along the x axis.
	* @param sizeY	Total number of pixels along the y axis.
	* @param sizeZ	Total number of planes in the Z-stack.
	*/
	private void setXYZDimensions(int sizeX, int sizeY, int sizeZ)
	{
		String  html = "<html><table colspan=0 rowspan=0 border=0><tr>";
		html += "<td>Size X:<br>Size Y:<br>Size Z:</td>";
		html += "<td align=right>"+sizeX+"<br>"+sizeY+"<br>"+sizeZ+"</td>";
		html += "</tr></table><html>";
		dimsInfo.setText(html);
	}
	
	/** Build and layout the GUI. */
	private void buildGUI(int x, int y)
	{
		JPanel topControls = new JPanel();
		topControls.setLayout(new BoxLayout(topControls, BoxLayout.X_AXIS)); 
		topControls.add(buildZPanel(x, y));
		topControls.add(Box.createHorizontalGlue());
		topControls.add(buildXYPanel());
		add(topControls);
		add(zSlider);
	}
	
	/** 
 	* Builds a panel containing the Z text field along with a labels indicating 
 	* the image dimensions across X, Y, and Z. 
	*
	* @param sizeX	The maximum X.
	* @param sizeY	The maximum Y.
	* @return	The above mentioned panel.
	*/
	private JPanel buildZPanel(int maxX, int maxY)
	{
		JPanel  p = new JPanel(), field = new JPanel();
		JLabel  current = new JLabel("Current Z: ");
		dimsInfo = new JLabel();
		setXYZDimensions(maxX, maxY, maxZ);
		current.setForeground(NavigationPalette.STEELBLUE);
		field.add(current);
		field.add(zField);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		dimsInfo.setAlignmentX(LEFT_ALIGNMENT);
		field.setAlignmentX(LEFT_ALIGNMENT);
		p.add(dimsInfo);
		p.add(field);
		return p;
	}
    
	/** 
	* Builds a panel containing info about the current position of the 
	* cursor within the image displayed by the viewer.
 	*
 	* @return	The above mentioned panel.
 	*/
	private JPanel buildXYPanel()
	{
		JPanel  p = new JPanel(), xy = new JPanel(), iconPanel = new JPanel();
		xy.add(xyInfo);
		JLabel  icon = new JLabel(im.getIcon(IconManager.CURSOR));
		icon.setToolTipText("Current position of the cursor on the image");
		iconPanel.add(icon);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		xy.setAlignmentY(CENTER_ALIGNMENT);
		iconPanel.setAlignmentY(CENTER_ALIGNMENT);
		p.add(iconPanel);
		p.add(xy);
		p.add(Box.createRigidArea(new Dimension(5, 5)));
		return p;
	} 

}
