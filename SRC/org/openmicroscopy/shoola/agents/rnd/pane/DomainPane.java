/*
 * org.openmicroscopy.shoola.agents.rnd.DomainPane
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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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

class DomainPane
	extends JPanel
{
	
	private static final int        DEPTH_START = 1, DEPTH_END = 8;

	private static final int		MAX = GraphicsRepresentation.MAX;
	private static final int		MIN = GraphicsRepresentation.MIN;
	
	private static final String[]   algorithms;
    
	//the Families
	static {
		algorithms = new String[4];
	   	algorithms[QuantumFactory.LINEAR] = "Linear";
	   	algorithms[QuantumFactory.EXPONENTIAL] ="Exponential";
	   	algorithms[QuantumFactory.LOGARITHMIC] = "Logarithmic";
	   	algorithms[QuantumFactory.POLYNOMIAL] ="Polynomial";      
	}
   	private static final HashMap	uiBR;
   	
	static {
		uiBR = new HashMap();
		uiBR.put(new Integer(QuantumFactory.DEPTH_1BIT), new Integer(1));
		uiBR.put(new Integer(QuantumFactory.DEPTH_2BIT), new Integer(2));
		uiBR.put(new Integer(QuantumFactory.DEPTH_3BIT), new Integer(3));
		uiBR.put(new Integer(QuantumFactory.DEPTH_4BIT), new Integer(4));
		uiBR.put(new Integer(QuantumFactory.DEPTH_5BIT), new Integer(5));
		uiBR.put(new Integer(QuantumFactory.DEPTH_6BIT), new Integer(6));
		uiBR.put(new Integer(QuantumFactory.DEPTH_7BIT), new Integer(7));
		uiBR.put(new Integer(QuantumFactory.DEPTH_8BIT), new Integer(8));
	}
	
	private JButton					histogram;
	private JLabel					gammaLabel;
	private JComboBox				transformations;
	private JComboBox				wavelengths;
	private JSlider					bitResolution;
	private JSlider					gamma;
	
	private JLabel					label_1, label_2, label_3, label_4;
	
	private JPanel					panel_1, panel_2, panel_3, panel_4, panel_5;

	private QuantumDef				qDef;
	
	/** Reference to the {@link DomainPaneManager manager}. */
	private DomainPaneManager		manager;
	
	DomainPane(Registry registry, QuantumPaneManager control, 
				ChannelData[] data, QuantumDef qDef, int index)
	{
		this.qDef = qDef;
		manager = new DomainPaneManager(this, control);
		initComboBoxes(data, index);
		initSliders();
		initLabel();
		initButton(registry);
		manager.attachListeners();
		buildGUI();
	}
	
	/** Getters. */
	DomainPaneManager getManager() { return manager; }
	
	JSlider getGamma() { return gamma; }

	JSlider getBitResolution() { return bitResolution; }
	
	JLabel getGammaLabel() { return gammaLabel; }

	JComboBox getTransformations() { return transformations; }

	JComboBox getWavelengths() { return wavelengths; }
	
	JButton getHistogram() { return histogram; }
	
	void setGammaText(double v)
	{
		String txt = " Gamma: "+v;
		gammaLabel.setText(txt);
	}

	/** Initializes the comboBoxes: wavelengths and transformations. */  
	private void initComboBoxes(ChannelData[] data, int index)
	{
		transformations = new JComboBox(algorithms);
		transformations.setSelectedIndex(qDef.family);
		String[] waves = new String[data.length];
		for (int i = 0; i < data.length; i++)
			waves[i] = ""+data[i].getNanometer();
		wavelengths = new JComboBox(waves);
		wavelengths.setSelectedIndex(index);  
		//When the color model is gray, the user cannot select the wavelength.
		wavelengths.setEnabled(false);
	}
	
	/** Initializes the sliders: gamma and bitResolution. */    
	private void initSliders()
	{
		int k = (int) (qDef.curveCoefficient*10);
		gamma = new JSlider(JSlider.HORIZONTAL, MIN, MAX, k);
		if (qDef.family == QuantumFactory.LINEAR || 
			qDef.family == QuantumFactory.LOGARITHMIC) 
			gamma.setEnabled(false);
		else gamma.setEnabled(true);
		Integer br = ((Integer) uiBR.get(new Integer(qDef.bitResolution)));
		int resolution = DEPTH_END;
		if (br != null) resolution = br.intValue();
		bitResolution = new JSlider(JSlider.HORIZONTAL, DEPTH_START, DEPTH_END,
									resolution);
	}
	
	/** Initializes the gamma label. */
	private void initLabel()
	{
		gammaLabel = new JLabel(" Gamma: "+qDef.curveCoefficient);
	}
	
	/** Initializes the histogram Button. */
	private void initButton(Registry registry)
	{
		IconManager IM = IconManager.getInstance(registry);
		histogram = new JButton(IM.getIcon(IconManager.HISTOGRAM));
		histogram.setToolTipText(
			UIUtilities.formatToolTipText("Bring up the histogram dialog."));
		histogram.setBorder(null);
	}

	/**Build and layout the GUI */
	private void buildGUI()
	{
		setLayout(new DomainPaneLM());
		label_1 = new JLabel(" Wavelength");
		label_2 = new JLabel(" Map");
		label_3 = new JLabel(" Resolution");
		label_4 = new JLabel(" Histogram");
		panel_1 = buildComboBoxPanel(wavelengths);
		panel_2 = buildComboBoxPanel(transformations);
		panel_3 = buildSliderPanel(gamma);
		panel_4 = buildSliderPanel(bitResolution);
		panel_5 = buildButtonPanel(histogram);
		add(label_1);
		add(label_2);
		add(gammaLabel);
		add(label_3);
		add(label_4);
		add(panel_1);
		add(panel_2);
		add(panel_3);
		add(panel_4);
		add(panel_5);
	}

	/**
	 * Build a JPanel which contains a JSlider.
	 * 
	 * @param slider	slider to add.
	 * @return See above.
	 */
	private JPanel buildSliderPanel(JSlider slider)	
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(slider);
		return p;
	}

	private JPanel buildComboBoxPanel(JComboBox box)
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(box);
		return p;
	}
	
	private JPanel buildButtonPanel(JButton b)
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(b);
		return p;
	}
	
	/** 
	 * Custom manager that takes care of the layout of {@link DomainPane}.
	 * This layout manager is tightly coupled to the specific structure of 
	 * {@link DomainPane} and its constituent components.
	 */
	private class DomainPaneLM 
		implements LayoutManager
	{
    	int V_SPACE = 5, H_SPACE = 5;
    	
		/** 
		 * Lays out the components of the domain Pane.
		 *
		 * @param domainPane     The domainPane. 
		 */
		public void layoutContainer(Container domainPane)
		{
			DomainPane c = (DomainPane) domainPane;
			Dimension dLabel = getLabelSize(c);
			Dimension dPanel = getPanelSize(c);
			int h = getHeightMax(dLabel, dPanel);
			int vMax = h+V_SPACE;
			int hMax = dLabel.width+H_SPACE;
			c.label_1.setBounds(0, 0, dLabel.width, h);
			c.label_2.setBounds(0, vMax, dLabel.width, h);
			c.gammaLabel.setBounds(0, 2*vMax, dLabel.width, h);
			c.label_3.setBounds(0, 3*vMax, dLabel.width, h);
			c.label_4.setBounds(0, 4*vMax, dLabel.width, h);
			c.panel_1.setBounds(hMax, 0, dPanel.width, h);
			c.panel_2.setBounds(hMax, vMax, dPanel.width, h);
			c.panel_3.setBounds(hMax, 2*vMax, dPanel.width, h);
			c.panel_4.setBounds(hMax, 3*vMax, dPanel.width, h);
			c.panel_5.setBounds(hMax, 4*vMax, dPanel.width, h);
		}
	
		private Dimension getLabelSize(DomainPane c)
		{
			Dimension d = c.label_1.getPreferredSize();
			int w = d.width;
			int h = d.height;
			d = c.label_2.getPreferredSize();
			if (d.width>w) w = d.width;
			if (d.height>h) h = d.height;
			d = c.gammaLabel.getPreferredSize();
			if (d.width>w) w = d.width;
			if (d.height>h) h = d.height;
			d = c.label_3.getPreferredSize();
			if (d.width>w) w = d.width;
			if (d.height>h) h = d.height;
			d = c.label_3.getPreferredSize();
			if (d.width>w) w = d.width;
			if (d.height>h) h = d.height;
			return new Dimension(w, h);
		}
		
		private Dimension getPanelSize(DomainPane c)
		{
			Dimension d = c.panel_1.getPreferredSize();
			int w = d.width;
			int h = d.height;
			d = d = c.panel_2.getPreferredSize();
			if (d.width>w) w = d.width;
			if (d.height>h) h = d.height;
			d = d = c.panel_3.getPreferredSize();
			if (d.width>w) w = d.width;
			if (d.height>h) h = d.height;
			d = d = c.panel_4.getPreferredSize();
			if (d.width>w) w = d.width;
			if (d.height>h) h = d.height;
			d = d = c.panel_5.getPreferredSize();
			if (d.width>w) w = d.width;
			if (d.height>h) h = d.height;
			return new Dimension(w, h);
		}
		
		private int getHeightMax(Dimension d1, Dimension d2)
		{
			int h = d1.height;
			if (d2.height>d1.height) h = d2.height;
			return h; 
		}
		
		/** 
		 * Returns the preferred amount of space for the layout.
		 *
		 * @param domainPane     The domain Pane. 
		 * @return The above mentioned dimensions.
		 */
		public Dimension preferredLayoutSize(Container domainPane)
		{
			DomainPane c = (DomainPane) domainPane;
			int w = 0, h = 0, hTotal = 0;
			
			Dimension dLabel = getLabelSize(c);
			Dimension dPanel = getPanelSize(c);
			w = dLabel.width+H_SPACE+dPanel.width;
			h = getHeightMax(dLabel, dPanel);
			hTotal = 4*V_SPACE+5*h;
			return new Dimension(w, hTotal);
		}
    
		/** 
		 * Returns the minimum amount of space the layout needs.
		 * This is the same as the preferred dimensions of the image canvas.
		 *
		 * @param domainPane     The domain Pane. 
		 * @return The above mentioned dimensions.
		 */ 
		public Dimension minimumLayoutSize(Container domainPane)
		{
			return preferredLayoutSize(domainPane);
		}

		/** 
		 * Required by I/F but not actually needed in our case, 
		 * no op implementation.
		 */
		public void addLayoutComponent(String name, Component comp) {}
	
		/** 
		 * Required by I/F but not actually needed in our case, 
		 * no op implementation.
		 */
		public void removeLayoutComponent(Component comp) {}
	}

}
