/*
 * org.openmicroscopy.shoola.env.rnd.RenderingControl
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainMapContext;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStats;

/** 
 * A facade to a given rendering environment for the UI benefit.
 * Allows to tune and read the rendering settings (set/get quantum, set/get 
 * channel, and so on).  Also allows to save the current settings.
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
public interface RenderingControl
{
	
	//Pixels metadata.
	public PixelsDimensions getPixelsDims();
	public PixelsStats getPixelsStats();
	
	//RenderingDef fields.
	public void setModel(int model);
	public int getModel();
	public int getDefaultZ();
	public int getDefaultT();
	//Is it the best way to do it?
    public void setDefaultZ(int z);
    public void setDefaultT(int t);
    
	//QuantumDef fields.  Two setters b/c we don't wanna rebuild all LUT's
	//if not necessary.
	public void setQuantumStrategy(int bitResolution);
	public void setCodomainInterval(int start, int end);
	public QuantumDef getQuantumDef();
	
	//ChannelBindings[] elements' fields.
    public void setQuantizationMap(int w, int family, double coefficient, 
                                    boolean noiseReduction);
    public int getChannelFamily(int w);
    public boolean getChannelNoiseReduction(int w);
    public double[] getChannelStats(int w);
    public double getChannelCurveCoefficient(int w);
	public void setChannelWindow(int w, double start, double end);
	public double getChannelWindowStart(int w);
	public double getChannelWindowEnd(int w);
	public void setRGBA(int w, int red, int green, int blue, int alpha);
	public int[] getRGBA(int w);
	public void setActive(int w, boolean active);
	public boolean isActive(int w);
	
	//Codomain chain definition.
	public void addCodomainMap(CodomainMapContext mapCtx);
	public void updateCodomainMap(CodomainMapContext mapCtx);
	public void removeCodomainMap(CodomainMapContext mapCtx);
	
	//Save display options to db.
	public void saveCurrentSettings();
	
	//ResetDefaults values.
	public void resetDefaults();
	
}
