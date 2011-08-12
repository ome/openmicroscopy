/*
 * org.openmicroscopy.shoola.env.data.OmeroImageServiceImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data;


//Java import
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

//Third-party libraries
import loci.formats.ImageReader;
import com.sun.opengl.util.texture.TextureData;

//Application-internal dependencies
import omero.api.RenderingEnginePrx;
import omero.model.Annotation;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.RenderingDef;
import omero.model.Screen;
import omero.model.ScreenI;
import omero.model.TagAnnotation;
import omero.romio.PlaneDef;
import omero.sys.Parameters;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.env.data.model.SaveAsParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.PixelsServicesFactory;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.image.io.WriterImage;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.ROIData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.WorkflowData;

/** 
* Implementation of the {@link OmeroImageService} I/F.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $ $Date: $)
* </small>
* @since OME3.0
*/
class OmeroImageServiceImpl
 	implements OmeroImageService
{

	/** The collection of supported file filters. */
	private FileFilter[]		filters;
	
	/** Uses it to gain access to the container's services. */
	private Registry                context;

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private OMEROGateway            gateway;
	
	/**
	 * Imports the specified candidates.
	 * 
	 * @param candidates The file to import.
	 * @param status The original status.
	 * @param object The object hosting information about the import.
	 * @param archived Pass <code>true</code> if the original file had to be
	 * 					archived, <code>false</code> otherwise.
	 * @param ioList The containers where to import the files.
	 * @param list   The list of annotations.
	 * @param userID The identifier of the user.
	 */
	private Boolean importCandidates(Map<File, StatusLabel> files, StatusLabel status, 
			ImportableObject object, boolean archived, IObject ioContainer, 
			List<Annotation> list, long userID, boolean close)
	{
		if (status.isMarkedAsCancel()) {
			gateway.closeImport();
			return Boolean.valueOf(false);
		}
		boolean thumbnail = object.isLoadThumbnail();
		Entry entry;
		Iterator jj = files.entrySet().iterator();
		StatusLabel label = null;
		//boolean archived = object.isArchivedFile(file);
		File file;
		Object result;
		List<ImageData> images = new ArrayList<ImageData>();
		ImageData image;
		Set<ImageData> ll;
		Iterator<ImageData> kk;
		List<Object> converted;
		boolean toClose = false;
		int n = files.size()-1;
		int index = 0;
		while (jj.hasNext()) {
			entry = (Entry) jj.next();
			file = (File) entry.getKey();
			if (ImportableObject.isHCSFile(file)) {
				if (ioContainer != null && 
					!(ioContainer.getClass().equals(Screen.class) ||
					ioContainer.getClass().equals(ScreenI.class)))
					ioContainer = null;
			}
			label = (StatusLabel) entry.getValue();
			if (close) {
				toClose = index == n;
				index++;
			}
			if (!label.isMarkedAsCancel()) {
				try {
					result = gateway.importImage(object, ioContainer, file,
							label, archived, toClose);
					if (result instanceof ImageData) {
						image = (ImageData) result;
						images.add(image);
						if (thumbnail)
							label.setFile(file, 
									createImportedImage(userID, image));
						else label.setFile(file, image);
					} else if (result instanceof Set) {
						ll = (Set<ImageData>) result;
						annotatedImportedImage(list, ll);
						images.addAll(ll);
						kk = ll.iterator();
						converted = new ArrayList<Object>(ll.size());
						while (kk.hasNext()) {
							image = kk.next();
							if (thumbnail)
								converted.add(
										createImportedImage(userID, image));
							else converted.add(image);
						}
						label.setFile(file, converted);
					} else label.setFile(file, result);
				} catch (ImportException e) {
					label.setFile(file, e);
				}
			}
		}
		annotatedImportedImage(list, images);
		if (close) gateway.closeImport();
		return null;
	}
	
	/**
	 * Annotates the imported images.
	 * 
	 * @param annotations The annotations to add.
	 * @param images The imported images.
	 */
	private void annotatedImportedImage(List<Annotation> annotations, 
			Collection images)
	{
		if (annotations.size() == 0 || images.size() == 0) return;
		Iterator i = images.iterator();
		ImageData image;
		Iterator<Annotation> j;
		List<IObject> list = new ArrayList<IObject>();
		IObject io;
		while (i.hasNext()) {
			image = (ImageData) i.next();
			j = annotations.iterator();
			while (j.hasNext()) {
				io = ModelMapper.linkAnnotation(image.asIObject(), j.next());
				if (io != null)
					list.add(io);
			}
		}
		if (list.size() == 0) return;
		try {
			gateway.saveAndReturnObject(list, new HashMap());
		} catch (Exception e) {
			//ignore 
		}
	}
	
	/**
	 * Creates a thumbnail for the imported image.
	 * 
	 * @param userID  The identifier of the user.
	 * @param image   The image to handle.
	 * @return See above.
	 */
	private Object createImportedImage(long userID, ImageData image)
	{
		if (image != null) {
			ThumbnailData data;
			try {
				PixelsData pix = image.getDefaultPixels();
				BufferedImage img = createImage(
						gateway.getThumbnailByLongestSide(pix.getId(),
								Factory.THUMB_DEFAULT_WIDTH));
				data = new ThumbnailData(image.getId(), img, userID, true);
				data.setImage(image);
				return data;
			} catch (Exception e) {
				data = new ThumbnailData(image.getId(), 
						createDefaultImage(image), userID, false);
				data.setImage(image);
				data.setError(e);
				return data;
			}
		}
		return image;
	}
	
	/**
	 * Formats the result of an image import.
	 * 
	 * @param image The image to handle.
	 * @param userID The user's id.
	 * @param thumbnail Pass <code>true</code> if thumbnail has to be created,
	 * 					<code>false</code> otherwise.
	 * @return See above.
	 */
	private Object formatResult(ImageData image, long userID, boolean thumbnail)
	{
		Boolean backoff = null;
		try {
			backoff = gateway.isLargeImage(
					image.getDefaultPixels().asPixels());
		} catch (Exception e) {}
		//if (backoff != null && backoff.booleanValue())
		//	return new ThumbnailData(image, backoff);
		if (thumbnail) {
			ThumbnailData thumb = (ThumbnailData) createImportedImage(userID, 
					image);
			thumb.setBackOffForPyramid(backoff);
			return thumb;
		} 
		return image;
	}
	
	
	/**
	 * Returns <code>true</code> if the binary data are available, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean isBinaryAvailable()
	{
		Boolean b = (Boolean) context.lookup(LookupNames.BINARY_AVAILABLE);
		if (b == null) return true;
		return b.booleanValue();
	}
	
	/**
	 * Creates a <code>BufferedImage</code> from the passed array of bytes.
	 * 
	 * @param values    The array of bytes.
	 * @return See above.
	 * @throws RenderingServiceException If we cannot create an image.
	 */
	private BufferedImage createImage(byte[] values) 
		throws RenderingServiceException
	{
		try {
			return WriterImage.bytesToImage(values);
		} catch (Exception e) {
			throw new RenderingServiceException("Cannot create buffered image",
					e);
		}
	}
	
    /**
     * Creates a default thumbnail for the passed image.
     * 
     * @param data The image to handle.
     * @return See above.
     */
    private BufferedImage createDefaultImage(ImageData data) 
    {
    	PixelsData pxd = null;
        try {
        	pxd = data.getDefaultPixels();
		} catch (Exception e) {} //something went wrong during import
        if (pxd == null)
        	return Factory.createDefaultImageThumbnail(-1);
        Dimension d = Factory.computeThumbnailSize(Factory.THUMB_DEFAULT_WIDTH,
        		Factory.THUMB_DEFAULT_HEIGHT, pxd.getSizeX(), pxd.getSizeY());
        return Factory.createDefaultImageThumbnail(d.width, d.height);
    }
    
	/**
	 * Creates a <code>BufferedImage</code> from the passed array of bytes.
	 * 
	 * @param values    The array of bytes.
	 * @return See above.
	 * @throws RenderingServiceException If we cannot create an image.
	 */
	private BufferedImage createImage(String path) 
		throws FSAccessException
	{
		try {
			return ImageIO.read(new File(path));
		} catch (Exception e) {
			throw new FSAccessException("Cannot create buffered image",
					e);
		}
	} 

	/**
	 * Creates a new instance.
	 * 
	 * @param gateway   Reference to the OMERO entry point.
	 *                  Mustn't be <code>null</code>.
	 * @param registry  Reference to the registry. Mustn't be <code>null</code>.
	 */
	OmeroImageServiceImpl(OMEROGateway gateway, Registry registry)
	{
		if (registry == null)
			throw new IllegalArgumentException("No registry.");
		if (gateway == null)
			throw new IllegalArgumentException("No gateway.");
		context = registry;
		this.gateway = gateway;
	}

	/** Shuts down all active rendering engines. */
	void shutDown()
	{
		PixelsServicesFactory.shutDownRenderingControls(context);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadRenderingControl(long)
	 */
	public RenderingControl loadRenderingControl(long pixelsID)
		throws DSOutOfServiceException, DSAccessException, FSAccessException
	{
		RenderingControl proxy = 
			PixelsServicesFactory.getRenderingControl(context, 
					Long.valueOf(pixelsID), true);
		if (proxy == null) {
			UserCredentials uc = 
				(UserCredentials) context.lookup(LookupNames.USER_CREDENTIALS);
			int compressionLevel;
			switch (uc.getSpeedLevel()) {
				case UserCredentials.MEDIUM:
					compressionLevel = RenderingControl.MEDIUM;
					break;
				case UserCredentials.LOW:
					compressionLevel = RenderingControl.LOW;
					break;
				default:
					compressionLevel = RenderingControl.UNCOMPRESSED;
			}
			ExperimenterData exp = (ExperimenterData) context.lookup(
					LookupNames.CURRENT_USER_DETAILS);
			RenderingEnginePrx re = gateway.createRenderingEngine(pixelsID);
			Pixels pixels = gateway.getPixels(pixelsID);
			List<RndProxyDef> defs = gateway.getRenderingSettingsFor(pixelsID, 
					exp.getId());
			Collection l = pixels.copyChannels();
			Iterator i = l.iterator();
			List<ChannelData> m = new ArrayList<ChannelData>(l.size());
			int index = 0;
			while (i.hasNext()) {
				m.add(new ChannelData(index, (Channel) i.next()));
				index++;
			}
			
			proxy = PixelsServicesFactory.createRenderingControl(context, re,
					pixels, m, compressionLevel, defs);
		}
		return proxy;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#renderImage(long, PlaneDef, boolean, boolean)
	 */
	public Object renderImage(long pixelsID, PlaneDef pDef, boolean asTexture,
			boolean largeImage)
		throws RenderingServiceException
	{
		try {
			//if (!largeImage)
				return PixelsServicesFactory.render(context, 
						Long.valueOf(pixelsID), pDef, asTexture);
			/*
			List<Long> ids = new ArrayList<Long>();
			ids.add(pixelsID);
			int w = pDef.x;
			int h = pDef.y;
			int max = w;
			if (max < h) max = h;
			if (max > RenderingControl.MAX_SIZE_THREE) 
				max = RenderingControl.MAX_SIZE_THREE;
			Map m = gateway.getThumbnailSet(ids, max, true);
			byte[] values = (byte[]) m.get(pixelsID);
			if (asTexture) {
				values = WriterImage.bytesToBytes(values);
				return PixelsServicesFactory.createTexture(values, w, h);
			} else {
				return createImage(values);
			}
			*/
		} catch (Exception e) {
			throw new RenderingServiceException("RenderImage", e);
		}
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#shutDown(long)
	 */
	public void shutDown(long pixelsID)
	{
		if (!PixelsServicesFactory.shutDownRenderingControl(context, pixelsID))
			gateway.removeREService(pixelsID);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getThumbnail(long, int, int, long)
	 */
	public BufferedImage getThumbnail(long pixID, int sizeX, int sizeY, 
									long userID)
		throws RenderingServiceException
	{
		try {
			if (pixID < 0) return null;
			if (!isBinaryAvailable()) return null;
			return createImage(gateway.getThumbnail(pixID, sizeX, sizeY, 
								userID));
		} catch (Exception e) {
			if (e instanceof DSOutOfServiceException) {
				context.getLogger().error(this, e.getMessage());
				return null;//getThumbnail(pixID, sizeX, sizeY, userID);
			}
			throw new RenderingServiceException("Get Thumbnail", e);
		}
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getThumbnailSet(List, int)
	 */
	public Map<Long, BufferedImage> getThumbnailSet(List pixelsID, int max)
		throws RenderingServiceException
	{
		Map<Long, BufferedImage> r = new HashMap<Long, BufferedImage>();
		
		List<Long> ids = new ArrayList<Long>();
		Iterator i;
		try {
			if (pixelsID == null || pixelsID.size() == 0) return r;
			Iterator j = pixelsID.iterator();
			long id;
			if (!isBinaryAvailable()) {
				while (j.hasNext()) {
					id = (Long) j.next();
					r.put(id, null);
				}
				return r;
			}
			List blocks = new ArrayList();
			int index = 0;
			List l = null;
			while (j.hasNext()) {
				if (index == 0) {
					l = new ArrayList();
				}
				l.add(j.next());
				index++;
				if (index == OMEROGateway.MAX_RETRIEVAL) {
					blocks.add(l);
					index = 0;
				}
			}
			if (l != null && l.size() > 0)
				blocks.add(l);
			ids.addAll(pixelsID);
			j = blocks.iterator();
			Map m = new HashMap();
			Map map;
			while (j.hasNext()) {
				map = gateway.getThumbnailSet((List) j.next(), max, false);
				m.putAll(map);
			}
			//m = gateway.getThumbnailSet(pixelsID, max, false);
			if (m == null || m.size() == 0) {
				i = ids.iterator();
				while (i.hasNext()) 
					r.put((Long) i.next(), null);
				return r;
			}
			i = m.keySet().iterator();
			
			byte[] values;
			while (i.hasNext()) {
				id = (Long) i.next();
				values = (byte[]) m.get(id);
				ids.remove(id);
				if (values == null || values.length == 0)
					r.put(id, null);
				else {
					try {
						r.put(id, createImage(values));
					} catch (Exception e) {
						r.put(id, null);
					}
				}
			}
			//could not get a thumbnail for remaining images
			if (ids.size() > 0) { 
				i = ids.iterator();
				while (i.hasNext()) 
					r.put((Long) i.next(), null);
			}
			return r;
		} catch (Exception e) {
			context.getLogger().error(this, e.getMessage());
			if (ids.size() > 0) { 
				i = ids.iterator();
				while (i.hasNext()) 
					r.put((Long) i.next(), null);
			} 
			return r;
		}
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#reloadRenderingService(long)
	 */
	public RenderingControl reloadRenderingService(long pixelsID)
		throws RenderingServiceException
	{
		RenderingControl proxy = 
			PixelsServicesFactory.getRenderingControl(context, 
					Long.valueOf(pixelsID), false);
		if (proxy == null) return null;
		try {
			RenderingEnginePrx re = gateway.createRenderingEngine(pixelsID);
			return PixelsServicesFactory.reloadRenderingControl(context, 
					pixelsID, re);
		} catch (Exception e) {
			throw new RenderingServiceException("Cannot restart the " +
					"rendering engine for : "+pixelsID, e);
		}
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#resetRenderingService(long)
	 */
	public RenderingControl resetRenderingService(long pixelsID)
		throws RenderingServiceException
	{
		RenderingControl proxy = 
			PixelsServicesFactory.getRenderingControl(context, 
					Long.valueOf(pixelsID), false);
		if (proxy == null) return null;
		try {
			RenderingEnginePrx re = gateway.createRenderingEngine(pixelsID);
			ExperimenterData exp = (ExperimenterData) context.lookup(
					LookupNames.CURRENT_USER_DETAILS);
			RenderingDef def = gateway.getRenderingDef(pixelsID, exp.getId());
			return PixelsServicesFactory.resetRenderingControl(context, 
					pixelsID, re, def);
		} catch (Exception e) {
			throw new RenderingServiceException("Cannot restart the " +
					"rendering engine for : "+pixelsID, e);
		}
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadPixels(long)
	 */
	public PixelsData loadPixels(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (pixelsID < 0) 
			throw new IllegalArgumentException("Pixels' ID not valid.");
		return (PixelsData) PojoMapper.asDataObject(
				gateway.getPixels(pixelsID));
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getPlane(long, int, int, int)
	 */
	public byte[] getPlane(long pixelsID, int z, int t, int c)
		throws DSOutOfServiceException, DSAccessException, FSAccessException
	{
		if (pixelsID < 0) 
			throw new IllegalArgumentException("Pixels' ID not valid.");
		return gateway.getPlane(pixelsID, z, t, c);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#pasteRenderingSettings(long, Class, List)
	 */
	public Map pasteRenderingSettings(long pixelsID, Class rootNodeType, 
			List nodesID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodesID == null || nodesID.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		return gateway.pasteRenderingSettings(pixelsID, rootNodeType, nodesID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#resetRenderingSettings(Class, List)
	 */
	public Map resetRenderingSettings(Class rootNodeType, List nodesID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodesID == null || nodesID.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		return gateway.resetRenderingSettings(rootNodeType, nodesID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#setMinMaxSettings(Class, List)
	 */
	public Map setMinMaxSettings(Class rootNodeType, List nodesID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodesID == null || nodesID.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		return gateway.setMinMaxSettings(rootNodeType, nodesID);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#setOwnerRenderingSettings(Class, List)
	 */
	public Map setOwnerRenderingSettings(Class rootNodeType, List nodesID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodesID == null || nodesID.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		return gateway.setOwnerRenderingSettings(rootNodeType, nodesID);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getRenderingSettings(long, long)
	 */
	public Map getRenderingSettings(long pixelsID, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getRenderingSettings(pixelsID, userID);
		/*
		Map m = gateway.getRenderingSettings(pixelsID, userID);
		if (m == null) return null;
		Iterator i = m.keySet().iterator();
		Object key;
		Map results = new HashMap(m.size());
		while (i.hasNext()) {
			key = i.next();
			results.put(key, 
					PixelsServicesFactory.convert((RenderingDef) m.get(key)));
		}
		return results;
		*/
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getRenderingSettingsFor(long, long)
	 */
	public List<RndProxyDef> getRenderingSettingsFor(long pixelsID, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getRenderingSettingsFor(pixelsID, userID);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#renderProjected(long, int, int, int, int, List)
	 */
	public BufferedImage renderProjected(long pixelsID, int startZ, int endZ, 
			 int stepping, int type, List<Integer> channels)
		throws RenderingServiceException, DSOutOfServiceException
	{
		return PixelsServicesFactory.renderProjected(context, pixelsID, startZ,
				endZ, type, stepping, channels);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#renderProjectedAsTexture(long, int, int, int, int, 
	 * List)
	 */
	public TextureData renderProjectedAsTexture(long pixelsID, int startZ, 
			int endZ, int stepping, int type, List<Integer> channels)
		throws RenderingServiceException, DSOutOfServiceException
	{
		return PixelsServicesFactory.renderProjectedAsTexture(context, 
				pixelsID, startZ, endZ, type, stepping, channels);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#projectImage(ProjectionParam)
	 */
	public ImageData projectImage(ProjectionParam ref)
		throws DSOutOfServiceException, DSAccessException
	{
		if (ref == null) return null;
		ImageData image = gateway.projectImage(ref.getPixelsID(), 
				ref.getStartT(), ref.getEndT(), ref.getStartZ(), 
				ref.getEndZ(), ref.getStepping(), ref.getProjectionType(), 
				ref.getChannels(), ref.getName(), ref.getPixelsType());
		if (image == null) return null;
		Image img = image.asImage();
		img.setDescription(omero.rtypes.rstring(ref.getDescription()));
		image = (ImageData) 
			PojoMapper.asDataObject(gateway.updateObject(img, 
					new Parameters()));
		image = gateway.getImage(image.getId(), new Parameters());
		List<DatasetData> datasets =  ref.getDatasets();
		if (datasets != null && datasets.size() > 0) {
			Iterator<DatasetData> i = datasets.iterator();
			//Check if we need to create a dataset.
			List<DatasetData> existing = new ArrayList<DatasetData>();
			List<DatasetData> toCreate = new ArrayList<DatasetData>();
			DatasetData dataset;
			while (i.hasNext()) {
				dataset = i.next();
				if (dataset.getId() > 0) existing.add(dataset);
				else toCreate.add(dataset);
			}
			if (toCreate.size() > 0) {
				i = toCreate.iterator();
				OmeroDataService svc = context.getDataService();
				while (i.hasNext()) {
					existing.add((DatasetData) svc.createDataObject(i.next(), 
										ref.getDatasetParent(), null));
				} 
			}
			List<IObject> links = new ArrayList<IObject>(datasets.size());
			img = image.asImage();
			IObject l;
			i = existing.iterator();
			while (i.hasNext()) {
				l = ModelMapper.linkParentToChild(img, i.next().asIObject());
				links.add(l);
			}
			gateway.createObjects(links);
		}
		return image;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#createRenderingSettings(long, RndProxyDef, List)
	 */
	public Boolean createRenderingSettings(long pixelsID, RndProxyDef rndToCopy,
			List<Integer> indexes) 
		throws DSOutOfServiceException, DSAccessException, FSAccessException
	{
		if (rndToCopy == null) {
			RenderingDef def = gateway.createRenderingDef(pixelsID);
			return (def != null);
		}
		RenderingControl rndControl = loadRenderingControl(pixelsID);
		try {
			rndControl.copyRenderingSettings(rndToCopy, indexes);
			//save them
			rndControl.saveCurrentSettings();
			//discard it
			shutDown(pixelsID);
		} catch (Exception e) {
			throw new DSAccessException("Unable to copy the " +
					"rendering settings.");
		}
		
		return Boolean.TRUE;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadPlaneInfo(long, int, int, int)
	 */
	public Collection loadPlaneInfo(long pixelsID, int z, int t, int channel)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadPlaneInfo(pixelsID, z, t, channel);
	}
	
	/**
	 * Recycles or creates the container.
	 * 
	 * @param dataset The dataset to create or recycle.
	 * @param container The container to create and link the dataset to.
	 * @param object The object hosting the import option.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IObject determineContainer(DatasetData dataset,
		DataObject container, ImportableObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		IObject ioContainer = null;
		Map parameters = new HashMap();
		DataObject createdData;
		IObject project = null;
		IObject link;
		if (dataset != null) { //dataset
			if (dataset.getId() <= 0) { 
				//Check if it has been already been created.
				//need to create it first
				if (container != null) {
					if (container.getId() <= 0) { 
						//project needs to be created to.
						createdData = object.hasObjectBeenCreated(
								container);
						if (createdData == null) {
							project = gateway.saveAndReturnObject(
									container.asIObject(), parameters);
							//register
							object.addNewDataObject(
									PojoMapper.asDataObject(
									project));
							//now create the dataset
							ioContainer = gateway.saveAndReturnObject(
									dataset.asIObject(), parameters);
							//register
							object.registerDataset(
									project.getId().getValue(),
									(DatasetData) 
									PojoMapper.asDataObject(
									ioContainer));
							link = (ProjectDatasetLink) 
							ModelMapper.linkParentToChild(
									(Dataset) ioContainer, 
									(Project) project);
							link = (ProjectDatasetLink) 
							gateway.saveAndReturnObject(link, 
									parameters);
						} else {
							DatasetData d;
							d = object.isDatasetCreated(
									createdData.getId(), dataset);
							if (d == null) {
								ioContainer = gateway.saveAndReturnObject(
										dataset.asIObject(), parameters);
								//register
								object.registerDataset(
										createdData.getId(),
										(DatasetData) 
										PojoMapper.asDataObject(
										ioContainer));
								link = (ProjectDatasetLink) 
								ModelMapper.linkParentToChild(
										(Dataset) ioContainer, 
										(Project) createdData.asProject());
								link = (ProjectDatasetLink) 
								gateway.saveAndReturnObject(link, 
										parameters);
							} else ioContainer = d.asIObject();
						}
					} else { //project already exists.
						createdData = object.isDatasetCreated(
								container.getId(), dataset);
						if (createdData == null) {
							ioContainer = gateway.saveAndReturnObject(
									dataset.asIObject(), parameters);
							//register
							object.registerDataset(
									container.getId(),
									(DatasetData) 
									PojoMapper.asDataObject(
									ioContainer));
							link = (ProjectDatasetLink) 
							ModelMapper.linkParentToChild(
									(Dataset) ioContainer, 
									(Project) container.asProject());
							link = (ProjectDatasetLink) 
							gateway.saveAndReturnObject(link, 
									parameters);
						} else ioContainer = createdData.asIObject();
					}
				} else { //dataset w/o project.
					createdData = object.hasObjectBeenCreated(dataset);
					if (createdData == null) {
						ioContainer = gateway.saveAndReturnObject(
								dataset.asIObject(), parameters);
						//register
						object.addNewDataObject(PojoMapper.asDataObject(
								ioContainer));
					} else ioContainer = createdData.asIObject();
				}
			} else ioContainer = dataset.asIObject();
		} else { //check on the container.
			if (container != null) {
				if (container.getId() <= 0) { 
					//container needs to be created to.
					createdData = object.hasObjectBeenCreated(
							container);
					if (createdData == null) {
						ioContainer = gateway.saveAndReturnObject(
								container.asIObject(), parameters);
						//register
						object.addNewDataObject(
								PojoMapper.asDataObject(
								project));
					} else {
						ioContainer = createdData.asIObject();
					}
				} else ioContainer = container.asIObject();
			}
		}
		return ioContainer;
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#importFile(ImportableObject, ImportableFile, 
	 * long, long, boolean)
	 */
	public Object importFile(ImportableObject object, ImportableFile importable, 
			long userID, long groupID, boolean close) 
		throws ImportException
	{
		if (importable == null || importable.getFile() == null)
			throw new IllegalArgumentException("No images to import.");
		StatusLabel status = importable.getStatus();
		if (status.isMarkedAsCancel()) {
			gateway.closeImport();
			return Boolean.valueOf(false);
		}
		Object result = null;
		Collection<TagAnnotationData> tags = object.getTags();
		List<Annotation> list = new ArrayList<Annotation>();
		List<IObject> l;
		//Tags
		if (tags != null && tags.size() > 0) {
			List<TagAnnotationData> values = new ArrayList<TagAnnotationData>();
			Iterator<TagAnnotationData> i = tags.iterator();
			TagAnnotationData tag;
			l = new ArrayList<IObject>();
			while (i.hasNext()) {
				tag = i.next();
				if (tag.getId() > 0) {
					values.add(tag);
					list.add((Annotation) tag.asIObject());
				} else l.add(tag.asIObject());
			}
			//save the tag.
			try {
				l = gateway.saveAndReturnObject(l, new HashMap());
				Iterator<IObject> j = l.iterator();
				Annotation a;
				while (j.hasNext()) {
					a = (Annotation) j.next();
					values.add(new TagAnnotationData((TagAnnotation) a));
					list.add(a);
				}
				object.setTags(values);
			} catch (Exception e) {}
		}
		IObject link;
		//prepare the container.
		List<ImageData> images = new ArrayList<ImageData>();
		//IObject io;
		List<IObject> links = new ArrayList<IObject>();
		Set<ImageData> ll;
		ImageData image;
		Iterator<ImageData> kk;
		List<Object> converted;
		List<String> candidates;
		File file = importable.getFile();
		boolean thumbnail = object.isLoadThumbnail();
		DatasetData dataset = importable.getDataset();
		DataObject container = importable.getParent();
		IObject ioContainer = null;
		Map parameters = new HashMap();
		DataObject createdData;
		IObject project = null;
		DataObject folder = null;
		boolean hcsFile;
		if (file.isFile()) {
			hcsFile = ImportableObject.isHCSFile(file);
			//Create the container if required.
			if (hcsFile) {
				boolean b = ImportableObject.isArbitraryFile(file);
				if (b) { //check if it is actually a HCS file.
					candidates = gateway.getImportCandidates(object, file, status);
					if (candidates.size() == 1) { 
						String value = candidates.get(0);
						if (!file.getAbsolutePath().equals(value) && 
							object.isFileinQueue(value)) {
							gateway.closeImport();
							status.markedAsDuplicate();
							return Boolean.valueOf(true);
						}
						if (!file.getName().endsWith(
								ImportableObject.DAT_EXTENSION))
							hcsFile = ImportableObject.isHCSFile(value);
						else hcsFile = false;
					}
				}
			}
			if (hcsFile) {
				dataset = null;
				if (!(container instanceof ScreenData))
					container = null;
			}
			//remove hcs check if we want to create screen from folder.
			if (!hcsFile && importable.isFolderAsContainer()) { 
				//we have to import the image in this container.
				folder = object.createFolderAsContainer(importable, hcsFile);
				if (folder instanceof DatasetData) {
					try {
						ioContainer = determineContainer((DatasetData) folder, 
								container, object);
						status.setContainerFromFolder(PojoMapper.asDataObject(
								ioContainer));
					} catch (Exception e) {
						context.getLogger().error(this, "Cannot create " +
								"the container hosting the images.");
					}
				} else if (folder instanceof ScreenData) {
					try {
						ioContainer = determineContainer(null, folder, object);
						status.setContainerFromFolder(PojoMapper.asDataObject(
								ioContainer));
					} catch (Exception e) {
						context.getLogger().error(this, "Cannot create " +
								"the container hosting the plate.");
					}
				}
			}
			if (folder == null && dataset != null) { //dataset
				try {
					ioContainer = determineContainer(dataset, container,
							object);
				} catch (Exception e) {
					context.getLogger().error(this, "Cannot create " +
					"the container hosting the images.");
				}
			} else { //no dataset specified.
				if (container instanceof ScreenData) {
					if (container.getId() <= 0) {
						//project needs to be created to.
						createdData = object.hasObjectBeenCreated(
								container);
						if (createdData == null) {
							try {
								ioContainer = gateway.saveAndReturnObject(
										container.asIObject(), parameters);
								//register
								object.addNewDataObject(
										PojoMapper.asDataObject(
												ioContainer));
							} catch (Exception e) {
								context.getLogger().error(this, 
										"Cannot create the Screen hosting " +
										"the data.");
							}
						}
					} else ioContainer = container.asIObject();
				}
			}
			if (ImportableObject.isArbitraryFile(file)) {
				candidates = gateway.getImportCandidates(object, file, status);
				int size = candidates.size();
				if (size == 0) return Boolean.valueOf(false);
				else if (size == 1) {
					String value = candidates.get(0);
					if (!file.getAbsolutePath().equals(value) && 
						object.isFileinQueue(value)) {
						gateway.closeImport();
						status.markedAsDuplicate();
						return Boolean.valueOf(true);
					}
					File f = new File(value);
					status.resetFile(f);
					result = gateway.importImage(object, ioContainer, f,
							status, importable.isArchived(), close);
					if (result instanceof ImageData) {
						image = (ImageData) result;
						images.add(image);
						annotatedImportedImage(list, images);
						return formatResult(image, userID, thumbnail);
					} else if (result instanceof Set) {
						ll = (Set<ImageData>) result;
						annotatedImportedImage(list, ll);
						kk = ll.iterator();
						converted = new ArrayList<Object>(ll.size());
						while (kk.hasNext()) {
							converted.add(formatResult(kk.next(), userID,
									thumbnail));	
						}
						return converted;
					}
					return result;
				} else {
					Map<File, StatusLabel> files = 
						new HashMap<File, StatusLabel>();
					Iterator<String> i = candidates.iterator();
					while (i.hasNext()) 
						files.put(new File(i.next()), new StatusLabel());
					status.setFiles(files);
					Boolean v = importCandidates(files, status, object, 
							importable.isArchived(), ioContainer, list, userID,
							close);
					if (v != null) {
						return v.booleanValue();
					}
				}
			} else { //single file let's try to import it.
				result = gateway.importImage(object, ioContainer, file, status, 
						importable.isArchived(), close);
				if (result instanceof ImageData) {
					image = (ImageData) result;
					images.add(image);
					annotatedImportedImage(list, images);
					return formatResult(image, userID, thumbnail);
				} else if (result instanceof Set) {
					ll = (Set<ImageData>) result;
					annotatedImportedImage(list, ll);
					kk = ll.iterator();
					converted = new ArrayList<Object>(ll.size());
					while (kk.hasNext()) {
						converted.add(formatResult(kk.next(), userID,
								thumbnail));
					}
					return converted;
				}
				return result;
			}
		} //file import ends.
		//Checks folder import.
		candidates = gateway.getImportCandidates(object, file, status);
		if (candidates.size() == 0) return Boolean.valueOf(false);
		Map<File, StatusLabel> hcsFiles = new HashMap<File, StatusLabel>();
		Map<File, StatusLabel> otherFiles = new HashMap<File, StatusLabel>();
		Map<File, StatusLabel> files = new HashMap<File, StatusLabel>();
		Iterator<String> i = candidates.iterator();
		File f;
		StatusLabel sl;
		while (i.hasNext()) {
			f = new File(i.next());
			sl = new StatusLabel();
			if (ImportableObject.isHCSFile(f))
				hcsFiles.put(f, sl);
			else otherFiles.put(f, sl);
			files.put(f, sl);
		}
		status.setFiles(files);
		//check candidates and see if we are dealing with HCS data
		if (hcsFiles.size() > 0) {
			//remove comment if we want screen from folder.
			//if (container == null && importable.isFolderAsContainer())
			//	container = object.createFolderAsContainer(importable, true);
			if (container != null && container instanceof ScreenData) {
				if (container.getId() <= 0) {
					//project needs to be created to.
					createdData = object.hasObjectBeenCreated(
							container);
					if (createdData == null) {
						try {
							ioContainer = gateway.saveAndReturnObject(
									container.asIObject(), parameters);
							//register
							object.addNewDataObject(
									PojoMapper.asDataObject(
											ioContainer));
						} catch (Exception e) {
							context.getLogger().error(this, 
									"Cannot create the Screen hosting " +
									"the data.");
						}
					}
				} else ioContainer = container.asIObject();
			}
			importCandidates(hcsFiles, status, object, 
					importable.isArchived(), ioContainer, list, userID, close);
		}
		if (otherFiles.size() > 0) {
			folder = object.createFolderAsContainer(importable);
			if (folder != null) { //folder
				//we have to import the image in this container.
				try {
					ioContainer = gateway.saveAndReturnObject(
							folder.asIObject(), parameters);
					status.setContainerFromFolder(PojoMapper.asDataObject(
							ioContainer));
					if (folder instanceof DatasetData) {
						if (container != null) {
							try {
								if (container.getId() <= 0) { 
									//project needs to be created to.
									createdData = object.hasObjectBeenCreated(
											container);
									if (createdData == null) {
										project = gateway.saveAndReturnObject(
												container.asIObject(), 
												parameters);
										object.addNewDataObject(
											PojoMapper.asDataObject(project));
										link = (ProjectDatasetLink) 
										ModelMapper.linkParentToChild(
												(Dataset) ioContainer, 
												(Project) project);
										link = (ProjectDatasetLink) 
										gateway.saveAndReturnObject(link, 
												parameters);
									} else {
										link = (ProjectDatasetLink) 
										ModelMapper.linkParentToChild(
												(Dataset) ioContainer, 
												(Project) 
												createdData.asProject());
										link = (ProjectDatasetLink) 
										gateway.saveAndReturnObject(link, 
												parameters);
									}
								} else { //project already exists.
									link = (ProjectDatasetLink) 
									ModelMapper.linkParentToChild(
											(Dataset) ioContainer, 
											(Project) container.asProject());
									link = (ProjectDatasetLink) 
									gateway.saveAndReturnObject(link, 
											parameters);
								}
							} catch (Exception e) {
								context.getLogger().error(this, 
										"Cannot create the container hosting " +
										"the data.");
							}
						}
					}
				} catch (Exception e) {
				}
			} else { //folder 
				if (dataset != null) { //dataset
					try {
						ioContainer = determineContainer(dataset, container,
								object);
					} catch (Exception e) {
						context.getLogger().error(this, 
								"Cannot create the container hosting " +
								"the data.");
					}
				}
			}
			
			importCandidates(otherFiles, status, object, 
					importable.isArchived(), ioContainer, list, userID, close);
		}
		return Boolean.valueOf(true);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getSupportedFileFormats()
	 */
	public FileFilter[] getSupportedFileFormats()
	{
		if (filters != null) return filters;
		//Retrieve values from bio-formats
		//filters = new ArrayList<FileFilter>();
		//improve that code.
		ImageReader reader = new ImageReader();
		FileFilter[] array = loci.formats.gui.GUITools.buildFileFilters(reader);
		if (array != null) {
			filters = new FileFilter[array.length];
			System.arraycopy(array, 0, filters, 0, array.length);
		} else filters = new FileFilter[0];
		return filters;
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#createMovie(long, long, List, MovieExportParam)
	 */
	public ScriptCallback createMovie(long imageID, long pixelsID, 
			List<Integer> channels, MovieExportParam param)
		throws ProcessException, DSOutOfServiceException, DSAccessException
	{
		if (imageID <= 0)
			throw new IllegalArgumentException("Image ID not valid.");
		if (param == null)
			throw new IllegalArgumentException("No parameters specified.");
		if (channels == null)
			channels = new ArrayList<Integer>();
		ExperimenterData exp = (ExperimenterData) context.lookup(
				LookupNames.CURRENT_USER_DETAILS);

		return gateway.createMovie(imageID, pixelsID, exp.getId(), channels, 
				param);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadROI(long, List, long)
	 */
	public List<ROIResult> loadROI(long imageID, List<Long> fileID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (imageID <= 0)
			throw new IllegalArgumentException("No image specified.");
		return gateway.loadROI(imageID, fileID, userID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#saveROI(long, long, List)
	 */
	public List<ROIData> saveROI(long imageID, long userID, List<ROIData> 
			roiList)
		throws DSOutOfServiceException, DSAccessException
	{
		if (imageID <= 0)
			throw new IllegalArgumentException("No image specified.");
		return gateway.saveROI(imageID, userID, roiList);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#exportImageAsOMETiff(long, File)
	 */
	public Object exportImageAsOMETiff(long imageID, File file)
			throws DSOutOfServiceException, DSAccessException
	{
		if (imageID <= 0)
			throw new IllegalArgumentException("No image specified.");
		if (file == null)
			throw new IllegalArgumentException("No File specified.");
		return gateway.exportImageAsOMETiff(file, imageID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#createFigure(List, Class, Object)
	 */
	public ScriptCallback createFigure(List<Long> ids, Class type, 
			Object parameters)
			throws ProcessException, DSOutOfServiceException, DSAccessException
	{
		if (parameters == null)
			throw new IllegalArgumentException("No parameters");
		ExperimenterData exp = (ExperimenterData) context.lookup(
				LookupNames.CURRENT_USER_DETAILS);
		if (parameters instanceof FigureParam) {
			FigureParam p = (FigureParam) parameters;
			return gateway.createFigure(ids, type, p, exp.getId());
		}
		return null;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadROIFromServer(long, long)
	 */
	public List<ROIResult> loadROIFromServer(long imageID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (imageID <= 0)
			throw new IllegalArgumentException("No image specified.");
		return gateway.loadROI(imageID, null, userID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#renderOverLays(long, PlaneDef, long, Map, boolean)
	 */
	public Object renderOverLays(long pixelsID, PlaneDef pd, long tableID,
			Map<Long, Integer> overlays, boolean asTexture)
			throws RenderingServiceException
	{
		try {
			return PixelsServicesFactory.renderOverlays(context,
					pixelsID, pd, tableID, overlays, asTexture);
		} catch (Exception e) {
			throw new RenderingServiceException("RenderImage", e);
		}
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#analyseFrap(List, Class, Object)
	 */
	public DataObject analyseFrap(List<Long> ids, Class type, Object param)
			throws DSOutOfServiceException, DSAccessException
	{
		if (ids == null || ids.size() <= 0)
			throw new IllegalArgumentException("No objects to analyse.");
		if (type == null)
			throw new IllegalArgumentException("No node to analyze.");
		long id = gateway.analyseFRAP(ids, type, param);
		if (id < 0) return null;
		return context.getMetadataService().loadAnnotation(id);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#runScript(ScriptObject)
	 */
	public ScriptCallback runScript(ScriptObject script)
			throws ProcessException, DSOutOfServiceException, DSAccessException
	{
		if (script == null) 
			throw new IllegalArgumentException("No script to run.");
		return gateway.runScript(script);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#loadAvailableScriptsWithUI()
	 */
	public List<ScriptObject> loadAvailableScriptsWithUI()
			throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadRunnableScriptsWithUI();
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#loadAvailableScripts(long)
	 */
	public List<ScriptObject> loadAvailableScripts(long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadRunnableScripts();
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#loadScript(long)
	 */
	public ScriptObject loadScript(long scriptID)
		throws ProcessException
	{
		return gateway.loadScript(scriptID);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#getScriptsAsString()
	 */
	public Map<Long, String> getScriptsAsString()
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getScriptsAsString();
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#uploadScript(ScriptObject)
	 */
	public Object uploadScript(ScriptObject script)
		throws DSOutOfServiceException, DSAccessException
	{
		if (script == null)
			throw new IllegalArgumentException("No script to upload.");
		Boolean b = (Boolean) context.lookup(
				LookupNames.USER_ADMINISTRATOR);
		boolean value = false;
		if (b != null) value = b.booleanValue();
		return gateway.uploadScript(script, value);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#loadRatings(Class, long, long)
	 */
	public Collection loadROIMeasurements(Class type, long id, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (ImageData.class.equals(type)) {
			return gateway.loadROIMeasurements(id, userID);
		}
			
		
		List<Long> ids = null;
		if (userID != -1) {
			ids = new ArrayList<Long>(1);
			ids.add(userID);
		}
		List<Long> nodeIds = new ArrayList<Long>(1);
		nodeIds.add(id);
		List<Class> types = new ArrayList<Class>();
		types.add(FileAnnotationData.class);
		Map map = gateway.loadAnnotations(type, nodeIds, types, ids, 
				new Parameters());
		if (map == null || map.size() == 0) return new ArrayList();
		Collection l = (Collection) map.get(id);
		List<FileAnnotationData> list = new ArrayList<FileAnnotationData>();
		if (l != null) {
			Iterator i = l.iterator();
			FileAnnotationData fa;
			String ns;
			while (i.hasNext()) {
				fa = (FileAnnotationData) i.next();
				ns = fa.getNameSpace();
				if (FileAnnotationData.MEASUREMENT_NS.equals(ns))
					list.add(fa);
			}
		}
		return list;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#getFSThumbnailSet(List, int, long)
	 */
	public Map<DataObject, BufferedImage> getFSThumbnailSet(
			List<DataObject> files, 
			int maxLength, long userID)
			throws DSAccessException, DSOutOfServiceException, FSAccessException
	{
		Map<DataObject, BufferedImage> 
			m = new HashMap<DataObject, BufferedImage>();
		if (files == null || files.size() == 0) return m;
		Iterator<DataObject> i = files.iterator();
		DataObject file;
		if (!isBinaryAvailable()) {
			while (i.hasNext()) {
				file = i.next();
				m.put(file, null);
			}
			return m;
		}
		FSFileSystemView view = gateway.getFSRepositories(userID);
		String path;
		while (i.hasNext()) {
			file = i.next();
			path = view.getThumbnail(file);
			try {
				if (path != null) m.put(file, 
						Factory.scaleBufferedImage(createImage(path), 
								maxLength));
				else m.put(file, null);
			} catch (Exception e) {
				m.put(file, null);
			}
		}
		return m;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#storeWorkflows(List, long)
	 */
	public Object storeWorkflows(List<WorkflowData> workflows, long userID)
			throws DSAccessException, DSOutOfServiceException
	{
		return gateway.storeWorkflows(workflows, userID);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#retrieveWorkflows(long)
	 */
	public List<WorkflowData> retrieveWorkflows(long userID) 
		throws DSAccessException, DSOutOfServiceException
	{
		ExperimenterData exp = (ExperimenterData) context.lookup(
					LookupNames.CURRENT_USER_DETAILS);
		if (userID < 0) userID = exp.getId();
		return gateway.retrieveWorkflows(userID);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#getExperimenterThumbnailSet(List, int)
	 */
	public Map<DataObject, BufferedImage> getExperimenterThumbnailSet(
			List<DataObject> experimenters, int maxLength)
			throws DSAccessException, DSOutOfServiceException
	{
		Map<DataObject, BufferedImage> 
			m = new HashMap<DataObject, BufferedImage>();
		if (experimenters == null || experimenters.size() == 0) return m;
		List<Long> ids = new ArrayList<Long>();
		Iterator<DataObject> i = experimenters.iterator();
		DataObject exp;
		if (!isBinaryAvailable()) {
			while (i.hasNext()) {
				m.put(i.next(), null);
			}
			return m;
		}
		
		String path;
		List<Class> types = new ArrayList<Class>();
		types.add(FileAnnotationData.class);
		Map<Long, DataObject> exps = new HashMap<Long, DataObject>();
		while (i.hasNext()) {
			exp = i.next();
			ids.add(exp.getId());
			m.put(exp, null);
			exps.put(exp.getId(), exp);
		}
		Map annotations;
		try {
			annotations = gateway.loadAnnotations(ExperimenterData.class, ids, 
					types, new ArrayList(), new Parameters());
		} catch (Exception e) {
			return m;
		}
		
		if (annotations == null || annotations.size() == 0)
			return m;
		//Make
		Entry entry;
		Iterator j = annotations.entrySet().iterator();
		Long id;
		Collection values;
		Iterator k;
		Object object;
		String ns;
		FileAnnotationData fa, ann;
		BufferedImage img;
		while (j.hasNext()) {
			entry = (Entry) j.next();
			id = (Long) entry.getKey();
			values = (Collection) entry.getValue();
			k = values.iterator();
			ann = null;
			while (k.hasNext()) {
				object = k.next();
				if (object instanceof FileAnnotationData) {
					fa = (FileAnnotationData) object;
					if (FileAnnotationData.EXPERIMENTER_PHOTO_NS.equals(
							fa.getNameSpace())) {
						if (ann == null) ann = fa;
						else {
							if (fa.getId() > ann.getId()) ann = fa;
						}
					}
				}
			}
			if (ann != null) {
				exp = exps.get(id);
				try {
					img = createImage(gateway.getUserPhoto(ann.getFileID(),
							ann.getFileSize()));
					m.put(exps.get(id), Factory.scaleBufferedImage(img, 
							maxLength));
				} catch (Exception e) {
					//nothing to do.
				}
				
			}
		}
		return m;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#saveAs(SaveAsParam)
	 */
	public ScriptCallback saveAs(SaveAsParam param)
		throws ProcessException, DSAccessException, DSOutOfServiceException
	{
		if (param == null)
			throw new IllegalArgumentException("No parameters specified.");
		List<DataObject> objects = param.getObjects();
		if (objects == null || objects.size() == 0)
			throw new IllegalArgumentException("No objects specified.");
		ExperimenterData exp = (ExperimenterData) context.lookup(
				LookupNames.CURRENT_USER_DETAILS);

		return gateway.saveAs(exp.getId(), param);
	}

}
