/*
 * org.openmicroscopy.shoola.env.rnd.metadata.MetadataSource
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

package org.openmicroscopy.shoola.env.rnd.metadata;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.is.StackStatistics;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.PixelsService;
import org.openmicroscopy.shoola.env.data.model.PixelsDescription;
import org.openmicroscopy.shoola.env.rnd.data.DataSink;
import org.openmicroscopy.shoola.env.rnd.defs.RenderingDef;

/** 
 * Manages access to the metadata associated with a pixels set within a
 * given image.
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
public class MetadataSource
{
	
    /**
     * Factory method to create a new <code>MetadataSource</code> to handle
     * access to the metadata associated with the specified pixels set within
     * the given image.
     *                 
     * @param imageID   The id of the image the pixels set belongs to.
     * @param pixelsID  The id of the pixels set.
     * @param context   The container's registry.  Mustn't be <code>null</code>.
     * @throws MetadataSourceException If an error occurs while retrieving
     *                                  data from <i>OMEDS</i>.
     */
    public static MetadataSource makeNew(int imageID, int pixelsID, 
                                            Registry context)
        throws MetadataSourceException
    {
        if (context == null) throw new NullPointerException("No registry.");
        MetadataSource source = new MetadataSource(imageID, pixelsID);
        source.load(context);
        return source;
    }
    
	/** The id of the image this object is dealing with. */
	private int					imageID;
	
	/** The id of the pixels set within the image. */
	private int					pixelsID;
	
	/** The pixel type identifier, as defined by {@link DataSink}. */
	private int					pixelType;
	
	/** 
	 * The dimensions of the pixels set. 
	 * Never <code>null</code> after a call to {@link #load()}.
	 */
	private PixelsDimensions	pixelsDims;
	
	/** 
	 * Various statistics calculated on the pixels set.
	 * Never <code>null</code> after a call to {@link #load()}.
	 */
	private PixelsStats			pixelsStats;
	
	/**
	 * The rendering settings (if any) associated with the pixels set.
	 * These are specific to a given user.
	 * If the metadata repository contains no such settings for the current
	 * user, this field will be left <code>null<code>.
	 */
	private RenderingDef		displayOptions;
    
    /** The id of the pixels set under <i>OMEIS</i>. */
    private long                omeisPixelsID;
    
    /** 
     * A <i>URL</i> pointing to the <i>OMEIS</i> instance that manages the
     * pixels set identified by {@link #omeisPixelsID}.
     */
    private String              omeisURL;	
	
	
    /**
     * Creates a new instance to handle the pixels metadata.
     * A call to {@link #load()} will retrieve the metadata which can then be
     * accessed via the various <code>getXXX</code> methods.
     * 
     * @param imageID   The id of the image whom the pixels set belongs to.
     * @param pixelsID  The id of the pixels set.
     */
    private MetadataSource(int imageID, int pixelsID)
    {
        this.imageID = imageID;
        this.pixelsID = pixelsID;
    }
    
    /**
     * Helper object to extract relevant statistic from the more general
     * purpose stats stored in <i>OME</i>.
     * 
     * @param s The <i>OME</i> stats.
     * @param d The dimensions of the pixels set.
     * @return A {@link PixelsStats} object containing only the stats that
     *          are relevant to the Rendering Engine.
     */
    private PixelsStats makeStats(StackStatistics s, PixelsDimensions d)
    {
        double gMin = 0;
        double gMax = 1;
        double min, max;
        PixelsStats ps = new PixelsStats(d.sizeW, d.sizeT);
        for (int w = 0; w < d.sizeW; w++) {
            for (int t = 0; t < d.sizeT; t++) {
                min = s.minimum[w][t];
                max = s.maximum[w][t];
                if (t == 0) {
                    gMin = min;
                    gMax = max;
                } else {
                    gMin = Math.min(gMin, min);
                    gMax = Math.max(gMax, max);
                }
                
                ps.setEntry(w, t, min, max); 
            }
            ps.setGlobalEntry(w, gMin, gMax);
        }
        return ps;
    }
	
	/**
	 * Loads metadata associated to the pixels set.
	 * 
     * @param context   The container's registry.
	 * @throws MetadataSourceException If an error occurs while retrieving the
	 * 									data from the source repository.
	 */
	private void load(Registry context)
		throws MetadataSourceException
	{
		DataManagementService dms = context.getDataManagementService();
		PixelsService ps = context.getPixelsService();
		StackStatistics stackStats;
		PixelsDescription desc;
		try {
			//Retrieve pixels information.
            desc = dms.retrievePixels(pixelsID, imageID);
			pixelType = DataSink.getPixelTypeID(desc.getPixelType());
			pixelsDims = new PixelsDimensions(desc.getSizeX(), desc.getSizeY(),
											desc.getSizeZ(), desc.getSizeC(), 
											desc.getSizeT());
			
            //Retrieve pixels stats and create suitable objects to hold them.
			//TODO: Stats have to be retrieved from STS, not from OMEIS.
            stackStats = ps.getStackStatistics(desc.getPixels());
            pixelsStats = makeStats(stackStats, pixelsDims);
			
			//Retrieve user settings (null if no settings available).
			displayOptions = dms.retrieveRenderingSettings(pixelsID, imageID, 
										pixelType);
            
            //Get parameters to locate pixels data.
            omeisPixelsID = desc.getImageServerID();
            omeisURL = desc.getImageServerUrl();
		} catch (Exception e) {
			throw new MetadataSourceException(
				"Can't retrieve the pixels metadata.", e);
		}
	}
	
    /**
     * Returns the storage type of the pixels within the pixels set.
     * 
     * @return One of the constants defined by {@link DataSink}.
     */
    public int getPixelType() { return pixelType; }
    
    /**
     * Returns the dimensions of the pixels set.
     * 
     * @return See above.
     */
    public PixelsDimensions getPixelsDims() { return pixelsDims; }

    /**
     * Returns various statistics calculated on the pixels set.
     * 
     * @return See above.
     */
    public PixelsStats getPixelsStats() { return pixelsStats; }
    
    /**
     * Returns the rendering settings (if any) associated with the pixels set.
     * These are specific to a given user.
     *
     * @return The rendering setting for the current user or <code>null</code>
     *          if the metadata repository contains no such settings.
     */
	public RenderingDef getDisplayOptions() { return displayOptions; }

    /**
     * Returns the id of the pixels set under <i>OMEIS</i>.
     * 
     * @return  See above.
     */
	public long getOmeisPixelsID() { return omeisPixelsID; }
    
    /**
     * Returns a <i>URL</i> pointing to the <i>OMEIS</i> instance that manages 
     * the pixels set.
     * 
     * @return See above.
     */
    public String getOmeisURL() { return omeisURL; }

}
