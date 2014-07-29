/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.basic;

import java.util.Map;

import ome.conditions.SecurityViolation;
import ome.model.IEnum;
import ome.model.IObject;
import ome.tools.hibernate.HibernateUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.MergeEvent;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.hibernate.event.internal.DefaultSaveEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.ForeignKeyDirection;
import org.springframework.util.Assert;

/**
 * event listener which will only handle newly created (transient) objects in
 * a given graph. It will ignore unloaded and detached objects by default. This
 * is primarily a performance optimization.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 *  * @since 4.0
 */
public class SaveEventListener extends DefaultSaveEventListener {

    public final static String SAVE_EVENT = "SaveEvent";

    private static final long serialVersionUID = 1111101677298961L;

    private static Logger log = LoggerFactory.getLogger(SaveEventListener.class);

    private final CurrentDetails cd;

    private final TokenHolder th;

    /** main constructor. Requires a non-null security system */
    public SaveEventListener(CurrentDetails cd, TokenHolder th) {
        Assert.notNull(cd);
        Assert.notNull(th);
        this.cd = cd;
        this.th = th;
    }

    @Override
    public void onSaveOrUpdate(SaveOrUpdateEvent event)
            throws HibernateException {
        if (cd.isDisabled(SAVE_EVENT)) {
            throw new SecurityViolation("The SaveOrUpdateEventListener has been disabled");
        }
        super.onSaveOrUpdate(event);
    }
    /*
    protected void copyValues(EntityPersister persister, Object entity,
            Object target, SessionImplementor source, Map copyCache) {

        if (entity instanceof IObject) {
            HibernateUtils.fixNulledOrFilteredCollections((IObject) entity,
                    (IObject) target, persister, source);
            propagateHiddenValues((IObject) entity, (IObject) target);
        }
        super.copyValues(persister, entity, target, source, copyCache);
    }


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

    protected void entityIsDetached(MergeEvent event, Map copyCache) {
        IObject orig = (IObject) event.getOriginal();
        if (HibernateUtils.isUnloaded(orig)) {
            final EventSource source = event.getSession();
            log("Reloading unloaded entity:", event.getEntityName(), ":", orig
                    .getId());
            Object obj = source.load(orig.getClass(), orig.getId());
            event.setResult(obj);
            copyCache.put(event.getEntity(), obj);
            fillReplacement(event);
            return; // EARLY EXIT!
            // TODO this was maybe a bug. check if findDirty is superfluous.
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
    */
}
