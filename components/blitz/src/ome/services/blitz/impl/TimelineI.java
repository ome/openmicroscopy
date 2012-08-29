/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import static omero.rtypes.rint;
import static omero.rtypes.rlong;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IShare;
import ome.conditions.InternalException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.SessionAnnotationLink;
import ome.model.core.Image;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.security.AdminAction;
import ome.security.SecuritySystem;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.sessions.SessionManager;
import ome.services.throttling.Adapter;
import ome.services.util.Executor.SimpleWork;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.tools.hibernate.QueryBuilder;
import omero.ApiUsageException;
import omero.RTime;
import omero.ServerError;
import omero.api.AMD_ITimeline_countByPeriod;
import omero.api.AMD_ITimeline_getByPeriod;
import omero.api.AMD_ITimeline_getEventLogsByPeriod;
import omero.api.AMD_ITimeline_getMostRecentAnnotationLinks;
import omero.api.AMD_ITimeline_getMostRecentObjects;
import omero.api.AMD_ITimeline_getMostRecentShareCommentLinks;
import omero.api._ITimelineOperations;
import omero.sys.Filter;
import omero.sys.Parameters;
import omero.util.IceMapper;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

/**
 * implementation of the ITimeline service interface.
 * 
 * @since Beta4
 */
public class TimelineI extends AbstractAmdServant implements
        _ITimelineOperations, ServiceFactoryAware, BlitzOnly {

    protected ServiceFactoryI factory;

    protected SessionManager sm;

    protected SecuritySystem ss;

    public TimelineI(BlitzExecutor be) {
        super(null, be);
    }

    public void setSessionManager(SessionManager sm) {
        this.sm = sm;
    }

    public void setSecuritySystem(SecuritySystem ss) {
        this.ss = ss;
    }

    public void setServiceFactory(ServiceFactoryI sf) {
        this.factory = sf;
    }

    // ~ Service methods
    // =========================================================================

    static final String LOOKUP_SHARE_COMMENTS = "select l from SessionAnnotationLink l "
            + "join fetch l.details.owner "
            + "join fetch l.parent as share "
            + "join fetch l.child as comment "
            + "join fetch comment.details.owner "
            + "join fetch comment.details.creationEvent "
            + "where comment.details.owner.id !=:id "
            + "and share.id in (:ids) "
            + "order by comment.details.creationEvent.time desc";

    // TODO fix mutability
    static final List<String> ALLTYPES = Arrays.asList("RenderingDef", "Image",
            "Project", "Dataset", "Annotation");
    static final Map<String, String> ORDERBY = new HashMap<String, String>();
    static final Map<String, String> BYPERIOD = new HashMap<String, String>();
    static final Map<String, String> OWNERSHIP = new HashMap<String, String>();
    static {

        String WHERE_OBJ_DETAILS = "where"
                + "    (obj.details.creationEvent.time >= :start "
                + "  or obj.details.updateEvent.time  >= :start) "
                + "and (obj.details.creationEvent.time <= :end"
                + " or obj.details.updateEvent.time <= :end ) ";

        BYPERIOD.put("Project", "from Project obj "
                + "join @FETCH@ obj.details.creationEvent "
                + "join @FETCH@ obj.details.owner "
                + "join @FETCH@ obj.details.group " + WHERE_OBJ_DETAILS);
        OWNERSHIP.put("Project", "obj");
        ORDERBY.put("Project", "order by obj.details.updateEvent.id desc");

        BYPERIOD.put("Dataset", "from Dataset obj "
                + "join @FETCH@ obj.details.creationEvent "
                + "join @FETCH@ obj.details.owner "
                + "join @FETCH@ obj.details.group "
                //+ "left outer join @FETCH@ obj.projectLinks pdl "
                //+ "left outer join @FETCH@ pdl.parent p "
                + WHERE_OBJ_DETAILS);
        OWNERSHIP.put("Dataset", "obj");
        ORDERBY.put("Dataset", "order by obj.details.updateEvent.id desc");

        BYPERIOD.put("RenderingDef", "from RenderingDef obj join @FETCH@ "
                + "obj.details.creationEvent join @FETCH@ obj.details.owner "
                + "join @FETCH@ obj.details.group left outer join @FETCH@ "
                + "obj.pixels p left outer join @FETCH@ p.image i "
                + WHERE_OBJ_DETAILS);
        OWNERSHIP.put("RenderingDef", "i");
        ORDERBY.put("RenderingDef",
                "order by i.details.creationEvent.time desc");

        BYPERIOD.put("Image", "from Image obj "
                + "join @FETCH@ obj.details.creationEvent "
                + "join @FETCH@ obj.details.owner "
                + "join @FETCH@ obj.details.group "
                //+ "left outer join @FETCH@ obj.datasetLinks dil "
                //+ "left outer join @FETCH@ dil.parent d "
                //+ "left outer join @FETCH@ d.projectLinks pdl "
                //+ "left outer join @FETCH@ pdl.parent p "
                + "where "
                + "      obj.acquisitionDate >= :start "
                + "and   obj.acquisitionDate <= :end ");
        OWNERSHIP.put("Image", "obj");
        ORDERBY.put("Image", "order by obj.acquisitionDate desc");

        BYPERIOD.put("EventLog", "from EventLog obj "
                + "left outer join @FETCH@ obj.event ev where "
                + "    obj.entityType in ("
                + "        'ome.model.containers.Dataset', "
                + "        'ome.model.containers.Project') "
                + "    and obj.action in ( 'INSERT', 'UPDATE', 'REINDEX') "
                + "    and ev.id in (     "
                + "        select e.id from Event e where "
                + "        e.time >= :start and e.time <= :end ");
        // NOTE This query requires special handling in do_periodQuery
        // to properly handle the ownership via Event and closing the
        // subquery.
    }

    public void countByPeriod_async(final AMD_ITimeline_countByPeriod __cb,
            final List<String> types, final RTime start, final RTime end,
            final Parameters p, final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.PRIMITIVE_MAP);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "countByPeriod") {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {

                Parameters pWithParameters = applyDefaults(p);
                return do_periodQuery(true, types, start, end, Long
                        .valueOf(-1L), session, pWithParameters);

            }
        }));
    }

    public void getByPeriod_async(final AMD_ITimeline_getByPeriod __cb,
            final List<String> types, final RTime start, final RTime end,
            final omero.sys.Parameters p, final boolean merge, Current __current)
            throws ServerError {

        final IceMapper mapper = new IceMapper(
                IceMapper.PRIMITIVE_FILTERABLE_COLLECTION_MAP);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getByPeriod") {

            @SuppressWarnings("unchecked")
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {

                Parameters pWithDefaults = applyDefaults(p);
                Map<String, List<IObject>> returnValue = (Map<String, List<IObject>>) do_periodQuery(
                        false, types, start, end, null, session, pWithDefaults);

                if (merge) {
                    returnValue = mergeMap(returnValue, pWithDefaults, false);
                }

                return returnValue;
            }

        }));
    }

    public void getEventLogsByPeriod_async(
            final AMD_ITimeline_getEventLogsByPeriod __cb, final RTime start,
            final RTime end, final omero.sys.Parameters p,
            final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.FILTERABLE_COLLECTION);

        
        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getEventLogsByPeriod") {

            @SuppressWarnings("unchecked")
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {

            	
                Parameters pWithDefaults = applyDefaults(p);

                Map<String, List<EventLog>> events = (Map<String, List<EventLog>>) do_periodQuery(
                        false, Arrays.asList("EventLog"), start, end, null,
                        session, pWithDefaults);
                List<EventLog> logs = events.get("EventLog");

                // WORKAROUND - currently there are no events for
                // Image.acquisitionDate meaning we have to generate them
                // here.
                QueryBuilder qb = new QueryBuilder(256);
                qb.select("i");
                qb.from("Image", "i");
                qb.join("i.details.owner", "owner", true, true);
                qb.join("i.details.group", "group", true, true);
                qb.where();
                qb.and("i.acquisitionDate > :start ");
                qb.param("start", new Timestamp(start.getValue()));
                qb.and("i.acquisitionDate < :end ");
                qb.param("end", new Timestamp(end.getValue()));

                // OWNER/GROUP
                applyOwnerGroup(pWithDefaults, qb, "owner.id", "group.id");

                Query q = qb.query(session);
                applyParameters(pWithDefaults, q);
                List<Image> images = (List<Image>) q.list();
                for (Image image : images) {
                    EventLog el = new EventLog();
                    el.setEntityId(image.getId());
                    el.setEntityType(image.getClass().getName());
                    el.setAction("INSERT");
                    el.setEvent(new Event());
                    el.getEvent().setTime(image.getAcquisitionDate());
                    logs.add(el);
                }
                return logs;
            }

        }));
    }

    public void getMostRecentObjects_async(
            final AMD_ITimeline_getMostRecentObjects __cb,
            final List<String> types, final omero.sys.Parameters p,
            final boolean merge, Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(
                IceMapper.PRIMITIVE_FILTERABLE_COLLECTION_MAP);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getMostRecentObjects") {

            @SuppressWarnings("unchecked")
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {

                Parameters pWithDefaults = applyDefaults(p);
                Map<String, List<IObject>> returnValue = (Map<String, List<IObject>>) do_periodQuery(
                        false, types, null, null, null, session, pWithDefaults);

                if (merge) {
                    returnValue = mergeMap(returnValue, pWithDefaults, false);
                }

                return returnValue;
            }

        }));
    }

    public void getMostRecentAnnotationLinks_async(
            final AMD_ITimeline_getMostRecentAnnotationLinks __cb,
            final List<String> parentTypes, final List<String> childTypes,
            final List<String> namespaces, final Parameters p,
            final Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.FILTERABLE_COLLECTION);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getMostRecentAnnotationLinks") {

            @SuppressWarnings("unchecked")
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {

                Parameters pWithDefaults = applyDefaults(p);
                Map<String, List<IObject>> rv = new HashMap<String, List<IObject>>();
                List<String> _parentTypes;

                if (parentTypes == null || parentTypes.size() == 0) {
                    _parentTypes = Arrays.asList("Project", "Dataset", "Image");
                } else {
                    _parentTypes = parentTypes;
                }

                for (String _parentType : _parentTypes) {
                    QueryBuilder qb = new QueryBuilder();
                    qb.select("link");
                    qb.from(_parentType + "AnnotationLink", "link");
                    qb.join("link.parent", "parent", false, false);
                    qb.join("link.child", "child", false, true);
                    qb.join("link.details.creationEvent", "creation", false,
                            true);
                    qb.join("link.details.updateEvent", "update", false, true);
                    qb.where();
                    qb.and("TRUE = TRUE ");
                    if (childTypes != null && childTypes.size() > 0) {

                        // WORKAROUND
                        if (childTypes.size() > 1) {
                            throw new ome.conditions.ApiUsageException(
                                    "HHH-879: "
                                            + "You can only restrict to a "
                                            + "single annotation type at the moment");
                        }

                        for (String _childType : childTypes) {
                            try {
                                Class kls = mapper.omeroClass(_childType, true);
                                qb.and("child.class = " + kls.getName());
                            } catch (Exception e) {
                                throw new ValidationException("Error mapping: "
                                        + _childType);
                            }
                        }

                    }

                    // NAMESPACES
                    if (namespaces != null && namespaces.size() > 0) {
                        qb.and(" ( ");
                        String param = qb.unique_alias("ns");
                        qb.append("child.ns like :" + param);
                        qb.param(param, namespaces.get(0));
                        for (int i = 1; i < namespaces.size(); i++) {
                            qb.append(" OR ");
                            String param2 = qb.unique_alias("ns");
                            qb.append("child.ns like :" + param2);
                            qb.param(param2, namespaces.get(i));
                        }
                        qb.append(" ) ");
                    }

                    // OWNER/GROUP
                    applyOwnerGroup(p, qb, "link.details.owner.id",
                            "link.details.group.id");

                    // ORDER
                    qb.order("link.details.updateEvent.id", false);

                    Query q = qb.query(session);
                    applyParameters(p, q);
                    rv.put(_parentType, q.list());
                }

                return mergeList(rv, pWithDefaults, false);
            }

        }));
    }

    public void getMostRecentShareCommentLinks_async(
            AMD_ITimeline_getMostRecentShareCommentLinks __cb,
            final Parameters p, Current __current) throws ServerError {

        final IceMapper mapper = new IceMapper(IceMapper.FILTERABLE_COLLECTION);

        runnableCall(__current, new Adapter(__cb, __current, mapper, factory
                .getExecutor(), factory.principal, new SimpleWork(this,
                "getMostRecentShareComments") {

            @Transactional(readOnly = true)
            public Object doWork(org.hibernate.Session session,
                    final ServiceFactory sf) {

                IShare sh = sf.getShareService();
                Set<ome.model.meta.Session> shares = new HashSet<ome.model.meta.Session>();
                Set<ome.model.meta.Session> shares1 = sh.getOwnShares(false);
                Set<ome.model.meta.Session> shares2 = sh.getMemberShares(false);

                if (shares1 != null) {
                    shares.addAll(shares1);
                }
                if (shares2 != null) {
                    shares.addAll(shares2);
                }

                if (shares.size() == 0) {
                    return new ArrayList<Annotation>(); // EARLY EXIT
                }

                final long userId = defaultId();
                final Set<Long> ids = new HashSet<Long>();
                for (ome.model.meta.Session s : shares) {
                    ids.add(s.getId());
                }

                final Parameters pWithDefaults = applyDefaults(p);
                final ome.parameters.Parameters ome_p;
                try {
                    ome_p = mapper.convert(pWithDefaults);
                } catch (ApiUsageException e) {
                    throw new InternalException("Failed to convert parameters"
                            + e.getMessage());
                }
                ome_p.addId(userId);
                ome_p.addIds(ids);

                final List<SessionAnnotationLink> rv = new ArrayList<SessionAnnotationLink>();
                ss.runAsAdmin(new AdminAction() {
                    public void runAsAdmin() {
                        List<SessionAnnotationLink> links = sf
                                .getQueryService().findAllByQuery(
                                        LOOKUP_SHARE_COMMENTS, ome_p);
                        rv.addAll(links);
                    }
                });

                return rv;
            }

        }));
    }

    // Helpers
    // =========================================================================

    /**
     * Main implementation for most of the interface methods in TimelineI.
     * Arguments: - parameters : if null, then no setFirst or setMax will be
     * called on query
     */
    private Map<?, ?> do_periodQuery(final boolean count,
            final List<String> types, final RTime start, final RTime end,
            final Object missingValue, final Session _s,
            final Parameters parameters) {

        final long activeStart;
        if (start != null) {
            activeStart = start.getValue();
        } else {
            activeStart = omero.rtypes.rtime_min().getValue();
        }

        final long activeEnd;
        if (end != null) {
            activeEnd = end.getValue();
        } else {
            activeEnd = omero.rtypes.rtime_max().getValue();
        }

        List<String> activeTypes = types;
        if (types == null || types.size() == 0) {
            activeTypes = new ArrayList<String>(ALLTYPES);
        }

        final Map<String, Object> returnValue = new HashMap<String, Object>();
        for (final String type : activeTypes) {

            String qString = BYPERIOD.get(type);
            if (qString == null) {
                returnValue.put(type, missingValue);
                continue;
            }

            QueryBuilder qb = new QueryBuilder(256);
            if (count) {
                qb.select("count(obj)");
                qb.skipFrom();
                qb.append(qString.replaceAll("@FETCH@", ""));
            } else {
                qb.select("obj");
                qb.skipFrom();
                qb.append(qString.replaceAll("@FETCH@", "fetch"));
            }
            qb.skipWhere();
            qb.and("");

            String owningObject = OWNERSHIP.get(type);
            if (owningObject == null) {
                if ("EventLog".equals(type)) {
                    // SPECIAL LOGIC WORKAROUND for the complicated EventLog
                    // query
                	
                	if (parameters != null && parameters.theFilter != null) {
                		if (parameters.theFilter.ownerId != null) {
                            qb.and("e.experimenter.id = :owner_id");
                            qb.param("owner_id", parameters.theFilter.ownerId
                                    .getValue());
                        }
                		if (parameters.theFilter.groupId != null) {
                            qb.and("e.experimenterGroup.id = :group_id");
                            qb.param("group_id", parameters.theFilter.groupId
                                    .getValue());
                        }
                	}
                    qb.append(")");
                } else {
                    throw new InternalException("No ownership info for: "
                            + type);
                }
            } else {
                applyOwnerGroup(parameters, qb, owningObject
                        + ".details.owner.id", owningObject
                        + ".details.group.id");
            }

            if (!count) {
                String orderBy = ORDERBY.get(type);
                if (orderBy != null) {
                    qb.append(orderBy);
                }
            }

            qb.param("start", new Timestamp(activeStart));
            qb.param("end", new Timestamp(activeEnd));
            Query q = qb.query(_s);
            applyParameters(parameters, q);

            if (count) {
                returnValue.put(type, q.uniqueResult());
            } else {
                returnValue.put(type, q.list());
            }
        }
        return returnValue;
    }

    /**
     * @see ticket:1232
     */
    private void applyParameters(final Parameters parameters, Query q) {
        int limit = Integer.MAX_VALUE;
        int offset = 0;
        if (parameters != null && parameters.theFilter != null) {
            Filter f = parameters.theFilter;
            if (f.offset != null) {
                offset = f.offset.getValue();
            }

            if (f.limit != null) {
                limit = f.limit.getValue();
            }
        }
        q.setFirstResult(offset);
        q.setMaxResults(limit);
    }

    private long defaultId() {
        String session = this.factory.sessionId().name;
        return sm.getEventContext(new Principal(session)).getCurrentUserId();
    }

    static class Entry {
        final Timestamp update;
        final String key;
        final IObject obj;

        Entry(String key, IObject obj) {
            this.key = key;
            this.obj = obj;
            this.update = obj.getDetails().getUpdateEvent().getTime();
        }
    }

    private List<Entry> mergeEntries(Map<String, List<IObject>> toMerge,
            Parameters p, boolean ascending) {

        final int swap = ascending ? 1 : -1;
        
        List<Entry> list = new ArrayList<Entry>();
        for (String key : toMerge.keySet()) {
            for (IObject obj : toMerge.get(key)) {
                list.add(new Entry(key, obj));
            }
        }

        Collections.sort(list, new Comparator<Entry>() {
            public int compare(Entry o1, Entry o2) {
                long u1 = o1.update.getTime();
                long u2 = o2.update.getTime();
                if (u1 < u2) {
                    return 1 * swap;
                } else if (u2 < u1) {
                    return -1 * swap;
                } else {
                    return 0;
                }
            }
        });

        return list;
    }

    /**
     * Accepts only a properly initialzed {@link Parameters} instance. See
     * {@link #applyDefaults(Parameters)}
     */
    private List<IObject> mergeList(Map<String, List<IObject>> toMerge,
            Parameters p, boolean ascending) {

        List<Entry> list = mergeEntries(toMerge, p, ascending);
        List<IObject> rv = new ArrayList<IObject>();
        int limit = p.theFilter.limit.getValue();

        for (int i = 0; i < Math.min(limit, list.size()); i++) {
            Entry entry = list.get(i);
            rv.add(entry.obj);
        }

        return rv;
    }

    /**
     * Accepts only a properly initialzed {@link Parameters} instance. See
     * {@link #applyDefaults(Parameters)}
     */
    private Map<String, List<IObject>> mergeMap(
            Map<String, List<IObject>> toMerge, Parameters p,
            boolean ascending) {

        // Prepare return value so there are no null arrays.
        Map<String, List<IObject>> rv = new HashMap<String, List<IObject>>();
        for (String key : toMerge.keySet()) {
            rv.put(key, new ArrayList<IObject>());
        }
        
        List<Entry> list = mergeEntries(toMerge, p, ascending);
        toMerge = null;

        int limit = p.theFilter.limit.getValue();
        for (int i = 0; i < Math.min(limit, list.size()); i++) {
            Entry entry = list.get(i);
            List<IObject> objs = rv.get(entry.key);
            objs.add(entry.obj);
        }

        return rv;
    }

    private Parameters applyDefaults(Parameters p) {
        if (p == null) {
            p = new Parameters();
        }
        if (p.theFilter == null) {
            p.theFilter = new Filter();
        }
        if (p.theFilter.offset == null) {
            p.theFilter.offset = rint(0);
        }
        if (p.theFilter.limit == null) {
            p.theFilter.limit = rint(50);
        }
        if (p.theFilter.groupId == null) {
            if (p.theFilter.ownerId == null) {
                p.theFilter.ownerId = rlong(defaultId());
            } else if (p.theFilter.ownerId.getValue() == -1L) {
                p.theFilter.ownerId = null; // Clearing as wildcard.
            }
        }

        return p;
    }

    private void applyOwnerGroup(final Parameters p, QueryBuilder qb,
            String ownerPath, String groupPath) {
        if (p != null && p.theFilter != null) {
            Filter f = p.theFilter;
            if (f.ownerId != null) {
                qb.and(ownerPath + " = :owner_id ");
                qb.param("owner_id", f.ownerId.getValue());
            }
            if (f.groupId != null) {
                qb.and(groupPath + " = :group_id ");
                qb.param("group_id", f.groupId.getValue());
            }
        }
    }

}
