/*
 * org.openmicroscopy.shoola.env.data.OmeroDataServiceImpl
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


//Java imports
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

//Application-internal dependencies
import omero.api.delete.DeleteCommand;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLink;
import omero.model.Channel;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetImageLink;
import omero.model.Event;
import omero.model.FileAnnotation;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.Pixels;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectDatasetLink;
import omero.model.Screen;
import omero.model.ScreenPlateLink;
import omero.model.TagAnnotation;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import pojos.ChannelData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;

/** 
 * Implementation of the {@link OmeroDataService} I/F.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class OmeroDataServiceImpl
	implements OmeroDataService
{

	/** Uses it to gain access to the container's services. */
	private Registry                context;

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private OMEROGateway            gateway;
	
	/**
	 * Unlinks the collection of children from the specified parent.
	 * 
	 * @param parent    The parent of the children.
	 * @param children  The children to unlink
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMEDS service. 
	 */
	private void cut(DataObject parent, Set children)
		throws DSOutOfServiceException, DSAccessException
	{
		IObject mParent = parent.asIObject();
		Iterator i = children.iterator();
		List<Long> ids = new ArrayList<Long>(children.size());
		while (i.hasNext()) {  
			ids.add(Long.valueOf(((DataObject) i.next()).getId())); 
		}
		List links = gateway.findLinks(mParent, ids);
		if (links != null) 
			gateway.deleteObjects(links);
	}
	
	/**
	 * Deletes the tag set.
	 * 
	 * @param id The identifier of the set.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMEDS service. 
	 */
	private List<DataObject> deleteTagSet(long id)
		throws DSOutOfServiceException, DSAccessException
	{
		List l = gateway.findAnnotationLinks(Annotation.class.getName(), id,
											null);
		
		List<Long> tagIds = new ArrayList<Long>();
		List<DataObject> tags = new ArrayList<DataObject>(); 
		Iterator i = l.iterator();
		AnnotationAnnotationLink link;
		long tagID;
		while (i.hasNext()) {
			link =  (AnnotationAnnotationLink) i.next();
			tagID = link.getChild().getId().getValue();
			if (!tagIds.contains(tagID)) {
				tagIds.add(tagID);
				tags.add(PojoMapper.asDataObject(link.getChild()));
			}
		}
		//delete the links
		gateway.deleteObjects(l);
		return tags;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param gateway   Reference to the OMERO entry point.
	 *                  Mustn't be <code>null</code>.
	 * @param registry  Reference to the registry. Mustn't be <code>null</code>.
	 */
	OmeroDataServiceImpl(OMEROGateway gateway, Registry registry)
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
	 * @see OmeroDataService#loadContainerHierarchy(Class, List, boolean, long,
	 * long)
	 */
	public Set loadContainerHierarchy(Class rootNodeType, List rootNodeIDs,
			boolean withLeaves, long userID, long groupID)
		throws DSOutOfServiceException, DSAccessException 
	{
		ParametersI param = new ParametersI();
		if (rootNodeIDs == null) {
			ExperimenterData exp = 
				(ExperimenterData) context.lookup(
						LookupNames.CURRENT_USER_DETAILS);
			if (userID < 0) userID = exp.getId();
			param.exp(omero.rtypes.rlong(userID));
		}
		if (withLeaves) param.leaves();
		else param.noLeaves();
		if (rootNodeIDs == null || rootNodeIDs.size() == 0) {
			if (ProjectData.class.equals(rootNodeType) || 
					ScreenData.class.equals(rootNodeType))
				param.orphan();
		}
		Set parents = gateway.loadContainerHierarchy(rootNodeType, rootNodeIDs,
				param); 
		return parents;                            
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#loadTopContainerHierarchy(Class, long)
	 */
	public Set loadTopContainerHierarchy(Class rootNodeType, long userID)
		throws DSOutOfServiceException, DSAccessException 
	{
		ParametersI param = new ParametersI();
		param.exp(omero.rtypes.rlong(userID));
		return gateway.loadContainerHierarchy(rootNodeType, null, param);                         
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#findContainerHierarchy(Class, List, long)
	 */
	public Set findContainerHierarchy(Class rootNodeType, List leavesIDs, 
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		ParametersI po = new ParametersI();
		po.leaves();
		po.exp(omero.rtypes.rlong(userID));
		return gateway.findContainerHierarchy(rootNodeType, leavesIDs, po);
	}
	
	/** 
	 * Implemented as specified by {@link OmeroDataService}. 
	 * @see OmeroDataService#getImages(Class, List, long)
	 */
	public Set getImages(Class nodeType, List nodeIDs, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (nodeType == null)
			throw new IllegalArgumentException("No type specified.");
		ParametersI po = new ParametersI();
		po.exp(omero.rtypes.rlong(userID));
		return gateway.getContainerImages(nodeType, nodeIDs, po);
	}

	/** 
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getExperimenterImages(long)
	 */
	public Set getExperimenterImages(long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		ParametersI po = new ParametersI();
		po.exp(omero.rtypes.rlong(userID));
		return gateway.getUserImages(po);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getCollectionCount(Class, String, List)
	 */
	public Map getCollectionCount(Class rootNodeType, String property, List 
			rootNodeIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		if (!(property.equals(IMAGES_PROPERTY)))
			throw new IllegalArgumentException("Property not supported.");
		//if (rootNodeType.equals(TagAnnotationData.class))
			//return gateway.getDataObjectsTaggedCount(rootNodeIDs);
		return gateway.getCollectionCount(rootNodeType, property, rootNodeIDs, 
				new Parameters());
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#createDataObject(DataObject, DataObject, 
	 * 										Collection)
	 */
	public DataObject createDataObject(DataObject child, DataObject parent,
										Collection children)
		throws DSOutOfServiceException, DSAccessException
	{
		if (child == null) 
			throw new IllegalArgumentException("The child cannot be null.");
		//Make sure parent is current

		IObject obj = ModelMapper.createIObject(child, parent);
		if (obj == null) 
			throw new NullPointerException("Cannot convert the object.");

		IObject created = gateway.createObject(obj);
		IObject link;
		/*
		if (child instanceof TagAnnotationData) {
			//add description.
			TagAnnotationData tag = (TagAnnotationData) child;
			TextualAnnotationData desc = tag.getTagDescription();
			if (desc != null) {
				OmeroMetadataService service = context.getMetadataService(); 
				service.annotate(TagAnnotationData.class, 
						created.getId().getValue(), desc);
			}
		}
		*/
		if (parent != null) {
			link = ModelMapper.linkParentToChild(created, parent.asIObject());
			if ((child instanceof TagAnnotationData) && link != null) {
				gateway.createObject(link);
			}
		}
			
		if (children != null && children.size() > 0) {
			Iterator i = children.iterator();
			Object node;
			List<IObject> links = new ArrayList<IObject>();
			while (i.hasNext()) {
				node = i.next();
				if (node instanceof DataObject)
					links.add(ModelMapper.linkParentToChild(
							((DataObject) node).asIObject(), created));
			}
			if (links.size() > 0)
				gateway.createObjects(links);
		}
		return PojoMapper.asDataObject(created);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#updateDataObject(DataObject)
	 */
	public DataObject updateDataObject(DataObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null) 
			throw new DSAccessException("No object to update.");
		if (object instanceof ExperimenterData) 
			return updateExperimenter((ExperimenterData) object, null);
		if (!object.isLoaded()) return object;
		IObject ho = null;
		IObject oldObject = null;
		oldObject = object.asIObject();
		ho = gateway.findIObject(oldObject);

		if (ho == null) return null;
		ModelMapper.fillIObject(oldObject, ho);
		ModelMapper.unloadCollections(ho);
		IObject updated = gateway.updateObject(ho, new Parameters());
		return PojoMapper.asDataObject(updated);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#addExistingObjects(DataObject, Collection)
	 */
	public void addExistingObjects(DataObject parent, Collection children)
		throws DSOutOfServiceException, DSAccessException
	{
		if (parent instanceof ProjectData) {
			try {
				children.toArray(new DatasetData[] {});
			} catch (ArrayStoreException ase) {
				throw new IllegalArgumentException(
						"items can only be datasets.");
			}
		} else if (parent instanceof DatasetData) {
			try {
				children.toArray(new ImageData[] {});
			} catch (ArrayStoreException ase) {
				throw new IllegalArgumentException(
						"items can only be images.");
			}
		} else if (parent instanceof ScreenData) {
			try {
				children.toArray(new PlateData[] {});
			} catch (ArrayStoreException ase) {
				throw new IllegalArgumentException(
						"items can only be plate.");
			}
		} else if (parent instanceof TagAnnotationData) {
			TagAnnotationData tagSet = (TagAnnotationData) parent;
			if (!TagAnnotationData.INSIGHT_TAGSET_NS.equals(
					tagSet.getNameSpace()))
				throw new IllegalArgumentException("Parent not supported");
			Iterator i = children.iterator();
			TagAnnotationData tag;
			Object object;
			while (i.hasNext()) {
				object = i.next();
				if (!(object instanceof TagAnnotationData))
					throw new IllegalArgumentException(
					"items can only be Tag.");
				tag = (TagAnnotationData) object;
				if (tag.getNameSpace() != null)
					throw new IllegalArgumentException(
					"items can only be Tag.");
					
			}
		} else
			throw new IllegalArgumentException("Parent not supported");

		List<IObject> objects = new ArrayList<IObject>();
		IObject ioParent = parent.asIObject();
		IObject ioChild;
		Iterator child = children.iterator();
		while (child.hasNext()) {
			ioChild = ((DataObject) child.next()).asIObject();
			//First make sure that the child is not linked to the parent.
			if (gateway.findLink(ioParent, ioChild) == null)
				objects.add(ModelMapper.linkParentToChild(ioChild, ioParent));
		}
		if (objects.size() != 0)
			gateway.createObjects(objects);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#cutAndPaste(Map, Map)
	 */
	public void cutAndPaste(Map toPaste, Map toCut)
		throws DSOutOfServiceException, DSAccessException
	{
		if (toPaste == null) toPaste = new HashMap();
		if (toCut == null) toCut = new HashMap();
		Iterator i;
		Object parent;
		i = toCut.entrySet().iterator();
		Entry entry;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			parent = entry.getKey();
			if (parent instanceof DataObject) //b/c of orphaned container
				cut((DataObject) parent, (Set) entry.getValue());
		}

		i = toPaste.entrySet().iterator();

		while (i.hasNext()) {
			entry = (Entry) i.next();
			parent = entry.getKey();
			if (parent instanceof DataObject)
				addExistingObjects((DataObject) parent,
						(Set) entry.getValue());
		}
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getChannelsMetadata(long)
	 */
	public List getChannelsMetadata(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		Pixels pixels = gateway.getPixels(pixelsID);
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
	 * @see OmeroDataService#getArchivedFiles(String, long)
	 */
	public Map<Boolean, Object> getArchivedImage(String path, long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		context.getLogger().debug(this, path);
		return gateway.getArchivedFiles(path, pixelsID);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#updateExperimenter(ExperimenterData, GroupData)
	 */
	public ExperimenterData updateExperimenter(ExperimenterData exp, GroupData 
			group) 
		throws DSOutOfServiceException, DSAccessException 
	{
		//ADD control
		if (exp == null) 
			throw new DSAccessException("No object to update.");
		UserCredentials uc = (UserCredentials) 
		context.lookup(LookupNames.USER_CREDENTIALS);
		ExperimenterData user = 
			(ExperimenterData) context.lookup(
					LookupNames.CURRENT_USER_DETAILS);
		gateway.updateExperimenter(exp.asExperimenter(), user.getId());
		ExperimenterData data;
		if (group != null && exp.getDefaultGroup().getId() != group.getId()) 
			gateway.changeCurrentGroup(exp, group.getId());
		data = gateway.getUserDetails(uc.getUserName());
		
		context.bind(LookupNames.CURRENT_USER_DETAILS, data);
//		Bind user details to all agents' registry.
		List agents = (List) context.lookup(LookupNames.AGENTS);
		Iterator i = agents.iterator();
		AgentInfo agentInfo;
		while (i.hasNext()) {
			agentInfo = (AgentInfo) i.next();
			agentInfo.getRegistry().bind(
					LookupNames.CURRENT_USER_DETAILS, data);
		}
		return data;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getImagesPeriod(Timestamp, Timestamp, long, boolean)
	 */
	public Collection getImagesPeriod(Timestamp startTime, Timestamp endTime, 
								long userID, boolean asDataObject)
		throws DSOutOfServiceException, DSAccessException
	{
		if (startTime == null && endTime == null)
			throw new NullPointerException("Time not specified.");
		
		ParametersI po = new ParametersI();
		po.leaves();
		po.exp(omero.rtypes.rlong(userID));
		if (startTime != null) 
			po.startTime(omero.rtypes.rtime(startTime.getTime()));
		if (endTime != null) 
			po.endTime(omero.rtypes.rtime(endTime.getTime()));
		return gateway.getImages(po, asDataObject);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getImagesAllPeriodCount(Timestamp, Timestamp, long)
	 */
	public List getImagesAllPeriodCount(Timestamp startTime, 
			Timestamp endTime, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (startTime == null || endTime == null)
			throw new NullPointerException("Time not specified.");
		Collection imgs = getImagesPeriod(startTime, endTime, userID, false);
		Iterator i = imgs.iterator();
		Image object;
		List<Timestamp> times = new ArrayList<Timestamp>(imgs.size());
		Event evt;
		while (i.hasNext()) {
			object = (Image) i.next();
			evt = object.getDetails().getCreationEvent();
			if (evt != null)
				times.add(new Timestamp(evt.getTime().getValue()));
		}
		return times;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#advancedSearchFor(SearchDataContext)
	 */
	public Object advancedSearchFor(SearchDataContext context) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (context == null)
			throw new IllegalArgumentException("No search context defined.");
		if (!context.isValid())
			throw new IllegalArgumentException("Search context not valid.");
		Map<Integer, Object> results = new HashMap<Integer, Object>();
		
		if (!context.hasTextToSearch()) {
			results.put(SearchDataContext.TIME, gateway.searchByTime(context));
			return results;
		}
		Object result = gateway.performSearch(context); 
		//Should returns a search context for the moment.
		//collection of images only.
		Map m = (Map) result;
		
		Integer key;
		List value;
		Iterator k;
		Set<Long> imageIDs = new HashSet<Long>();
		Set images;
		DataObject img;
		List owners = context.getOwners();
		Set<Long> ownerIDs = new HashSet<Long>(owners.size());
		k = owners.iterator();
		while (k.hasNext()) {
			ownerIDs.add(((DataObject) k.next()).getId());
		}
		
		Set<DataObject> nodes;
		Object v;
		Iterator i = m.entrySet().iterator();
		Entry entry;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			key = (Integer) entry.getKey();
			v =  entry.getValue();
			if (v instanceof Integer) {
				results.put(key, v);
			} else {
				value = (List) v;
				nodes = new HashSet<DataObject>(); 
				results.put(key, nodes);
				if (value.size() > 0) {
					switch (key) {
						case SearchDataContext.NAME:
						case SearchDataContext.DESCRIPTION:
						case SearchDataContext.TAGS:
						case SearchDataContext.TEXT_ANNOTATION:
						case SearchDataContext.FILE_ANNOTATION:
						case SearchDataContext.URL_ANNOTATION:
						case SearchDataContext.CUSTOMIZED:
							images = gateway.getContainerImages(ImageData.class, 
									value, new Parameters());
							k = images.iterator();
							while (k.hasNext()) {
								img = (DataObject) k.next();
								if (!imageIDs.contains(img.getId())) {
									imageIDs.add(img.getId());
									nodes.add(img);
								}
							}
					}
				}
			}
		}
		return results; 
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#findContainerPaths(Class, long, long)
	 */
	public Collection findContainerPaths(Class type, long id, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			Class parentClass = null;
			if (DatasetData.class.equals(type))
				parentClass = Project.class;
			else if (ImageData.class.equals(type))
				parentClass = Dataset.class;
			else if (PlateData.class.equals(type))
				parentClass = Screen.class;
			else if (TagAnnotationData.class.equals(type))
				parentClass = TagAnnotation.class;
			else if (FileAnnotationData.class.equals(type))
				parentClass = FileAnnotation.class;
			if (parentClass == null) return new HashSet();
			List links = gateway.findLinks(parentClass, id, userID);
			if (links == null) return new HashSet();
			Iterator i = links.iterator();
			Set<DataObject> nodes = new HashSet<DataObject>();
			IObject object, parent = null;
			DataObject data;
			List<Long> ids = new ArrayList<Long>();
			long parentId;
			IObject link;
			while (i.hasNext()) {
				if (parentClass.equals(Project.class))
					parent = ((ProjectDatasetLink) i.next()).getParent();
				else if (parentClass.equals(Dataset.class))
					parent = ((DatasetImageLink) i.next()).getParent();
				else if (parentClass.equals(Screen.class))
					parent = ((ScreenPlateLink) i.next()).getParent();
				else if (parentClass.equals(TagAnnotation.class)) 
					parent = ((AnnotationAnnotationLink) i.next()).getParent();
				else if (parentClass.equals(FileAnnotation.class)) {
					link = (IObject) i.next();
					if (link instanceof ProjectAnnotationLink)
						parent = ((ProjectAnnotationLink) link).getParent();
					else if (link instanceof DatasetAnnotationLink)
						parent = ((DatasetAnnotationLink) link).getParent();
					else if (link instanceof ImageAnnotationLink)
						parent = ((ImageAnnotationLink) link).getParent();
				}
				parentId = parent.getId().getValue();
				if (!ids.contains(parentId)) {
					object = gateway.findIObject(parent.getClass().getName(), 
							parent.getId().getValue());
					data = PojoMapper.asDataObject(object);
					if (TagAnnotation.class.equals(parentClass)) {
						if (data instanceof TagAnnotationData) {
							TagAnnotationData tag = (TagAnnotationData) data;
							if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
									tag.getNameSpace())) {
								nodes.add(data);
							}
						}
					} else nodes.add(data);
					ids.add(parentId);
				}
			}
			return nodes;
		} catch (Exception e) {
			throw new DSAccessException(e.getMessage());
		}
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getOriginalFiles(long)
	 */
	public Collection getOriginalFiles(long pixelsID) 
		throws DSOutOfServiceException, DSAccessException
	{
		if (pixelsID < 0)
			throw new IllegalArgumentException("Pixels set ID not valid.");
		return gateway.getOriginalFiles(pixelsID);
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#loadPlateWells(long, long, long)
	 */
	public Collection loadPlateWells(long plateID, long acquisitionID,
			long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadPlateWells(plateID, acquisitionID, userID);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#delete(Collection)
	 */
	public DeleteCallback delete(Collection<DeletableObject> objects) 
		throws DSOutOfServiceException, DSAccessException, ProcessException
	{
		if (objects == null || objects.size() == 0) return null;
		Iterator<DeletableObject> i = objects.iterator();
		DeletableObject object;
		List<DeleteCommand> commands = new ArrayList<DeleteCommand>();
		DeleteCommand cmd;
		Map<String, String> options;
		DataObject data;
		List<Class> annotations;
		Iterator<Class> j;
		List<DataObject> contents;
		String ns;
		Iterator<DataObject> k;
		List<GroupData> groups = null;
		while (i.hasNext()) {
			contents = null;
			object = i.next();
			data = object.getObjectToDelete();
			annotations = object.getAnnotations();
			options = null;
			if (annotations != null && annotations.size() > 0) {
				options = new HashMap<String, String>();
				j = annotations.iterator();
				while (j.hasNext()) {
					options.put(gateway.createDeleteOption(j.next().getName()), 
							OMEROGateway.KEEP);
				}
			}
			if (!object.deleteContent()) {
				if (options == null) 
					options = new HashMap<String, String>();
				if (data instanceof DatasetData) {
					options.put(gateway.createDeleteCommand(
							ImageData.class.getName()), 
							OMEROGateway.KEEP);
				} else if (data instanceof ProjectData) {
					options.put(gateway.createDeleteCommand(
							DatasetData.class.getName()), 
							OMEROGateway.KEEP);
					options.put(gateway.createDeleteCommand(
							ImageData.class.getName()), 
							OMEROGateway.KEEP);
				} else if (data instanceof ScreenData) {
					options.put(gateway.createDeleteCommand(
							PlateData.class.getName()), 
							OMEROGateway.KEEP);
					options.put(gateway.createDeleteCommand(
							WellData.class.getName()), 
							OMEROGateway.KEEP);
					options.put(gateway.createDeleteCommand(
							PlateAcquisitionData.class.getName()), 
							OMEROGateway.KEEP);
					options.put(gateway.createDeleteCommand(
							ImageData.class.getName()), 
							OMEROGateway.KEEP);
				} else if (data instanceof TagAnnotationData) {
					options = null;
					ns = ((TagAnnotationData) data).getNameSpace();
					if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
						contents = deleteTagSet(data.getId());
					}
				}
			}
			cmd = new DeleteCommand(gateway.createDeleteCommand(
					data.getClass().getName()), data.getId(), options);
			commands.add(cmd);
			if (contents != null && contents.size() > 0) {
				k = contents.iterator();
				DataObject d;
				while (k.hasNext()) {
					d = k.next();
					cmd = new DeleteCommand(gateway.createDeleteCommand(
							d.getClass().getName()), d.getId(), options);
					commands.add(cmd);
				}
			}
		}
		return gateway.deleteObject(commands.toArray(new DeleteCommand[] {}));
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getFSRepositories()
	 */
	public FSFileSystemView getFSRepositories(long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getFSRepositories(userID);
	}
	
}
