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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;


//Third-party libraries
import loci.formats.ImageReader;
import loci.formats.gui.FormatFileFilter;

//Application-internal dependencies
import omero.api.RenderingEnginePrx;
import omero.model.Channel;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.RenderingDef;
import omero.romio.PlaneDef;
import omero.sys.Parameters;

import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.PixelsServicesFactory;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.image.io.WriterImage;

import com.sun.opengl.util.texture.TextureData;

import pojos.ChannelData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PixelsData;


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
	private List<FileFilter>		filters;
	
	/** The extensions of the supported files formats. */
	private String[]				supportedExtensions;
	
	/** Uses it to gain access to the container's services. */
	private Registry                context;

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private OMEROGateway            gateway;
	
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
			ByteArrayInputStream stream = new ByteArrayInputStream(values);
			return ImageIO.read(stream);
		} catch (Exception e) {
			throw new RenderingServiceException("Cannot create buffered image",
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
		throws DSOutOfServiceException, DSAccessException
	{
		RenderingControl proxy = 
			PixelsServicesFactory.getRenderingControl(context, 
					new Long(pixelsID), true);
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
			RenderingDef def = gateway.getRenderingDef(pixelsID, exp.getId());
			Collection l = pixels.copyChannels();
			Iterator i = l.iterator();
			List<ChannelData> m = new ArrayList<ChannelData>(l.size());
			int index = 0;
			while (i.hasNext()) {
				m.add(new ChannelData(index, (Channel) i.next()));
				index++;
			}
			
			proxy = PixelsServicesFactory.createRenderingControl(context, re,
					pixels, m, compressionLevel, def);
		}
		return proxy;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#renderImage(long, PlaneDef, boolean, boolean)
	 */
	public Object renderImage(long pixelsID, PlaneDef pDef, boolean
			asTexture, boolean largeImage)
		throws RenderingServiceException
	{
		try {
			if (!largeImage)
				return PixelsServicesFactory.render(context, new Long(pixelsID), 
						pDef, asTexture);
			List<Long> ids = new ArrayList<Long>();
			ids.add(pixelsID);
			int w = pDef.x;
			int h = pDef.y;
			int max = w;
			if (max < h) max = h;
			if (max > RenderingControl.MAX_SIZE_THREE) 
				max = RenderingControl.MAX_SIZE_THREE;
			Map m = gateway.getThumbnailSet(ids, max);
			byte[] values = (byte[]) m.get(pixelsID);
			if (asTexture) {
				return PixelsServicesFactory.createTexture(
						WriterImage.bytesToDataBufferJPEG(values), w, h);
			} else {
				return createImage(values);
			}
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
			return createImage(gateway.getThumbnail(pixID, sizeX, sizeY, 
								userID));
		} catch (Exception e) {
			if (e instanceof DSOutOfServiceException) {
				context.getLogger().error(this, e.getMessage());
				return getThumbnail(pixID, sizeX, sizeY, userID);
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
		try {
			Map<Long, BufferedImage> r = new HashMap<Long, BufferedImage>();
			if (pixelsID == null || pixelsID.size() == 0)
				return r;
			Map m = gateway.getThumbnailSet(pixelsID, max);
			if (m == null || m.size() == 0) return r;
			Iterator i = m.keySet().iterator();
			long id;
			byte[] values;
			while (i.hasNext()) {
				id = (Long) i.next();
				values = (byte[]) m.get(id);
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
			return r;
		} catch (Exception e) {
			if (e instanceof DSOutOfServiceException) {
				context.getLogger().error(this, e.getMessage());
				return getThumbnailSet(pixelsID, max);
			}
			throw new RenderingServiceException("Get Thumbnail set", e);
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
					new Long(pixelsID), false);
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
					new Long(pixelsID), false);
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
		throws DSOutOfServiceException, DSAccessException
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
	 * @see OmeroImageService#setOriginalRenderingSettings(Class, List)
	 */
	public Map setOriginalRenderingSettings(Class rootNodeType, List nodesID) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (nodesID == null || nodesID.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		return gateway.setOriginalRenderingSettings(rootNodeType, nodesID);
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
	public List getRenderingSettingsFor(long pixelsID, long userID) 
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
		throws DSOutOfServiceException, DSAccessException
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
		Collection planes = gateway.loadPlaneInfo(pixelsID, z, t, channel);
		return planes;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#importImage(DataObject, File, StatusLabel, long, 
	 * long, boolean, String, int)
	 */
	public Object importImage(DataObject container, File file, 
			StatusLabel status, long userID, long groupID, boolean archived, 
			String name, int depth) 
		throws ImportException
	{
		if (file == null)
			throw new IllegalArgumentException("No images to import.");
		Object object;
		if (file.isDirectory()) {
			object = gateway.importFolder(container, file, status, 
					archived, depth);
		} else {
			object = gateway.importImage(container, file, status, archived, 
					name);
		}
				
		if (!(object instanceof ImageData)) return object;
		
		ImageData image = (ImageData) object;
		if (image != null) {
			try {
				PixelsData pix = image.getDefaultPixels();
				BufferedImage img = createImage(
						gateway.getThumbnailByLongestSide(pix.getId(), 24));
				ThumbnailData data = new ThumbnailData(image.getId(), img, 
						userID, true);
				data.setImage(image);
				return data;
			} catch (Exception e) {
				DeletableObject d = new DeletableObject(image);
				List<DeletableObject> l = new ArrayList<DeletableObject>(1);
				l.add(d);
				try {
					context.getDataService().delete(l);
				} catch (Exception ex) {}
				throw new ImportException("Failed to create thumbnail", e, 
						gateway.getReaderType());
			}
		}
		return image;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getSupportedFileFilters()
	 */
	public List<FileFilter> getSupportedFileFilters()
	{
		if (filters != null) return filters;
		//Retrieve values from bio-formats
		filters = new ArrayList<FileFilter>();
		//improve that code.
		ImageReader reader = new ImageReader();
		FileFilter[] array = loci.formats.gui.GUITools.buildFileFilters(reader);
		if (array != null) {
			FileFilter f;
			for (int i = 0; i < array.length; i++) {
				f = array[i];
				if ((f instanceof FormatFileFilter) && 
						!f.toString().contains(OmeroImageService.ZIP_EXTENSION))
					filters.add(f);
			}
		}
		return filters;
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#getFSFileSystemView()
	 */
	public FileSystemView getFSFileSystemView()
	{
		String path = (String) context.lookup(LookupNames.FS_DEFAUL_DIR);
		return gateway.getFSFileSystemView(path);
	}
	
	public Object monitor(String directory, DataObject container, 
			long userID, long groupID)
	{
		if (supportedExtensions == null) {
			List<FileFilter> l = getSupportedFileFilters();
			Iterator<FileFilter> i = l.iterator();
			List<String> formats = new ArrayList<String>();
			String description;
			String regEx = "\\*";
			String[] terms;
			String v;
			while (i.hasNext()) {
				description = (i.next()).getDescription();
				terms = description.split(regEx);
				for (int j = 1; j < terms.length; j++) {
					v = terms[j].trim();
					v = v.replaceAll(",", "");
					if (v.endsWith(")"))
						v = v.substring(0, v.length()-1);
					formats.add(v);
				}
			}
			supportedExtensions = new String[formats.size()];
			Iterator<String> k = formats.iterator();
			int index = 0;
			
			while (k.hasNext()) {
				supportedExtensions[index] = k.next();
				index++;
			}
		}
		gateway.monitor(directory, supportedExtensions, container);
		return true;
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#createMovie(long, long, List, MovieExportParam)
	 */
	public DataObject createMovie(long imageID, long pixelsID, 
			List<Integer> channels, MovieExportParam param)
		throws DSOutOfServiceException, DSAccessException
	{
		if (imageID <= 0)
			throw new IllegalArgumentException("Image ID not valid.");
		if (param == null)
			throw new IllegalArgumentException("No parameters specified.");
		if (channels == null)
			channels = new ArrayList<Integer>();
		ExperimenterData exp = (ExperimenterData) context.lookup(
				LookupNames.CURRENT_USER_DETAILS);
		long id = gateway.createMovie(imageID, pixelsID, exp.getId(), channels, 
				param);
		if (id < 0) return null;
		return context.getMetadataService().loadAnnotation(id);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#analyseFretFit(long, long, long)
	 */
	public DataObject analyseFretFit(long controlID, long toAnalyzeID, 
			long irfID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (controlID <= 0)
			throw new IllegalArgumentException("Control ID not valid.");
		if (toAnalyzeID <= 0)
			throw new IllegalArgumentException("No image to analyze.");
		long id = gateway.analyseFretFit(controlID, toAnalyzeID, irfID);
		if (id < 0) return null;
		return context.getMetadataService().loadAnnotation(id);
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
	
}
