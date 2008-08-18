/*
 * $Id$
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package ome.services.blitz.impl;

// Java imports
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.core.OriginalFile;
import ome.model.enums.Format;
import ome.model.jobs.JobStatus;
import ome.model.jobs.JobOriginalFileLink;
import ome.model.jobs.ScriptJob;
import ome.parameters.Parameters;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import omero.RLong;
import omero.RObject;
import omero.RString;
import omero.RType;
import omero.ServerError;
import omero.api.AMD_IScript_deleteScript;
import omero.api.AMD_IScript_getParams;
import omero.api.AMD_IScript_getScript;
import omero.api.AMD_IScript_getScriptID;
import omero.api.AMD_IScript_getScriptWithDetails;
import omero.api.AMD_IScript_getScripts;
import omero.api.AMD_IScript_runScript;
import omero.api.AMD_IScript_uploadScript;
import omero.api._IScriptDisp;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.JobParams;
import omero.grid.Param;
import omero.model.OriginalFileI;
import omero.model.ScriptJobI;
import omero.model.TagAnnotationI;

import org.hibernate.Session;
import org.springframework.transaction.TransactionStatus;

import Ice.Current;

/**
 * implementation of the IScript service interface.
 * 
 * @author Donald MacDonald, donald@lifesci.dundee.ac.uk
 * @version $Revision: 1949 $, $Date: 2007-12-03 11:54:46 +0000 (Mon, 03 Dec
 *          2007) $
 * @since 3.0-M3
 * @see IScript
 */
public class ScriptI extends _IScriptDisp {

    /** The text representation of the format in a python script. */
    private final static String PYTHONSCRIPT = "text/x-python";

    /** Message used in jobs which are only being parsed */
    private final static String PARSING = "ScriptI.parsing_only";

    protected final ServiceFactoryI factory;

    public ScriptI(ServiceFactoryI factory) {
        this.factory = factory;
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
    public void getScriptID_async(AMD_IScript_getScriptID cb,
            String scriptName, Current __current) throws ServerError {
        OriginalFile file = getOriginalFile(scriptName);
        if (file == null) {
            cb.ice_response(-1L);
        } else {
            cb.ice_response(file.getId());
        }
    }

    /**
     * Upload script to the server.
     * 
     * @param script
     * @param __current
     *            ice context.
     * @return id of the script.
     */
    public void uploadScript_async(AMD_IScript_uploadScript cb,
            final String script, Current __current) throws ServerError {
        if (!validateScript(script)) {
            cb.ice_exception(new ApiUsageException("Invalid script"));
        }
        final OriginalFile tempFile = makeFile(script);
        writeContent(tempFile, script);
        JobParams params = getScriptParams(tempFile, __current);
        if (originalFileExists(params.name)) {
            deleteOriginalFile(tempFile);
            cb.ice_exception(new ApiUsageException("A script with name "
                    + params.name + " already exists on server."));
        }
        if (params == null) {
            cb.ice_exception(new ApiUsageException(
                    "Script error: no params found."));
        }
        tempFile.setName(params.name);
        tempFile.setPath(params.name);
        writeContent(tempFile, script);
        OriginalFile scriptFile = updateFile(tempFile);
        cb.ice_response(scriptFile.getId());
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
    public void getScriptWithDetails_async(AMD_IScript_getScriptWithDetails cb,
            long id, Current __current) throws ServerError {

        final OriginalFile file = getOriginalFile(id);
        if (file == null) {
            cb.ice_response(null);
        }

        final long size = file.getSize();
        if (size > Integer.MAX_VALUE || size < 0) {
            cb
                    .ice_exception(new ome.conditions.ValidationException(
                            "Script size : " + size
                                    + " invalid on Blitz.OMERO server."));
        }

        Map<String, RType> scr = new HashMap<String, RType>();
        scr.put((String) factory.executor.execute(factory.principal,
                new Executor.Work() {

                    public Object doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {
                        RawFileStore rawFileStore = sf.createRawFileStore();
                        try {
                            rawFileStore.setFileId(file.getId());
                            String script = new String(rawFileStore.read(0L,
                                    (int) size));

                            return script;
                        } finally {
                            rawFileStore.close();
                        }
                    }
                }), new omero.util.IceMapper().toRType(file));
        cb.ice_response(scr);
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
    public void getScript_async(AMD_IScript_getScript cb, long id,
            Current __current) throws ServerError {

        final OriginalFile file = getOriginalFile(id);
        if (file == null) {
            cb.ice_response(null);
        }

        final long size = file.getSize();
        if (size > Integer.MAX_VALUE || size < 0) {
            cb
                    .ice_exception(new ome.conditions.ValidationException(
                            "Script size : " + size
                                    + " invalid on Blitz.OMERO server."));
        }

        cb.ice_response((String) factory.executor.execute(factory.principal,
                new Executor.Work() {

                    public Object doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {
                        RawFileStore rawFileStore = sf.createRawFileStore();
                        try {
                            rawFileStore.setFileId(file.getId());
                            String script = new String(rawFileStore.read(0L,
                                    (int) size));

                            return script;
                        } finally {
                            rawFileStore.close();
                        }
                    }
                }));
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
    public void getParams_async(AMD_IScript_getParams cb, long id,
            Current __current) throws ServerError {
        OriginalFile file = getOriginalFile(id);
        JobParams params = getScriptParams(file, __current);
        Map<String, RType> temporary = new HashMap<String, RType>();
        for (String key : params.inputs.keySet()) {
            Param p = params.inputs.get(key);
            temporary.put(key, p.prototype);
        }
        cb.ice_response(temporary);
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
    public void runScript_async(AMD_IScript_runScript cb, long id,
            Map<String, RType> map, Current __current) throws ServerError {
        Map<String, RType> params = getParams(id, __current);
        Iterator<String> paramIterator = params.keySet().iterator();
        while (paramIterator.hasNext()) {
            String paramName = paramIterator.next();
            RType scriptParamType = params.get(paramName);
            if (!map.containsKey(paramName)) {
                cb
                        .ice_exception(new ApiUsageException(
                                "Script takes parameter "
                                        + paramName
                                        + " which has not supplied input params to runScript."));
            }
            RType inputParamType = map.get(paramName);
            if (!scriptParamType.getClass().equals(inputParamType.getClass())) {
                cb.ice_exception(new ApiUsageException(
                        "Script takes parameter " + paramName + " of type "
                                + scriptParamType
                                + " runScript was passed parameter "
                                + paramName + " of type " + inputParamType
                                + "."));
            }
        }
        ScriptJobI job = buildJob(id);
        InteractiveProcessorPrx proc = this.factory.acquireProcessor(job, 10,
                __current);
        omero.grid.ProcessPrx prx = proc.execute(new omero.RMap(map));
        prx._wait();
        cb.ice_response(proc.getResults(prx).val);
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
    public void getScripts_async(AMD_IScript_getScripts cb, Current __current)
            throws ServerError {
        final Map<Long, String> scriptMap = new HashMap<Long, String>();
        final long fmt = getFormat(PYTHONSCRIPT).getId();
        final String queryString = "from OriginalFile as o where o.format.id = "
                + fmt;
        factory.executor.execute(factory.principal, new Executor.Work() {

            public Object doWork(TransactionStatus status, Session session,
                    ServiceFactory sf) {
                List<OriginalFile> fileList = sf.getQueryService()
                        .findAllByQuery(queryString, null);
                for (OriginalFile file : fileList) {
                    scriptMap.put(new Long(file.getId()), file.getName());
                }
                return null;
            }
        });
        cb.ice_response(scriptMap);
    }

    /**
     * Delete the script with id from the server.
     * 
     * @param id
     *            the id of the script to delete.
     * 
     */
    public void deleteScript_async(AMD_IScript_deleteScript cb, long id,
            Current __current) throws ServerError {
        OriginalFile file = getOriginalFile(id);
        if (file == null) {
            cb.ice_exception(new ApiUsageException("No script with id " + id
                    + " on server."));
        }
        deleteOriginalFile(file);
        cb.ice_response();
    }

    // Non-public-methods
    // =========================================================================

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
            throw new ApiUsageException(file + " is not persistent.");
        }
        ScriptJobI job = buildJob(file.getId());
        InteractiveProcessorPrx proc = this.factory.acquireProcessor(job, 10,
                __current);
        JobParams rv = proc.params();
        deleteTempJob(proc.getJob().id.val);
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
        tempFile.setSha1("FIXME"); // FIXME
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
                factory.principal, new Executor.Work() {

                    public Object doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {
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
        factory.executor.execute(factory.principal, new Executor.Work() {
            public Object doWork(TransactionStatus status, Session session,
                    ServiceFactory sf) {

                RawFileStore rawFileStore = sf.createRawFileStore();
                rawFileStore.setFileId(file.getId());
                rawFileStore.write(script.getBytes(), 0,
                        script.getBytes().length);
                return file.getId();
            }
        });
    }

    /**
     * Build a job from a script name
     * 
     * @param script
     * @return job.
     * @throws ServerError
     */
    private ScriptJobI buildJob(String script) throws ServerError {
        long id = getScriptID(script);
        return buildJob(id);
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
                new Executor.Work() {

                    public Object doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {
                        try {
                            IUpdate update = sf.getUpdateService();
                            update.deleteObject(new ome.model.jobs.ScriptJob(
                                    id, false));
                        } catch (ValidationException ve) {
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
                new Executor.Work() {

                    public Object doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {
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
                        } catch (ValidationException ve) {
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
                .execute(factory.principal, new Executor.Work() {

                    public Object doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {
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
     * Method to get the original file of the script with name
     * 
     * @param name
     *            See above.
     * @return original file or null if script does not exist or more than one
     *         script with name exists.
     */
    @SuppressWarnings("unchecked")
    private OriginalFile getOriginalFile(String name) {
        final String queryString = "from OriginalFile as o where o.format.id = "
                + getFormat(PYTHONSCRIPT).getId()
                + " and o.name = '"
                + name
                + "'";
        OriginalFile file = (OriginalFile) factory.executor.execute(
                factory.principal, new Executor.Work() {

                    public Object doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {
                        return sf.getQueryService().findByQuery(queryString,
                                null);
                    }
                });
        return file;
    }

    /**
     * Method to get the original file of the script with id
     * 
     * @param name
     *            See above.
     * @return original file or null if script does not exist or more than one
     *         script with name exists.
     */
    @SuppressWarnings("unchecked")
    private OriginalFile getOriginalFile(long id) {
        final String queryString = "from OriginalFile as o where o.format.id = "
                + getFormat(PYTHONSCRIPT).getId() + " and o.id = " + id;
        OriginalFile file = (OriginalFile) factory.executor.execute(
                factory.principal, new Executor.Work() {

                    public Object doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {
                        return sf.getQueryService().findByQuery(queryString,
                                null);
                    }
                });
        return file;
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
                new Executor.Work() {

                    public Object doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {
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
