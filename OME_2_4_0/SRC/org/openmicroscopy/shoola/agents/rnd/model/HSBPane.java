/*
 * org.openmicroscopy.shoola.agents.rnd.model.HSBMapping
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

package org.openmicroscopy.shoola.agents.rnd.model;

//Java imports
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.agents.rnd.RenderingAgtUIF;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.util.ui.ColoredButton;
import org.openmicroscopy.shoola.util.ui.IColorChooser;

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
public class HSBPane
	extends ModelPane 
    implements IColorChooser
{

	private IconManager 			im;
	
	private HSBPaneManager			manager;
	
	private JPanel					contents;
	
	public HSBPane()
	{
		manager = new HSBPaneManager(this);
	}

	public void buildComponent()
	{
		im = IconManager.getInstance(eventManager.getRegistry());
		manager.setEventManager(eventManager);
		buildGUI();
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		buildBody();
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		add(contents);
	}

	/** Build the main component. */
	private void buildBody()
	{
		contents = new JPanel();
		contents.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		GridBagLayout gridbag = new GridBagLayout();
		contents.setLayout(gridbag);
		GridBagConstraints cst = new GridBagConstraints();
		cst.ipadx = RenderingAgtUIF.H_SPACE;
		cst.weightx = 0.5;
		cst.fill = GridBagConstraints.HORIZONTAL;
		cst.anchor = GridBagConstraints.EAST;
		ChannelData[] channelData = eventManager.getChannelData();
		Color color;
		int[] rgba;
		boolean active;
		for (int i = 0; i < channelData.length; i++) {
			rgba = eventManager.getRGBA(i);
			active = eventManager.isActive(i);
			color = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
			addRow(gridbag, cst, i, channelData[i], color, active);
		}
	}
	
	/** Build a row in the table. */
	private void addRow(GridBagLayout gridbag, GridBagConstraints c, int index,
						ChannelData data, Color color, boolean active)
	{
		//init JButton
		JButton b = new JButton();
		b.setIcon(im.getIcon(IconManager.INFO));
		b.setBorder(null);
		
		//init JLabel
		String s = " Wavelength "+data.getNanometer();
		JLabel label = new JLabel(s);
		
		//init CheckBox
		JCheckBox box = new JCheckBox();
		if (active) box.setSelected(true);
		
		//init Color button
		ColoredButton colorButton = new ColoredButton();
		colorButton.setBorder(BorderFactory.createLineBorder(
                            RenderingAgtUIF.COLORBUTTON_BORDER));
		colorButton.setBackground(color);
		colorButton.setPreferredSize(RenderingAgtUIF.COLORBUTTON_DIM);
		colorButton.setSize(RenderingAgtUIF.COLORBUTTON_DIM);
		c.gridx = 0;
		c.gridy = index;
		JPanel p = buttonPanel(b);
		gridbag.setConstraints(p, c);
		contents.add(p);
		c.gridx = 1;
		gridbag.setConstraints(label, c);
		contents.add(label);
		c.gridx = 2;
		gridbag.setConstraints(box, c);
		contents.add(box);
		p = buttonPanel(colorButton);
		c.gridx = 3;
		gridbag.setConstraints(p, c);
		contents.add(p);
		//attach listener.
		manager.attachObjectListener(b, index);
		manager.attachObjectListener(box, index);
		manager.attachObjectListener(colorButton, index);
	}
	
	/** Display a button in a JPanel. */
	private JPanel buttonPanel(JButton button)
	{
		JPanel p = new JPanel();
		p.add(button);
		return p;
	}

    /** Implemented as specified by the I/F. */
    public JFrame getReferenceFrame()
    {
        return eventManager.getReferenceFrame();
    }

    /** Implemented as specified by the I/F. */
    public void setColor(int index, Color color) 
    {
        manager.setRGBA(index, color);
    }
    
    public Registry getRegistry() { return eventManager.getRegistry(); }
	
}