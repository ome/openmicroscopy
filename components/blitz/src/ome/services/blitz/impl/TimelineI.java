/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static omero.rtypes.*;

import ome.api.local.LocalQuery;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.sessions.SessionManager;
import ome.system.Principal;
import omero.RInt;
import omero.RTime;
import omero.ServerError;
import omero.api.AMD_ITimeline_countByPeriod;
import omero.api.AMD_ITimeline_getByPeriod;
import omero.api.AMD_ITimeline_getEventsByPeriod;
import omero.api.AMD_ITimeline_getMostRecentObjects;
import omero.api.AMD_ITimeline_getMostRecentShareComments;
import omero.api._ITimelineOperations;
import omero.model.Event;
import omero.model.IObject;
import omero.sys.Filter;
import omero.sys.Parameters;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.HibernateCallback;

import Ice.Current;

/**
 * implementation of the ITimeline service interface.
 * 
 * @since Beta4
 * @see ome.api.ITimeline
 */
public class TimelineI extends AbstractAmdServant implements
        _ITimelineOperations, ServiceFactoryAware, BlitzOnly {

    private final static Log log = LogFactory.getLog(TimelineI.class);

    protected ServiceFactoryI factory;

    protected LocalQuery query;

    protected SessionManager sm;

    public TimelineI(BlitzExecutor be) {
        super(null, be);
    }

    public void setSessionManager(SessionManager sm) {
        this.sm = sm;
    }

    public void setLocalQuery(LocalQuery localQuery) {
        this.query = localQuery;
    }

    public void setServiceFactory(ServiceFactoryI sf) {
        this.factory = sf;
    }

    // ~ Service methods
    // =========================================================================

    // TODO fix mutability
    static final List<String> ALLTYPES = Arrays.asList("RenderingDef", "Image",
            "Project", "Dataset", "Annotation");
    static final Map<String, String> ORDERBY = new HashMap<String, String>();
    static final Map<String, String> BYPERIOD = new HashMap<String, String>();
    static {
        BYPERIOD.put("Project", "from Project obj "
                + "join @FETCH@ obj.details.creationEvent "
                + "join @FETCH@ obj.details.owner "
                + "join @FETCH@ obj.details.group "
                + "where obj.details.owner.id=:id and "
                + "    (obj.details.creationEvent.time > :start "
                + "  or obj.details.updateEvent.time  > :start) "
                + "and (obj.details.creationEvent.time < :end"
                + " or obj.details.updateEvent.time < :end )");
        BYPERIOD.put("Dataset", "from Dataset obj "
                + "join @FETCH@ obj.details.creationEvent "
                + "join @FETCH@ obj.details.owner "
                + "join @FETCH@ obj.details.group "
                + "left outer join @FETCH@ obj.projectLinks pdl "
                + "left outer join @FETCH@ pdl.parent p "
                + "where obj.details.owner.id=:id and "
                + "     (obj.details.creationEvent.time > :start "
                + "   or obj.details.updateEvent.time > :start) "
                + "and  (obj.details.creationEvent.time < :end "
                + "   or obj.details.updateEvent.time < :end)");
        BYPERIOD.put("RenderingDef", "from RenderingDef obj join @FETCH@ "
                + "obj.details.creationEvent join @FETCH@ obj.details.owner "
                + "join @FETCH@ obj.details.group left outer join @FETCH@ "
                + "obj.pixels p left outer join @FETCH@ p.image i "
                + "where i.details.owner.id=:id and"
                + "    (obj.details.creationEvent.time > :start "
                + "  or obj.details.updateEvent.time > :start) "
                + "and (obj.details.creationEvent.time < :end "
                + "  or obj.details.updateEvent.time < :end) ");
        ORDERBY.put("RenderingDef",
                "order by i.details.creationEvent.time desc");
        BYPERIOD.put("Image", "from Image obj "
                + "join @FETCH@ obj.details.creationEvent "
                + "join @FETCH@ obj.details.owner "
                + "join @FETCH@ obj.details.group "
                + "left outer join @FETCH@ obj.datasetLinks dil "
                + "left outer join @FETCH@ dil.parent d "
                + "left outer join @FETCH@ d.projectLinks pdl "
                + "left outer join @FETCH@ pdl.parent p "
                + "where obj.details.owner.id=:id and "
                + "     (obj.details.creationEvent.time > :start "
                + "   or obj.details.updateEvent.time " + " > :start) "
                + "and  (obj.details.creationEvent.time < :end "
                + "   or obj.details.updateEvent.time < :end)");
        BYPERIOD
                .put(
                        "Event",
                        "from EventLog obj "
                                + "left outer join @FETCH@ obj.event ev where ( "
                                + "    obj.entityType in ("
                                + "        'ome.model.core.Image', "
                                + "        'ome.model.containers.Dataset', "
                                + "        'ome.model.containers.Project') "
                                + "    and ev.id in (     "
                                + "        select id from Event where "
                                + "        experimenter.id=:id "
                                + "        and time > :start and time < :end)"
                                + ") OR (             "
                                + "    obj.entityType = 'ome.model.display.RenderingDef' "
                                + "    and obj.entityId in ("
                                + "        select rd from RenderingDef rd where "
                                + "            rd.pixels.image  in ("
                                + "                select id from Image i where"
                                + "                i.details.owner.id=:id))"
                                + "    and ev.id in ("
                                + "        select id from Event where     "
                                + "        time > :start and time < :end)"
                                + ")               ");
    }

    public void countByPeriod_async(final AMD_ITimeline_countByPeriod __cb,
            final List<String> types, final RTime start, final RTime end,
            final Current __current) throws ServerError {
        runnableCall(__current, new Runnable() {
            public void run() {
                try {
                    log.info(" Meth: TimelineI.countByPeriod");
                    log.info(String.format(" Args: %s, %s, %s", types, start,
                            end));
                    final Map<String, Long> returnValue = (Map<String, Long>) do_periodQuery(
                            true, types, userId(), start, end, Long
                                    .valueOf(-1L), new IceMapper(
                                    IceMapper.PRIMITIVE), null);
                    log.info(" Rslt: " + returnValue);
                    __cb.ice_response(returnValue);
                } catch (Exception e) {
                    log.info(" Excp: " + e);
                    __cb.ice_exception(e);
                }
            }

        });
    }

    public void getByPeriod_async(final AMD_ITimeline_getByPeriod __cb,
            final List<String> types, final RTime start, final RTime end,
            final omero.sys.Parameters p, final boolean merge, Current __current)
            throws ServerError {
        runnableCall(__current, new Runnable() {
            public void run() {
                try {
                    log.info(" Meth: TimelineI.getByPeriod");
                    log.info(String.format(" Args: %s, %s, %s", types, start,
                            end, p));
                    
                    Parameters pWithDefaults = applyDefaults(p);
                    Map<String, List<IObject>> returnValue = (Map<String, List<IObject>>) do_periodQuery(
                            false, types, userId(), start, end, null,
                            new IceMapper(IceMapper.FILTERABLE_COLLECTION),
                            pWithDefaults);

                    if (merge) {
                        returnValue = merge(returnValue, pWithDefaults);
                    }

                    log.info(" Rslt: " + returnValue);
                    __cb.ice_response(returnValue);
                } catch (Exception e) {
                    log.info(" Excp: " + e);
                    __cb.ice_exception(e);
                }
            }

        });
    }

    public void getEventsByPeriod_async(
            final AMD_ITimeline_getEventsByPeriod __cb, final RTime start,
            final RTime end, final omero.sys.Parameters p,
            final Current __current) throws ServerError {
        runnableCall(__current, new Runnable() {
            public void run() {
                try {
                    log.info(" Meth: TimelineI.getEventsByPeriod");
                    log.info(String.format(" Args: %s, %s", start, end, p));
                    Map<String, List<Event>> events = (Map<String, List<Event>>) do_periodQuery(
                            false, Arrays.asList("Event"), userId(), start,
                            end, null, new IceMapper(
                                    IceMapper.FILTERABLE_COLLECTION),
                            applyDefaults(p));
                    log.info(" Rslt: " + events.get("Event"));
                    __cb.ice_response(events.get("Event"));
                } catch (Exception e) {
                    log.info(" Excp: " + e);
                    __cb.ice_exception(e);
                }
            }

        });
    }

    public void getMostRecentObjects_async(
            final AMD_ITimeline_getMostRecentObjects __cb,
            final List<String> types, final omero.sys.Parameters p,
            final boolean merge, Current __current) throws ServerError {

        runnableCall(__current, new Runnable() {
            public void run() {
                try {
                    Filter theFilter = p == null ? null : p.theFilter;
                    RInt limit = theFilter == null ? null : theFilter.limit;
                    RInt offset = theFilter == null ? null : theFilter.offset;
                    log.info(" Meth: TimelineI.getMostRecentObjects");
                    log.info(String.format(" Args: %s offset=%s, limit=%s",
                            types, offset, limit));
                    
                    Parameters pWithDefaults = applyDefaults(p);
                    Map<String, List<IObject>> returnValue = (Map<String, List<IObject>>) do_periodQuery(
                            false, types, userId(), null, null, null,
                            new IceMapper(IceMapper.FILTERABLE_COLLECTION),
                            pWithDefaults);

                    if (merge) {
                        returnValue = merge(returnValue, pWithDefaults);
                    }

                    log.info(" Rslt: " + returnValue);
                    __cb.ice_response(returnValue);
                } catch (Exception e) {
                    log.info(" Excp: " + e);
                    __cb.ice_exception(e);
                }
            }

        });
    }

    public void getMostRecentShareComments_async(
            final AMD_ITimeline_getMostRecentShareComments __cb,
            final omero.sys.Parameters p, Current __current) throws ServerError {
        runnableCall(__current, new Runnable() {
            public void run() {
                try {
                    __cb.ice_response(null);
                } catch (Exception e) {
                    __cb.ice_exception(e);
                }
            }

        });
    }

    // Helpers
    // =========================================================================

    /**
     * Main implementation for most of the interface methods in TimelineI.
     * Arguments: - parameters : if null, then no setFirst or setMax will be
     * called on query
     */
    private Map do_periodQuery(final boolean count, final List<String> types,
            final long userId, final RTime start, final RTime end,
            final Object missingValue, final IceMapper mapper,
            final Parameters parameters) throws Ice.UserException {

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
            activeTypes = new ArrayList(ALLTYPES);
        }

        final Map<String, Object> returnValue = new HashMap<String, Object>();
        for (final String type : activeTypes) {
            HibernateCallback hc = new HibernateCallback() {
                public Object doInHibernate(org.hibernate.Session _s)
                        throws org.hibernate.HibernateException,
                        java.sql.SQLException {

                    String qString = BYPERIOD.get(type);
                    if (qString == null) {
                        return missingValue;
                    } else {
                        if (count) {
                            qString = "select count(obj) " + qString;
                            qString = qString.replaceAll("@FETCH@", "");
                        } else {
                            qString = "select obj " + qString;
                            qString = qString.replaceAll("@FETCH@", "fetch");
                            String orderBy = ORDERBY.get(type);
                            if (orderBy != null) {
                                qString = qString + orderBy;
                            }
                        }

                        Query q = _s.createQuery(qString);
                        q.setParameter("start", new Timestamp(activeStart));
                        q.setParameter("end", new Timestamp(activeEnd));
                        q.setParameter("id", userId);

                        if (parameters != null && parameters.theFilter != null) {
                            Filter f = parameters.theFilter;
                            if (f.offset != null) {
                                q.setFirstResult(f.offset.getValue());
                            }

                            if (f.limit != null) {
                                q.setMaxResults(f.limit.getValue());
                            }
                        }

                        if (count) {
                            return q.uniqueResult();
                        } else {
                            return q.list();
                        }
                    }
                };
            };
            Object value = query.execute(hc);
            value = mapper.mapReturnValue(value);
            returnValue.put(type, value);
        }
        return returnValue;
    }

    private long userId() {
        String session = this.factory.sessionId().name;
        return sm.getEventContext(new Principal(session)).getCurrentUserId();
    }

    /**
     * Accepts only a properly initialzed {@link Parameters} instance. See
     * {@link #applyDefaults(Parameters)}
     */
    private Map<String, List<IObject>> merge(Map<String, List<IObject>> toMerge, Parameters p) {
        
        int limit = p.theFilter.limit.getValue();
        
        class Entry {
            final long updateId;
            final String key;
            final IObject obj;
            Entry(String key, IObject obj) {
                this.key = key;
                this.obj = obj;
                this.updateId = obj.getDetails().getUpdateEvent().getId().getValue();
            }
        }
        
        List<Entry> list = new ArrayList<Entry>();
        for (String key : toMerge.keySet()) {
            for (IObject obj : toMerge.get(key)) {
                list.add(new Entry(key, obj));
            }
        }
        
        Collections.sort(list, new Comparator<Entry>(){
            public int compare(Entry o1, Entry o2) {
                long u1 = o1.updateId;
                long u2 = o2.updateId;
                if (u1 < u2) {
                    return 1;
                } else if (u2 < u1) {
                    return -1;
                } else {
                    return 0;
                }
            }});

        toMerge.clear();
        for (int i = 0; i < Math.min(limit, list.size()); i++) {
            Entry entry = list.get(i);
            List<IObject> objs = toMerge.get(entry.key);
            if (objs == null) {
                objs = new ArrayList<IObject>();
                toMerge.put(entry.key, objs);
            }
            objs.add(entry.obj);
        }
        
        return toMerge;
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

        return p;
    }
}
