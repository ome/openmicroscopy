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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.IconManager;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumDef;
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
	private static final int 		ROW_HEIGHT = 25;
	
	/** row's width. */
	private static final int		COLUMN_WIDTH = 90;
	
	/** background color of the JTable. */ 
	private static final Color		CELL_COLOR = Color.LIGHT_GRAY;
	
	/** Dimension of the JPanel which contains the slider. */
	private static final int		PANEL_HEIGHT = 25;
	private static final int		PANEL_WIDTH = 100;
	
	private static final Dimension	DIM = new Dimension(PANEL_WIDTH, 
														PANEL_HEIGHT);

	/** Dimension of the JButton. */
	private static final int		BUTTON_HEIGHT = 20;
	private static final int		BUTTON_WIDTH = 100;		
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

	private JButton					histogram;
	private JLabel					gammaLabel;
	private JComboBox				transformations;
	private JComboBox				wavelengths;
	private JSlider					bitResolution;
	private JSlider					gamma;
	
	private DomainPaneManager		manager;
	private QuantumDef				qDef;
	
	DomainPane(Registry registry, QuantumMappingManager control, 
				String[] waves, QuantumDef qDef)
	{
		this.qDef = qDef;
		manager = new DomainPaneManager(this, control);
		initComboBoxes(waves);
		initSliders();
		initLabel();
		initButton(registry);
		buildGUI();
	}
	
	/** Getters. */
	public DomainPaneManager getManager()
	{
		return manager;
	}
	
	public JSlider getGamma()
	{
		return gamma;
	}

	public JSlider getBitResolution()
	{
		return bitResolution;
	}
	
	public JLabel getGammaLabel()
	{
		return gammaLabel;
	}


	public JComboBox getTransformations()
	{
		return transformations;
	}

	public JComboBox getWavelengths()
	{
		return wavelengths;
	}

	public void setGammaText(double v)
	{
		String txt = " Gamma: "+v;
		gammaLabel.setText(txt);
	}

	public JButton getHistogram()
	{
		return histogram;
	}
	
	/** Initializes the comboBoxes: wavelengths and transformations. */  
	private void initComboBoxes(String[] waves)
	{
		transformations = new JComboBox(algorithms);
		transformations.setSelectedIndex(qDef.family); 
		wavelengths = new JComboBox(waves);
		wavelengths.setSelectedIndex(0); 
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
		bitResolution = new JSlider(JSlider.HORIZONTAL, DEPTH_START, DEPTH_END,
							 qDef.bitResolution);
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
		add(buildTable());
	}
	
	/** Build the JTable. */
	private JTable buildTable()
	{
		JTable table = new TableComponent(5, 2);
		table.setTableHeader(null);
		table.setRowHeight(ROW_HEIGHT);
		table.setBackground(CELL_COLOR);
		
		//Set the columns' width.
		TableColumnModel columns = table.getColumnModel();
		TableColumn column = columns.getColumn(0);
		column.setPreferredWidth(COLUMN_WIDTH);
		column.setWidth(COLUMN_WIDTH);
		
		//Reset the width of the second column
		column = columns.getColumn(1);
		//column.setPreferredWidth(PANEL_WIDTH);
		column.setWidth(PANEL_WIDTH);

		//First row.
		JLabel label = new JLabel(" Transformation");
		table.setValueAt(label, 0, 0);
		table.setValueAt(transformations, 0, 1);

		//Second row.
		table.setValueAt(gammaLabel, 1, 0);
		table.setValueAt(buildSliderPanel(gamma), 1, 1);

		//Third row.
		label = new JLabel(" Resolution");
		table.setValueAt(label, 2, 0);
		table.setValueAt(buildSliderPanel(bitResolution), 2, 1);

		//Fourth row.
		label = new JLabel(" Wavelength");
		table.setValueAt(label, 3, 0);
		table.setValueAt(wavelengths, 3, 1);
		
		//Fifth row.
		label = new JLabel(" Histogram");
		table.setValueAt(label, 4, 0);
		table.setValueAt(buildButtonPanel(histogram), 4, 1);

		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
		return table;
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
		//sliderPanel.setPreferredSize(DIM);
		sliderPanel.setSize(DIM);
		slider.setPreferredSize(DIM);
		slider.setBounds(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
		slider.setBackground(Color.LIGHT_GRAY);
		sliderPanel.add(slider);
		
		return sliderPanel;
	}
	
	/**
	 * Build a JPanel which contains a JButton.
	 * @param button
	 * @return See above.
	 */
	private JPanel buildButtonPanel(JButton button)
	{
		JPanel p = new JPanel();
		p.setBackground(Color.LIGHT_GRAY);
		p.setBorder(null);
		button.setPreferredSize(DIM_BUTTON);
		button.setBounds(0, 0, 40, 20);
		button.setContentAreaFilled(false);
		p.setPreferredSize(DIM_BUTTON);
		p.setSize(DIM_BUTTON);
		p.add(button);
		
		return p;
	}

}
