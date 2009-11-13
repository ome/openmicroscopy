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
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;

//Third-party libraries
import com.sun.opengl.util.texture.TextureData;

//Application-internal dependencies
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.log.Logger;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.ChannelData;
import pojos.PixelsData;

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

    /** The default error message. */
    private static final String ERROR = " An error occured while modifying  " +
    		"the rendering settings.";
    
    /** The Model sub-component. */
    private RendererModel   model;
    
    /** The Control sub-component. */
    private RendererControl controller;
    
    /** The View sub-component. */
    private RendererUI      view;
	
	/** List of active channels before switching between color mode. */
	private List            historyActiveChannels;

	/**
	 * Notifies the user than an error occurred while trying to modify the 
	 * rendering settings and dispose of the viewer 
	 * if the passed exception is a <code>RenderingServiceException</code>
	 * or reloads the rendering engine if it is an 
	 * <code>DSOutOfServiceException</code>.
	 * 
	 * @param e The exception to handle.
	 */
	private void handleException(Throwable e)
	{
		Logger logger = MetadataViewerAgent.getRegistry().getLogger();
		UserNotifier un = MetadataViewerAgent.getRegistry().getUserNotifier();
		if (e instanceof RenderingServiceException) {
			RenderingServiceException rse = (RenderingServiceException) e;
			LogMessage logMsg = new LogMessage();
			logMsg.print("Rendering Exception:");
			logMsg.println(rse.getExtendedMessage());
			logMsg.print(rse);
			logger.error(this, logMsg);
			if (e.getCause() instanceof OutOfMemoryError) {
				un.notifyInfo("Image", "Due to an out of Memory error, " +
						"\nit is not possible to render the image.");
			} else {
				JFrame f = 
					MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
				MessageBox box = new MessageBox(f, "Rendering Error", "An " +
						"error occurred while modifying the settings.\nDo you " +
						"want to reload the settings?");
				if (box.centerMsgBox() == MessageBox.YES_OPTION) {
					firePropertyChange(RELOAD_PROPERTY, Boolean.valueOf(false), 
							Boolean.valueOf(true));
				} else {
					firePropertyChange(RELOAD_PROPERTY, Boolean.valueOf(true), 
							Boolean.valueOf(false));
				}
				
				
				/*
				un.notifyError(ImViewerAgent.ERROR, logMsg.toString(), 
						e.getCause());
						*/
			}
			//model.discard();
			//fireStateChange();
		} else if (e instanceof DSOutOfServiceException) {
			logger.debug(this, "Reload rendering Engine.");
			un.notifyError(ERROR, "Out of service.", e.getCause());
			model.discard();
			fireStateChange();
		}
		return;
	}
	
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight
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
     * @see Renderer#setBitResolution(int)
     */
	public void setBitResolution(int v)
	{
        try {
        	model.setBitResolution(v);
        	if (model.isGeneralIndex()) model.saveRndSettings();
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.valueOf(false), 
            		Boolean.valueOf(true));
		} catch (Exception ex) {
			handleException(ex);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setChannelSelection(int, boolean)
     */
	public void setChannelSelection(int index, boolean selected)
	{
		int selectedIndex = index;
		boolean render = true;
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
				if (!selected && model.isChannelActive(index) &&
						model.getSelectedChannel() != index) {
					selectedIndex = index;
					render = false;
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
			}
				
			model.setSelectedChannel(selectedIndex);
			view.setSelectedChannel();
        	if (model.isGeneralIndex()) model.saveRndSettings();
        	if (render)
        		firePropertyChange(RENDER_PLANE_PROPERTY, 
        				Boolean.valueOf(false), Boolean.valueOf(true));
        	firePropertyChange(SELECTED_CHANNEL_PROPERTY, -1, selectedIndex);
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
        	firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.valueOf(false), 
            		Boolean.valueOf(true));
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
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.valueOf(false), 
            		Boolean.valueOf(true));
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
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.valueOf(false), 
            		Boolean.valueOf(true));
		} catch (Exception ex) {
			handleException(ex);
		}
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setInputInterval(double, double)
     */
	public void setInputInterval(double start, double end)
	{
		setChannelWindow(model.getSelectedChannel(), start, end);
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
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.valueOf(false), 
            		Boolean.valueOf(true));
		} catch (Exception ex) {
			handleException(ex);
		}
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
			view.setChannelColor(index);
			firePropertyChange(CHANNEL_COLOR_PROPERTY, -1, index);
			if (GREY_SCALE_MODEL.equals(model.getColorModel()))
				setColorModel(RGB_MODEL, true);
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
     * @see Renderer#setColorModel(String, boolean)
     */
	public void setColorModel(String index, boolean update)
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
						if (active.contains(j)) {
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
			if (update)
				firePropertyChange(RENDER_PLANE_PROPERTY,
						Boolean.valueOf(false), Boolean.valueOf(true));
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
			model.setSelectedXYPlane(z, t);
			if (defaultZ != z) {
				firePropertyChange(Z_SELECTED_PROPERTY, 
						Integer.valueOf(defaultZ), Integer.valueOf(z));
			}
			if (defaultT != t) {
				firePropertyChange(T_SELECTED_PROPERTY, 
						Integer.valueOf(defaultT), Integer.valueOf(t));
			}
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
     * @see Renderer#applyToAll()
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
	public void onSettingsApplied(RenderingControl rndControl)
	{ 
		if (rndControl == null) return;
		model.setRenderingControl(rndControl);
		view.onSettingsApplied();
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getDefaultT()
     */
	public int getDefaultT() { return model.getDefaultT(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getDefaultZ()
     */
	public int getDefaultZ() { return model.getDefaultZ(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getPixelsDimensionsT()
     */
	public int getPixelsDimensionsT() { return model.getMaxT(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getPixelsDimensionsX()
     */
	public int getPixelsDimensionsX() { return model.getMaxX(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getPixelsDimensionsY()
     */
	public int getPixelsDimensionsY() { return model.getMaxY(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getPixelsDimensionsZ()
     */
	public int getPixelsDimensionsZ() { return model.getMaxZ(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getChannelData()
     */
	public List<ChannelData> getChannelData()
	{
		return model.getChannelData();
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getActiveChannels()
     */
	public List<Integer> getActiveChannels()
	{
		return model.getActiveChannels();
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getChannelColor(int)
     */
	public Color getChannelColor(int index)
	{
		return model.getChannelColor(index);
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getCompressionLevel()
     */
	public int getCompressionLevel() { return model.getCompressionLevel(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getPixelsDimensionsC()
     */
	public int getPixelsDimensionsC() { return model.getMaxC(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getPixelsSizeX()
     */
	public double getPixelsSizeX() { return model.getPixelsSizeX(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getPixelsSizeY()
     */
	public double getPixelsSizeY() { return model.getPixelsSizeY(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getPixelsSizeZ()
     */
	public double getPixelsSizeZ() { return model.getPixelsSizeZ(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#getRndSettingsCopy()
     */
	public RndProxyDef getRndSettingsCopy()
	{
		return model.getRndSettingsCopy();
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#hasRGB()
     */
	public boolean[] hasRGB()
	{
		boolean[] rgb = new boolean[3];
		rgb[0] = model.hasActiveChannel(0);
		rgb[1] = model.hasActiveChannel(1);
		rgb[2] = model.hasActiveChannel(2);
		return rgb;
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#isChannelActive(int)
     */
	public boolean isChannelActive(int index)
	{
		return model.isChannelActive(index);
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#isColorComponent(int, int)
     */
	public boolean isColorComponent(int band, int index)
	{
		return model.isColorComponent(band, index);
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#isCompressed()
     */
	public boolean isCompressed() { return model.isCompressed(); }

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#isSameSettings(RndProxyDef, boolean)
     */
	public boolean isSameSettings(RndProxyDef def, boolean checkPlane)
	{
		return model.isSameSettings(def, checkPlane);
	}

    /** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#resetSettings()
     */
	public void resetSettings()
	{
		try {
			model.resetDefaults();
			view.resetDefaultRndSettings();
		} catch (Throwable e) {
			handleException(e);
		}
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#resetSettings(RndProxyDef)
     */
	public void resetSettings(RndProxyDef settings)
	{
		try {
			model.resetSettings(settings);
		} catch (Throwable e) {
			handleException(e);
		}
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#saveCurrentSettings()
     */
	public RndProxyDef saveCurrentSettings()
		throws RenderingServiceException, DSOutOfServiceException
	{
		return model.saveCurrentSettings();
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setActive(int, boolean)
     */
	public void setActive(int index, boolean active)
	{
		try {
			model.setActive(index, active);
		} catch (Throwable e) {
			handleException(e);
		}
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setChannelWindow(int, double, double)
     */
	public void setChannelWindow(int index, double start, double end)
	{
		try {
			double s = model.getWindowStart(index);
			double e = model.getWindowEnd(index);
			if (start == s && end == e) return;
			model.setInputInterval(index, start, end);
        	if (model.isGeneralIndex()) model.saveRndSettings();
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.valueOf(false), 
            		Boolean.valueOf(true));
        	firePropertyChange(INPUT_INTERVAL_PROPERTY, Boolean.valueOf(false), 
            		Boolean.valueOf(true));
		} catch (Throwable e) {
			handleException(e);
		}
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setCompression(int)
     */
	public void setCompression(int compression)
	{
		model.setCompression(compression);
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setOriginalRndSettings()
     */
	public void setOriginalRndSettings()
	{
		try {
			model.setOriginalRndSettings();
			view.resetDefaultRndSettings();
		} catch (Throwable e) {
			handleException(e);
		}
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#validatePixels(PixelsData)
     */
	public boolean validatePixels(PixelsData pixels)
	{
		if (pixels == null) return false;
		return model.validatePixels(pixels);
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#renderPlane(PlaneDef)
     */
	public BufferedImage renderPlane(PlaneDef pDef)
	{
		if (pDef == null) return null;
		try {
			return model.renderPlane(pDef);
		} catch (Throwable e) {
			handleException(e);
		}
		return null;
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#renderPlaneAsTexture(PlaneDef)
     */
	public TextureData renderPlaneAsTexture(PlaneDef pDef)
	{
		if (pDef == null) return null;
		try {
			return model.renderPlaneAsTexture(pDef);
		} catch (Throwable e) {
			handleException(e);
		}
		return null;
	}
	
	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#reloadUI(boolean)
     */
	public void reloadUI(boolean reload)
	{
		view.resetDefaultRndSettings();
		firePropertyChange(COLOR_MODEL_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
		if (reload) {
			
		}
	}
	
	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setRangeAllChannels()
     */
	public void setRangeAllChannels()
	{
		try {
			double min, max;
			for (int i = 0; i < model.getMaxC(); i++) {
				min = model.getGlobalMin(i);
				max = model.getGlobalMax(i);
				model.setInputInterval(i, min, max);
			}
			if (model.isGeneralIndex()) model.saveRndSettings();
            firePropertyChange(RENDER_PLANE_PROPERTY, Boolean.valueOf(false), 
            		Boolean.valueOf(true));
        	firePropertyChange(INPUT_INTERVAL_PROPERTY, Boolean.valueOf(false), 
            		Boolean.valueOf(true));

		} catch (Throwable e) {
			handleException(e);
		}
	}

	/** 
     * Implemented as specified by the {@link Renderer} interface.
     * @see Renderer#setOverlays(long, Map)
     */
	public void setOverlays(long tableID, Map<Long, Integer> overlays)
	{
		try {
			model.setOverlays(tableID, overlays);
		} catch (Exception e) {
			handleException(e);
		}
	}

}
