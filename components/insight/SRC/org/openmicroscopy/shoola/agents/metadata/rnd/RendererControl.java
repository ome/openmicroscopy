/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.RendererControl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.Action;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.metadata.actions.ContrastStretchingAction;
import org.openmicroscopy.shoola.agents.metadata.actions.HistogramAction;
import org.openmicroscopy.shoola.agents.metadata.actions.ManageRndSettingsAction;
import org.openmicroscopy.shoola.agents.metadata.actions.NoiseReductionAction;
import org.openmicroscopy.shoola.agents.metadata.actions.PlaneSlicingAction;
import org.openmicroscopy.shoola.agents.metadata.actions.ReverseIntensityAction;
import org.openmicroscopy.shoola.agents.metadata.actions.RndAction;
import org.openmicroscopy.shoola.agents.metadata.actions.ViewAction;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewerFactory;
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.colourpicker.ColourPicker;

/** 
 * The Renderer's controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class RendererControl 
	implements PropertyChangeListener
{

    /** Identifies the action to select the bit resolution. */
    static final Integer    BIT_RESOLUTION = Integer.valueOf(0);
    
    /** Identifies the action to select the family. */
    static final Integer    FAMILY = Integer.valueOf(1);
    
    /** Identifies the action to select the coefficient. */
    static final Integer    COEFFICIENT = Integer.valueOf(2);
    
    /** Identifies the action to select the noise reduction algorithm. */
    static final Integer    NOISE_REDUCTION = Integer.valueOf(3);
    
    /** Identifies the action to select the reverse intensity transformation. */
    static final Integer    REVERSE_INTENSITY = Integer.valueOf(4);
    
    /** Identifies the action to select the plane slicing transformation. */
    static final Integer    PLANE_SLICING = Integer.valueOf(5);
    
    /**
     * Identifies the action to select the contrast stretching transformation.
     */
    static final Integer    CONTRAST_STRETCHING = Integer.valueOf(6);
    
    /** Identifies the action to bring up the histogram widget. */
    static final Integer    HISTOGRAM = Integer.valueOf(7);
    
    /** Identifies the action to modify the color model. */
    static final Integer    COLOR_MODEL = Integer.valueOf(8);
    
    /** Identifies the action to set the intensity values to min, max. */
    static final Integer    RND_MIN_MAX = Integer.valueOf(9);
    
    /** Identifies the action to rest the settings. */
    static final Integer    RND_RESET = Integer.valueOf(10);
    
    /** Identifies the action to undo the changes. */
    static final Integer    RND_UNDO = Integer.valueOf(11);
    
    /** Identifies the action to undo the changes. */
    static final Integer    RND_OWNER = Integer.valueOf(12);
    
    /** Identifies the action to view the image. */
    static final Integer    VIEW = Integer.valueOf(13);
    
    /** Identifies the action to apply settings to all. */
    static final Integer    APPLY_TO_ALL = Integer.valueOf(14);
    
    /** Identifies the action to set the intensity values to min, max. */
    static final Integer    RND_ABSOLUTE_MIN_MAX = Integer.valueOf(15);
    
    /** Identifies the action to save the rendering settings. */
    static final Integer    SAVE = Integer.valueOf(16);
    
    /** Identifies the action to redo the changes. */
    static final Integer    RND_REDO = Integer.valueOf(17);
    
    /** Identifies the action to copy the rendering settings. */
    static final Integer COPY = Integer.valueOf(18);
    
    /** Identifies the action to paste the rendering settings. */
    static final Integer PASTE = Integer.valueOf(19);
    
    /**
     * Reference to the {@link Renderer} component, which, in this context,
     * is regarded as the Model.
     */
    private Renderer    			model;
    
    /** Reference to the View. */
    private RendererUI  			view;
    
    /** Maps actions identifier onto actual <code>Action</code> object. */
    private Map<Integer, RndAction>	actionsMap;
    
    /** Index of the channel invoking the color picker. */
	private int         			colorPickerIndex;
	
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(REVERSE_INTENSITY, new ReverseIntensityAction(model));
        actionsMap.put(PLANE_SLICING, new PlaneSlicingAction(model));
        actionsMap.put(CONTRAST_STRETCHING, 
                        new ContrastStretchingAction(model));
        actionsMap.put(NOISE_REDUCTION, new NoiseReductionAction(model));
        actionsMap.put(HISTOGRAM, new HistogramAction(model));
        actionsMap.put(COLOR_MODEL, new ColorModelAction(model));
        actionsMap.put(VIEW, new ViewAction(model));
        actionsMap.put(RND_MIN_MAX, new ManageRndSettingsAction(model, 
        		ManageRndSettingsAction.MIN_MAX));
        actionsMap.put(RND_RESET, new ManageRndSettingsAction(model, 
        		ManageRndSettingsAction.RESET));
        actionsMap.put(APPLY_TO_ALL, new ManageRndSettingsAction(model, 
        		ManageRndSettingsAction.APPLY_TO_ALL));
        actionsMap.put(RND_ABSOLUTE_MIN_MAX, new ManageRndSettingsAction(model, 
        		ManageRndSettingsAction.ABSOLUTE_MIN_MAX));
        actionsMap.put(SAVE, new ManageRndSettingsAction(model, 
        		ManageRndSettingsAction.SAVE));

        ManageRndSettingsAction a = new ManageRndSettingsAction(model, 
                ManageRndSettingsAction.UNDO);
        a.setEnabled(false);
        actionsMap.put(RND_UNDO, a);
        
        a = new ManageRndSettingsAction(model, 
                ManageRndSettingsAction.REDO);
        a.setEnabled(false);
        actionsMap.put(RND_REDO, a);
        
        a = new ManageRndSettingsAction(model, 
                ManageRndSettingsAction.COPY);
        actionsMap.put(COPY, a);
        
        a = new ManageRndSettingsAction(model, 
                ManageRndSettingsAction.PASTE);
        a.setEnabled(false);
        actionsMap.put(PASTE, a);
        
    }
    
    /** 
     * Attaches a window listener to the view and a property listener to the 
     * model.
     */
    private void attachListeners()
    {
        model.addPropertyChangeListener(this);
    }
    
    /**
     * Brings up the color picker with the color associated to the passed
     * channel.
     * 
     * @param channel The index of the selected channel.
     */
    void showColorPicker(int channel)
    {
        showColorPicker(channel, null);
    }
    
    /**
     * Brings up the color picker with the color associated to the passed
     * channel.
     * 
     * @param channel The index of the selected channel.
     * @param location The location where to show the dialog
     */
    void showColorPicker(int channel, Point location)
    {
		colorPickerIndex = channel;
		Color c = view.getChannelColor(channel);
		JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
		ColourPicker dialog = new ColourPicker(f, c);
		dialog.setPreviewVisible(true);
		dialog.addPropertyChangeListener(this);
		if (location == null)
		    UIUtilities.centerAndShow(dialog);
		else
		    UIUtilities.showOnScreen(dialog, location);
    }
    
    /** 
     * Creates a new instance.
     * The {@link #initialize(Renderer, RendererUI) initialize} method should 
     * be called straight after to link this Controller to the other MVC
     * components.
     */
    RendererControl()
    {
        actionsMap = new HashMap<Integer, RndAction>();
    }
    
    /**
     * Links this Controller to its Model and View.
     * 
     * @param model Reference to the {@link Renderer} component, which, in this
     *              context, is regarded as the Model. Mustn't be
     *              <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(Renderer model, RendererUI view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
        createActions();
        attachListeners();
    }
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return actionsMap.get(id); }
    
    /**
     * Registers the specified observer.
     * 
     * @param observer  The observer to register.
     */
    void addPropertyListener(PropertyChangeListener observer)
    {
        model.addPropertyChangeListener(observer);
    }

    /** 
     * Sets the the pixels intensity interval for the
     * currently selected channel.
     * 
     * @param s The lower bound of the interval.
     * @param e	The upper bound of the interval.
     */
    void setInputInterval(double s, double e)
    {
        model.setInputInterval(s, e);
    }
    
    /** 
     * Sets the the pixels intensity interval for the specified channel.
     * 
     * @param s 	  The lower bound of the interval.
     * @param e		  The upper bound of the interval.
     * @param channel The channel to handle.
     */
    void setInputInterval(double s, double e, int channel)
    {
    	model.setChannelWindow(channel, s, e);
    }
    
    /**
     * Checks if the image pixel type is integer
     * @return See above
     */
    boolean isIntegerPixelData() {
        return model.isIntegerPixelData();
    };
    
    /** 
     * Sets the sub-interval of the device space. 
     * 
     * @param s	The lower bound of the interval.
     * @param e The upper bound of the interval.
     */
    void setCodomainInterval(int s, int e) { model.setCodomainInterval(s, e); }

    /**
     * Sets the selected plane.
     * 
     * @param z The selected z-section.
     * @param t The selected timepoint.
     */
	void setSelectedXYPlane(int z, int t)
	{
		setSelectedXYPlane(z, t, -1);
	}
	
	/**
	 * Renders the specified XY-Plane.
	 * 
	 * @param z   The selected z-section.
	 * @param t   The selected timepoint.
	 * @param bin The selected bin, only used for lifetime.
	 */
	void setSelectedXYPlane(int z, int t, int bin)
	{
		model.setSelectedXYPlane(z, t, bin);
	}
	
	/**
	 * Indicates that a channel has been selected using the channel button.
	 * 
	 * @param index The index of the channel.
	 * @param active Pass <code>true</code> to indicate that the channel is
	 * 				 active, <code>false</code> otherwise.
	 */
	void setChannelSelection(int index, boolean active)
	{
		model.setChannelSelection(index, active);
	}

	/**
	 * Sets the family.
	 * 
	 * @param channel The index of the channel.
	 * @param family The family to set.
	 */
	void setChannelFamily(int channel, String family)
	{
	    model.setFamily(channel, family);
	    view.onCurveChange();
	}

    /**
     * Sets the coefficient identifying a curve in the family
     * and updates the image.
     * 
     * @param channel The channel to handle.
     * @param k The new curve coefficient.
     */
	void setCurveCoefficient(int channel, double k)
	{
	    model.setCurveCoefficient(channel, k);
	    view.onCurveChange();
	}
    
    /**
     * Enables/Disables the paste action depending on if 
     * there are rendering settings which can be pasted
     */
	void updatePasteAction() {
	    boolean enabled = MetadataViewerFactory.hasRndSettingsCopied(model.getRefImage().getId());
	    actionsMap.get(PASTE).setEnabled(enabled);
	}
	
    /**
     * Reacts to property change events.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        
        if (RenderingDefinitionHistory.CAN_REDO.equals(name)) {
            boolean value = (Boolean)evt.getNewValue();
            actionsMap.get(RND_REDO).setEnabled(value);
        }

        if (RenderingDefinitionHistory.CAN_UNDO.equals(name)) {
            boolean value = (Boolean)evt.getNewValue();
            actionsMap.get(RND_UNDO).setEnabled(value);
        }
        
        /*
        } else if (name.equals(
            CodomainMapContextDialog.UPDATE_MAP_CONTEXT_PROPERTY)) {
            CodomainMapContext ctx = (CodomainMapContext)  evt.getNewValue();
            model.updateCodomainMap(ctx);
        */
        if (ControlPane.BIT_RESOLUTION_PROPERTY.equals(name)) {
            Integer oldValue = (Integer) evt.getOldValue();
            Integer newValue = (Integer) evt.getNewValue();
            if (newValue.equals(oldValue)) return;
            model.setBitResolution(newValue.intValue());
        } if (ChannelButton.CHANNEL_SELECTED_PROPERTY.equals(name)) {
            Map map = (Map) evt.getNewValue();
			if (map == null) return;
			if (map.size() != 1) return;
			Entry entry = (Entry) map.entrySet().iterator().next();
			Integer index = (Integer) entry.getKey();
			setChannelSelection(index.intValue(), (Boolean) entry.getValue());
        } else if (ChannelButton.CHANNEL_COLOUR_PROPERTY.equals(name)) {
        	showColorPicker(((Integer) evt.getNewValue()).intValue());
        } else if (Renderer.INPUT_INTERVAL_PROPERTY.equals(name)) {
            view.setInputInterval();
        } else if (Renderer.RANGE_INPUT_PROPERTY.equals(name)) {
        	Boolean b = (Boolean) evt.getNewValue();
            view.setInputRange(b.booleanValue());
        } else if (ColourPicker.COLOUR_PROPERTY.equals(name)) { 
			Color c = (Color) evt.getNewValue();
			if (colorPickerIndex != -1) {
				model.setChannelColor(colorPickerIndex, c, false);
			}
        } else if (ColourPicker.COLOUR_PREVIEW_PROPERTY.equals(name)) { 
			Color c = (Color) evt.getNewValue();
			if (colorPickerIndex != -1) {
				model.setChannelColor(colorPickerIndex, c, true);
			}
		} else if (ColourPicker.CANCEL_PROPERTY.equals(name)) {
			model.setChannelColor(colorPickerIndex, null, true);
		} else if (Renderer.Z_SELECTED_PROPERTY.equals(name)) {
			view.setZSection(((Integer) evt.getNewValue()).intValue());
		} else if (Renderer.T_SELECTED_PROPERTY.equals(name)) {
			view.setTimepoint(((Integer) evt.getNewValue()).intValue());
		}   
        
        if(Renderer.SAVE_SETTINGS_PROPERTY.equals(name)) {
            actionsMap.get(SAVE).setEnabled(false);
        } 
        else {
            boolean settingsModified = model.isModified(false);
            actionsMap.get(SAVE).setEnabled(settingsModified && model.canAnnotate());
        }
        
        boolean pasteEnabled = MetadataViewerFactory.hasRndSettingsCopied(model.getRefImage().getId());
        actionsMap.get(PASTE).setEnabled(pasteEnabled);
    }

}
