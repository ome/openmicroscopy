/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.impl;

import static omero.rtypes.rmap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.model.core.OriginalFile;
import ome.model.enums.Format;
import ome.model.jobs.JobOriginalFileLink;
import ome.parameters.Parameters;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.BlitzOnly;
import ome.services.blitz.util.ServiceFactoryAware;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import ome.util.Utils;
import omero.ApiUsageException;
import omero.InternalException;
import omero.RType;
import omero.ServerError;
import omero.ValidationException;
import omero.api.AMD_IScript_deleteScript;
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
import omero.grid.Param;
import omero.model.OriginalFileI;
import omero.model.ScriptJobI;

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

    /** The text representation of the format in a python script. */
    private final static String PYTHONSCRIPT = "text/x-python";

    /** Message used in jobs which are only being parsed */
    private final static String PARSING = "ScriptI.parsing_only";

    protected ServiceFactoryI factory;

    public ScriptI(BlitzExecutor be) {
        super(null, be);
    }

    public void setServiceFactory(ServiceFactoryI sf) {
        this.factory = sf;
    }

    // ~ Service methods
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
    public void getScriptID_async(final AMD_IScript_getScriptID cb,
            final String scriptName, final Current __current)
            throws ServerError {
        runnableCall(__current, new Runnable() {
            public void run() {

                final OriginalFile file = getOriginalFileOrNull(scriptName);

                if (file == null) {
                    cb.ice_response(-1L);
                } else {
                    cb.ice_response(file.getId());
                }
            }

        });
    }

    /**
     * Upload script to the server.
     * 
     * @param script
     * @param __current
     *            ice context.
     * @return id of the script.
     */
    public void uploadScript_async(final AMD_IScript_uploadScript cb,
            final String script, final Current __current) throws ServerError {
        runnableCall(__current, new Runnable() {
            public void run() {

                try {

                    if (!validateScript(script)) {
                        cb.ice_exception(new ApiUsageException(null, null,
                                "Invalid script"));
                        return; // EARLY EXIT
                    }

                    final OriginalFile tempFile = makeFile(script);
                    writeContent(tempFile, script);
                    JobParams params = getScriptParams(tempFile, __current);

                    if (params == null) {
                        cb.ice_exception(new ApiUsageException(null, null,
                                "Script error: no params found."));
                        return; // EARLY EXIT
                    }

                    if (originalFileExists(params.name)) {
                        deleteOriginalFile(tempFile);
                        cb.ice_exception(new ApiUsageException(null, null,
                                "A script with name " + params.name
                                        + " already exists on server."));
                        return; // EARLY EXIT
                    }

                    tempFile.setName(params.name);
                    tempFile.setPath(params.name);
                    writeContent(tempFile, script);
                    OriginalFile scriptFile = updateFile(tempFile);
                    cb.ice_response(scriptFile.getId());

                } catch (Exception e) {
                    cb.ice_exception(e);
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
    public void getScriptWithDetails_async(
            final AMD_IScript_getScriptWithDetails cb, final long id,
            Current __current) throws ServerError {

        runnableCall(__current, new Runnable() {

            public void run() {

                final OriginalFile file = getOriginalFileOrNull(id);

                if (file == null) {
                    cb.ice_response(null);
                    return; // EARLY EXIT
                }

                final Long size = file.getSize();
                if (size == null || size.longValue() > Integer.MAX_VALUE
                        || size.longValue() < 0) {
                    cb.ice_exception(new ValidationException(null, null,
                            "Script size : " + size
                                    + " invalid on Blitz.OMERO server."));
                    return; // EARLY EXIT
                }

                try {
                    Map<String, RType> scr = new HashMap<String, RType>();
                    scr.put((String) factory.executor.execute(
                            factory.principal, new Executor.SimpleWork(this, "getScriptWithDetails") {
                                @Transactional(readOnly=true)
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
                    cb.ice_response(scr);
                } catch (omero.ApiUsageException e) {
                    cb.ice_exception(e);
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
    public void getScript_async(final AMD_IScript_getScript cb, final long id,
            Current __current) throws ServerError {

        runnableCall(__current, new Runnable() {
            public void run() {

                final OriginalFile file = getOriginalFileOrNull(id);
                if (file == null) {
                    cb.ice_response(null);
                    return; // EARLY EXIT.
                }

                final Long size = file.getSize();
                if (size == null || size.longValue() > Integer.MAX_VALUE
                        || size.longValue() < 0) {
                    cb.ice_exception(new ValidationException(null, null,
                            "Script size : " + size
                                    + " invalid on Blitz.OMERO server."));
                    return; // EARLY EXIT
                }

                try {
                    cb.ice_response((String) factory.executor.execute(
                            factory.principal, new Executor.SimpleWork(this, "getScript") {

				    @Transactional(readOnly=true)
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
                            }));
                } catch (Exception e) {
                    cb.ice_exception(e);
                }
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
    public void getParams_async(final AMD_IScript_getParams cb, final long id,
            final Current __current) throws ServerError {
        runnableCall(__current, new Runnable() {

            public void run() {
                try {
                    cb.ice_response(getParams(id, __current));
                } catch (Exception e) {
                    cb.ice_exception(e);
                }
            }
        });
    }

    /**
     * Run the script. This script also tests the parameters against the script
     * params, checking that the names of the parameters match and their types
     * match.
     * 
     * @param id
     *            of the script to run
     * @param map
     *            the map of parameters {String:RType} map of the parameters
     *            name, value.
     * @param __current
     *            ice context.
     * @return The results, map {String:RType}
     * @throws ServerError
     *             validation, api usage.
     */
    public void runScript_async(final AMD_IScript_runScript cb, final long id,
            final Map<String, RType> map, final Current __current)
            throws ServerError {

        runnableCall(__current, new Runnable() {

            public void run() {
                try {

                    Map<String, RType> params = getParams(id, __current);
                    for (Map.Entry<String, RType> entry : params.entrySet()) {
                        String paramName = entry.getKey();
                        RType scriptParamType = entry.getValue();
                        if (!map.containsKey(paramName)) {
                            cb
                                    .ice_exception(new ApiUsageException(
                                            null,
                                            null,
                                            "Script takes parameter "
                                                    + paramName
                                                    + " which has not supplied input params to runScript."));
                            return; // EARLY EXIT
                        }
                        RType inputParamType = map.get(paramName);
                        if (!scriptParamType.getClass().equals(
                                inputParamType.getClass())) {
                            cb
                                    .ice_exception(new ApiUsageException(
                                            null,
                                            null,
                                            "Script takes parameter "
                                                    + paramName
                                                    + " of type "
                                                    + scriptParamType
                                                    + " runScript was passed parameter "
                                                    + paramName + " of type "
                                                    + inputParamType + "."));
                            return; // EARLY EXIT
                        }
                    }
                    ScriptJobI job = buildJob(id);
                    InteractiveProcessorPrx proc = factory.acquireProcessor(
                            job, 10, __current);
                    omero.grid.ProcessPrx prx = proc.execute(rmap(map));
                    prx._wait();
                    cb.ice_response(proc.getResults(prx).getValue());
                } catch (Exception e) {
                    cb.ice_exception(e);
                }
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
    public void getScripts_async(final AMD_IScript_getScripts cb,
            Current __current) throws ServerError {

        runnableCall(__current, new Runnable() {
            public void run() {
                try {
                    final Map<Long, String> scriptMap = new HashMap<Long, String>();
                    final long fmt = getFormat(PYTHONSCRIPT).getId();
                    final String queryString = "from OriginalFile as o where o.format.id = "
                            + fmt;
                    factory.executor.execute(factory.principal,
                            new Executor.SimpleWork(this, "getScripts") {

				@Transactional(readOnly=true)
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
                    cb.ice_response(scriptMap);
                } catch (Exception e) {
                    cb.ice_exception(e);
                }
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
        runnableCall(__current, new Runnable() {
            public void run() {

                OriginalFile file = getOriginalFileOrNull(id);
                if (file == null) {
                    cb.ice_exception(new ApiUsageException(null, null,
                            "No script with id " + id + " on server."));
                    return; // EARLY EXIT
                }

                try {
                    deleteOriginalFile(file);
                    cb.ice_response();
                } catch (ServerError e) {
                    cb.ice_exception(e);
                }

            }
        });
    }

    // Non-public-methods
    // =========================================================================

    private Map<String, RType> getParams(long id, Ice.Current __current)
            throws ServerError {
        OriginalFile file = getOriginalFileOrNull(id);
        JobParams params = getScriptParams(file, __current);
        Map<String, RType> temporary = new HashMap<String, RType>();
        for (String key : params.inputs.keySet()) {
            Param p = params.inputs.get(key);
            temporary.put(key, p.prototype);
        }
        return temporary;
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
    private JobParams getScriptParams(OriginalFile file, Current __current)
            throws ServerError {
        if (file == null || file.getId() == null) {
            throw new ApiUsageException(null, null, file
                    + " is not persistent.");
        }
        ScriptJobI job = buildJob(file.getId());
        InteractiveProcessorPrx proc = this.factory.acquireProcessor(job, 10,
                __current);
        if (proc == null) {
            throw new InternalException(null, null, "No processor acquired.");
        }
        JobParams rv = proc.params();
        deleteTempJob(proc.getJob().getId().getValue());
        return rv;
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
    private OriginalFile makeFile(final String script) throws ServerError {
        String fName = "ScriptName" + UUID.randomUUID();
        OriginalFile tempFile = new OriginalFile();
        tempFile.setName(fName);
        tempFile.setPath(fName);
        tempFile.setFormat(getFormat(PYTHONSCRIPT));
        tempFile.setSize((long) script.getBytes().length);
        tempFile.setSha1(Utils.bufferToSha1(script.getBytes()));
        return updateFile(tempFile);
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

			@Transactional(readOnly=false)
                    public Object doWork(Session session, ServiceFactory sf) {
                        IUpdate update = sf.getUpdateService();
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
        factory.executor.execute(factory.principal, new Executor.SimpleWork(this, "writeContent") {
		@Transactional(readOnly=true)
            public Object doWork(Session session, ServiceFactory sf) {

                RawFileStore rawFileStore = sf.createRawFileStore();
                rawFileStore.setFileId(file.getId());
                rawFileStore.write(script.getBytes(), 0,
                        script.getBytes().length);
                return file.getId();
            }
        });
    }

    /**
     * Build a job for the script with id.
     * 
     * @param id
     *            script id.
     * @return the job.
     * @throws ServerError
     */
    private ScriptJobI buildJob(final long id) throws ServerError {
        final OriginalFileI file = new OriginalFileI(id, false);
        final ScriptJobI job = new ScriptJobI();
        job.linkOriginalFile(file);
        return job;
    }

    /**
     * Delete a temporary job used for parsing.
     */
    private void deleteTempJob(final long id) throws ServerError {

        Boolean success = (Boolean) factory.executor.execute(factory.principal,
                new Executor.SimpleWork(this, "deleteTempJob") {

		    @Transactional(readOnly=false)
                    public Object doWork(Session session, ServiceFactory sf) {
                        try {
                            IUpdate update = sf.getUpdateService();
                            update.deleteObject(new ome.model.jobs.ScriptJob(
                                    id, false));
                        } catch (ome.conditions.ValidationException ve) {
                            return false;
                        }
                        return true;
                    }

                });

        if (success == null || !success) {
            throw new omero.ApiUsageException(null, null, "Cannot delete "
                    + new ome.model.jobs.ScriptJob(id, false)
                    + "\nIs in use by other objects");
        }
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

		    @Transactional(readOnly=false)
                    public Object doWork(Session session, ServiceFactory sf) {
                        IUpdate update = sf.getUpdateService();
                        List<JobOriginalFileLink> links = sf.getQueryService()
                                .findAllByQuery(
                                        "select link from JobOriginalFileLink link "
                                                + "join link.child as file "
                                                + "join link.parent as job "
                                                + "where file.id = :id and "
                                                + "job.message = :msg",
                                        new Parameters().addId(file.getId())
                                                .addString("msg", PARSING));
                        try {
                            for (JobOriginalFileLink jobOriginalFileLink : links) {
                                update.deleteObject(jobOriginalFileLink);
                            }
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
     * Method to check that an originalFile exists in the server.
     * 
     * @param fileName
     *            name of the script file.
     * @return see above.
     */
    @SuppressWarnings("unchecked")
    private boolean originalFileExists(String fileName) {
        final String queryString = "from OriginalFile as o where o.format.id = "
                + getFormat(PYTHONSCRIPT).getId()
                + " and o.name = '"
                + fileName + "'";
        List<OriginalFile> fileList = (List<OriginalFile>) factory.executor
                .execute(factory.principal, new Executor.SimpleWork(this, "originalFileExists") {

			@Transactional(readOnly=true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().findAllByQuery(queryString,
                                null);
                    }
                });
        if (fileList == null) {
            return false;
        }
        if (fileList.size() == 0) {
            return false;
        }
        return true;
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
            final String queryString = "from OriginalFile as o where o.format.id = "
                    + getFormat(PYTHONSCRIPT).getId()
                    + " and o.name = '"
                    + name + "'";
            OriginalFile file = (OriginalFile) factory.executor.execute(
                    factory.principal, new Executor.SimpleWork(this, "getOriginalFileOrNull") {

			    @Transactional(readOnly=true)
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
                    + getFormat(PYTHONSCRIPT).getId() + " and o.id = " + id;
            OriginalFile file = (OriginalFile) factory.executor.execute(
                    factory.principal, new Executor.SimpleWork(this, "getOriginalFileOrNull") {

			    @Transactional(readOnly=true)
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
     * Get the iFormat object.
     * 
     * @param fmt
     *            the format to retrieve.
     * @return see above.
     */
    Format getFormat(String fmt) {
        return (Format) factory.executor.execute(factory.principal,
                new Executor.SimpleWork(this, "getFormat") {

		    @Transactional(readOnly=true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().findByQuery(
                                "from Format as f where f.value='"
                                        + PYTHONSCRIPT + "'", null);
                    }
                });
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

}
