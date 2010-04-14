/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.model.core.OriginalFile;
import ome.parameters.Parameters;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import ome.util.Utils;
import omero.ApiUsageException;
import omero.RInt;
import omero.RType;
import omero.ResourceError;
import omero.ServerError;
import omero.ValidationException;
import omero.api.AMD_IScript_deleteScript;
import omero.api.AMD_IScript_editScript;
import omero.api.AMD_IScript_getParams;
import omero.api.AMD_IScript_getScript;
import omero.api.AMD_IScript_getScriptID;
import omero.api.AMD_IScript_getScriptWithDetails;
import omero.api.AMD_IScript_getScripts;
import omero.api.AMD_IScript_runScript;
import omero.api.AMD_IScript_uploadScript;
import omero.api._IScriptOperations;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.JobParams;
import omero.grid.ParamsHelper;
import omero.grid.ProcessPrx;
import omero.grid.ScriptProcessPrx;
import omero.model.OriginalFileI;
import omero.model.ScriptJob;
import omero.model.ScriptJobI;
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
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

    private final static Log log = LogFactory.getLog(ScriptI.class);

    protected ServiceFactoryI factory;

    protected ParamsHelper helper;

    public ScriptI(BlitzExecutor be) {
        super(null, be);
    }

    public void setServiceFactory(ServiceFactoryI sf) throws ServerError {
        this.factory = sf;
        helper = new ParamsHelper(
            sf.sharedResources(), sf.getExecutor(), sf.getPrincipal());
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
                InteractiveProcessorPrx prx =
                    factory.sharedResources().acquireProcessor(job, timeout);
                ProcessPrx proc = prx.execute(omero.rtypes.rmap(inputs));

                ScriptProcessI process = new ScriptProcessI(factory, __current, prx, proc);
                return process.getProxy();
            }

        });

    }

    // ~ Script Service methods
    // =========================================================================

    /**
     * Get the id of the script with name scriptName.
     * 
     * @param scriptName
     *            Name of the script to find id for.
     * @param __current
     *            ice context.
     * @return The id of the script, -1 if no script found, or more than one
     *         script with that name.
     */
    public void getScriptID_async(final AMD_IScript_getScriptID __cb,
            final String scriptName, final Current __current)
            throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<Long>(){
            public Long call() {
                final OriginalFile file = getOriginalFileOrNull(scriptName);
                if (file == null) {
                    return -1L;
                } else {
                    return file.getId();
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
            final String scriptText, final Current __current) throws ServerError {
        safeRunnableCall(__current, __cb, false, new Callable<Long>() {
            public Long call() throws Exception {

                if (!validateScript(scriptText)) {
                    throw new ApiUsageException(null, null, "Invalid script");
                }

                OriginalFile file = makeFile(scriptText); // FIXME PATH & PERMS
                writeContent(file, scriptText);
                updateFileWithParams(__current, file);
                return file.getId();

            }

        });
    }

    public void editScript_async(final AMD_IScript_editScript __cb,
            final omero.model.OriginalFile fileObject, final String scriptText,
            final Current __current) throws ServerError {

        safeRunnableCall(__current, __cb, true, new Callable<Object>() {
            public Object call() throws Exception {

                if (!validateScript(scriptText)) {
                    throw new ApiUsageException(null, null, "Invalid script");
                }

                IceMapper mapper = new IceMapper();
                OriginalFile file = (OriginalFile) mapper.reverse(fileObject);
                writeContent(file, scriptText);
                updateFileWithParams(__current, file);
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
            Current __current) throws ServerError {

        safeRunnableCall(__current, __cb, false, new Callable<Object>() {

            public Object call() throws Exception {

                final OriginalFile file = getOriginalFileOrNull(id);

                if (file == null) {
                    return null;
                }

                final Long size = file.getSize();
                if (size == null || size.longValue() > Integer.MAX_VALUE
                        || size.longValue() < 0) {
                    throw new ValidationException(null, null,
                            "Script size : " + size
                                    + " invalid on Blitz.OMERO server.");
                }

                Map<String, RType> scr = new HashMap<String, RType>();
                scr.put((String) factory.executor.execute(
                        factory.principal, new Executor.SimpleWork(this,
                                "getScriptWithDetails") {
                            @Transactional(readOnly = true)
                            public Object doWork(Session session,
                                    ServiceFactory sf) {
                                RawFileStore rawFileStore = sf
                                        .createRawFileStore();
                                try {
                                    rawFileStore.setFileId(file.getId());
                                    String script = new String(rawFileStore
                                            .read(0L, (int) size
                                                    .longValue()));

                                    return script;
                                } finally {
                                    rawFileStore.close();
                                }
                            }
                        }), new omero.util.IceMapper().toRType(file));
                return scr;
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
    public void getScript_async(final AMD_IScript_getScript __cb, final long id,
            Current __current) throws ServerError {

        safeRunnableCall(__current, __cb, false, new Callable<Object>() {
            public Object call() throws Exception {

                final OriginalFile file = getOriginalFileOrNull(id);
                if (file == null) {
                    return null;
                }

                final Long size = file.getSize();
                if (size == null || size.longValue() > Integer.MAX_VALUE
                        || size.longValue() < 0) {
                    throw new ValidationException(null, null,
                            "Script size : " + size
                                    + " invalid on Blitz.OMERO server.");
                }

                return (String) factory.executor.execute(
                        factory.principal, new Executor.SimpleWork(this,
                                "getScript") {

                            @Transactional(readOnly = true)
                            public Object doWork(Session session,
                                    ServiceFactory sf) {
                                RawFileStore rawFileStore = sf
                                        .createRawFileStore();
                                try {
                                    rawFileStore.setFileId(file.getId());
                                    String script = new String(rawFileStore
                                            .read(0L, (int) size
                                                    .longValue()));

                                    return script;
                                } finally {
                                    rawFileStore.close();
                                }
                            }
                        });

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
                return helper.getOrCreateParams(id, __current);
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
                final Map<Long, String> scriptMap = new HashMap<Long, String>();
                final long fmt = helper.pythonFormat.getId();
                final String queryString = "from OriginalFile as o where o.format.id = "
                        + fmt;
                factory.executor.execute(factory.principal,
                        new Executor.SimpleWork(this, "getScripts") {

                            @Transactional(readOnly = true)
                            public Object doWork(Session session,
                                    ServiceFactory sf) {
                                List<OriginalFile> fileList = sf
                                        .getQueryService().findAllByQuery(
                                                queryString, null);
                                for (OriginalFile file : fileList) {
                                    scriptMap.put(file.getId(), file
                                            .getName());
                                }
                                return null;
                            }
                        });
                return scriptMap;
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
            final long id, Current __current) throws ServerError {
        safeRunnableCall(__current, cb, true, new Callable<Object>() {
            public Object call() throws Exception {

                OriginalFile file = getOriginalFileOrNull(id);
                if (file == null) {
                    throw new ApiUsageException(null, null,
                            "No script with id " + id + " on server.");
                }

                deleteOriginalFile(file);
                return null; // void

            }
        });
    }

    // Non-public-methods
    // =========================================================================

    /**
     * Make the file, this is a temporary file which will be changed when the
     * script is validated.
     *
     * @param script
     *            script.
     * @return OriginalFile tempfile..
     * @throws ServerError
     */
    private OriginalFile makeFile(final String script) throws ServerError {
        String fName = "ScriptName" + UUID.randomUUID();
        OriginalFile file = new OriginalFile();
        file.setName(fName);
        file.setPath(fName);
        file.setFormat(helper.pythonFormat.proxy());
        file.setSize((long) script.getBytes().length);
        file.setSha1(Utils.bufferToSha1(script.getBytes()));
        return updateFile(file);
    }

    /**
     * Update the file with new data.
     * 
     * @param file
     *            new file data to be updated.
     * @return updated file.
     * @throws ServerError
     */
    private OriginalFile updateFile(final OriginalFile file) throws ServerError {
        OriginalFile updatedFile = (OriginalFile) factory.executor.execute(
                factory.principal, new Executor.SimpleWork(this, "updateFile") {

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
    private void writeContent(final OriginalFile file, final String script)
            throws ServerError {
        factory.executor.execute(factory.principal, new Executor.SimpleWork(
                this, "writeContent") {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                RawFileStore rawFileStore = sf.createRawFileStore();
                try {
                    rawFileStore.setFileId(file.getId());
                    rawFileStore.write(script.getBytes(), 0,
                            script.getBytes().length);
                    return file.getId();
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
    private void deleteOriginalFile(final OriginalFile file) throws ServerError {
        Boolean success = (Boolean) factory.executor.execute(factory.principal,
                new Executor.SimpleWork(this, "deleteOriginalFile") {

                    @Transactional(readOnly = false)
                    public Object doWork(Session session, ServiceFactory sf) {
                        IUpdate update = sf.getUpdateService();
                        try {
                            update.deleteObject(file);
                        } catch (ome.conditions.ValidationException ve) {
                            return false;
                        }
                        return true;
                    }

                });

        if (success == null || !success) {
            throw new omero.ApiUsageException(null, null, "Cannot delete "
                    + file + "\nIs in use by other objects");
        }
    }

    /**
     * Method to get the original file of the script with name. This method will
     * not throw an exception, but instead will return null.
     * 
     * @param name
     *            See above.
     * @return original file or null if script does not exist or more than one
     *         script with name exists.
     */
    @SuppressWarnings("unchecked")
    private OriginalFile getOriginalFileOrNull(String name) {

        try {
            final Parameters p = new Parameters().addString("name", name);
            final String queryString = "from OriginalFile as o where o.format.id = "
                    + helper.pythonFormat.getId()
                    + " and o.name = :name";
            OriginalFile file = (OriginalFile) factory.executor.execute(
                    factory.principal, new Executor.SimpleWork(this,
                            "getOriginalFileOrNull") {

                        @Transactional(readOnly = true)
                        public Object doWork(Session session, ServiceFactory sf) {
                            return sf.getQueryService().findByQuery(
                                    queryString, p);
                        }
                    });
            return file;
        } catch (RuntimeException re) {
            return null;
        }
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
    private OriginalFile getOriginalFileOrNull(long id) {

        try {
            final String queryString = "from OriginalFile as o where o.format.id = "
                    + helper.pythonFormat.getId() + " and o.id = " + id;
            OriginalFile file = (OriginalFile) factory.executor.execute(
                    factory.principal, new Executor.SimpleWork(this,
                            "getOriginalFileOrNull", id) {

                        @Transactional(readOnly = true)
                        public Object doWork(Session session, ServiceFactory sf) {
                            return sf.getQueryService().findByQuery(
                                    queryString, null);
                        }
                    });
            return file;
        } catch (RuntimeException re) {
            return null;
        }
    }

    /**
     * Validate the script, checking that the params are specified correctly and
     * that the script does not contain any invalid commands.
     * 
     * @param script
     * @return true if script valid.
     */
    private boolean validateScript(String script) {
        return true;
    }

    private void updateFileWithParams(final Current __current,
            OriginalFile file) throws ServerError, ApiUsageException {
        try {
            JobParams params = helper.getOrCreateParams(file.getId(), __current);

            if (params == null) {
                throw new ApiUsageException(null, null, "Script error: no params found.");
            }

            file.setName(params.name);
            updateFile(file);

        } catch (ResourceError re) {
            // Probably a user script for which there's currently
            // no processor running. This doesn't signal a bad script
            // like null params do, but rather just that we'll have to
            // generate the params later.
        } catch (ValidationException ve) {
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
            log.warn("Unexpected exception on updateFileWithParams", e);
        }
    }
}
