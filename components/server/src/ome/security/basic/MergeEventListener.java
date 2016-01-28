/*
 * ome.security.basic.MergeEventListener
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

// Java imports
import java.util.Map;

import ome.conditions.SecurityViolation;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.meta.Event;
import ome.tools.hibernate.HibernateUtils;
import ome.util.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.EventSource;
import org.hibernate.event.MergeEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.ForeignKeyDirection;
import org.springframework.orm.hibernate3.support.IdTransferringMergeEventListener;
import org.springframework.util.Assert;

/**
 * responsible for responding to merge events. in particular in load/re-loading
 * certain types to make use by clients easier.
 * 
 * In general, enforces the detached-graph re-attachment "Commandments" as
 * outlined in TODO. Objects that are transient (no ID) are unchanged; objects
 * that are managed (with ID) are checked for validity (i.e. must have a
 * version); and unloaded/filtered objects & collections are re-filled.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since 3.0
 */
public class MergeEventListener extends IdTransferringMergeEventListener {

    public final static String MERGE_EVENT = "MergeEvent";

    private static final long serialVersionUID = 240558701677298961L;

    private static Logger log = LoggerFactory.getLogger(MergeEventListener.class);

    private final CurrentDetails cd;

    private final TokenHolder th;

    /** main constructor. Requires a non-null security system */
    public MergeEventListener(CurrentDetails cd, TokenHolder th) {
        Assert.notNull(cd);
        Assert.notNull(th);
        this.cd = cd;
        this.th = th;
    }

    @Override
    public void onMerge(MergeEvent event) throws HibernateException {
        if (cd.isDisabled(MERGE_EVENT)) {
            throw new SecurityViolation(
                    "The MergeEventListener has been disabled.");
        }

        if (earlyExit(event)) {
            return;
        }
        super.onMerge(event);
    }

    @Override
    public void onMerge(MergeEvent event, Map copyCache)
            throws HibernateException {
        if (cd.isDisabled(MERGE_EVENT)) {
            throw new SecurityViolation(
                    "The MergeEventListener has been disabled.");
        }

        if (earlyExit(event)) {
            return;
        }
        super.onMerge(event, copyCache);
    }

    private boolean earlyExit(MergeEvent event) {

        final Object entity = event.getEntity();
        final EventSource source = event.getSession();

        if ( entity instanceof IObject) {
            IObject iobject = (IObject) entity;
            if (!iobject.isLoaded()) {
                log.trace("ignoring unloaded iobject");
                event.setResult(source.load(event.getEntityName(), iobject.getId()));
                return true; //EARLY EXIT!
            }
        }
        return false;
    }


    @Override
    protected void copyValues(EntityPersister persister, Object entity,
            Object target, SessionImplementor source, Map copyCache) {

        if (entity instanceof IObject) {
            HibernateUtils.fixNulledOrFilteredCollections((IObject) entity,
                    (IObject) target, persister, source);
            propagateHiddenValues((IObject) entity, (IObject) target);
        }
        super.copyValues(persister, entity, target, source, copyCache);
    }

    @Override
    protected void copyValues(EntityPersister persister, Object entity,
            Object target, SessionImplementor source, Map copyCache,
            ForeignKeyDirection foreignKeyDirection) {

        if (entity instanceof IObject) {
            HibernateUtils.fixNulledOrFilteredCollections((IObject) entity,
                    (IObject) target, persister, source);
            propagateHiddenValues((IObject) entity, (IObject) target);
        }
        super.copyValues(persister, entity, target, source, copyCache,
                foreignKeyDirection);
    }

    @Override
    @SuppressWarnings( { "cast", "unchecked" })
    protected void entityIsTransient(MergeEvent event, Map copyCache) {
        Class cls = event.getOriginal().getClass();
        IEnum extant = null;
        if (IEnum.class.isAssignableFrom(cls)) {
            String value = ((IEnum) event.getOriginal()).getValue();
            Class type = ((IEnum) event.getOriginal()).getClass();
            Criteria c = event.getSession().createCriteria(type).add(
                    Restrictions.eq("value", value));
            extant = (IEnum) c.uniqueResult();
            if (null != extant) {
                log("Using existing Enum(", event.getEntityName(),
                        ") with value:", value);
                copyCache.put(event.getEntity(), extant);
                event.setResult(extant);
            }
        }

        // the above didn't succeed. process normally.
        if (extant == null) {
            super.entityIsTransient(event, copyCache);
        }
        fillReplacement(event);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void entityIsDetached(MergeEvent event, Map copyCache) {
        final IObject orig = (IObject) event.getOriginal();
        final EventSource source = event.getSession();
        if (HibernateUtils.isUnloaded(orig)) {
            log("Reloading unloaded entity:", event.getEntityName(), ":", orig
                    .getId());
            Class<?> k = Utils.trueClass(orig.getClass());
            Object obj = source.load(k, orig.getId());
            event.setResult(obj);
            copyCache.put(event.getEntity(), obj);
            // TODO this was maybe a bug. check if findDirty is superfluous.
        }

        else if (orig instanceof Event) {
            final Object obj = source.load(Event.class, orig.getId());
            event.setResult(obj);
            copyCache.put(event.getEntity(), obj);
        }

        else {
            super.entityIsDetached(event, copyCache);
        }
        fillReplacement(event);
    }

    // ~ Helpers
    // =========================================================================

    protected void fillReplacement(MergeEvent event) {
        if (event.getOriginal() instanceof IObject) {
            IObject obj = (IObject) event.getOriginal();
            obj.getGraphHolder().setReplacement((IObject) event.getResult());
        }
    }

    protected void propagateHiddenValues(IObject from, IObject to) {
        th.copyToken(from, to);
        if (from.getDetails() != null && from.getDetails().filteredSize() > 0) {
            to.getDetails().addFiltered(from.getDetails().filteredSet());
        }
    }

    private void log(Object... objects) {
        if (log.isDebugEnabled() && objects != null && objects.length > 0) {
            StringBuilder sb = new StringBuilder(objects.length * 16);
            for (Object obj : objects) {
                sb.append(obj == null ? "null" : obj.toString());
            }
            log.debug(sb.toString());
        }
    }
}
