/*
 * org.openmicroscopy.shoola.agents.rnd.DomainPaneManager
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStatsEntry;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;

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
class DomainPaneManager
	implements ActionListener, ChangeListener
{
	/** Action command ID. */ 
	static final int        		FAMILY = 0;  
	static final int        		BR = 1;
	static final int        		GAMMA = 2;
	private static final int        WAVELENGTH =3;
	private static final int        HISTOGRAM = 4;
	
	static final HashMap			resolutions;
	static {
		resolutions = new HashMap();
		resolutions.put(new Integer(1), new Integer(QuantumFactory.DEPTH_1BIT));
		resolutions.put(new Integer(2), new Integer(QuantumFactory.DEPTH_2BIT));
		resolutions.put(new Integer(3), new Integer(QuantumFactory.DEPTH_3BIT));
		resolutions.put(new Integer(4), new Integer(QuantumFactory.DEPTH_4BIT));
		resolutions.put(new Integer(5), new Integer(QuantumFactory.DEPTH_5BIT));
		resolutions.put(new Integer(6), new Integer(QuantumFactory.DEPTH_6BIT));
		resolutions.put(new Integer(7), new Integer(QuantumFactory.DEPTH_7BIT));
		resolutions.put(new Integer(8), new Integer(QuantumFactory.DEPTH_8BIT));
	}
	
	private DomainPane				view;
	
	private HistogramDialog         histogramDialog;
	
	/** Reference to the main {@link QuantumPaneManager manager}. */
	private QuantumPaneManager		control;
		
	DomainPaneManager(DomainPane view, QuantumPaneManager control)
	{
		this.control = control;
		this.view = view;
	}

	/** Initializes the listeners. */
	void attachListeners()
	{
		//sliders
		view.getGamma().addChangeListener(this);
		view.getBitResolution().addChangeListener(this);
		//comboboxes
		JComboBox transformations = view.getTransformations(),
				  wavelengths = view.getWavelengths();
		wavelengths.addActionListener(this);
		wavelengths.setActionCommand(""+WAVELENGTH);
		transformations.addActionListener(this);
		transformations.setActionCommand(""+FAMILY);
		
		//button
		JButton button = view.getHistogram();
		button.addActionListener(this);
		button.setActionCommand(""+HISTOGRAM);
	}

	/**
	 * Resize the input window, update the Histogram view if exists.
	 * The method is called by the control {@link QuantumPaneManager}.
	 * 
	 * @param value		real input value.
	 */
	void setInputWindowStart(int value)
	{
		if (histogramDialog != null)
			histogramDialog.getManager().setInputWindowStart(value);
	}
	
	/**
	 * Resize the input window.
	 * The method is called by the control {@link QuantumPaneManager}.
	 * 
	 * @param value		real input value.
	 */
	void setInputWindowEnd(int value)
	{
		if (histogramDialog != null)
			histogramDialog.getManager().setInputWindowEnd(value);
	}
	
	/** Handles events fired  by the JComboBoxes and the JButtons. */
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		try {
			int index = Integer.parseInt(s);
			switch (index) { 
				case FAMILY:
					JComboBox cbx = (JComboBox) e.getSource();
					setFamily(cbx.getSelectedIndex());
					break;
				case WAVELENGTH:
					JComboBox cbx1 = (JComboBox) e.getSource();
					setWavelength(cbx1.getSelectedIndex());
					break;
				case HISTOGRAM:
					popUpHistogram();
			}
		} catch(NumberFormatException nfe) {  
				throw nfe;  //just to be on the safe side...
		} 
	}
	
	/** Handle events fired by sliders. */
	public void stateChanged(ChangeEvent e)
	{
		JSlider source = (JSlider) e.getSource();
		if (source == view.getGamma())
			setCurveCoefficient(source.getValue());
		else setBitResolution(source.getValue());
	}
	
	/** 
	 * Set the curve coefficient and forward event to 
	 * @see QuantumPaneManager#setStrategy.
	 * 
	 * @param value		slider's value.
	 */
	private void setCurveCoefficient(int value)
	{
		double k = value/10;
		view.getGammaLabel().setText(" Gamma: "+k);
		view.repaint();
		int family = view.getTransformations().getSelectedIndex(); //family
		int b = view.getBitResolution().getValue();	// bitResolution
		int br = ((Integer) resolutions.get(new Integer(b))).intValue();
		control.setQuantumStrategy((int) k, family, br, GAMMA);
	}
	
	/**
	 * Modify the bit resolution.
	 * Forward event to @see QuantumPaneManager#setStrategy.
	 * 
	 * @param v		slider's value in the range 1-8.
	 */
	private void setBitResolution(int v)
	{
		int k = view.getGamma().getValue();	//gamma
		int family = view.getTransformations().getSelectedIndex(); //family
		//bitResolution
		int br = ((Integer) resolutions.get(new Integer(v))).intValue(); 
		
		control.setQuantumStrategy(k/10, family, br, BR);
	}
	
	/** 
	 * Select a family.
	 * Forward event to @see QuantumPaneManager#setStrategy.
	 * 
	 * @param family    family index.
	 */  
	private void setFamily(int family)
	{
		if (family == QuantumFactory.LOGARITHMIC || 
			family == QuantumFactory.LINEAR) 
			view.getGamma().setEnabled(false);
		else
			view.getGamma().setEnabled(true);
		resetDefaultGamma();
		view.repaint();
		int b = view.getBitResolution().getValue();	// bitResolution
		int br = ((Integer) resolutions.get(new Integer(b))).intValue();
		int k = view.getGamma().getValue();	//gamma
		control.setQuantumStrategy(k/10, family, br, FAMILY);
	}
	
	
	/**
	 * Select a wavelength.
	 * @param w	wavelength indexd.
	 */
	private void setWavelength(int w)
	{
		histogramDialog = null;
		control.setWavelength(w);
	}

	/** Initialize and display the histogramDialog window. */
	private void popUpHistogram()
	{
		if (histogramDialog == null) {
			int w, start, end, min, max;
			w = view.getWavelengths().getSelectedIndex();
			start = control.getChannelWindowStart(w);
			end  = control.getChannelWindowEnd(w);
			min = control.getGlobalChannelWindowStart(w);
			max = control.getGlobalChannelWindowEnd(w);
			PixelsStatsEntry[] channelStats = control.getChannelStats(w);
			if (channelStats == null) // this shouln't happen
				throw new IllegalArgumentException("Cannot retrieve the stats");
			histogramDialog = new HistogramDialog(control, min, max, start, end,
												channelStats);
		}
		control.showDialog(histogramDialog);
	}
	
	/** 
	 * Re-set the curveCoefficient to 1.0. 
	 * Method called when a new family is selected.
	 */
	private void resetDefaultGamma()
	{
		view.getGammaLabel().setText(" Gamma: "+(double) 
											GraphicsRepresentation.INIT/10);
		JSlider ccSlider = view.getGamma();
		//Remove temporarily the listener otherwise an event is fired.
		ccSlider.removeChangeListener(this);
		ccSlider.setValue(GraphicsRepresentation.INIT);
		ccSlider.addChangeListener(this);
	}
	
}
