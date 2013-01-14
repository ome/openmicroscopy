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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

//Third-party libraries
import loci.common.RandomAccessInputStream;
import loci.formats.ImageReader;
import loci.formats.tiff.TiffParser;
import loci.formats.tiff.TiffSaver;

import com.sun.opengl.util.texture.TextureData;

//Application-internal dependencies
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportContainer;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
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
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.data.util.Target;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.PixelsServicesFactory;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.filter.file.OMETIFFFilter;
import org.openmicroscopy.shoola.util.filter.file.XMLFilter;
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
	 * @param ctx The security context.
	 * @param candidates The file to import.
	 * @param status The original status.
	 * @param object The object hosting information about the import.
	 * @param ioList The containers where to import the files.
	 * @param list   The list of annotations.
	 * @param userID The identifier of the user.
	 * @param hcs Value returns by the import containers.
	 */
	private Boolean importCandidates(SecurityContext ctx,
		Map<File, StatusLabel> files, StatusLabel status,
		ImportableObject object, IObject ioContainer,
		List<Annotation> list, long userID, boolean close, boolean hcs)
	throws DSAccessException, DSOutOfServiceException
	{
		if (status.isMarkedAsCancel()) {
			if (close) gateway.closeImport(ctx);
			return Boolean.valueOf(false);
		}
		boolean thumbnail = object.isLoadThumbnail();
		Entry<File, StatusLabel> entry;
		Iterator<Entry<File, StatusLabel>> jj = files.entrySet().iterator();
		StatusLabel label = null;
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
		ImportCandidates ic;
		List<ImportContainer> icContainers;
		while (jj.hasNext()) {
			entry = jj.next();
			file = (File) entry.getKey();
			if (hcs) {
			//if (ImportableObject.isHCSFile(file) || hcs) {
				if (!file.getName().endsWith(
						ImportableObject.DAT_EXTENSION)) {
					if (ioContainer != null && 
							!(ioContainer.getClass().equals(Screen.class) ||
							ioContainer.getClass().equals(ScreenI.class)))
							ioContainer = null;
				}
			}
			label = (StatusLabel) entry.getValue();
			if (close) {
				toClose = index == n;
				index++;
			}
			if (!label.isMarkedAsCancel()) {
				try {
					if (ioContainer == null) label.setNoContainer();
					ic = gateway.getImportCandidates(ctx, object, file, status);
					icContainers = ic.getContainers();
					if (icContainers.size() == 0)
						return Boolean.valueOf(false);
					result = gateway.importImage(ctx, object, ioContainer,
							icContainers.get(0),
							label, toClose, ImportableObject.isHCSFile(file));
					if (result instanceof ImageData) {
						image = (ImageData) result;
						images.add(image);
						if (thumbnail)
							label.setFile(file, 
								createImportedImage(ctx, userID, image));
						else label.setFile(file, image);
					} else if (result instanceof Set) {
						ll = (Set<ImageData>) result;
						annotatedImportedImage(ctx, list, ll);
						images.addAll(ll);
						kk = ll.iterator();
						converted = new ArrayList<Object>(ll.size());
						while (kk.hasNext()) {
							image = kk.next();
							if (thumbnail)
								converted.add(
									createImportedImage(ctx, userID, image));
							else converted.add(image);
						}
						label.setFile(file, converted);
					} else label.setFile(file, result);
				} catch (Exception e) {
					label.setFile(file, e);
				}
			}
		}
		annotatedImportedImage(ctx, list, images);
		if (close) gateway.closeImport(ctx);
		return null;
	}
	
	/**
	 * Annotates the imported images.
	 * 
	 * @param ctx The security context.
	 * @param annotations The annotations to add.
	 * @param images The imported images.
	 */
	private void annotatedImportedImage(SecurityContext ctx,
		List<Annotation> annotations, Collection images)
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
			gateway.saveAndReturnObject(ctx, list, new HashMap());
		} catch (Exception e) {
			//ignore 
		}
	}
	
	/**
	 * Creates a thumbnail for the imported image.
	 * 
	 * @param ctx The security context.
	 * @param userID  The identifier of the user.
	 * @param image   The image to handle.
	 * @return See above.
	 */
	private Object createImportedImage(SecurityContext ctx, long userID,
		ImageData image)
	{
		if (image != null) {
			ThumbnailData data;
			try {
				PixelsData pix = image.getDefaultPixels();
				BufferedImage img = createImage(
						gateway.getThumbnailByLongestSide(ctx, pix.getId(),
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
	 * @param ctx The security context.
	 * @param image The image to handle.
	 * @param userID The user's id.
	 * @param thumbnail Pass <code>true</code> if thumbnail has to be created,
	 * 					<code>false</code> otherwise.
	 * @return See above.
	 */
	private Object formatResult(SecurityContext ctx, ImageData image,
		long userID, boolean thumbnail)
	{
		Boolean backoff = null;
		try {
			PixelsData pixels = image.getDefaultPixels();
			backoff = gateway.isLargeImage(ctx, pixels.getId());
		} catch (Exception e) {}
		//if (backoff != null && backoff.booleanValue())
		//	return new ThumbnailData(image, backoff);
		if (thumbnail) {
			ThumbnailData thumb = (ThumbnailData) createImportedImage(ctx,
				userID, image);
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
	 * Recycles or creates the container.
	 * 
	 * @param ctx The security context.
	 * @param dataset The dataset to create or recycle.
	 * @param container The container to create and link the dataset to.
	 * @param object The object hosting the import option.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IObject determineContainer(SecurityContext ctx, DatasetData dataset,
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
								container, ctx);
						if (createdData == null) {
							project = gateway.saveAndReturnObject(ctx,
									container.asIObject(), parameters);
							//register
							object.addNewDataObject(
									PojoMapper.asDataObject(
									project));
							//now create the dataset
							ioContainer = gateway.saveAndReturnObject(ctx, 
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
							gateway.saveAndReturnObject(ctx, link,
									parameters);
						} else {
							DatasetData d;
							d = object.isDatasetCreated(
									createdData.getId(), dataset);
							if (d == null) {
								ioContainer = gateway.saveAndReturnObject(ctx,
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
								gateway.saveAndReturnObject(ctx, link,
										parameters);
							} else ioContainer = d.asIObject();
						}
					} else { //project already exists.
						createdData = object.isDatasetCreated(
								container.getId(), dataset);
						if (createdData == null) {
							ioContainer = gateway.saveAndReturnObject(ctx,
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
							gateway.saveAndReturnObject(ctx, link,
									parameters);
						} else ioContainer = createdData.asIObject();
					}
				} else { //dataset w/o project.
					createdData = object.hasObjectBeenCreated(dataset, ctx);
					if (createdData == null) {
						ioContainer = gateway.saveAndReturnObject(ctx,
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
							container, ctx);
					if (createdData == null) {
						ioContainer = gateway.saveAndReturnObject(ctx,
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

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadRenderingControl(SecurityContext, long)
	 */
	public RenderingControl loadRenderingControl(SecurityContext ctx,
		long pixelsID)
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
			RenderingEnginePrx re = gateway.createRenderingEngine(ctx,
					pixelsID);
			Pixels pixels = gateway.getPixels(ctx, pixelsID);
			if (pixels == null) return null;
				
			List<RndProxyDef> defs = gateway.getRenderingSettingsFor(
					ctx, pixelsID, exp.getId());
			Collection l = pixels.copyChannels();
			Iterator i = l.iterator();
			List<ChannelData> m = new ArrayList<ChannelData>(l.size());
			int index = 0;
			while (i.hasNext()) {
				m.add(new ChannelData(index, (Channel) i.next()));
				index++;
			}
			
			proxy = PixelsServicesFactory.createRenderingControl(context, ctx,
					re, pixels, m, compressionLevel, defs);
		}
		return proxy;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#renderImage(SecurityContext, long, PlaneDef, boolean, boolean)
	 */
	public Object renderImage(SecurityContext ctx, long pixelsID, PlaneDef pDef,
		boolean asTexture, boolean largeImage)
		throws RenderingServiceException
	{
		try {
			return PixelsServicesFactory.render(context, ctx,
						Long.valueOf(pixelsID), pDef, asTexture, largeImage);
		} catch (Exception e) {
			throw new RenderingServiceException("RenderImage", e);
		}
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#shutDown(SecurityContext,long)
	 */
	public void shutDown(SecurityContext ctx, long pixelsID)
	{
		try {
			if (!PixelsServicesFactory.shutDownRenderingControl(context,
					pixelsID))
				gateway.removeREService(ctx, pixelsID);
		} catch (Exception e) {
			context.getLogger().error(this, e.getMessage());
		}
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getThumbnail(SecurityContext, long, int, int, long)
	 */
	public BufferedImage getThumbnail(SecurityContext ctx,long pixID, int sizeX,
		int sizeY, long userID)
		throws RenderingServiceException
	{
		try {
			if (pixID < 0) return null;
			if (!isBinaryAvailable()) return null;
			return createImage(gateway.getThumbnail(ctx, pixID, sizeX, sizeY,
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
	 * @see OmeroImageService#getThumbnailSet(SecurityContext, List, int)
	 */
	public Map<Long, BufferedImage> getThumbnailSet(SecurityContext ctx,
		List pixelsID, int max)
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
				map = gateway.getThumbnailSet(ctx, (List) j.next(), max, false);
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
	 * @see OmeroImageService#reloadRenderingService(SecurityContext, long)
	 */
	public RenderingControl reloadRenderingService(SecurityContext ctx,
		long pixelsID)
		throws RenderingServiceException
	{
		RenderingControl proxy = 
			PixelsServicesFactory.getRenderingControl(context, 
					Long.valueOf(pixelsID), false);
		if (proxy == null) return null;
		try {
			RenderingEnginePrx re = gateway.createRenderingEngine(ctx,
					pixelsID);
			return PixelsServicesFactory.reloadRenderingControl(context, 
					pixelsID, re);
		} catch (Exception e) {
			throw new RenderingServiceException("Cannot restart the " +
					"rendering engine for : "+pixelsID, e);
		}
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#resetRenderingService(SecurityContext, long)
	 */
	public RenderingControl resetRenderingService(SecurityContext ctx,
		long pixelsID)
		throws RenderingServiceException
	{
		RenderingControl proxy = 
			PixelsServicesFactory.getRenderingControl(context, 
					Long.valueOf(pixelsID), false);
		if (proxy == null) return null;
		try {
			RenderingEnginePrx re = gateway.createRenderingEngine(ctx,
					pixelsID);
			ExperimenterData exp = (ExperimenterData) context.lookup(
					LookupNames.CURRENT_USER_DETAILS);
			RenderingDef def = gateway.getRenderingDef(ctx, pixelsID,
					exp.getId());
			return PixelsServicesFactory.resetRenderingControl(context, 
					pixelsID, re, def);
		} catch (Exception e) {
			throw new RenderingServiceException("Cannot restart the " +
					"rendering engine for : "+pixelsID, e);
		}
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadPixels(SecurityContext, long)
	 */
	public PixelsData loadPixels(SecurityContext ctx, long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (pixelsID < 0) 
			throw new IllegalArgumentException("Pixels' ID not valid.");
		return (PixelsData) PojoMapper.asDataObject(
				gateway.getPixels(ctx, pixelsID));
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getPlane(SecurityContext, long, int, int, int)
	 */
	public byte[] getPlane(SecurityContext ctx, long pixelsID, int z, int t,
			int c)
		throws DSOutOfServiceException, DSAccessException, FSAccessException
	{
		if (pixelsID < 0) 
			throw new IllegalArgumentException("Pixels' ID not valid.");
		return gateway.getPlane(ctx, pixelsID, z, t, c);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#pasteRenderingSettings(SecurityContext, long,
	 * Class, List)
	 */
	public Map pasteRenderingSettings(SecurityContext ctx, long pixelsID,
		Class rootNodeType, List nodesID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodesID == null || nodesID.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		return gateway.pasteRenderingSettings(ctx, pixelsID, rootNodeType,
				nodesID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#resetRenderingSettings(Class, List)
	 */
	public Map resetRenderingSettings(SecurityContext ctx, Class rootNodeType,
		List nodesID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodesID == null || nodesID.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		return gateway.resetRenderingSettings(ctx, rootNodeType, nodesID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#setMinMaxSettings(SecurityContext, Class, List)
	 */
	public Map setMinMaxSettings(SecurityContext ctx, Class rootNodeType,
			List nodesID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodesID == null || nodesID.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		return gateway.setMinMaxSettings(ctx, rootNodeType, nodesID);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#setOwnerRenderingSettings(SecurityContext, Class,
	 * List)
	 */
	public Map setOwnerRenderingSettings(SecurityContext ctx,
			Class rootNodeType, List nodesID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodesID == null || nodesID.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		return gateway.setOwnerRenderingSettings(ctx, rootNodeType, nodesID);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getRenderingSettings(ctx, long, long)
	 */
	public Map getRenderingSettings(SecurityContext ctx, long pixelsID,
		long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getRenderingSettings(ctx, pixelsID, userID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getRenderingSettingsFor(long, long)
	 */
	public List<RndProxyDef> getRenderingSettingsFor(SecurityContext ctx,
		long pixelsID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getRenderingSettingsFor(ctx, pixelsID, userID);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#renderProjected(SecurityContext, long, int, int,
	 * int, int, List)
	 */
	public BufferedImage renderProjected(SecurityContext ctx, long pixelsID,
		int startZ, int endZ, int stepping, int type, List<Integer> channels)
		throws RenderingServiceException, DSOutOfServiceException
	{
		return PixelsServicesFactory.renderProjected(context, pixelsID, startZ,
				endZ, type, stepping, channels);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#renderProjectedAsTexture(SecurityContext, long,
	 * int, int, int, int, List)
	 */
	public TextureData renderProjectedAsTexture(SecurityContext ctx,
		long pixelsID, int startZ, int endZ, int stepping, int type,
		List<Integer> channels)
		throws RenderingServiceException, DSOutOfServiceException
	{
		return PixelsServicesFactory.renderProjectedAsTexture(context, 
				pixelsID, startZ, endZ, type, stepping, channels);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#projectImage(SecurityContext, ProjectionParam)
	 */
	public ImageData projectImage(SecurityContext ctx, ProjectionParam ref)
		throws DSOutOfServiceException, DSAccessException
	{
		if (ref == null) return null;
		ImageData image = gateway.projectImage(ctx, ref.getPixelsID(),
				ref.getStartT(), ref.getEndT(), ref.getStartZ(), 
				ref.getEndZ(), ref.getStepping(), ref.getProjectionType(),
				ref.getChannels(), ref.getName(), ref.getPixelsType());
		if (image == null) return null;
		Image img = image.asImage();
		img.setDescription(omero.rtypes.rstring(ref.getDescription()));
		image = (ImageData) 
			PojoMapper.asDataObject(gateway.updateObject(ctx, img,
					new Parameters()));
		image = gateway.getImage(ctx, image.getId(), new Parameters());
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
					existing.add((DatasetData)
						svc.createDataObject(ctx, i.next(),
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
			gateway.createObjects(ctx, links);
		}
		return image;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#createRenderingSettings(SecurityContext, long,
	 * RndProxyDef, List)
	 */
	public Boolean createRenderingSettings(SecurityContext ctx, long pixelsID,
		RndProxyDef rndToCopy, List<Integer> indexes) 
		throws DSOutOfServiceException, DSAccessException, FSAccessException
	{
		if (rndToCopy == null) {
			RenderingDef def = gateway.createRenderingDef(ctx, pixelsID);
			return (def != null);
		}
		RenderingControl rndControl = loadRenderingControl(ctx, pixelsID);
		try {
			rndControl.copyRenderingSettings(rndToCopy, indexes);
			//save them
			rndControl.saveCurrentSettings();
			//discard it
			shutDown(ctx, pixelsID);
		} catch (Exception e) {
			throw new DSAccessException("Unable to copy the " +
					"rendering settings.");
		}
		
		return Boolean.TRUE;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadPlaneInfo(SecurityContext, long, int, int, int)
	 */
	public Collection loadPlaneInfo(SecurityContext ctx, long pixelsID, int z,
			int t, int channel)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadPlaneInfo(ctx, pixelsID, z, t, channel);
	}
	
	/**
	 * Returns <code>true</code> if the containers are <code>HCS</code>
	 * containers, <code>false</code> otherwise.
	 * 
	 * @param containers The collection to handle.
	 * @return See above.
	 */
	private boolean isHCS(List<ImportContainer> containers)
	{
		if (containers == null || containers.size() == 0) return false;
		int count = 0;
		Iterator<ImportContainer> i = containers.iterator();
		ImportContainer ic;
		while (i.hasNext()) {
			ic = i.next();
			if (ic.getIsSPW()) count++;
		}
		return count == containers.size();
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#importFile(ImportableObject,
	 * ImportableFile, long, long, boolean)
	 */
	public Object importFile(ImportableObject object,
		ImportableFile importable, long userID, boolean close) 
		throws ImportException, DSAccessException, DSOutOfServiceException
	{
		if (importable == null || importable.getFile() == null)
			throw new IllegalArgumentException("No images to import.");
		StatusLabel status = importable.getStatus();
		SecurityContext ctx = 
			new SecurityContext(importable.getGroup().getId());
		if (status.isMarkedAsCancel()) {
			if (close) gateway.closeImport(ctx);
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
				l = gateway.saveAndReturnObject(ctx, l, new HashMap());
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
		Set<ImageData> ll;
		ImageData image;
		Iterator<ImageData> kk;
		List<Object> converted;
		List<String> candidates;
		ImportCandidates ic = null;
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
		boolean hcs;
		List<ImportContainer> icContainers;
		if (file.isFile()) {
			hcsFile = ImportableObject.isHCSFile(file);
			//Create the container if required.
			if (hcsFile) {
				boolean b = ImportableObject.isArbitraryFile(file);
				if (b) { //check if it is actually a HCS file.
					ic = gateway.getImportCandidates(ctx, object, file, status);
					if (ic != null) {
						candidates = ic.getPaths();
						if (candidates.size() == 1) { 
							String value = candidates.get(0);
							if (!file.getAbsolutePath().equals(value) && 
								object.isFileinQueue(value)) {
								if (close) gateway.closeImport(ctx);
								status.markedAsDuplicate();
								return Boolean.valueOf(true);
							}
							if (!file.getName().endsWith(
									ImportableObject.DAT_EXTENSION)) {
								hcsFile = isHCS(ic.getContainers());
								/*
								hcsFile = ImportableObject.isHCSFile(value);
								if (!hcsFile) {
									hcsFile = isHCS(ic.getContainers());
								}*/
							} else hcsFile = false;
						}
					}
				}
			}
			if (!hcsFile && ImportableObject.isOMEFile(file)) {
				ic = gateway.getImportCandidates(ctx, object, file, status);
				if (ic != null) {
					hcsFile = isHCS(ic.getContainers());
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
						ioContainer = determineContainer(ctx,
								(DatasetData) folder, container, object);
						status.setContainerFromFolder(PojoMapper.asDataObject(
								ioContainer));
					} catch (Exception e) {
						context.getLogger().error(this, "Cannot create " +
								"the container hosting the images.");
					}
				} else if (folder instanceof ScreenData) {
					try {
						ioContainer = determineContainer(ctx, null, folder, 
								object);
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
					ioContainer = determineContainer(ctx, dataset, container,
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
								container, ctx);
						if (createdData == null) {
							try {
								ioContainer = gateway.saveAndReturnObject(ctx,
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
				if (ic == null) //already check if hcs.
					ic = gateway.getImportCandidates(ctx, object, file, status);
				candidates = ic.getPaths();
				int size = candidates.size();
				if (size == 0) return Boolean.valueOf(false);
				else if (size == 1) {
					String value = candidates.get(0);
					if (!file.getAbsolutePath().equals(value) && 
						object.isFileinQueue(value)) {
						if (close) gateway.closeImport(ctx);
						status.markedAsDuplicate();
						return Boolean.valueOf(true);
					}
					File f = new File(value);
					status.resetFile(f);
					if (ioContainer == null)
						status.setNoContainer();
					result = gateway.importImage(ctx, object, ioContainer,
							ic.getContainers().get(0),
							status, close, ImportableObject.isHCSFile(f));
					if (result instanceof ImageData) {
						image = (ImageData) result;
						images.add(image);
						annotatedImportedImage(ctx, list, images);
						return formatResult(ctx, image, userID, thumbnail);
					} else if (result instanceof Set) {
						ll = (Set<ImageData>) result;
						annotatedImportedImage(ctx, list, ll);
						kk = ll.iterator();
						converted = new ArrayList<Object>(ll.size());
						while (kk.hasNext()) {
							converted.add(formatResult(ctx, kk.next(), userID,
									thumbnail));	
						}
						return converted;
					}
					return result;
				} else {
					hcs = isHCS(ic.getContainers());
					Map<File, StatusLabel> files = 
						new HashMap<File, StatusLabel>();
					Iterator<String> i = candidates.iterator();
					while (i.hasNext()) 
						files.put(new File(i.next()), new StatusLabel());
					status.setFiles(files);
					Boolean v = importCandidates(ctx, files, status, object,
							ioContainer, list, userID, close, hcs);
					if (v != null) {
						return v.booleanValue();
					}
				}
			} else { //single file let's try to import it.
				if (ioContainer == null)
					status.setNoContainer();
				ic = gateway.getImportCandidates(ctx, object, file, status);
				icContainers = ic.getContainers();
				if (icContainers.size() == 0)
					return Boolean.valueOf(false);;
				result = gateway.importImage(ctx, object, ioContainer,
						icContainers.get(0),
					status, close, ImportableObject.isHCSFile(file));
				if (result instanceof ImageData) {
					image = (ImageData) result;
					images.add(image);
					annotatedImportedImage(ctx, list, images);
					return formatResult(ctx, image, userID, thumbnail);
				} else if (result instanceof Set) {
					ll = (Set<ImageData>) result;
					annotatedImportedImage(ctx, list, ll);
					kk = ll.iterator();
					converted = new ArrayList<Object>(ll.size());
					while (kk.hasNext()) {
						converted.add(formatResult(ctx, kk.next(), userID,
								thumbnail));
					}
					return converted;
				}
				return result;
			}
		} //file import ends.
		//Checks folder import.
		ic = gateway.getImportCandidates(ctx, object, file, status);
		candidates = ic.getPaths();
		if (candidates.size() == 0) return Boolean.valueOf(false);
		Map<File, StatusLabel> hcsFiles = new HashMap<File, StatusLabel>();
		Map<File, StatusLabel> otherFiles = new HashMap<File, StatusLabel>();
		Map<File, StatusLabel> files = new HashMap<File, StatusLabel>();
		Iterator<String> i = candidates.iterator();
		File f;
		StatusLabel sl;
		int n = candidates.size();
		hcs = isHCS(ic.getContainers());
		while (i.hasNext()) {
			f = new File(i.next());
			sl = new StatusLabel();
			//if (ImportableObject.isHCSFile(f) || hcs) {
			if (hcs) {
				if (n == 1 && file.list().length > 1)
					hcsFiles.put(f, sl);
				else if (n > 1) {
					if (f.getName().endsWith(ImportableObject.DAT_EXTENSION))
						otherFiles.put(f, sl);
					else hcsFiles.put(f, sl);
				} else hcsFiles.put(f, sl);
			} else otherFiles.put(f, sl);
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
							container, ctx);
					if (createdData == null) {
						try {
							ioContainer = gateway.saveAndReturnObject(ctx,
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
			importCandidates(ctx, hcsFiles, status, object,
					ioContainer, list, userID, close, hcs);
		}
		if (otherFiles.size() > 0) {
			folder = object.createFolderAsContainer(importable);
			if (folder != null) { //folder
				//we have to import the image in this container.
				try {
					ioContainer = gateway.saveAndReturnObject(ctx,
							folder.asIObject(), parameters);
					status.setContainerFromFolder(PojoMapper.asDataObject(
							ioContainer));
					if (folder instanceof DatasetData) {
						if (container != null) {
							try {
								if (container.getId() <= 0) { 
									//project needs to be created to.
									createdData = object.hasObjectBeenCreated(
											container, ctx);
									if (createdData == null) {
										project = gateway.saveAndReturnObject(
												ctx, container.asIObject(),
												parameters);
										object.addNewDataObject(
											PojoMapper.asDataObject(project));
										link = (ProjectDatasetLink) 
										ModelMapper.linkParentToChild(
												(Dataset) ioContainer, 
												(Project) project);
										link = (ProjectDatasetLink) 
										gateway.saveAndReturnObject(ctx, link,
												parameters);
									} else {
										link = (ProjectDatasetLink) 
										ModelMapper.linkParentToChild(
												(Dataset) ioContainer, 
												(Project) 
												createdData.asProject());
										link = (ProjectDatasetLink) 
										gateway.saveAndReturnObject(ctx, link,
												parameters);
									}
								} else { //project already exists.
									link = (ProjectDatasetLink) 
									ModelMapper.linkParentToChild(
											(Dataset) ioContainer, 
											(Project) container.asProject());
									link = (ProjectDatasetLink) 
									gateway.saveAndReturnObject(ctx, link, 
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
						ioContainer = determineContainer(ctx, dataset, container,
								object);
					} catch (Exception e) {
						context.getLogger().error(this, 
								"Cannot create the container hosting " +
								"the data.");
					}
				}
			}
			//import the files that are not hcs files.
			importCandidates(ctx, otherFiles, status, object,
				ioContainer, list, userID, close, false);
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
	 * @see OmeroImageService#createMovie(SecurityContext, long, long, List,
	 * MovieExportParam)
	 */
	public ScriptCallback createMovie(SecurityContext ctx, long imageID,
		long pixelsID, List<Integer> channels, MovieExportParam param)
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

		return gateway.createMovie(ctx, imageID, pixelsID, exp.getId(),
				channels, param);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadROI(SecurityContext, long, List, long)
	 */
	public List<ROIResult> loadROI(SecurityContext ctx, long imageID,
			List<Long> fileID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (imageID <= 0)
			throw new IllegalArgumentException("No image specified.");
		return gateway.loadROI(ctx, imageID, fileID, userID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#saveROI(SecurityContext, long, long, List)
	 */
	public List<ROIData> saveROI(SecurityContext ctx, long imageID, long userID,
		List<ROIData> roiList)
		throws DSOutOfServiceException, DSAccessException
	{
		if (imageID <= 0)
			throw new IllegalArgumentException("No image specified.");
		return gateway.saveROI(ctx, imageID, userID, roiList);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#exportImageAsOMEObject(SecurityContext, int, long,
	 * File, Target)
	 */
	public Object exportImageAsOMEFormat(SecurityContext ctx, int index,
			long imageID, File file, Target target)
			throws DSOutOfServiceException, DSAccessException
	{
		if (imageID <= 0)
			throw new IllegalArgumentException("No image specified.");
		if (file == null)
			throw new IllegalArgumentException("No File specified.");
		//To be modified
		//check file name and index.
		String path = file.getAbsolutePath();
		switch (index) {
			case EXPORT_AS_OMETIFF:
				if (!(path.endsWith(OMETIFFFilter.OME_TIFF) || 
						path.endsWith(OMETIFFFilter.OME_TIF))) {
					path += "."+OMETIFFFilter.OME_TIFF;
					file.delete();
					file = new File(path);
				}
				break;
			case EXPORT_AS_OME_XML:
				if (!(path.endsWith(XMLFilter.OME_XML))) {
					path += "."+XMLFilter.OME_XML;
					file.delete();
					file = new File(path);
				}
		}
		File f = gateway.exportImageAsOMEObject(ctx, index, file, imageID);
		if (target == null) return f;
		//Apply the transformations
		List<InputStream> transforms = target.getTransforms();
		if (transforms == null || transforms.size() == 0) return f;
		//Apply each transform one after another.
		Iterator<InputStream> i = transforms.iterator();
		//Create a tmp file then we will copy to the correct location
		
		TransformerFactory factory;
        Transformer transformer;
		InputStream stream;
		File r;
		File output = null;
		String ext = "."+XMLFilter.OME_XML;
		List<File> files = new ArrayList<File>();
		InputStream in = null;
		OutputStream out = null;
		File tmp = null;
		try {
			File inputXML = File.createTempFile(RandomStringUtils.random(10),
					ext);
			files.add(inputXML);
			if (index == EXPORT_AS_OMETIFF) {
				tmp = File.createTempFile(RandomStringUtils.random(10),
						"."+OMETIFFFilter.OME_TIFF);
				files.add(tmp);
				FileUtils.copyFile(f, tmp);
				String c = new TiffParser(f.getAbsolutePath()).getComment();
				FileUtils.writeStringToFile(inputXML, c);
			} else {
				FileUtils.copyFile(f, inputXML);
			}
			while (i.hasNext()) {
				factory = TransformerFactory.newInstance();
				stream = i.next();
				output = File.createTempFile(RandomStringUtils.random(10), ext);
				transformer = factory.newTransformer(new StreamSource(stream));
				out = new FileOutputStream(output);
				in =  new FileInputStream(inputXML);
				transformer.transform(new StreamSource(in),
						new StreamResult(out));
				files.add(output);
				inputXML = output;
				stream.close();
				out.close();
				in.close();
			}
			
			file.delete();
			//Copy the result
			r = new File(path);
			if (index == EXPORT_AS_OME_XML)
				FileUtils.copyFile(inputXML, r);
			else {
				FileUtils.copyFile(tmp, r);
				TiffSaver saver = new TiffSaver(path);
				RandomAccessInputStream ra = new RandomAccessInputStream(path);
				saver.overwriteComment(ra, 
						FileUtils.readFileToString(inputXML));
				ra.close();
			}
			//delete file
			Iterator<File> j = files.iterator();
			while (j.hasNext()) {
				j.next().delete();
			}
		} catch (Exception e) {
			try {
				if (in != null) in.close();
				if (out != null) out.close();
			} catch (Exception ex) {}
			
			throw new IllegalArgumentException("Unable to apply the transforms",
					e);
		}
		
		return r;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#createFigure(SecurityContext, List, Class, Object)
	 */
	public ScriptCallback createFigure(SecurityContext ctx, List<Long> ids,
		Class type, Object parameters)
		throws ProcessException, DSOutOfServiceException, DSAccessException
	{
		if (parameters == null)
			throw new IllegalArgumentException("No parameters");
		ExperimenterData exp = (ExperimenterData) context.lookup(
				LookupNames.CURRENT_USER_DETAILS);
		if (parameters instanceof FigureParam) {
			FigureParam p = (FigureParam) parameters;
			return gateway.createFigure(ctx, ids, type, p, exp.getId());
		}
		return null;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadROIFromServer(SecurityContext, long, long)
	 */
	public List<ROIResult> loadROIFromServer(SecurityContext ctx, long imageID,
		long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (imageID <= 0)
			throw new IllegalArgumentException("No image specified.");
		return gateway.loadROI(ctx, imageID, null, userID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#renderOverLays(SecurityContext, long, PlaneDef,
	 * long, Map, boolean)
	 */
	public Object renderOverLays(SecurityContext ctx, long pixelsID,
		PlaneDef pd, long tableID, Map<Long, Integer> overlays,
		boolean asTexture)
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
	 * @see OmeroImageService#runScript(SecurityContext, ScriptObject)
	 */
	public ScriptCallback runScript(SecurityContext ctx, ScriptObject script)
			throws ProcessException, DSOutOfServiceException, DSAccessException
	{
		if (script == null) 
			throw new IllegalArgumentException("No script to run.");
		return gateway.runScript(ctx, script);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#loadAvailableScriptsWithUI(SecurityContext)
	 */
	public List<ScriptObject> loadAvailableScriptsWithUI(SecurityContext ctx)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadRunnableScriptsWithUI(ctx);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#loadAvailableScripts(SecurityContext, long)
	 */
	public List<ScriptObject> loadAvailableScripts(SecurityContext ctx,
		long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadRunnableScripts(ctx);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#loadScript(SecurityContext, long)
	 */
	public ScriptObject loadScript(SecurityContext ctx, long scriptID)
		throws ProcessException
	{
		return gateway.loadScript(ctx, scriptID);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#getScriptsAsString(SecurityContext)
	 */
	public Map<Long, String> getScriptsAsString(SecurityContext ctx)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getScriptsAsString(ctx);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#uploadScript(SecurityContext, ScriptObject)
	 */
	public Object uploadScript(SecurityContext ctx, ScriptObject script)
		throws DSOutOfServiceException, DSAccessException
	{
		if (script == null)
			throw new IllegalArgumentException("No script to upload.");
		Boolean b = (Boolean) context.lookup(
				LookupNames.USER_ADMINISTRATOR);
		boolean value = false;
		if (b != null) value = b.booleanValue();
		return gateway.uploadScript(ctx, script, value);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#loadRatings(SecurityContext, Class, long, long)
	 */
	public Collection loadROIMeasurements(SecurityContext ctx, Class type,
		long id, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (ImageData.class.equals(type)) {
			return gateway.loadROIMeasurements(ctx, id, userID);
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
		Map map = gateway.loadAnnotations(ctx, type, nodeIds, types, ids,
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
	 * @see OmeroImageService#getFSThumbnailSet(SecurityContext, List, int, long)
	 */
	public Map<DataObject, BufferedImage> getFSThumbnailSet(SecurityContext ctx,
		List<DataObject> files, int maxLength, long userID)
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
		FSFileSystemView view = gateway.getFSRepositories(ctx, userID);
		String path;
		while (i.hasNext()) {
			file = i.next();
			path = view.getThumbnail(file);
			try {
				if (path != null) m.put(file, 
					Factory.scaleBufferedImage(createImage(path), maxLength));
				else m.put(file, null);
			} catch (Exception e) {
				m.put(file, null);
			}
		}
		return m;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#storeWorkflows(SecurityContext, List, long)
	 */
	public Object storeWorkflows(SecurityContext ctx,
		List<WorkflowData> workflows, long userID)
		throws DSAccessException, DSOutOfServiceException
	{
		return gateway.storeWorkflows(ctx, workflows, userID);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#retrieveWorkflows(SecurityContext, long)
	 */
	public List<WorkflowData> retrieveWorkflows(SecurityContext ctx,
		long userID) 
		throws DSAccessException, DSOutOfServiceException
	{
		ExperimenterData exp = (ExperimenterData) context.lookup(
					LookupNames.CURRENT_USER_DETAILS);
		if (userID < 0) userID = exp.getId();
		return gateway.retrieveWorkflows(ctx, userID);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#getExperimenterThumbnailSet(SecurityContext, List, int)
	 */
	public Map<DataObject, BufferedImage> getExperimenterThumbnailSet(
		SecurityContext ctx, List<DataObject> experimenters, int maxLength)
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
			annotations = gateway.loadAnnotations(ctx, ExperimenterData.class,
				ids, types, new ArrayList(), new Parameters());
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
					img = createImage(gateway.getUserPhoto(ctx,
						ann.getFileID(), ann.getFileSize()));
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
	 * @see OmeroImageService#saveAs(SecurityContext, SaveAsParam)
	 */
	public ScriptCallback saveAs(SecurityContext ctx, SaveAsParam param)
		throws ProcessException, DSAccessException, DSOutOfServiceException
	{
		if (param == null)
			throw new IllegalArgumentException("No parameters specified.");
		List<DataObject> objects = param.getObjects();
		if (objects == null || objects.size() == 0)
			throw new IllegalArgumentException("No objects specified.");
		ExperimenterData exp = (ExperimenterData) context.lookup(
				LookupNames.CURRENT_USER_DETAILS);

		return gateway.saveAs(ctx, exp.getId(), param);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroImageService#isLargeImage(SecurityContext, long)
	 */
	public Boolean isLargeImage(SecurityContext ctx, long pixelsId)
		throws DSAccessException, DSOutOfServiceException
	{
		return gateway.isLargeImage(ctx, pixelsId);
	}
	
}
