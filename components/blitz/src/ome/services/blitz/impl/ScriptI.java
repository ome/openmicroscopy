/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.model.core.OriginalFile;
import ome.model.enums.ChecksumAlgorithm;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ParamsCache;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.scripts.RepoFile;
import ome.services.scripts.ScriptRepoHelper;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.ServiceFactory;
import ome.tools.hibernate.QueryBuilder;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumType;
import omero.ApiUsageException;
import omero.RInt;
import omero.RType;
import omero.ResourceError;
import omero.ServerError;
import omero.ValidationException;
import omero.api.AMD_IScript_canRunScript;
import omero.api.AMD_IScript_deleteScript;
import omero.api.AMD_IScript_editScript;
import omero.api.AMD_IScript_getParams;
import omero.api.AMD_IScript_getScriptID;
import omero.api.AMD_IScript_getScriptText;
import omero.api.AMD_IScript_getScriptWithDetails;
import omero.api.AMD_IScript_getScripts;
import omero.api.AMD_IScript_getUserScripts;
import omero.api.AMD_IScript_runScript;
import omero.api.AMD_IScript_uploadOfficialScript;
import omero.api.AMD_IScript_uploadScript;
import omero.api.AMD_IScript_validateScript;
import omero.api._IScriptOperations;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.JobParams;
import omero.grid.ParamsHelper.Acquirer;
import omero.grid.ProcessPrx;
import omero.grid.ProcessorPrx;
import omero.grid.ScriptProcessPrx;
import omero.grid._InteractiveProcessorOperations;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.IObject;
import omero.model.Job;
import omero.model.OriginalFileI;
import omero.model.ScriptJob;
import omero.model.ScriptJobI;
import omero.util.IceMapper;

import org.apache.commons.io.FilenameUtils;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

/**
 * implementation of the IScript service interface.
 * 
 * @author Donald MacDonald, donald@lifesci.dundee.ac.uk
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 * @see ome.api.IScript
 */
public class ScriptI extends AbstractAmdServant implements _IScriptOperations,
        ServiceFactoryAware, BlitzOnly {

    private final static Logger log = LoggerFactory.getLogger(ScriptI.class);

    protected ServiceFactoryI factory;

    protected ParamsCache cache;

    protected final ScriptRepoHelper scripts;

    protected final ChecksumProviderFactory cpf;

    public ScriptI(BlitzExecutor be, ScriptRepoHelper scripts,
            ChecksumProviderFactory cpf, ParamsCache cache) {
        super(null, be);
        this.scripts = scripts;
        this.cpf = cpf;
        this.cache = cache;
    }

    public void setServiceFactory(ServiceFactoryI sf) throws ServerError {
        this.factory = sf;
    }

    protected Acquirer acquirer() throws ServerError {
        return (Acquirer) this.factory.getServant(
                this.factory.sharedResources(null).ice_getIdentity());
    }

    // ~ Process Service methods
    // =========================================================================

    public void runScript_async(AMD_IScript_runScript __cb, final long scriptID,
            final Map<String, RType> inputs, final RInt waitSecs, final Current __current)
            throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<ScriptProcessPrx>(){
            public ScriptProcessPrx call() throws ServerError {

                ScriptJob job = new ScriptJobI();
                job.linkOriginalFile(new OriginalFileI(scriptID, false));
                int timeout = 5;
                if (waitSecs != null) {
                    timeout = waitSecs.getValue();
                }

                InteractiveProcessorPrx ipPrx = acquirer().acquireProcessor(job, timeout, __current);
                _InteractiveProcessorOperations ip =
                    (_InteractiveProcessorOperations) factory.getServant(ipPrx.ice_getIdentity());
                ProcessPrx proc = ip.execute(omero.rtypes.rmap(inputs), __current);

                ScriptProcessI process = new ScriptProcessI(factory, __current, ipPrx, ip, proc);
                process.setApplicationContext(factory.context);
                process.setHolder(factory.holder);
                return process.getProxy();
            }

        });

    }

    public void canRunScript_async(AMD_IScript_canRunScript __cb, final long scriptID,
            final Current __current) throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<Boolean>(){
            public Boolean call() throws ServerError {

                if (scripts.isInRepo(scriptID)) {
                    return true;
                }

                // currently assuming that if we have the scriptID then
                // it will have to be visible to the user/group and
                // therefore those are the values we should ask the
                // processor about.

                ProcessorCallbackI callback = new ProcessorCallbackI(factory);
                callback.setApplicationContext(factory.context);
                callback.setHolder(factory.holder);
                ProcessorPrx server = callback.activateAndWait(__current);

                return server != null;
            }

        });

    }

    // ~ Script Service methods
    // =========================================================================

    /**
     * Get the id of the official script with given path.
     * 
     * @param scriptPath
     *            {@link OriginalFile#getPath()} of the script to find id for.
     * @param __current
     *            ice context.
     * @return The id of the script, -1 if no script found, or more than one
     *         script with that name.
     */
    public void getScriptID_async(final AMD_IScript_getScriptID __cb,
            final String scriptPath, final Current __current)
            throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<Long>(){
            public Long call() {
                Long id = scripts.findInDb(scriptPath, true);
                if (id == null) {
                    return -1L;
                } else {
                    return id;
                }
            }

        });
    }

    /**
     * Upload script to the server.
     * 
     * @param scriptText
     * @param __current
     *            ice context.
     * @return id of the script.
     */
    public void uploadScript_async(final AMD_IScript_uploadScript __cb,
            final String path, final String scriptText, final Current __current) throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<Long>() {
            public Long call() throws Exception {
                OriginalFile file = makeFile(path, scriptText, __current);
                file = writeContent(file, scriptText, __current);
                validateParams(__current, file);
                return file.getId();
            }
        });
    }

    public void uploadOfficialScript_async(
            AMD_IScript_uploadOfficialScript __cb, final String path,
            final String scriptText, final Current __current) throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<Long>() {
            public Long call() throws Exception {
                EventContext ec = factory.getEventContext();
                if ( ! ec.isCurrentUserAdmin() ) {
                    throw new omero.SecurityViolation(null, null, "User is not an administrator");
                }
                try {
                    // ticket:2356 - should only overwrite non-scripts
                    Long scriptID = scripts.findInDb(path, true);
                    Long fileID = scripts.findInDb(path, false);
                    if (scriptID != null) {
                        throw new ApiUsageException(null, null,
                                "Path already exists: " + path + "\n" +
                                "Use editScript to modify existing official scripts.");
                    } else if (fileID != null) {
                        log.info("Overwriting existing non-script: " + fileID);
                        cache.removeParams(fileID);
                    }
                    RepoFile f = scripts.write(path, scriptText);
                    OriginalFile file = scripts.addOrReplace(f, fileID);
                    validateParams(__current, file);
                    return file.getId();
                } catch (IOException e) {
                    omero.ServerError se = new omero.InternalException(null, null, "Cannot write " + path);
                    IceMapper.fillServerError(se, e);
                    throw se;
                }
            }
        });
    }

    public void editScript_async(final AMD_IScript_editScript __cb,
            final omero.model.OriginalFile fileObject, final String scriptText,
            final Current __current) throws ServerError {

        safeRunnableCall(__current, __cb, true, new Callable<Object>() {
            public Object call() throws Exception {

                if (fileObject == null) {
                    throw new ApiUsageException(null, null, "file object cannot be null");
                }

                OriginalFile file = null;
                if (fileObject.isLoaded()) {
                    IceMapper mapper = new IceMapper();
                    file = (OriginalFile) mapper.reverse(fileObject);
                } else {
                    file = getOriginalFileOrNull(fileObject.getId().getValue(),
                            __current);
                }

                if (file == null) {
                    throw new ApiUsageException(null, null, "could not find file: "
                            + fileObject.getId().getValue());
                }

                // Removing update event
                // to prevent optimistic locking
                scripts.setMimetype(file);
                file = updateFile(file, __current);

                OriginalFile official = scripts.load(file.getId(), true);
                if (official != null) {
                    String fullname = official.getPath() + official.getName();
                    RepoFile f = scripts.write(fullname, scriptText);
                    file = scripts.update(f, file.getId(), __current.ctx);
                } else {
                    file = writeContent(file, scriptText, __current);
                }
                cache.removeParams(file.getId());
                validateParams(__current, file);
                return null; // void
            }

        });
    }

    /**
     * Return the script with the name to the user.
     * 
     * @param name
     *            see above.
     * @param __current
     *            ice context.
     * @return see above.
     * @throws ServerError
     *             validation, api usage.
     */
    public void getScriptWithDetails_async(
            final AMD_IScript_getScriptWithDetails __cb, final long id,
            final Current __current) throws ServerError {

        safeRunnableCall(__current, __cb, false, new Callable<Object>() {

            public Object call() throws Exception {

                final OriginalFile file = getOriginalFileOrNull(id, __current);

                if (file == null) {
                    return null;
                }

                Map<String, RType> scr = new HashMap<String, RType>();
                scr.put(loadText(file, __current), new omero.util.IceMapper().toRType(file));
                return scr;
            }

        });
    }

    private String loadText(final OriginalFile file, final Ice.Current current)
        throws ServerError {

        if (scripts.isInRepo(file.getId())) {
            try {
                return scripts.read(file.getPath() + file.getName());
            } catch (IOException e) {
                omero.ResourceError re = new omero.ResourceError(null, null, "Failed to load " + file);
                IceMapper.fillServerError(re, e);
                throw re;
            }
        }

        final Long size = file.getSize();
        if (size == null || size.longValue() > Integer.MAX_VALUE
                || size.longValue() < 0) {
            throw new ValidationException(null, null,
                    "Script size : " + size
                            + " invalid on Blitz.OMERO server.");
        }

        return (String) factory.executor.execute(current.ctx, factory.principal,
                new Executor.SimpleWork(this, "getScriptWithDetails") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        RawFileStore rawFileStore = sf.createRawFileStore();
                        try {
                            rawFileStore.setFileId(file.getId());
                            String script = new String(rawFileStore.read(0L,
                                    (int) file.getSize().longValue()));

                            return script;
                        } finally {
                            rawFileStore.close();
                        }
                    }
                });
    }

    /**
     * Return the script with the name to the user.
     * 
     * @param name
     *            see above.
     * @param __current
     *            ice context.
     * @return see above.
     * @throws ServerError
     *             validation, api usage.
     */
    public void getScriptText_async(final AMD_IScript_getScriptText __cb, final long id,
            final Current __current) throws ServerError {

        safeRunnableCall(__current, __cb, false, new Callable<Object>() {
            public Object call() throws Exception {

                final OriginalFile file = getOriginalFileOrNull(id, __current);
                if (file == null) {
                    return null;
                }

                return loadText(file, __current);
            }
        });
    }

    /**
     * Get the Parameters of the script.
     * 
     * @param id
     *            see above.
     * @param __current
     *            Ice context
     * @return see above.
     * @throws ServerError
     *             validation, api usage.
     */
    public void getParams_async(final AMD_IScript_getParams __cb, final long id,
            final Current __current) throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<Object>() {
            public Object call() throws Exception {
                final OriginalFile file = getOriginalFileOrNull(id, __current);
                if (file == null) {
                    return null;
                }
                return cache.getParams(id, file.getHash(), __current);
            }
        });
    }

    /**
     * Get Scripts will return all the scripts by id and name available on the
     * server.
     * 
     * @param __current
     *            ice context,
     * @return see above.
     * @throws ServerError
     *             validation, api usage.
     */
    public void getScripts_async(final AMD_IScript_getScripts __cb,
            Current __current) throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<Object>() {
            public Object call() throws Exception {
                List<OriginalFile> files = scripts.loadAll(true); // FIXME
                IceMapper mapper = new IceMapper();
                return mapper.map(files);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void getUserScripts_async(AMD_IScript_getUserScripts __cb,
            final List<IObject> acceptsList, final Current __current) throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<Object>() {
            public Object call() throws Exception {

                final QueryBuilder qb = new QueryBuilder();
                qb.select("o").from("OriginalFile", "o");

                if (!parseAcceptsList(qb, acceptsList)) {
                    long gid = factory.sessionManager
                        .getEventContext(factory.principal)
                        .getCurrentGroupId();
                    qb.and("o.details.group.id = " + gid);
                }

                List<Long> officialIds = scripts.idsInDb();
                if (officialIds != null && officialIds.size() > 0) {
                    qb.and("o.id not in (:ids) ");
                    qb.paramList("ids", officialIds);
                }

                List<OriginalFile> files = (List<OriginalFile>)
                factory.executor.execute(__current.ctx, factory.principal,
                        new Executor.SimpleWork(this, "getUserScripts") {
                            @Transactional(readOnly = true)
                            public Object doWork(Session session,
                                    ServiceFactory sf) {
                                return qb.query(session).list();
                            }
                        });
                IceMapper mapper = new IceMapper();
                return mapper.map(files);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void validateScript_async(AMD_IScript_validateScript __cb, final Job j,
            final List<IObject> acceptsList, final Current __current) throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<Object>() {
            public Object call() throws Exception {

                final boolean official =
                    acceptsList != null && acceptsList.size() == 0;

                final QueryBuilder qb = new QueryBuilder();
                qb.select("o").from("Job", "j");
                qb.join("j.originalFileLinks", "links", false, false);
                qb.join("links.child", "o", false, false);

                parseAcceptsList(qb, acceptsList);

                qb.and("j.id = :id");
                qb.param("id", j.getId().getValue());


                OriginalFile file = (OriginalFile) factory.executor.execute(__current.ctx, factory.principal,
                        new Executor.SimpleWork(this, "validateScript", j.getId().getValue(), acceptsList) {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {

                        List<OriginalFile> files = (List<OriginalFile>) qb.query(session).list();
                        if (files.size() != 1) {
                            throw new ome.conditions.ValidationException("Found wrong number of files: " + files);
                        }
                        Long id = files.get(0).getId();

                        if (official) {
                            return scripts.load(id, session,getSqlAction(),  true);
                        } else {
                            return sf.getQueryService()
                                    .get(OriginalFile.class, id);
                        }
                    }
                });

                return new IceMapper().map(file);

            }
        });
    }

    /**
     * Delete the script with id from the server.
     * 
     * @param id
     *            the id of the script to delete.
     * 
     */
    public void deleteScript_async(final AMD_IScript_deleteScript cb,
            final long id, final Current __current) throws ServerError {
        safeRunnableCall(__current, cb, true, new Callable<Object>() {
            public Object call() throws Exception {

                OriginalFile file = getOriginalFileOrNull(id, __current);
                if (file == null) {
                    throw new ApiUsageException(null, null,
                            "No script with id " + id + " on server.");
                }

                deleteOriginalFile(file, __current);
                return null; // void

            }
        });
    }

    // Non-public-methods
    // =========================================================================

    private boolean parseAcceptsList(final QueryBuilder qb,
            final List<IObject> acceptsList) {
        qb.where();
        scripts.buildQuery(qb);

        if (acceptsList != null && acceptsList.size() > 0) {
            for (IObject object : acceptsList) {
                if (object instanceof Experimenter) {
                    qb.and("o.details.owner.id = :oid");
                    qb.param("oid", object.getId().getValue());
                } else if (object instanceof ExperimenterGroup) {
                        qb.and("o.details.group.id = :gid");
                        qb.param("gid", object.getId().getValue());
                } else {
                    throw new ome.conditions.ValidationException(
                            "Unsupported accept-type: " + object);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Make the file, this is a temporary file which will be changed when the
     * script is validated.
     *
     * @param script
     *            script.
     * @return OriginalFile tempfile..
     * @throws ServerError
     */
    private OriginalFile makeFile(final String path, final String script,
            Ice.Current current) throws ServerError {
        OriginalFile file = new OriginalFile();
        file.setName(FilenameUtils.getName(path));
        file.setPath(FilenameUtils.getFullPath(path));
        file.setSize((long) script.getBytes().length);
        file.setHasher(new ChecksumAlgorithm("SHA1-160"));
        file.setHash(cpf.getProvider(ChecksumType.SHA1)
                .putBytes(script.getBytes()).checksumAsString());
        scripts.setMimetype(file);
        return updateFile(file, current);
    }

    /**
     * Update the file with new data.
     * 
     * @param file
     *            new file data to be updated.
     * @return updated file.
     * @throws ServerError
     */
    private OriginalFile updateFile(final OriginalFile file, final Ice.Current current) throws ServerError {
        OriginalFile updatedFile = (OriginalFile) factory.executor.execute(
                current.ctx, factory.principal, new Executor.SimpleWork(this, "updateFile") {

                    @Transactional(readOnly = false)
                    public Object doWork(Session session, ServiceFactory sf) {
                        IUpdate update = sf.getUpdateService();
                        file.getDetails().setUpdateEvent(null);
                        return update.saveAndReturnObject(file);
                    }

                });
        return updatedFile;

    }

    /**
     * Write the content of the script to the script to the originalfile.
     * 
     * @param file
     *            file
     * @param script
     *            script
     * @throws ServerError
     */
    private OriginalFile writeContent(final OriginalFile file, final String script,
            final Ice.Current current) throws ServerError {
        return (OriginalFile) factory.executor.execute(current.ctx, factory.principal, new Executor.SimpleWork(
                this, "writeContent") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                final byte[] buf = script.getBytes();
                final RawFileStore rawFileStore = sf.createRawFileStore();
                try {
                    rawFileStore.setFileId(file.getId());
                    rawFileStore.truncate(buf.length); // ticket:2337
                    rawFileStore.write(buf, 0, buf.length);
                    return rawFileStore.save();
                } finally {
                    rawFileStore.close();
                }
            }
        });
    }

    /**
     * Method to delete the original file
     * 
     * @param file
     *            the original file.
     * 
     */
    private void deleteOriginalFile(final OriginalFile file, final Ice.Current current)
        throws ServerError {

        if (file == null) {
            return;
        }

        if (scripts.delete(file.getId())) {
            return;
        }

        scripts.simpleDelete(current.ctx, factory.executor, factory.principal,
            file.getId());

    }

    /**
     * Method to get the original file of the script with id. This method will
     * not throw an exception, but instead will return null.
     * 
     * @param name
     *            See above.
     * @return original file or null if script does not exist or more than one
     *         script with name exists.
     */
    @SuppressWarnings("unchecked")
    private OriginalFile getOriginalFileOrNull(long id, final Ice.Current current) {

        try {
            final QueryBuilder qb = new QueryBuilder();
            qb.select("o").from("OriginalFile", "o");
            qb.where();
            scripts.buildQuery(qb);
            qb.and("o.id = :id");
            qb.param("id", id);

            OriginalFile file = (OriginalFile) factory.executor.execute(
                    current.ctx, factory.principal, new Executor.SimpleWork(this,
                            "getOriginalFileOrNull", id) {

                        @Transactional(readOnly = true)
                        public Object doWork(Session session, ServiceFactory sf) {
                            return qb.query(session).uniqueResult();
                        }
                    });
            return file;
        } catch (RuntimeException re) {
            return null;
        }
    }

    private void validateParams(final Current __current,
            OriginalFile file) throws ServerError, ApiUsageException {


        try {

            JobParams params = cache.getParams(file.getId(), file.getHash(), __current);

            if (params == null) {
                throw new ApiUsageException(null, null, "Script error: no params found.");
            }

        } catch (ResourceError re) {
            // Probably a user script for which there's currently
            // no processor running. This doesn't signal a bad script
            // like null params do, but rather just that we'll have to
            // generate the params later.
        } catch (ValidationException ve) {

            // Disable file - ticket:2282
            file.setMimetype("text/plain");
            file = updateFile(file, __current);

            // ticket:2184 - No longer catching ValidationException
            // so that if a processor is available that users get
            // feedback as quickly as possible. If there is something
            // else throwing a ValidationException in this call path
            // then we will have to add a new exception subclass.
            throw ve;
        } catch (Exception e) {
            // ticket:2044. Ignoring  other exceptions as well since these
            // may be caused by processor misbehavior. Note: the same
            // exception may be thrown again during execution
            log.warn("Unexpected exception on validateParams", e);
        }
    }
}
