/*
 * org.openmicroscopy.shoola.agents.rnd.pane.CodomaimPane
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

package org.openmicroscopy.shoola.agents.rnd.pane;

//Java imports
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.agents.rnd.RenderingAgtUIF;
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
class CodomainPane
	extends JPanel
{
	
	private JButton					cStretching;
	private JButton					pSlicing;
	private JCheckBox				ri;
	private JCheckBox				cs;
	private JCheckBox				ps;
	
	private CodomainPaneManager		manager;
	
	public CodomainPane(Registry registry, QuantumPaneManager control)
	{
		//TODO: retrieve Data from CodomainMapDefs.
		manager = new CodomainPaneManager(this, control);
		initComponents(registry);
		manager.attachListeners();
		buildGUI();
	}

	/** Getters. */
	CodomainPaneManager getManager() { return manager;}
	
	/** Contrast Stretching checkBox. */
	JCheckBox getCS() { return cs; }

	/** Contrast Stretching button. */
	JButton getCStretching() { return cStretching; }

	/** Plane slicing checkBox. */
	JCheckBox getPS() { return ps; }

	/** Plane slicing button. */
	JButton getPSlicing() { return pSlicing; }

	/** Reverse intensity checkBox. */
	JCheckBox getRI() { return ri; }
	
	/** Initialize the checkboxes and buttons. */
	private void initComponents(Registry registry)
	{
		IconManager im = IconManager.getInstance(registry);
		cStretching = new JButton(im.getIcon(IconManager.STRETCHING));
		cStretching.setBorder(null);
		pSlicing = new JButton(im.getIcon(IconManager.SLICING));
		pSlicing.setBorder(null);
		cStretching.setEnabled(false);
		pSlicing.setEnabled(false);
		//TODO: check according to user settings.
		ri = new JCheckBox();
		cs = new JCheckBox();
		ps = new JCheckBox();	
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		add(buildBody());
	}
	
	/** Build and lay out the main panel. */
	private JPanel buildBody()
	{
		JPanel body = new JPanel();
		body.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		GridBagLayout gridbag = new GridBagLayout();
		body.setLayout(gridbag);
		GridBagConstraints c = new GridBagConstraints();
	
		JLabel label = new JLabel(" Reverse Intensity");
		c.ipadx = RenderingAgtUIF.H_SPACE;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.EAST;
		//c.insets = TOP_PADDING;
		gridbag.setConstraints(label, c);
		body.add(label);
		label = new JLabel(" Contrast Stretching");
		c.gridy = 1;
		gridbag.setConstraints(label, c);
		body.add(label);
		label = new JLabel(" Plane Slicing");
		c.gridy = 2;
		gridbag.setConstraints(label, c);
		body.add(label);
		//checkbox
		c.gridx = 1;
		c.gridy = 0;
		gridbag.setConstraints(ri, c);
		body.add(ri);
		c.gridy = 1;
		gridbag.setConstraints(cs, c);
		body.add(cs);
		c.gridy = 2;
		gridbag.setConstraints(ps, c);
		body.add(ps);
		//buttons if any.
		c.gridx = 2;
		c.gridy = 1;
		JPanel p = buildButtonPanel(cStretching);
		gridbag.setConstraints(p, c);
		body.add(p);
		c.gridy = 2;
		p = buildButtonPanel(pSlicing);
		gridbag.setConstraints(p, c);
		body.add(p);
		return body;
	}

	/**
	 * Build a JPanel which contains a JButton.
	 * 
	 * @param button
	 */
	private JPanel buildButtonPanel(JButton button)
	{
		JPanel p = new JPanel();
		p.add(button);
		return p;
	}

}
