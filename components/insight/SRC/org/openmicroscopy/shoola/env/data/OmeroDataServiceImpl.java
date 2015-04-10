/*
 * org.openmicroscopy.shoola.env.data.OmeroDataServiceImpl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
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
import java.util.Map.Entry;
import java.util.Set;

//Application-internal dependencies
import omero.cmd.Request;
import omero.cmd.graphs.ChildOption;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLink;
import omero.model.Dataset;
import omero.model.DatasetAnnotationLink;
import omero.model.DatasetImageLink;
import omero.model.Event;
import omero.model.Experimenter;
import omero.model.ExperimenterI;
import omero.model.FileAnnotation;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.Plate;
import omero.model.PlateAnnotationLink;
import omero.model.Project;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectDatasetLink;
import omero.model.Screen;
import omero.model.ScreenAnnotationLink;
import omero.model.ScreenPlateLink;
import omero.model.TagAnnotation;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.AgentInfo;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResult;
import org.openmicroscopy.shoola.env.data.util.AdvancedSearchResultCollection;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.data.util.SearchParameters;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.log.LogMessage;

import pojos.AnnotationData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;
//Third-party libraries
import omero.api.StatefulServiceInterfacePrx;

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
	private Registry context;

	/** Reference to the entry point to access the <i>OMERO</i> services. */
	private OMEROGateway gateway;

	/**
	 * Unlinks the collection of children from the specified parent.
	 *
	 * @param ctx The security context.
	 * @param parent    The parent of the children.
	 * @param children  The children to unlink
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMEDS service.
	 */
	private void cut(SecurityContext ctx, DataObject parent, Set children)
		throws DSOutOfServiceException, DSAccessException
	{
		IObject mParent = parent.asIObject();
		Iterator i = children.iterator();
		List<Long> ids = new ArrayList<Long>(children.size());
		while (i.hasNext()) {
			ids.add(Long.valueOf(((DataObject) i.next()).getId()));
		}
		List links = gateway.findLinks(ctx, mParent, ids);
		if (links != null)
			gateway.deleteObjects(ctx, links);
	}

	/**
	 * Deletes the tag set.
	 *
	 * @param ctx The security context.
	 * @param id The identifier of the set.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to
	 * retrieve data from OMEDS service.
	 */
	private List<DataObject> deleteTagSet(SecurityContext ctx, long id)
		throws DSOutOfServiceException, DSAccessException
	{
		List l = gateway.findAnnotationLinks(ctx, Annotation.class.getName(),
				id, null);

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
		gateway.deleteObjects(ctx, l);
		return tags;
	}

	/**
	 * Returns the user name or <code>null</code> if the passed user is
	 * the user currently logged in.
	 *
	 * @param user The user to handle.
	 * @return See above.
	 */
	private String getUserName(ExperimenterData user)
	{
		ExperimenterData loggedIn = context.getAdminService().getUserDetails();
		if (user != null && user.getId() != loggedIn.getId())
			return user.getUserName();
		return null;
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
	 * @see OmeroDataService#loadContainerHierarchy(SecurityContext, Class, List, boolean, long)
	 */
	public Set loadContainerHierarchy(SecurityContext ctx,
			Class rootNodeType, List rootNodeIDs, boolean withLeaves,
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		ParametersI param = new ParametersI();
		if (rootNodeIDs == null) {
			if (userID >= 0) param.exp(omero.rtypes.rlong(userID));
		}
		if (withLeaves) param.leaves();
		else param.noLeaves();
		if (rootNodeIDs == null || rootNodeIDs.size() == 0) {
			if (ProjectData.class.equals(rootNodeType) ||
					ScreenData.class.equals(rootNodeType))
				param.orphan();
		}
		return gateway.loadContainerHierarchy(ctx, rootNodeType, rootNodeIDs,
				param);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#loadTopContainerHierarchy(SecurityContext, Class, long)
	 */
	public Set loadTopContainerHierarchy(SecurityContext ctx,
			Class rootNodeType, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		ParametersI param = new ParametersI();
		param.exp(omero.rtypes.rlong(userID));
		return gateway.loadContainerHierarchy(ctx, rootNodeType, null, param);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#findContainerHierarchy(SecurityContext, Class, List, long)
	 */
	public Set findContainerHierarchy(SecurityContext ctx, Class rootNodeType,
			List leavesIDs, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		ParametersI po = new ParametersI();
		po.leaves();
		po.exp(omero.rtypes.rlong(userID));
		return gateway.findContainerHierarchy(ctx, rootNodeType, leavesIDs, po);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getImages(SecurityContext, Class, List, long)
	 */
	public Set getImages(SecurityContext ctx, Class nodeType, List nodeIDs,
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (nodeType == null)
			throw new IllegalArgumentException("No type specified.");
		ParametersI po = new ParametersI();
		if (userID >= 0) po.exp(omero.rtypes.rlong(userID));
		return gateway.getContainerImages(ctx, nodeType, nodeIDs, po);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getExperimenterImages(SecurityContext, long,
	 * boolean)
	 */
	public Set getExperimenterImages(SecurityContext ctx, long userID, boolean
			orphan)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getUserImages(ctx, userID, orphan);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getCollectionCount(SecurityContext, Class, String, List)
	 */
	public Map getCollectionCount(SecurityContext ctx, Class rootNodeType,
			String property, List rootNodeIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		if (!(property.equals(IMAGES_PROPERTY)))
			throw new IllegalArgumentException("Property not supported.");
		//if (rootNodeType.equals(TagAnnotationData.class))
			//return gateway.getDataObjectsTaggedCount(rootNodeIDs);
		return gateway.getCollectionCount(ctx, rootNodeType, property,
				rootNodeIDs, new Parameters());
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#createDataObject(SecurityContext, DataObject,
	 * 										DataObject, Collection)
	 */
	public DataObject createDataObject(SecurityContext ctx, DataObject child,
			DataObject parent, Collection children)
		throws DSOutOfServiceException, DSAccessException
	{
		if (child == null)
			throw new IllegalArgumentException("The child cannot be null.");
		//Make sure parent is current

		String userName = getUserName(ctx.getExperimenterData());
		IObject obj = ModelMapper.createIObject(child, parent);
		if (obj == null)
			throw new NullPointerException("Cannot convert the object.");

		IObject created = gateway.createObject(ctx, obj, userName);
		IObject link;
		if (parent != null) {
			link = ModelMapper.linkParentToChild(created, parent.asIObject());
			if ((child instanceof TagAnnotationData) && link != null) {
				gateway.createObject(ctx, link, userName);
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
				gateway.createObjects(ctx, links);
		}
		try {
			gateway.shutDownDerivedConnector(ctx);
		} catch (Exception e) {
			context.getLogger().info(this, "Cannot shut down the connectors.");
		}

		return PojoMapper.asDataObject(created);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#updateDataObject(SecurityContext, DataObject)
	 */
	public DataObject updateDataObject(SecurityContext ctx, DataObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		if (object == null)
			throw new DSAccessException("No object to update.");
		if (!object.isDirty()) return object;
		ctx = gateway.checkContext(ctx, object);
		if (object instanceof ExperimenterData)
			return updateExperimenter(ctx, (ExperimenterData) object, null);
		if (!object.isLoaded()) return object;
		IObject ho = null;
		IObject oldObject = null;
		oldObject = object.asIObject();
		ho = gateway.findIObject(ctx, oldObject);

		if (ho == null) return null;
		ModelMapper.fillIObject(oldObject, ho);
		ModelMapper.unloadCollections(ho);
		IObject updated = gateway.updateObject(ctx, ho, new Parameters());
		return PojoMapper.asDataObject(updated);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#addExistingObjects(SecurityContext, DataObject, Collection)
	 */
	public void addExistingObjects(SecurityContext ctx, DataObject parent,
			Collection children)
		throws DSOutOfServiceException, DSAccessException
	{
		ctx = gateway.checkContext(ctx, parent);
		if (parent instanceof ProjectData) {
			try {
				children.toArray(new DatasetData[] {});
			} catch (ArrayStoreException ase) {
				throw new IllegalArgumentException(
						"items can only be datasets.");
			}
		} else if (parent instanceof GroupData) {
			try {
				ExperimenterData[] exp =
					(ExperimenterData[])
					children.toArray(new ExperimenterData[] {});
				List<ExperimenterData> list = new ArrayList<ExperimenterData>();
				for (int i = 0; i < exp.length; i++) {
					list.add(exp[i]);
				}
				context.getAdminService().addExperimenters(ctx,
						(GroupData) parent, list);
				return;
			} catch (ArrayStoreException ase) {
				throw new IllegalArgumentException(
						"items can only be experimenters.");
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
		Iterator i = children.iterator();
		DataObject child;
		while (i.hasNext()) {
			child = (DataObject) i.next();
			if (child.getGroupId() == ctx.getGroupID()) {
				ioChild = child.asIObject();
				//First make sure that the child is not linked to the parent.
				if (gateway.findLink(ctx, ioParent, ioChild) == null)
					objects.add(
							ModelMapper.linkParentToChild(ioChild, ioParent));
			}
		}
		if (objects.size() != 0)
			gateway.createObjects(ctx, objects);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#cutAndPaste(SecurityContext, Map, Map)
	 */
	public void cutAndPaste(SecurityContext ctx, Map toPaste, Map toCut)
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
				cut(ctx, (DataObject) parent, (Set) entry.getValue());
		}

		i = toPaste.entrySet().iterator();

		while (i.hasNext()) {
			entry = (Entry) i.next();
			parent = entry.getKey();
			if (parent instanceof DataObject)
				addExistingObjects(ctx, (DataObject) parent,
						(Set) entry.getValue());
		}
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getArchivedFiles(SecurityContext, File, long)
	 */
	public Map<Boolean, Object> getArchivedImage(SecurityContext ctx,
			File file, long imageID)
		throws DSOutOfServiceException, DSAccessException
	{
		context.getLogger().debug(this, file.getAbsolutePath());
		//Check the image is archived.
		ImageData image = gateway.getImage(ctx, imageID, null);
		return gateway.getArchivedFiles(ctx, file, image);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#updateExperimenter(SecurityContext, ExperimenterData, GroupData)
	 */
	public ExperimenterData updateExperimenter(SecurityContext ctx,
			ExperimenterData exp, GroupData group)
		throws DSOutOfServiceException, DSAccessException
	{
		if (exp == null)
			throw new DSAccessException("No object to update.");
		ctx = gateway.checkContext(ctx, exp);
		UserCredentials uc = (UserCredentials)
		context.lookup(LookupNames.USER_CREDENTIALS);
		ExperimenterData user =
			(ExperimenterData) context.lookup(
					LookupNames.CURRENT_USER_DETAILS);
		gateway.updateExperimenter(ctx, exp.asExperimenter(), user.getId());
		ExperimenterData data;
		if (group != null && exp.getDefaultGroup().getId() != group.getId())
			gateway.changeCurrentGroup(ctx, exp, group.getId());
		data = gateway.getUserDetails(ctx, uc.getUserName(), true);

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
	 * @see OmeroDataService#getImagesPeriod(SecurityContext, Timestamp, Timestamp, long, boolean)
	 */
	public Collection getImagesPeriod(SecurityContext ctx, Timestamp startTime,
			Timestamp endTime, long userID, boolean asDataObject)
		throws DSOutOfServiceException, DSAccessException
	{
		if (startTime == null && endTime == null)
			throw new NullPointerException("Time not specified.");

		ParametersI po = new ParametersI();
		po.leaves();
		if (userID >= 0) po.exp(omero.rtypes.rlong(userID));
		if (startTime != null)
			po.startTime(omero.rtypes.rtime(startTime.getTime()));
		if (endTime != null)
			po.endTime(omero.rtypes.rtime(endTime.getTime()));
		return gateway.getImages(ctx, po, asDataObject);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getImagesAllPeriodCount(SecurityContext, Timestamp, Timestamp, long)
	 */
	public List getImagesAllPeriodCount(SecurityContext ctx,
			Timestamp startTime, Timestamp endTime, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (startTime == null || endTime == null)
			throw new NullPointerException("Time not specified.");
		Collection imgs = getImagesPeriod(ctx, startTime, endTime, userID,
				false);
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
	 * @see OmeroDataService#advancedSearchFor(List, SearchDataContext)
	 */
	public AdvancedSearchResultCollection search(SecurityContext ctx,
	        SearchParameters context)
		throws DSOutOfServiceException, DSAccessException
	{
		if (ctx == null)
			throw new IllegalArgumentException("No security context defined.");
		if (context == null)
			throw new IllegalArgumentException("No search context defined.");
		if (!context.isValid())
			throw new IllegalArgumentException("Search context not valid.");
		
		AdvancedSearchResultCollection results = new AdvancedSearchResultCollection();
		
		// If terms contain ids only, just add them as potential result to the results, 
		// findByIds() will remove them if they can't be found
		long[] ids = convertSearchTermsToIds(context.getQuery());
		if(ids!=null) {
		    for(long id: ids) {
		        List<Class<? extends DataObject>> types = new ArrayList<Class<? extends DataObject>>();
		        if(context.getTypes().isEmpty()) {
		            types.add(ImageData.class);
		            types.add(DatasetData.class);
		            types.add(ProjectData.class);
		            types.add(ScreenData.class);
		            types.add(PlateData.class);
		        }
		        else {
		            types = context.getTypes();
		        }
		        for(Class<? extends DataObject> type : types) {
		            AdvancedSearchResult res = new AdvancedSearchResult();
		            res.setObjectId(id);
		            res.setType(type);
		            res.setIdMatch(true);
		            results.add(res);
		        }
		    }
		}
		
		// search by ID:
		if(!results.isEmpty())
		    findByIds(ctx, results, true);
		
		// search by text:
		AdvancedSearchResultCollection searchResults = gateway.search(ctx, context);
		results.addAll(searchResults);
		if (searchResults.isError()) 
		    results.setError(searchResults.getError());

                // loads the images PixelsData (needed for thumbnail request)
                initializeImages(results);
		
		return results;
	}
    	
	/**
	 * Tries to find and load the Objects in results; removes them from results
	 * if they can't be found.
	 * @param ctx
	 * @param results
	 * @param allGroups
	 */
        private void findByIds(SecurityContext ctx, AdvancedSearchResultCollection results, boolean allGroups) throws DSOutOfServiceException{
            Iterator<AdvancedSearchResult> it = results.iterator();
            while (it.hasNext()) {
                AdvancedSearchResult r = it.next();
                IObject obj = null;
                    try {
                        String type = PojoMapper.convertTypeForSearchByQuery(r.getType());
                        String query = "select x from "+type+" x join fetch x.details.creationEvent where x.id="+r.getObjectId();
                        obj = gateway.findIObjectByQuery(ctx, query, true);
                    } catch (DSAccessException e) {
                        // Object can't be found/loaded; just skip it
                        // and remove the regarding search result
                    }
                if (obj == null)
                    it.remove();
                else {
                    r.setObject(PojoMapper.asDataObject(obj));
                }
            }
        }
        
        /**
         * Skims through the results and loads all images via the Containerservice. 
         * This is necessary to load the image's PixelsData.
         * @param results
         */
        private void initializeImages(AdvancedSearchResultCollection results) {
            Map<Long, List<AdvancedSearchResult>> byGroup = results
                    .getByGroup(ImageData.class);
    
            for (long groupId : byGroup.keySet()) {
                List<Long> ids = new ArrayList<Long>();
                for (AdvancedSearchResult r : byGroup.get(groupId)) {
                    ids.add(r.getObjectId());
                }
    
                SecurityContext ctx = new SecurityContext(groupId);
    
                try {
                    Set tmp = gateway.getContainerImages(ctx, ImageData.class, ids,
                            new Parameters());
                    
                    for(Object obj : tmp) {
                        ImageData img = (ImageData) obj;
                        for(AdvancedSearchResult r : byGroup.get(groupId)) {
                            if(r.getObjectId()==img.getId()) {
                                r.setObject(img);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    	
        /**
         * Tries to convert all search terms into ids;
         * @param terms
         * @return The ids or null if one or multiple terms contain non numeric characters
         */
        private long[] convertSearchTermsToIds(String query) {
            // split by commas and spaces
            String[] tmp = query.split("\\s|\\s*,\\s*");
            long[] result = new long[tmp.length];
            try {
                for (int i = 0; i < tmp.length; i++) {
                    result[i] = Long.parseLong(tmp[i]);
                }
            } catch (NumberFormatException e) {
                // hit a term with non numeric characters
                return null;
            }
            return result;
        }

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#findContainerPaths(SecurityContext, Class, long,
	 * long)
	 */
	public Collection findContainerPaths(SecurityContext ctx,
			Class type, long id, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			Class parentClass = gateway.convertPojos(type);
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
			else if (WellSampleData.class.equals(type) ||
					WellData.class.equals(type))
				parentClass = Plate.class;
			else if (PlateAcquisitionData.class.equals(type))
                parentClass = Plate.class;
			if (parentClass == null) return new HashSet();
			List links = gateway.findLinks(ctx, parentClass, id, userID);
			if (ImageData.class.equals(type) && (links == null ||
					links.size() == 0)) {
				return gateway.findPlateFromImage(ctx, id, userID);
			}
			if (PlateAcquisitionData.class.equals(type) && (links == null ||
                    links.size() == 0)) {
                return gateway.findPlateFromRun(ctx, id, userID);
            }
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
					else if (link instanceof PlateAnnotationLink)
						parent = ((PlateAnnotationLink) link).getParent();
					else if (link instanceof ScreenAnnotationLink)
						parent = ((ScreenAnnotationLink) link).getParent();
				}
				parentId = parent.getId().getValue();
				if (!ids.contains(parentId)) {
					object = gateway.findIObject(ctx,
							parent.getClass().getName(),
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
	 * @see OmeroDataService#getOriginalFiles(SecurityContext, long)
	 */
	public Collection getOriginalFiles(SecurityContext ctx, long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		if (pixelsID < 0)
			throw new IllegalArgumentException("Pixels set ID not valid.");
		return gateway.getOriginalFiles(ctx, pixelsID);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#loadPlateWells(SecurityContext, long, long, long)
	 */
	public Collection loadPlateWells(SecurityContext ctx,
			long plateID, long acquisitionID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.loadPlateWells(ctx, plateID, acquisitionID);
	}

    /**
     * Implemented as specified by {@link OmeroDataService}.
     * 
     * @see OmeroDataService#delete(SecurityContext, Collection)
     */
    public RequestCallback delete(SecurityContext ctx,
            Collection<DeletableObject> objects)
            throws DSOutOfServiceException, DSAccessException, ProcessException {
        if (CollectionUtils.isEmpty(objects) || ctx == null)
            return null;

        Iterator<DeletableObject> it = objects.iterator();
        DeletableObject object;
        while (it.hasNext()) {
            object = it.next();
            if (object.getGroupId() != ctx.getGroupID()) {
                it.remove();
            }
        }
        if (objects.size() == 0)
            return null;

        Map<String, List<Long>> toDelete = new HashMap<String, List<Long>>();
        Map<String, List<ChildOption>> options = new HashMap<String, List<ChildOption>>();

        for (DeletableObject delo : objects) {
            List<ChildOption> opts = new ArrayList<ChildOption>();
            DataObject data = delo.getObjectToDelete();

            if (!CollectionUtils.isEmpty(delo.getAnnotations())) {
                opts.add(Requests.option(null,
                        PojoMapper.getGraphType(AnnotationData.class)));
            }

            if (!delo.deleteContent()) {
                if (data instanceof DatasetData) {
                    opts.add(Requests.option(null,
                            PojoMapper.getGraphType(ImageData.class)));
                } else if (data instanceof ProjectData) {
                    opts.add(Requests.option(null,
                            PojoMapper.getGraphType(DatasetData.class)));
                    opts.add(Requests.option(null,
                            PojoMapper.getGraphType(ImageData.class)));
                } else if (data instanceof ScreenData) {
                    opts.add(Requests.option(null,
                            PojoMapper.getGraphType(PlateData.class)));
                    opts.add(Requests.option(null,
                            PojoMapper.getGraphType(WellData.class)));
                    opts.add(Requests.option(null,
                            PojoMapper.getGraphType(PlateAcquisitionData.class)));
                    opts.add(Requests.option(null,
                            PojoMapper.getGraphType(ImageData.class)));
                } else if (data instanceof PlateData) {
                    opts.add(Requests.option(null,
                            PojoMapper.getGraphType(PlateAcquisitionData.class)));
                    opts.add(Requests.option(null,
                            PojoMapper.getGraphType(ImageData.class)));
                } else if (data instanceof PlateAcquisitionData) {
                    opts.add(Requests.option(null,
                            PojoMapper.getGraphType(ImageData.class)));
                }

                else if (data instanceof TagAnnotationData) {
                    String ns = ((TagAnnotationData) data).getNameSpace();
                    if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
                        deleteTagSet(ctx, data.getId());
                    }
                }
            }

            String type = PojoMapper.getGraphType(data.getClass());
            List<Long> ids = toDelete.get(type);
            if (ids == null) {
                ids = new ArrayList<Long>();
                toDelete.put(type, ids);
            }
            ids.add(delo.getObjectToDelete().getId());

            options.put(type, opts);
        }

        List<Request> cmds = new ArrayList<Request>();
        for (String type : toDelete.keySet()) {
            cmds.add(Requests.delete(type, toDelete.get(type),
                    options.get(type)));
        }

        return gateway.submit(cmds, ctx);
    }

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getFSRepositories(SecurityContext, long)
	 */
	public FSFileSystemView getFSRepositories(SecurityContext ctx, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		return gateway.getFSRepositories(ctx, userID);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#transfer(SecurityContext, SecurityContext, List,
	 * List)
	 */
	public RequestCallback transfer(SecurityContext ctx,
			SecurityContext target, List<DataObject> targetNodes,
		List<DataObject> objects)
		throws DSOutOfServiceException, DSAccessException, ProcessException
	{
		if (target == null)
			throw new IllegalArgumentException("No target specified.");
		if (objects == null || objects.size() == 0)
			throw new IllegalArgumentException("No object to move.");
		Iterator<DataObject> i = objects.iterator();
		Map<DataObject, List<IObject>> map =
			new HashMap<DataObject, List<IObject>>();
		DataObject data;
		List<IObject> l;
		Iterator<DataObject> j;
		DataObject object;
		IObject link;
		String userName = getUserName(ctx.getExperimenterData());
		ExperimenterData exp = context.getAdminService().getUserDetails();
		ExperimenterData owner;
		Experimenter o = null;
		IObject newObject;
		PermissionData perms;
		List<IObject> targets = new ArrayList<IObject>();

		while (i.hasNext()) {
			data = i.next();
			owner = data.getOwner();
			perms = data.getPermissions();
			if (owner.getId() != exp.getId() &&
				perms.getPermissionsLevel() == GroupData.PERMISSIONS_PRIVATE) {
				o = new ExperimenterI(owner.getId(), false);
				break;
			}
		}

		if (!CollectionUtils.isEmpty(targetNodes)) {
			List<IObject> toCreate = new ArrayList<IObject>();
			j = targetNodes.iterator();
			while (j.hasNext()) {
				object = j.next();
				if (object != null) {
					if (object.getId() < 0) {
						newObject = object.asIObject();
						if (newObject != null && o != null) {
							newObject.getDetails().setOwner(o);
						}
						toCreate.add(newObject);
					} else targets.add(object.asIObject());
				}
			}
			if (toCreate.size() > 0) {
				toCreate = gateway.saveAndReturnObject(target, toCreate,
						new HashMap<Object, Object>(), userName);
				targets.addAll(toCreate);
			}
		}
		i = objects.iterator();
		Iterator<IObject> k;
		boolean notEmpty = !CollectionUtils.isEmpty(targets);
		while (i.hasNext()) {
			data = i.next();
			owner = data.getOwner();
			perms = data.getPermissions();
			l = new ArrayList<IObject>();
			if (notEmpty) {
				k = targets.iterator();
				while (k.hasNext()) {
					newObject = k.next();
					if (newObject != null) {
						//due to move all option when moving mif
						//the following scenario could happen
						//select a dataset to move to a Project
						//missing image from mif will be added to the queue
						//(Move all) The image cannot be linked to the target
						//i.e. project so an exception is thrown
						try {
							link = ModelMapper.linkParentToChild(
									data.asIObject(), newObject);
							if (link != null) {
								if (o != null) link.getDetails().setOwner(o);
								l.add(link);
							}	
						} catch (Exception e) {
							StringBuffer buffer = new StringBuffer();
							buffer.append("Cannot link ");
							buffer.append(data.getClass());
							buffer.append(" to ");
							buffer.append(newObject.getClass());
							buffer.append(" ");
							LogMessage msg = new LogMessage();
							msg.print(buffer.toString());
							msg.print(e);
							context.getLogger().debug(this, msg);
						}
					}
				}
			}
			map.put(data, l);
		}
		Map<String, String> options = new HashMap<String, String>();
		return gateway.transfer(ctx, target, map, options);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#loadPlateFromImage(SecurityContext, Collection)
	 */
	public Map<Long, PlateData> loadPlateFromImage(SecurityContext ctx,
		Collection<Long> ids)
	throws DSOutOfServiceException, DSAccessException
	{
		if (CollectionUtils.isEmpty(ids))
			throw new IllegalArgumentException("No images specified.");
		Map<Long, PlateData> r = new HashMap<Long, PlateData>();
		Iterator<Long> i = ids.iterator();
		Long id;
		while (i.hasNext()) {
			id = i.next();
			r.put(id, gateway.getImportedPlate(ctx, id));
		}
		return r;
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#closeService(SecurityContext,
	 * StatefulServiceInterfacePrx)
	 */
	public void closeService(SecurityContext ctx,
			StatefulServiceInterfacePrx svc)
	{
		if (ctx == null || svc == null) return;
		gateway.closeService(ctx, svc);
	}

	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#getImagesBySplitFilesets(SecurityContext, Class,
	 * List)
	 */
	public Map<Long, Map<Boolean, List<ImageData>>> getImagesBySplitFilesets(
			SecurityContext ctx, Class<?> rootType, List<Long> rootIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		if (CollectionUtils.isEmpty(rootIDs) || rootType == null)
			throw new IllegalArgumentException("No objects specified.");
		ParametersI param = new ParametersI();
		Map<Long, Map<Boolean, List<Long>>> m =
				gateway.getImagesBySplitFilesets(ctx, rootType, rootIDs, param);

		Map<Long, Map<Boolean, List<ImageData>>>
		r = new HashMap<Long, Map<Boolean, List<ImageData>>>();
		if (m == null || m.size() == 0) return r;
		List<Long> ids = new ArrayList<Long>();
		Iterator<Map<Boolean, List<Long>>> i = m.values().iterator();
		while (i.hasNext()) {
			Map<Boolean, List<Long>> map = i.next();
			Iterator<List<Long>> j = map.values().iterator();
			while (j.hasNext()) {
				ids.addAll(j.next());
			}
		}
		Set<ImageData> imgs = getImages(ctx, ImageData.class, ids, -1);
		Map<Long, ImageData> idMap = new HashMap<Long, ImageData>(imgs.size());
		Iterator<ImageData> k = imgs.iterator();
		ImageData img;
		while (k.hasNext()) {
			img = k.next();
			idMap.put(img.getId(), img);
		}
		Entry<Long, Map<Boolean, List<Long>>> e;
		Iterator<Entry<Long, Map<Boolean, List<Long>>>> ii =
				m.entrySet().iterator();
		List<Long> l;
		Entry<Boolean, List<Long>> entry;
		Iterator<Entry<Boolean, List<Long>>> j;
		while (ii.hasNext()) {
			e = ii.next();
			j = e.getValue().entrySet().iterator();
			Map<Boolean, List<ImageData>> converted =
					new HashMap<Boolean, List<ImageData>>();
			while (j.hasNext()) {
				entry = j.next();
				l = entry.getValue();
				Iterator<Long> kk = l.iterator();
				List<ImageData> convertedList = new ArrayList<ImageData>();
				while (kk.hasNext()) {
					convertedList.add(idMap.get(kk.next()));
				}
				converted.put(entry.getKey(), convertedList);
			}
			r.put(e.getKey(), converted);
		}
		return r;
	}
	
	/**
	 * Implemented as specified by {@link OmeroDataService}.
	 * @see OmeroDataService#findDatasetsByImageId(SecurityContext ctx, imgId)
	 */
	public Map<Long, List<DatasetData>> findDatasetsByImageId(SecurityContext ctx, List<Long> imgIds) throws DSOutOfServiceException, DSAccessException
		{
	    Map<Long, List<DatasetData>> result = new HashMap<Long, List<DatasetData>>();
		List queryResult = gateway.findDatasetLinks(ctx, imgIds, -1);
		for(Object tmp : queryResult) {
			if(tmp instanceof DatasetImageLink) {
			        DatasetImageLink dl = ((DatasetImageLink) tmp);
				DatasetData dsd = (DatasetData) PojoMapper.asDataObject(dl.getParent());
				long imgId = dl.getChild().getId().getValue();
				
				List<DatasetData> sets = result.get(imgId);
				if(sets == null) {
				    sets = new ArrayList<DatasetData>();
				    result.put(imgId, sets);
				}
				
				sets.add(dsd);
			}
		}
                
		return result;
	}

}
