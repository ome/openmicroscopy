/*
 * org.openmicroscopy.shoola.env.rnd.codomain.CodomainChain
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

package org.openmicroscopy.shoola.env.rnd.codomain;

//Java imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies

/** 
 * Queues the contexts that define the spatial domain transformations that have 
 * to be applied to the image.
 * <p>A lookup table is built by composing all maps (in the same order as their 
 * contexts were enqueued) in a single tranformation and then by applying this
 * map to each value in the codomain interval 
 * <code>[intervalStart, intervalEnd]</code> &#151; note that, in order to
 * compose the maps, this interval has to be both the domain and codomain of
 * each transformation.  The LUT is re-built everytime the definition of the 
 * codomain interval or the state of the queue changes.</p>
 * <p>Contexts are privately owned ({@link #add(CodomainMapContext) add} and 
 * {@link #update(CodomainMapContext) update} make copies) because we want to
 * exclude the possibility that a context's state can  be modified after the
 * lookup table is built.</p>
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
public class CodomainChain
{
	
	/** 
	 * Maximum value (<code>255</code>) allowed for the upper bound of the 
	 * codomain interval.
	 */
	public static final int		MAX = 255;
	
	/** 
	 * Minimum value (<code>0</code>) allowed for the lower bound of the 
	 * codomain interval.
	 */
	public static final int		MIN = 0;
	
	/** 
	 * Identity map.
	 * This is a singleton and is always the first map in a codomain chain. 
	 */
	private static CodomainMapContext	identityCtx;
	
	/** Codomain lookup table.*/
	private int[]				LUT;
	
	/** 
	 * A queue to sequence the context of each codomain transformation 
	 * that has to be applied.
	 */
	private List				chain;
	
	/** The lower bound of the codomain interval. */
	private int					intervalStart;
	
	/** The upper bound of the codomain interval. */
	private int					intervalEnd;
	
	/**
	 * Creates a new chain.
	 * The chain will contain the identity context.  So if no transformation
	 * is added, the {@link #transform(int) transform} method returns the
	 * input value.
	 * The interval defined by <code>start</code> and <code>end</code> has to
	 * be a sub-interval of <code>[{@link #MIN}, {@link #MAX}]</code>. 
	 * 
	 * @param start	The lower bound of the codomain interval.
	 * @param end	The upper bound of the codomain interval.
	 */
	public CodomainChain(int start, int end)
	{
		this(start, end, null);
	}
	
	/**
	 * Creates a new chain.
	 * The chain will contain the specified contexts &#151; if 
	 * <code>mapContexts</code> is <code>null</code> or empty, the chain
	 * will contain only the identity context.
	 * The interval defined by <code>start</code> and <code>end</code> has to
	 * be a sub-interval of <code>[{@link #MIN}, {@link #MAX}]</code>.
	 * 
	 * @param start	The lower bound of the codomain interval.
	 * @param end	The upper bound of the codomain interval.
	 * @param mapContexts	The sequence of {@link CodomainMapContext} objects
	 * 						that define the chain.  No two objects of the same
	 * 						class are allowed.  The objects in this list are
	 * 						copied.
	 */
	public CodomainChain(int start, int end, List mapContexts)
	{
		chain = new ArrayList();
		if (identityCtx == null) identityCtx = new IdentityMapContext();
		if (mapContexts != null && 0 < mapContexts.size()) {
			Iterator i = mapContexts.iterator();
			CodomainMapContext ctx;
			while (i.hasNext()) {
				ctx = (CodomainMapContext) i.next();
				if (chain.contains(ctx))  //Recall equals() is overridden.
					throw new IllegalArgumentException(
													"Context already defined.");
				ctx = ctx.copy();
				chain.add(ctx);
			}
		} else {
			chain.add(identityCtx);
		}
		setInterval(start, end);
	}
	
	/**
	 * Sets the codomain interval.
	 * This triggers an update of all map contexts in the chain and a re-build
	 * of the lookup table.
	 * The interval defined by <code>start</code> and <code>end</code> has to
	 * be a sub-interval of <code>[{@link #MIN}, {@link #MAX}]</code>. 
	 * 
	 * @param start	The lower bound of the codomain interval.
	 * @param end	The upper bound of the codomain interval.
	 */
	public void setInterval(int start, int end)
	{
		verifyInterval(start, end);
		intervalStart = start;
		intervalEnd = end;
		CodomainMapContext ctx;
		Iterator i = chain.iterator();
		while (i.hasNext()) {
			ctx = (CodomainMapContext) i.next();
			ctx.setCodomain(start, end);
			ctx.buildContext();
		}
		buildLUT();
	}
	
	/** Returns the upper bound of the codomain interval. */
	public int getIntervalEnd() { return intervalEnd; }

	/** The lower bound of the codomain interval. */
	public int getIntervalStart() { return intervalStart; }
	
	/** 
	 * Adds a map context to the chain.
	 * This means that the transformation associated to the passed context
	 * will be applied after all the currently queued transformations.
	 * An exception will be thrown if the chain already contains an object of
	 * the same class as <code>mapCtx</code>.  This is because we don't want
	 * to compose the same transformation twice.
	 * This method adds a copy of <code>mapCtx</code> to the chain.  This is
	 * because we want to exclude the possibility that the context's state can
	 * be modified after the lookup table is built.
	 * This method triggers a re-build of the lookup table. 
	 * 
	 * @param mapCtx	The context to add.  Mustn't be <code>null</code> or
	 * 					already contained in the chain.
	 */
	public void add(CodomainMapContext mapCtx)
	{
		if (mapCtx == null)	throw new NullPointerException("No context.");
		if (chain.contains(mapCtx))  //Recall equals() is overridden.
			throw new IllegalArgumentException("Context already defined.");
		mapCtx = mapCtx.copy();  //Get memento and discard original object.
		mapCtx.setCodomain(intervalStart, intervalEnd);
		mapCtx.buildContext();
		chain.add(mapCtx);
		buildLUT();
	}
	
	/** 
	 * Updates a map context in the chain.
	 * An exception will be thrown if the chain doesn't contain an object of
	 * the same class as <code>mapCtx</code>.
	 * This method replaces the old context with a copy of <code>mapCtx</code>.
	 * This is because we want to exclude the possibility that the context's
	 * state can be modified after the lookup table is built.
	 * This method triggers a re-build of the lookup table. 
	 * 
	 * @param mapCtx	The context to add.  Mustn't be <code>null</code> or
	 * 					already contained in the chain.
	 */
	public void update(CodomainMapContext mapCtx)
	{
		if (mapCtx == null)	throw new NullPointerException("No context.");
		int i = chain.indexOf(mapCtx);  //Recall equals() is overridden.
		if (i == -1)
			throw new IllegalArgumentException("No such a context.");
		mapCtx = mapCtx.copy();  //Get memento and discard original object.
		mapCtx.setCodomain(intervalStart, intervalEnd);
		mapCtx.buildContext();
		chain.set(i, mapCtx);
		buildLUT();
	}
	
	/**
	 * Removes a map context from the chain.
	 * This method removes the object (if any) in the chain that is an instance
	 * of the same class as <code>mapCtx</code>.  This means that the
	 * transformation associated to the passed context won't be applied.
	 * This method triggers a re-build of the lookup table.
	 * 
	 * @param mapCtx	The context to remove.
	 */
	public void remove(CodomainMapContext mapCtx)
	{
		if (mapCtx != null && 
			chain.contains(mapCtx)) {  //Recall equals() is overridden.
			chain.remove(mapCtx);
			buildLUT();	
		}
	}
	
	/** 
	 * Applies the transformation.
	 * This transformation is the result of the composition of all maps defined
	 * by the current chain.  Composition follows the chain order.
	 * 
	 * @param x	The input value.  Must be in the current codomain interval.
	 * @return	The output value, y.
	 */
	public int transform(int x)
	{
		int y = verifyInput(x);
		return LUT[y-intervalStart];
	}
	
	/** Builds the lookup table. */
	private void buildLUT()
	{
		LUT = new int[intervalEnd-intervalStart+1];
		CodomainMap map;
		CodomainMapContext ctx;
		int v;
		for (int x = intervalStart; x <= intervalEnd; ++x) {
			v = x;
			Iterator i = chain.iterator();
			while (i.hasNext()) {
				ctx = (CodomainMapContext) i.next();
				map = ctx.getCodomainMap();
				map.setContext(ctx);
				v = map.transform(v);
			}
			LUT[x-intervalStart] = v;	
		}
	}
	
	/** 
	 * Verifies the bounds of the codomain interval. 
	 * 
	 * @param start		Lower bound of the interval.
	 * @param end		Upper bound of the interval.
	 */
	private void verifyInterval(int start, int end)
	{
		if (start >= end || start < MIN || end > MAX)
			throw new IllegalArgumentException("Interval not consistent.");
	}
	
	/** 
	 * Verifies that the specifed input value is in the codomain interval.
	 * 
	 * @param x		Input value.
	 */
	private int verifyInput(int x)
	{
		if (x < intervalStart) x = intervalStart;
		else if (x > intervalEnd) x = intervalEnd;
		return x;
	}
	
}
