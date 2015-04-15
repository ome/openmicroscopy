/*
 * org.openmicroscopy.shoola.env.data.OmeroMetadataServiceImpl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ome.formats.model.UnitsFactory;
import ome.model.units.BigResult;
import omero.cmd.OriginalMetadataRequest;
import omero.cmd.Request;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLink;
import omero.model.BooleanAnnotation;
import omero.model.Channel;
import omero.model.DatasetAnnotationLink;
import omero.model.DoubleAnnotation;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImagingEnvironment;
import omero.model.ImagingEnvironmentI;
import omero.model.Length;
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.MapAnnotation;
import omero.model.Medium;
import omero.model.NamedValue;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.ObjectiveSettingsI;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Pressure;
import omero.model.ProjectAnnotationLink;
import omero.model.StageLabel;
import omero.model.StageLabelI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.Temperature;
import omero.model.TermAnnotation;
import omero.model.XmlAnnotation;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AnnotationLinkData;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.data.model.TableParameters;
import org.openmicroscopy.shoola.env.data.model.TableResult;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;

import pojos.util.PojoMapper;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSOutOfServiceException;

import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;

import pojos.AnnotationData;
import pojos.BooleanAnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.DoubleAnnotationData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.FilesetData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;
import pojos.LongAnnotationData;
import pojos.MapAnnotationData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ROIData;
import pojos.RatingAnnotationData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;
import pojos.TextualAnnotationData;
import pojos.XMLAnnotationData;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/** 
 * Implementation of the {@link OmeroMetadataService} I/F.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class OmeroMetadataServiceImpl 
	implements OmeroMetadataService
{

	/** Uses it to gain access to the container's services. */
	private Registry context;

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private OMEROGateway gateway;
	
	/**
	 * Updates the loaded channels.
	 * 
	 * @param ref The modified channels.
	 * @param list The channels to update.
	 */
	private void updateChannels(List<ChannelData> ref,
			List<Channel> list)
	{
		Iterator<Channel> i = list.iterator();
		Channel channel;
		ChannelData data;
		int index = 0;
		int n = ref.size();
		while (i.hasNext()) {
			channel = i.next();
			if (index == n) break;
			data = ref.get(index);
			channel.getLogicalChannel().setName(omero.rtypes.rstring(
					data.getName()));
			index++;
		}
	}
	
	/**
	 * Returns <code>true</code> if the value contains the terms specified,
	 * <code>false</code> otherwise.
	 * 
	 * @param terms The terms to check.
	 * @param value The value to handle.
	 * @return See above.
	 */
	private boolean containTerms(List<String> terms, String value)
	{
		if (terms == null || terms.size() == 0 || value == null) return false;
		Iterator<String> i = terms.iterator();
		while (i.hasNext()) {
			if (value.contains(i.next()))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the annotation is shared, 
	 * <code>false</code> otherwise.
	 * 
	 * @param ctx The security context.
	 * @param annotation The annotation to handle.
	 * @param object The object to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	private boolean isAnnotationShared(SecurityContext ctx,
			AnnotationData annotation, DataObject object)
		throws DSOutOfServiceException, DSAccessException 
	{
		List<Long> ids = new ArrayList<Long>();
		ids.add(annotation.getId());
		List l = gateway.findAnnotationLinks(ctx, object.getClass().getName(),
				-1, ids);
		if (l == null) return false;
		return l.size() > 0;
	}
	
	/**
	 * Removes the specified annotation from the object.
	 * Returns the updated object.
	 * 
	 * @param ctx The security context.
	 * @param annotation	The annotation to create. 
	 * 						Mustn't be <code>null</code>.
	 * @param object		The object to handle. Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	private void removeAnnotation(SecurityContext ctx,
			Object annotation, DataObject object) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (annotation == null || object == null ||
				(!(annotation instanceof IObject || 
					annotation instanceof DataObject))) return;
		IObject o = null;
		if (annotation instanceof IObject) o = (IObject) annotation;
		else o = ((DataObject) annotation).asIObject();
		IObject ho = gateway.findIObject(ctx, o);
		if (ho == null) return;
		long id = -1;//getUserDetails().getId();
		if (ho instanceof Annotation) {
			List links = gateway.findAnnotationLinks(ctx, object.getClass(),
				   object.getId(), Arrays.asList(ho.getId().getValue()), id);
			Iterator i = links.iterator();
			IObject link;
			while (i.hasNext()) {
				link = (IObject) i.next();
				if (link != null && gateway.canDelete(link)) {
					try {
						gateway.deleteObject(ctx, link);
					} catch (Exception e) {
					}
				}
			}
		} else {
			if (gateway.canDelete(o)) {
				try {
					gateway.deleteObject(ctx, ho);
				} catch (Exception e) {
				}
			}
		}
		
	}
	
	/**
	 * Saves the logical channel.
	 * 
	 * @param ctx The security context.
	 * @param data The data to save
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMEDS service.
	 * @throws DSAccessException
	 */
	private void saveChannelData(SecurityContext ctx, ChannelData data)
		throws DSOutOfServiceException, DSAccessException
	{
		LogicalChannel lc = data.asChannel().getLogicalChannel();
		ModelMapper.unloadCollections(lc);
		gateway.saveAndReturnObject(ctx, lc, null);
	}
	
	/**
	 * Saves the metadata linked to a logical channel.
	 * 
	 * @param ctx The security context.
	 * @param data The data to save
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMEDS service.
	 * @throws DSAccessException
	 */
	private void saveChannelAcquisitionData(SecurityContext ctx,
			ChannelAcquisitionData data)
	{
		//LogicalChannel lc = (LogicalChannel) data.asIObject();
	}
	
	/**
	 * Saves the metadata linked to an image.
	 * 
	 * @param ctx The security context.
	 * @param data The data to save
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMEDS service.
	 * @throws DSAccessException
	 */
	private void saveImageAcquisitionData(SecurityContext ctx,
			ImageAcquisitionData data)
		throws DSOutOfServiceException, DSAccessException
	{
		Image image = data.asImage();
		long id;
		IObject object;
		//stage Label
		List<IObject> toCreate = new ArrayList<IObject>();
		List<IObject> toUpdate = new ArrayList<IObject>();
		if (data.isStageLabelDirty()) {
			StageLabel label;
			id = data.getStageLabelId();
			if (id < 0) { //create a new one.
				label = new StageLabelI();
				toCreate.add(label);
			} else {
				label = (StageLabel) gateway.findIObject(ctx,
						StageLabel.class.getName(), id);
				toUpdate.add(label);
			}
			label.setName(omero.rtypes.rstring(data.getLabelName()));
            Length o = null;
            try {
                o = data.getPositionX(UnitsFactory.StageLabel_X);
            } catch (BigResult e) {
                context.getLogger().warn(
                        this,
                        "Could not get X position in "
                                + UnitsFactory.StageLabel_X);
            }
            if (o != null)
                label.setPositionX(o);
            try {
                o = data.getPositionY(UnitsFactory.StageLabel_Y);
            } catch (BigResult e) {
                context.getLogger().warn(
                        this,
                        "Could not get Y position in "
                                + UnitsFactory.StageLabel_Y);
            }
            if (o != null)
                label.setPositionY(o);
            try {
                o = data.getPositionZ(UnitsFactory.StageLabel_Z);
            } catch (BigResult e) {
                context.getLogger().warn(
                        this,
                        "Could not get Z position in "
                                + UnitsFactory.StageLabel_Z);
            }
            if (o != null)
                label.setPositionZ(o);
		}
		//Environment
		if (data.isImagingEnvironmentDirty()) {
			id = data.getImagingEnvironmentId();
			ImagingEnvironment condition;
			if (id < 0) {
				condition = new ImagingEnvironmentI();
				toCreate.add(condition);
			} else {
				condition = (ImagingEnvironment) gateway.findIObject(ctx,
						ImagingEnvironment.class.getName(), id);
				toUpdate.add(condition);
			}
            try {
                Pressure press = data
                        .getAirPressure(UnitsFactory.ImagingEnvironment_AirPressure);
                if (press != null)
                    condition.setAirPressure(press);
            } catch (BigResult e) {
                context.getLogger().warn(
                        this,
                        "Could not get pressure in "
                                + UnitsFactory.ImagingEnvironment_AirPressure);
            }
			condition.setHumidity(omero.rtypes.rdouble(
					data.getHumidity()));
            try {
                Temperature o = data
                        .getTemperature(UnitsFactory.ImagingEnvironment_Temperature);
                if (o != null)
                    condition.setTemperature(o);
            } catch (BigResult e) {
                context.getLogger().warn(
                        this,
                        "Could not get temperature in "
                                + UnitsFactory.ImagingEnvironment_Temperature);
            }
			condition.setCo2percent(omero.rtypes.rdouble(
					data.getCo2Percent()));
		}
		
		if (data.isObjectiveSettingsDirty()) {
			id = data.getObjectiveSettingsId();
			ObjectiveSettings settings;
			if (id < 0) {
				settings = new ObjectiveSettingsI();
				toCreate.add(settings);
			} else {
				settings = (ObjectiveSettings) gateway.findIObject(ctx,
						ObjectiveSettings.class.getName(), id);
				toUpdate.add(settings);
			}
			settings.setCorrectionCollar(
					omero.rtypes.rdouble(data.getCorrectionCollar()));
			settings.setRefractiveIndex(
					omero.rtypes.rdouble(data.getRefractiveIndex()));
			object = data.getMediumAsEnum();
			if (object != null)
				settings.setMedium((Medium) object);
		}
		
		long objectiveSettingsID = data.getObjectiveSettingsId();

		if (toUpdate.size() > 0) {
			gateway.updateObjects(ctx, toUpdate, new Parameters());
		}
		Iterator<IObject> i;
		if (toCreate.size() > 0) {
			List<IObject> l = gateway.createObjects(ctx, toCreate);
			i = l.iterator();
			image = (Image) gateway.findIObject(ctx, data.asIObject());
			while (i.hasNext()) {
				object = i.next();
				if (object instanceof StageLabel)
					image.setStageLabel((StageLabel) object);
				else if (object instanceof ImagingEnvironment)
					image.setImagingEnvironment((ImagingEnvironment) object);
				else if (object instanceof ObjectiveSettings) {
					objectiveSettingsID = object.getId().getValue();
					image.setObjectiveSettings((ObjectiveSettings) object);
				}
			}
			ModelMapper.unloadCollections(image);
			gateway.updateObject(ctx, image, new Parameters());
		}
		toUpdate.clear();
		toCreate.clear();
		//Now we can deal with the objective.
		//objective settings exist
		
		if (toUpdate.size() > 0) {
			gateway.updateObjects(ctx, toUpdate, new Parameters());
		} else { 
			//create the object and link it to the objective settings.
			//and link it to an instrument.
			List<IObject> l = gateway.createObjects(ctx, toCreate);
			i = l.iterator();
			ObjectiveSettings settings = (ObjectiveSettings) 
				gateway.findIObject(ctx, ObjectiveSettings.class.getName(), 
						objectiveSettingsID);
			while (i.hasNext()) {
				object = i.next();
				if (object instanceof Objective)
					settings.setObjective((Objective) object);
			}
			ModelMapper.unloadCollections(settings);
			gateway.updateObject(ctx, settings, new Parameters());
		}
	}
	
	/**
	 * Returns the current user's details.
	 * 
	 * @return See above.
	 */
	private ExperimenterData getUserDetails()
	{ 
		return (ExperimenterData) context.lookup(
									LookupNames.CURRENT_USER_DETAILS);
	}
	
	/** 
	 * Prepares the annotation to add.
	 * 
	 * @param ctx The security context.
	 * @param toAdd The collection of annotation to prepare.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMEDS service.
	 */
	private List<AnnotationData> prepareAnnotationToAdd(SecurityContext ctx,
            List<AnnotationData> toAdd)
    	throws DSOutOfServiceException, DSAccessException
    {
		List<AnnotationData> annotations = new ArrayList<AnnotationData>();
		Iterator i;
		if (toAdd == null || toAdd.size() == 0) return annotations;
		i = toAdd.iterator();
		AnnotationData ann;
		Annotation iobject = null;
		FileAnnotationData fileAnn;
		FileAnnotation fa;
		OriginalFile of;
		List<IObject> toCreate = new ArrayList<IObject>();
		//TextualAnnotationData desc;
		AnnotationData tag;
		//IObject link = null;
		//DataObject data;
		while (i.hasNext()) {
			ann = (AnnotationData) i.next();
			if (ann.getId() < 0) {
				if (ann instanceof FileAnnotationData) {
					fileAnn = (FileAnnotationData) ann;
					of = gateway.uploadFile(ctx, fileAnn.getAttachedFile(),
							fileAnn.getServerFileMimetype(), -1);
					fa = new FileAnnotationI();
					fa.setFile(of);
					iobject = fa;
				} else {
					iobject = ModelMapper.createAnnotation(ann);
				}
				if (iobject != null) {
				    boolean added = false;
				    //Check for tagset - tag link
				    if (ann instanceof TagAnnotationData) {
				        TagAnnotationData t = (TagAnnotationData) ann;
				        Set<DataObject> parents = t.getDataObjects();
				        if (CollectionUtils.isNotEmpty(parents)) {
				            Iterator<DataObject> j = parents.iterator();
				            DataObject d;
				            IObject link;
				            while (j.hasNext()) {
				                d = j.next();
				                if (d instanceof TagAnnotationData) {
				                    link = ModelMapper.linkParentToChild(
				                            t.asIObject(), d.asIObject());
				                    if (link != null) {
				                        toCreate.add(link);
				                        added = true;
				                    }
				                }
				            }
				        }
				    }
				    if (!added) {
				        toCreate.add(iobject);
				    }
				}
			} else {
				if (ann instanceof TagAnnotationData ||
					ann instanceof TermAnnotationData ||
					ann instanceof XMLAnnotationData ||
					ann instanceof MapAnnotationData) {
					//update description
					tag = (AnnotationData) ann;
					ann = (AnnotationData) updateAnnotationData(ctx, tag);
				}
				annotations.add(ann);
			}
		}

		if (toCreate.size() > 0) {
			i = toCreate.iterator();
			List<IObject> l = new ArrayList<IObject>(toCreate.size());
			while (i.hasNext()) 
				l.add((IObject) i.next());

			List<IObject> r = gateway.createObjects(ctx, l);
			toCreate.clear();
			i = r.iterator();
			while (i.hasNext()) {
                Object object = i.next();
                if (object instanceof AnnotationAnnotationLink) {
                    AnnotationAnnotationLink link =
                            (AnnotationAnnotationLink) object;
                    annotations.add((AnnotationData)
                            PojoMapper.asDataObject(link.getChild()));
                }
            }
			annotations.addAll(PojoMapper.asDataObjects(r));
		}
		return annotations;
    }

	/**
	 * Links the annotation and the data object.
	 * 
	 * @param ctx The security context.
	 * @param data The data object to annotate.
	 * @param annotation The annotation to link.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMEDS service.
	 */
	private void linkAnnotation(SecurityContext ctx, DataObject data,
			AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException
	{			
		String ioType = PojoMapper.getModelType(data.getClass()).getName();
		IObject ho = gateway.findIObject(ctx, ioType, data.getId());
		if (ho == null) return;
		ModelMapper.unloadCollections(ho);
		IObject link = null;
		boolean exist = false;

		Annotation an = annotation.asAnnotation();
        long[] expIds = new long[] { data.getOwner().getId(),
                getUserDetails().getId() };
		if (annotation instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) annotation;
			//tag a tag.
			if (TagAnnotationData.class.equals(data.getClass())) {
				link = findAnnotationLink(ctx,
						AnnotationData.class, tag.getId(),
						ho.getId().getValue(), expIds);
				if (link == null) 
					link = ModelMapper.linkAnnotation(an, (Annotation) ho);
				else exist = true;
			} else {
				link = findAnnotationLink(ctx, ho.getClass(),
						ho.getId().getValue(), tag.getId(), expIds);
				if (link == null)
					link = ModelMapper.linkAnnotation(ho, an);
				else {
					updateAnnotationData(ctx, tag);
					exist = true;
				}
			}
		}
		else if (annotation instanceof MapAnnotationData) {
			MapAnnotationData map = (MapAnnotationData) annotation;
			link = findAnnotationLink(ctx, ho.getClass(),
					ho.getId().getValue(), map.getId(), expIds);
			if (link == null)
				link = ModelMapper.linkAnnotation(ho, an);
			else {
				updateAnnotationData(ctx, map);
				exist = true;
			}
		}
		else if (annotation instanceof RatingAnnotationData) {
			clearAnnotation(ctx, data.getClass(), data.getId(),
					RatingAnnotationData.class);
			link = ModelMapper.linkAnnotation(ho, an);
		} else {
			link = ModelMapper.linkAnnotation(ho, an);
		}
		if (link != null && !exist) 
			gateway.createObject(ctx, link);
	}
	
	/**
	 * Calls {@link OMEROGateway#findAnnotationLink(SecurityContext, Class, long, long, long)}
	 * for multiple {@link ExperimenterData} ids.
	 * @param ctx The {@link SecurityContext}
	 * @param type The class
	 * @param parentID The parent Id
	 * @param childID The child Id
	 * @param userIDs Array of {@link ExperimenterData} ids
	 * @return The link (if found, <code>null</code> otherwise)
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
    private IObject findAnnotationLink(SecurityContext ctx, Class type,
            long parentID, long childID, long[] userIDs)
            throws DSOutOfServiceException, DSAccessException {
        for (long userID : userIDs) {
            IObject obj = gateway.findAnnotationLink(ctx, type, parentID,
                    childID, userID);
            if (obj != null)
                return obj;
        }
        return null;
    }
	
	/**
	 * Updates the passed annotation.
	 * 
	 * @param ctx The security context.
	 * @param ann The annotation to update.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMEDS service.
	 */
	private DataObject updateAnnotationData(SecurityContext ctx, DataObject ann)
		throws DSOutOfServiceException, DSAccessException
	{
		long id;
		String ioType;
		if (ann instanceof TagAnnotationData && ann.isDirty()) {
			TagAnnotationData tag = (TagAnnotationData) ann;
			id = tag.getId();
			ioType = PojoMapper.getModelType(TagAnnotationData.class).getName();
			TagAnnotation ho = (TagAnnotation) gateway.findIObject(ctx, ioType,
					id);
			ho.setTextValue(omero.rtypes.rstring(tag.getTagValue()));
			ho.setDescription(omero.rtypes.rstring(tag.getTagDescription()));
			IObject object = gateway.updateObject(ctx, ho, new Parameters());
			return PojoMapper.asDataObject(object);
		} else if (ann instanceof TermAnnotationData && ann.isDirty()) {
			TermAnnotationData tag = (TermAnnotationData) ann;
			id = tag.getId();
			ioType = PojoMapper.getModelType(TermAnnotationData.class).getName();
			TermAnnotation ho = (TermAnnotation) gateway.findIObject(ctx,
					ioType, id);
			ho.setTermValue(omero.rtypes.rstring(tag.getTerm()));
			ho.setDescription(omero.rtypes.rstring(tag.getTermDescription()));
			IObject object = gateway.updateObject(ctx, ho, new Parameters());
			return PojoMapper.asDataObject(object);
		} else if (ann instanceof XMLAnnotationData && ann.isDirty()) {
			XMLAnnotationData tag = (XMLAnnotationData) ann;
			id = tag.getId();
			ioType = PojoMapper.getModelType(XMLAnnotationData.class).getName();
			XmlAnnotation ho = (XmlAnnotation) gateway.findIObject(ctx,
					ioType, id);
			ho.setTextValue(omero.rtypes.rstring(tag.getText()));
			ho.setDescription(omero.rtypes.rstring(tag.getDescription()));
			IObject object = gateway.updateObject(ctx, ho, new Parameters());
			return PojoMapper.asDataObject(object);
		} else if (ann instanceof LongAnnotationData && ann.isDirty()) {
			LongAnnotationData tag = (LongAnnotationData) ann;
			id = tag.getId();
			ioType = PojoMapper.getModelType(LongAnnotationData.class).getName();
			LongAnnotation ho = (LongAnnotation) gateway.findIObject(ctx,
					ioType, id);
			ho.setLongValue(omero.rtypes.rlong(tag.getDataValue()));
			IObject object = gateway.updateObject(ctx, ho, new Parameters());
			return PojoMapper.asDataObject(object);
		} else if (ann instanceof DoubleAnnotationData && ann.isDirty()) {
			DoubleAnnotationData tag = (DoubleAnnotationData) ann;
			id = tag.getId();
			ioType = PojoMapper.getModelType(DoubleAnnotationData.class).getName();
			DoubleAnnotation ho = (DoubleAnnotation) gateway.findIObject(ctx,
					ioType, id);
			ho.setDoubleValue(omero.rtypes.rdouble(tag.getDataValue()));
			IObject object = gateway.updateObject(ctx, ho, new Parameters());
			return PojoMapper.asDataObject(object);
		} else if (ann instanceof BooleanAnnotationData && ann.isDirty()) {
			BooleanAnnotationData tag = (BooleanAnnotationData) ann;
			id = tag.getId();
			ioType = PojoMapper.getModelType(BooleanAnnotationData.class).getName();
			BooleanAnnotation ho = (BooleanAnnotation) gateway.findIObject(ctx,
					ioType, id);
			ho.setBoolValue(omero.rtypes.rbool(tag.getValue()));
			IObject object = gateway.updateObject(ctx, ho, new Parameters());
			return PojoMapper.asDataObject(object);
		}
		else if (ann instanceof MapAnnotationData && ann.isDirty()) {
			MapAnnotationData map = (MapAnnotationData) ann;
			id = map.getId();
			ioType = PojoMapper.getModelType(MapAnnotationData.class).getName();
			MapAnnotation m = (MapAnnotation) gateway.findIObject(ctx,
					ioType, id);
			m.setMapValue((List<NamedValue>) map.getContent());
			IObject object = gateway.updateObject(ctx, m, new Parameters());
			return PojoMapper.asDataObject(object);
		}
		return ann;
	}
	
	/**
	 * Converts the element of the second collection into their corresponding
	 * <code>DataObject</code>s.
	 * 
	 * @param all The list hosting the converted object.
	 * @param l   The collection of elements to convert.
	 */
	private void convert(List<DataObject> all, Collection l)
	{
		Iterator i;
		if (l == null || l.size() == 0) return;
		i = l.iterator();
		IObject object;
		while (i.hasNext()) {
			object = (IObject) i.next();
			if (object instanceof ProjectAnnotationLink) {
				all.add(PojoMapper.asDataObject(
						((ProjectAnnotationLink) object).getChild()));
			} else if (object instanceof DatasetAnnotationLink) {
				all.add(PojoMapper.asDataObject(
						((DatasetAnnotationLink) object).getChild()));
			} else if (object instanceof ImageAnnotationLink) {
				all.add(PojoMapper.asDataObject(
						((ImageAnnotationLink) object).getChild()));
			}
		}
	}
	
	/**
	 * Loads the attachments.
	 * 
	 * @param ctx The security context.
	 * @param type The type of object the attachment is related to.
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	private Collection loadAllAttachments(SecurityContext ctx, Class type,
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		List<DataObject> all = new ArrayList<DataObject>();
		if (type == null) {
			//retrieve attachment to P/D and I
			Collection l;
			l = gateway.findAllAnnotations(ctx, ProjectData.class, userID);
			if (l != null && l.size() > 0) convert(all, l);
			l = gateway.findAllAnnotations(ctx, DatasetData.class, userID);
			if (l != null && l.size() > 0) convert(all, l);
			l = gateway.findAllAnnotations(ctx, ImageData.class, userID);
			if (l != null && l.size() > 0) convert(all, l);
			return getFileAnnotations(ctx, all);
		} 
		convert(all, gateway.findAllAnnotations(ctx, type, userID));
		return getFileAnnotations(ctx, all);
	}
	
	/**
	 * Returns the file annotations.
	 * 
	 * @param ctx The security context.
	 * @param annotations The collection to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	private List<AnnotationData> getFileAnnotations(SecurityContext ctx,
			Collection annotations)
		throws DSOutOfServiceException, DSAccessException
	{
		List<AnnotationData> result = new ArrayList<AnnotationData>();
		if (annotations == null || annotations.size() == 0)
			return result;
		Iterator i = annotations.iterator();
		AnnotationData data;
		FileAnnotation fa;
		long fileID;
		OriginalFile of;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (data instanceof FileAnnotationData) {
				fa = (FileAnnotation) data.asAnnotation();
				fileID = fa.getFile().getId().getValue();
				of = gateway.getOriginalFile(ctx, fileID);
				if (of != null) 
					((FileAnnotationData) data).setContent(of);
				result.add(data);
			}
		}
		return result;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param gateway Reference to the OMERO entry point.
	 *                Mustn't be <code>null</code>.
	 * @param registry Reference to the registry. Mustn't be <code>null</code>.
	 */
	OmeroMetadataServiceImpl(OMEROGateway gateway, Registry registry)
	{
		if (registry == null)
			throw new IllegalArgumentException("No registry.");
		if (gateway == null)
			throw new IllegalArgumentException("No gateway.");
		context = registry;
		this.gateway = gateway;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadRatings(SecurityContext, Class, long, long)
	 */
	public Collection loadRatings(SecurityContext ctx, Class type, long id,
			long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<Long> ids = null;
		if (userID != -1) {
			ids = new ArrayList<Long>(1);
			ids.add(userID);
		}
		List<Long> nodeIds = new ArrayList<Long>(1);
		nodeIds.add(id);
		List<Class> types = new ArrayList<Class>();
		types.add(RatingAnnotationData.class);
		Map map = gateway.loadAnnotations(ctx, type, nodeIds, types, ids, 
				new Parameters());
		if (map == null || map.size() == 0) return new ArrayList();
		return (Collection) map.get(id);
	}

	/**
	 * Organizes the results and load more annotation is required.
	 *
	 * @param ctx The security context.
	 * @param userID The user of reference.
	 * @param annotations The collections of annotations for that object.
	 * @param results Placeholder for the results.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
    private void loadStructuredData(SecurityContext ctx,
            long userID, Collection annotations, StructuredDataResults results,
            boolean loadLinks)
        throws DSOutOfServiceException, DSAccessException
    {
        Object object = results.getRelatedObject();
        if (CollectionUtils.isNotEmpty(annotations)) {
            List<TextualAnnotationData> 
                texts = new ArrayList<TextualAnnotationData>();
            List<TagAnnotationData> tags = new ArrayList<TagAnnotationData>();
            List<TermAnnotationData> 
            terms = new ArrayList<TermAnnotationData>();
            List<FileAnnotationData> 
            attachments = new ArrayList<FileAnnotationData>();
            List<RatingAnnotationData> 
            ratings = new ArrayList<RatingAnnotationData>();
            List<XMLAnnotationData> 
            xml = new ArrayList<XMLAnnotationData>();
            List<MapAnnotationData> 
            maps = new ArrayList<MapAnnotationData>();
            
            List<AnnotationData> 
            other = new ArrayList<AnnotationData>();
            
            Iterator i = annotations.iterator();
            AnnotationData data;
            BooleanAnnotationData b;
            Map<Long, AnnotationData> map = new HashMap<Long, AnnotationData>();
            //Check link when not owner 
            List<Long> annotationIds = new ArrayList<Long>();
            while (i.hasNext()) {
                data = (AnnotationData) i.next();
                if (data instanceof TermAnnotationData) {
                    annotationIds.add(data.getId());
                    terms.add((TermAnnotationData) data);
                } else if (data instanceof TextualAnnotationData)
                    texts.add((TextualAnnotationData) data);
                else if (data instanceof TagAnnotationData) {
                    annotationIds.add(data.getId());
                    map.put(data.getId(), data);
                    tags.add((TagAnnotationData) data);
                } else if (data instanceof RatingAnnotationData)
                    ratings.add((RatingAnnotationData) data);
                else if (data instanceof FileAnnotationData) {
                    annotationIds.add(data.getId());
                    map.put(data.getId(), data);
                    attachments.add((FileAnnotationData) data);
                } else if (data instanceof XMLAnnotationData) {
                    annotationIds.add(data.getId());
                    xml.add((XMLAnnotationData) data);
                } else if (data instanceof MapAnnotationData) {
                    annotationIds.add(data.getId());
                    maps.add((MapAnnotationData) data);
                } 
                else {
                    annotationIds.add(data.getId());
                    other.add(data);
                }
            }
            //load the links tags and attachments
            if (loadLinks && annotationIds.size() > 0 && 
                !(object instanceof TagAnnotationData
                    || object instanceof FileAnnotationData)) {
                List links = gateway.findAnnotationLinks(ctx, object.getClass(),
                        results.getObjectId(), annotationIds, -1);
                formatAnnotationLinks(links, results);
            }
            results.setOtherAnnotation(other);
            results.setXMLAnnotations(xml);
            results.setTextualAnnotations(texts);
            results.setTerms(terms);
            results.setTags(tags);
            results.setRatings(ratings);
            results.setAttachments(attachments);
            results.setMapAnnotations(maps);
        }
    }
    
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadStructuredData(SecurityContext, DataObject, long, boolean)
	 */
	public StructuredDataResults loadStructuredData(SecurityContext ctx,
			Object object, long userID, boolean viewed) 
	    throws DSOutOfServiceException, DSAccessException 
	{
		if (object == null)
			throw new IllegalArgumentException("Object not valid.");
		StructuredDataResults results = null;
		DataObject r = null;
		if (object instanceof File) {
			File f = (File) object;
			DataObject fd = gateway.loadFolder(f.getAbsolutePath());
			//load the data object if any.
			return new StructuredDataResults(fd);
		} else if (object instanceof DataObject) {
			r = (DataObject) object;
			results = new StructuredDataResults(r);
		}
		if (r == null)
			throw new IllegalArgumentException("Data Object not initialized.");
		
		Collection annotations = loadStructuredAnnotations(ctx,
				object.getClass(), r.getId(), userID);
		loadStructuredData(ctx, userID, annotations, results, true);
		//in-place import check
		if (object instanceof ImageData) {
			ImageData img = (ImageData) object;
			long fID = img.getFilesetId();
			if (fID >=0) {
				Map<Long, Collection<AnnotationData>> map =
					loadAnnotations(ctx, FilesetData.class, Arrays.asList(fID),
						TextualAnnotationData.class,
						Arrays.asList(AnnotationData.FILE_TRANSFER_NS), null);
				results.setTransferlinks(map.get(fID));
			}
		}
		return results;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadStructuredData(SecurityContext, List, long, boolean)
	 */
	public Map<DataObject, StructuredDataResults>
		loadStructuredData(SecurityContext ctx, List<DataObject> data,
			long userID, boolean viewed) 
	    throws DSOutOfServiceException, DSAccessException 
	{
		if (data == null)
			throw new IllegalArgumentException("Object not valid.");
		Map<DataObject, StructuredDataResults> 
			results = new HashMap<DataObject, StructuredDataResults>();
		Iterator<DataObject> i = data.iterator();
		DataObject n;
		List<Long> ids = new ArrayList<Long>();
		Class<?> klass = null;
		List<Long> fids = new ArrayList<Long>();
		ImageData img;
		while (i.hasNext()) {
			n = i.next();
			if (n != null) {
			    ids.add(n.getId());
			    if (klass == null) {
			        klass = n.getClass();
			    }
			    if (n instanceof ImageData) {
			        img = (ImageData) n;
			        long fID = img.getFilesetId();
			        if (fID >=0 && !fids.contains(fID)) {
			            fids.add(fID);
			        }
			    }
			}
		}
		Map<Long, Collection<AnnotationData>> filesetMap =
		        new HashMap<Long, Collection<AnnotationData>>();
		if (!fids.isEmpty()) {
		    filesetMap = loadAnnotations(ctx, FilesetData.class, fids,
                    TextualAnnotationData.class,
                    Arrays.asList(AnnotationData.FILE_TRANSFER_NS), null);
		}
		//load all the annotations
		List<Long> usersIDs = null;
        if (userID != -1) {
            usersIDs = new ArrayList<Long>(1);
            usersIDs.add(userID);
        }
        Map map = gateway.loadAnnotations(ctx, klass, ids, null, usersIDs,
                new Parameters());
        Multimap<Long, IObject> linkMap = ArrayListMultimap.create();
        if (!(klass.equals(TagAnnotationData.class) ||
                klass.equals(FileAnnotationData.class))) {
            Collection values = map.values();
            Iterator k = values.iterator();
            List<Long> annotationIds = new ArrayList<Long>();
            while (k.hasNext()) {
                Collection l = (Collection) k.next();
                Iterator j = l.iterator();
                while (j.hasNext()) {
                    AnnotationData object = (AnnotationData) j.next();
                    if (!annotationIds.contains(object.getId()))
                        annotationIds.add(object.getId());
                    
                }
               
            }
            if (CollectionUtils.isNotEmpty(annotationIds)) {
                linkMap = gateway.findAnnotationLinks(ctx, klass, ids,
                        annotationIds, userID);
            }
        }
        //format the results
        i = data.iterator();
        StructuredDataResults r;
        List<IObject> links;
        while (i.hasNext()) {
            n = i.next();
            if (n != null) {
                r = new StructuredDataResults(n);
                loadStructuredData(ctx, userID,
                        (Collection) map.get(n.getId()), r, false);
                results.put(n, r);
                if (n instanceof ImageData) {
                    img = (ImageData) n;
                    r.setTransferlinks(filesetMap.get(img.getFilesetId()));
                }
                formatAnnotationLinks(linkMap.get(n.getId()), r);
            }
        }
		return results;
	}

	/**
	 * Formats the annotation links.
	 *
	 * @param links The links to handle.
	 * @param results The placeholder for result.
	 */
	private void formatAnnotationLinks(Collection<IObject> links,
	        StructuredDataResults results)
	{
	    if (CollectionUtils.isEmpty(links)) return;
	    Map<DataObject, ExperimenterData>
	    m = new HashMap<DataObject, ExperimenterData>();
	    Iterator j = links.iterator();
	    IObject link;
	    DataObject d;
	    List<AnnotationLinkData>
	    l = new ArrayList<AnnotationLinkData>();
	    IObject ho;
	    while (j.hasNext()) {
	        link = (IObject) j.next();
	        ho = ModelMapper.getChildFromLink(link);
	        d = PojoMapper.asDataObject(ho);
	        l.add(new AnnotationLinkData(link, d,
	                PojoMapper.asDataObject(
	                        ModelMapper.getParentFromLink(link))));
	        if (d != null)
	            m.put(d, (ExperimenterData) PojoMapper.asDataObject(
	                    link.getDetails().getOwner()));
	    }
	    results.setLinks(m);
	    results.setAnnotationLinks(l);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#annotate(SecurityContext, DataObject, AnnotationData)
	 */
	public DataObject annotate(SecurityContext ctx, DataObject toAnnotate,
			AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException
	{
		if (toAnnotate == null)
			throw new IllegalArgumentException("DataObject cannot be null");
		ctx = gateway.checkContext(ctx, toAnnotate);
		return annotate(ctx, toAnnotate.getClass(), toAnnotate.getId(),
				annotation);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#annotate(SecurityContext, Class, long, AnnotationData)
	 */
	public DataObject annotate(SecurityContext ctx, Class type, long id,
			AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException 
	{
		if (annotation == null)
			throw new IllegalArgumentException("DataObject cannot be null");
		String ioType = PojoMapper.getModelType(type).getName();
		IObject ho = gateway.findIObject(ctx, ioType, id);
		ModelMapper.unloadCollections(ho);
		IObject link = null;
		boolean exist = false;
		IObject annObject;
		ExperimenterData exp = getUserDetails();
		if (annotation instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) annotation;
			//tag a tag
			if (TagAnnotationData.class.equals(type)) {
				if (tag.getId() <= 0) {
					TagAnnotation ann = new TagAnnotationI();
		    		ann.setTextValue(
		    				omero.rtypes.rstring(tag.getContentAsString()));
		    		ann.setDescription(
		    				omero.rtypes.rstring(tag.getTagDescription()));
		    		link = ModelMapper.linkAnnotation(ann, (Annotation) ho);
				} else {
					annObject = tag.asIObject();
					ModelMapper.unloadCollections(annObject);
					link = gateway.findAnnotationLink(ctx,
							AnnotationData.class, tag.getId(),
							ho.getId().getValue(), exp.getId());
					if (link == null) 
						link = ModelMapper.linkAnnotation(annObject, 
								(Annotation) ho);
				}
			} else {
				if (tag.getId() <= 0) 
					link = ModelMapper.createAnnotationAndLink(ho, annotation);
				else {
					annObject = tag.asIObject();
					ModelMapper.unloadCollections(annObject);
					link = gateway.findAnnotationLink(ctx, ho.getClass(),
												ho.getId().getValue(),
												tag.getId(), exp.getId());
					if (link == null)
						link = ModelMapper.linkAnnotation(ho, 
								(Annotation) annObject);
					else exist = true;
				}
			}
		} else if (annotation instanceof RatingAnnotationData) {
			//only one annotation of type rating.
			//Remove the previous ones.
			clearAnnotation(ctx, type, id, RatingAnnotationData.class);
			link = ModelMapper.createAnnotationAndLink(ho, annotation);
		} else if (annotation instanceof FileAnnotationData) {
			FileAnnotationData ann = (FileAnnotationData) annotation;
			if (ann.getId() < 0) {
				OriginalFile of = gateway.uploadFile(ctx, ann.getAttachedFile(),
						ann.getServerFileMimetype(), -1);
				FileAnnotation fa = new FileAnnotationI();
				fa.setFile(of);
				link = ModelMapper.linkAnnotation(ho, fa);
			} else {
				annObject = ann.asIObject();
				ModelMapper.unloadCollections(annObject);
				OriginalFile of = ((FileAnnotation) annObject).getFile();
				link = ModelMapper.linkAnnotation(ho, (Annotation) annObject);
			}
		} else
			link = ModelMapper.createAnnotationAndLink(ho, annotation);
		if (link != null) {
			IObject object;
			if (exist) object = link;
			else object = gateway.createObject(ctx, link);
			return PojoMapper.asDataObject(
								ModelMapper.getAnnotatedObject(object));
		}
		
		return null;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#clearAnnotation(SecurityContext, Class, long, Class)
	 */
	public void clearAnnotation(SecurityContext ctx, Class type, long id,
			Class annotationType)
		throws DSOutOfServiceException, DSAccessException
	{
		if (type == null)
			throw new IllegalArgumentException("No object specified.");
		long userID = getUserDetails().getId();
		Collection annotations = loadStructuredAnnotations(ctx, type, id,
				userID);
		if (annotations == null || annotations.size() == 0)
			return;
		List<IObject> toRemove = new ArrayList<IObject>(); 
		List<Long> ids = new ArrayList<Long>(); 
		Iterator i = annotations.iterator();
		AnnotationData data;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (annotationType == null || 
					data.getClass().equals(annotationType)) {
				toRemove.add(data.asIObject());
				ids.add(data.getId());
			} 
		}
		List l = null;
		String klass = PojoMapper.getModelType(type).getName();
		if (ids.size() != 0)
			l = gateway.findAnnotationLinks(ctx, klass, id, ids);
		if (l != null) {
			i = l.iterator();
			IObject o;
			while (i.hasNext()) {
				o = (IObject) i.next();
				if (gateway.canDelete(o))
					gateway.deleteObject(ctx, o);
			}
				

			//Need to check if the object is not linked to other object.
			
			i = toRemove.iterator();
			IObject obj;
			while (i.hasNext()) {
				obj = (IObject) i.next();
				ids = new ArrayList<Long>(); 
				ids.add(obj.getId().getValue());
				l = gateway.findAnnotationLinks(ctx, klass, -1, ids);
				if (l == null || l.size() == 0) {
					if (gateway.canDelete(obj))
						gateway.deleteObject(ctx, obj);
				}
					
			}
		}
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#clearAnnotation(SecurityContext, DataObject, Class)
	 */
	public void clearAnnotation(SecurityContext ctx, DataObject object,
			Class annotationType) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No object specified.");
		ctx = gateway.checkContext(ctx, object);
		clearAnnotation(ctx, object.getClass(), object.getId(),
				annotationType);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#clearAnnotation(SecurityContext, DataObject)
	 */
	public void clearAnnotation(SecurityContext ctx, DataObject object) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No object specified.");
		ctx = gateway.checkContext(ctx, object);
		clearAnnotation(ctx, object.getClass(), object.getId(), null);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadStructuredAnnotations(SecurityContext, Class, long, long)
	 */
	public Collection loadStructuredAnnotations(SecurityContext ctx, Class type,
			long id, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (id < 0) new ArrayList<Long>();
		if (type == null) 
			throw new IllegalArgumentException("No type specified.");
		List<Long> ids = null;
		if (userID != -1) {
			ids = new ArrayList<Long>(1);
			ids.add(userID);
		}
		List<Long> objects = new ArrayList<Long>(1);
		objects.add(id);
		Map map = gateway.loadAnnotations(ctx, type, objects, null, ids,
				new Parameters());
		return (Collection) map.get(id);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadAnnotations(SecurityContext, Class, String, long)
	 */
	public Collection loadAnnotations(SecurityContext ctx, Class annotationType,
	        String nameSpace, long userID)
	                throws DSOutOfServiceException, DSAccessException
	{
	    ParametersI po = new ParametersI();
	    if (userID >= 0) po.exp(omero.rtypes.rlong(userID));
	    List<String> toInclude = new ArrayList<String>();
	    List<String> toExclude = new ArrayList<String>();
	    if (nameSpace != null) 
	        toInclude.add(nameSpace);
        if (TagAnnotationData.class.equals(annotationType)) {
            po.orphan();
            return gateway.loadTagSets(ctx, po);
        }
	    if (FileAnnotationData.class.equals(annotationType)) {
	        if (!FileAnnotationData.COMPANION_FILE_NS.equals(nameSpace))
	            toExclude.add(FileAnnotationData.COMPANION_FILE_NS);
	        if (!FileAnnotationData.MEASUREMENT_NS.equals(nameSpace))
	            toExclude.add(FileAnnotationData.MEASUREMENT_NS);
	        if (!FileAnnotationData.FLIM_NS.equals(nameSpace))
	            toExclude.add(FileAnnotationData.FLIM_NS);
	        if (!FileAnnotationData.EXPERIMENTER_PHOTO_NS.equals(nameSpace))
	            toExclude.add(FileAnnotationData.EXPERIMENTER_PHOTO_NS);
	        if (!FileAnnotationData.LOG_FILE_NS.equals(nameSpace))
	            toExclude.add(FileAnnotationData.LOG_FILE_NS);
	    }
	    return gateway.loadSpecificAnnotation(ctx, annotationType, toInclude,
	            toExclude, po);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#saveData(SecurityContext, Collection, List, List, long)
	 */
	public Object saveData(SecurityContext ctx, Collection<DataObject> data,
		List<AnnotationData> toAdd, List<Object> toRemove, long userID) 
			throws DSOutOfServiceException, DSAccessException
	{
		if (data == null)
			throw new IllegalArgumentException("No data to save");
		Iterator<DataObject> j = data.iterator();
		DataObject object;
		OmeroDataService service = context.getDataService();
		
		
		List<AnnotationData> annotations = prepareAnnotationToAdd(ctx, toAdd);
		Iterator i;
		
		j = data.iterator();
		//First create the new annotations 
		AnnotationData ann;
		List<DataObject> updated = new ArrayList<DataObject>();
		List<Long> ids = new ArrayList<Long>();
		while (j.hasNext()) {
			object = j.next();
			if (!ids.contains(object.getId())) {
				ids.add(object.getId());
				if (object instanceof AnnotationData) {
					updateAnnotationData(ctx, object);
				} else {
					if (object.isLoaded() && object.isDirty())
						updated.add(service.updateDataObject(ctx, object));
					else updated.add(object);
				}
				if (annotations.size() > 0) {
					i = annotations.iterator();
					while (i.hasNext()) {
						ann = (AnnotationData) i.next();
						if (ann != null)
							linkAnnotation(ctx, object, ann);
					}
				}
				if (toRemove != null) {
					Iterator<Object> k = toRemove.iterator();
					List<IObject> toDelete = new ArrayList<IObject>();
					Object o;
					while (k.hasNext()) {
						o = k.next();
						if (o != null) {
							removeAnnotation(ctx, o, object);
							if (o instanceof TextualAnnotationData) {
								ann = (AnnotationData) o;
								if (!isAnnotationShared(ctx, ann, object)) {
									toDelete.add(ann.asIObject());
								}
							}
						}
					}
					if (toDelete.size() > 0) {
						gateway.deleteObjects(ctx, toDelete);
					}
				}
			}
		}
		return updated;//data;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#saveBatchData(SecurityContext, Collection, List, List, long)
	 */
	public Object saveBatchData(SecurityContext ctx,
		Collection<DataObject> data, List<AnnotationData> toAdd,
		List<Object> toRemove, long userID) 
			throws DSOutOfServiceException, DSAccessException
	{
		if (data == null)
			throw new IllegalArgumentException("No data to save");
		OmeroDataService service = context.getDataService();
		Iterator i;
		Iterator<DataObject> j = data.iterator();
		DataObject object, child;
		List<Long> ids;
		Set images = null;
		Parameters po = new Parameters();
		Iterator k;
		List result = null;
		//First create the new annotations 
		List<AnnotationData> annotations = prepareAnnotationToAdd(ctx, toAdd);
		List<Long> childrenIds = new ArrayList<Long>();
		while (j.hasNext()) {
			object = j.next();
			if (result == null) result = new ArrayList();
			//Need code for tag 
			if (object instanceof DatasetData) {
				//retrieve all images in the dataset.
				//Tmp solution, this code should be pushed server side.
				ids = new ArrayList<Long>(1);
				ids.add(object.getId());
				images = gateway.getContainerImages(ctx, DatasetData.class,
						ids, po);
				if (images != null) {
					k = images.iterator();
					while (k.hasNext()) {
						child = (DataObject) k.next();
						if (!childrenIds.contains(child.getId())) {
							result.add(child);
							childrenIds.add(child.getId());
							if (annotations != null) {
								i = annotations.iterator();
								while (i.hasNext())
									linkAnnotation(ctx, child,
											(AnnotationData) i.next());
							}
							if (toRemove != null) {
								i = toRemove.iterator();
								while (i.hasNext())
									removeAnnotation(ctx, i.next(), child);
							}
						}
					}
				}
			} else if (object instanceof PlateData) {
				//Load all the wells
				images = gateway.loadPlateWells(ctx, object.getId(), -1);
				if (images != null) {
					k = images.iterator();
					while (k.hasNext()) {
						child = (DataObject) k.next();
						if (!childrenIds.contains(child.getId())) {
							result.add(child);
							childrenIds.add(child.getId());
							if (annotations != null) {
								i = annotations.iterator();
								while (i.hasNext())
									linkAnnotation(ctx, child, 
											(AnnotationData) i.next());
							}
							if (toRemove != null) {
								i = toRemove.iterator();
								while (i.hasNext())
									removeAnnotation(ctx,
										(AnnotationData) i.next(), child);
							}
						}
					}
				}
			} else if (object instanceof ImageData) {
				service.updateDataObject(ctx, object);
				if (annotations != null) {
					i = annotations.iterator();
					while (i.hasNext())
						linkAnnotation(ctx, object, (AnnotationData) i.next());
						//annotate(object, (AnnotationData) i.next());
				}
				if (toRemove != null) {
					i = toRemove.iterator();
					while (i.hasNext())
						removeAnnotation(ctx, (AnnotationData) i.next(),
								object);
				}
			}
		}
		if (result == null) return data;
		return result;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#saveBatchData(SecurityContext, TimeRefObject, List, List, long)
	 */
	public Object saveBatchData(SecurityContext ctx, TimeRefObject data,
		List<AnnotationData> toAdd, List<Object> toRemove, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (data == null)
			throw new IllegalArgumentException("No data to save");
		OmeroDataService service = context.getDataService();
		Collection images = service.getImagesPeriod(ctx, data.getStartTime(),
			data.getEndTime(), userID, true);
		List r = new ArrayList();
		if (images == null) return r;
		Iterator i = images.iterator();
		DataObject child;
		Iterator j;
		//First create the new annotations 
		List<AnnotationData> annotations = prepareAnnotationToAdd(ctx, toAdd);
		while (i.hasNext()) {
			child = (DataObject) i.next();
			r.add(child);
			if (annotations != null) {
				j = annotations.iterator();
				while (j.hasNext()) 
					linkAnnotation(ctx, child, (AnnotationData) i.next());
			}
			if (toRemove != null) {
				j = toRemove.iterator();
				while (j.hasNext())
					removeAnnotation(ctx, j.next(), child);
			}
		}
		return r;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#downloadFile(SecurityContext, String, int)
	 */
	public File downloadFile(SecurityContext ctx, File file, long fileID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (fileID < 0)
			throw new IllegalArgumentException("File ID not valid");
		if (file == null)
			throw new IllegalArgumentException("File path not valid");
		return gateway.downloadFile(ctx, file, fileID);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadRatings(SecurityContext, Class, List, long)
	 */
	public Map<Long, Collection> loadRatings(SecurityContext ctx,
		Class nodeType, List<Long> nodeIds, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<Long> ids = null;
		if (userID != -1) {
			ids = new ArrayList<Long>(1);
			ids.add(userID);
		}
		List<Class> types = new ArrayList<Class>();
		types.add(RatingAnnotationData.class);
		Map map = gateway.loadAnnotations(ctx, nodeType, nodeIds, types, ids,
				new Parameters());
		Map<Long, Collection> results = new HashMap<Long, Collection>();
		if (map == null) return results;
		Entry entry;
		
		Iterator i = map.entrySet().iterator();
		Long id;
		AnnotationData data;
		Iterator j;
		List<AnnotationData> result;
		Collection l;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			id = (Long) entry.getKey();
			l = (Collection) entry.getValue();
			result = new ArrayList<AnnotationData>();
			j = l.iterator();
			while (j.hasNext()) {
				data = (AnnotationData) j.next();
				if (data instanceof RatingAnnotationData)
					result.add(data);
			}
			if (result.size() > 0)
				results.put(id, result);
		}
		return results;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#filterByAnnotation(SecurityContext,
	 * Class, List, Class, List, long)
	 */
	public Collection filterByAnnotation(SecurityContext ctx, Class nodeType,
		List<Long> nodeIds, Class annotationType, List<String> terms,
		long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		Set<Long> results = new HashSet<Long>();
		
		List<Long> ids = null;
		if (userID != -1) {
			ids = new ArrayList<Long>(1);
			ids.add(userID);
		}
		List<Class> types = new ArrayList<Class>();
		types.add(annotationType);
		Map map = gateway.loadAnnotations(ctx, nodeType, nodeIds, types, ids,
				new Parameters());
		if (map == null || map.size() == 0) return results;
		long id;
		Collection l;
		AnnotationData data;
		Iterator i, j;
		Entry entry;
		Map<Long, List<Long>> m = new HashMap<Long, List<Long>>();
		List<Long> nodes;
		if (terms != null && terms.size() > 0) {
			//retrieve the annotations corresponding to the specified terms.
			//List annotations = gateway.filterBy(annotationType, terms,
			//		                           null, null, exp);
			
			i = map.entrySet().iterator();
			while (i.hasNext()) {
				entry = (Entry) i.next();
				id = (Long) entry.getKey();
				l = (Collection) entry.getValue();
				j = l.iterator();
				while (j.hasNext()) {
					data = (AnnotationData) j.next();
					if (annotationType.equals(TagAnnotationData.class)) {
						if (data instanceof TagAnnotationData) {
							if (terms.contains(
									((TagAnnotationData) data).getTagValue())) {
								nodes = m.get(data.getId());
								if (nodes == null) {
									nodes = new ArrayList<Long>();
									nodes.add(id);
								}
								if (!nodes.contains(id))
									nodes.add(id);
								m.put(data.getId(), nodes);
							}
						}
					} else if (annotationType.equals(
							 TextualAnnotationData.class)) {
						if (data instanceof TextualAnnotationData) {
							if (containTerms(terms, 
									((TextualAnnotationData) data).getText())) {
								nodes = m.get(data.getId());
								if (nodes == null) {
									nodes = new ArrayList<Long>();
									nodes.add(id);
								}
								if (!nodes.contains(id))
									nodes.add(id);
								m.put(data.getId(), nodes);
							}
						}
					}
				}
			}
		} else
			return filterByAnnotated(ctx, nodeType, nodeIds, annotationType,
					true, userID);
		
		i = m.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			id = (Long) entry.getKey();
			nodes = (List) entry.getValue();
			//if (results.size() == 0) results.addAll(nodes);
			//else results = ListUtils.intersection(results, nodes);
			results.addAll(nodes);
		}
		return results;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#filterByAnnotated(SecurityContext,
	 * Class, List, Class, boolean, long)
	 */
	public Collection filterByAnnotated(SecurityContext ctx, Class nodeType,
		List<Long> nodeIds, Class annotationType, boolean annotated,
		long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<Long> results = new ArrayList<Long>();
	
		List<Long> ids = null;
		if (userID != -1) {
			ids = new ArrayList<Long>(1);
			ids.add(userID);
		}
		
		List<Class> types = new ArrayList<Class>();
		types.add(annotationType);
		Map map = gateway.loadAnnotations(ctx, nodeType, nodeIds, types, ids,
				new Parameters());
		if (map == null || map.size() == 0) return results;
		long id;
		Collection l;
		AnnotationData data;
		Iterator i, j;
		Entry entry;
		i = map.entrySet().iterator();
		if (annotated) {
			while (i.hasNext()) {
				entry = (Entry) i.next();
				id = (Long) entry.getKey();
				l = (Collection) entry.getValue();
				j = l.iterator();
				while (j.hasNext()) {
					data = (AnnotationData) j.next();
					if (annotationType.equals(TagAnnotationData.class)) {
						if (data instanceof TagAnnotationData) {
							if (!results.contains(id)) {
								results.add(id);
							}
						}
					} else if (annotationType.equals(
							TextualAnnotationData.class)) {
						if (!(data instanceof TagAnnotationData)) {
							if (!results.contains(id)) {
								results.add(id);
							}
						}
					}
				}
			}
		} else {
			List<Long> toExclude = new ArrayList<Long>();
			results.addAll(nodeIds);
			while (i.hasNext()) {
				entry = (Entry) i.next();
				id = (Long) entry.getKey();
				l = (Collection) entry.getValue();
				j = l.iterator();
				
				while (j.hasNext()) {
					data = (AnnotationData) j.next();
					if (annotationType.equals(TagAnnotationData.class)) {
						if (data instanceof TagAnnotationData) {
							if (!toExclude.contains(id)) {
								toExclude.add(id);
							}
						}
					} else if (annotationType.equals(
							TextualAnnotationData.class)) {
						if (!(data instanceof TagAnnotationData)) {
							if (!toExclude.contains(id)) {
								toExclude.add(id);
							}
						}
					}
				}
			}
			results.removeAll(toExclude);
		}
		return results;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#filterByAnnotation(SecurityContext, Class,
	 * List, FilterContext, long)
	 */
	public Collection filterByAnnotation(SecurityContext ctx, Class nodeType,
		List<Long> ids, FilterContext filter, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (filter == null)
			throw new IllegalArgumentException("No filtering context.");
		int rateIndex = filter.getRateIndex();
		int roiIndex = filter.getRoiIndex();
		List<Long> filteredNodes = new ArrayList<Long>();

		List<Long> userIDs = null;
		if (userID != -1) {
			userIDs = new ArrayList<Long>(1);
			userIDs.add(userID);
		}
		List<Class> annotationTypes = new ArrayList<Class>();
		//types.add(annotationType);
		Map map = gateway.loadAnnotations(ctx, nodeType, ids, annotationTypes,
				userIDs, new Parameters());
		
		if (map == null || map.size() == 0) {
			if (rateIndex == FilterContext.EQUAL && filter.getRate() == 0)
				return ids;
		}
		
		//TODO: retrieve the experimenter corresponding to the passed id.
		ExperimenterData exp = getUserDetails();
		
		Timestamp start = filter.getFromDate();
		Timestamp end = filter.getToDate();
		Set<Long> annotationsIds = new HashSet<Long>();
		Iterator i, j, k;
		Long id;
		Collection l;
		List annotations;
		int resultType = filter.getResultType();
		Map<Class, List<String>> types = filter.getAnnotationType();
		
		Map<Class, List<Long>> r = new HashMap<Class, List<Long>>();
		List<Long> found;
		Class type;
		Entry entry;
		if (types != null && types.size() > 0) {
			i = types.entrySet().iterator();
			Map<Long, List<Long>> m = new HashMap<Long, List<Long>>();
			List<Long> nodes;
			AnnotationData data;
			if (resultType == FilterContext.INTERSECTION) {
				while (i.hasNext()) {
					entry = (Entry) i.next();
					type = (Class) entry.getKey();
					found = new ArrayList<Long>();
					annotations = gateway.filterBy(ctx, type, (List<String>)
							entry.getValue(), start, end, exp);
					j = annotations.iterator();
					while (j.hasNext()) 
						annotationsIds.add((Long) j.next());
					
					j = map.entrySet().iterator();
					while (j.hasNext()) {
						entry = (Entry) j.next();
						id = (Long) entry.getKey();
						l = (Collection) entry.getValue();
						if (l.size() >= annotations.size()) {
							k = l.iterator();
							while (k.hasNext()) {
								data = (AnnotationData) k.next();
								if (annotations.contains(data.getId())) {
									nodes = m.get(data.getId());
									if (nodes == null) {
										nodes = new ArrayList<Long>();
										nodes.add(id);
									}
									if (!nodes.contains(id))
										nodes.add(id);
									m.put(data.getId(), nodes);
								}
							}
						}
					}
					j = m.entrySet().iterator();
					while (j.hasNext()) {
						entry = (Entry) j.next();
						id = (Long) entry.getKey();
						nodes = (List) entry.getValue();
						if (found.size() == 0) found.addAll(nodes);
						else found = ListUtils.intersection(found, nodes);
					}
					r.put(type, found);
				}
			} else if (resultType == FilterContext.UNION) {
				while (i.hasNext()) {
					type = (Class) i.next();
					annotations = gateway.filterBy(ctx, type, types.get(type),
						start, end, exp);
					i = annotations.iterator();
					while (i.hasNext())
						annotationsIds.add((Long) i.next());
				}

				i = map.entrySet().iterator();
				
				while (i.hasNext()) {
					entry = (Entry) i.next();
					id = (Long) entry.getKey();
					l = (Collection) entry.getValue();
					j = l.iterator();
					while (j.hasNext()) {
						data = (AnnotationData) j.next();
						if (annotationsIds.contains(data.getId()))
							filteredNodes.add(id);
					}
				}
			}
		}
		
		if(roiIndex != -1) {
                    found = new ArrayList<Long>();
                    for (long imgId : ids) {
                        List<ROIResult> roiResults = gateway.loadROI(ctx, imgId, null,
                                userID);
                        List rois = new ArrayList();
                        for (ROIResult tmp : roiResults) {
                            rois.addAll(tmp.getROIs());
                        }
                        int numberOfRois = filter.getROIs();
                        switch (roiIndex) {
                            case FilterContext.EQUAL:
                                if (rois.size() == numberOfRois) {
                                    found.add(imgId);
                                }
                                break;
                            case FilterContext.LOWER_EQUAL:
                                if (rois.size() <= numberOfRois) {
                                    found.add(imgId);
                                }
                                break;
                            case FilterContext.GREATER_EQUAL:
                                if (rois.size() >= numberOfRois) {
                                    found.add(imgId);
                                }
                                break;
                        }
                    }
                    if (resultType == FilterContext.UNION)
                        filteredNodes.addAll(found);
                    else if (resultType == FilterContext.INTERSECTION)
                        r.put(ROIData.class, found);
		}
		
		if (rateIndex != -1) {
			int rate = filter.getRate();
			int value;
			i = map.keySet().iterator();
			AnnotationData data;
			found = new ArrayList<Long>();
			switch (rateIndex) {
				case FilterContext.EQUAL:
					if (rate == 0) { //unrated element.
						found.addAll(ids);
						while (i.hasNext()) 
							found.remove(i.next());
					} else {
						
						while (i.hasNext()) {
			    			id = (Long) i.next();
			    			l = (Collection) map.get(id);
			    			j = l.iterator();
			    			while (j.hasNext()) {
								data = (AnnotationData) j.next();
								if (data instanceof RatingAnnotationData) {
									value = ((RatingAnnotationData) 
			    							data).getRating();
									if (rate == value) 
										found.add(id);
								}
							}
			    		}
					}
					break;
				case FilterContext.LOWER_EQUAL:
					if (rate == 0) { //unrated element.
						found.addAll(ids);
						
						while (i.hasNext()) 
							found.remove(i.next());
					} else {
						while (i.hasNext()) {
			    			id = (Long) i.next();
			    			l = (Collection) map.get(id);
			    			j = l.iterator();
			    			while (j.hasNext()) {
								data = (AnnotationData) j.next();
								if (data instanceof RatingAnnotationData) {
									value = ((RatingAnnotationData) 
			    							data).getRating();
									if (value <= rate) found.add(id);
								}
							}
			    		}
					}
					break;
				case FilterContext.GREATER_EQUAL:
					while (i.hasNext()) {
		    			id = (Long) i.next();
		    			l = (Collection) map.get(id);
		    			j = l.iterator();
		    			while (j.hasNext()) {
							data = (AnnotationData) j.next();
							if (data instanceof RatingAnnotationData) {
								value = ((RatingAnnotationData) 
		    							data).getRating();
								if (value >= rate) found.add(id);
							}
						}
		    		}
			}
			if (resultType == FilterContext.UNION)
				filteredNodes.addAll(found);
			else if (resultType == FilterContext.INTERSECTION)
				r.put(RatingAnnotationData.class, found);
		}
		if (resultType == FilterContext.UNION)
			return filteredNodes;
		
		//Intersection.
		filteredNodes.clear();
		
		if (r.size() == 0) return filteredNodes;
		
		int index = 0;
		type = null;
		/*
		while (i.hasNext()) {
			type = (Class) i.next();
			if (index == 0) {
				filteredNodes.addAll(r.get(type));
				break;
			}
			index++;
		}
		r.remove(type);
		*/
		i = r.values().iterator();
		if(!i.hasNext()) {
		    return filteredNodes;
		}
		found = (List)i.next();
		filteredNodes.addAll(found);
		while (i.hasNext()) {
		        found = (List)i.next();
			filteredNodes = ListUtils.intersection(filteredNodes, found);
		}
		
		return filteredNodes;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#getEnumeration(SecurityContext, String)
	 */
	public Collection getEnumeration(SecurityContext ctx, String type) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getEnumerations(ctx, type);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadAcquisitionData(SecurityContext, Object)
	 */
	public Object loadAcquisitionData(SecurityContext ctx, Object refObject)
		throws DSOutOfServiceException, DSAccessException
	{
		if (refObject instanceof ImageData) {
			ctx = gateway.checkContext(ctx, (ImageData) refObject);
			return gateway.loadImageAcquisitionData(ctx,
					((ImageData) refObject).getId());
		} else if (refObject instanceof ChannelData) {
			ctx = gateway.checkContext(ctx, (ChannelData) refObject);
			Channel c = ((ChannelData) refObject).asChannel();
			if (c.getLogicalChannel() == null) return null;
			long id = c.getLogicalChannel().getId().getValue();
			return gateway.loadChannelAcquisitionData(ctx, id);
		}
		return null;
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadInstrument(SecurityContext, long)
	 */
	public Object loadInstrument(SecurityContext ctx, long instrumentID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (instrumentID <= 0) return null;
		return gateway.loadInstrument(ctx, instrumentID);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#saveAcquisitionData(SecurityContext, Object)
	 */
	public Object saveAcquisitionData(SecurityContext ctx, Object refObject)
		throws DSOutOfServiceException, DSAccessException
	{
		if (refObject instanceof ImageAcquisitionData) {
			ImageAcquisitionData data = (ImageAcquisitionData) refObject;
			ctx = gateway.checkContext(ctx, data);
			saveImageAcquisitionData(ctx, data);
			return null;//loadAcquisitionData(data.asImage());
		} else if (refObject instanceof ChannelData) {
			ChannelData data = (ChannelData) refObject;
			ctx = gateway.checkContext(ctx, data);
			saveChannelData(ctx, data);
		} else if (refObject instanceof ChannelAcquisitionData) {
			ChannelAcquisitionData data = (ChannelAcquisitionData) refObject;
			ctx = gateway.checkContext(ctx, data);
			saveChannelAcquisitionData(ctx, data);
		}
		return null;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#archivedFile(SecurityContext ctx,
	 * FileAnnotationData, File, int, DataObject)
	 */
	public Object archivedFile(SecurityContext ctx,
		FileAnnotationData fileAnnotation, File file, int index,
		DataObject linkTo) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (file == null) 
			throw new IllegalArgumentException("No file to save.");
		if (fileAnnotation == null) return null;
		ctx = gateway.checkContext(ctx, fileAnnotation);
		//Upload the file back to the server
		long id = fileAnnotation.getId();
		long originalID = fileAnnotation.getFileID();
		OriginalFile of = gateway.uploadFile(ctx, file,
				fileAnnotation.getServerFileMimetype(), originalID);
		//Need to relink and delete the previous one.
		FileAnnotation fa;
		String desc = fileAnnotation.getDescription();
		if (id < 0) {
			fa = new FileAnnotationI();
			fa.setFile(of);
			if (desc != null) fa.setDescription(omero.rtypes.rstring(desc));
			IObject object = gateway.createObject(ctx, fa);
			id = object.getId().getValue();
		} else {
			fa = (FileAnnotation) 
				gateway.findIObject(ctx, FileAnnotation.class.getName(), id);
			fa.setFile(of);
			if (desc != null) fa.setDescription(omero.rtypes.rstring(desc));
			gateway.updateObject(ctx, fa, new Parameters());
		}
		fa = (FileAnnotation) 
			gateway.findIObject(ctx, FileAnnotation.class.getName(), id);
		FileAnnotationData data = 
			(FileAnnotationData) PojoMapper.asDataObject(fa);
		if (of != null) {
			data.setContent(of);
		}	
		if (linkTo != null) {
			if (linkTo.getId() > 0) {
				annotate(ctx, linkTo, data);
			}
		}
		return data;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadTags(SecurityContext, Long,
	 * boolean, long, long)
	 */
	public Collection loadTags(SecurityContext ctx, Long id,
		boolean topLevel, long userID, long groupID)
		throws DSOutOfServiceException, DSAccessException
	{
		ParametersI po = new ParametersI();
		if (userID >= 0) po.exp(omero.rtypes.rlong(userID));
		if (groupID >= 0) po.grp(omero.rtypes.rlong(groupID));
		if (topLevel) {
			po.orphan();
			return gateway.loadTagSets(ctx, po);
		}
		return gateway.loadTags(ctx, id, po);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#countFileType(SecurityContext, long, int)
	 */
	public long countFileType(SecurityContext ctx, long userID, int fileType)
		throws DSOutOfServiceException, DSAccessException
	{
		List<String> include = new ArrayList<String>();
		List<String> exclude = new ArrayList<String>();
		switch (fileType) {
			case MOVIE:
				include.add(FileAnnotationData.MOVIE_NS);
				break;
			case TAG_NOT_OWNED:
				return gateway.countAnnotationsUsedNotOwned(ctx,
						TagAnnotationData.class, userID);
			case OTHER:
			default:
				exclude.add(FileAnnotationData.MOVIE_NS);
				exclude.add(FileAnnotationData.COMPANION_FILE_NS);
				exclude.add(FileAnnotationData.MEASUREMENT_NS);
				exclude.add(FileAnnotationData.FLIM_NS);
				exclude.add(FileAnnotationData.EXPERIMENTER_PHOTO_NS);
				exclude.add(FileAnnotationData.LOG_FILE_NS);
		}
		ParametersI po = new ParametersI();
		if (userID >= 0) po.exp(omero.rtypes.rlong(userID));
		return gateway.countSpecificAnnotation(ctx, FileAnnotationData.class,
				include, exclude, po);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadFiles(SecurityContext, int, long)
	 */
	public Collection loadFiles(SecurityContext ctx, int fileType, long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		List<String> include = new ArrayList<String>();
		List<String> exclude = new ArrayList<String>();
		ParametersI po = new ParametersI();
		if (userID >= 0) po.exp(omero.rtypes.rlong(userID));
		switch (fileType) {
			case MOVIE:
				include.add(FileAnnotationData.MOVIE_NS);
				break;
			case TAG_NOT_OWNED:
				return gateway.loadAnnotationsUsedNotOwned(ctx,
						TagAnnotationData.class, userID);
			case OTHER:
			default:
				exclude.add(FileAnnotationData.MOVIE_NS);
				exclude.add(FileAnnotationData.COMPANION_FILE_NS);
				exclude.add(FileAnnotationData.MEASUREMENT_NS);
				exclude.add(FileAnnotationData.FLIM_NS);
				exclude.add(FileAnnotationData.EXPERIMENTER_PHOTO_NS);
				exclude.add(FileAnnotationData.LOG_FILE_NS);
		}
		
		return gateway.loadSpecificAnnotation(ctx, FileAnnotationData.class,
				include, exclude, po);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadAnnotation(SecurityContext, long)
	 */
	public DataObject loadAnnotation(SecurityContext ctx, long annotationID)
			throws DSOutOfServiceException, DSAccessException
	{
		//Tmp code
		Set<DataObject> set = gateway.loadAnnotation(ctx, 
				Arrays.asList(annotationID));
		if (set.size() != 1) return null;
		Iterator<DataObject> i = set.iterator();
		while (i.hasNext()) {
			return i.next();
		}
		return null;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadTabularData(SecurityContext ctx, 
	 * TableParameters, long)
	 */
	public List<TableResult> loadTabularData(SecurityContext ctx,
		TableParameters parameters, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (parameters == null)
			throw new IllegalArgumentException("No parameters specified.");
		return gateway.loadTabularData(ctx, parameters, userID);
	}

        /**
         * Implemented as specified by {@link OmeroImageService}.
         * 
         * @see OmeroMetadataService#loadParentsOfAnnotations(SecurityContext, long)
         */
        public List<DataObject> loadParentsOfAnnotations(SecurityContext ctx, long annotationId) throws DSOutOfServiceException,
                DSAccessException {
    
            ExperimenterData exp = (ExperimenterData) context
                    .lookup(LookupNames.CURRENT_USER_DETAILS);
            long userId = exp.getId();
    
            return loadParentsOfAnnotations(ctx, annotationId, userId);
        }

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadParentsOfAnnotations(SecurityContext, long, long)
	 */
	public List<DataObject> loadParentsOfAnnotations(SecurityContext ctx,
		long annotationId, long userId)
		throws DSOutOfServiceException, DSAccessException
	{
		if (annotationId < 0)
			throw new IllegalArgumentException("Annotation id not valid.");
		//Check possible links
		
		List links = gateway.findLinks(ctx, FileAnnotation.class, annotationId,
				userId);
		List<DataObject> nodes = new ArrayList<DataObject>();
		if (links != null) {
			Iterator j = links.iterator();
			Object o;
			while (j.hasNext()) {
				o = j.next();
				if (o instanceof ProjectAnnotationLink) {
					nodes.add(PojoMapper.asDataObject(
							((ProjectAnnotationLink) o).getParent()));
				} else if (o instanceof DatasetAnnotationLink) {
					nodes.add(PojoMapper.asDataObject(
							((DatasetAnnotationLink) o).getParent()));
				} else if (o instanceof ImageAnnotationLink) {
					nodes.add(PojoMapper.asDataObject(
							((ImageAnnotationLink) o).getParent()));
				}
			}
		}
		return nodes;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#saveChannelData(SecurityContext, List, List)
	 */
	public List<Long> saveChannelData(SecurityContext ctx,
			List<ChannelData> channels, List<DataObject> objects)
			throws DSOutOfServiceException, DSAccessException
	{
		List<Long> images = new ArrayList<Long>();
		if (channels == null || channels.size() == 0) return images;
		//Update the channels only
		if (objects == null || objects.size() == 0) return images;
		List<IObject> pixels = gateway.getPixels(ctx, objects);
		if (pixels == null) return images;
		Iterator<IObject> i = pixels.iterator();
		Pixels p;
		int sizeC;
		int n = channels.size();
		List<IObject> toUpdate = new ArrayList<IObject>();
		List<Channel> l;
		while (i.hasNext()) {
			p = (Pixels) i.next();
			sizeC = p.getSizeC().getValue();
			if (sizeC == n) {
				l = p.copyChannels();
				updateChannels(channels, l);
				toUpdate.addAll(l);
				images.add(p.getImage().getId().getValue());
			}
		}
		//Update the channels now
		gateway.saveAndReturnObject(ctx, toUpdate, null, null);
		return images;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getChannelsMetadata(SecurityContext, long)
	 */
	public List<ChannelData> getChannelsMetadata(SecurityContext ctx,
			long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		Pixels pixels = gateway.getPixels(ctx, pixelsID);
		if (pixels == null) return new ArrayList<ChannelData>();
		Collection l = pixels.copyChannels();
		if (l == null) return new ArrayList<ChannelData>();
		Iterator i = l.iterator();
		List<ChannelData> m = new ArrayList<ChannelData>(l.size());
		int index = 0;
		while (i.hasNext()) {
			m.add(new ChannelData(index, (Channel) i.next()));
			index++;
		}
		return m;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadAnnotations(SecurityContext, Class, List,
	 * Class, List, List)
	 */
	public Map<Long, Collection<AnnotationData>>
		loadAnnotations(SecurityContext ctx, Class<?> rootType,
			List<Long> rootIDs, Class<?> annotationType, List<String> nsInclude,
			List<String> nsExclude)
		throws DSOutOfServiceException, DSAccessException
	{
		if (rootType == null || CollectionUtils.isEmpty(rootIDs))
			throw new IllegalArgumentException("No node specified");
		if (annotationType == null)
			throw new IllegalArgumentException("No annotation type specified");
		//always exclude the log file
		if (nsExclude == null) nsExclude = new ArrayList<String>();
		nsExclude.add(FileAnnotationData.LOG_FILE_NS);
		return gateway.loadSpecifiedAnnotationsLinkedTo(ctx, rootType, rootIDs,
				annotationType, nsInclude, nsExclude, new Parameters());
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#downloadMetadataFile(SecurityContext, File, long)
	 */
	public RequestCallback downloadMetadataFile(SecurityContext ctx,
			File file, long id)
			throws DSOutOfServiceException, DSAccessException, ProcessException
	{
		//Get the file annotation
		if (id < 0) return null;
		OriginalMetadataRequest cmd = new OriginalMetadataRequest();
		cmd.imageId = id;
		return gateway.submit(Arrays.<Request>asList(cmd), ctx);
	}

    /**
     * Implemented as specified by {@link OmeroDataService}.
     * @see OmeroDataService#loadLogFiles(SecurityContext, Class, List)
     */
    public Map<Long, List<IObject>> loadLogFiles(SecurityContext ctx,
            Class<?> rootType, List<Long> rootIDs)
                    throws DSOutOfServiceException, DSAccessException
   {
        if (rootType == null || CollectionUtils.isEmpty(rootIDs))
            throw new IllegalArgumentException("No node specified");
        return gateway.loadLogFiles(ctx, rootType, rootIDs);
    }
}
