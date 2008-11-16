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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

//Third-party libraries

//Application-internal dependencies
import omero.api.RenderingEnginePrx;
import omero.model.Channel;
import omero.model.Coating;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImagingEnvironment;
import omero.model.ImagingEnvironmentI;
import omero.model.Immersion;
import omero.model.Medium;
import omero.model.Objective;
import omero.model.ObjectiveI;
import omero.model.ObjectiveSettings;
import omero.model.ObjectiveSettingsI;
import omero.model.Pixels;
import omero.model.RenderingDef;
import omero.model.StageLabel;
import omero.model.StageLabelI;
import omero.romio.PlaneDef;
import omero.sys.PojoOptions;
import omero.util.PojoOptionsI;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.ProjectionParam;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.PixelsServicesFactory;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import pojos.ChannelData;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageAcquisitionData;
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
					new Long(pixelsID));
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
	 * @see OmeroImageService#renderImage(long, PlaneDef)
	 */
	public BufferedImage renderImage(long pixelsID, PlaneDef pDef)
		throws RenderingServiceException
	{
		try {
			return PixelsServicesFactory.render(context, new Long(pixelsID), 
					pDef);
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
		PixelsServicesFactory.shutDownRenderingControl(context, pixelsID);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#shutDownDataSink(long)
	 */
	public void shutDownDataSink(long pixelsID)
	{
		PixelsServicesFactory.shutDownRenderingControl(context, pixelsID);
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
				if (values == null)
					r.put(id, null);
				else {
					try {
						r.put(id, createImage(values));
					} catch (Exception e) {
						e.printStackTrace();
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
					new Long(pixelsID));
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
					new Long(pixelsID));
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
	 * @see OmeroImageService#getRenderingSettings(long)
	 */
	public Map getRenderingSettings(long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		Map m = gateway.getRenderingSettings(pixelsID);
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
			PojoMapper.asDataObject(
					gateway.updateObject(img, new PojoOptionsI().map()));
		image = gateway.getImage(image.getId());
		List<DatasetData> datasets =  ref.getDatasets();
		if (datasets != null && datasets.size() > 0) {
			Map map = (new PojoOptionsI()).map();
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
										null, null));
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
			gateway.createObjects(links, map);
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
	 * @see OmeroImageService#loadAcquisitionData(Object)
	 */
	public Object loadAcquisitionData(Object refObject)
		throws DSOutOfServiceException, DSAccessException
	{
		if (refObject instanceof ImageData) {
			return gateway.loadImageAcquisitionData(
					((ImageData) refObject).getId());
			
		} else if (refObject instanceof ChannelData) {
			
		}
		return null;
	}
	
	/**
	 * Saves the metadata linked to an image.
	 * 
	 * @param data
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	private void saveImageAcquisitionData(ImageAcquisitionData data)
		throws DSOutOfServiceException, DSAccessException
	{
		Image image = data.asImage();
		long id;
		String value;
		IObject object;
		//stage Label
		List<IObject> toCreate = new ArrayList<IObject>();
		List<IObject> toUpdate = new ArrayList<IObject>();
		if (data.isPositionDirty()) {
			StageLabel label;
			id = data.getPositionId();
			if (id < 0) { //create a new one.
				label = new StageLabelI();
				toCreate.add(label);
			} else {
				label = (StageLabel) gateway.findIObject(
						StageLabel.class.getName(), id);
				toUpdate.add(label);
			}
			label.setName(omero.rtypes.rstring(data.getLabelName()));
			label.setPositionX(omero.rtypes.rfloat(data.getPositionX()));
			label.setPositionY(omero.rtypes.rfloat(data.getPositionY()));
			label.setPositionZ(omero.rtypes.rfloat(data.getPositionZ()));
		}
		//Environment
		if (data.isConditionDirty()) {
			id = data.getConditionId();
			ImagingEnvironment condition;
			if (id < 0) {
				condition = new ImagingEnvironmentI();
				toCreate.add(condition);
			} else {
				condition = (ImagingEnvironment) gateway.findIObject(
						ImagingEnvironment.class.getName(), id);
				toUpdate.add(condition);
			}
			condition.setAirPressure(omero.rtypes.rfloat(
					data.getAirPressure()));
			condition.setHumidity(omero.rtypes.rfloat(
					data.getHumidity()));
			condition.setTemperature(omero.rtypes.rfloat(
					data.getTemperature()));
			condition.setCo2percent(omero.rtypes.rfloat(
					data.getCo2Percent()));
		}
		
		//TODO: check with DB update
		if (data.isObjectiveDirty()) {
			id = data.getObjectiveId();
			Objective objective;
			if (id < 0) {
				objective = new ObjectiveI();
				toCreate.add(objective);
			} else {
				objective = (Objective) gateway.findIObject(
						Objective.class.getName(), id);
				toUpdate.add(objective);
			}
			objective.setModel(omero.rtypes.rstring(data.getModel()));
			objective.setSerialNumber(omero.rtypes.rstring(
					data.getSerialNumber()));
			objective.setManufacturer(
					omero.rtypes.rstring(data.getManufacturer()));
			objective.setLensNA(omero.rtypes.rfloat(data.getLensNA()));
			objective.setNominalMagnification(omero.rtypes.rint(
					data.getNominalMagnification()));
			objective.setCalibratedMagnification(omero.rtypes.rfloat(
					data.getCalibratedMagnification()));
			value = data.getImmersion();
			if (value != null) {
				object = gateway.getEnumeration(Immersion.class, value);
				if (object != null)
					objective.setImmersion((Immersion) object);
			}
			value = data.getCoating();
			if (value != null) {
				object = gateway.getEnumeration(Coating.class, value);
				if (object != null)
					objective.setCoating((Coating) object);
			}
			objective.setWorkingDistance(omero.rtypes.rfloat(data.getWorkingDistance()));
			//getEnumerations.
			
		}
		if (data.isObjectiveSettingsDirty()) {
			id = data.getObjectiveSettingsId();
			ObjectiveSettings settings;
			if (id < 0) {
				settings = new ObjectiveSettingsI();
				toCreate.add(settings);
			} else {
				settings = (ObjectiveSettings) gateway.findIObject(
						ObjectiveSettings.class.getName(), id);
				toUpdate.add(settings);
			}
			settings.setCorrectionCollar(
					omero.rtypes.rfloat(data.getCorrectionCollar()));
			settings.setRefractiveIndex(
					omero.rtypes.rfloat(data.getRefractiveIndex()));
			value = data.getMedium();
			if (value != null) {
				object = gateway.getEnumeration(Medium.class, value);
				if (object != null)
					settings.setMedium((Medium) object);
			}
		}
		
		
		if (toUpdate.size() > 0) {
			gateway.updateObjects(toUpdate, (new PojoOptions()).map());
		}
		if (toCreate.size() > 0) {
			List<IObject> l = gateway.createObjects(toUpdate, 
					      				(new PojoOptions()).map());
			Iterator<IObject> i = l.iterator();
			image = (Image) gateway.findIObject(data.asIObject());
			while (i.hasNext()) {
				object = i.next();
				if (object instanceof StageLabel)
					image.setPosition((StageLabel) object);
				else if (object instanceof ImagingEnvironment)
					image.setCondition((ImagingEnvironment) object);
				else if (object instanceof ObjectiveSettings)
					image.setObjectiveSettings((ObjectiveSettings) object);
			}
			ModelMapper.unloadCollections(image);
			gateway.updateObject(image, (new PojoOptions()).map());
		}
	}
	
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#saveAcquisitionData(Object)
	 */
	public Object saveAcquisitionData(Object refObject)
		throws DSOutOfServiceException, DSAccessException
	{
		if (refObject instanceof ImageAcquisitionData) {
			ImageAcquisitionData data = (ImageAcquisitionData) refObject;
			saveImageAcquisitionData(data);
			return loadAcquisitionData(data.asImage());
		} else if (refObject instanceof ChannelData) {
			
		}
		return null;
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroImageService#loadPlaneInfo(long)
	 */
	public Collection loadPlaneInfo(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		Collection planes = gateway.loadPlaneInfo(pixelsID);
		return planes;
	}
	
}
