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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.agents.rnd.RenderingAgt;
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
			UIUtilities.formatToolTipText("Bring the histogram dialog."));
		histogram.setBorder(null);
	}

	/**Build and layout the GUI */
	private void buildGUI()
	{
		setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		GridBagLayout gridbag = new GridBagLayout();
		setLayout(gridbag);
		GridBagConstraints c = new GridBagConstraints();
	
		JLabel label = new JLabel(" Wavelength");
		c.ipadx = RenderingAgt.H_SPACE;
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(label, c);
		add(label);
		c.gridy = 1;
		label = new JLabel(" Map");
		gridbag.setConstraints(label, c);
		add(label);
		c.gridy = 2;
		gridbag.setConstraints(gammaLabel, c);
		add(gammaLabel);
		c.gridy = 3;
		
		label = new JLabel(" Resolution");
		gridbag.setConstraints(label, c);
		add(label);
		c.gridy = 4;
		label = new JLabel(" Histogram");
		gridbag.setConstraints(label, c);
		add(label);
		c.gridx = 1;
		c.gridy = 0;
		JPanel wp = buildComboBoxPanel(wavelengths);
		gridbag.setConstraints(wp, c);
		add(wp);
		c.gridy = 1;
		wp = buildComboBoxPanel(transformations);
		gridbag.setConstraints(wp, c);
		add(wp);
		c.gridy = 2;
		c.weightx = 1.0;
		c.ipadx = 5; 
		JPanel gp = buildSliderPanel(gamma);
		gridbag.setConstraints(gp, c);
		add(gp);
		c.gridy = 3;
		JPanel brp = buildSliderPanel(bitResolution);
		gridbag.setConstraints(brp, c);
		add(brp);
		c.gridy = 4;
		c.weightx = 0.0;
		c.ipadx = 0; 
		JPanel hp = buildButtonPanel(histogram);
		gridbag.setConstraints(hp, c);
		add(hp);
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
		//p.setLayout(null);
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		//slider.setPreferredSize(DIM_SLIDER);
		//slider.setSize(DIM_SLIDER);
		//p.setPreferredSize(DIM_SLIDER);
		//p.setSize(DIM_SLIDER);
		p.add(slider);

		return p;
	}

	private JPanel buildComboBoxPanel(JComboBox box)
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		//p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(box);
		return p;
	}
	
	private JPanel buildButtonPanel(JButton b)
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		//p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(b);
		return p;
	}

}
