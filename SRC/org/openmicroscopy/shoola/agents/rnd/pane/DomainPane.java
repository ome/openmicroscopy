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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.agents.rnd.metadata.ChannelData;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;
import org.openmicroscopy.shoola.util.ui.TableComponent;
import org.openmicroscopy.shoola.util.ui.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.TableComponentCellRenderer;

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
	
	/** row's height. */ 
	private static final int 		ROW_HEIGHT = 30;
	
	/** row's width. */
	private static final int		COLUMN_WIDTH = 110, COLUMN_TWO = 110;
	
	/** Dimension of the JPanel which contains the slider. */
	private static final int		PANEL_HEIGHT = 25;
	private static final int		PANEL_WIDTH = 100;
	
	private static final Dimension	DIM = new Dimension(PANEL_WIDTH, 
														PANEL_HEIGHT);

	/** Dimension of the JButton. */
	private static final int		BUTTON_HEIGHT = 20, BUTTON_WIDTH = 40;	
		
	private static final Dimension	DIM_BUTTON = new Dimension(BUTTON_WIDTH, 
															BUTTON_HEIGHT);
											
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
	}
	
	/**Build and layout the GUI */
	private void buildGUI()
	{
		setLayout(new GridLayout(1, 1));
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.add(buildTable());
		add(p);
	}
	
	/** Build the JTable. */
	private TableComponent buildTable()
	{
		TableComponent table = new TableComponent(5, 2);
		setTableLayout(table);

		//First row.
		JLabel label = new JLabel(" Wavelength");
		table.setValueAt(label, 0, 0);
		table.setValueAt(wavelengths, 0, 1);
		
		//Second row.
		label = new JLabel(" Map");
		table.setValueAt(label, 1, 0);
		table.setValueAt(transformations, 1, 1);

		//Third row.
		table.setValueAt(gammaLabel, 2, 0);
		table.setValueAt(buildSliderPanel(gamma), 2, 1);

		//Fourth row.
		label = new JLabel(" Resolution");
		table.setValueAt(label, 3, 0);
		table.setValueAt(buildSliderPanel(bitResolution), 3, 1);

		//Fifth row.
		label = new JLabel(" Histogram");
		table.setValueAt(label, 4, 0);
		table.setValueAt(buildButtonPanel(histogram), 4, 1);
				
		return table;
	}
	
	/** Set the table layout. */
	private void setTableLayout(TableComponent table) 
	{
		table.setTableHeader(null);
		table.setOpaque(false);
		table.setShowGrid(false);
		table.setRowHeight(ROW_HEIGHT);
		
		//Set the columns' width.
		TableColumnModel columns = table.getColumnModel();
		TableColumn column = columns.getColumn(0);
		column.setPreferredWidth(COLUMN_WIDTH);
		column.setWidth(COLUMN_WIDTH);
		
		//Set the width of the second column
		column = columns.getColumn(1);
		column.setPreferredWidth(COLUMN_TWO);
		column.setWidth(COLUMN_TWO);
		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());	
	}
	
	/**
	 * Build a JPanel which contains a JSlider.
	 * 
	 * @param slider	slider to add.
	 * @return See above.
	 */
	private JPanel buildSliderPanel(JSlider slider)	
	{
		//JPanel which contains a slider.
		JPanel sliderPanel = new JPanel();
		sliderPanel.setLayout(null);
		sliderPanel.setOpaque(false);
		sliderPanel.setPreferredSize(DIM);
		sliderPanel.setSize(DIM);
		slider.setPreferredSize(DIM);
		slider.setBounds(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
		slider.setOpaque(false);
		sliderPanel.add(slider);
		return sliderPanel;
	}
	
	/**
	 * Build a JPanel which contains a JButton.
	 * 
	 * @param button
	 * @return See above.
	 */
	private JPanel buildButtonPanel(JButton button)
	{
		JPanel p = new JPanel();
		p.setBorder(null);
		button.setPreferredSize(DIM_BUTTON);
		button.setBounds(0, 0, BUTTON_WIDTH, BUTTON_HEIGHT);
		button.setContentAreaFilled(false);
		p.setPreferredSize(DIM_BUTTON);
		p.setSize(DIM_BUTTON);
		p.add(button);
		return p;
	}

}
