/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.GraphicsPane
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer.rnd;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//Third-party libraries
import layout.TableLayout;

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
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
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

	/** Text of the preview check box. */
	private static final String		PREVIEW = "Immediate Update";
	
	/** The description of the preview check box. */
	private static final String		PREVIEW_DESCRIPTION = "Update the " +
			"rendering settings without releasing the mouse.";
		
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

    /** Preview option for render settings */
    private JCheckBox			preview;

    /** Flag indicating to paint a line when moving the sliders' knobs. */
    private boolean 			paintLine;
    
    /** The equation the horizontal line. */
    private int					horizontalLine = -1;
    
    /** The equation of the vertical line. */
    private int					verticalLine = -1;
    
    /** Initializes the domain slider. */
    private void initDomainSlider()
    {
    	int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
    	RendererFactory.initSlider(domainSlider, (int) model.getLowestValue(), 
    			(int) model.getHighestValue(), (int) model.getGlobalMin(), 
    			(int) model.getGlobalMax(), s, e);
    }
    
    /** Initializes the components. */
    private void initComponents()
    {
        uiDelegate = new GraphicsPaneUI(this, model);
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
        domainSlider = new TwoKnobsSlider();
        initDomainSlider();
        domainSlider.setPaintLabels(false);
        domainSlider.setPaintEndLabels(false);
        domainSlider.setPaintTicks(false);
        domainSlider.addPropertyChangeListener(this);
        double min = model.getGlobalMin();
        double max = model.getGlobalMax();
        startField = new JTextField();
        int length = (""+max).length()-2; 
        startField.setColumns(length);
        endField = new JTextField();
        endField.setColumns(length);
        startField.setText(""+s);
        endField.setText(""+e);
        startField.addActionListener(this);
        startField.setActionCommand(""+START_SELECTED);
        startField.addFocusListener(this);
        endField.addActionListener(this);
        endField.setActionCommand(""+END_SELECTED);
        endField.addFocusListener(this);
        maxLabel = new JLabel(""+(int) max);
        minLabel = new JLabel(""+(int) min);
        preview = new JCheckBox(PREVIEW);
        preview.setToolTipText(PREVIEW_DESCRIPTION);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(buildGraphicsPane());
        add(buildFieldsControls());
    }
    
    /** 
     * Builds the UI component hosting the controls used to determine th
     * input and output windows, and the histogram.
     * 
     * @return See above.
     */
    private JPanel buildGraphicsPane()
    {
    	 JPanel p = new JPanel();
    	 int knobWidth = domainSlider.getKnobWidth();
    	 int knobHeight = domainSlider.getKnobHeight();
    	 int width = codomainSlider.getPreferredSize().width;
    	 double size[][] =
         {{width, knobWidth/2, TableLayout.FILL, knobWidth/2},  // Columns
          {knobHeight/2, TableLayout.FILL, knobHeight/2, knobHeight+2, 
        	 TableLayout.PREFERRED, 5}}; // Rows
    	 p.setLayout(new TableLayout(size));
    	 p.add(codomainSlider, "0, 0, 0, 2");
    	 p.add(uiDelegate, "2, 1");
    	 p.add(domainSlider, "1, 3, 3, 3");
    	 p.add(preview, "0, 4, 3, 4");
         return p;//UIUtilities.buildComponentPanel(p);
    }
    
    /**
     * Builds and lays out the UI component hosting the text fields.
     * 
     * @return See above.
     */
    private JPanel buildFieldsControls()
    {
        JPanel p = new JPanel();
        double size[][] =
        {{TableLayout.PREFERRED, 10, TableLayout.PREFERRED},  // Columns
         {TableLayout.PREFERRED, 5}}; // Rows
   	 	p.setLayout(new TableLayout(size));
   	 	JPanel panel = buildFieldsPanel("Min", minLabel, "Start", startField);
   	 	p.add(panel, "0, 0");
   	 	panel = buildFieldsPanel("Max", maxLabel, "End", endField);
   	 	p.add(panel, "2, 0");
        return p;//UIUtilities.buildComponentPanel(p);
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
        double size[][] =
        {{TableLayout.PREFERRED, 5, TableLayout.PREFERRED},  // Columns
         {TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}; // Rows
   	 	p.setLayout(new TableLayout(size));
   	 	JLabel label =  new JLabel();
   	 	label.setText(txt1);
   	 	p.add(label, "0, 0");
   	 	p.add(l, "2, 0");
   	 	label =  new JLabel();
   	 	label.setText(txt2);
   	 	p.add(label, "0, 2");
   	 	p.add(f, "2, 2");
        return p;
    }
    
    /** 
     * Checks the validity of the startField. This method will be called when
     * the startField changes value; user enters data. 
     * 
     * @return true if startField is in a valid range. 
     */
    private boolean startFieldValid()
    {
        double val = 0;
        double e = model.getWindowEnd();
        try {
            val = Double.parseDouble(startField.getText());
            return (model.getLowestValue() <= val && val < e);
        } catch(NumberFormatException nfe) {}
        return false;
    }
    
    /** 
     * Checks the validity of the endField. This method will be called when
     * the endField changes value; user enters data. 
     * 
     * @return true if endField is in a valid range. 
     */
    private boolean endFieldValid()
    {
         double val = 0;
         double s = model.getWindowStart();
         try {
             val = Double.parseDouble(endField.getText());
             return (s < val && val <= model.getHighestValue());
         } catch(NumberFormatException nfe) {}
         return false;
    }
    
    /** 
     * Handles the action event fired by the start text field when the user 
     * enters some text. 
     * If that text doesn't evaluate to a value, then we simply 
     * suggest the user to enter a valid one.
     */
    private void startSelectionHandler()
    {
    	double e = model.getWindowEnd();
    	double val = -1;
    	try {
    		val = Double.parseDouble(startField.getText());
    	} catch(NumberFormatException nfe) { 
    		UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
    		un.notifyInfo("Invalid pixels intensity interval", 
    				" The value must be in the interval ["+
    				(int) model.getWindowStart()+","+(int) e+"]");
    		return;
    	}
    	
        if (val == model.getWindowStart()) return;
             
    	if (startFieldValid()) {
    		controller.setInputInterval(val, e, true);
    		onCurveChange();
    	} else {
    		startField.selectAll();
            UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid pixels intensity interval", 
            				"The value must be in the interval "+
            				"["+(int) val+","+(int) e+"]");
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
    	double s = model.getWindowStart();
    	double val = -1;
    	try {
    		val = Double.parseDouble(endField.getText());
    	} catch(NumberFormatException nfe) { 
    		UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
    		un.notifyInfo("Invalid pixels intensity interval", 
    				"The value must be in the interval ["+
    				(int) s+","+(int) model.getWindowEnd()+"]");
    		return;
    	}
        if (val == model.getWindowEnd()) return;
    	if (endFieldValid()) {
    		controller.setInputInterval(s, val, true);
    		onCurveChange();
    	} else {
    		endField.selectAll();
            UserNotifier un = ImViewerAgent.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid pixels intensity interval", 
            			"The value must be in the interval "+
                    	"["+(int) s+","+(int) val+"]");
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
        initDomainSlider();
        /*
        domainSlider.setValues((int) model.getHighestValue(), 
        		(int) model.getLowestValue(), max, min, s, e);
        		*/
        onCurveChange();
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
        onCurveChange();
    }
    
    /** Sets the value of the codomain interval. */
    void setCodomainInterval()
    {
        codomainSlider.setStartValue(model.getCodomainStart());
        codomainSlider.setEndValue(model.getCodomainEnd());
        onCurveChange();
    }
    
    /** 
     * Updates the UI when a new curve is selected i.e. when a new family
     * is selected or when a new gamma value is selected.
     */
    void onCurveChange()
    {
        uiDelegate.invalidate();
        uiDelegate.repaint();
    }
    
    /**
     * Returns <code>true</code> if a vertical or horizontal has 
     * to be painted, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isPaintLine() { return paintLine; }
    
    /**
     * Returns the equation of the horizontal line or <code>-1</code>
     * if no horizontal line defined.
     * 
     * @return See above.
     */
    int getHorizontalLine() { return horizontalLine; }
    
    /**
     * Returns the equation of the vertical line or <code>-1</code>
     * if no vertical line defined.
     * 
     * @return See above.
     */
    int getVerticalLine() { return verticalLine; }
     
    /**
     * Returns the value of the partial minimum.
     * 
     * @return See above.
     */
    int getPartialMinimum() { return domainSlider.getPartialMinimum(); }
    
    /**
     * Returns the value of the partial maximum.
     * 
     * @return See above.
     */
    int getPartialMaximum() { return domainSlider.getPartialMaximum(); }
    
    /**
     * Reacts to property changes fired by the {@link TwoKnobsSlider}s.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
		String name = evt.getPropertyName();
		Object source = evt.getSource();
		if (!preview.isSelected()) {
			if (TwoKnobsSlider.KNOB_RELEASED_PROPERTY.equals(name)) {
				paintLine = false;
				horizontalLine = -1;
				verticalLine = -1;
				if (source.equals(domainSlider)) {
					controller.setInputInterval(domainSlider.getStartValue(),
							domainSlider.getEndValue(), true);
					onCurveChange();
				} else if (source.equals(codomainSlider)) {
					int s = codomainSlider.getStartValue();
					int e = codomainSlider.getEndValue();
					controller.setCodomainInterval(s, e, true);
					onCurveChange();
				}
			}
			if (TwoKnobsSlider.LEFT_MOVED_PROPERTY.equals(name)) {
				paintLine = true;
				if (source.equals(domainSlider)) {
					verticalLine = domainSlider.getStartValue();
					horizontalLine = -1;
					onCurveChange();
				} else if (source.equals(codomainSlider)) {
					horizontalLine = codomainSlider.getEndValue();
					verticalLine = -1;
					onCurveChange();
				}
			} else if (TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name)) {
				paintLine = true;
				if (source.equals(domainSlider)) {
					verticalLine = domainSlider.getEndValue();
					horizontalLine = -1;
					onCurveChange();
				} else if (source.equals(codomainSlider)) {
					horizontalLine = codomainSlider.getStartValue();
					verticalLine = -1;
					onCurveChange();
				}
			}
		} else {
			paintLine = false;
			horizontalLine = -1;
			verticalLine = -1;
			if (TwoKnobsSlider.LEFT_MOVED_PROPERTY.equals(name)
					|| TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name)) {
				if (source.equals(domainSlider)) {
					controller.setInputInterval(domainSlider.getStartValue(),
							domainSlider.getEndValue(), false);
					onCurveChange();
				} else if (source.equals(codomainSlider)) {
					int s = codomainSlider.getStartValue();
					int e = codomainSlider.getEndValue();
					controller.setCodomainInterval(s, e, false);
					onCurveChange();
				}
			}
			if (TwoKnobsSlider.KNOB_RELEASED_PROPERTY.equals(name)) {
				if (source.equals(domainSlider)) {
					controller.setInputInterval(domainSlider.getStartValue(),
							domainSlider.getEndValue(), true);
					onCurveChange();
				} else if (source.equals(codomainSlider)) {
					int s = codomainSlider.getStartValue();
					int e = codomainSlider.getEndValue();
					controller.setCodomainInterval(s, e, true);
					onCurveChange();
				}
			}
		}
	}

    /**
	 * Sets the pixels intensity window when the start or end value is modified.
	 * 
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
      if (fe.getSource() == startField) {
    	  if (startFieldValid()) startSelectionHandler();
  		  else startField.setText(""+(int) model.getWindowStart());
      }
      if (fe.getSource() == endField) {
    	  if (endFieldValid()) endSelectionHandler();
  		  else endField.setText(""+model.getWindowEnd());
      }	
    }
    
    /** 
     * Required by the {@link FocusListener} I/F but not actually needed 
     * in our case, no op implementation.
     * @see FocusListener#focusGained(FocusEvent)
     */ 
    public void focusGained(FocusEvent e) {}

}