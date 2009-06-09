/*
 * org.openmicroscopy.shoola.agents.metadata.rnd.RendererComponent 
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
import java.util.Iterator;
import java.util.List;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import pojos.ChannelData;

/** 
 * Implements the {@link RendererComponent} interface to provide the 
 * functionality required of the renderer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class RendererComponent 
	extends AbstractComponent
	implements Renderer
{

    /** The Model sub-component. */
    private RendererModel   model;
    
    /** The Control sub-component. */
    private RendererControl controller;
    
    /** The View sub-component. */
    private RendererUI      view;
	
	/** List of active channels before switching between color mode. */
	private List            historyActiveChannels;

	/**
	 * Notifies the user than an error occured while trying to modify the 
	 * rendering settings and dispose of the viewer 
	 * if the passed exception is a <code>RenderingServiceException</code>
	 * or reloads the rendering engine if it is an 
	 * <code>DSOutOfServiceException</code>.
	 * 
	 * @param e The exception to handle.
	 */
	private void handleException(Throwable e)
	{
		
	}
	
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straigh 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component. Mustn't be <code>null</code>.
     */
    RendererComponent(RendererModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        controller = new RendererControl();
        view = new RendererUI();
    }
    
    /** 
     * Links up the MVC triad. 
     * 
     * @param metadataView The The view of the metadata.
     */
    void initialize()
    {
        model.initialize(this);
        controller.initialize(this, view);
        view.initialize(controller, model);
		setSelectedChannel(-1);
    }
    
    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#discard()
     */
	public void discard()
	{
		 model.discard();
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getGlobalMax()
     */
	public double getGlobalMax() { return model.getGlobalMax(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getGlobalMin()
     */
	public double getGlobalMin() { return model.getGlobalMin(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getHighestValue()
     */
	public double getHighestValue() { return model.getHighestValue(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getLowestValue()
     */
	public double getLowestValue() { return model.getLowestValue(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getState()
     */
	public int getState()
	{
		return model.getState();
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getUI()
     */
	public JComponent getUI() { return view; }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getWindowEnd()
     */
	public double getWindowEnd() { return model.getWindowEnd(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getWindowStart()
     */
	public double getWindowStart() { return model.getWindowStart(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#onStateChange(boolean)
     */
	public void onStateChange(boolean b)
	{
		if (view != null) view.onStateChange(b);
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#resetRndSettings()
     */
	public void resetRndSettings()
	{
		
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setBitResolution(int)
     */
	public void setBitResolution(int v)
	{
        try {
        	model.setBitResolution(v);
        	if (model.isGeneralIndex()) model.saveRndSettings();
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
           		 			Boolean.TRUE);
		} catch (Exception ex) {
			handleException(ex);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setChannelColor(int)
     */
	public void setChannelColor(int index) { view.setChannelColor(index); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setChannelSelection(int, boolean)
     */
	public void setChannelSelection(int index, boolean selected)
	{
		int selectedIndex = index;
		try {
			if (GREY_SCALE_MODEL.equals(model.getColorModel())) {
				if (model.isChannelActive(index)) return;
				boolean c;
				for (int i = 0; i < model.getMaxC(); i++) {
					c = i == index;
					if (c) selectedIndex = index;
					model.setChannelActive(i, c);  
				}
			} else {
				model.setChannelActive(index, selected);
				List<Integer> active = model.getActiveChannels();
				if (!active.contains(index) && active.size() > 0) {
					int oldSelected = model.getSelectedChannel();
					if (active.contains(oldSelected)) 
						selectedIndex = oldSelected;
					else {
						int setIndex = model.createSelectedChannel();
						if (setIndex >= 0) selectedIndex = setIndex;
					}
				}	
			}
				
			model.setSelectedChannel(selectedIndex);
			view.setSelectedChannel();
        	if (model.isGeneralIndex()) model.saveRndSettings();
        	firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
        			Boolean.TRUE);
		} catch (Exception ex) {
			handleException(ex);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setCodomainInterval(int, int)
     */
	public void setCodomainInterval(int s, int e)
	{
		try {
        	model.setCodomainInterval(s, e);
        	if (model.isGeneralIndex()) model.saveRndSettings();
        	firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
        			Boolean.TRUE);
		} catch (Exception ex) {
			handleException(ex);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setColorModelChanged()
     */
	public void setColorModelChanged() { view.setColorModelChanged(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setCurveCoefficient(double)
     */
	public void setCurveCoefficient(double k)
	{
		try {
        	model.setCurveCoefficient(k);
        	if (model.isGeneralIndex()) model.saveRndSettings();
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
           		 			Boolean.TRUE);
		} catch (Exception ex) {
			handleException(ex);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setFamily(String)
     */
	public void setFamily(String family)
	{
		try {
        	model.setFamily(family);
        	if (model.isGeneralIndex()) model.saveRndSettings();
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
           		 			Boolean.TRUE);
		} catch (Exception ex) {
			handleException(ex);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setInputInterval(double, double)
     */
	public void setInputInterval(double s, double e)
	{
		try {
        	model.setInputInterval(s, e);
        	if (model.isGeneralIndex()) model.saveRndSettings();
        	firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
           		 			Boolean.TRUE);
        	firePropertyChange(INPUT_INTERVAL_PROPERTY, Boolean.FALSE, 
                    Boolean.TRUE);
		} catch (Exception ex) {
			handleException(ex);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setNoiseReduction(boolean)
     */
	public void setNoiseReduction(boolean b)
	{
		try {
        	model.setNoiseReduction(b);
        	if (model.isGeneralIndex()) model.saveRndSettings();
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.FALSE, 
           		 			Boolean.TRUE);
		} catch (Exception ex) {
			handleException(ex);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setRenderingControl(RenderingControl)
     */
	public void setRenderingControl(RenderingControl rndControl)
	{
		
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setSelectedChannel(int)
     */
	public void setSelectedChannel(int c)
	{
        List<Integer> active = model.getActiveChannels();
        if (!active.contains(c) && active.size() > 0) {
        	int oldSelected = model.getSelectedChannel();
        	if (active.contains(oldSelected)) c = oldSelected;
        	else {
        		int setIndex = model.createSelectedChannel();
        		if (setIndex >= 0) c = setIndex;
        	}
    	}	
        model.setSelectedChannel(c);
        view.setSelectedChannel();
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setChannelColor(int, Color)
     */
	public void setChannelColor(int index, Color color)
	{
		try {
			model.setChannelColor(index, color);
			setChannelColor(index);
			if (!model.isChannelActive(index)) {
				//setChannelActive(index, true);
				//view.setChannelActive(index, ImViewerUI.ALL_VIEW);
			}
			if (GREY_SCALE_MODEL.equals(model.getColorModel()))
				setColorModel(RGB_MODEL);
			else {
				if (model.isGeneralIndex()) model.saveRndSettings();
				firePropertyChange(RENDER_PLANE_PROPERTY, 
						Boolean.valueOf(false), Boolean.valueOf(true));
			}
				
		} catch (Exception e) {
			handleException(e);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getColorModel()
     */
	public String getColorModel()
	{
		return model.getColorModel();
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setColorModel(String)
     */
	public void setColorModel(String index)
	{
		try {
			List active = model.getActiveChannels();
			Iterator i;
			int j;
			model.setColorModel(index);
			if (GREY_SCALE_MODEL.equals(index)) {
				historyActiveChannels = model.getActiveChannels();
				if (active != null && active.size() >= 1) {
					List<ChannelData> channels = model.getChannelData();
					ChannelData channel;
					i = channels.iterator();
					boolean set = false;
					while (i.hasNext()) {
						channel = (ChannelData) i.next();
						j = channel.getIndex();
						if (active.contains(index)) {
							if (set) 
								model.setChannelActive(j, false);
							else {
								model.setChannelActive(j, true);
								set = true;
							}
						}
					}
				} else if (active == null || active.size() == 0) {
					//no channel so no active channel
					model.setChannelActive(0, true);
				}
				if (active != null) {
					i = active.iterator();
					while (i.hasNext()) {
						j = ((Integer) i.next()).intValue();
					}
				}
			} else {
				if (historyActiveChannels != null && 
						historyActiveChannels.size() > 0) {
					i = historyActiveChannels.iterator();
					while (i.hasNext()) {
						j = ((Integer) i.next()).intValue();
						model.setChannelActive(j, true);
					}
				} else {
					if (active == null || active.size() == 0) {
						//no channel so one will be active.
						model.setChannelActive(0, true);
					} else {
						i = active.iterator();
						while (i.hasNext()) {
							j = ((Integer) i.next()).intValue();
							model.setChannelActive(j, true);
						}
					}
				}
			}
			view.setColorModelChanged();
			if (model.isGeneralIndex()) 
				model.saveRndSettings();
			firePropertyChange(COLOR_MODEL_PROPERTY, Boolean.valueOf(false), 
   		 			Boolean.valueOf(true));
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.valueOf(false), 
           		 			Boolean.valueOf(true));
		} catch (Exception e) {
			handleException(e);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setSelectedXYPlane(int, int)
     */
	public void setSelectedXYPlane(int z, int t)
	{
		int defaultZ = model.getDefaultZ();
		int defaultT = model.getDefaultT();
		if (defaultZ == z && defaultT == t) return;
		try {
			if (defaultZ != z) {
				firePropertyChange(Z_SELECTED_PROPERTY, 
						Integer.valueOf(defaultZ), Integer.valueOf(z));
			}
			if (defaultT != t) {
				firePropertyChange(T_SELECTED_PROPERTY, 
						Integer.valueOf(defaultT), Integer.valueOf(t));
			}
			model.setSelectedXYPlane(z, t);
			if (model.isGeneralIndex()) 
				model.saveRndSettings();
			 firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.valueOf(false), 
    		 			Boolean.valueOf(true));
		} catch (Exception ex) {
			handleException(ex);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setSelectedXYPlane(int, int)
     */
	public void applyToAll()
	{
		firePropertyChange(APPLY_TO_ALL_PROPERTY,  Boolean.valueOf(false), 
    		 			Boolean.valueOf(true));
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#onSettingsApplied()
     */
	public void onSettingsApplied()
	{
		view.onSettingsApplied();
	}

}
