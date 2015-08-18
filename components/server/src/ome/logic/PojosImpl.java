/*
 *   $Id$
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ome.annotations.RolesAllowed;
import ome.api.IContainer;
import ome.api.IQuery;
import ome.api.ServiceInterface;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.fs.Fileset;
import ome.model.screen.Plate;
import ome.model.screen.Screen;
import ome.model.screen.Well;
import ome.parameters.Parameters;
import ome.services.query.PojosFindHierarchiesQueryDefinition;
import ome.services.query.PojosGetImagesByOptionsQueryDefinition;
import ome.services.query.PojosGetImagesQueryDefinition;
import ome.services.query.PojosGetUserImagesQueryDefinition;
import ome.services.query.PojosLoadHierarchyQueryDefinition;
import ome.services.query.Query;
import ome.tools.HierarchyTransformations;
import ome.tools.lsid.LsidUtils;
import ome.util.CBlock;
import ome.services.query.HierarchyNavigator;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * implementation of the Pojos service interface.
 *
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date: 2007-10-03
 *          13:25:20 +0100 (Wed, 03 Oct 2007) $) </small>
 * @since OMERO 2.0
 */
@Transactional
public class PojosImpl extends AbstractLevel2Service implements IContainer {

	/**
	 * Returns the Interface implemented by this class.
	 * 
	 * @return See above.
	 */
    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IContainer.class;
    }

    // ~ READ
    // =========================================================================

    /** Query to load the number of annotations per image. */
    final static String loadCountsImages = "select img from Image img "
            + "left outer join fetch img.annotationLinksCountPerOwner iac "
            + "where img in (:list)";
    
    /** Query to load the number of annotations per dataset. */
    final static String loadCountsDatasets = "select d from Dataset d "
            + "left outer join fetch d.annotationLinksCountPerOwner "
            + "left outer join fetch d.imageLinksCountPerOwner where d " 
            + "in (:list)";
    
    /** Query to load the number of annotations per plate. */
    final static String loadCountsPlates = "select p from Plate p "
            + "left outer join fetch p.annotationLinksCountPerOwner " 
            + "where p in (:list)";

    /** Query to load the number of annotations per dataset. */
    final static String loadLinksDatasets = "select d from Dataset d "
            + "left outer join fetch d.annotationLinksCountPerOwner "
            + "left outer join fetch d.imageLinksCountPerOwner where d " 
            + "in (:list)";
    
    /* A model object hierarchy navigator that is convenient for getImagesBySplitFilesets.
     * To switch its API from bare Longs to an IObject-based query interface, it is easy to implement
     * HierarchyNavigatorWrap<Class<? extends IObject>, IObject> and implement noteLookups with its methods. */
    private static class HierarchyNavigatorPlain extends HierarchyNavigator {

        HierarchyNavigatorPlain(IQuery iQuery) {
            super(iQuery);
        }

        /**
         * @{inheritDoc}
         */
        public void prepareLookups(String toType, String fromType, Collection<Long> fromIds) {
            super.prepareLookups(toType, fromType, fromIds);
        }

        /**
         * @{inheritDoc}
         */
        public ImmutableSet<Long> doLookup(String toType, String fromType, Long fromId) {
            return super.doLookup(toType, fromType, fromId);
        }

        /**
         * Perform {@link #prepareLookups(String, String, Collection)} and {@link #doLookup(String, String, Long)}
         * for the given arguments and add the results to <code>toIdsAccumulator</code>.
         * @param fromType the query objects' type, not <code>null</code>
         * @param toType the type of the objects to which the query objects may be related, not <code>null</code>
         * @param fromIds the query objects' database IDs, none <code>null</code>
         * @param toIdsAccumulator collection into which to store the related objects' database IDs, not <code>null</code>
         */
        public void noteLookups(String fromType, String toType, Collection<Long> fromIds, Collection<Long> toIdsAccumulator) {
            super.prepareLookups(toType, fromType, fromIds);
            for (final Long fromId : fromIds) {
                toIdsAccumulator.addAll(super.doLookup(toType, fromType, fromId));
            }
        }
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#loadContainerHierarchy(Class, Set, Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIds,
            Parameters options) {

        options = new Parameters(options); // Checks for null
        
        if (null == rootNodeIds && !options.isExperimenter() && !options.isGroup()) {
            throw new ApiUsageException(
                    "Set of ids for loadContainerHierarchy() may not be null "
                            + "if experimenter and group options are null.");
        }

        if (!Project.class.equals(rootNodeType)
                && !Dataset.class.equals(rootNodeType)
                && !Screen.class.equals(rootNodeType) &&
                !Plate.class.equals(rootNodeType)) {
            throw new ApiUsageException(
                    "Class parameter for loadContainerIHierarchy() must be in "
                            + "{Project,Dataset, Screen, Plate}, not "
                            + rootNodeType);
        }

        Query<List<IObject>> q = getQueryFactory().lookup(
                PojosLoadHierarchyQueryDefinition.class.getName(),
                options.addClass(rootNodeType).addIds(rootNodeIds));
        List<IObject> l = iQuery.execute(q);

        Dataset d;
        Plate plate;
        // WORKAROUND ticket:882
        if (Project.class.equals(rootNodeType)) {

            Set<Dataset> datasets = new HashSet<Dataset>();
            Project p;
            for (IObject o : l) {
                p = (Project) o;
                datasets.addAll(p.linkedDatasetList());
            }
            if (options.isOrphan()) {
                if (CollectionUtils.isEmpty(rootNodeIds)) {
                    Iterator<Dataset> i = datasets.iterator();
                    Set<Long> linked = new HashSet<Long>();
                    while (i.hasNext()) {
                        linked.add(i.next().getId());
                    }
                    q = getQueryFactory().lookup(
                            PojosLoadHierarchyQueryDefinition.class.getName(),
                            options.addClass(Dataset.class).addIds(rootNodeIds));
                    List<IObject> list = iQuery.execute(q);
                    Iterator<IObject> j = list.iterator();
                    Long id;

                    Map<Long, Dataset> notLinked = new HashMap<Long, Dataset>();
                    while (j.hasNext()) {
                        d = (Dataset) j.next();
                        id = d.getId();
                        if (!linked.contains(id)) {
                            notLinked.put(id, d);// not linked to user's project
                        }
                    }
                    StringBuffer sb = new StringBuffer();
                    sb.append("select this from Project this ");
                    sb.append("left outer join fetch this.datasetLinks pdl ");
                    sb.append("left outer join fetch pdl.child ds ");
                    sb.append("where ds in (:list)");
                    if (notLinked.size() > 0) {
                        List<Dataset> nl = new ArrayList<Dataset>();
                        nl.addAll(notLinked.values());
                        List<IObject> projects =
                                iQuery.findAllByQuery(sb.toString(),
                                new Parameters().addList("list", nl));
                        if (projects.isEmpty()) {
                            datasets.addAll(nl);
                            l.addAll(nl);
                        } else { //some datasets are in the projects
                            for (IObject o : projects) {
                                p = (Project) o;
                                List<Dataset> ll = p.linkedDatasetList();
                                for (Dataset data : ll) {
                                    if (notLinked.containsKey(data.getId())) {
                                        notLinked.remove(data.getId());
                                    }
                                }
                            }
                            if (notLinked.size() > 0) {
                                nl = new ArrayList<Dataset>();
                                nl.addAll(notLinked.values());
                                datasets.addAll(nl);
                                l.addAll(nl); 
                            }
                        }
                    }
                }
            }
            if (datasets.size() > 0) {
                iQuery.findAllByQuery(loadCountsDatasets, new Parameters()
                        .addSet("list", datasets));
            }
        } else if (Dataset.class.isAssignableFrom(rootNodeType)) {
            Set<Image> images = new HashSet<Image>();
            for (IObject o : l) {
                d = (Dataset) o;
                images.addAll(d.linkedImageList());
            }
            if (images.size() > 0) {
                iQuery.findAllByQuery(loadCountsImages, new Parameters()
                        .addSet("list", images));
            }
            // WORKAROUND ticket:907
            // Destructive changes in this block
            if (!options.isLeaves()) {
                EvictBlock<Dataset> evict = new EvictBlock<Dataset>();
                for (IObject o : l) {
                    d = (Dataset) o;
                    evict.call(d);
                    d.putAt(Dataset.IMAGELINKS, null);
                }
            }
        } else if (Screen.class.isAssignableFrom(rootNodeType)) {
        	Set<Plate> plates = new HashSet<Plate>();
            Screen p;
            for (IObject o : l) {
                p = (Screen) o;
                plates.addAll(p.linkedPlateList());
            }
            if (options.isOrphan()) {
                if (rootNodeIds == null || rootNodeIds.size() == 0) {
                    Iterator<Plate> i = plates.iterator();
                    Set<Long> linked = new HashSet<Long>();
                    while (i.hasNext()) {
                        linked.add(i.next().getId());
                    }
                    q = getQueryFactory().lookup(
                            PojosLoadHierarchyQueryDefinition.class.getName(),
                            options.addClass(Plate.class).addIds(rootNodeIds));
                    List<IObject> list = iQuery.execute(q);
                    Iterator<IObject> j = list.iterator();
                    Long id;
                    Plate pp;
                    Map<Long, Plate> notLinked = new HashMap<Long, Plate>();
                    while (j.hasNext()) {
                        pp = (Plate) j.next();
                        id = pp.getId();
                        if (!linked.contains(id)) {
                            notLinked.put(id, pp);// not linked to user's screen
                        }
                    }
                    StringBuffer sb = new StringBuffer();
                    sb.append("select this from Screen this ");
                    sb.append("left outer join fetch this.plateLinks pdl ");
                    sb.append("left outer join fetch pdl.child ds ");
                    sb.append("where ds in (:list)");
                    if (notLinked.size() > 0) {
                        List<Plate> nl = new ArrayList<Plate>();
                        nl.addAll(notLinked.values());
                        List<IObject> screens =
                                iQuery.findAllByQuery(sb.toString(),
                                new Parameters().addList("list", nl));
                        if (screens.isEmpty()) {
                            plates.addAll(nl);
                            l.addAll(nl);
                        } else { //some datasets are in the projects
                            for (IObject o : screens) {
                                p = (Screen) o;
                                List<Plate> ll = p.linkedPlateList();
                                for (Plate data : ll) {
                                    if (notLinked.containsKey(data.getId())) {
                                        notLinked.remove(data.getId());
                                    }
                                }
                            }
                            if (notLinked.size() > 0) {
                                nl = new ArrayList<Plate>();
                                nl.addAll(notLinked.values());
                                plates.addAll(nl);
                                l.addAll(nl); 
                            }
                        }
                    }
                }
            }
            if (plates.size() > 0) {
                iQuery.findAllByQuery(loadCountsPlates, new Parameters()
                        .addSet("list", plates));
            }
        } 
        return new HashSet<IObject>(l);
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#findContainerHierarchies(Class, Set, Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Set findContainerHierarchies(final Class rootNodeType,
            final Set imageIds, Parameters options) {

        options = new Parameters(options); // Checks for null

        // TODO refactor to use Hierarchy class H.isTopLevel()
        if (!(Project.class.equals(rootNodeType))) {
            throw new ApiUsageException(
                    "Class parameter for findContainerHierarchies() must be"
                            + " in {Project}, not "
                            + rootNodeType);
        }

        Query<List<Image>> q = getQueryFactory().lookup(
                PojosFindHierarchiesQueryDefinition.class.getName(),
                options.addClass(rootNodeType).addIds(imageIds));
        List<Image> l = iQuery.execute(q);

        //
        // Destructive changes below this point.
        //

        // TODO; this if-else statement could be removed if Transformations
        // did their own dispatching
        // TODO: logging, null checking. daos should never return null
        // TODO then size!
        if (Project.class.equals(rootNodeType)) {
            if (imageIds.size() == 0) {
                return new HashSet();
            }

            return HierarchyTransformations.invertPDI(new HashSet<Image>(l),
                    new EvictBlock<IObject>());

        }

        else {
            throw new InternalException("This can't be reached.");
        }

    }

   
    static final Map<Class, String> paginationQueries = new HashMap();
    static {
        paginationQueries.put(Dataset.class,
                "select link.child.id from DatasetImageLink "
                        + " link where link.parent.id in (:ids)"
                        + "order by link.child.id");
        paginationQueries
                .put(
                        Project.class,
                        "select distinct dil.child.id from ProjectDatasetLink pdl "
                                + "join pdl.child ds join ds.imageLinks as dil "
                                + "where pdl.parent.id in (:ids) order by dil.child.id");
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#getImages(Class, Set, Map)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Set getImages(final Class rootNodeType, final Set rootNodeIds,
            Parameters options) {

        if (rootNodeIds.size() == 0) {
            return new HashSet();
        }

        options = new Parameters(options); // Checks for null
        final Parameters view = options;

        // Effective values
        Class effType = rootNodeType;
        Set<Long> effIds = rootNodeIds;

        if (options.isPagination()) {
            final String query = paginationQueries.get(rootNodeType);
            if (query == null) {
                throw new ApiUsageException(rootNodeType.getName()
                        + " does not support pagination yet.");
            }
            effType = Image.class;
            effIds = new HashSet<Long>((List<Long>) iQuery
                    .execute(new HibernateCallback() {
                        public Object doInHibernate(Session s)
                                throws HibernateException, SQLException {
                            org.hibernate.Query q = s.createQuery(query);
                            q.setParameterList("ids", rootNodeIds);
                            // ticket:1232
                            if (view.getLimit() != null) {
                                q.setMaxResults(view.getLimit());
                            } else {
                                q.setMaxResults(Integer.MAX_VALUE);
                            }
                            if (view.getOffset() != null) {
                                q.setFirstResult(view.getOffset());
                            } else {
                                q.setFirstResult(0);
                            }
                            return q.list();
                        }
                    }));
            if (effIds == null || effIds.size() == 0) {
                return new HashSet();
            }
            /* paging has now been done */
            options = options.page(0, Integer.MAX_VALUE);
        }

        Query<List<IObject>> q = getQueryFactory().lookup(
                PojosGetImagesQueryDefinition.class.getName(),
                options.addIds(effIds).addClass(effType));

        List<IObject> l = iQuery.execute(q);
        return new HashSet<IObject>(l);
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#getImagesByOptions(Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Set getImagesByOptions(Parameters options) {

        options = new Parameters(options); // Checks for null

        if (options.getStartTime() == null && options.getEndTime() == null) {
            throw new ApiUsageException("start or end time option "
                    + "is required for getImagesByOptions().");
        }

        Query<List<IObject>> q = getQueryFactory().lookup(
                PojosGetImagesByOptionsQueryDefinition.class.getName(),
                options);

        List<IObject> l = iQuery.execute(q);
        return new HashSet<IObject>(l);

    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#getImagesBySplitFilesets(Map)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Map<Long, Map<Boolean, List<Long>>> getImagesBySplitFilesets(Map<Class<? extends IObject>, List<Long>> included,
            Parameters options) {

        /* note which entities have been explicitly referenced */

        final Set<Long> projectIds = new HashSet<Long>();
        final Set<Long> datasetIds = new HashSet<Long>();
        final Set<Long> screenIds  = new HashSet<Long>();
        final Set<Long> plateIds   = new HashSet<Long>();
        final Set<Long> wellIds    = new HashSet<Long>();
        final Set<Long> filesetIds = new HashSet<Long>();
        final Set<Long> imageIds   = new HashSet<Long>();

        for (final Entry<Class<? extends IObject>, List<Long>> typeAndIds : included.entrySet()) {
            final Class<? extends IObject> type = typeAndIds.getKey();
            final List<Long> ids = typeAndIds.getValue();
            if (Project.class.isAssignableFrom(type)) {
                projectIds.addAll(ids);
            } else if (Dataset.class.isAssignableFrom(type)) {
                datasetIds.addAll(ids);
            } else if (Screen.class.isAssignableFrom(type)) {
                screenIds.addAll(ids);
            } else if (Plate.class.isAssignableFrom(type)) {
                plateIds.addAll(ids);
            } else if (Well.class.isAssignableFrom(type)) {
                wellIds.addAll(ids);
            } else if (Image.class.isAssignableFrom(type)) {
                imageIds.addAll(ids);
            } else if (Fileset.class.isAssignableFrom(type)) {
                filesetIds.addAll(ids);
            }
        }

        /* also note which entities have been implicitly referenced */

        final HierarchyNavigatorPlain hierarchyNavigator = new HierarchyNavigatorPlain(iQuery);

        hierarchyNavigator.noteLookups("/Project", "/Dataset", projectIds, datasetIds);
        hierarchyNavigator.noteLookups("/Dataset", "/Image", datasetIds, imageIds);
        hierarchyNavigator.noteLookups("/Screen", "/Plate", screenIds, plateIds);
        hierarchyNavigator.noteLookups("/Plate", "/Well", plateIds, wellIds);
        hierarchyNavigator.noteLookups("/Well", "/Image", wellIds, imageIds);
        hierarchyNavigator.noteLookups("/Fileset", "/Image", filesetIds, imageIds);

        /* note which filesets are associated with referenced images */

        final Set<Long> filesetIdsRequired = new HashSet<Long>();
        hierarchyNavigator.noteLookups("/Image", "/Fileset", imageIds, filesetIdsRequired);

        /* make sure that associated filesets have all their images referenced */

        final Map<Long, Map<Boolean, List<Long>>> imagesBySplitFilesets = new HashMap<Long, Map<Boolean, List<Long>>>();
        final Set<Long> filesetIdsMissing = Sets.difference(filesetIdsRequired, filesetIds);
        hierarchyNavigator.prepareLookups("/Image", "/Fileset", filesetIdsMissing);
        for (final long filesetIdMissing : filesetIdsMissing) {
            final Set<Long> imageIdsRequiredUnordered = hierarchyNavigator.doLookup("/Image", "/Fileset", filesetIdMissing);
            final SortedSet<Long> imageIdsRequired = new TreeSet<Long>(imageIdsRequiredUnordered);
            final Set<Long> includedImageIds = Sets.intersection(imageIdsRequired, imageIds);
            final Set<Long> excludedImageIds = Sets.difference(imageIdsRequired, includedImageIds);
            if (!excludedImageIds.isEmpty()) {
                final Map<Boolean, List<Long>> partitionedImages = new HashMap<Boolean, List<Long>>(2);
                partitionedImages.put(true,  new ArrayList<Long>(includedImageIds));
                partitionedImages.put(false, new ArrayList<Long>(excludedImageIds));
                imagesBySplitFilesets.put(filesetIdMissing, partitionedImages);
            }
        }
        return imagesBySplitFilesets;
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#getUserImages(Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Set getUserImages(Parameters options) {

        options = new Parameters(options); // Checks for null
        
        if (!options.isExperimenter() && !options.isGroup()) {
            throw new ApiUsageException("experimenter or group option "
                    + "is required for getUserImages().");
        }

        Query<List<Image>> q = getQueryFactory().lookup(
                PojosGetUserImagesQueryDefinition.class.getName(), options);

        List<Image> l = iQuery.execute(q);
        return new HashSet<Image>(l);

    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#getCollectionCount(String, String, Set, Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Map getCollectionCount(String type, String property, Set ids,
            Parameters options) {

        String parsedProperty = LsidUtils.parseField(property);

        checkType(type);
        checkProperty(type, parsedProperty);

        Map<Long, Integer> results = new HashMap<Long, Integer>();

        String query = "select size(table." + parsedProperty + ") from " + type
                + " table where table.id = :id";
        // FIXME: optimize by doing new list(id,size(table.property)) ... group
        // by id
        for (Iterator iter = ids.iterator(); iter.hasNext();) {
            Long id = (Long) iter.next();
            Query<List<Integer>> q = getQueryFactory().lookup(query,
                    new Parameters().addId(id));
            Integer count = iQuery.execute(q).get(0);
            results.put(id, count);
        }

        return results;
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#retrieveCollection(IObject, String, Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = true)
    public Collection retrieveCollection(IObject arg0, String arg1, Parameters arg2) {
        IObject context = iQuery.get(arg0.getClass(), arg0.getId());
        Collection c = (Collection) context.retrieve(arg1); // FIXME not
        // type.o.null safe
        iQuery.initialize(c);
        return c;
    }

    // ~ WRITE
    // =========================================================================

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#createDataObject(IObject, Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public IObject createDataObject(IObject arg0, Parameters arg1) {
        return iUpdate.saveAndReturnObject(arg0);
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#createDataObject(IObject[], Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public IObject[] createDataObjects(IObject[] arg0, Parameters arg1) {
        return iUpdate.saveAndReturnArray(arg0);
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#unlink(ILink[], Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void unlink(ILink[] arg0, Parameters arg1) {
        deleteDataObjects(arg0, arg1);
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#link(ILink[], Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public ILink[] link(ILink[] arg0, Parameters arg1) {
        IObject[] retVal = iUpdate.saveAndReturnArray(arg0);
        // IUpdate returns an IObject array here. Can't be cast using (Link[])
        ILink[] links = new ILink[retVal.length];
        System.arraycopy(retVal, 0, links, 0, retVal.length);
        return links;
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#updateDataObject(IObject, Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public IObject updateDataObject(IObject arg0, Parameters arg1) {
        return iUpdate.saveAndReturnObject(arg0);
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#updateDataObject(IObject[], Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public IObject[] updateDataObjects(IObject[] arg0, Parameters arg1) {
        return iUpdate.saveAndReturnArray(arg0);
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#deleteDataObject(IObject, Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void deleteDataObject(IObject row, Parameters arg1) {
        iUpdate.deleteObject(row);
    }

    /**
     * Implemented as specified by the {@link IContainer} I/F
     * @see IContainer#deleteDataObjects(IObject[], Parameters)
     */
    @RolesAllowed("user")
    @Transactional(readOnly = false)
    public void deleteDataObjects(IObject[] rows, Parameters options) {
        for (IObject object : rows) {
            deleteDataObject(object, options);
        }
    }

    // ~ Helpers
    // =========================================================================

    final static String alphaNumeric = "^\\w+$";

    final static String alphaNumericDotted = "^\\w[.\\w]+$"; // TODO

    // annotations

    protected void checkType(String type) {
        if (!type.matches(alphaNumericDotted)) {
            throw new ApiUsageException(
                    "Type argument to getCollectionCount may ONLY be "
                            + "alpha-numeric with dots (" + alphaNumericDotted
                            + ")");
        }

        if (!iQuery.checkType(type)) {
            throw new ApiUsageException(type + " is an unknown type.");
        }
    }

    protected void checkProperty(String type, String property) {

        if (!property.matches(alphaNumeric)) {
            throw new ApiUsageException("Property argument to "
                    + "getCollectionCount may ONLY be alpha-numeric ("
                    + alphaNumeric + ")");
        }

        if (!iQuery.checkProperty(type, property)) {
            throw new ApiUsageException(type + "." + property
                    + " is an unknown property on type " + type);
        }

    }

    @SuppressWarnings("unchecked")
    class EvictBlock<E extends IObject> implements CBlock {
        public E call(IObject object) {
            iQuery.evict(object);
            return (E) object;
        };
    }

}
