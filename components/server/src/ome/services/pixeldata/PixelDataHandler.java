/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.pixeldata;

import ome.api.IQuery;
import ome.api.IUpdate;
import ome.io.nio.PixelsService;
import ome.model.core.Channel;
import ome.model.core.Pixels;
import ome.model.meta.EventLog;
import ome.model.stats.StatsInfo;
import ome.parameters.Parameters;
import ome.services.eventlogs.EventLogLoader;
import ome.services.util.Executor.SimpleWork;
import ome.system.ServiceFactory;
import ome.util.ShallowCopy;
import ome.util.SqlAction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simple action which can be done in an asynchronous thread in order to process
 * PIXELDATA event logs.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4.3
 */
public class PixelDataHandler extends SimpleWork {

    private final static Log log = LogFactory.getLog(PixelDataHandler.class);

    final protected PersistentEventLogLoader loader;

    final protected PixelsService pixelsService;

    protected int reps = 5;

    /**
     * Spring injector. Sets the number of indexing runs will be made if there
     * is a substantial backlog.
     */
    public void setRepetitions(int reps) {
        this.reps = reps;
        ;
    }

    public PixelDataHandler(PersistentEventLogLoader ll, PixelsService pixelsService) {
        super("PixelDataHandler", "process");
        this.loader = ll;
        this.pixelsService = pixelsService;
    }

    /**
     * Since these instances are used repeatedly, we need to check for
     * already set SqlAction
     */
    @Override
    public synchronized void setSqlAction(SqlAction sql) {
        if (getSqlAction() == null) {
            super.setSqlAction(sql);
        }
    }

    /**
     * Handles only single elements from the {@link PersistentEventLogLoader}
     * in order to keep transactions short and safe.
     *
     * @see ticket:5814
     */
    @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
    public Object doWork(Session session, ServiceFactory sf) {

        EventLog eventLog = loadNext();
        if (eventLog == null)
        {
            return null;
        }

        final long start = System.currentTimeMillis();
        final boolean handled = process(eventLog.getEntityId(), sf, session);
        final String msg = String.format("EventLog:%s(entityId=%s) [%s ms.]",
                eventLog.getId(), eventLog.getEntityId(),
                (System.currentTimeMillis() - start));

        if (handled) {
            log.info("HANDLED "+ msg);
        } else {
            log.debug("SKIPPED "+ msg);
        }

        return null;
    }

    /**
     * Synchronized loading since the event log loader infrastructure assumes
     * a single threaded environment.
     */
    private synchronized EventLog loadNext()
    {
        if (!loader.hasNext()) {
            log.debug("No objects indexed");
            return null;
        }

        return loader.next();
    }

    /**
     * Here we assume that our log loader will only return
     * us the proper types, since we are using the specific
     * type defined in this package.
     *
     * @param eventLog
     * @param sf
     * @param s
     * @return
     */
    public boolean process(Long id, ServiceFactory sf, Session s) {

        final Pixels pixels = getPixels(id, sf);

        if (pixels == null) {
            log.error("No valid pixels found with id=" + id);
            return false;
        }

        try
        {
            StatsInfo[] statsInfo = pixelsService.makePyramid(pixels);
            if(statsInfo == null) {
                // Either exists or failed to be created, but that's
                // the PixelsService's business. It should throw an exception
                // if it wants us to more concretely handle any issues.
                log.debug("No min/max values for pixels " + id);
                return false;
            }

            for(int c=0;c<statsInfo.length;c++) {
                final StatsInfo si = statsInfo[c];
                final Channel ch = pixels.getChannel(c);
                long siId = getSqlAction().setStatsInfo(ch, si);
                log.info(String.format("Added StatsInfo:%s for %s - C:%s Max:%s Min:%s",
                        siId, ch, c, si.getGlobalMax(), si.getGlobalMin()));
            }
        } catch (Exception t) {
            log.error("Failed to handle pixels " + id, t);
            return false;
        }

        return true;
    }

    protected Pixels getPixels(Long id, ServiceFactory sf)
    {
        final IQuery iQuery = sf.getQueryService();
        final IUpdate iUpdate = sf.getUpdateService();
        final Pixels pixels = iQuery.findByQuery(
                "select p from Pixels as p " +
                "left outer join fetch p.channels ch " + // For statsinfo
                "join fetch p.pixelsType where p.id = :id ",
                new Parameters().addId(id));
        return pixels;
    }

}
