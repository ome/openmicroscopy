/*
 * org.openmicroscopy.shoola.agents.imviewer.view.ControlPane
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

package org.openmicroscopy.shoola.agents.imviewer.view;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



//Third-party libraries
import layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorPickerAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ViewerAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelButton;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.slider.OneKnobSlider;

/** 
 * Presents variable controls.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 			<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ControlPane
    extends JPanel
    implements ActionListener, ChangeListener, MouseWheelListener
{

    /** The description of a z-sections selection slider. */
    private static final String Z_SLIDER_DESCRIPTION = "Use the slider to " +
                            "select a z-section.";

    /** The description of a timepoint selection slider. */
    private static final String T_SLIDER_DESCRIPTION = "Use the slider to " +
                            "select a timepoint.";
    
    /** The description of a magnification selection slider. */
    private static final String RATIO_SLIDER_DESCRIPTION = "Select the " +
                            "magnification factor of an image composing " +
                            "the grid.";

    /** The description of a magnification selection slider. */
    private static final String ZOOM_SLIDER_DESCRIPTION = "Select the " +
                            "magnification factor of the image.";
    
    /** The tipString of the {@link #zSlider}. */
    private static final String Z_SLIDER_TIPSTRING = "Z";

    /** The tipString of the {@link #tSlider}. */
    private static final String T_SLIDER_TIPSTRING = "T";
    
    /** The maximum height of a magnification slider. */
    private static final int	SLIDER_HEIGHT = 100;
    
    /** Dimension of the box between the channel buttons. */
    private static final Dimension VBOX = new Dimension(1, 10);
    
    /** Reference to the Control. */
    private ImViewerControl 		controller;
    
    /** Reference to the Model. */
    private ImViewerModel   		model;
    
    /** Reference to the View. */
    private ImViewerUI      		view;
    
    /** The box displaying the supported rating level. */
    private JComboBox       		ratingBox;
    
    /** Slider to select the z-section. */
    private OneKnobSlider			zSlider;
    
    /** Slider to select the timepoint. */
    private OneKnobSlider			tSlider;
    
    /** Slider to select the z-section. */
    private OneKnobSlider			zSliderGrid;
    
    /** Slider to select the timepoint. */
    private OneKnobSlider			tSliderGrid;
    
    /** Slider to select the z-section. */
    private OneKnobSlider			zSliderAnnotator;
    
    /** Slider to select the timepoint. */
    private OneKnobSlider			tSliderAnnotator;
    
    /** Slider to set the magnification factor of an image of the grid. */
    private OneKnobSlider			gridRatioSlider;
    
    /** Slider to set the magnification factor of the image. */
    private OneKnobSlider			ratioSlider;
    
    /** One  {@link ChannelButton} per channel. */
    private Set<ChannelButton>		channelButtons;

    /** One  {@link ChannelButton} per channel. */
    private Set<ChannelButton>		channelButtonsGrid;
   
    /** Button to play movie across channel. */
    private JButton         		channelMovieButton;
    
    /** Button to select the color model. */
    private JButton         		colorModelButton;
    
    /** Button to select the color model. */
    private JButton         		colorModelButtonGrid;
    
    /** Button to bring up the color picker. */
    private JButton         		colorPickerButton;
    
    /** Button to paint some textual information on top of the grid image. */
    private JToggleButton			textVisibleButton;
    
    /** Button to play movie across T. */
    private JButton					playTMovie;
    
    /** Button to play movie across T displayed in the split view. */
    private JButton					playTMovieGrid;
    
    /** Button to play movie across T. */
    private JButton					playZMovie;
    
    /** Button to play movie across T displayed in the split view. */
    private JButton					playZMovieGrid;
    
    /** Helper reference. */
    private IconManager     		icons;
    
    /**
     * Handles the event when the wheel is moved over the {@link #zSlider}
     * or {@link #zSliderGrid}.
     * 
     * @param e The event to handle.
     */
    private void mouseWheelMovedZ(MouseWheelEvent e)
    {
        boolean up = true;
        if (e.getWheelRotation() > 0) up = false;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int v = model.getDefaultZ()-e.getWheelRotation();
            if (up) {
                if (v <= model.getMaxZ())
                    controller.setSelectedXYPlane(v,  model.getDefaultT());
            } else { //moving down
                if (v >= 0)
                    controller.setSelectedXYPlane(v,  model.getDefaultT());
            }
        } else {
     
        }
    }
    
    /**
     * Handles the event when the wheel is moved over the {@link #tSlider}
     * or {@link #tSliderGrid}.
     * 
     * @param e The event to handle.
     */
    private void mouseWheelMovedT(MouseWheelEvent e)
    {
        boolean up = true;
        if (e.getWheelRotation() > 0) up = false;
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int v = model.getDefaultT()-e.getWheelRotation();
            if (up) {
                if (v <= model.getMaxT())
                    controller.setSelectedXYPlane(model.getDefaultZ(), v);
            } else { //moving down
                if (v >= 0)
                    controller.setSelectedXYPlane(model.getDefaultZ(), v);
            }
        } else {
            
        }
    }
    
    /**
     * Returns the description of the current color model.
     * 
     * @param model The color model.
     * @return See above.
     */
    private String getColorModelDescription(String model)
    {
    	if (model.equals(ImViewer.GREY_SCALE_MODEL))
            return ColorModelAction.DESCRIPTION_GREY_SCALE;
        else if (model.equals(ImViewer.RGB_MODEL))
        	return ColorModelAction.DESCRIPTION_RGB;
        else if (model.equals(ImViewer.HSB_MODEL))
        	return ColorModelAction.DESCRIPTION_RGB;
        return null;
    }
    
    /**
     * Returns the icon corresponding to the current color model.
     * 
     * @param model The color model.
     * @return See above.
     */
    private Icon getColorModelIcon(String model)
    {
        if (model.equals(ImViewer.GREY_SCALE_MODEL))
            return icons.getIcon(IconManager.GRAYSCALE);
        else if (model.equals(ImViewer.RGB_MODEL))
            return icons.getIcon(IconManager.RGB);
        else if (model.equals(ImViewer.HSB_MODEL))
            return icons.getIcon(IconManager.RGB);
        return null;
    }

    /** Initializes the components composing the display. */
    private void initComponents()
    {
    	channelButtons = new HashSet<ChannelButton>();
    	channelButtonsGrid = new HashSet<ChannelButton>();
        ViewerAction[] ratingActions = new ViewerAction[5];
        ratingActions[0] = controller.getAction(ImViewerControl.RATING_ONE);
        ratingActions[1] = controller.getAction(ImViewerControl.RATING_TWO);
        ratingActions[2] = controller.getAction(ImViewerControl.RATING_THREE);
        ratingActions[3] = controller.getAction(ImViewerControl.RATING_FOUR);
        ratingActions[4] = controller.getAction(ImViewerControl.RATING_FIVE);
        ratingBox =  new JComboBox(ratingActions);
        ratingBox.setEnabled(false);

        zSlider = new OneKnobSlider(OneKnobSlider.VERTICAL, 0, 1, 0);
        zSlider.setEnabled(false);
        tSlider = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 0, 1, 0);
        tSlider.setEnabled(false);
        zSliderGrid = new OneKnobSlider(OneKnobSlider.VERTICAL, 0, 1, 0);
        zSliderGrid.setEnabled(false);
        tSliderGrid = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 0, 1, 0);
        tSliderGrid.setEnabled(false);
        zSliderAnnotator = new OneKnobSlider(OneKnobSlider.VERTICAL, 0, 1, 0);
        zSliderAnnotator.setEnabled(false);
        tSliderAnnotator = new OneKnobSlider(OneKnobSlider.HORIZONTAL, 0, 1, 0);
        tSliderAnnotator.setEnabled(false);
        
        IconManager icons = IconManager.getInstance();
        gridRatioSlider = new OneKnobSlider(OneKnobSlider.VERTICAL, 1, 10, 5);
        gridRatioSlider.setEnabled(true);
        gridRatioSlider.setShowArrows(true);
        gridRatioSlider.setToolTipText(RATIO_SLIDER_DESCRIPTION);
        gridRatioSlider.setArrowsImageIcon(
        				icons.getImageIcon(IconManager.RATIO_MAX), 
        				icons.getImageIcon(IconManager.RATIO_MIN));
        ratioSlider = new OneKnobSlider(OneKnobSlider.VERTICAL, 
        					ZoomAction.MIN_ZOOM_INDEX, 
        					ZoomAction.MAX_ZOOM_INDEX, 
        					ZoomAction.DEFAULT_ZOOM_INDEX);
        ratioSlider.setEnabled(true);
        ratioSlider.setShowArrows(true);
        ratioSlider.setToolTipText(ZOOM_SLIDER_DESCRIPTION);
       
        ratioSlider.setArrowsImageIcon(
        		icons.getImageIcon(IconManager.RATIO_MAX), 
        		icons.getImageIcon(IconManager.RATIO_MIN));
        
        
        channelMovieButton = new JButton(
                controller.getAction(ImViewerControl.CHANNEL_MOVIE));
        UIUtilities.unifiedButtonLookAndFeel(channelMovieButton);
        colorModelButton = new JButton();
        UIUtilities.unifiedButtonLookAndFeel(colorModelButton);
        colorModelButton.addActionListener(controller);
        colorModelButtonGrid = new JButton();
        UIUtilities.unifiedButtonLookAndFeel(colorModelButtonGrid);
        colorModelButtonGrid.addActionListener(controller);
        
        ViewerAction a = controller.getAction(ImViewerControl.COLOR_PICKER);
        colorPickerButton = new JButton(a);
        colorPickerButton.addMouseListener((ColorPickerAction) a);
        UIUtilities.unifiedButtonLookAndFeel(colorPickerButton);
        textVisibleButton = new JToggleButton();
        textVisibleButton.setSelected(model.isTextVisible());
        textVisibleButton.setAction(
        		controller.getAction(ImViewerControl.TEXT_VISIBLE));
        playTMovie = new JButton(
        			controller.getAction(ImViewerControl.PLAY_MOVIE_T));
        UIUtilities.unifiedButtonLookAndFeel(playTMovie);
        playTMovieGrid = new JButton(
    			controller.getAction(ImViewerControl.PLAY_MOVIE_T));
        UIUtilities.unifiedButtonLookAndFeel(playTMovieGrid);
        playZMovie = new JButton(
    			controller.getAction(ImViewerControl.PLAY_MOVIE_Z));
	    UIUtilities.unifiedButtonLookAndFeel(playZMovie);
	    playZMovieGrid = new JButton(
				controller.getAction(ImViewerControl.PLAY_MOVIE_Z));
	    UIUtilities.unifiedButtonLookAndFeel(playZMovieGrid);
    }
    
    /**
     * Attaches listener to the passed slider and sets the default values.
     * 
     * @param slider	The slider to handle.
     * @param max		The maximum value.
     * @param v			The default value.
     * @param toolTip	The tooltip text.
     * @param endLabel	The text for the tooltip which is displayed when 
     * 					slider changes value, as well as the label shown at 
     * 					the end of the text. 
     */
    private void initSlider(OneKnobSlider slider, int max, int v, 
    						String toolTip, String endLabel)
    {
    	slider.setVisible(max != 0);
    	slider.setMaximum(max);
    	slider.setValue(v);
    	slider.addChangeListener(this);
        slider.addMouseWheelListener(this);
        slider.setToolTipText(toolTip);
        slider.setEndLabel(endLabel);
        slider.setShowEndLabel(true);
        slider.setShowTipLabel(true);
    }
    
    /**
     * Initializes the value of the components displaying the currently selected
     * z-section and timepoint.
     */
    private void initializeValues()
    {
        switch (model.getRatingLevel()) {
            case ImViewerModel.RATING_ONE:
                ratingBox.setSelectedIndex(0);
                break;
            case ImViewerModel.RATING_TWO:
                ratingBox.setSelectedIndex(1);
                break;
            case ImViewerModel.RATING_THREE:
                ratingBox.setSelectedIndex(2);
                break;
            case ImViewerModel.RATING_FOUR:
                ratingBox.setSelectedIndex(3);
                break;
            case ImViewerModel.RATING_FIVE:
                ratingBox.setSelectedIndex(4);
        }
        int maxZ = model.getMaxZ();
        int maxT = model.getMaxT();
        ratingBox.addActionListener(this);
        initSlider(zSlider, maxZ, model.getDefaultZ(), 
        			Z_SLIDER_DESCRIPTION, Z_SLIDER_TIPSTRING);
        initSlider(zSliderGrid, maxZ, model.getDefaultZ(), 
    			Z_SLIDER_DESCRIPTION, Z_SLIDER_TIPSTRING);
        initSlider(zSliderAnnotator, maxZ, model.getDefaultZ(), 
    			Z_SLIDER_DESCRIPTION, Z_SLIDER_TIPSTRING);
        initSlider(tSlider, maxT, model.getDefaultT(), 
        		T_SLIDER_DESCRIPTION, T_SLIDER_TIPSTRING);
        initSlider(tSliderGrid, maxT, model.getDefaultT(), 
        		T_SLIDER_DESCRIPTION, T_SLIDER_TIPSTRING);
        initSlider(tSliderAnnotator, maxT, model.getDefaultT(), 
        		T_SLIDER_DESCRIPTION, T_SLIDER_TIPSTRING);
        gridRatioSlider.addChangeListener(this);
        ratioSlider.addChangeListener(this);
        
        playTMovie.setVisible(maxT != 0);
        playTMovieGrid.setVisible(maxT != 0);
        playZMovie.setVisible(maxZ != 0);
        playZMovieGrid.setVisible(maxZ != 0);
        colorModelButton.setIcon(getColorModelIcon(model.getColorModel()));
        colorModelButton.setToolTipText(
        				getColorModelDescription(model.getColorModel()));
        colorModelButtonGrid.setIcon(getColorModelIcon(model.getColorModel()));
        colorModelButtonGrid.setToolTipText(
        				getColorModelDescription(model.getColorModel()));
    }
    
    /**
     * Helper method to create a panel hosting the passed slider.
     * 
     * @param slider    The slider to host.
     * @return See above.
     */
    private JPanel layoutSlider(JSlider slider)
    {
        JPanel pane = new JPanel();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(slider);
        return pane;
    }
    
    /**
     * Helper method to create a panel hosting the passed slider.
     * 
     * @return See above.
     */
    private JPanel createZSliderPane()
    {
    	JPanel pane = new JPanel();
    	double[][] tl = {{TableLayout.FILL}, 
				{TableLayout.FILL, TableLayout.PREFERRED}};
    	pane.setLayout(new TableLayout(tl));
    	pane.add(zSlider, "0, 0");
    	pane.add(playZMovie, "0, 1");
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(pane);
        return p;
    }

    /**
     * Helper method to create a panel hosting the passed slider.
     * 
     * @return See above.
     */
    private JPanel createZGridSliderPane()
    {
    	JPanel pane = new JPanel();
    	double[][] tl = {{TableLayout.FILL}, 
				{TableLayout.FILL, TableLayout.PREFERRED}};
    	pane.setLayout(new TableLayout(tl));
    	pane.add(zSliderGrid, "0, 0");
    	pane.add(playZMovieGrid, "0, 1");
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(pane);
        return p;
    }
    
    /** 
     * Builds the tool bar displayed on the left side of the image.
     * 
     * @return See above.
     */
    private JToolBar buildToolBar()
    {
        JToolBar bar = new JToolBar(JToolBar.VERTICAL);
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(colorModelButton);
        bar.add(Box.createRigidArea(VBOX));
        bar.add(channelMovieButton);
        bar.add(Box.createRigidArea(VBOX));
        bar.add(colorPickerButton);
        return bar;
    }
    
    /** 
     * Builds the tool bar displayed on the left side of the  grid view.
     * 
     * @return See above.
     */
    private JToolBar buildGridBar()
    {
    	JToolBar bar = new JToolBar(JToolBar.VERTICAL);
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(colorModelButtonGrid);
        bar.add(Box.createRigidArea(VBOX));
        bar.add(textVisibleButton);
        return bar;
    }
    
    /**
     * Creates a UI component hosting the {@link ChannelButton}s.
     * 
     * @return See above.
     */
    private JPanel createChannelsPane()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        ChannelMetadata[] data = model.getChannelData();
        ChannelButton button;
        ChannelMetadata d;
        p.add(Box.createRigidArea(VBOX));
        boolean gs = model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL);
        for (int k = 0; k < data.length; k++) {
            d = data[k];
            button = new ChannelButton(""+d.getEmissionWavelength(), 
                    model.getChannelColor(k), k, model.isChannelActive(k));
            if (gs) button.setGrayedOut(gs);
            button.addPropertyChangeListener(controller);
            button.setPreferredSize(ChannelButton.DEFAULT_MIN_SIZE);
            channelButtons.add(button);
            p.add(button);
            p.add(Box.createRigidArea(VBOX));
        }
        
        JPanel controls = new JPanel();
        double size[][] = {{TableLayout.PREFERRED}, 
        				{TableLayout.PREFERRED, TableLayout.PREFERRED,
        				TableLayout.PREFERRED, SLIDER_HEIGHT}};
        controls.setLayout(new TableLayout(size));
        controls.add(Box.createVerticalStrut(20), "0, 0");
        controls.add(buildToolBar(), "0, 1, c, c");
        controls.add(p, "0, 2");
        controls.add(ratioSlider, "0, 3, c, c");
        return UIUtilities.buildComponentPanel(controls);
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(createChannelsPane());
        add(createZSliderPane());
    }
    
    /**
     * Sets the value of the specified slider when a propertyChange has been
     * fired.
     * 
     * @param slider The slider to update.
     * @param v The selected value.
     */
    private void updateSlider(JSlider slider, int v)
    {
        slider.removeChangeListener(this);
        slider.setValue(v);
        slider.addChangeListener(this);
    }
  
    /**
     * Creates a new instance.
     * 
     * @param controller    Reference to the Control.
     *                      Mustn't be <code>null</code>.
     * @param model         Reference to the Model.
     *                      Mustn't be <code>null</code>.
     * @param view          Reference to the View.
     *                      Mustn't be <code>null</code>.                    
     */
    ControlPane(ImViewerControl controller, ImViewerModel model,
                ImViewerUI view)
    {
        if (controller == null) throw new NullPointerException("No control.");
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.controller = controller;
        this.model = model;
        this.view = view;
        icons = IconManager.getInstance();
        initComponents();
    }
    
    /** 
     * This method should be called straight after the metadata and the
     * rendering settings are loaded.
     */
    void buildComponent()
    {
        initializeValues();
        buildGUI();
    }
    
    /**
     * Returns the UI component hosting the z-section slider.
     * 
     * @return See above.
     */
    JPanel buildAnnotatorComponent()
    {
    	return layoutSlider(zSliderAnnotator);
    }
    
    /**
     * Builds the control panel displayed in the grid view.
     * 
     * @return See above.
     */
    JPanel buildGridComponent()
    {
    	JPanel p = createZGridSliderPane();
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        ChannelMetadata[] data = model.getChannelData();
        ChannelButton button;
        ChannelMetadata d;
        buttons.add(Box.createRigidArea(VBOX));
        boolean gs = model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL);
        for (int k = 0; k < data.length; k++) {
            d = data[k];
            button = new ChannelButton(""+d.getEmissionWavelength(), 
                    model.getChannelColor(k), k, model.isChannelActive(k));
            if (gs) button.setGrayedOut(gs);
            button.addPropertyChangeListener(controller);
            button.setPreferredSize(ChannelButton.DEFAULT_MIN_SIZE);
            channelButtonsGrid.add(button);
            buttons.add(button);
            buttons.add(Box.createRigidArea(VBOX));
        }
        
        JPanel controls = new JPanel();
        double size[][] = {{TableLayout.PREFERRED}, 
        				{TableLayout.PREFERRED, TableLayout.PREFERRED,
        				TableLayout.PREFERRED, SLIDER_HEIGHT}};
        
        controls.setLayout(new TableLayout(size));
        controls.add(Box.createVerticalStrut(20), "0, 0");
        controls.add(buildGridBar(), "0, 1, c, c");
        controls.add(buttons, "0, 2");
        controls.add(gridRatioSlider, "0, 3, c, c");
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
        content.add(UIUtilities.buildComponentPanel(controls));
        content.add(p);
        return content;
    }
    
    /**
     * Updates UI components when a new timepoint is selected.
     * 
     * @param t The selected timepoint.
     */
    void setTimepoint(int t)
    { 
    	updateSlider(tSlider, t);
    	updateSlider(tSliderGrid, t);
    	updateSlider(tSliderAnnotator, t);
    }
    
    /**
     * Updates UI components when a new z-section is selected.
     * 
     * @param z The selected z-section.
     */
    void setZSection(int z)
    { 
    	updateSlider(zSlider, z); 
    	updateSlider(zSliderGrid, z); 
    	updateSlider(zSliderAnnotator, z);
    }
    
    /**
     * Updates UI components when a rating factor is selected.
     * 
     * @param action    The selected action embedding the rating factor
     *                  information.
     */
    void setRatingFactor(ViewerAction action)
    {
        ratingBox.removeActionListener(this);
        ratingBox.setSelectedItem(action);
        ratingBox.addActionListener(this);
    }
    
    /** Updates UI components when a new color model is selected. */
    void setColorModel()
    {
        boolean gs = (model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL));
        Iterator i = channelButtons.iterator();
        ChannelButton button;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            button.setSelected(
                    model.isChannelActive(button.getChannelIndex()));
            button.setGrayedOut(gs);
        }
        i = channelButtonsGrid.iterator();
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            if (!gs) 
            	button.setSelected(
                   model.isChannelActive(button.getChannelIndex()));
            button.setGrayedOut(gs);
        }
        
        colorModelButton.setIcon(getColorModelIcon(model.getColorModel()));
        colorModelButton.setToolTipText(getColorModelDescription(
				model.getColorModel()));
        colorModelButtonGrid.setIcon(getColorModelIcon(model.getColorModel()));
        colorModelButtonGrid.setToolTipText(getColorModelDescription(
				model.getColorModel()));
    }
    
    /**
     * Sets the selected channels in the grid view.
     * 
     * @param channels Collection of channels to set.
     */
    void setChannelsSelection(List channels)
    {
    	Iterator i = channelButtonsGrid.iterator();
        ChannelButton button;
        int index;
		while (i.hasNext()) {
            button = (ChannelButton) i.next();
            index = button.getChannelIndex();
            button.setSelected(channels.contains(index));
        }
    }
    
    /** 
     * Updates the {@link ChannelButton}s when a new one is selected or 
     * deselected.
     * 
     * @param index One of the following constants {@link ImViewerUI#GRID_ONLY},
	 * 				{@link ImViewerUI#VIEW_ONLY} and 
	 * 				{@link ImViewerUI#GRID_AND_VIEW}.
     */
    void setChannelsSelection(int index)
    {
        Iterator i;
        ChannelButton button;
        switch (index) {
			case ImViewerUI.GRID_ONLY:
				i = channelButtonsGrid.iterator();
				while (i.hasNext()) {
		            button = (ChannelButton) i.next();
		            button.setSelected(
		                    model.isChannelActive(button.getChannelIndex()));
		        }
				break;
			case ImViewerUI.VIEW_ONLY:
				i = channelButtons.iterator();
				while (i.hasNext()) {
		            button = (ChannelButton) i.next();
		            button.setSelected(
		                    model.isChannelActive(button.getChannelIndex()));
		        }
				break;
			case ImViewerUI.GRID_AND_VIEW:
				i = channelButtons.iterator();
				while (i.hasNext()) {
		            button = (ChannelButton) i.next();
		            button.setSelected(
		                    model.isChannelActive(button.getChannelIndex()));
		        }
				i = channelButtonsGrid.iterator();
				while (i.hasNext()) {
		            button = (ChannelButton) i.next();
		            button.setSelected(
		                    model.isChannelActive(button.getChannelIndex()));
		        }
		}
    }
    
    /**
     * Sets the color of selected channel.
     * 
     * @param index The channel index.
     * @param c     The color to set.
     */
    void setChannelColor(int index, Color c)
    {
        Iterator i = channelButtons.iterator();
        ChannelButton button;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            if (index == button.getChannelIndex()) 
                button.setColor(c);
        }
        i = channelButtonsGrid.iterator();
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            if (index == button.getChannelIndex()) 
                button.setColor(c);
        }
    }
    
    /** Resets the default. */
    void resetDefaults()
    {
        boolean gs = (model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL));
        Iterator i = channelButtons.iterator();
        ChannelButton button;
        int index;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            index = button.getChannelIndex();
            button.setSelected(model.isChannelActive(index));
            button.setColor(model.getChannelColor(index)); 
            button.setGrayedOut(gs);
        }
        i = channelButtonsGrid.iterator();
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            index = button.getChannelIndex();
            button.setSelected(model.isChannelActive(index));
            button.setColor(model.getChannelColor(index)); 
            button.setGrayedOut(gs);
        }
        Icon icon = getColorModelIcon(model.getColorModel());
        String tip = getColorModelDescription(model.getColorModel());
        colorModelButton.setIcon(icon);
        colorModelButton.setToolTipText(tip);
        colorModelButtonGrid.setIcon(icon);
        colorModelButtonGrid.setToolTipText(tip);
        setZSection(model.getDefaultZ());
        setTimepoint(model.getDefaultT());
    }
    
    /**
     * Helper method to create a panel hosting the sliders.
     * 
     * @return See above.
     */
    JPanel createGridSliderPanes()
    {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(layoutSlider(zSliderGrid));
        //p.add(createSliderPane(tSliderGrid));
        return p;
    }
    
    /** 
     * Reacts to {@link ImViewer} change events.
     * 
     * @param b Pass <code>true</code> to enable the UI components, 
     *          <code>false</code> otherwise.
     */
    void onStateChange(boolean b)
    {
        //if (model.isPlayingMovie()) enableSliders(!b);
        //else enableSliders(b);
        ratingBox.setEnabled(b);
        Iterator i = channelButtons.iterator();
        while (i.hasNext())
            ((ChannelButton) i.next()).setEnabled(b);
        colorModelButton.setEnabled(b);
    }
    
    /**
     * Builds and returns a UI component hosting the time slider 
     * corresponding to the passed index.
     * 
     * @param index The index used to identify the slider.
     * @return See above.
     */
    JPanel getTimeSliderPane(int index) 
    {
    	switch (index) {
			case ImViewer.ANNOTATOR_INDEX:
				return layoutSlider(tSliderAnnotator);
			case ImViewer.GRID_INDEX:
				JPanel p = new JPanel();
	        	p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
	        	p.add(playTMovieGrid);
	        	p.add(tSliderGrid);
	        	return p;
			case ImViewer.VIEW_INDEX:
			default:
				JPanel pane = new JPanel();
	        	pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
	        	pane.add(playTMovie);
	        	pane.add(tSlider);
	        	return pane;
		}
    }
    
    /**
     * Sets the <code>enable</code> flag of the slider used to select
     * the current z-section and timepoint.
     * 
     * @param b Pass <code>true</code> to enable the sliders,
     * 			<code>false</code> otherwise.
     */
    void enableSliders(boolean b)
    {
    	enableZSliders(b);
    	enableTSliders(b);
	}
    
    /**
     * Sets the <code>enable</code> flag of the slider used to select
     * the current z-section and timepoint.
     * 
     * @param b Pass <code>true</code> to enable the sliders,
     * 			<code>false</code> otherwise.
     */
    void enableZSliders(boolean b)
    {
    	if (b) {
            zSlider.setEnabled(model.getMaxZ() != 0);
            zSliderGrid.setEnabled(model.getMaxZ() != 0);
            zSliderAnnotator.setEnabled(model.getMaxZ() != 0);
        } else {
            zSlider.setEnabled(b);
            zSliderGrid.setEnabled(b);
            zSliderAnnotator.setEnabled(b);
        } 
	}
    
    /**
     * Sets the <code>enable</code> flag of the slider used to select
     * the current z-section and timepoint.
     * 
     * @param b Pass <code>true</code> to enable the sliders,
     * 			<code>false</code> otherwise.
     */
    void enableTSliders(boolean b)
    {
    	if (b) {
            tSlider.setEnabled(model.getMaxT() != 0);
            tSliderGrid.setEnabled(model.getMaxT() != 0);
            tSliderAnnotator.setEnabled(model.getMaxT() != 0);
        } else {
            tSlider.setEnabled(b);
            tSliderGrid.setEnabled(b);
            tSliderAnnotator.setEnabled(b);
        } 
	}
    
    /**
     * Sets the specified channel to active.
     * 
     * @param index   The channel's index.
     * @param uiIndex One of the following constants 
     * 				  {@link ImViewerUI#GRID_ONLY} and 
	 * 				  {@link ImViewerUI#GRID_AND_VIEW}.
     */
    void setChannelActive(int index, int uiIndex)
    {
    	Iterator i;
        ChannelButton button;
        switch (uiIndex) {
			case ImViewerUI.GRID_ONLY:
				 i = channelButtonsGrid.iterator();
			        while (i.hasNext()) {
			            button = (ChannelButton) i.next();
			            if (index == button.getChannelIndex()) 
			                button.setSelected(true);
			        }
				break;
			case ImViewerUI.GRID_AND_VIEW:
				i = channelButtons.iterator();
				while (i.hasNext()) {
					button = (ChannelButton) i.next();
					if (index == button.getChannelIndex()) 
						button.setSelected(true);
				}
				i = channelButtonsGrid.iterator();
				while (i.hasNext()) {
					button = (ChannelButton) i.next();
					if (index == button.getChannelIndex()) 
						button.setSelected(true);
				}
		}       
	}
    
    /**
     * Returns the collection of active channels in the grid view.
     * 
     * @return See above.
     */
    List getActiveChannelsInGrid()
    {
    	List<Integer> active = new ArrayList<Integer>();
    	Iterator i = channelButtonsGrid.iterator();
    	ChannelButton button;
        while (i.hasNext()) {
            button = (ChannelButton) i.next();
            if (button.isSelected()) active.add(button.getChannelIndex());
        }
        return active;
    }
    
    /**
     * Updates UI components when a zooming factor is selected.
     * 
     * @param zoomIndex The index of the selected zoomFactor.
     */
    void setZoomFactor(int zoomIndex)
    {
    	if (ratioSlider.getMinimum() > zoomIndex || 
    		ratioSlider.getMaximum() < zoomIndex)
    		return;
    	ratioSlider.removeChangeListener(this);
    	ratioSlider.setValue(zoomIndex);
    	ratioSlider.addChangeListener(this);
    }
    
    /**
     * Updates UI components when a zooming factor for the grid
     * is selected.
     * 
     * @param zoomIndex The index of the selected zoomFactor.
     */
    void setGridMagnificationFactor(int zoomIndex)
    {
    	if (gridRatioSlider.getMinimum() > zoomIndex || 
    			gridRatioSlider.getMaximum() < zoomIndex)
    		return;
    	gridRatioSlider.removeChangeListener(this);
    	gridRatioSlider.setValue(zoomIndex);
    	gridRatioSlider.addChangeListener(this);
    }
    
    /**
     * Reacts to the selection of an item in the {@link #zoomingBox} or
     * {@link #ratingBox}.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        JComboBox cb = (JComboBox) e.getSource();
        ViewerAction va = (ViewerAction) cb.getSelectedItem();
        va.actionPerformed(e);
    }

    /**
     * Reacts to selection of a new plane.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        Object object = e.getSource();
        if (object instanceof JSlider) {
        	if (object == gridRatioSlider) {
        		double r = (double) gridRatioSlider.getValue()/10;
        		controller.setGridMagnificationFactor(r);
        		return;
        	} else if (object == ratioSlider) {
        		controller.setZoomFactor(ratioSlider.getValue());
        	}
        	if (object == zSlider || object == tSlider)
        		controller.setSelectedXYPlane(zSlider.getValue(), 
                    tSlider.getValue());
        	else if (object == zSliderGrid || object == tSliderGrid)
        		controller.setSelectedXYPlane(zSliderGrid.getValue(), 
                        tSliderGrid.getValue());
        	else if (object == zSliderAnnotator || object == tSliderAnnotator)
        		controller.setSelectedXYPlane(zSliderAnnotator.getValue(), 
        				tSliderAnnotator.getValue());
        }
    }
    
    /**
     * Reacts to wheels moved event related to the {@link #zSlider},
     * {@link #tSlider}, {@link #zSliderGrid} and {@link #zSliderGrid}.
     * @see MouseWheelListener#mouseWheelMoved(MouseWheelEvent)
     */
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        Object source = e.getSource();
        if (source == zSlider && zSlider.isEnabled()) mouseWheelMovedZ(e);
        else if (source == tSlider && tSlider.isEnabled())
            mouseWheelMovedT(e);
        else if (source == zSliderGrid && zSliderGrid.isEnabled()) 
        	mouseWheelMovedZ(e);
        else if (source == tSliderGrid && tSliderGrid.isEnabled())
            mouseWheelMovedT(e);
        else if (source == zSliderAnnotator && zSliderAnnotator.isEnabled()) 
        	mouseWheelMovedZ(e);
        else if (source == tSliderAnnotator && tSliderAnnotator.isEnabled())
            mouseWheelMovedT(e);
    }

}
