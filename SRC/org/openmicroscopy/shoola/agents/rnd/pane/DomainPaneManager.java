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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.QuantumDef;

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
	/** ID to control events*/    
	private static final int        WAVELENGTH = 0;
	private static final int        TRANSFORMATION = 1;
	private static final int        HISTOGRAM = 2;
	
	private QuantumMappingManager	control;
	private DomainPane				view;
	private HistogramDialog         histogramDialog;
	
	DomainPaneManager(DomainPane view, QuantumMappingManager control)
	{
		this.control = control;
		this.view = view;
	}

	/** Initializes the listeners. */
	void initListeners()
	{
		//sliders
		view.getGamma().addChangeListener(this);
		view.getBitResolution().addChangeListener(this);
		
		//combobox
		JComboBox   transformations = view.getTransformations(),
					wavelengths = view.getWavelengths();
		wavelengths.addActionListener(this);
		wavelengths.setActionCommand(""+WAVELENGTH);
		transformations.addActionListener(this);
		transformations.setActionCommand(""+TRANSFORMATION);
		
		//button
		JButton     button = view.getHistogram();
		button.addActionListener(this);
		button.setActionCommand(""+HISTOGRAM);
	}

	/** Handles events fired  by the JComboBoxes and the JButtons. */
	public void actionPerformed(ActionEvent e)
	{
		String s = (String) e.getActionCommand();
		try {
			int     index = Integer.parseInt(s);
			switch (index) { 
				case TRANSFORMATION:
					JComboBox cbx = (JComboBox) e.getSource();
					setFamily(cbx.getSelectedIndex());
					break;
				case WAVELENGTH:
					JComboBox cbx1 = (JComboBox) e.getSource();
					setWavelength(cbx1.getSelectedIndex());
					break;
				case HISTOGRAM:
					popUpHistogram();
			}// end switch  
		} catch(NumberFormatException nfe) {  //impossible if IDs are set correctly 
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
	
	private void setCurveCoefficient(int value)
	{
		view.getGammaLabel().setText(""+((double) value/10));
		control.setStrategy();
	}
	
	private void setBitResolution(int value)
	{
		control.setStrategy();
	}
	
	/** 
	 * Forward event to {@link QuantumMappingManager}.
	 * @param family    family index.
	 */  
	private void setFamily(int family)
	{
		//update or create a new QuantumDef ?????
		if (family == QuantumMapping.LOGARITHMIC || 
			family == QuantumMapping.LINEAR) {
			view.getGamma().setEnabled(false);
		} else {
			view.getGamma().setEnabled(true);
		}
		control.setStrategy();
	}
	
	/** 
	 * Forward event to {@link QuantumMappingManager}.
	 *  
	 * @param index wavelength index.
	 */	
	private void setWavelength(int index)
	{
		control.setWavelength(index);
	}

	private void popUpHistogram()
	{
	}
	/** 
	 * Resets the default values for gamma.
	 */
	void resetDefaultGamma()
	{
		//TODO MODIFY
		view.getGammaLabel().setText(""+1);
		JSlider ccSlider = view.getGamma();
		// doesn't fire an event
		ccSlider.removeChangeListener(this);
		ccSlider.setValue(10);
		ccSlider.addChangeListener(this);
	}
}
