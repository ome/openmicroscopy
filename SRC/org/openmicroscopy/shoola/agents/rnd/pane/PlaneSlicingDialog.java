/*
 * org.openmicroscopy.shoola.agents.rnd.pane.PlaneSlicingDialog
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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.agents.rnd.RenderingAgt;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.codomain.PlaneSlicingContext;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

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
class PlaneSlicingDialog
	extends JDialog
{
	
	private static final String			TEXT = 
									"Highlight a specific gray-level range.";
	
	private static final Dimension		DIM = new Dimension(
												PlaneSlicingPanel.WIDTH,
												PlaneSlicingPanel.HEIGHT),
										DIM_ALL = new Dimension(
												2*PlaneSlicingPanel.WIDTH,
												PlaneSlicingPanel.HEIGHT);
														
	private static final String[]   	RANGE;

	static {
		RANGE = new String[7];
		RANGE[PlaneSlicingDialogManager.B_ONE] = "1-bit plane";
		RANGE[PlaneSlicingDialogManager.B_TWO] = "2-bit plane";
		RANGE[PlaneSlicingDialogManager.B_THREE] = "3-bit plane";
		RANGE[PlaneSlicingDialogManager.B_FOUR] = "4-bit plane";
		RANGE[PlaneSlicingDialogManager.B_FIVE] = "5-bit plane";
		RANGE[PlaneSlicingDialogManager.B_SIX] = "6-bit plane";
		RANGE[PlaneSlicingDialogManager.B_SEVEN] = "7-bit plane";         
	}
	
	private static final HashMap		rBitPlanes;
	
	static {
		rBitPlanes = new HashMap();
		rBitPlanes.put(new Integer(PlaneSlicingContext.BIT_ONE), 
						new Integer(PlaneSlicingDialogManager.B_ONE));
		rBitPlanes.put(new Integer(PlaneSlicingContext.BIT_TWO), 
						new Integer(PlaneSlicingDialogManager.B_TWO));
		rBitPlanes.put(new Integer(PlaneSlicingContext.BIT_THREE), 
						new Integer(PlaneSlicingDialogManager.B_THREE));
		rBitPlanes.put(new Integer(PlaneSlicingContext.BIT_FOUR), 
						new Integer(PlaneSlicingDialogManager.B_FOUR));
		rBitPlanes.put(new Integer(PlaneSlicingContext.BIT_FIVE), 
						new Integer(PlaneSlicingDialogManager.B_FIVE));
		rBitPlanes.put(new Integer(PlaneSlicingContext.BIT_FIVE), 
						new Integer(PlaneSlicingDialogManager.B_FIVE));
		rBitPlanes.put(new Integer(PlaneSlicingContext.BIT_SIX), 
						new Integer(PlaneSlicingDialogManager.B_SIX));
		rBitPlanes.put(new Integer(PlaneSlicingContext.BIT_SEVEN), 
						new Integer(PlaneSlicingDialogManager.B_SEVEN));
	}
	
	private	JRadioButton				radioStatic, radioDynamic;
	
	private JComboBox					range;
		
	private PlaneSlicingPanel			psPanel;
	
	private PlaneSlicingStaticPanel		pssPanel;
	
	private PlaneSlicingDialogManager	manager;

	PlaneSlicingDialog(QuantumPaneManager control, PlaneSlicingContext psCtx)
	{
		super(control.getReferenceFrame(), "Plane Slicing", true);	
		manager = new PlaneSlicingDialogManager(this, control, psCtx);
		int yStart, yEnd, s, e; 
		s = control.getCodomainStart();
		e = control.getCodomainEnd();
		yStart = PlaneSlicingPanel.topBorder+
				manager.convertRealIntoGraphics(psCtx.getLowerLimit(), s-e, e);
		yEnd = PlaneSlicingPanel.topBorder+
				manager.convertRealIntoGraphics(psCtx.getUpperLimit(), s-e, e);
		psPanel = new PlaneSlicingPanel(yStart, yEnd);
		pssPanel = new PlaneSlicingStaticPanel();
		boolean constant = psCtx.IsConstant();
		psPanel.setIsSelected(constant);
		pssPanel.setIsSelected(!constant);
		Integer pSelected = new Integer(psCtx.getPlaneSelected());
		Integer j = (Integer) rBitPlanes.get(pSelected);
		int i = PlaneSlicingDialogManager.B_SEVEN;
		if (j != null) i = j.intValue();
		initialize(i, constant); 
		manager.attachListeners();
		buildGUI(control.getRegistry());
	}

	JComboBox getRange() { return range; }
	
	JRadioButton getRadioStatic() { return radioStatic; }

	JRadioButton getRadioDynamic() { return radioDynamic;}
	
	/** Returns the dynamic panel. */
	PlaneSlicingPanel getPSPanel() { return psPanel; }
	
	/** Returns the static panel. */
	PlaneSlicingStaticPanel getPSSPanel() { return pssPanel; }
	
	/** Returns the manager. */
	PlaneSlicingDialogManager getManager(){ return manager; }

	/** Initializes the component. */
	private void initialize(int index, boolean constant)
	{
		String txtDynamic = "<html>Highlights a range,<br>" +
							"reduces others to a constant level (cf. (1))." +
							"</html>";
		String txtStatic = "<html>Highlights a range,<br>" +
							"preserves others (cf. (2)).</html>";
		radioStatic = new JRadioButton(txtStatic);
		radioDynamic = new JRadioButton(txtDynamic);
		ButtonGroup group = new ButtonGroup();
		group.add(radioDynamic);
		group.add(radioStatic);	
		if (constant) radioDynamic.setSelected(true);
		else radioStatic.setSelected(true);
		range = new JComboBox(RANGE);
		range.setSelectedIndex(index);
	}
	
	/** Build and lay out the GUI. */
	private void buildGUI(Registry registry)
	{
		IconManager im = IconManager.getInstance(registry);
		TitlePanel tp = new TitlePanel("Plane Slicing", TEXT, QuantumPane.NOTE,
									im.getIcon(IconManager.SLICING_BIG));
									
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().add(tp, BorderLayout.NORTH);
		getContentPane().add(buildBody(), BorderLayout.CENTER);
		setSize(2*PlaneSlicingPanel.WIDTH, 3*PlaneSlicingPanel.HEIGHT);
	}
	
	private JPanel buildBody()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildGraphicsPane());
		p.add(Box.createRigidArea(RenderingAgt.VBOX));
		p.add(buildControlsPanel());
		return p;
	}
	
	private JPanel buildGraphicsPane()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.setPreferredSize(DIM_ALL);
		p.setSize(DIM_ALL);
		psPanel.setPreferredSize(DIM);
		psPanel.setSize(DIM);
		p.add(psPanel);
		pssPanel.setPreferredSize(DIM);
		pssPanel.setSize(DIM);
		p.add(pssPanel);
		return p;
	}
	
	/** Build a panel containing the radiogroup. */    
	private JPanel buildControlsPanel()
	{
		JPanel p = new JPanel(), pAll = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		p.setLayout(gridbag);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(radioDynamic, c);
		p.add(radioDynamic);
		c.gridy = 1;
		gridbag.setConstraints(radioStatic, c);
		p.add(radioStatic);
		JPanel comboPanel = buildComboBoxPanel();
		c.gridy = 2;
		gridbag.setConstraints(comboPanel, c);
		p.add(comboPanel);
		pAll.setLayout(new FlowLayout(FlowLayout.LEFT));
		pAll.add(p);
		return pAll;
	}
	
	/** Build a Panel with label and comboBox. */
	private JPanel buildComboBoxPanel()
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel label = new JLabel(" Select a plane: ");
		p.add(label);
		p.add(buildComboBoxPanel(range));  
		return p;	
	}

	/** Build a panel with the comboBox. */
	private JPanel buildComboBoxPanel(JComboBox box)
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(box);
		return p;
	}
	
}
