/*
 * org.openmicroscopy.shoola.env.rnd.defs.RenderingDef
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

package org.openmicroscopy.shoola.env.rnd.defs;


//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainMapContext;

/** 
 * Aggregates all information needed to render an image.
 * Define constants that dictate how quantized data is mapped into a 
 * color space. 
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
public class RenderingDef
{

	/** GreyScale model. */
	public static final int 	GS = 0;
	
	/** RGB model. */
	public static final int 	RGB = 1;
	
	/** HSB model. */
	public static final int 	HSB = 2;
	
	/** The XY-plane to display when the image is open. */
	private int		 			defaultZ;
	
	/** The timepoint to display when the image is open. */
	private int		 			defaultT;
	
	/** One the constants defined above. */
	private int					model;
	
	private ChannelBindings[]	channelBindings;
	
	private List				cdChainDef;
	
	private QuantumDef			qDef;


	private void setChannelBindings(ChannelBindings[] cb)
	{
		if (cb == null || cb.length == 0)
			throw new IllegalArgumentException("No channel bindings.");
		channelBindings = new ChannelBindings[cb.length];
		for (int i = 0; i < cb.length; ++i) {
			if (cb[i] == null)
				throw new IllegalArgumentException(
											"No binding for wavelength: "+i);
			if (cb[i].getIndex() != i)
				throw new IllegalArgumentException(
				"The wavelength index doesn't match the element position: "+i);
			channelBindings[i] = cb[i];
		}
	}

	//channelBindings must be such that channelBindings[i].getIndex() == i
	public RenderingDef(int defaultZ, int defaultT, int model, QuantumDef qDef,
						ChannelBindings[] channelBindings)
	{
		this.defaultZ = defaultZ;
		this.defaultT = defaultT;
		setModel(model);
		setQuantumDef(qDef);
		setChannelBindings(channelBindings);
		cdChainDef = new ArrayList();
	}
	
	/** empty constructor.*/
	private RenderingDef() {}
	
	/** only one codomain transformation of the same type. */
	public void addCodomainMapCtx(CodomainMapContext mapCtx)
	{
		if (mapCtx == null)	throw new NullPointerException("No context.");
		if (cdChainDef.contains(mapCtx))  //Recall equals() is overridden.
			throw new IllegalArgumentException("Context already defined.");
		cdChainDef.add(mapCtx);
	}
	
	/** Remove the specified codomainMapContext. */
	public void removeCodomainMapCtx(CodomainMapContext mapCtx)
	{
		cdChainDef.remove(mapCtx);
	}
	
	/** Remove all codomainMapContext. */
	public void remove()
	{
		cdChainDef.removeAll(cdChainDef);
	}
	
	/**
	 * Update a codomain transformation context if it has already been selected.
	 * 
	 * @param cmd CodomainMapDef to be updated.
	 */
	public void updateCodomainMapCtx(CodomainMapContext mapCtx)
	{
		if (mapCtx == null)	throw new NullPointerException("No context.");
		int i = cdChainDef.indexOf(mapCtx);  //Recall equals() is overridden.
		if (i == -1)
			throw new IllegalArgumentException("No such a context.");
		cdChainDef.set(i, mapCtx);
	}
	
	/** Set the model. One of the constant defined above.*/
	public void setModel(int model)
	{
		if (model != GS && model != RGB && model != HSB)  
			throw new IllegalArgumentException("Unsupported model type.");
		this.model = model;
	}
	
	public int getModel() { return model; }

	public void setQuantumDef(QuantumDef qDef)
	{
		if (qDef == null)
			throw new IllegalArgumentException("No quantum strategy.");
		this.qDef = qDef;
	}
	
	public QuantumDef getQuantumDef() { return qDef; }

	//returned array cb is such that cb[i].getIndex() == i
	public ChannelBindings[] getChannelBindings()
	{
		ChannelBindings[] copy = new ChannelBindings[channelBindings.length];
		for (int i = 0; i < copy.length; ++i)
			copy[i] = channelBindings[i];
		return copy;	
	}

	public List getCodomainChainDef() { return cdChainDef; }
	
	public int getDefaultZ() { return defaultZ; }
	
	public int getDefaultT() { return defaultT; }
	
	public RenderingDef copy()
	{
		RenderingDef copy = new RenderingDef();
		copy.defaultZ = this.defaultZ;
		copy.defaultT = this.defaultT;
		copy.qDef = this.qDef.copy();
		ChannelBindings[] cb = new ChannelBindings[channelBindings.length];
		for (int i = 0; i < channelBindings.length; i++)
			cb[i] = channelBindings[i].copy();
		copy.channelBindings = cb;
		List list = new ArrayList();
		Iterator j = cdChainDef.iterator();
		CodomainMapContext ctxCopy;
		while (j.hasNext()) {
			ctxCopy = ((CodomainMapContext) j.next()).copy();
			list.add(ctxCopy);
		}
		copy.cdChainDef = list;
		return copy;
	}

}
