/*
 * org.openmicroscopy.shoola.env.rnd.Renderer
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
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.codomain.CodomainChain;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.data.DataSourceException;
import org.openmicroscopy.shoola.env.rnd.defs.ChannelBindings;
import org.openmicroscopy.shoola.env.rnd.defs.PlaneDef;
import org.openmicroscopy.shoola.env.rnd.defs.QuantumDef;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;
import org.openmicroscopy.shoola.env.rnd.metadata.MetadataSource;
import org.openmicroscopy.shoola.env.rnd.metadata.MetadataSourceException;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsDimensions;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsGlobalStatsEntry;
import org.openmicroscopy.shoola.env.rnd.metadata.PixelsStats;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantizationException;
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;

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
class Renderer
{
	
	private int 				imageID;
	private int					pixelsID;
	private long 				omeisPixelsID;
	private PixelsDimensions	pixelsDims;
	private PixelsStats			pixelsStats;
	private RenderingDef		renderingDef;
	
	private PlaneDef			planeDef;
	private QuantumManager		quantumManager;
	private DataSink			dataSink;
	private RenderingStrategy	renderingStrategy;
	private CodomainChain		codomainChain;

	private RenderingEngine		engine;	
		
	/** 
	 * Helper method to create the default settings if none is available.
	 * In this case we use a grayscale model to map the first wavelength in
	 * the pixels file.  The mapping is linear and the intervals are selected
	 * according to a "best-guess" statistical approach.
	 * 
	 * @param stats	For each wavelength, it contains the global minimum and
	 * 				maximum of the wavelength stack across time.
	 * @return	The default rendering settings.
	 */
	private RenderingDef createDefaultRenderingDef(PixelsDimensions dims,
											PixelsStats stats, int pixelType)
	{
		QuantumDef qDef = new QuantumDef(QuantumFactory.LINEAR, pixelType, 1, 
											0, QuantumFactory.DEPTH_8BIT,
											QuantumFactory.DEPTH_8BIT);
		ChannelBindings[] waves = new ChannelBindings[dims.sizeW];
		PixelsGlobalStatsEntry wGlobal;
		int[] rgb;
		for (int w = 0; w < dims.sizeW; ++w) {
			wGlobal = stats.getGlobalEntry(w);
			//TODO: calcultate default interval, should come in next version.
			rgb = setDefaultColor(w);
			waves[w] = new ChannelBindings(w, wGlobal.getGlobalMin(),
												wGlobal.getGlobalMax(),
											rgb[0], rgb[1], rgb[2], 255, false);
			
			
		}
		
		waves[0].setActive(true);  //NOTE: ImageDimensions enforces 1 < sizeW.
		return new RenderingDef(dims.sizeZ/2+dims.sizeZ%2-1, 0, 
								RenderingDef.GS, qDef, waves);
		//NOTE: middle of stack is z=1 if szZ==3, z=1 if szZ==4, etc.
	}
	
	private int[] setDefaultColor(int w)
	{
		int[] rgb = new int[3];
		if (w == 1) {
			rgb[0] = 0;
			rgb[1] = 255;
			rgb[2] = 0;
		} else if (w == 2) {
			rgb[0] = 0;
			rgb[1] = 0;
			rgb[2] = 255;
		} else {
			rgb[0] = 255;
			rgb[1] = 0;
			rgb[2] = 0;
		}
		return rgb;
	}
    
    /** Only used to create shallow copies. */
    private Renderer(int imageID, int pixelsID, long omeisPixelsID,
                        PixelsDimensions pixelsDims, PixelsStats pixelsStats,
                        RenderingDef renderingDef, PlaneDef planeDef,
                        QuantumManager quantumManager, DataSink dataSink,
                        RenderingStrategy renderingStrategy,
                        CodomainChain codomainChain,
                        RenderingEngine engine)
    {
        this.imageID = imageID;
        this.pixelsID = pixelsID;
        this.omeisPixelsID = omeisPixelsID;
        this.pixelsDims = pixelsDims;
        this.pixelsStats = pixelsStats;
        this.renderingDef = renderingDef;
        this.planeDef = planeDef;
        this.quantumManager = quantumManager;
        this.dataSink = dataSink;
        this.renderingStrategy = renderingStrategy;
        this.codomainChain = codomainChain;
        this.engine = engine;
    }
    
	/**
	 * Creates a new instance to render the specified pixels set.
	 * The {@link #initialize() initialize} method has to be called straight
	 * after in order to get this new instance ready for rendering.
	 * 
	 * @param imageID	The id of the image the pixels set belongs to.
	 * @param pixelsID	The id of the pixels set.
	 * @param engine	Reference to the rendering engine.
	 */
	Renderer(int imageID, int pixelsID, RenderingEngine engine)
	{
		this.imageID = imageID;
		this.pixelsID = pixelsID;
		this.engine = engine;
	}
	
	/**
	 * Initializes the rendering environment, loads the pixels metadata and
	 * the display options.
	 * 
	 * @throws MetadataSourceException If an error occurs while retrieving the
	 * 									data from the source repository.
	 */
	void initialize()
		throws MetadataSourceException
	{
		//Load pixels metadata (create default display options if none 
		//available)
		MetadataSource source = engine.getMetadataSource(imageID, pixelsID);
		source.load();
		omeisPixelsID = source.getOmeisPixelsID();
		pixelsDims = source.getPixelsDims();
		pixelsStats = source.getPixelsStats();
		renderingDef = source.getDisplayOptions();
		if (renderingDef == null)
			renderingDef = createDefaultRenderingDef(pixelsDims, pixelsStats,
														source.getPixelType());
		
		//Set the default plane.
		planeDef = new PlaneDef(PlaneDef.XY, renderingDef.getDefaultT());
		planeDef.setZ(renderingDef.getDefaultZ());
		
		//Create and configure the quantum strategies.
		QuantumDef qd = renderingDef.getQuantumDef();
		quantumManager = new QuantumManager(pixelsDims.sizeW);
		quantumManager.initStrategies(qd, pixelsStats, 
						renderingDef.getChannelBindings());
		
		//Create and configure the codomain chain.
		codomainChain = new CodomainChain(qd.cdStart, qd.cdEnd,
											renderingDef.getCodomainChainDef());
		
		//Cache a reference to the pixels source gateway. 
		dataSink = engine.getDataSink(source.getPixels(),  //TODO: to be turned into int. 
										source.getPixelType(), pixelsDims);
		
		//Create an appropriate rendering strategy.
		renderingStrategy = RenderingStrategy.makeNew(renderingDef.getModel());
	}
	
	void setModel(int model)
	{
		renderingDef.setModel(model);
		renderingStrategy = RenderingStrategy.makeNew(model);
	}
	
	/**
	 * Updates the {@link QuantumManager} and configures it according to the
	 * current quantum definition.
	 */
	void updateQuantumManager()
	{
		QuantumDef qd = renderingDef.getQuantumDef();
		ChannelBindings[] cb = renderingDef.getChannelBindings();
		quantumManager.initStrategies(qd, pixelsStats, cb);
	}
	
	/**
	 * Renders the data selected by <code>pd</code> according to the current
	 * rendering settings.
	 * The passed argument selects a plane orthogonal to one of the <i>X</i>, 
	 * <i>Y</i>, or <i>Z</i> axes.  How many wavelengths are rendered and
	 * what color model is used depends on the current rendering settings.
	 * 
	 * @param pd	Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
	 * 				or <i>Z</i> axes.  Mustn't be <code>null</code>.
	 * @return	A buffered image ready to be displayed on screen.
	 * @throws DataSourceException If an error occured while trying to pull out
	 * 								data from the pixels data repository.
	 */
	BufferedImage render(PlaneDef pd)
		throws DataSourceException, QuantizationException
	{
		if (pd == null)
			throw new NullPointerException("No plane definition.");
		planeDef = pd;
		return renderingStrategy.render(this);
	}
	
	/** Reset the plane definition to an XYPlane.*/
	void resetPlaneDef(PlaneDef pd) { planeDef = pd; }
	
	/**
	 * Renders the data selected by the current plane definition according to
	 * the current rendering settings.
	 * The current plane definition is initially set to the <i>XY</i> plane
	 * specified in the display options and then to the argument passed to
	 * {@link #render(PlaneDef)}.
	 * 
	 * @return	A buffered image ready to be displayed on screen.
	 * @throws DataSourceException If an error occured while trying to pull out
	 * 								data from the pixels data repository.
	 */
	BufferedImage render()
		throws DataSourceException, QuantizationException
	{
		//Note that planeDef can never be null.
		return renderingStrategy.render(this);
	}
    
    /**
     * Makes a shallow copy of this objects.
     * The returned new object shares all references with this object, except
     * for the plane definition if the passed argument is not <code>null</code>.
     * 
     * @param pd A new plane definition for the copy or <code>null</code> to
     *              have an exact shallow copy.
     * @return A shallow copy of this object.
     */
    Renderer makeShallowCopy(PlaneDef pd)
    {
        return new Renderer(imageID, pixelsID, omeisPixelsID,
                            pixelsDims, pixelsStats, renderingDef,
                            (pd == null) ? planeDef : pd,
                            quantumManager, dataSink, renderingStrategy,
                            codomainChain, engine);
    }

	PixelsDimensions getPixelsDims() { return pixelsDims; }

	PixelsStats getPixelsStats() { return pixelsStats; }

	PlaneDef getPlaneDef() { return planeDef; }

	RenderingDef getRenderingDef() { return renderingDef; }

	QuantumManager getQuantumManager() { return quantumManager; }

	DataSink getDataSink() { return dataSink; }

	CodomainChain getCodomainChain() { return codomainChain; }
	
	RenderingEngine getEngine() { return engine; }

	int getImageID() { return imageID; }

	long getOmeisPixelsID() { return omeisPixelsID; }

	int getPixelsID() { return pixelsID; }

}
