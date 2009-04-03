/*
 * org.openmicroscopy.shoola.agents.rnd.RenderingAgt
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

package org.openmicroscopy.shoola.agents.rnd;

//Java imports
import java.awt.Color;
import java.awt.Dimension;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.events.DisplayRendering;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.events.ServiceActivationRequest;
import org.openmicroscopy.shoola.env.data.model.ChannelData;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainMapContext;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.events.ImageLoaded;
import org.openmicroscopy.shoola.env.rnd.events.LoadImage;
import org.openmicroscopy.shoola.env.rnd.events.RenderImage;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStats;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStatsEntry;

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
public class RenderingAgt
	implements Agent, AgentEventListener
{
	
	public static final Dimension	HBOX = new Dimension(10, 0), 
									VBOX = new Dimension(0, 10);
	
	/** horizontal space between the cells in the grid. */
	public static final int			H_SPACE = 10;
	
	/** Dimension of the colored button, same size as the icon. */
	public static final Dimension	COLORBUTTON_DIM = new Dimension(16, 16);
	
	public static final Color		COLORBUTTON_BORDER = Color.BLACK;
	
	private PixelsStats				pxsStats;
	
	private PixelsDimensions		pxsDims;
	
	private ChannelData[]			channelData;
	
	/** Reference to the registry. */
	private Registry				registry;
	
	/** Reference to the GUI. */
	private RenderingAgtUIF			presentation;
	
	/** Reference to the control component. */
	private RenderingAgtCtrl		control;
	
	private RenderingControl		renderingControl;
	
	/** Current image displayed: imageID and set of pixelsID. */
	private int						curImageID, curPixelsID;
	
	/** Allow or not to update the channel info. */ 
	private boolean					canUpdate;
	
	/** Creates a new instance. */
	public RenderingAgt() {}
	
	/** Implemented as specified by {@link Agent}. */
	public void activate() {}

	/** Implemented as specified by {@link Agent}. */
	public void terminate() {}

	/** Implemented as specified by {@link Agent}. */
	public void setContext(Registry ctx)
	{
		registry = ctx;
		canUpdate = true;
		EventBus bus = registry.getEventBus();
		bus.register(this, ImageLoaded.class);
		bus.register(this, DisplayRendering.class);
	}

	/** Implemented as specified by {@link Agent}. */
	public boolean canTerminate() { return true; }
	
	/** Implement as specified by {@link AgentEventListener}. */
	public void eventFired(AgentEvent e) 
	{
		if (e instanceof ImageLoaded) handleImageLoaded((ImageLoaded) e);	
		else if (e instanceof DisplayRendering) presentation.deIconify();
	}
	
	/** Render a new image when a control has been activated. */
	private void refreshImage()
	{
		RenderImage event = new RenderImage(curPixelsID);
		registry.getEventBus().post(event);	
	}
	
	/** Handle the event @see ImageLoaded. */
	private void handleImageLoaded(ImageLoaded response)
	{
		LoadImage request = (LoadImage) response.getACT();
		renderingControl = response.getProxy();
		pxsDims = renderingControl.getPixelsDims();
		pxsStats = renderingControl.getPixelsStats();
		curImageID = request.getImageID();
		curPixelsID = request.getPixelsID();
		initChannelData();
		if (presentation != null) removePresentation();
		buildPresentation(request.getImageName());
	}
	
	/** Build the presentation. */
	private void buildPresentation(String imageName)
	{
		control  = new RenderingAgtCtrl(this);
		presentation = new RenderingAgtUIF(control, registry, imageName);
		control.setPresentation(presentation);
	}
	
	/** 
	 * Remove and rebuild the presentation. 
	 * The method is invoked when a new image is loaded and the presentation
	 * is displayed.
	 *
	 */
	private void removePresentation()
	{
		control.disposeDialogs();
		presentation.dispose();
		control = null;
		presentation = null;
	}
	
	/** Return the {@link RenderingAgtUIF presentation}. */
	public RenderingAgtUIF getPresentation() { return presentation; }

	/** Return a refence to the {@link Registry}. */
	Registry getRegistry() { return registry; }

	/** 
	 * The method is called when we can't retrieve the data from DB. 
	 * In this case, the user can't update the channel data.
	 */
	private void defaultInitChannelData()
	{
		for (int i = 0; i < pxsDims.sizeW; i++)
			channelData[i] = new ChannelData(i, i, i, "Wavelenth "+i,  i, null);
	  	canUpdate = false;
	}
	
	/** Initializes the channel data. */
	void initChannelData()
	{
		try {
			DataManagementService ds = registry.getDataManagementService();
			channelData = ds.getChannelData(curImageID); 
            if (channelData == null) defaultInitChannelData();
			else {
                if (channelData.length != pxsDims.sizeW) 
                    defaultInitChannelData();
            }
                
		} catch(DSAccessException dsae) {
			String s = "Can't retrieve the channel data for "+curImageID+".";
			registry.getLogger().error(this, s+" Error: "+dsae); 
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s,
				 									dsae);
			defaultInitChannelData();
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
									ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
            defaultInitChannelData();
		} 
	}

	/** Update the channel. */
	void updateChannelData(ChannelData cd)
	{
		try {
			if (canUpdate) {
				DataManagementService ds = registry.getDataManagementService();
				ds.updateChannelData(cd);
				channelData[cd.getIndex()] = cd;
			} else {
				String msg = "The channel data can't be updated b/c of a data" +
						" retrieval failure at initialization time.";
				registry.getUserNotifier().notifyInfo("Update channel", msg);
			}
		}  catch(DSAccessException dsae) {
			String s = "Can't update the channel data.";
			registry.getLogger().error(this, s+" Error: "+dsae); 
			registry.getUserNotifier().notifyError("Data Retrieval Failure", s, 
													dsae);
		} catch(DSOutOfServiceException dsose) {	
			ServiceActivationRequest request = new ServiceActivationRequest(
								ServiceActivationRequest.DATA_SERVICES);
			registry.getEventBus().post(request);
		} 
	}
	
	/** Return the channelData array of the currentImage. */
	ChannelData[] getChannelData() { return channelData; }
	
	/** 
	 * Return the channelData of the specified wavelength.
	 * 
	 * @param w		OME index of the wavelength.
	 */
	ChannelData getChannelData(int w) { return channelData[w]; }
	
	/** Return the current rendering model. */
	int getModel() { return renderingControl.getModel(); }
	
	/** Set the rendering model. */
	void setModel(int model)
	{
		renderingControl.setModel(model);
		refreshImage();
	}
	
	/** 
	 * Retrieve the stats of a specified channel across time.
	 * 
	 * @param w		OME index of the specified wavelength.
	 */
	PixelsStatsEntry[] getChannelStats(int w)
	{
		PixelsStatsEntry[] channelStats = new PixelsStatsEntry[pxsDims.sizeT];
		for (int t = 0; t < pxsDims.sizeT; t++) 
			channelStats[t] = pxsStats.getEntry(w, t);

		return channelStats;
	}
	
	/** 
	 * Return the lower bound of the codomain interval. 
	 * Value in the range [0, 255].
	 */
	int getCodomainStart() { return renderingControl.getQuantumDef().cdStart; }
	
	/** 
	 * Set the lower bound of the codomain interval.
	 *
	 * @param x		value in the range [0, 255].
	 */
	void setCodomainLowerBound(int x)
	{
		QuantumDef qDef = renderingControl.getQuantumDef();
		renderingControl.setCodomainInterval(x, qDef.cdEnd);
		refreshImage();
	}
	
	/** 
	 * Return the upper bound of the codomain interval. 
	 * Value in the range [0, 255].
	 */
	int getCodomainEnd() { return renderingControl.getQuantumDef().cdEnd; }
	
	/** 
	 * Set the upper bound of the codomain interval.
	 *
	 * @param x		value in the range [0, 255].
	 */
	void setCodomainUpperBound(int x)
	{
		QuantumDef qDef = renderingControl.getQuantumDef();
		renderingControl.setCodomainInterval(qDef.cdStart, x);
		refreshImage();
	}
	
	/**
	 * Return the minimum pixel intensities of a specified channel
	 * across time.
	 * @param w		OME index of the specified wavelength.
	 * @return
	 */
	double getGlobalChannelWindowStart(int w)
	{
		return pxsStats.getGlobalEntry(w).globalMin;
	}
	
	/**
	 * Return the maximum pixel intensities of a specified channel
	 * across time.
	 * @param w		OME index of the specified wavlength.
	 * @return
	 */
	double getGlobalChannelWindowEnd(int w)
	{
		return pxsStats.getGlobalEntry(w).globalMax;
	}
	
	/**
	 * Get the lower bound of the pixel intensity interval 
	 * of a specified channel.
	 * 
	 * @param w		OME index of the specified wavlength.
	 */
	double getChannelWindowStart(int w)
	{
		return renderingControl.getChannelWindowStart(w);
	}
	
	/** 
	 * Set the lower bound of the input interval of a specified channel. 
	 * 
	 * @param w		OME index of the specified wavlength.
	 * @param x		lower bound.
	 */
	void setChannelWindowStart(int w, double x)
	{
		double end = renderingControl.getChannelWindowEnd(w);
		renderingControl.setChannelWindow(w, x, end);
		refreshImage();
	}
	
	/**
	 * Set the upper bound of the pixel intensity interval 
	 * of a specified channel.
	 * 
	 * @param w		OME index of the specified wavlength.
	 */
	double getChannelWindowEnd(int w)
	{
		return renderingControl.getChannelWindowEnd(w);
	}
	
	/** 
	 * Set the upper bound of the input interval of a specified channel. 
	 * 
	 * @param w		OME index of the specified wavlength.
	 * @param x		upper bound.
	 */
	void setChannelWindowEnd(int w, double x)
	{
		double start = renderingControl.getChannelWindowStart(w);
		renderingControl.setChannelWindow(w, start, x);
		refreshImage();
	}
	
	/** Return the strategy definition object. */
	QuantumDef getQuantumDef() { return renderingControl.getQuantumDef(); }
	
	/**
	 * Set a strategy.
	 * 
	 * @param k				curveCoefficient.
	 * @param family		constant that identifies a transformation.
	 * @param resolution	bit-resolution.
	 */
	void setQuantumStrategy(double k, int family, int resolution)
	{
		renderingControl.setQuantumStrategy(family, k, resolution);
		refreshImage();
	}
	
	/** Add the codomainMap context. */
	void addCodomainMap(CodomainMapContext ctx)
	{
		renderingControl.addCodomainMap(ctx);
		refreshImage();
	}
	
	/** Remove the codomainMap context. */
	void removeCodomainMap(CodomainMapContext ctx)
	{
		renderingControl.removeCodomainMap(ctx);
		refreshImage();
	}
	
	/** Update the codomain map context. */
	void updateCodomainMap(CodomainMapContext ctx)
	{
		renderingControl.updateCodomainMap(ctx);
		refreshImage();
	}
	
	/** 
	 * Retrieve the color's component of a specified channel
	 * 
	 * @param w		OME index of the specified wavlength.
	 * @return	array of values in the range 0-255.
	 */
	int[] getRGBA(int w) { return renderingControl.getRGBA(w); }
	
	/**
	 * Set the color's component of a specified channel.
	 * 
	 * @param w			specified wavelength index.
	 * @param red		red component, value in the range 0-255.
	 * @param green		green component, value in the range 0-255.
	 * @param blue		blue component, value in the range 0-255.
	 * @param alpha		alpha component, value in the range 0-255.
	 */
	void setRGBA(int w, int red, int green, int blue, int alpha)
	{
		renderingControl.setRGBA(w, red, green, blue, alpha);
		refreshImage();
	}
	
	/**
	 * Map or not a specified channel.
	 * 
	 * @param w			OME index of the specified wavlength.
	 * @param active	<code>true</code> if the wavelength has to be mapped
	 * 					<code>false</code> otherwise.
	 */
	void setActive(int w, boolean active)
	{
		renderingControl.setActive(w, active);
		refreshImage();
	}

	/**
	 * Map the specified channel and set the active to false for the other 
	 * channels.
	 * @param OME index of the specified wavlength.
	 */
	void setActive(int w)
	{
		for (int i = 0; i < pxsDims.sizeW; i++)
			renderingControl.setActive(i, i == w); 
		refreshImage();
	}
	
	boolean isActive(int w) { return renderingControl.isActive(w); }

	/** Reset the rendering engine defaults. */
	void resetDefaults()
	{
		renderingControl.resetDefaults();
		refreshImage();
	}
	
	/** Save the rendering settings. */
	void saveDisplayOptions()
	{
		renderingControl.saveCurrentSettings();
		String msg = "The settings have now been saved, Note that the " +
			"parameters set in \"options\" haven't been saved.";
		registry.getUserNotifier().notifyInfo("Rendering", msg);
	}
	
}
