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
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.TextualTwoKnobsSlider;
import org.openmicroscopy.shoola.util.ui.slider.TwoKnobsSlider;
import pojos.ChannelData;

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
    implements PropertyChangeListener
{

	/** 
	 * Factor used to determine the percentage of the range added 
	 * (resp. removed) to (resp. from) the maximum (resp. the minimum).
	 */
	static final double RATIO = 0.2;
	
	/** Text of the preview check box. */
	private static final String	PREVIEW = "Immediate Update";
	
	/** The description of the preview check box. */
	private static final String	PREVIEW_DESCRIPTION = "Update the " +
			"rendering settings immediately. Not available for large " +
			"images";
    
    /** Slider to select a sub-interval of [0, 255]. */
    private TwoKnobsSlider      	codomainSlider;
    
    /** Slider to select the pixels intensity interval. */
    private TextualTwoKnobsSlider	domainSlider;
    
    /** The label displaying the global max. */
    private JLabel              	maxLabel;
    
    /** The label displaying the global minimum. */
    private JLabel              	minLabel;

    /** The component displaying the plane histogram. */
    private GraphicsPaneUI      	uiDelegate;
    
    /** Reference to the Model.*/
    protected RendererModel     	model;
    
    /** Reference to the Control.*/
    protected RendererControl   	controller;

    /** Preview option for render settings */
    private JCheckBox				preview;

    /** Flag indicating to paint a line when moving the sliders' knobs. */
    //private boolean 				paintLine;
    
    /** Flag indicating to paint a vertical line. */
    private boolean 				paintVertical;
    
    /** Flag indicating to paint a vertical line. */
    private boolean 				paintHorizontal;
    
    /** The equation the horizontal line. */
    private int						horizontalLine = -1;
    
    /** The equation of the vertical line. */
    private int						verticalLine = -1;
    
    /** Hosts the sliders controlling the pixels intensity values. */
    private List<ChannelSlider> 	sliders;
    
    /**
	 * Formats the specified value.
	 * 
	 * @param value The value to format.
	 * @return See above.
	 */
    private String formatValue(double value)
	{
		if (model.getRoundFactor() == 1) return ""+(int) value;
		return UIUtilities.formatToDecimal(value);
	}
    
    /** Initializes the domain slider. */
    private void initDomainSlider()
    {
    	int f = model.getRoundFactor();
    	int s = (int) (model.getWindowStart()*f);
        int e = (int) (model.getWindowEnd()*f);
        int absMin = (int) (model.getLowestValue()*f);
        int absMax = (int) (model.getHighestValue()*f);
        int min = (int) (model.getGlobalMin()*f);
        int max = (int) (model.getGlobalMax()*f);
        double range = (max-min)*RATIO;
        int lowestBound = (int) (min-range);
        if (lowestBound < absMin) lowestBound = absMin;
        int highestBound = (int) (max+range);
        if (highestBound > absMax) highestBound = absMax;
        //domainSlider.setValues(highestBound, lowestBound, max, min, s, e);
        domainSlider.setValues(max, min, highestBound, lowestBound,
        		max, min, s, e, f);
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
        codomainSlider.setColourGradients(Color.BLACK, Color.WHITE);
        //codomainSlider.setOrientation(TwoKnobsSlider.VERTICAL);
        codomainSlider.addPropertyChangeListener(this);
        
        domainSlider = new TextualTwoKnobsSlider();
        domainSlider.setBackground(UIUtilities.BACKGROUND_COLOR);
        initDomainSlider();
        domainSlider.getSlider().setPaintLabels(false);
        domainSlider.getSlider().setPaintEndLabels(false);
        domainSlider.getSlider().setPaintTicks(false);
        domainSlider.addPropertyChangeListener(this);
        maxLabel = new JLabel(formatValue(model.getGlobalMax()));
        minLabel = new JLabel(formatValue(model.getGlobalMin()));
        maxLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
        minLabel.setBackground(UIUtilities.BACKGROUND_COLOR);
        preview = new JCheckBox(PREVIEW);
        preview.setEnabled(!model.isBigImage());
        preview.setBackground(UIUtilities.BACKGROUND_COLOR);
        preview.setToolTipText(PREVIEW_DESCRIPTION);
        if (model.getMaxC() < Renderer.MAX_CHANNELS) {
        //if (model.isGeneralIndex() && model.getMaxC() < Renderer.MAX_CHANNELS) {
        	sliders = new ArrayList<ChannelSlider>();
        	List<ChannelData> channels = model.getChannelData();
        	Iterator<ChannelData> i = channels.iterator();
        	ChannelSlider slider;
        	int columns = 0;
        	while (i.hasNext()) {
        		slider = new ChannelSlider(this, model, controller, i.next());
        		columns = Math.max(columns, slider.getColumns());
        		sliders.add(slider);
			}
        	Iterator<ChannelSlider> j = sliders.iterator();
        	while (j.hasNext()) {
        		j.next().setColumns(columns);
			}
        }
    }
    
    /** Builds and lays out the GUI. */
    private void buildGUI()
    {
    	setBackground(UIUtilities.BACKGROUND_COLOR);
    	double size[][] = {{TableLayout.FILL},  // Columns
    	         {TableLayout.PREFERRED, 5, TableLayout.PREFERRED}}; // Rows
    	    	setLayout(new TableLayout(size));
    	if (model.isGeneralIndex()) {
    		add(buildGeneralPane(), "0, 0");
    	} else {
    		add(buildPane(), "0, 0");
    		add(buildGeneralPane(), "0, 2");
    		/*
    		add(buildGraphicsPane(), "0, 0");
            add(buildFieldsControls(), "0, 2");
            */
    	}
    }
    
    /** 
     * Builds hosting the various sliders
     * 
     * @return See above.
     */
    private JPanel buildGeneralPane()
    {
    	JPanel content = new JPanel();
    	content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    	content.add(new JSeparator());
   	 	content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
   	 	JPanel p = UIUtilities.buildComponentPanel(preview);
   	 	p.setBackground(UIUtilities.BACKGROUND_COLOR);
   	 	content.add(p);
   	 	content.setBackground(UIUtilities.BACKGROUND_COLOR);
    	Iterator<ChannelSlider> i = sliders.iterator();
    	while (i.hasNext()) {
    		content.add(i.next());
    	}
    	return content;
    	
    }
    /** 
     * Builds the UI component hosting the controls used to determine the
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
    	 //if (!model.isGeneralIndex())
    	 p.add(preview, "0, 4, 3, 4");
         return p;
    }
 
    /** 
     * Builds and lays out the slider.
     * 
     * @return See above.
     */
    private JPanel buildPane()
    {
    	 JPanel p = new JPanel();
    	 p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    	 p.setBackground(UIUtilities.BACKGROUND_COLOR);
    	 p.add(codomainSlider);
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
        minLabel.setText(formatValue(model.getGlobalMin()));
        maxLabel.setText(formatValue(model.getGlobalMax()));
        initDomainSlider();
        int f = model.getRoundFactor();
        int s = (int) (model.getWindowStart()*f);
        int e = (int) (model.getWindowEnd()*f);
        domainSlider.setInterval(s, e);
        onCurveChange();
    }
    
    /** Sets the pixels intensity interval. */
    void setInputInterval()
    {
    	int f, s, e;
    	if (model.isGeneralIndex()) {
    		Iterator<ChannelSlider> i = sliders.iterator();
    		ChannelSlider slider;
    		while (i.hasNext()) {
    			slider = i.next();
    			f = model.getRoundFactor(slider.getIndex());
                s = (int) (model.getWindowStart(slider.getIndex())*f);
                e = (int) (model.getWindowEnd(slider.getIndex())*f);
                slider.setInterval(s, e);
			}
    	} else {
    		f = model.getRoundFactor();
            s = (int) (model.getWindowStart()*f);
            e = (int) (model.getWindowEnd()*f);
            domainSlider.setInterval(s, e);
            onCurveChange();
    	}
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
     * Returns <code>true</code> if a vertical or horizontal line has 
     * to be painted, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isPaintLine() { return paintVertical() || paintHorizontal(); }
    
    /**
     * Returns <code>true</code> if the preview is selected, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isPreviewSelected() { return preview.isSelected(); }
    
    /**
     * Returns <code>true</code> if a vertical line has 
     * to be painted, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean paintVertical() { return paintVertical; }
    
    /**
     * Returns <code>true</code> if a horizontal line has 
     * to be painted, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean paintHorizontal() { return paintHorizontal; }
    
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
	}
	
	/** Toggles between color model and Greyscale. */
    void setColorModelChanged() 
    {
    	if (sliders == null || sliders.size() == 0) return;
    	Iterator<ChannelSlider> i = sliders.iterator();
    	while (i.hasNext()) {
			 i.next().setColorModelChanged();
		}
    }
    
    /**
     * Sets the color of the passed channel.
     *  
     * @param index The index of the channel.
     */
    void setChannelColor(int index)
    {
    	if (sliders != null && sliders.size() > 0) {
    		Iterator<ChannelSlider> i = sliders.iterator();
    		ChannelSlider slider;
        	while (i.hasNext()) {
        		slider = i.next();
        		if (slider.getIndex() == index) {
        			slider.setChannelColor();
        			break;
        		}
    		}
    	}
    	repaint();
    }
    
    JComponent getCodomainSlider() { return codomainSlider; }
    
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
				//paintLine = false;
				//horizontalLine = -1;
				//verticalLine = -1;
				paintHorizontal = false;
				paintVertical = false;
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
				//paintLine = true;
				if (source.equals(domainSlider)) {
					verticalLine = (int) 
						(domainSlider.getStartValue()
						*domainSlider.getRoundingFactor());
					//horizontalLine = -1;
					paintHorizontal = false;
					paintVertical = true;
					onCurveChange();
				} else if (source.equals(codomainSlider)) {
					horizontalLine = codomainSlider.getEndValue();
					//verticalLine = -1;
					paintHorizontal = true;
					paintVertical = false;
					onCurveChange();
				}
			} else if (TwoKnobsSlider.RIGHT_MOVED_PROPERTY.equals(name)) {
				//paintLine = true;
				if (source.equals(domainSlider)) {
					verticalLine = (int) (domainSlider.getEndValue()
						*domainSlider.getRoundingFactor());
					horizontalLine = -1;
					paintHorizontal = false;
					paintVertical = true;
					onCurveChange();
				} else if (source.equals(codomainSlider)) {
					horizontalLine = codomainSlider.getStartValue();
					verticalLine = -1;
					paintHorizontal = true;
					paintVertical = false;
					onCurveChange();
				}
			} 
		} else {
			//paintLine = false;
			paintHorizontal = false;
			paintVertical = false;
			//horizontalLine = -1;
			//verticalLine = -1;
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

}
