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
import org.openmicroscopy.shoola.env.config.Registry;
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
 * Transforms raw image data into an {@link BufferedImage} object that can be 
 * displayed on screen. 
 * <p>Every instance of this class works against a given pixels set within an
 * image &#151; recall that an image can have more than one pixels set, and
 * holds the rendering environment for that pixels set.  Said environment is
 * composed of:</p>
 * <ul>
 *  <li>Resources to access pixels raw data and metadata.</li>
 *  <li>Cached pixels metadata (statistic measurements).</li>
 *  <li>Settings that define the transformation context &#151; that is, a 
 *   specification of how raw data is to be transformed into an image that
 *   can be displayed on screen.</li>
 *  <li>Resources to apply the transformations defined by the transformation
 *   context to raw pixels.</li>
 * </ul>
 * <p>This class delegates the actual rendering to a {@link RenderingStrategy},
 * which is selected depending on how transformed data is to be mapped into
 * a color space.</p>
 *
 * @see MetadataSource
 * @see DataSink
 * @see RenderingDef
 * @see QuantumManager
 * @see CodomainChain
 * @see RenderingStrategy
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
    
    /**
     * Factory method to create a new instance to render the specified pixels
     * set.
     * 
     * @param imageID   The id of the image the pixels set belongs to.
     * @param pixelsID  The id of the pixels set.
     * @param engine    Reference to the rendering engine.  
     *                  Mustn't be <code>null</code>.
     * @throws MetadataSourceException If an error occurs while retrieving the
     *                                  data from <i>OMEDS</i>.
     * @throws DataSourceException If an error occurs while creating the 
     *                              gateway to <i>OMEIS</i>.
     */
    static Renderer makeNew(int imageID, int pixelsID, RenderingEngine engine)
        throws MetadataSourceException, DataSourceException
    {
        if (engine == null) throw new NullPointerException("No engine.");
        Registry context = RenderingEngine.getRegistry();
        MetadataSource source = 
                            MetadataSource.makeNew(imageID, pixelsID, context);
        DataSink sink = 
                    DataSink.makeNew(source, engine.getCmdProcessor(), context);
        Renderer r = new Renderer(imageID, pixelsID, sink);
        r.initialize(source);
        return r;
    }
    
    
    /** 
     * The id of the image containing the pixels set this object works on.
     * This is the id hold in the database and used by <i>OMEDS</i>. 
     */
	private int 				imageID;
    
    /** 
     * The id of the pixels set this object works on. 
     * This is the id hold in the database and used by <i>OMEDS</i>.
     */
	private int					pixelsID;
    
    /** 
     * The <i>OMEIS</i> id of the pixels set this object works on.
     * This is the parallel id used by <i>OMEIS</i> to identify the pixels
     * set pointed by {@link #pixelsID}. 
     */
	private long 				omeisPixelsID;
    
    /** 
     * The dimensions of the 5D array containing the pixels set this object
     * works on. 
     */
	private PixelsDimensions	pixelsDims;
    
    /** Statistic measurements on the the pixels set this object works on. */
	private PixelsStats			pixelsStats;
	
    /**
     * The settings that define the transformation context.
     * That is, a specification of how raw data is to be transformed into an 
     * image that can be displayed on screen.
     */
    private RenderingDef		renderingDef;
	
    /**
     * Manages and allows to retrieve the objects that are used to quantize
     * wavelength data.
     */
	private QuantumManager		quantumManager;
    
    /**
     * Allows to retrieve the sequence of spatial transformations to be
     * applied to quantized data.
     */
    private CodomainChain       codomainChain;
    
	/**
     * Takes care of the actual rendering, given the context hold by this
     * object.
	 */
	private RenderingStrategy	renderingStrategy;
	
    /** Allows to access the pixels raw data. */
    private DataSink            dataSink;	
		
    /**
     * Creates a new instance to render the specified pixels set.
     * The {@link #initialize() initialize} method has to be called straight
     * after in order to get this new instance ready for rendering.
     * 
     * @param imageID   The id of the image the pixels set belongs to.
     * @param pixelsID  The id of the pixels set.
     */
    private Renderer(int imageID, int pixelsID, DataSink sink)
    {
        this.imageID = imageID;
        this.pixelsID = pixelsID;
        this.dataSink = sink;
    }
    
    /**
     * Initializes the rendering environment, loads the pixels metadata and
     * the display options.
     * 
     * @param source    The pixels metadata source.
     */
    private void initialize(MetadataSource source)
        throws MetadataSourceException
    {
        //Grab pixels metadata (create default display options if 
        //none available).
        omeisPixelsID = source.getOmeisPixelsID();
        pixelsDims = source.getPixelsDims();
        pixelsStats = source.getPixelsStats();
        renderingDef = source.getDisplayOptions();
        boolean isNull = false;
        if (renderingDef == null) {
            isNull = true;
            renderingDef = createDefaultRenderingDef(pixelsDims, pixelsStats,
                        source.getPixelType());
           
        }
        //Create and configure the quantum strategies.
        QuantumDef qd = renderingDef.getQuantumDef();
        quantumManager = new QuantumManager(pixelsDims.sizeW);
        
        ChannelBindings[] cBindings= renderingDef.getChannelBindings();
        //Compute the stats if necessary. Should be removed asap.
        StatsComputer.computeStats(dataSink, source, cBindings, 
                                    getDefaultPlaneDef(), isNull);
        
        //quantumManager.initStrategies(qd, pixelsStats, 
        //                renderingDef.getChannelBindings());
        
        quantumManager.initStrategies(qd, pixelsStats, cBindings);
        //Create and configure the codomain chain.
        codomainChain = new CodomainChain(qd.cdStart, qd.cdEnd,
                                            renderingDef.getCodomainChainDef());
        
        //Create an appropriate rendering strategy.
        renderingStrategy = RenderingStrategy.makeNew(renderingDef.getModel());
    }
    
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
        QuantumDef qDef = new QuantumDef(pixelType, 0, 
                          QuantumFactory.DEPTH_8BIT, QuantumFactory.DEPTH_8BIT,
                          QuantumFactory.NOISE_REDUCTION);
		ChannelBindings[] waves = new ChannelBindings[dims.sizeW];
		PixelsGlobalStatsEntry wGlobal;
		int[] rgb;
		for (int w = 0; w < dims.sizeW; ++w) {
            wGlobal = stats.getGlobalEntry(w);
            rgb = setDefaultColor(w);
            waves[w] = new ChannelBindings(w, wGlobal.getGlobalMin(),
                                           wGlobal.getGlobalMax(), rgb[0], 
                                           rgb[1], rgb[2], 255, false,
                                           QuantumFactory.LINEAR, 1);
		}
		
		waves[0].setActive(true);  //NOTE: ImageDimensions enforces 1 < sizeW.
		return new RenderingDef(dims.sizeZ/2+dims.sizeZ%2-1, 0, 
								RenderingDef.GS, qDef, waves);
		//NOTE: middle of stack is z=1 if szZ==3, z=1 if szZ==4, etc.
	}
	
	private int[] setDefaultColor(int w)
	{
		int[] rgb = new int[3];
		if (w == 0) { //blue
            rgb[0] = ChannelBindings.COLOR_MIN;
            rgb[1] = ChannelBindings.COLOR_MIN;
            rgb[2] = ChannelBindings.COLOR_MAX;
		} else if (w == 1) { //green
            rgb[0] = ChannelBindings.COLOR_MIN;
            rgb[1] = ChannelBindings.COLOR_MAX;
            rgb[2] = ChannelBindings.COLOR_MIN;
		} else {  //red
			rgb[0] = ChannelBindings.COLOR_MAX;
			rgb[1] = ChannelBindings.COLOR_MIN;
			rgb[2] = ChannelBindings.COLOR_MIN;
		}
		return rgb;
	}
	
    /**
     * Specifies the model that dictates how transformed raw data is to be 
     * mapped into a color space.
     * This class delegates the actual rendering to a {@link RenderingStrategy},
     * which is selected depending on that model.  So setting the model also
     * results in changing the rendering strategy.
     * 
     * @param model Identifies the color space model.  One of the constants
     *              defined by {@link RenderingDef}.
     */
	void setModel(int model)
	{
		renderingDef.setModel(model);
		renderingStrategy = RenderingStrategy.makeNew(model);
	}
	
    void setDefaultZ(int z) 
    {
        renderingDef.setDefaultZ(z);
    }
    
    void setDefaultT(int t) 
    {
        renderingDef.setDefaultT(t);
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
     * Creates the default plane definition to use for the generation of the
     * very first image displayed by 2D viewers.
     * 
     * @return Said plane definition.
     */
    public PlaneDef getDefaultPlaneDef()
    {
        PlaneDef pd = new PlaneDef(PlaneDef.XY, renderingDef.getDefaultT());
        pd.setZ(renderingDef.getDefaultZ());
        return pd;
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
     * @throws QuantizationException If an error occurred while quantizing the
     *                                  pixels raw data.
	 */
	BufferedImage render(PlaneDef pd)
		throws DataSourceException, QuantizationException
	{
		if (pd == null)
			throw new NullPointerException("No plane definition.");
		return renderingStrategy.render(this, pd);
	}

    /**
     * Returns the size, in bytes, of the {@link BufferedImage} that would be
     * rendered from the plane selected by <code>pd</code>.
     * Note that the returned value also depends on the current rendering
     * strategy which is selected by the {@link #setModel(int) setModel} method.
     * So a subsequent invocation of this method may return a different value
     * if the {@link #setModel(int) setModel} method has been called since the
     * first call to this method.
     * 
     * @param pd    Selects a plane orthogonal to one of the <i>X</i>, <i>Y</i>,
     *              or <i>Z</i> axes.  Mustn't be <code>null</code>.
     * @return  See above.
     */
    int getImageSize(PlaneDef pd)
    {
        if (pd == null)
            throw new NullPointerException("No plane definition.");
        return renderingStrategy.getImageSize(pd, pixelsDims);
    }
    
    /**
     * Returns the dimensions of the 5D array containing the pixels set this
     * object works on.
     * 
     * @return  See above.
     */
	PixelsDimensions getPixelsDims() { return pixelsDims; }

    /**
     * Returns statistic measurements on the the pixels set this object
     * works on.
     * 
     * @return  See above.
     */
	PixelsStats getPixelsStats() { return pixelsStats; }

    /**
     * Returns the settings that define the transformation context.
     * That is, a specification of how raw data is to be transformed into an 
     * image that can be displayed on screen.
     * 
     * @return  See above.
     */
	RenderingDef getRenderingDef() { return renderingDef; }

    /**
     * Returns the object that manages and allows to retrieve the objects 
     * that are used to quantize wavelength data.
     * 
     * @return  See above.
     */
	QuantumManager getQuantumManager() { return quantumManager; }

    /**
     * Returns the object that allows to access the pixels raw data.
     * 
     * @return  See above.
     */
	DataSink getDataSink() { return dataSink; }

    /**
     * Returns the object that allows to retrieve the sequence of spatial 
     * transformations to be applied to quantized data.
     * 
     * @return  See above.
     */
	CodomainChain getCodomainChain() { return codomainChain; }

    /**
     * Returns the id of the image containing the pixels set this object 
     * works on.
     * This is the id hold in the database and used by <i>OMEDS</i>. 
     * 
     * @return  See above.
     */
	int getImageID() { return imageID; }

    /**
     * Returns the <i>OMEIS</i> id of the pixels set this object works on.
     * This is the parallel id used by <i>OMEIS</i> to identify the pixels
     * set pointed by {@link #pixelsID}. 
     * 
     * @return See above.
     */
	long getOmeisPixelsID() { return omeisPixelsID; }

    /**
     * Returns the id of the pixels set this object works on. 
     * This is the id hold in the database and used by <i>OMEDS</i>.
     * 
     * @return  See above.
     */
	int getPixelsID() { return pixelsID; }
    
}
