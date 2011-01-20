/*
 * org.openmicroscopy.shoola.env.data.OmeroMetadataServiceImpl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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



//Java imports
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//Third-party libraries
import org.apache.commons.collections.ListUtils;

//Application-internal dependencies
import omero.model.Annotation;
import omero.model.Channel;
import omero.model.DatasetAnnotationLink;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImagingEnvironment;
import omero.model.ImagingEnvironmentI;
import omero.model.LogicalChannel;
import omero.model.Medium;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.ObjectiveSettingsI;
import omero.model.OriginalFile;
import omero.model.ProjectAnnotationLink;
import omero.model.StageLabel;
import omero.model.StageLabelI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.FilterContext;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.env.data.util.ViewedByDef;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import pojos.AnnotationData;
import pojos.BooleanAnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.URLAnnotationData;

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
	private Registry                context;

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private OMEROGateway            gateway;
	
	/** Reference to the dummy folder wrapping a file on the file system. */
	//private FolderData				folder;
	
	/**
	 * Creates or recycles the folder wrapping the passed file.
	 * 
	 * @param file The file to wrap.
	 * @return See above.
	 */
	private DataObject createFolder(File file)
	{
		/*
		if (folder == null) folder = new FolderData();
		folder.setFile(file);
		return folder;
		*/
		return null;
	}
	
	/**
	 * Removes the specified annotation from the object.
	 * Returns the updated object.
	 * @param annotation	The annotation to create. 
	 * 						Mustn't be <code>null</code>.
	 * @param object		The object to handle. Mustn't be <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	private DataObject removeAnnotation(AnnotationData annotation, 
										DataObject object) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (annotation == null)
			throw new IllegalArgumentException("No annotation to remove.");
		if (object == null)
			throw new IllegalArgumentException("No object to handle.");
		IObject ho = gateway.findIObject(annotation.asIObject());
		IObject link = gateway.findAnnotationLink(object.getClass(), 
				                         object.getId(), ho.getId().getValue());
		if (ho != null && link != null) {
			gateway.deleteObject(link);
			//Check that the annotation is not shared.
			/*
			List<Long> ids = new ArrayList<Long>();
			ids.add(ho.getId().getValue());
			List l = gateway.findAnnotationLinks(object.getClass().getName(), 
					-1, ids);
			if (l == null || l.size() == 0)
				gateway.deleteObject(ho);//oly work if the annotation is not shared
				*/
		}
		return PojoMapper.asDataObject(gateway.findIObject(object.asIObject()));
	}

	
	/**
	 * Saves the logical channel.
	 * 
	 * @param data The data to save
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service.
	 * @throws DSAccessException
	 */
	private void saveChannelData(ChannelData data)
		throws DSOutOfServiceException, DSAccessException
	{
		LogicalChannel lc = data.asChannel().getLogicalChannel();
		ModelMapper.unloadCollections(lc);
		gateway.updateObject(lc, new Parameters());
	}
	
	/**
	 * Saves the metadata linked to a logical channel.
	 * 
	 * @param data The data to save
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service.
	 * @throws DSAccessException
	 */
	private void saveChannelAcquisitionData(ChannelAcquisitionData data)
	{
		//LogicalChannel lc = (LogicalChannel) data.asIObject();
	}
	
	/**
	 * Saves the metadata linked to an image.
	 * 
	 * @param data The data to save
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service.
	 * @throws DSAccessException
	 */
	private void saveImageAcquisitionData(ImageAcquisitionData data)
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
				label = (StageLabel) gateway.findIObject(
						StageLabel.class.getName(), id);
				toUpdate.add(label);
			}
			label.setName(omero.rtypes.rstring(data.getLabelName()));
			Object o = data.getPositionX();
			if (o != null)
				label.setPositionX(omero.rtypes.rdouble((Float) o));
			o = data.getPositionY();
			if (o != null)
				label.setPositionY(omero.rtypes.rdouble((Float) o));
			o = data.getPositionZ();
			if (o != null)
				label.setPositionZ(omero.rtypes.rdouble((Float) o));
		}
		//Environment
		if (data.isImagingEnvironmentDirty()) {
			id = data.getImagingEnvironmentId();
			ImagingEnvironment condition;
			if (id < 0) {
				condition = new ImagingEnvironmentI();
				toCreate.add(condition);
			} else {
				condition = (ImagingEnvironment) gateway.findIObject(
						ImagingEnvironment.class.getName(), id);
				toUpdate.add(condition);
			}
			condition.setAirPressure(omero.rtypes.rdouble(
					data.getAirPressure()));
			condition.setHumidity(omero.rtypes.rdouble(
					data.getHumidity()));
			Object o = data.getTemperature();
			if (o != null)
				condition.setTemperature(omero.rtypes.rdouble((Float) o));
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
				settings = (ObjectiveSettings) gateway.findIObject(
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
			gateway.updateObjects(toUpdate, new Parameters());
		}
		Iterator<IObject> i;
		if (toCreate.size() > 0) {
			List<IObject> l = gateway.createObjects(toCreate);
			i = l.iterator();
			image = (Image) gateway.findIObject(data.asIObject());
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
			gateway.updateObject(image, new Parameters());
		}
		toUpdate.clear();
		toCreate.clear();
		//Now we can deal with the objective.
		//objective settings exist
		
		if (toUpdate.size() > 0) {
			gateway.updateObjects(toUpdate, new Parameters());
		} else { 
			//create the object and link it to the objective settings.
			//and link it to an instrument.
			List<IObject> l = gateway.createObjects(toCreate);
			i = l.iterator();
			ObjectiveSettings settings = (ObjectiveSettings) 
				gateway.findIObject(ObjectiveSettings.class.getName(), 
						objectiveSettingsID);
			while (i.hasNext()) {
				object = i.next();
				if (object instanceof Objective)
					settings.setObjective((Objective) object);
			}
			ModelMapper.unloadCollections(settings);
			gateway.updateObject(settings, new Parameters());
		}
	}
	
	/**
	 * Loads the description of the passed tag for the specified user.
	 * 
	 * @param tag	 The tag to handle.
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	private TagAnnotationData loadTagDescription(TagAnnotationData tag, 
			                                    long userID)
	    throws DSOutOfServiceException, DSAccessException 
	{
		Collection descriptions;
		descriptions = loadTextualAnnotations(TagAnnotationData.class, 
				                              tag.getId(), userID);
		if (descriptions != null && descriptions.size() > 0)
		tag.setTagDescriptions((List) descriptions);
		return tag;
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
	 * @param toAdd The collection of annotation to prepare.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMEDS service.
	 */
	private List<AnnotationData> prepareAnnotationToAdd(
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
		List<Annotation> toCreate = new ArrayList<Annotation>();
		//List<IObject> links = new ArrayList<IObject>();
		//TextualAnnotationData desc;
		TagAnnotationData tag;
		//IObject link = null;
		//DataObject data;
		while (i.hasNext()) {
			ann = (AnnotationData) i.next();
			if (ann.getId() < 0) {
				if (ann instanceof FileAnnotationData) {
					fileAnn = (FileAnnotationData) ann;
					of = gateway.uploadFile(fileAnn.getAttachedFile(), 
							fileAnn.getServerFileFormat(), -1);
					fa = new FileAnnotationI();
					fa.setFile(of);
					iobject = fa;
				} else {
					/*
					if (ann instanceof TagAnnotationData) {
						IObject r = gateway.createObject(
								ModelMapper.createAnnotation(ann), map);
						tag = (TagAnnotationData) ann;
						desc = tag.getTagDescription();
						if (desc != null) {
							link = ModelMapper.createAnnotationAndLink(r, desc);
							if (link != null) 
								gateway.createObject(link, map);
						}
						data = PojoMapper.asDataObject(r);
						if (data != null)
							annotations.add((AnnotationData) data);
					} else 
					*/
					iobject = ModelMapper.createAnnotation(ann);
				} 
				if (iobject != null)
					toCreate.add(iobject);

			} else {
				if (ann instanceof TagAnnotationData) {
					//update description
					tag = (TagAnnotationData) ann;
					ann = (TagAnnotationData) updateAnnotationData(tag);
				}
				annotations.add(ann);
			}
		}

		if (toCreate.size() > 0) {
			i = toCreate.iterator();
			List<IObject> l = new ArrayList<IObject>(toCreate.size());
			while (i.hasNext()) 
				l.add((IObject) i.next());

			List<IObject> r = gateway.createObjects(l);
			annotations.addAll(PojoMapper.asDataObjects(r));
		}
		/*
		if (links.size() > 0) {
			i = links.iterator();
			List<IObject> l = new ArrayList<IObject>(toCreate.size());
			while (i.hasNext()) 
				l.add((IObject) i.next());
			gateway.createObjects(l);
		}
		*/
		return annotations;
    }

	/**
	 * Links the annotation and the data object.
	 * 
	 * @param data       The data object to annotate.
	 * @param annotation The annotation to link.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMEDS service.
	 */
	private void linkAnnotation(DataObject data, AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException
	{
		String ioType = gateway.convertPojos(data.getClass()).getName();
		IObject ho = gateway.findIObject(ioType, data.getId());
		ModelMapper.unloadCollections(ho);
		IObject link = null;
		boolean exist = false;
		
		//Annotation an = (Annotation) gateway.findIObject(gateway.convertPojos(
		//		annotation.getClass()), annotation.getId());
		//ModelMapper.unloadCollections(an);
		Annotation an = annotation.asAnnotation();
		if (annotation instanceof TagAnnotationData) {
			TagAnnotationData tag = (TagAnnotationData) annotation;
			//tag a tag.
			if (TagAnnotationData.class.equals(data.getClass())) {
				link = gateway.findAnnotationLink(
						AnnotationData.class, tag.getId(), 
						ho.getId().getValue());
				if (link == null) 
					link = ModelMapper.linkAnnotation(an, (Annotation) ho);
				else exist = true;
			} else {
				link = gateway.findAnnotationLink(ho.getClass(), 
						ho.getId().getValue(), tag.getId());
				if (link == null)
					link = ModelMapper.linkAnnotation(ho, an);
				else {
					updateAnnotationData(tag);
					exist = true;
				}
			}
		} else if (annotation instanceof RatingAnnotationData) {
			clearAnnotation(data.getClass(), data.getId(), 
					RatingAnnotationData.class);
			link = ModelMapper.linkAnnotation(ho, an);
		} else {
			link = ModelMapper.linkAnnotation(ho, an);
		}
		if (link != null && !exist) 
			gateway.createObject(link);
	}
	
	/**
	 * Updates the passed annotation.
	 * 
	 * @param ann The annotation to update.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMEDS service.
	 */
	private DataObject updateAnnotationData(DataObject ann)
		throws DSOutOfServiceException, DSAccessException
	{
		long id;
		String ioType;
		TagAnnotation ho;
		IObject link = null;
		if (ann instanceof TagAnnotationData) {
			/*
			TagAnnotationData tag = (TagAnnotationData) ann;
			TextualAnnotationData description = tag.getTagDescription();
			//if (description != null) {
			id = tag.getId();
			if (id >= 0) {
				gateway.removeTagDescription(id, getUserDetails().getId());
			}	
			ioType = gateway.convertPojos(TagAnnotationData.class).getName();
			ho = (TagAnnotation) gateway.findIObject(ioType, id);
			
			ModelMapper.unloadCollections(ho);
			link = ModelMapper.createAnnotationAndLink(ho, description);
			if (link != null) 
				gateway.createObject(link, (new PojoOptionsI()).map());
			RString string = ho.getTextValue();
			if (!tag.getTagValue().equals(string.getValue())) {
				ho.setTextValue(omero.rtypes.rstring(tag.getTagValue()));
				IObject object = 
					gateway.updateObject(ho, (new PojoOptionsI()).map());
				return PojoMapper.asDataObject(object);
			}
			*/
			TagAnnotationData tag = (TagAnnotationData) ann;
			id = tag.getId();
			ioType = gateway.convertPojos(TagAnnotationData.class).getName();
			ho = (TagAnnotation) gateway.findIObject(ioType, id);
			ho.setTextValue(omero.rtypes.rstring(tag.getTagValue()));
			ho.setDescription(omero.rtypes.rstring(tag.getTagDescription()));
			IObject object = gateway.updateObject(ho, new Parameters());
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
	 * @param type		The type of object the attachment is related to.
	 * @param userID	The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	private Collection loadAllAttachments(Class type, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		List<DataObject> all = new ArrayList<DataObject>();
		if (type == null) {
			//retrieve attachment to P/D and I
			Collection l;
			l = gateway.findAllAnnotations(ProjectData.class, userID);
			if (l != null && l.size() > 0) convert(all, l);
			l = gateway.findAllAnnotations(DatasetData.class, userID);
			if (l != null && l.size() > 0) convert(all, l);
			l = gateway.findAllAnnotations(ImageData.class, userID);
			if (l != null && l.size() > 0) convert(all, l);
			return getFileAnnotations(all);
		} 
		convert(all, gateway.findAllAnnotations(type, userID));
		return getFileAnnotations(all);
	}
	
	/**
	 * Returns the file annotations.
	 * 
	 * @param annotations The collection to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	private List<AnnotationData> getFileAnnotations(Collection annotations)
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
				of = gateway.getOriginalFile(fileID);
				if (of != null) 
					((FileAnnotationData) data).setContent(of);
				result.add(data);
			}
		}
		return result;
	}
	
	/**
	 * Loads the descriptions of the tags.
	 * 
	 * @param type
	 * @param id
	 * @param userID
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	private Collection loadTextualAnnotations(Class type, long id, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<Long> ids = new ArrayList<Long>(1);
		ids.add(id);
		List<Class> annotationTypes = new ArrayList<Class>(1);
		annotationTypes.add(TextualAnnotationData.class);
		List<Long> annotators = new ArrayList<Long>(1);
		annotators.add(userID);
		Map annotations = gateway.loadAnnotations(type, ids, 
				annotationTypes, annotators, new Parameters());
		List<AnnotationData> result = new ArrayList<AnnotationData>();
		if (annotations == null || annotations.size() == 0)
			return result;
		Collection l = (Collection) annotations.get(id);
		/*
		Collection annotations = loadStructuredAnnotations(type, id, userID);
		if (annotations == null || annotations.size() == 0)
			return annotations;
			*/
		Iterator i = l.iterator();
		
		AnnotationData data;
		while (i.hasNext()) {
			data = (AnnotationData) i.next();
			if (data instanceof TextualAnnotationData)
				result.add(data);
		}
		return result;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param gateway   Reference to the OMERO entry point.
	 *                  Mustn't be <code>null</code>.
	 * @param registry  Reference to the registry. Mustn't be <code>null</code>.
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
	 * @see OmeroMetadataService#loadRatings(Class, long, long)
	 */
	public Collection loadRatings(Class type, long id, long userID) 
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
		Map map = gateway.loadAnnotations(type, nodeIds, types, ids, 
				new Parameters());
		if (map == null || map.size() == 0) return new ArrayList();
		return (Collection) map.get(id);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadStructuredData(DataObject, long, boolean)
	 */
	public StructuredDataResults loadStructuredData(Object object, 
			                                        long userID, boolean viewed) 
	    throws DSOutOfServiceException, DSAccessException 
	{
		if (object == null)
			throw new IllegalArgumentException("Object not valid.");
		StructuredDataResults results = null;
		DataObject r = null;
		if (object instanceof File) {
			File f = (File) object;
			DataObject fd = gateway.loadFolder(f.getAbsolutePath());
			if (fd == null)
				fd = createFolder(f);
			//load the data object if any.
			results = new StructuredDataResults(fd);
			return results;
		} else if (object instanceof DataObject) {
			r = (DataObject) object;
			results = new StructuredDataResults(r);
		}
		if (r == null)
			throw new IllegalArgumentException("Data Object not initialized.");
		
		Collection annotations = loadStructuredAnnotations(object.getClass(),
													r.getId(), userID);
		if (annotations != null && annotations.size() > 0) {
			List<AnnotationData> texts = new ArrayList<AnnotationData>();
			List<AnnotationData> tags = new ArrayList<AnnotationData>();
			List<AnnotationData> urls = new ArrayList<AnnotationData>();
			List<AnnotationData> attachments = new ArrayList<AnnotationData>();
			List<AnnotationData> ratings = new ArrayList<AnnotationData>();
			List<AnnotationData> published = new ArrayList<AnnotationData>();
			Iterator i = annotations.iterator();
			AnnotationData data;
			BooleanAnnotationData b;
			while (i.hasNext()) {
				data = (AnnotationData) i.next();
				if (data instanceof URLAnnotationData)
					urls.add(data);
				else if (data instanceof TextualAnnotationData)
					texts.add(data);
				else if ((data instanceof TagAnnotationData)) {
					tags.add(data);
					/*
					if (!(object instanceof TagAnnotationData)) {
						tag = loadTagDescription((TagAnnotationData) data, 
				                userID);
						tags.add(tag);
					} else isChild = false;
					*/
					//need to load the description
				} else if (data instanceof RatingAnnotationData)
					ratings.add(data);
				else if (data instanceof FileAnnotationData) {
					attachments.add(data);
				} else if (data instanceof BooleanAnnotationData) {
					b = (BooleanAnnotationData) data;
					if (BooleanAnnotationData.INSIGHT_PUBLISHED_NS.equals(
							b.getNameSpace()))
						published.add(data);
				}
			}
			/*
			if ((object instanceof TagAnnotationData) && isChild) {
				List<Long> ids = new ArrayList<Long>();
				ids.add(object.getId());
				Collection r = gateway.fetchAnnotations(ids, userID, true);
				if (r != null) {
					i = r.iterator();
					while (i.hasNext()) {
						data = (AnnotationData) i.next();
						if (data instanceof TagAnnotationData) {
							tag = loadTagDescription((TagAnnotationData) data, 
					                userID);
			                tags.add(tag);
						}
					}
				}
			}
			*/
			results.setTextualAnnotations(texts);
			results.setUrls(urls);
			results.setTags(tags);
			results.setRatings(ratings);
			results.setAttachments(attachments);
			results.setPublished(published);
		}
	
		/*
		if ((object instanceof ImageData) && viewed) {
			ImageData img = (ImageData) object;
			long pixelsID = -1;
			try {
				pixelsID = img.getDefaultPixels().getId();
			} catch (Exception e) {}
			if (pixelsID >= 0)
				results.setViewedBy(loadViewedBy(img.getId(), pixelsID));
		}
		*/
		return results;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadStructuredData(List, long, boolean)
	 */
	public Map loadStructuredData(List<DataObject> data, long userID, 
								boolean viewed) 
	    throws DSOutOfServiceException, DSAccessException 
	{
		if (data == null)
			throw new IllegalArgumentException("Object not valid.");
		
		Map<Long, StructuredDataResults> 
			results = new HashMap<Long, StructuredDataResults>();
		Iterator<DataObject> i = data.iterator();
		DataObject node;
		while (i.hasNext()) {
			node = i.next();
			if (node != null) {
				results.put(node.getId(), 
						loadStructuredData(node, userID, viewed));
			}
		}
		return results;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#annotate(DataObject, AnnotationData)
	 */
	public DataObject annotate(DataObject toAnnotate, AnnotationData annotation)
		throws DSOutOfServiceException, DSAccessException
	{
		if (toAnnotate == null)
			throw new IllegalArgumentException("DataObject cannot be null");
		return annotate(toAnnotate.getClass(), toAnnotate.getId(), annotation);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#annotate(Class, long, AnnotationData)
	 */
	public DataObject annotate(Class type, long id, AnnotationData annotation) 
		throws DSOutOfServiceException, DSAccessException 
	{
		if (annotation == null)
			throw new IllegalArgumentException("DataObject cannot be null");
		String ioType = gateway.convertPojos(type).getName();
		IObject ho = gateway.findIObject(ioType, id);
		ModelMapper.unloadCollections(ho);
		IObject link = null;
		boolean exist = false;
		IObject annObject;
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
					link = gateway.findAnnotationLink(
							AnnotationData.class, tag.getId(), 
							ho.getId().getValue());
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
					link = gateway.findAnnotationLink(ho.getClass(), 
												ho.getId().getValue(), 
												tag.getId());
					if (link == null)
						link = ModelMapper.linkAnnotation(ho, 
								(Annotation) annObject);
					else exist = true;
				}
			}
		} else if (annotation instanceof RatingAnnotationData) {
			//only one annotation of type rating.
			//Remove the previous ones.
			clearAnnotation(type, id, RatingAnnotationData.class);
			link = ModelMapper.createAnnotationAndLink(ho, annotation);
		} else if (annotation instanceof FileAnnotationData) {
			FileAnnotationData ann = (FileAnnotationData) annotation;
			if (ann.getId() < 0) {
				OriginalFile of = gateway.uploadFile(ann.getAttachedFile(), 
						ann.getServerFileFormat(), -1);
				FileAnnotation fa = new FileAnnotationI();
				fa.setFile(of);
				link = ModelMapper.linkAnnotation(ho, fa);
			} else {
				annObject = ann.asIObject();
				ModelMapper.unloadCollections(annObject);
				link = ModelMapper.linkAnnotation(ho, (Annotation) annObject);
			}
			
		} else
			link = ModelMapper.createAnnotationAndLink(ho, annotation);
		if (link != null) {
			IObject object;
			if (exist) object = link;
			else object = gateway.createObject(link);
			return PojoMapper.asDataObject(
								ModelMapper.getAnnotatedObject(object));
		}
		
		return null;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#clearAnnotation(Class, long, Class)
	 */
	public void clearAnnotation(Class type, long id, Class annotationType)
		throws DSOutOfServiceException, DSAccessException
	{
		if (type == null)
			throw new IllegalArgumentException("No object specified.");
		long userID = getUserDetails().getId();
		Collection annotations = loadStructuredAnnotations(type, id, userID);
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
		String klass = gateway.convertPojos(type).getName();
		if (ids.size() != 0)
			l = gateway.findAnnotationLinks(klass, id, ids);
		if (l != null) {
			i = l.iterator();
			while (i.hasNext()) {
				gateway.deleteObject((IObject) i.next());
			}
			
			//Need to check if the object is not linked to other object.
			
			i = toRemove.iterator();
			IObject obj;
			while (i.hasNext()) {
				obj = (IObject) i.next();
				ids = new ArrayList<Long>(); 
				ids.add(obj.getId().getValue());
				l = gateway.findAnnotationLinks(klass, -1, ids);
				if (l == null || l.size() == 0) gateway.deleteObject(obj);
			}
		}
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#clearAnnotation(DataObject, Class)
	 */
	public void clearAnnotation(DataObject object, Class annotationType) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No object specified.");
		clearAnnotation(object.getClass(), object.getId(), annotationType);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#clearAnnotation(DataObject)
	 */
	public void clearAnnotation(DataObject object) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new IllegalArgumentException("No object specified.");
		clearAnnotation(object.getClass(), object.getId(), null);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadStructuredAnnotations(Class, long, long)
	 */
	public Collection loadStructuredAnnotations(Class type, long id, 
			                                    long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (id < 0)
			throw new IllegalArgumentException("Object id not valid.");
		if (type == null) 
			throw new IllegalArgumentException("No type specified.");
		List<Long> ids = null;
		if (userID != -1) {
			ids = new ArrayList<Long>(1);
			ids.add(userID);
		}
		List<Long> objects = new ArrayList<Long>(1);
		objects.add(id);
		Map map = gateway.loadAnnotations(type, objects, null, ids, 
				new Parameters());
		return (Collection) map.get(id);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadAnnotations(Class, String, long)
	 */
	public Collection loadAnnotations(Class annotationType, String nameSpace, 
			                         long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		ParametersI po = new ParametersI();
		if (userID >= 0) po.exp(omero.rtypes.rlong(userID));
		//else po.grp(i)
		List<String> toInclude = new ArrayList<String>();
		List<String> toExclude = new ArrayList<String>();
		if (nameSpace != null) 
			toInclude.add(nameSpace);
		if (FileAnnotationData.class.equals(annotationType)) {
			toExclude.add(FileAnnotationData.COMPANION_FILE_NS);
		}
		return gateway.loadSpecificAnnotation(annotationType, toInclude, 
				toExclude, po);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#saveData(Collection, List, List, long)
	 */
	public Object saveData(Collection<DataObject> data, 
			        List<AnnotationData> toAdd, List<AnnotationData> toRemove, 
			        long userID) 
			throws DSOutOfServiceException, DSAccessException
	{
		if (data == null)
			throw new IllegalArgumentException("No data to save");
		OmeroDataService service = context.getDataService();
		
		DataObject object;
		List<AnnotationData> annotations = prepareAnnotationToAdd(toAdd);
		Iterator i;
		Iterator<DataObject> j = data.iterator();
		//First create the new annotations 
		AnnotationData ann;
		while (j.hasNext()) {
			object = j.next();
			if (object instanceof AnnotationData) {
				updateAnnotationData(object);
			} else {
				service.updateDataObject(object);
			}
			if (annotations.size() > 0) {
				i = annotations.iterator();
				while (i.hasNext()) {
					ann = (AnnotationData) i.next();
					if (ann != null) linkAnnotation(object, ann);
				}
			}
			if (toRemove != null) {
				i = toRemove.iterator();
				while (i.hasNext()) {
					ann = (AnnotationData) i.next();
					if (ann != null) removeAnnotation(ann, object);
				}
			}
		}
		return data;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#saveBatchData(Collection, List, List, long)
	 */
	public Object saveBatchData(Collection<DataObject> data, 
				List<AnnotationData> toAdd, List<AnnotationData> toRemove, 
				long userID) 
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
		List<AnnotationData> annotations = prepareAnnotationToAdd(toAdd);
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
				images = gateway.getContainerImages(DatasetData.class, ids, po);
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
									linkAnnotation(child, 
											(AnnotationData) i.next());
							}
							if (toRemove != null) {
								i = toRemove.iterator();
								while (i.hasNext())
									removeAnnotation((AnnotationData) i.next(), 
														child);
							}
						}
					}
				}
			} else if (object instanceof PlateData) {
				//Load all the wells
				images = gateway.loadPlateWells(object.getId(), -1, userID);
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
									linkAnnotation(child, 
											(AnnotationData) i.next());
							}
							if (toRemove != null) {
								i = toRemove.iterator();
								while (i.hasNext())
									removeAnnotation((AnnotationData) i.next(), 
														child);
							}
						}
					}
				}
				
				
			} else if (object instanceof ImageData) {
				service.updateDataObject(object);
				if (annotations != null) {
					i = annotations.iterator();
					while (i.hasNext())
						linkAnnotation(object, (AnnotationData) i.next());
						//annotate(object, (AnnotationData) i.next());
				}
				if (toRemove != null) {
					i = toRemove.iterator();
					while (i.hasNext())
						removeAnnotation((AnnotationData) i.next(), object);
				}
			}
		}
		if (result == null) return data;
		return result;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#saveBatchData(TimeRefObject, List, List, long)
	 */
	public Object saveBatchData(TimeRefObject data, List<AnnotationData> toAdd,
			                  List<AnnotationData> toRemove, long userID) 
			throws DSOutOfServiceException, DSAccessException
	{
		if (data == null)
			throw new IllegalArgumentException("No data to save");
		OmeroDataService service = context.getDataService();
		Collection images = service.getImagesPeriod(data.getStartTime(), 
				                             data.getEndTime(), userID, true);
		List r = new ArrayList();
		if (images == null) return r;
		Iterator i = images.iterator();
		DataObject child;
		Iterator j;
		//First create the new annotations 
		List<AnnotationData> annotations = prepareAnnotationToAdd(toAdd);
		while (i.hasNext()) {
			child = (DataObject) i.next();
			r.add(child);
			if (annotations != null) {
				j = annotations.iterator();
				while (j.hasNext()) 
					linkAnnotation(child, (AnnotationData) i.next());
					//annotate(child, (AnnotationData) j.next());
			}
			if (toRemove != null) {
				j = toRemove.iterator();
				while (j.hasNext())
					removeAnnotation((AnnotationData) j.next(), child);
			}
		}
		return r;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#downloadFile(String, long, int)
	 */
	public File downloadFile(File file, long fileID, long size) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (fileID < 0)
			throw new IllegalArgumentException("File ID not valid");
		if (file == null)
			throw new IllegalArgumentException("File path not valid");
		return gateway.downloadFile(file, fileID, size);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadRatings(Class, List, long)
	 */
	public Map<Long, Collection> loadRatings(Class nodeType, 
			List<Long> nodeIds, long userID) 
			throws DSOutOfServiceException, DSAccessException
	{
		List<Long> ids = null;
		if (userID != -1) {
			ids = new ArrayList<Long>(1);
			ids.add(userID);
		}
		List<Class> types = new ArrayList<Class>();
		types.add(RatingAnnotationData.class);
		Map map = gateway.loadAnnotations(nodeType, nodeIds, types, ids, 
				new Parameters());
		Map<Long, Collection> results = new HashMap<Long, Collection>();
		if (map == null) return results;
		
		Iterator<Long> i = map.keySet().iterator();
		Long id;
		AnnotationData data;
		Iterator j;
		List<AnnotationData> result;
		Collection l;
		while (i.hasNext()) {
			id = i.next();
			l = (Collection) map.get(id);
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
	 * @see OmeroMetadataService#filterByAnnotation(Class, List, Class, List, 
	 * 												long)
	 */
	public Collection filterByAnnotation(Class nodeType, List<Long> nodeIds, 
		Class annotationType, List<String> terms, long userID) 
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
		Map map = gateway.loadAnnotations(nodeType, nodeIds, types, ids, 
				new Parameters());
		if (map == null || map.size() == 0) return results;
		ExperimenterData exp = getUserDetails();
		long id;
		Collection l;
		AnnotationData data;
		Iterator i, j;
		if (terms != null && terms.size() > 0) {
			Set annotations = gateway.filterBy(annotationType, terms,
					                           null, null, exp);
			i = map.keySet().iterator();
			while (i.hasNext()) {
				id = (Long) i.next();
				l = (Collection) map.get(id);
				j = l.iterator();
				while (j.hasNext()) {
					data = (AnnotationData) j.next();
					if (annotationType.equals(TagAnnotationData.class)) {
						if (data instanceof TagAnnotationData) {
							if (annotations.contains(data.getId())) {
								if (!results.contains(id))
									results.add(id);
							}
						}
					} else if (annotationType.equals(
							 TextualAnnotationData.class)) {
						if (!(data instanceof TagAnnotationData)) {
							if (annotations.contains(data.getId())) {
								if (!results.contains(id))
									results.add(id);
							}
						}
					}
				}
			}
		} else
			return filterByAnnotated(nodeType, nodeIds, annotationType, true, 
					    userID);
		return results;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#filterByAnnotated(Class, List, Class, boolean, 
	 * 												long)
	 */
	public Collection filterByAnnotated(Class nodeType, List<Long> nodeIds, 
		Class annotationType, boolean annotated, long userID) 
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
		Map map = gateway.loadAnnotations(nodeType, nodeIds, types, ids, 
				new Parameters());
		if (map == null || map.size() == 0) return results;
		long id;
		Collection l;
		AnnotationData data;
		Iterator i, j;
		i = map.keySet().iterator();
		if (annotated) {
			while (i.hasNext()) {
				id = (Long) i.next();
				l = (Collection) map.get(id);
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
				id = (Long) i.next();
				l = (Collection) map.get(id);
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
	 * @see OmeroMetadataService#filterByAnnotation(Class, List, FilterContext, 
	 * 												long)
	 */
	public Collection filterByAnnotation(Class nodeType, List<Long> ids, 
			FilterContext filter, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (filter == null)
			throw new IllegalArgumentException("No filtering context.");
		int rateIndex = filter.getIndex();
		List<Long> filteredNodes = new ArrayList<Long>();

		List<Long> userIDs = null;
		if (userID != -1) {
			userIDs = new ArrayList<Long>(1);
			userIDs.add(userID);
		}
		List<Class> annotationTypes = new ArrayList<Class>();
		//types.add(annotationType);
		Map map = gateway.loadAnnotations(nodeType, ids, annotationTypes, 
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
		Set annotations;
		int resultType = filter.getResultType();
		Map<Class, List<String>> types = filter.getAnnotationType();
		
		Map<Class, List<Long>> r = new HashMap<Class, List<Long>>();
		List<Long> found;
		Class type;
		Entry entry;
		if (types != null && types.size() > 0) {
			i = types.entrySet().iterator();
			
			AnnotationData data;
			if (resultType == FilterContext.INTERSECTION) {
				
				while (i.hasNext()) {
					entry = (Entry) i.next();
					type = (Class) entry.getKey();
					found = new ArrayList<Long>();
					annotations = gateway.filterBy(type, (List<String>)
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
								if (annotationsIds.contains(data.getId()) &&
										!found.contains(id)) {
									found.add(id);
								}
							}
						}
					}
					r.put(type, found);
				}
			} else if (resultType == FilterContext.UNION) {
				while (i.hasNext()) {
					type = (Class) i.next();
					annotations = gateway.filterBy(type, types.get(type), 
												start, end, exp);
					i = annotations.iterator();
					while (i.hasNext())
						annotationsIds.add((Long) i.next());
				}

				i = map.keySet().iterator();
				
				while (i.hasNext()) {
					id = (Long) i.next();
					l = (Collection) map.get(id);
					j = l.iterator();
					while (j.hasNext()) {
						data = (AnnotationData) j.next();
						if (annotationsIds.contains(data.getId()))
							filteredNodes.add(id);
					}
				}
			}
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
				case FilterContext.LOWER:
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
				case FilterContext.HIGHER:
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
		
		i = r.keySet().iterator();
		int index = 0;
		type = null;
		while (i.hasNext()) {
			type = (Class) i.next();
			if (index == 0) {
				filteredNodes.addAll(r.get(type));
				break;
			}
			index++;
		}
		r.remove(type);
		i = r.keySet().iterator();
		while (i.hasNext()) {
			type = (Class) i.next();
			filteredNodes = ListUtils.intersection(filteredNodes, r.get(type));
		}
		return filteredNodes;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadTagSetsContainer(Long, boolean, long)
	 */
	public Collection loadTagSetsContainer(Long id, boolean dataObject, 
										long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		/*
		//Collection of tags linked to tags.
		if (id >= 0)
			return gateway.loadTagSetsAndDataObjects(id);
	
		Collection c = gateway.loagTagSets(userID);
		List<Long> ids = new ArrayList<Long>();
		Iterator i = c.iterator();
		TagAnnotationData tag, child;
		Set children;
		Iterator j;
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			ids.add(tag.getId());
			children = tag.getTags();
			if (children != null) {
				j = children.iterator();
				while (j.hasNext()) {
					child = (TagAnnotationData) j.next();
					if (!ids.contains(child.getId()))
						ids.add(child.getId());
				}
			}
		}
		//load the tags not linked to a tag
		Collection allTags = loadAnnotations(TagAnnotationData.class, null, -1, 
				                             userID);
		i = allTags.iterator();
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (!ids.contains(tag.getId()))
				c.add(tag);
		}
		return c;
		*/
		return null;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#getEnumeration(String)
	 */
	public Collection getEnumeration(String type) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getEnumerations(type);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadAcquisitionData(Object)
	 */
	public Object loadAcquisitionData(Object refObject)
		throws DSOutOfServiceException, DSAccessException
	{
		if (refObject instanceof ImageData) {
			return gateway.loadImageAcquisitionData(
					((ImageData) refObject).getId());
			
		} else if (refObject instanceof ChannelData) {
			Channel c = ((ChannelData) refObject).asChannel();
			if (c.getLogicalChannel() == null) return null;
			long id = c.getLogicalChannel().getId().getValue();
			return gateway.loadChannelAcquisitionData(id);
		}
		return null;
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadInstrument(long)
	 */
	public Object loadInstrument(long instrumentID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (instrumentID <= 0) return null;
		return gateway.loadInstrument(instrumentID);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#saveAcquisitionData(Object)
	 */
	public Object saveAcquisitionData(Object refObject)
		throws DSOutOfServiceException, DSAccessException
	{
		if (refObject instanceof ImageAcquisitionData) {
			ImageAcquisitionData data = (ImageAcquisitionData) refObject;
			saveImageAcquisitionData(data);
			
			return null;//loadAcquisitionData(data.asImage());
		} else if (refObject instanceof ChannelData) {
			ChannelData data = (ChannelData) refObject;
			saveChannelData(data);
		} else if (refObject instanceof ChannelAcquisitionData) {
			ChannelAcquisitionData data = (ChannelAcquisitionData) refObject;
			saveChannelAcquisitionData(data);
		}
		return null;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#archivedFile(FileAnnotationData, File, int,
	 * 											DataObject)
	 */
	public Object archivedFile(FileAnnotationData fileAnnotation, File file, 
			int index, DataObject linkTo) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (file == null) 
			throw new IllegalArgumentException("No file to save.");
		String ns = null;
		switch (index) {
			case EDITOR_PROTOCOL:
				ns = FileAnnotationData.EDITOR_PROTOCOL_NS;
				break;
			case EDITOR_EXPERIMENT:
				ns = FileAnnotationData.EDITOR_EXPERIMENT_NS;
				break;
		}
		//Upload the file back to the server
		long id = fileAnnotation.getId();
		long originalID = fileAnnotation.getFileID();
		OriginalFile of = gateway.uploadFile(file, 
				fileAnnotation.getServerFileFormat(), originalID);
		//Need to relink and delete the previous one.
		FileAnnotation fa;
		String desc = fileAnnotation.getDescription();
		if (id < 0) {
			fa = new FileAnnotationI();
			fa.setFile(of);
			if (desc != null) fa.setDescription(omero.rtypes.rstring(desc));
			if (ns != null)
				fa.setNs(omero.rtypes.rstring(ns));
			IObject object = gateway.createObject(fa);
			id = object.getId().getValue();
		} else {
			fa = (FileAnnotation) 
				gateway.findIObject(FileAnnotation.class.getName(), id);
			fa.setFile(of);
			if (desc != null) fa.setDescription(omero.rtypes.rstring(desc));
			if (ns != null)
				fa.setNs(omero.rtypes.rstring(ns));
			gateway.updateObject(fa, new Parameters());
		}
		fa = (FileAnnotation) 
			gateway.findIObject(FileAnnotation.class.getName(), id);
		FileAnnotationData data = 
			(FileAnnotationData) PojoMapper.asDataObject(fa);
		if (of != null) 
			data.setContent(of);
		if (linkTo != null) {
			if (linkTo.getId() > 0) {
				annotate(linkTo, data);
			}
		}
		return data;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadViewedBy(long, long)
	 */
	public Collection loadViewedBy(long imageID, long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		OmeroImageService rds = context.getImageService();
		Map settings = rds.getRenderingSettings(pixelsID, -1);
		List<ViewedByDef> list = new ArrayList<ViewedByDef>();
		if (settings != null) {
			 Iterator i = settings.keySet().iterator();
			 ExperimenterData exp;
			 ViewedByDef def;
			 while (i.hasNext()) {
				 exp = (ExperimenterData) i.next();
				 def = new ViewedByDef(exp, (RndProxyDef) settings.get(exp), 
						 null);
				 def.setIds(imageID, pixelsID);
				 list.add(def);
			}
		}
		return list;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadTags(Long, boolean, boolean, long)
	 */
	public Collection loadTags(Long id, boolean dataObject, boolean topLevel,
			long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		ParametersI po = new ParametersI();
		if (userID >= 0) po.exp(omero.rtypes.rlong(userID));
		if (topLevel) {
			po.orphan();
			return gateway.loadTagSets(po);
		}
		Collection l = gateway.loadTags(id, po);
		return l;
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#countFileType(int)
	 */
	public long countFileType(int fileType) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<String> include = new ArrayList<String>();
		List<String> exclude = new ArrayList<String>();
		switch (fileType) {
			case EDITOR_PROTOCOL:
				include.add(FileAnnotationData.EDITOR_PROTOCOL_NS);
				break;
			case EDITOR_EXPERIMENT:
				include.add(FileAnnotationData.EDITOR_EXPERIMENT_NS);
				break;
			case MOVIE:
				include.add(FileAnnotationData.MOVIE_MPEG_NS);
				include.add(FileAnnotationData.MOVIE_QUICK_TIME_NS);
				include.add(FileAnnotationData.MOVIE_WINDOWS_MEDIA_NS);
				break;
			case OTHER:
			default:
				exclude.add(FileAnnotationData.EDITOR_PROTOCOL_NS);
				exclude.add(FileAnnotationData.EDITOR_EXPERIMENT_NS);
				exclude.add(FileAnnotationData.MOVIE_MPEG_NS);
				exclude.add(FileAnnotationData.MOVIE_QUICK_TIME_NS);
				exclude.add(FileAnnotationData.MOVIE_WINDOWS_MEDIA_NS);
				exclude.add(FileAnnotationData.COMPANION_FILE_NS);
				exclude.add(FileAnnotationData.MEASUREMENT_NS);
		}
		ParametersI po = new ParametersI();
		long userID = getUserDetails().getId();
		if (userID >= 0) po.exp(omero.rtypes.rlong(userID));
		return gateway.countSpecificAnnotation(FileAnnotationData.class, 
				include, exclude, po);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadFiles(int, long)
	 */
	public Collection loadFiles(int fileType, long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		List<String> include = new ArrayList<String>();
		List<String> exclude = new ArrayList<String>();
		switch (fileType) {
			case EDITOR_PROTOCOL:
				include.add(FileAnnotationData.EDITOR_PROTOCOL_NS);
				break;
			case EDITOR_EXPERIMENT:
				include.add(FileAnnotationData.EDITOR_EXPERIMENT_NS);
				break;
			case MOVIE:
				include.add(FileAnnotationData.MOVIE_MPEG_NS);
				include.add(FileAnnotationData.MOVIE_QUICK_TIME_NS);
				include.add(FileAnnotationData.MOVIE_WINDOWS_MEDIA_NS);
				break;
			case OTHER:
			default:
				exclude.add(FileAnnotationData.MOVIE_MPEG_NS);
				exclude.add(FileAnnotationData.MOVIE_QUICK_TIME_NS);
				exclude.add(FileAnnotationData.MOVIE_WINDOWS_MEDIA_NS);
				exclude.add(FileAnnotationData.EDITOR_PROTOCOL_NS);
				exclude.add(FileAnnotationData.EDITOR_EXPERIMENT_NS);
				exclude.add(FileAnnotationData.COMPANION_FILE_NS);
				exclude.add(FileAnnotationData.MEASUREMENT_NS);
		}
		ParametersI po = new ParametersI();
		if (userID >= 0) po.exp(omero.rtypes.rlong(userID));
		return gateway.loadSpecificAnnotation(FileAnnotationData.class, 
				include, exclude, po);
	}

	/** 
	 * Implemented as specified by {@link OmeroImageService}. 
	 * @see OmeroMetadataService#loadAnnotation(long)
	 */
	public DataObject loadAnnotation(long annotationID)
			throws DSOutOfServiceException, DSAccessException
	{
		//Tmp code
		List<Long> ids = new ArrayList<Long>(1);
		ids.add(annotationID);
		Set<DataObject> set = gateway.loadAnnotation(ids);
		if (set.size() != 1) return null;
		Iterator<DataObject> i = set.iterator();
		while (i.hasNext()) {
			return i.next();	
		}
		return null;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroMetadataService#loadRatings(Class, long, long)
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
	
}
