/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.GraphicsPane
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

package org.openmicroscopy.shoola.agents.imviewer.rnd;



//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;

/** 
 * Component hosting the diagram and the controls to select the pixels intensity 
 * interval and the codomain interval.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class GraphicsPane
    extends JPanel
    implements ActionListener, FocusListener, PropertyChangeListener
{

    /** Action command ID to indicate that the start value is modified.*/
    private static final int        START_SELECTED = 0;
    
    /** Action command ID to indicate that the start value is modified.*/
    private static final int        END_SELECTED = 1;
    
    /** Slider to select a sub-interval of [0, 255]. */
    private TwoKnobsSlider      codomainSlider;
    
    /** Slider to select the pixels intensity interval. */
    private TwoKnobsSlider      domainSlider;
    
    /** Field to display the starting pixel intensity value. */
    private JTextField          startField;
    
    /** Field to display the ending pixel intensity value. */
    private JTextField          endField;

    /** The label displaying the global max. */
    private JLabel              maxLabel;
    
    /** The label displaying the global min. */
    private JLabel              minLabel;
    
    /** The component displaying the plane histogram. */
    private GraphicsPaneUI      uiDelegate;
    
    /** Reference to the Model.*/
    protected RendererModel     model;
    
    /** Reference to the Control.*/
    protected RendererControl   controller;

    /** Initializes the components. */
    private void initComponents()
    {
        uiDelegate = new GraphicsPaneUI();
        codomainSlider = new TwoKnobsSlider(RendererModel.CD_START, 
                                        RendererModel.CD_END, 
                                        model.getCodomainStart(),
                                        model.getCodomainEnd());
        codomainSlider.setPaintLabels(false);
        codomainSlider.setPaintEndLabels(false);
        codomainSlider.setPaintTicks(false);
        codomainSlider.setOrientation(TwoKnobsSlider.VERTICAL);
        codomainSlider.addPropertyChangeListener(this);
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        domainSlider = new TwoKnobsSlider((int) model.getGlobalMin(), 
                            (int) model.getGlobalMax(), s, e);
        domainSlider.setPaintLabels(false);
        domainSlider.setPaintEndLabels(false);
        domainSlider.setPaintTicks(false);
        domainSlider.addPropertyChangeListener(this);
        double min = model.getGlobalMin();
        double max = model.getGlobalMax();
        startField = new JTextField();
        startField.setColumns((""+min).length());
        endField = new JTextField();
        endField.setColumns((""+max).length());
        startField.setText(""+s);
        endField.setText(""+e);
        startField.addActionListener(this);
        startField.setActionCommand(""+START_SELECTED);
        startField.addFocusListener(this);
        endField.addActionListener(this);
        endField.setActionCommand(""+END_SELECTED);
        endField.addFocusListener(this);
        maxLabel = new JLabel(""+max);
        minLabel = new JLabel(""+min);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(codomainSlider);
        p.add(uiDelegate);
        add(p);
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(domainSlider);
        p.add(buildFieldsControls());
        add(p);
    }
    
    /**
     * Builds and lays out the UI component hosting the text fields.
     * 
     * @return See above.
     */
    private JPanel buildFieldsControls()
    {
        JPanel p = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(new GridBagLayout());
        c.insets = new Insets(5,20,5,30);
        c.anchor = GridBagConstraints.WEST;
        p.add(buildFieldsPanel("Min", minLabel, "Start", startField), c);
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 1;
        p.add(buildFieldsPanel("Max", maxLabel, "End", endField), c);
        return p;
    }
    
    /**
     * Builds panel used to display the min/start pair or max/end pair.
     * 
     * @param txt1  The text associated to the global value.
     * @param l     The label displaying the global value.
     * @param txt2  The text associated to the interval bound.
     * @param f     The text field displaying the interval bound.
     * @return  See above.
     */
    private JPanel buildFieldsPanel(String txt1, JLabel l, String txt2, 
                                JTextField f)
    {
        JPanel p = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,10,5,10);
        c.weightx = 60;
        c.anchor = GridBagConstraints.WEST;
        p.setLayout(new GridBagLayout());
        JLabel label = new JLabel(txt1);
        p.add(label, c);
        c.gridx = 1;
        c.weightx = 40;
        c.anchor = GridBagConstraints.WEST;
        p.add(l, c);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 60;
        
        label = new JLabel(txt2);
        c.anchor = GridBagConstraints.CENTER;
        
        p.add(label, c);
        c.gridx = 1;
        c.weightx = 40;
       
        
        p.add(f, c);
        p.validate();
        return p;
    }
    
    /** 
     * Handles the action event fired by the start text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a value, then we simply 
     * suggest the user to enter a valid one.
     */
    private void startSelectionHandler()
    {
        boolean valid = false;
        int val = 0;
        try {
            val = Integer.parseInt(startField.getText());
            if (model.getGlobalMin() <= val && val < model.getWindowEnd()) 
                valid = true;
        } catch(NumberFormatException nfe) {}
        if (valid) {
            controller.setInputInterval(val, model.getWindowEnd(), true);
        } else {
            startField.selectAll();
            UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid pixels intensity interval", 
                    "["+val+","+model.getWindowEnd()+"]");
        }
    }
    
    /** 
     * Handles the action event fired by the end text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a value, then we simply 
     * suggest the user to enter a valid one.
     */
    private void endSelectionHandler()
    {
        boolean valid = false;
        int val = 0;
        try {
            val = Integer.parseInt(endField.getText());
            if (model.getWindowStart() < val && val <= model.getGlobalMax()) 
                valid = true;
        } catch(NumberFormatException nfe) {}
        if (valid) {
            controller.setInputInterval(model.getWindowStart(), val, true);
        } else {
            endField.selectAll();
            UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid pixels intensity interval", 
                    "["+model.getWindowStart()+","+val+"]");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model         Reference to the model. 
     *                      Mustn't be <code>null</code>.
     * @param controller    Reference to the control.
     *                      Mustn't be <code>null</code>.
     */
    GraphicsPane(RendererModel model, RendererControl controller)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (controller == null) 
            throw new NullPointerException("No controller.");
        this.model = model;
        this.controller = controller;
        initComponents();
        buildGUI();
    }

    /** Updates the controls when a new channel is selected. */
    void setSelectedChannel()
    {
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        int min = (int) model.getGlobalMin();
        int max = (int) model.getGlobalMax();
        endField.setText(""+e);
        startField.setText(""+s);
        minLabel.setText(""+min);
        maxLabel.setText(""+max);
        domainSlider.setValues(max, min, s, e);
    }
    
    /** Sets the pixels intensity interval. */
    void setInputInterval()
    {
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        endField.setText(""+e);
        startField.setText(""+s);
        domainSlider.setStartValue(s);
        domainSlider.setEndValue(e);
    }
    
    /** Sets the value of the codomain interval. */
    void setCodomainInterval()
    {
        codomainSlider.setStartValue(model.getCodomainStart());
        codomainSlider.setEndValue(model.getCodomainEnd());
    }
    
    /**
     * Reacts to property changes fired by the {@link TwoKnobsSlider}s.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        Object source = evt.getSource();
        if (name.equals(TwoKnobsSlider.KNOB_RELEASED_PROPERTY)) {
            if (source.equals(domainSlider)) {
                controller.setInputInterval(domainSlider.getStartValue(), 
                        domainSlider.getEndValue(), true);
            } else if (source.equals(codomainSlider)) {
                int s = codomainSlider.getStartValue();
                int e = codomainSlider.getEndValue();
                controller.setCodomainInterval(s, e, true);
            }
        }
    }

    /**
     * Sets the pixels intensity window when the start or end value is modified.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) {
                case START_SELECTED:
                    startSelectionHandler(); break;
                case END_SELECTED:
                    endSelectionHandler(); 
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }

    /** 
     * Handles the lost of focus on the start text field and end
     * text field.
     * If focus is lost while editing, then we don't consider the text 
     * currently displayed in the text field and we reset it to the current
     * timepoint.
     * @see FocusListener#focusLost(FocusEvent)
     */
    public void focusLost(FocusEvent fe)
    {
        String sVal = startField.getText(), s = ""+model.getWindowStart();
        String eVal = endField.getText(), e = ""+model.getWindowEnd();
        if (sVal == null || !sVal.equals(s)) startField.setText(s);
        if (eVal == null || !eVal.equals(e)) endField.setText(e);
    }
    
    /** 
     * Required by the {@link FocusListener} I/F but not actually needed 
     * in our case, no op implementation.
     * @see FocusListener#focusGained(FocusEvent)
     */ 
    public void focusGained(FocusEvent e) {}
    
}
