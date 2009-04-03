/*
 * org.openmicroscopy.shoola.agents.spots.ui.TrajectoryPanel
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet;
import org.openmicroscopy.shoola.agents.spots.range.AxisBoundedRangeModel;
import org.openmicroscopy.shoola.agents.spots.range.RangeSelector;
import org.openmicroscopy.shoola.agents.spots.range.RangeSlider;
import org.openmicroscopy.shoola.agents.spots.ui.java2d.TrajectoryCanvas;
import org.openmicroscopy.shoola.agents.spots.ui.java2d.TrajectoryCanvasFactory;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.config.Registry;


/** 
 * A panel holding a single 2d projection of trajectories, along with axes,
 * labels, and rangeslider.
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

public class TrajectoryPanel extends JPanel implements ActionListener,  ChangeListener {
	
	private static int SLIDER_HEIGHT=15;
	private static int XBOUNDARY=2;
	private RangeSelector rangeSelector;
	private RangeSlider slider;
	
	private AxisBoundedRangeModel model;
	
	private JButton resetButton;
	
	private TrajectoryCanvas canvas;
	
	private int horizontal;
	private int vertical;
	
	private TrajectoriesPanel panel;
		
	public TrajectoryPanel(Registry registry,TrajectoriesPanel panel,
				int horizontal, int vertical,boolean horizLabelOnTop,
				SpotsTrajectorySet tSet) {
		super();
		this.horizontal = horizontal;
		this.vertical = vertical;
		this.panel = panel;
		TPLabelVert vlab;
		TPLabelHoriz hlab;
		Dimension d;
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		setBackground(Color.BLACK);

		if (horizLabelOnTop) {
			add(Box.createRigidArea(new Dimension(0,XBOUNDARY)));
			hlab = new TPLabelHoriz(SpotsTrajectory.getLabel(horizontal));
			d = new Dimension(1000,SLIDER_HEIGHT);
			hlab.setPreferredSize(d);
			hlab.setMaximumSize(d);
			add(hlab);
		}
		JPanel topPanel = new JPanel();
		topPanel.setBackground(Color.LIGHT_GRAY);
		topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.X_AXIS));
		vlab = new TPLabelVert(SpotsTrajectory.getLabel(vertical));
		d = new Dimension(SLIDER_HEIGHT,1000);
		vlab.setPreferredSize(d);
		vlab.setMaximumSize(d);
		topPanel.add(vlab);
		
		double horizExtent = tSet.getExtent(horizontal);
		double vertExtent  = tSet.getExtent(vertical);
		canvas = TrajectoryCanvasFactory.getCanvas(horizontal,vertical,
					horizExtent,vertExtent);
		canvas.setPanel(panel);
		topPanel.add(canvas);
		
		add(topPanel);
		
		// add xlabel below toppanel
		if (!horizLabelOnTop) {
		
			hlab = new TPLabelHoriz(SpotsTrajectory.getLabel(horizontal));
			d = new Dimension(1000,SLIDER_HEIGHT);
			hlab.setPreferredSize(d);
			hlab.setMaximumSize(d);
			add(hlab);
		}
		//add(Box.createRigidArea(new Dimension(0,XBOUNDARY)));
		
		JPanel sliderpanel = new JPanel();
		sliderpanel.setLayout(new BoxLayout(sliderpanel,BoxLayout.X_AXIS));
		resetButton = getButton(registry);
		
		sliderpanel.add(resetButton);
		
		resetButton.addActionListener(this);
		// must go to next greater integer to make sure range model is all-
		// inclusive
		int sliderMax = (int) Math.ceil(horizExtent);
		model = new AxisBoundedRangeModel(horizontal,0,sliderMax,0,sliderMax);
		slider = new RangeSlider(model);
	
		slider.setEnabled(true);
		sliderpanel.add(slider);
	
		
		model.addChangeListener(this);
		add(sliderpanel);
		
	}
	
	private JButton getButton(Registry registry) {
		IconFactory icons = (IconFactory) 
		registry.lookup("/resources/icons/MyFactory");
		Icon resetIcon = icons.getIcon("reset.png");
		return new JButton(resetIcon); 
	}
	

	
	public void initialize(SpotsTrajectorySet tSet) {
		addTrajectories(tSet);
		rangeSelector= RangeSelector.getRangeSelector(horizontal,tSet);
	}
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == resetButton) {
			resetSlider();
		}
	}
	
	private void resetSlider() { 
		int low = model.getMinimum();
		int high = model.getMaximum();
		model.setRangeProperties(low,high-low,low,high,false);
		setSliderVals(low,high);
	}
	
	private void setSliderVals(int low, int high) {
		rangeSelector.setRange(low,high);
	}

	public void stateChanged(ChangeEvent e) {
		
		int low = model.getValue();
		int high = low+model.getExtent();
		setSliderVals(low,high);
		panel.setVals(canvas.getVisibleCount());
	}
	
	public void addTrajectories(SpotsTrajectorySet tSet) {
		canvas.addTrajectories(tSet);
	}
	
	public int getTrajectoryCount() {
		return canvas.getTrajectoryCount();
	}
	
	public AxisBoundedRangeModel getModel() {
		return model;
	}
	
	public TrajectoryCanvas getCanvas() {
		return canvas;
	}
	
	private class TPLabelVert extends JPanel {
		
		public TPLabelVert(String text) {
			super();
			setBackground(Color.WHITE);
			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
			JLabel lab = new JLabel(text);
			add(Box.createVerticalGlue());
			add(lab);
			lab.setAlignmentY(Component.CENTER_ALIGNMENT);
			add(Box.createVerticalGlue());
		}
	}
	
	private class TPLabelHoriz extends JPanel {
		
		public TPLabelHoriz(String text) {
			super();
			setBackground(Color.WHITE);
			setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
			JLabel lab = new JLabel(text);
			add(Box.createHorizontalGlue());
			add(lab);
			lab.setAlignmentX(Component.CENTER_ALIGNMENT);
			add(Box.createHorizontalGlue());
		}
	}
	

}