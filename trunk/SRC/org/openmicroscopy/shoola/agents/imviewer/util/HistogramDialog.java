/*
 * org.openmicroscopy.shoola.agents.imviewer.util.HistogramDialog
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

package org.openmicroscopy.shoola.agents.imviewer.util;



//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.ImViewerAgent;
import org.openmicroscopy.shoola.agents.imviewer.rnd.Renderer;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;

/** 
 * A non-modal displaying the global minimum and maximum of a given
 * channel across time.
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
public class HistogramDialog
    extends JDialog
    implements ActionListener, FocusListener, PropertyChangeListener
    
{

    /** The window's title. */
    private static final String     TITLE = "Histogram across time";
    
    /** The text displayed in the window title pane.*/
    private static final String     TEXT =  " Select the pixels intensity " +
                                            "interval across time.";
    
    /** The text of the {@link #startLabel}. */
    private static final String     START = "Start: ";
    
    /** The text of the {@link #endLabel}. */
    private static final String     END = "End: ";
    
    /** Indicates that a new start value is selected. */
    private static final int        START_CMD = 0;
    
    /** Indicates that a new end value is selected. */
    private static final int        END_CMD = 1;
    
    /** Reference to the {@link Renderer}. */
    private Renderer        model;
    
    /** The label displaying the start value. */
    private JTextField      startLabel;
    
    /** The label displaying the end value. */
    private JTextField      endLabel;
    
    /** Slider used to determine the pixels intensity interval. */
    private TwoKnobsSlider  slider;
    
    /** The canvas displaying the histogram across time. */
    private HistogramCanvas canvas;
    
    /** Initializes the listners. */
    private void initListeners()
    {
        slider.addPropertyChangeListener(this);
        model.addPropertyChangeListener(this);
        startLabel.addActionListener(this);
        startLabel.setActionCommand(""+START_CMD);
        startLabel.addFocusListener(this);
        endLabel.addActionListener(this);
        endLabel.setActionCommand(""+START_CMD);
        endLabel.addFocusListener(this);
    }
    
    /** Initializes the components composing the display. */
    private void initComponents()
    {
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        startLabel = new JTextField(""+s);
        endLabel = new JTextField(""+e);
        int min = (int) model.getGlobalMin();
        int max = (int) model.getGlobalMax();
        slider = new TwoKnobsSlider(min, max, s, e);
        slider.setOrientation(TwoKnobsSlider.VERTICAL);
        slider.setPaintEndLabels(false);
        slider.setPaintLabels(false);
        slider.setPaintTicks(false);
        canvas = new HistogramCanvas();
    }
    
    /**
     * Builds and lays out the panel hosting the text fields.
     * 
     * @return See above
     */
    private JPanel buildLabels()
    {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        JLabel label = new JLabel(START);
        p.add(label, c);
        c.gridx = 1;
        p.add(startLabel, c);
        c.gridx = 0;
        c.gridy = 1;
        label = new JLabel(END);
        p.add(label, c);
        c.gridx = 1;
        p.add(endLabel, c);
        return p;
    }
    
    /**
     * Builds and lays out the widget's body.
     * 
     * @return See above.
     */
    private JPanel buildBody()
    {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.add(slider);
        p.add(canvas);
        body.add(p);
        body.add(buildLabels());
        return body;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        Container c = getContentPane();
        IconManager icons = IconManager.getInstance();
        TitlePanel tp = new TitlePanel(TITLE, TEXT, 
                icons.getIcon(IconManager.HISTOGRAM_BIG));
        c.add(tp, BorderLayout.NORTH);
        c.add(buildBody(), BorderLayout.CENTER);
        pack();
    }
    
    /** Sets pixels intensity interval. */
    private void setInputInterval()
    {
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        startLabel.setText(START+s);
        slider.setStartValue(s);
        endLabel.setText(END+e);
        slider.setEndValue(e);
    }
    
    /** Resets the values of widget. */
    private void resetValues()
    {
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        int min = (int) model.getGlobalMin();
        int max = (int) model.getGlobalMax();
        slider.setValues(max, min, s, e);
        startLabel.setText(START+s);
        endLabel.setText(END+e);
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
            val = Integer.parseInt(startLabel.getText());
            if (model.getGlobalMin() <= val && val < model.getWindowEnd()) 
                valid = true;
        } catch(NumberFormatException nfe) {}
        if (valid) {
            model.setInputInterval(val, model.getWindowEnd(), true);
        } else {
            startLabel.selectAll();
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
            val = Integer.parseInt(endLabel.getText());
            if (model.getWindowStart() < val && val <= model.getGlobalMax()) 
                valid = true;
        } catch(NumberFormatException nfe) {}
        if (valid) {
            model.setInputInterval(model.getWindowStart(), val, true);
        } else {
            endLabel.selectAll();
            UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid pixels intensity interval", 
                    "["+model.getWindowStart()+","+val+"]");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of this dialog.
     * @param model Reference to the model. Mustn't be <code>null</code>.
     */
    public HistogramDialog(JFrame owner, Renderer model)
    {
        super(owner);
        setTitle(TITLE);
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
        initComponents();
        initListeners();
        buildGUI();
    }

    /**
     * Reacts to channel selections and to pixels intensity interval selection.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (name.equals(TwoKnobsSlider.KNOB_RELEASED_PROPERTY))
            model.setInputInterval(slider.getStartValue(), slider.getEndValue(),
                                    true);
        else if (name.equals(Renderer.INPUT_INTERVAL_PROPERTY))
            setInputInterval(); 
        else if (name.equals(Renderer.SELECTED_CHANNEL_PROPERTY)) 
            resetValues();
    }

    /**
     * Reacts to values entered in the text fields. 
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) {
                case START_CMD:
                    startSelectionHandler(); break;
                case END_CMD:
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
        String sVal = startLabel.getText(), s = ""+model.getWindowStart();
        String eVal = endLabel.getText(), e = ""+model.getWindowEnd();
        if (sVal == null || !sVal.equals(s)) startLabel.setText(s);
        if (eVal == null || !eVal.equals(e)) endLabel.setText(e);
    }
    
    /** 
     * Required by the {@link FocusListener} I/F but not actually needed 
     * in our case, no op implementation.
     * @see FocusListener#focusGained(FocusEvent)
     */ 
    public void focusGained(FocusEvent e) {}
    
}
