/*
 * org.openmicroscopy.shoola.agents.spots.ui.SpotsWindow
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

package org.openmicroscopy.shoola.agents.spots.ui;

//Java imports
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import java.util.Iterator;
import javax.swing.JFrame;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet;
import org.openmicroscopy.shoola.agents.spots.ui.java3d.Panel3D;
import org.openmicroscopy.shoola.agents.spots.ui.java3d.Spots3DCanvas;
import org.openmicroscopy.shoola.agents.spots.ui.TrajectoriesPanel;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * The main window for the spots agent 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */



public class SpotsWindow extends JFrame implements ActionListener, ItemListener {

	
	private static final int SLIDER_HEIGHT=15;
	private static final int PANEL_SEPARATION=2;
	
	public static final float POINT_RED=0.0f;
	public static final float POINT_GREEN = 0.0f;
	public static final float POINT_BLUE=0.8f;

	public static final float HIGHLIGHT_RED=1.0f;
	public static final float HIGHLIGHT_BLUE=0.0f;
	public static final float HIGHLIGHT_GREEN=0.0f;


	private TrajectoriesPanel proj;
	
	
	private Spots3DCanvas canvas;
	
	private SpotsTrajectorySet tSet;
	
	public SpotsWindow(Registry registry,SpotsTrajectorySet tSet) {
		super("Spots");
		this.tSet = tSet;
		buildFrame(registry);	
		if (proj != null)
			proj.initialize(tSet);
		repaint();
	}
				
	
	private void buildFrame(Registry registry) {
		
		// set things up to mix heavy (visad/java3d) and
		// lightweight components
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
		
		
		Container content = getContentPane();
		content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
		
		
		
		JToolBar t= buildToolBar();
		content.add(t);
		
		
		
		// creat the visad panel for the right	
		Panel3D panel3d = new Panel3D(tSet);
	
		panel3d.setPreferredSize(new Dimension(500,500));
		canvas = panel3d.getCanvas();
		
		// and the other panel for the right
		proj = new TrajectoriesPanel(registry,canvas,tSet); 
		JPanel main = new JPanel();
		main.setLayout(new BoxLayout(main,BoxLayout.X_AXIS));	
		
		main.setBackground(Color.BLACK);
		main.add(proj);
		main.add(Box.createRigidArea(new Dimension(PANEL_SEPARATION,0)));
		main.add(panel3d);
	
		// and add it to main
		content.add(main);
		
		
		
		setBounds(10, 100, 500, 1200);
		pack();
		show();
		
	}
	
	private JToolBar buildToolBar() {
		JToolBar t = new JToolBar();
		
		ButtonGroup rotGroup = new ButtonGroup();

		JLabel rot = new JLabel("Rotation");
		t.add(rot);
		t.add(Box.createRigidArea(new Dimension(5,0)));
		JButton xbutton = new JButton("x");
		t.add(xbutton);
		xbutton.addActionListener(this);
		xbutton.setActionCommand("x");
		rotGroup.add(xbutton);
		
		JButton ybutton = new JButton("y");
		t.add(ybutton);
		ybutton.addActionListener(this);
		ybutton.setActionCommand("y");
		rotGroup.add(ybutton);
		
		JButton allbutton = new JButton("x and y");
		t.add(allbutton);
		allbutton.addActionListener(this);
		allbutton.setActionCommand("x and y");
		rotGroup.add(allbutton);
		
		t.add(Box.createRigidArea(new Dimension(10,0)));
		t.add(new JLabel("Views"));
		JButton xy = new JButton("X-Y");
		
		t.add(Box.createRigidArea(new Dimension(5,0)));
		t.add(xy);
		xy.addActionListener(this);
		xy.setActionCommand("X-Y");
		
		JButton yz = new JButton("Y-Z");
		t.add(yz);
		yz.setActionCommand("Y-Z");
		yz.addActionListener(this);
		
		JButton zx = new JButton("Z-X");
		
		t.add(zx);
		zx.addActionListener(this);
		zx.setActionCommand("Z-X");
		t.setFloatable(false);
		
		JCheckBox fill = new JCheckBox("Fill Boxes",true);
		fill.addItemListener(this);
		fill.setActionCommand("fill");
		t.add(fill);
		
		JCheckBox size  = new JCheckBox("Scaled Boxes",false);
		size.setActionCommand("scaled");
		size.addItemListener(this);
		t.add(size);
		
		return t;

	}
	
	 
	public void actionPerformed(ActionEvent e)  {
		
		if (e.getActionCommand().equals("x"))
			canvas.setRotateXOnly();
		else if (e.getActionCommand().equals("y"))
			canvas.setRotateYOnly();
		else if (e.getActionCommand().equals("x and y"))
			canvas.setRotateBoth();
		else if (e.getActionCommand().equals("X-Y"))
			canvas.setXYTransform();
		else if (e.getActionCommand().equals("Y-Z"))
			canvas.setYZTransform();
		else if (e.getActionCommand().equals("Z-X"))
			canvas.setZXTransform(); 
	}
	
	
	
	

	public void itemStateChanged(ItemEvent e) {
		Object src = e.getSource();
		AbstractButton b = (AbstractButton) src;
		int state = e.getStateChange();
		if (b.getActionCommand().equals("fill")){
		if (state == ItemEvent.SELECTED)
			canvas.drawCubes();
			else
			canvas.drawOutline();
		} else if (b.getActionCommand().equals("scaled")) {
			if (state == ItemEvent.SELECTED)
				setScaled(true);
			else
				setScaled(false);
		} else if (b.getActionCommand().equals("scaled-aspect")) {
			canvas.setScaledAspects(state==ItemEvent.SELECTED);
		} 
	}
	
	private void setScaled(boolean v) {
		Iterator iter = tSet.iterator();
		SpotsTrajectory t;
		while (iter.hasNext()) {
			t = (SpotsTrajectory) iter.next();
			t.setScaled(v);
		}
	}
}

