/*
 * org.openmicroscopy.shoola.env.rnd.RenderingControlImpl
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

package org.openmicroscopy.shoola.env.rnd;

//Java imports
import java.util.Iterator;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainChain;
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainMapContext;
import org.openmicroscopy.shoola.env.rnd.defs.ChannelBindings;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStats;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumStrategy;

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
class RenderingControlImpl
	implements RenderingControl
{

	private Renderer 	renderer;
	
	RenderingControlImpl(Renderer renderer)
	{
		this.renderer = renderer;
	}
	
	/** Implemented as specified by {@link RenderingControl}. */
	public PixelsDimensions getPixelsDims() 
	{
		return renderer.getPixelsDims();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public PixelsStats getPixelsStats()
	{
		return renderer.getPixelsStats();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void setModel(int model) 
	{
		RenderingDef rd = renderer.getRenderingDef();
		rd.setModel(model);
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public int getModel()
	{
		RenderingDef rd = renderer.getRenderingDef();
		return rd.getModel();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public int getDefaultZ() 
	{
		RenderingDef rd = renderer.getRenderingDef();
		return rd.getDefaultZ();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public int getDefaultT() 
	{
		RenderingDef rd = renderer.getRenderingDef();
		return rd.getDefaultT();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void setQuantumStrategy(int family, double coefficient,
												int bitResolution)
	{
		RenderingDef rd = renderer.getRenderingDef();
		QuantumDef qd = rd.getQuantumDef(), newQd;
		newQd = new QuantumDef(family, qd.pixelType, coefficient, 
								qd.cdStart, qd.cdEnd, bitResolution);
		rd.setQuantumDef(newQd);
		renderer.makeQuantumManager();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void setCodomainInterval(int start, int end)
	{
		CodomainChain chain = renderer.getCodomainChain();
		chain.setInterval(start, end);
		RenderingDef rd = renderer.getRenderingDef();
		QuantumDef qd = rd.getQuantumDef(), newQd;
		newQd = new QuantumDef(qd.family, qd.pixelType, qd.curveCoefficient, 
												start, end, qd.bitResolution);
		rd.setQuantumDef(newQd);
		CodomainMapContext mapCtx;
		Iterator i = rd.getCodomainChainDef().iterator();
		while (i.hasNext()) {
			mapCtx = (CodomainMapContext) i.next();
			mapCtx.setCodomain(start, end);
		}
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public QuantumDef getQuantumDef()
	{
		RenderingDef rd = renderer.getRenderingDef();
		return rd.getQuantumDef();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void setChannelWindow(int w, Comparable start, Comparable end) 
	{
		QuantumManager qm = renderer.getQuantumManager();
		QuantumStrategy qs = qm.getStrategyFor(w);
		qs.setWindow(start, end);
		RenderingDef rd = renderer.getRenderingDef();
		ChannelBindings[] cb = rd.getChannelBindings();
		cb[w].setInputWindow(start, end);
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public Comparable getChannelWindowStart(int w) 
	{
		RenderingDef rd = renderer.getRenderingDef();
		ChannelBindings[] cb = rd.getChannelBindings();
		return cb[w].getInputStart();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public Comparable getChannelWindowEnd(int w) 
	{
		RenderingDef rd = renderer.getRenderingDef();
		ChannelBindings[] cb = rd.getChannelBindings();
		return cb[w].getInputEnd();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void setRGBA(int w, int red, int green, int blue, int alpha) 
	{
		RenderingDef rd = renderer.getRenderingDef();
		ChannelBindings[] cb = rd.getChannelBindings();
		cb[w].setRGBA(red, green, blue, alpha);
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public int[] getRGBA(int w) 
	{
		RenderingDef rd = renderer.getRenderingDef();
		ChannelBindings[] cb = rd.getChannelBindings();
		return cb[w].getRGBA();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void setActive(int w, boolean active)
	{
		RenderingDef rd = renderer.getRenderingDef();
		ChannelBindings[] cb = rd.getChannelBindings();
		cb[w].setActive(active);
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public boolean isActive(int w) 
	{
		RenderingDef rd = renderer.getRenderingDef();
		ChannelBindings[] cb = rd.getChannelBindings();
		return cb[w].isActive();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void addCodomainMap(CodomainMapContext mapCtx) 
	{	
		CodomainChain chain = renderer.getCodomainChain();
		chain.add(mapCtx);
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void updateCodomainMap(CodomainMapContext mapCtx)
	{
		CodomainChain chain = renderer.getCodomainChain();
		chain.update(mapCtx);
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void removeCodomainMap(CodomainMapContext mapCtx)
	{
		CodomainChain chain = renderer.getCodomainChain();
		chain.remove(mapCtx);
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void saveCurrentSettings() 
	{
		//TODO: implement when display options in DB are sorted out.	
	}

}
