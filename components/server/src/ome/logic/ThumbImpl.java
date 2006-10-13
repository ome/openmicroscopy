/*
 * ome.logic.QueryImpl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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

package ome.logic;

//Java imports
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.interceptor.Interceptors;

//Third-party libraries
import org.springframework.transaction.annotation.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;

//Application-internal dependencies
import ome.api.IScale;
import ome.api.IThumb;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.ResourceError;
import ome.io.nio.ThumbnailService;
import ome.model.core.Pixels;
import ome.model.display.RenderingDef;
import ome.model.display.Thumbnail;
import ome.parameters.Parameters;
import omeis.providers.re.RGBBuffer;
import omeis.providers.re.RenderingEngine;
import omeis.providers.re.data.PlaneDef;

/** 
 * Provides methods for directly querying object graphs.
 * 
 * @author  Chris Allan &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @version 3.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 3.0
 * 
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional
@Stateless
@Remote(IThumb.class)
@RemoteBinding (jndiBinding="omero/remote/ome.api.IThumb")
@Local(IThumb.class)
@LocalBinding (jndiBinding="omero/local/ome.api.IThumb")
@SecurityDomain("OmeroSecurity")
@Interceptors({SimpleLifecycle.class})
public class ThumbImpl extends AbstractLevel2Service implements IThumb
{
	/** The logger for this class. */
	private static Log log = LogFactory.getLog(ThumbImpl.class);
	
	/** The rendering engine that this service uses for thumbnail creation. */
	private RenderingEngine re;
	
	/** The scaling service that will be used to scale buffered images. */
	private IScale iScale;
	
	/** The ROMIO thumbnail service. */
	private ThumbnailService ioService;
	
	/** The default X-width for a thumbnail. */
	public static final int DEFAULT_X_WIDTH = 48;
	
	/** The default Y-width for a thumbnail. */
	public static final int DEFAULT_Y_WIDTH = 48;
	
	/** The default compression quality (85%). */
	public static final float DEFAULT_COMPRESSION_QUALITY = 0.85F;
	
	/**
	 * Rendering Engine Bean injector.
	 * @param re a <code>RenderingEngine</code>.
	 */
	public void setRenderingEngine(RenderingEngine re)
	{
		this.re = re;
	}
	
	/**
	 * Scale service Bean injector.
	 * @param iScale an <code>IScale</code>.
	 */
	public void setScaleService(IScale iScale)
	{
		this.iScale = iScale;
	}
	
	/**
	 * I/O service (ThumbnailService) Bean injector.
	 * @param ioService a <code>ThumbnailService</code>.
	 */
	public void setIoService(ThumbnailService ioService)
	{
		this.ioService = ioService;
	}
	
	@Override
	protected final Class<? extends ServiceInterface> getServiceInterface()
	{
		return IThumb.class;
	}
	
	/**
	 * Creates a buffered image from a rendering engine RGB buffer without data
	 * copying.
	 * @param buf the rendering engine RGB buffer.
	 * @param sizeX the X-width of the image rendered.
	 * @param sizeY the Y-width of the image rendered.
	 * @return a buffered image wrapping <i>buf</i> with the X-Y dimensions
	 * provided.
	 */
	private BufferedImage createBufferedImage(RGBBuffer buf,
	                                          int sizeX, int sizeY)
	{
		RGBByteBuffer j2DBuf = new RGBByteBuffer(buf, sizeX, sizeY);
		BandedSampleModel csm =
			new BandedSampleModel(DataBuffer.TYPE_BYTE, sizeX, sizeY, 3);
		BufferedImage image =
			new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
		image.setData(Raster.createWritableRaster(csm, j2DBuf, null));
		return image;
		
		/*
		Unfortunately, the following creates "CUSTOM" buffered images which
		with some VM's cannot be used in the ImagingLib. Nasty.
		
		ColorModel cm = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB), 
				null, false, false, Transparency.OPAQUE, 
				DataBuffer.TYPE_BYTE);
		WritableRaster r = Raster.createWritableRaster(csm, j2DBuf, null);
		return new BufferedImage(cm, r, false, null);
		*/
	}
	
    /**
     * Compresses a buffered image thumbnail to an output stream.
     * @param image the thumbnail's buffered image.
     * @param stream the stream to write to.
     * @throws IOException if there is a problem when writing to <i>stream<i>.
     */
    private void compressThumbnailToStream(BufferedImage image,
                                           OutputStream stream)
    	throws IOException
    {
		// Get a JPEG image writer
		ImageWriter jpegWriter = 
			ImageIO.getImageWritersByFormatName("jpeg").next();
		
		// Setup the compression value from (0.05, 0.75 and 0.95)
		ImageWriteParam iwp = jpegWriter.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(DEFAULT_COMPRESSION_QUALITY);
		
		// Write the JPEG to our ByteArray stream
		ImageOutputStream imageOutputStream =
			ImageIO.createImageOutputStream(stream);
		jpegWriter.setOutput(imageOutputStream);
		jpegWriter.write(null, new IIOImage(image, null, null), iwp);
    }
    
    /**
     * Compresses a buffered image thumbnail to disk.
     * @param thumb the thumbnail metadata.
     * @param image the thumbnail's buffered image.
     * @throws IOException if there is a problem writing to disk.
     */ 
    private void compressThumbnailToDisk(Thumbnail thumb, BufferedImage image)
    	throws IOException
    {
    	FileOutputStream stream = ioService.getThumbnailOutputStream(thumb);
    	compressThumbnailToStream(image, stream);
    }
    
    /**
     * Retrieves metadata for a thumbnail given its pixels set and X-Y
     * dimensions.
     * @param p the pixels set the thumbnail is of.
     * @param sizeX the X-width of the thumbnail.
     * @param sizeY the Y-width of the thumbnail.
     * @return the thumbnail metadata. <code>null</code> if the object does not
     * exist.
     */
    private Thumbnail getThumbnailMetadata(Pixels p, int sizeX, int sizeY)
    {
    	// FIXME: We need dimensions here.
    	Thumbnail thumb = (Thumbnail)
    		iQuery.findByQuery("select t from Thumbnail as t where t.pixels.id = :id",
    				new Parameters().addId(p.getId()));
    	return thumb;
    }
    
    /**
     * Creates metadata for a thumbnail given its pixels set and X-Y dimensions.
     * @param p the pixels set the thumbnail is of.
     * @param sizeX the X-width of the thumbnail.
     * @param sizeY the Y-width of the thumbnail.
     * @return the thumbnail metadata as created.
     * @see getThumbnailMetadata()
     */
    private Thumbnail createThumbnailMetadata(Pixels p, int sizeX, int sizeY)
    {
    	Thumbnail thumb = new Thumbnail();
		thumb.setPixels(p);
		thumb.setMimeType("image");  // FIXME: Hack
		thumb.setSizeX(sizeX);
		thumb.setSizeY(sizeY);
		return iUpdate.saveAndReturnObject(thumb);
    }
    
    /**
     * Initializes the rendering engine.
     * @param pixels the pixels set to load.
     */
    private void initializeRenderingEngine(Pixels pixels)
    {
		re.lookupPixels(pixels.getId());
		re.lookupRenderingDef(pixels.getId());
		re.load();
    }
    
    /**
     * Checks that sizeX and sizeY are not out of range for a given pixels set.
     * @param pixels the pixels set.
     * @param sizeX the X-width for the requested thumbnail.
     * @param sizeY the Y-width for the requested thumbnail.
     */
    private void sanityCheckThumbnailSizes(Pixels pixels, int sizeX, int sizeY)
    {
		// Sanity checks
		if (sizeX > pixels.getSizeX())
			throw new ApiUsageException("sizeX > pixels.sizeX");
		if (sizeX < 0)
			throw new ApiUsageException("sizeX is negative");
		if (sizeY > pixels.getSizeY())
			throw new ApiUsageException("sizeY > pixels.sizeY");
		if (sizeY < 0)
			throw new ApiUsageException("sizeY is negative");    	
    }
    
    /**
     * Creates a scaled buffered image from a give pixels set.
     * @param pixels the pixels set.
     * @param def the rendering settings to use for buffered image creation.
     * @param sizeX the X-width of the requested, scaled image.
     * @param sizeY the Y-width of the requested, scaled image.
     * @return a scaled buffered image.
     */
    private BufferedImage createScaledImage(Pixels pixels, RenderingDef def,
                                            Integer sizeX, Integer sizeY)
    {
		// Original sizes and thumbnail metadata
		int origSizeX = pixels.getSizeX();
		int origSizeY = pixels.getSizeY();
		
		// Retrieve our rendered data and translate to a buffered image
		initializeRenderingEngine(pixels);
		PlaneDef pd = new PlaneDef(PlaneDef.XY, re.getDefaultT());
		pd.setZ(re.getDefaultZ());
		RGBBuffer buf = re.render(pd);
		BufferedImage image = createBufferedImage(buf, origSizeX, origSizeY);
		
		// Finally, scale our image using scaling factors (percentage).
		log.info("Setting xScale factor: " + sizeX + "/" + origSizeX);
		float xScale = (float) sizeX / origSizeX;
		log.info("Setting yScale factor: " + sizeX + "/" + origSizeX);
		float yScale = (float) sizeY / origSizeY;
		return iScale.scaleBufferedImage(image, xScale, yScale);
    }
    
	/* (non-Javadoc)
	 * @see ome.api.IThumb#createThumbnail(ome.model.core.Pixels, ome.model.display.RenderingDef, java.lang.Integer, java.lang.Integer)
	 */
	public void createThumbnail(Pixels pixels, RenderingDef def,
	                            Integer sizeX, Integer sizeY)
	{
		// Set defaults and sanity check thumbnail sizes
		if (sizeX == null)
			sizeX = DEFAULT_X_WIDTH;
		if (sizeY == null)
			sizeY = DEFAULT_Y_WIDTH;
		sanityCheckThumbnailSizes(pixels, sizeX, sizeY);

		Thumbnail metadata = getThumbnailMetadata(pixels, sizeX, sizeY);
		if (metadata == null)
			metadata = createThumbnailMetadata(pixels, sizeX, sizeY);
		
		BufferedImage image = createScaledImage(pixels, def, sizeX, sizeY);
		try
		{
			compressThumbnailToDisk(metadata, image);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new ResourceError(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see ome.api.IThumb#getThumbnail(ome.model.core.Pixels, ome.model.display.RenderingDef, java.lang.Integer, java.lang.Integer)
	 */
	public byte[] getThumbnail(Pixels pixels, RenderingDef def,
	                           Integer sizeX, Integer sizeY)
	{
		// Set defaults and sanity check thumbnail sizes
		if (sizeX == null)
			sizeX = DEFAULT_X_WIDTH;
		if (sizeY == null)
			sizeY = DEFAULT_Y_WIDTH;
		sanityCheckThumbnailSizes(pixels, sizeX, sizeY);
		
		try
		{		
			Thumbnail metadata = getThumbnailMetadata(pixels, sizeX, sizeY);
			if (metadata == null)
			{
				// First create a scaled buffered image
				BufferedImage image =
					createScaledImage(pixels, def, sizeX, sizeY);
				
				// Now write it to the disk cache and return what we've written
				metadata = createThumbnailMetadata(pixels, sizeX, sizeY);
				compressThumbnailToDisk(metadata, image);
				return ioService.getThumbnail(metadata);
			}
			return ioService.getThumbnail(metadata);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new ResourceError(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see ome.api.IThumb#getThumbnailDirect(ome.model.core.Pixels, ome.model.display.RenderingDef, java.lang.Integer, java.lang.Integer)
	 */
	@Transactional( readOnly = true )
	public byte[] getThumbnailDirect(Pixels pixels, RenderingDef def,
	                                 Integer sizeX, Integer sizeY)
	{
		// Set defaults and sanity check thumbnail sizes
		if (sizeX == null)
			sizeX = DEFAULT_X_WIDTH;
		if (sizeY == null)
			sizeY = DEFAULT_Y_WIDTH;
		sanityCheckThumbnailSizes(pixels, sizeX, sizeY);
		
		BufferedImage image = createScaledImage(pixels, def, sizeX, sizeY);
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try
		{
			compressThumbnailToStream(image, byteStream);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new ResourceError(e.getMessage());
		}
		return byteStream.toByteArray();
	}

	/* (non-Javadoc)
	 * @see ome.api.IThumb#thumbnailExists(ome.model.core.Pixels, java.lang.Integer, java.lang.Integer)
	 */
	@Transactional( readOnly = false )
	public boolean thumbnailExists(Pixels pixels, Integer sizeX, Integer sizeY)
	{
		// Set defaults and sanity check thumbnail sizes
		if (sizeX == null)
			sizeX = DEFAULT_X_WIDTH;
		if (sizeY == null)
			sizeY = DEFAULT_Y_WIDTH;
		sanityCheckThumbnailSizes(pixels, sizeX, sizeY);
		
		Thumbnail thumb = getThumbnailMetadata(pixels, sizeX, sizeY);
		if (thumb == null)
			return false;
		return true;
	}

}
				
