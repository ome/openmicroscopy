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
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStatsEntry;
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
class DomainPaneManager
    implements ActionListener, ChangeListener
{

    /** Action command ID used to control components' changes. */ 
    static final int                FAMILY = 0;  
    static final int                BR = 1;
    static final int                GAMMA = 2;
    private static final int        WAVELENGTH =3;
    private static final int        HISTOGRAM = 4;
    private static final int        NOISE = 5;
    
    static final HashMap            resolutions;
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
    
    private DomainPane              view;
    
    private HistogramDialog         histogramDialog;
    
    /** Reference to the main {@link QuantumPaneManager manager}. */
    private QuantumPaneManager      control;
    
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
        attachComboBoxListeners(view.getWavelengths(), WAVELENGTH);
        attachComboBoxListeners(view.getTransformations(), FAMILY);
        //button
        attachButtonListeners(view.getHistogram(), HISTOGRAM);
        //CheckBox
        attachButtonListeners(view.getNoise(), NOISE);
    }
    
    /** Attach an {@link ActionListener} to an {@link AbstractButton}. */
    private void attachButtonListeners(AbstractButton button, int id)
    {
        button.addActionListener(this);
        button.setActionCommand(""+id);
    }
    
    /** Attach an {@link ActionListener} to a {@link JComboBox}. */
    private void attachComboBoxListeners(JComboBox box, int id)
    {
        box.addActionListener(this);
        box.setActionCommand(""+id);
    }
    
    /** Close the {@link HistogramDialog} is on. */
    void disposeDialogs()
    {
        if (histogramDialog != null) histogramDialog.dispose();
        histogramDialog = null;
    }
    
    /**
     * Resize the input window, update the Histogram view if exists.
     * The method is called by the control {@link QuantumPaneManager}.
     * 
     * @param value     real input value.
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
     * @param value     real input value.
     */
    void setInputWindowEnd(int value)
    {
        if (histogramDialog != null)
            histogramDialog.getManager().setInputWindowEnd(value);
    }
    
    /** 
     * Reset the curveCoefficient to 1.0. 
     * Method called when a new family is selected.
     */
    void resetDefaultGamma(double k, int family)
    {
        view.getGammaLabel().setText(" Gamma: "+k);
        JSlider slider = view.getGamma();
        if (family == QuantumFactory.LOGARITHMIC || 
                family == QuantumFactory.LINEAR) 
            slider.setEnabled(false);
        else slider.setEnabled(true);
        //Remove temporarily the listener otherwise an event is fired.
        slider.removeChangeListener(this);
        slider.setValue((int) (k*10));
        slider.addChangeListener(this);
    }
    
    /** Reset transformations to linear. */
    void resetDefaultComboBox(JComboBox box, int index)
    {
        //Remove temporarily the listener otherwise an event is fired.
        box.removeActionListener(this);
        box.setSelectedIndex(index);
        box.addActionListener(this);
    }
    
    /** Reset the default. */
    void resetDefaultCheckBox(JCheckBox box, boolean b)
    {
        box.removeActionListener(this);
        box.setSelected(b);
        box.addActionListener(this);
    }
    
    /** Reset the defaults settings. */
    void resetDefaults(boolean noiseReduction)
    {
        resetDefaultGamma(1, QuantumFactory.LINEAR);
        resetDefaultBitResolution();
        resetDefaultComboBox(view.getTransformations(), QuantumFactory.LINEAR);
        resetDefaultComboBox(view.getWavelengths(), 0);
        resetDefaultCheckBox(view.getNoise(), noiseReduction);
        histogramDialog = null;
    }
    
    /** Handles events fired  by the JComboBoxes and the JButtons. */
    public void actionPerformed(ActionEvent e)
    {
        String s = e.getActionCommand();
        int index = Integer.parseInt(s);
        try {
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
                    break;
                case NOISE:
                    JCheckBox box = (JCheckBox) e.getSource();
                    setNoiseReduction(box.isSelected());
            }
        } catch(NumberFormatException nfe) {  
            throw new Error("Invalid Action ID "+index, nfe);
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
     * {@link QuantumPaneManager#setStrategy}.
     * 
     * @param value     slider's value.
     */
    private void setCurveCoefficient(int value)
    {
        double k = (double) value/10;
        view.getGammaLabel().setText(" Gamma: "+k);
        int w = view.getWavelengths().getSelectedIndex(); //channel
        int family = view.getTransformations().getSelectedIndex(); //family
        boolean nr = view.getNoise().isSelected();
        control.setQuantizationMap(w, family, k, GAMMA, nr);
    }

    /** Modify the algorithm to map the pixel intensity. */
    private void setNoiseReduction(boolean noise)
    {
        int w = view.getWavelengths().getSelectedIndex(); //channel
        int family = view.getTransformations().getSelectedIndex(); //family
        double k = (double) view.getGamma().getValue()/10;  // gamma
        control.setQuantizationMap(w, family, k, NOISE, noise);
    }

    /**
     * Modify the bit resolution.
     * Forward event to {@link QuantumPaneManager#setStrategy}.
     * 
     * @param v     slider's value in the range 1-8.
     */
    private void setBitResolution(int v)
    {
        int br = ((Integer) resolutions.get(new Integer(v))).intValue(); 
        control.setQuantumStrategy(br);
    }

    /** 
     * Select a family.
     * Forward event to {@link QuantumPaneManager#setStrategy}.
     * 
     * @param family    family index.
     */  
    private void setFamily(int family)
    {
        resetDefaultGamma(1, family);
        int w = view.getWavelengths().getSelectedIndex();
        boolean nr = view.getNoise().isSelected();
        control.setQuantizationMap(w, family, 1, FAMILY, nr);
    }

    /**
     * Select a wavelength.
     * @param w wavelength index.
     */
    private void setWavelength(int w)
    {
        disposeDialogs();
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
            histogramDialog = new HistogramDialog(control, min, max, start, end,
                                                channelStats);
        }
        UIUtilities.centerAndShow(histogramDialog);
    }

    /** Reset the default for the bit resolution. */
    private void resetDefaultBitResolution()
    {
        JSlider slider = view.getBitResolution();
        //Remove temporarily the listener otherwise an event is fired.
        slider.removeChangeListener(this);
        slider.setValue(DomainPane.DEPTH_END);
        slider.addChangeListener(this);
    }


}
