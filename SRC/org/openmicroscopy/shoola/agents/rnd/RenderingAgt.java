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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.metadata.ChannelData;
import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainMapContext;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStats;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStatsEntry;
import org.openmicroscopy.shoola.env.ui.TopFrame;

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
	implements Agent
{
	
	private PixelsStats			pxsStats;
	
	private PixelsDimensions	pxsDims;
	
	private ChannelData[]		channelData;
	
	/** Reference to the registry. */
	private Registry			registry;
	
	/** Reference to the GUI. */
	private RenderingAgtUIF		presentation;
	
	/** Reference to the control component. */
	private RenderingAgtCtrl	control;
	
	private RenderingControl	renderingControl;
	
	//TEMPO
	/** Reference to the topFrame. */
	private TopFrame				topFrame;
	
	/** Creates a new instance. */
	public RenderingAgt() {}
	
	/** Implemented as specified by {@link Agent}. */
	public void activate()
	{
		topFrame.addToDesktop(presentation, TopFrame.PALETTE_LAYER);
		presentation.setVisible(true);
	}

	/** Implemented as specified by {@link Agent}. */
	public void terminate()
	{
	
	}

	/** Implemented as specified by {@link Agent}. */
	public void setContext(Registry ctx)
	{
		registry = ctx;
		control  = new RenderingAgtCtrl(this);
		presentation = new RenderingAgtUIF(control, registry);
		//TODO: to be removed
		topFrame = registry.getTopFrame();
		topFrame.addToMenu(TopFrame.VIEW, presentation.getViewMenuItem());
	}

	/** Implemented as specified by {@link Agent}. */
	public boolean canTerminate()
	{
		return true;
	}

	public RenderingAgtUIF getPresentation()
	{
		return presentation;
	}

	Registry getRegistry()
	{
		return registry;
	}
	
	//TODO: retrieve from DataManager service.
	ChannelData[] getChannelData()
	{
		if (pxsDims == null) renderingControl.getPixelsDims();
		if (channelData == null) channelData = new ChannelData[pxsDims.sizeW];
		for (int i = 0; i < pxsDims.sizeW; i++)
			channelData[i] = new ChannelData(i, i, "Wavelenth "+i);
			
		return channelData;
	}

	/** Return the current rendering model. */
	int getModel()
	{
		return renderingControl.getModel();
	}
	
	/** Set the rendering model. */
	void setModel(int model)
	{
		renderingControl.setModel(model);
	}
	
	/** 
	 * Retrieve the stats of a specified channel across time.
	 * 
	 * @param w		specified wavelength index.
	 */
	PixelsStatsEntry[] getChannelStats(int w)
	{
		if (pxsDims == null) renderingControl.getPixelsDims();
		if (pxsStats ==  null) renderingControl.getPixelsStats();
		PixelsStatsEntry[] channelStats = new PixelsStatsEntry[pxsDims.sizeT];
		for (int t = 0; t < pxsDims.sizeT; t++) 
			channelStats[t] = pxsStats.getEntry(w, t);

		return channelStats;
	}
	
	/** 
	 * Return the lower bound of the codomain interval. 
	 * Value in the range [0, 255].
	 */
	int getCodomainStart()
	{
		return renderingControl.getQuantumDef().cdStart;
	}
	
	/** 
	 * Set the lower bound of the codomain interval.
	 *
	 * @param x		value in the range [0, 255].
	 */
	void setCodomainLowerBound(int x)
	{
		QuantumDef qDef = renderingControl.getQuantumDef();
		renderingControl.setCodomainInterval(x, qDef.cdEnd);
	}
	
	/** 
	 * Return the upper bound of the codomain interval. 
	 * Value in the range [0, 255].
	 */
	int getCodomainEnd()
	{
		return renderingControl.getQuantumDef().cdEnd;
	}
	
	/** 
	 * Set the upper bound of the codomain interval.
	 *
	 * @param x		value in the range [0, 255].
	 */
	void setCodomainUpperBound(int x)
	{
		QuantumDef qDef = renderingControl.getQuantumDef();
		renderingControl.setCodomainInterval(qDef.cdStart, x);
	}
	
	/**
	 * Return the minimum pixel intensities of a specified channel
	 * across time.
	 * @param w		specified wavelength index.
	 * @return
	 */
	double getGlobalChannelWindowStart(int w)
	{
		if (pxsStats == null) renderingControl.getPixelsStats();
		return pxsStats.getGlobalEntry(w).globalMin;
	}
	
	/**
	 * Return the maximum pixel intensities of a specified channel
	 * across time.
	 * @param w		specified wavelength index.
	 * @return
	 */
	double getGlobalChannelWindowEnd(int w)
	{
		if (pxsStats == null) renderingControl.getPixelsStats();
		return pxsStats.getGlobalEntry(w).globalMax;
	}
	
	/**
	 * Get the lower bound of the pixel intensity interval 
	 * of a specified channel.
	 * 
	 * @param w		specified wavelength index.
	 */
	Comparable getChannelWindowStart(int w)
	{
		return renderingControl.getChannelWindowStart(w);
	}
	
	/** 
	 * Set the lower bound of the input interval of a specified channel. 
	 * 
	 * @param w		specified wavelength index.
	 * @param x		lower bound.
	 */
	void setChannelWindowStart(int w, Comparable x)
	{
		Comparable end = renderingControl.getChannelWindowEnd(w);
		renderingControl.setChannelWindow(w, x, end);
	}
	
	/**
	 * Set the upper bound of the pixel intensity interval 
	 * of a specified channel.
	 * 
	 * @param w		specified wavelength index.
	 */
	Comparable getChannelWindowEnd(int w)
	{
		return renderingControl.getChannelWindowEnd(w);
	}
	
	/** 
	 * Set the upper bound of the input interval of a specified channel. 
	 * 
	 * @param w		specified wavelength index.
	 * @param x		upper bound.
	 */
	void setChannelWindowEnd(int w, Comparable x)
	{
		Comparable start = renderingControl.getChannelWindowStart(w);
		renderingControl.setChannelWindow(w, start, x);
	}
	
	/** Return the strategy definition object. */
	QuantumDef getQuantumDef()
	{
		return renderingControl.getQuantumDef();
	}
	
	/**
	 * Set a strategy.
	 * 
	 * @param k				curveCoefficient.
	 * @param family		constant that identifies a transformation.
	 * @param resolution	bit-resolution.
	 */
	void setQuantumStrategy(int k, int family, int resolution)
	{
		renderingControl.setQuantumStrategy(family, k, resolution);
	}
	
	/** Add the codomainMap context. */
	void addCodomainMap(CodomainMapContext ctx)
	{
		renderingControl.addCodomainMap(ctx);
	}
	
	/** Remove the codomainMap context. */
	void removeCodomainMap(CodomainMapContext ctx)
	{
		renderingControl.removeCodomainMap(ctx);
	}
	
	/** Update the codomain map context. */
	void updateCodomainMap(CodomainMapContext ctx)
	{
		renderingControl.updateCodomainMap(ctx);
	}
	
	/** 
	 * Retrieve the color's component of a specified channel
	 * 
	 * @param w		specified wavlength index.
	 * @return	array of values in the range 0-255.
	 */
	int[] getRGBA(int w)
	{
		return renderingControl.getRGBA(w);
	}
	
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
	}
	
	/**
	 * Map or not a specified channel.
	 * 
	 * @param w			specified wavelength index.
	 * @param active	<code>true</code> if the wavelength has to be mapped
	 * 					<code>false</code> otherwise.
	 */
	void setActive(int w, boolean active)
	{
		renderingControl.setActive(w, active);
	}

	boolean isActive(int w)
	{
		return renderingControl.isActive(w);
	}
	
}
