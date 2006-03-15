/*
 * omeis.providers.re.RenderingEngine
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

package omeis.providers.re;


//Java imports
import java.io.IOException;

import ome.model.display.QuantumDef;
import ome.system.SelfConfigurableService;

//Third-party libraries

//Application-internal dependencies
import omeis.providers.re.codomain.CodomainMapContext;
import omeis.providers.re.data.PlaneDef;
import omeis.providers.re.quantum.QuantizationException;

/** 
 * Defines a service to render a given pixels set. 
 * <p>A pixels set is a <i>5D</i> array that stores the pixels data of an image,
 * that is the pixels intensity values.  Every instance of this service is
 * paired up to a pixels set.  Use this service to transform planes within the
 * pixels set onto an <i>RGB</i> image.</p>  
 * <p>The <code>RenderingEngine</code> allows to fine-tune the settings that 
 * define the transformation context &#151; that is, a specification of how raw
 * pixels data is to be transformed into an image that can be displayed on 
 * screen.  Those settings are referred to as rendering settings or display
 * options.  After tuning those settings it is possible to save them to the
 * metadata repository so that they can be used the next time the pixels set
 * is accessed for rendering; for example by another <code>RenderingEngine
 * </code> instance.  Note that the display options are specific to the given
 * pixels set and are experimenter scoped &#151; that is, two different users
 * can specify different display options for the <i>same</i> pixels set.  (A
 * <code>RenderingEngine</code> instance takes this into account automatically
 * as it is always bound to a given experimenter.)</p>
 * <p>This service is <b>thread-safe</b>.</p>
 * 
 * @see RenderingEngineDescriptor
 *  
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/06/12 23:28:59 $)
 * </small>
 * @since OME2.2
 */
public interface RenderingEngine extends SelfConfigurableService
{

    /**
     * Renders the data selected by <code>pd</code> according to the current
     * rendering settings.
     * The passed argument selects a plane orthogonal to one of the <i>X</i>, 
     * <i>Y</i>, or <i>Z</i> axes.  How many wavelengths are rendered and
     * what color model is used depends on the current rendering settings.
     * 
     * @param pd Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *           or <i>Z</i> axes.
     * @return An <i>RGB</i> image ready to be displayed on screen.
     * @throws IOException If an error occured while trying to pull out
     *                     data from the pixels data repository.
     * @throws QuantizationException If an error occurred while quantizing the
     *                               pixels raw data.
     * @throws NullPointerException If <code>pd</code> is <code>null</code>.
     */
    public RGBBuffer render(PlaneDef pd)
        throws IOException, QuantizationException;
    
    
    //TODO: javadoc the rest!
    
	//State management.
	public void lookupPixels(long pixelsId);
	public void lookupRenderingDef(long pixelsId);
	public void load();
	
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
