/*
 * org.openmicroscopy.shoola.env.rnd.gsStrategy
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
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainChain;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.data.Plane2D;
import org.openmicroscopy.shoola.env.rnd.defs.ChannelBindings;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.metadata.ImageDimensions;
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
class GreyScaleStrategy 
	extends RenderingStrategy
{
	/** Number of Pixels on the X1-axis. */
	private int 		sizeX1;
	
	/** Number of Pixels on the X2-axis. */
	private int 		sizeX2;
	
	private Renderer	renderer;
	
	BufferedImage render(Renderer ctx)
	{
		renderer = ctx;
		ImageDimensions	d = renderer.getImageDims();
		PlaneDef		pd = renderer.getPlaneDef();
		RenderingDef	rd = renderer.getRenderingDef();
		
		//init sizeX1 and sizeX2
		initAxesSize(pd, d);
		DataBufferByte dataBuf = new DataBufferByte(sizeX1*sizeX2, 3);
		
		QuantumManager QM = renderer.getQuantumManager();
		DataSink ds = renderer.getDataSink();
		List cBindings = rd.channelBindings;
		Iterator i = cBindings.iterator();
		ChannelBindings cb;
		Plane2D plane;
		boolean notRendered = true;
		// we pick the first active one if more than one are active.
		while (i.hasNext()) {
			cb = (ChannelBindings) i.next();
			if (cb.active && notRendered) {
				notRendered  = false;
				plane = ds.getPlane2D(pd, cb.index, sizeX1, sizeX2);
				renderWave(dataBuf, plane, QM.getStrategyFor(cb.index), 
							cb.rgba);
			}
		}
		ComponentColorModel ccm = new ComponentColorModel(
									ColorSpace.getInstance(ColorSpace.CS_GRAY), 
									null, false, false, Transparency.OPAQUE, 
									DataBuffer.TYPE_BYTE);
		BandedSampleModel   csm = new BandedSampleModel(DataBuffer.TYPE_BYTE, 
										sizeX1, sizeX2, 1);
		return new BufferedImage(ccm, 
								Raster.createWritableRaster(csm, dataBuf, null), 
								false, null);
	}

	/** 
	 * Initialize the sizes <code>sizeX1</code> and <code>sizeX2</code>
	 * according to the specified {PlaneDef#slice slice}.
	 * @param pd	reference to the PlaneDef object defined for the strategy.
	 * @param d		image dimensions object.
	 */
	private void initAxesSize(PlaneDef pd, ImageDimensions d)
	{
		switch (pd.getSlice()) {
			case PlaneDef.XY:
				initSizes(d.sizeX, d.sizeY);
				break;
			case PlaneDef.XZ:
				initSizes(d.sizeZ, d.sizeX);
				break;
			case PlaneDef.YZ:
				initSizes(d.sizeZ, d.sizeY);
		}
	}
	
	
	private void initSizes(int sizeX1, int sizeX2)
	{
		this.sizeX1 = sizeX1;
		this.sizeX2 = sizeX2;
	}
	
	private void renderWave(DataBufferByte dataBuf, Plane2D plane, 
							QuantumStrategy qs, int[] rgba)
	{
		CodomainChain cc = renderer.getCodomainChain();
		int x1, x2, discreteValue, v;
		float alpha = ((float) rgba[3])/255;
		for (x1 = 0; x1 < sizeX1; ++x1) 
			for (x2 = 0; x2 < sizeX2; ++x2) {
				discreteValue = qs.quantize(plane.getPixelValue(x2, x1));
				v = cc.transform(discreteValue);
				dataBuf.setElem(0, sizeX2*x1+x2, (int) (v*alpha/255));
	   } 
	}
}

