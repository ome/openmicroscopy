/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.grid;

import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.security.SecuritySystem;
import ome.services.util.Executor;
import ome.services.util.IceUtil;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omero.InternalException;
import omero.ServerError;
import omero.model.Job;
import omero.model.OriginalFileI;
import omero.model.ParseJob;
import omero.model.ParseJobI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;
import Ice.ReadObjectCallback;
import Ice.UnmarshalOutOfBoundsException;

/**
 *
 *
 * @since Beta4.2
 */
public class ParamsHelper {

    private final static Logger log = LoggerFactory.getLogger(ParamsHelper.class);

    private final Acquirer acq;

    private final Executor ex;

    private final Principal p;

    private final SecuritySystem secSys;

    public ParamsHelper(Acquirer acq, Executor ex, Principal p) {
        this.acq = acq;
        this.ex = ex;
        this.p = p;
        this.secSys = // FIXME REFACTOR
            (SecuritySystem) ex.getContext().getBean("securitySystem");
    }

    /**
     * Build a job for the script with id.
     *
     * @param id
     *            script id.
     * @return the job.
     * @throws ServerError
     */
    public ParseJobI buildParseJob(final long id) throws ServerError {
        final OriginalFileI file = new OriginalFileI(id, false);
        final ParseJobI job = new ParseJobI();
        job.linkOriginalFile(file);
        job.setMessage(omero.rtypes.rstring(String.format("Parsing script %s",
                id)));
        return job;
    }

    /**
     * Get the script params for the file.
     *
     * @param file
     *            the original file.
     * @param __current
     *            cirrent
     * @return jobparams of the script.
     * @throws ServerError
     */
    public JobParams getOrCreateParams(final long id, Current __current)
            throws ServerError {

        JobParams params = getParamsOrNull(id, __current);
        if (params == null) {
            return generateScriptParams(id, __current);
        }
        return params;
    }

    JobParams getParamsOrNull(final long scriptId, Current __current) {
        ome.model.jobs.ParseJob job = getParseJobForScript(scriptId, __current);

        if (job != null) {
            try {
                return parse(job.getParams(), __current);
            } catch (CorruptJob e) {
                // pass
            }
        }
        return null;
    }

    ome.model.jobs.ParseJob getParseJobForScript(final long scriptId, final Ice.Current current) {
        ome.model.jobs.ParseJob job = (ome.model.jobs.ParseJob) ex.execute(current.ctx, p,
                new Executor.SimpleWork(this, "getParseJobForScript", scriptId) {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        Parameters p = new Parameters();
                        p.page(0, 1);
                        p.addLong("id", scriptId);
                        return sf
                                .getQueryService()
                                .findByQuery(
                                        "select job from ParseJob job "
                                                + "join job.originalFileLinks scriptlinks "
                                                + "join scriptlinks.child script "
                                                + "where job.params is not null "
                                                + "and script.id = :id "
                                                + "and script.details.updateEvent.id <= job.details.updateEvent.id "
                                                + "order by job.details.updateEvent.id desc",
                                        p);
                    }

                });
        return job;
    }

    JobParams generateScriptParams(long id, Ice.Current __current)
            throws ServerError {

        ParseJob job = buildParseJob(id);
        InteractiveProcessorPrx proc = acq.acquireProcessor(job, 10, __current);
        if (proc == null) {
            throw new InternalException(null, null, "No processor acquired.");
        }

        job = (ParseJob) proc.getJob(__current.ctx);
        final JobParams rv = proc.params(__current.ctx);
        // Guaranteed non-null for a parse job
        saveScriptParams(rv, job, __current);
        return rv;
    }

    void saveScriptParams(JobParams params,
            final ParseJob job, Ice.Current __current)
            throws ServerError {

        final byte[] data = parse(params, __current);
        ex.execute(__current.ctx, p, new Executor.SimpleWork(this, "saveScriptParams", job.getId().getValue()) {
            @Transactional(readOnly = false)
            public Object doWork(final Session session, final ServiceFactory sf) {
                ome.model.jobs.ParseJob parseJob = sf.getQueryService().get(
                        ome.model.jobs.ParseJob.class, job.getId().getValue());
                parseJob.setParams(data);
                secSys.runAsAdmin(new AdminAction(){
                    public void runAsAdmin() {
                        session.flush();
                    }});
                return null;
            }
        });
    }

    byte[] parse(JobParams params, Ice.Current current) {
        Ice.OutputStream os = IceUtil.createSafeOutputStream(
                current.adapter.getCommunicator());
        byte[] bytes = null;
        try {
            os.writeObject(params);
            os.writePendingObjects();
            bytes = os.finished();
        } finally {
            os.destroy();
        }
        return bytes;
    }

    JobParams parse(byte[] data, Ice.Current current) throws CorruptJob {

        if (data == null) {
            return null; // EARLY EXIT!
        }

        Ice.InputStream is = IceUtil.createSafeInputStream(
                current.adapter.getCommunicator(), data);
        final JobParams[] params = new JobParams[1];
        try {
            is.readObject(new ReadObjectCallback() {
                public void invoke(Ice.Object arg0) {
                    params[0] = (JobParams) arg0;
                }
            });
            is.readPendingObjects();
        } catch (UnmarshalOutOfBoundsException oob) {
            // ok, asking for deletion
            log.error(String.format("UnmarshalOutOfBoundsException: %s",
                    oob.reason));
            throw new CorruptJob();
        } catch (Ice.MarshalException me) {
            // less specific than oob; not great, but also asking for delete. #5662
            log.error(String.format("MarshalException: %s (len=%s)",
                    me.reason, data.length));
            throw new CorruptJob();
        } catch (OutOfMemoryError oom) {
            // Not ok, but not much we can do.
            // This is caused by changes to slice files.
            // See:
            log.error("http://www.zeroc.com/forums/bug-reports/4782-3-3-1-outofmemory-client-when-slice-definition-modified.html");
            throw new CorruptJob();
        } finally {
            is.destroy();
        }
        return params[0];
    }

    /**
     * Interface added in order to allow ParamHelper instances to use methods
     * from SharedResourcesI. The build does not allow for a dependency between
     * the two.
     * @DEV.TODO refactor
     */
    public interface Acquirer {
        public InteractiveProcessorPrx acquireProcessor(final Job submittedJob,
                int seconds, final Current current) throws ServerError;
    }

    /**
     * Exception raised by {@link ParamsHelper#parse(byte[], Current)} when
     * an error occurs. Under some conditions the params[0] variable can be
     * filled with an incomplete but non-null entry, making it safer to never
     * let the variable escape.
     */
    private class CorruptJob extends Exception {

        private static final long serialVersionUID = 1L;

    }
}
