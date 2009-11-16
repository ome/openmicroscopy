/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.GraphicsPane 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.rnd;


//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;
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
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class GraphicsPane  
	extends JPanel
    implements ActionListener, PropertyChangeListener
{

	/** 
	 * Factor used to determine the percentage of the range added 
	 * (resp. removed) to (resp. from) the maximum (resp. the minimum).
	 */
	private static final double RATIO = 0.2;
	
	/** Text of the preview check box. */
	private static final String	PREVIEW = "Immediate Update";
	
	/** The description of the preview check box. */
	private static final String	PREVIEW_DESCRIPTION = "Update the " +
			"rendering settings without releasing the mouse.";
    
    /** 
     * Action command ID to set the start and end values to the minimum and 
     * maximum.
     */
    private static final int	RANGE = 0;
    
    /** 
     * Action command ID to apply the settings to all selected or displayed
     * images.
     */
    private static final int	APPLY = 1;
    
    /** Slider to select a sub-interval of [0, 255]. */
    private TwoKnobsSlider      	codomainSlider;
    
    /** Slider to select the pixels intensity interval. */
    private TextualTwoKnobsSlider	domainSlider;
    
    /** The label displaying the global max. */
    private JLabel              	maxLabel;
    
    /** The label displaying the global minimum. */
    private JLabel              	minLabel;
    
    /** Button to set the start and end value to the minimum and maximum. */
    private JButton					rangeButton;
    
    /** The component displaying the plane histogram. */
    private GraphicsPaneUI      	uiDelegate;
    
    /** Reference to the Model.*/
    protected RendererModel     	model;
    
    /** Reference to the Control.*/
    protected RendererControl   	controller;

    /** Preview option for render settings */
    private JCheckBox				preview;

    /** Flag indicating to paint a line when moving the sliders' knobs. */
    private boolean 				paintLine;
    
    /** The equation the horizontal line. */
    private int						horizontalLine = -1;
    
    /** The equation of the vertical line. */
    private int						verticalLine = -1;

    /** Button to apply the settings to all selected or displayed image. */
    private JButton					applyButton;
    
    /** Initializes the domain slider. */
    private void initDomainSlider()
    {
    	int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        int absMin = (int) model.getLowestValue();
        int absMax = (int) model.getHighestValue();
        int min = (int) model.getGlobalMin();
        int max = (int) model.getGlobalMax();
        double range = (max-min)*RATIO;
        int lowestBound = (int) (min-range);
        if (lowestBound < absMin) lowestBound = absMin;
        int highestBound = (int) (max+range);
        if (highestBound > absMax) highestBound = absMax;
        //domainSlider.setValues(highestBound, lowestBound, max, min, s, e);
        domainSlider.setValues(max, min, highestBound, lowestBound,
        		max, min, s, e);
        if (model.getMaxC() > Renderer.MAX_CHANNELS)
        	domainSlider.setInterval(min, max);

    }
    
    /** Initializes the components. */
    private void initComponents()
    {
        uiDelegate = new GraphicsPaneUI(this, model);
        codomainSlider = new TwoKnobsSlider(RendererModel.CD_START, 
                                        RendererModel.CD_END, 
                                        model.getCodomainStart(),
                                        model.getCodomainEnd());
        codomainSlider.setBackground(UIUtilities.BACKGROUND_COLOR);
        codomainSlider.setPaintLabels(false);
        codomainSlider.setPaintEndLabels(false);
        codomainSlider.setPaintTicks(false);
        codomainSlider.setOrientation(TwoKnobsSlider.VERTICAL);
        codomainSlider.addPropertyChangeListener(this);
        
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        domainSlider = new TextualTwoKnobsSlider((int) model.getGlobalMin(), 
    			(int) model.getGlobalMax(), (int) model.getGlobalMin(), 
    			(int) model.getGlobalMax(), s, e);
        domainSlider.setValues((int) model.getGlobalMax(), 
        		(int) model.getGlobalMin(), (int) model.getHighestValue(), 
        		(int) model.getLowestValue(), (int) model.getGlobalMin(), 
    			(int) model.getGlobalMax(), s, e);
        /*
        domainSlider = new TextualTwoKnobsSlider((int) model.getLowestValue(), 
    			(int) model.getHighestValue(), (int) model.getGlobalMin(), 
    			(int) model.getGlobalMax(), s, e);
    	*/
        domainSlider.setBackground(UIUtilities.BACKGROUND_COLOR);
        initDomainSlider();
        domainSlider.getSlider().setPaintLabels(false);
        domainSlider.getSlider().setPaintEndLabels(false);
        domainSlider.getSlider().setPaintTicks(false);
        domainSlider.addPropertyChangeListener(this);
        double min = model.getGlobalMin();
        double max = model.getGlobalMax();
       
        maxLabel = new JLabel(""+(int) max);
        minLabel = new JLabel(""+(int) min);
        maxLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
        minLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
        preview = new JCheckBox(PREVIEW);
        preview.setBackground(UIUtilities.BACKGROUND_COLOR);
        preview.setToolTipText(PREVIEW_DESCRIPTION);
        rangeButton = new JButton("Min/Max");
        rangeButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        rangeButton.addActionListener(this);
        rangeButton.setActionCommand(""+RANGE);
        rangeButton.setToolTipText("Apply maximum range to all channels.");
        applyButton = new JButton("Apply to All");
        applyButton.setToolTipText("Apply settings to the displayed images.");
        applyButton.setBackground(UIUtilities.BACKGROUND_COLOR);
        applyButton.addActionListener(this);
        applyButton.setActionCommand(""+APPLY);
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setBackground(UIUtilities.BACKGROUND_COLOR);
    	double size[][] = {{TableLayout.FILL},  // Columns
    	         {TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}; // Rows
    	    	setLayout(new TableLayout(size));
    	//setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(buildGraphicsPane(), "0, 0");
        add(buildFieldsControls(), "0, 2");
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
    	 p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	 int knobWidth = domainSlider.getSlider().getKnobWidth();
    	 int knobHeight = domainSlider.getSlider().getKnobHeight();
    	 int width = codomainSlider.getPreferredSize().width;
    	 double size[][] =
         {{width, (double) knobWidth/2, TableLayout.FILL, (double) knobWidth/2},  // Columns
          {(double) knobHeight/2, TableLayout.FILL, (double) knobHeight/2, 
        	 (double) knobHeight+2, TableLayout.PREFERRED, 5}}; // Rows
    	 p.setLayout(new TableLayout(size));
    	 p.add(codomainSlider, "0, 0, 0, 2");
    	 p.add(uiDelegate, "2, 1");
    	 p.add(domainSlider.getSlider(), "1, 3, 3, 3");
    	 if (!model.isGeneralIndex())
    		 p.add(preview, "0, 4, 3, 4");
         return p;
    }
    
    /**
     * Builds and lays out the UI component hosting the text fields.
     * 
     * @return See above.
     */
    private JPanel buildFieldsControls()
    {
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        double size[][] =
        {{TableLayout.PREFERRED, 10, TableLayout.FILL},  // Columns
         {TableLayout.PREFERRED, 5}}; // Rows
   	 	p.setLayout(new TableLayout(size));
   	 	JPanel panel = buildFieldsPanel("Min", minLabel, "Start", 
   	 			domainSlider.getFieldComponent(TextualTwoKnobsSlider.START));
   	 	p.add(panel, "0, 0");
   	 	panel = buildFieldsPanel("Max", maxLabel, "End", 
   	 		domainSlider.getFieldComponent(TextualTwoKnobsSlider.END));
   	 	p.add(panel, "2, 0");
   	 	
   	 	JPanel content = new JPanel();
   	 	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
   	 	content.setBackground(UIUtilities.BACKGROUND_COLOR);
   	 	content.add(p);
   	 	JPanel controls = new JPanel();
   	 	controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
   	 	JPanel comp;
   	 	if (model.getMaxC() < Renderer.MAX_CHANNELS) {
	   	 	comp = UIUtilities.buildComponentPanel(rangeButton);
	   	 	comp.setBackground(UIUtilities.BACKGROUND_COLOR);
	   	 	controls.add(comp);
   	 	}
   	 	if (model.isGeneralIndex()) {
	   	 	comp = UIUtilities.buildComponentPanel(applyButton);
	   	 	comp.setBackground(UIUtilities.BACKGROUND_COLOR);
	   	 	controls.add(comp);
   	 	}
   	 	content.add(controls);
        return content;
    }
    
    /**
     * Builds panel used to display the min/start pair or max/end pair.
     * 
     * @param txt1  The text associated to the global value.
     * @param l     The label displaying the global value.
     * @param txt2  The text associated to the interval bound.
     * @param f     The component displaying the interval bound.
     * @return  See above.
     */
    private JPanel buildFieldsPanel(String txt1, JLabel l, String txt2, 
                                JComponent f)
    {
        JPanel p = new JPanel();
        p.setBackground(UIUtilities.BACKGROUND_COLOR);
        double size[][] =
        {{TableLayout.PREFERRED, 5, TableLayout.PREFERRED},  // Columns
         {TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}; // Rows
   	 	p.setLayout(new TableLayout(size));
   	 	JLabel label =  new JLabel();
   	 	label.setBackground(UIUtilities.BACKGROUND_COLOR);
   	 	label.setText(txt1);
   	 	p.add(label, "0, 0");
   	 	p.add(l, "2, 0");
   	 	label = new JLabel();
   	 	label.setBackground(UIUtilities.BACKGROUND_COLOR);
   	 	label.setText(txt2);
   	 	p.add(label, "0, 2");
   	 	p.add(f, "2, 2");
        return p;
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
    	if (!model.hasSelectedChannel()) return;
        int min = (int) model.getGlobalMin();
        int max = (int) model.getGlobalMax();
        minLabel.setText(""+min);
        maxLabel.setText(""+max);
        initDomainSlider();
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        domainSlider.setInterval(s, e);
        onCurveChange();
    }
    
    /** Sets the pixels intensity interval. */
    void setInputInterval()
    {
        int s = (int) model.getWindowStart();
        int e = (int) model.getWindowEnd();
        domainSlider.setInterval(s, e);
        onCurveChange();
    }
    
    /** Sets the value of the codomain interval. */
    void setCodomainInterval()
    {
    	codomainSlider.setInterval(model.getCodomainStart(), 
    			model.getCodomainEnd());
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
    int getPartialMinimum()
    { 
    	return domainSlider.getSlider().getPartialMinimum(); 
    }
    
    /**
     * Returns the value of the partial maximum.
     * 
     * @return See above.
     */
    int getPartialMaximum()
    { 
    	return domainSlider.getSlider().getPartialMaximum();
    }
    
    /** 
     * Sets the enabled flag of the UI components.
     * 
     * @param b The value to set.
     */
	void onStateChange(boolean b)
	{
		if (codomainSlider != null) codomainSlider.setEnabled(b);
		if (domainSlider != null) domainSlider.setEnabled(b);
		if (rangeButton != null) rangeButton.setEnabled(b);
		if (applyButton != null) applyButton.setEnabled(b);
	}
	
	/** Updates the UI when the rendering settings have been applied. */
	void onSettingsApplied()
	{
		if (applyButton != null) applyButton.setEnabled(true);
	}
	
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
							domainSlider.getEndValue());
					onCurveChange();
				} else if (source.equals(codomainSlider)) {
					int s = codomainSlider.getStartValue();
					int e = codomainSlider.getEndValue();
					controller.setCodomainInterval(s, e);
					onCurveChange();
				}
			}
			if (TwoKnobsSlider.LEFT_MOVED_PROPERTY.equals(name)){
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
							domainSlider.getEndValue());
					onCurveChange();
				} else if (source.equals(codomainSlider)) {
					int s = codomainSlider.getStartValue();
					int e = codomainSlider.getEndValue();
					controller.setCodomainInterval(s, e);
					onCurveChange();
				}
			}
			if (TwoKnobsSlider.KNOB_RELEASED_PROPERTY.equals(name)) {
				if (source.equals(domainSlider)) {
					controller.setInputInterval(domainSlider.getStartValue(),
							domainSlider.getEndValue());
				} else if (source.equals(codomainSlider)) {
					int s = codomainSlider.getStartValue();
					int e = codomainSlider.getEndValue();
					controller.setCodomainInterval(s, e);
					onCurveChange();
				}
			}
		}
	}

    /**
     * Sets the range.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case RANGE:
				//Sets the range for all channels
				controller.setRangeAllChannels();
            	//controller.setInputInterval(model.getGlobalMin(), 
            	//						model.getGlobalMax());
            	break;
			case APPLY:
				applyButton.setEnabled(false);
				controller.applyToAll();
		}
	}

}
