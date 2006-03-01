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
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainChain;
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainMapContext;
import org.openmicroscopy.shoola.env.rnd.defs.ChannelBindings;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.metadata.MetadataSourceException;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStats;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumStrategy;

/** 
 * Implements RenderingControl acting as a facade to the various bits of the
 * rendering settings.
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
	public PixelsDimensions getPixelsDims() { return renderer.getPixelsDims(); }

	/** Implemented as specified by {@link RenderingControl}. */
	public PixelsStats getPixelsStats() { return renderer.getPixelsStats(); }

	/** Implemented as specified by {@link RenderingControl}. */
	public void setModel(int model) { renderer.setModel(model); }

	/** Implemented as specified by {@link RenderingControl}. */
	public int getModel() { return renderer.getRenderingDef().getModel(); }

	/** Implemented as specified by {@link RenderingControl}. */
	public int getDefaultZ()
	{ 
		return renderer.getRenderingDef().getDefaultZ(); 
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public int getDefaultT() 
	{
		return renderer.getRenderingDef().getDefaultT();
	}
    
    /** Implemented as specified by {@link RenderingControl}. */
    public void setDefaultZ(int z) { renderer.setDefaultZ(z); }
    
    /** Implemented as specified by {@link RenderingControl}. */
    public void setDefaultT(int t) { renderer.setDefaultT(t); }
    
	/** Implemented as specified by {@link RenderingControl}. */
	public void setQuantumStrategy(int bitResolution)
    {
        RenderingDef rd = renderer.getRenderingDef();
        QuantumDef qd = rd.getQuantumDef(), newQd;
        newQd = new QuantumDef(qd.pixelType, qd.cdStart, qd.cdEnd,
                            bitResolution);
        rd.setQuantumDef(newQd);
        renderer.updateQuantumManager();
    }

	/** Implemented as specified by {@link RenderingControl}. */
	public void setCodomainInterval(int start, int end)
    {
        CodomainChain chain = renderer.getCodomainChain();
        chain.setInterval(start, end);
        RenderingDef rd = renderer.getRenderingDef();
        QuantumDef qd = rd.getQuantumDef(), newQd;
        newQd = new QuantumDef(qd.pixelType, start, end, qd.bitResolution);
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
		return renderer.getRenderingDef().getQuantumDef();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void setChannelWindow(int w, double start, double end) 
	{
		QuantumStrategy qs = renderer.getQuantumManager().getStrategyFor(w);
		qs.setWindow(start, end);
		ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
		cb[w].setInputWindow(start, end);
	}
    
     /** Implemented as specified by {@link RenderingControl}. */
    public void setQuantizationMap(int w, int family, double coefficient, 
                                boolean noiseReduction)
    {
        QuantumStrategy qs = renderer.getQuantumManager().getStrategyFor(w);
        qs.setQuantizationMap(family, coefficient, noiseReduction);
        ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
        cb[w].setQuantizationMap(family, coefficient, noiseReduction);
    }
    
    /** Implemented as specified by {@link RenderingControl}. */
    public double[] getChannelStats(int w)
    {
        ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
        return cb[w].getStats();
    }
    
    /** Implemented as specified by {@link RenderingControl}. */
    public boolean getChannelNoiseReduction(int w)
    {
        ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
        return cb[w].getNoiseReduction();
    }
    
    /** Implemented as specified by {@link RenderingControl}. */
    public int getChannelFamily(int w)
    {
        ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
        return cb[w].getFamily();
    }
    
    /** Implemented as specified by {@link RenderingControl}. */
    public double getChannelCurveCoefficient(int w)
    {
        ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
        return cb[w].getCurveCoefficient();
    }

	/** Implemented as specified by {@link RenderingControl}. */
	public double getChannelWindowStart(int w) 
	{
		ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
		return cb[w].getInputStart();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public double getChannelWindowEnd(int w) 
	{
		ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
		return cb[w].getInputEnd();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void setRGBA(int w, int red, int green, int blue, int alpha) 
	{
		ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
		cb[w].setRGBA(red, green, blue, alpha);
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public int[] getRGBA(int w) 
	{
		ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
		return cb[w].getRGBA();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void setActive(int w, boolean active)
	{
		ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
		cb[w].setActive(active);
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public boolean isActive(int w) 
	{
		ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
		return cb[w].isActive();
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void addCodomainMap(CodomainMapContext mapCtx) 
	{	
		renderer.getCodomainChain().add(mapCtx);
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void updateCodomainMap(CodomainMapContext mapCtx)
	{
		renderer.getCodomainChain().update(mapCtx);
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void removeCodomainMap(CodomainMapContext mapCtx)
	{
		renderer.getCodomainChain().remove(mapCtx);
	}
	
	/** Implemented as specified by {@link RenderingControl}. */
	public void saveCurrentSettings() 
	{
        int imageID = renderer.getImageID();
        int pixelsID = renderer.getPixelsID();
        Registry context = RenderingEngine.getRegistry();
        try {
            context.getSemanticTypesService().saveRenderingSettings(pixelsID, 
                    imageID, renderer.getRenderingDef());
        } catch (Exception e) {
            MetadataSourceException mse = new MetadataSourceException(
                "Can't save settings.", e);
            hanldeException(context, "can't save settings", mse);
        }
	}

	/** Implemented as specified by {@link RenderingControl}. */
	public void resetDefaults()
	{
	    //bitResolution <=> 255
        setQuantumStrategy(QuantumFactory.DEPTH_8BIT);
        setCodomainInterval(0, QuantumFactory.DEPTH_8BIT);
        ChannelBindings[] cb = renderer.getRenderingDef().getChannelBindings();
        PixelsStats stats = renderer.getPixelsStats();
        for (int i = 0; i < cb.length; i++)
                resetDefaultsChannel(i, stats);
        //Remove all the codomainMapCtx except the identity.
        renderer.getCodomainChain().remove();
        
        //reset the strategy.
        setModel(RenderingDef.GS);
		
		//reset the strategy.
		setModel(RenderingDef.GS);
	}

	
	/** Reset the defaults for each channel. */
	private void resetDefaultsChannel(int w, PixelsStats stats)
	{
		setActive(w, w == 0);
		double s = stats.getGlobalEntry(w).globalMin, 
				e = stats.getGlobalEntry(w).globalMax;
		setChannelWindow(w, s, e);
		setRGBA(w, 255, 0, 0, 255); //red-green-blue-alpha
	}
	
	private void hanldeException(Registry registry, String message, 
								Exception cause)
	{
		LogMessage msg = new LogMessage();
		msg.print("Rendering Engine Exception: ");
		msg.println(message);
		msg.print(cause);
		registry.getLogger().error(this, msg);
		registry.getUserNotifier().notifyError("Rendering Engine Exception", 
												message, msg.toString());
	}

}
