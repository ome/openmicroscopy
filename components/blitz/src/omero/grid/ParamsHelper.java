/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.grid;

import ome.model.enums.Format;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.security.SecuritySystem;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.tools.hibernate.SecureMerge;
import omero.InternalException;
import omero.ServerError;
import omero.model.OriginalFileI;
import omero.model.ParseJob;
import omero.model.ParseJobI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private final static Log log = LogFactory.getLog(ParamsHelper.class);

    /** The text representation of the format in a python script. */
    public final static String PYTHONSCRIPT = "text/x-python";

    public final static String OCTETSTREAM = "application/octet-stream";

    private final SharedResourcesPrx sr;

    private final Executor ex;

    private final Principal p;

    private final SecuritySystem secSys;

    public final Format pythonFormat;

    public final Format octetFormat;

    public ParamsHelper(SharedResourcesPrx sr, Executor ex, Principal p) {
        this.sr = sr;
        this.ex = ex;
        this.p = p;
        this.secSys = // FIXME REFACTOR
            (SecuritySystem) ex.getContext().getBean("securitySystem");

        pythonFormat = loadFormat(PYTHONSCRIPT);
        octetFormat = loadFormat(OCTETSTREAM);
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
        ome.model.jobs.ParseJob job = getParseJobForScript(scriptId);

        if (job != null) {
            return parse(job.getParams(), __current);
        }
        return null;
    }

    ome.model.jobs.ParseJob getParseJobForScript(final long scriptId) {
        ome.model.jobs.ParseJob job = (ome.model.jobs.ParseJob) ex.execute(p,
                new Executor.SimpleWork(this, "getParseJobForScript", scriptId) {
                    @Transactional(readOnly = false)
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
                                                + "order by job.details.updateEvent.id desc",
                                        p);
                    }

                });
        return job;
    }

    JobParams generateScriptParams(long id, Ice.Current __current)
            throws ServerError {

        ParseJob job = buildParseJob(id);
        InteractiveProcessorPrx proc = sr.acquireProcessor(job, 10);
        if (proc == null) {
            throw new InternalException(null, null, "No processor acquired.");
        }

        job = (ParseJob) proc.getJob();
        final JobParams rv = proc.params();
        // Guaranteed non-null for a parse job
        saveScriptParams(rv, job, __current);
        return rv;
    }

    void saveScriptParams(JobParams params,
            final ParseJob job, Ice.Current __current)
            throws ServerError {

        final byte[] data = parse(params, __current);
        ex.execute(p, new Executor.SimpleWork(this, "saveScriptParams", job.getId().getValue()) {
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
        Ice.OutputStream os = Ice.Util.createOutputStream(current.adapter
                .getCommunicator());
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

    JobParams parse(byte[] data, Ice.Current current) {

        if (data == null) {
            return null; // EARLY EXIT!
        }

        Ice.InputStream is = Ice.Util.createInputStream(current.adapter
                .getCommunicator(), data);
        final JobParams[] params = new JobParams[1];
        try {
            is.readObject(new ReadObjectCallback() {
                public void invoke(Ice.Object arg0) {
                    params[0] = (JobParams) arg0;
                }
            });
            is.readPendingObjects();
        } catch (UnmarshalOutOfBoundsException oob) {
            // ok, returning null.
        } finally {
            is.destroy();
        }
        return params[0];
    }

    /**
     * Get the Format object.
     *
     * @param fmt
     *            the format to retrieve.
     * @return see above.
     */
    Format loadFormat(final String fmt) {
        return (Format) ex.execute(p, new Executor.SimpleWork(this,
                "loadFormat") {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return sf.getQueryService().findByQuery(
                        "from Format as f where f.value='" + fmt + "'", null);
            }
        });
    }

}
