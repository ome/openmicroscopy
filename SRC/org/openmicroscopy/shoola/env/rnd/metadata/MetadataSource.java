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
 * Manages access to the metadata associated to a pixels set within a
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
	
	/** The id of the image this object is dealing with. */
	private int		imageID;
	
	/** The id of the pixels set within the image. */
	private int		pixelsID;
	
	/** The id of the pixels set under <i>OMEIS</i>.*/
	private long	omeisPixelsID;
	
	/** The pixel type identifier, as defined by {@link DataSink}. */
	private int		pixelType;
	
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
	 * The rendering settings (if any) associated to the pixels set.
	 * These are specific to a given user.
	 * If the metadata repository contains no such settings for the current
	 * user, this field will be left <code>null<code>.
	 */
	private RenderingDef		displayOptions;
	
	/** The container's registry. */
	private Registry			context;	
	
	
	/**
	 * Creates a new instance to handle the pixels metadata.
	 * A call to {@link #load()} will retrieve the metadata which can then be
	 * accessed via the various <code>getXXX</code> methods.
	 * 
	 * @param imageID The id of the image whom the pixels set belongs to.
	 * @param pixelsID The id of the pixels set.
	 * @param context The container's registry.
	 */
	public MetadataSource(int imageID, int pixelsID, Registry context)
	{
		this.imageID = imageID;
		this.pixelsID = pixelsID;
		this.context = context;
	}
	
	/**
	 * Loads metadata associated to the pixels set.
	 * 
	 * @throws MetadataSourceException If an error occurs while retrieving the
	 * 									data from the source repository.
	 */
	public void load()
		throws MetadataSourceException
	{
		DataManagementService dms = context.getDataManagementService();
		PixelsService ps = context.getPixelsService();
		StackStatistics stackStats;
		PixelsDescription desc;
		try {
			desc = dms.retrievePixels(pixelsID);
			stackStats = ps.getStackStatistics(desc.getPixels());
			//TODO: tmp hack; stats have to be retrieved from STS and desc
			//shouldn't contain Pixels.
			omeisPixelsID = desc.getImageServerID();
			pixelType = DataSink.getPixelTypeID(desc.getPixelType());
			pixelsDims = new PixelsDimensions(desc.getSizeX(), desc.getSizeY(),
											desc.getSizeZ(), desc.getSizeC(), 
											desc.getSizeT());
			pixelsStats = makeStats(stackStats, pixelsDims);
		} catch (Exception e) {
			throw new MetadataSourceException(
				"Can't retrieve the pixels metadata.", e);
		}
	}

	public RenderingDef getDisplayOptions()
	{
		return displayOptions;
	}

	public long getOmeisPixelsID()
	{
		return omeisPixelsID;
	}

	public PixelsDimensions getPixelsDims()
	{
		return pixelsDims;
	}

	public PixelsStats getPixelsStats()
	{
		return pixelsStats;
	}

	public int getPixelType() 
	{
		return pixelType;
	}
	
	private PixelsStats makeStats(StackStatistics s, PixelsDimensions d)
	{
		//TODO: implement.
		return null;
	}

}
