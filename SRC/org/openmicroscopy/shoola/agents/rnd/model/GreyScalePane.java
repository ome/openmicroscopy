/*
 * org.openmicroscopy.shoola.agents.rnd.model.GreyScaleMapping
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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.agents.rnd.RenderingAgt;
import org.openmicroscopy.shoola.env.data.model.ChannelData;

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
public class GreyScalePane
	extends ModelPane
{
								
	private IconManager 			im;
	
	private GreyScalePaneManager	manager;
	
	private JPanel					contents;
	
	public GreyScalePane()
	{ 
		manager = new GreyScalePaneManager();
	}
	
	/** Re-build the component. */
	public void buildComponent()
	{
		manager.setEventManager(eventManager);
		im = IconManager.getInstance(eventManager.getRegistry());
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
	
	private void buildBody()
	{
		contents = new JPanel();
		contents.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		GridBagLayout gridbag = new GridBagLayout();
		contents.setLayout(gridbag);
		GridBagConstraints cst = new GridBagConstraints();
		cst.ipadx = RenderingAgt.H_SPACE;
		cst.weightx = 0.5;
		cst.fill = GridBagConstraints.HORIZONTAL;
		cst.anchor = GridBagConstraints.EAST;
		ChannelData[] channelData = eventManager.getChannelData();
		ButtonGroup group = new ButtonGroup();
		for (int i = 0; i < channelData.length; i++)
			addRow(gridbag, cst, group, i, channelData[i], 
					eventManager.isActive(i));
		
	}
	
	
	/** Build a row in the table. */
	private void addRow(GridBagLayout gridbag, GridBagConstraints c, 
						ButtonGroup group, int index, ChannelData data, 
						boolean active)
	{
		//init JButton
		JButton b = new JButton();
		b.setBorder(null);
		b.setIcon(im.getIcon(IconManager.INFO));
		
		//init JLabel
		String s = " Wavelength "+data.getNanometer();
		JLabel label = new JLabel(s);
		
		//init radioButton
		JRadioButton rb = new JRadioButton();
		rb.setSelected(active);
		group.add(rb);
		
		c.gridx = 0;
		c.gridy = index;
		JPanel p = buttonPanel(b);
		gridbag.setConstraints(p, c);
		contents.add(p);
		c.gridx = 1;
		gridbag.setConstraints(label, c);
		contents.add(label);
		c.gridx = 2;
		gridbag.setConstraints(rb, c);
				contents.add(rb);
		//attach listeners to the object
		manager.attachObjectListener(b, index);
		manager.attachObjectListener(rb, index);
	}
	
	/** Display a button in a JPanel. */
	private JPanel buttonPanel(JButton button)
	{
		JPanel p = new JPanel();
		p.add(button);
		return p;
	}
	
}
