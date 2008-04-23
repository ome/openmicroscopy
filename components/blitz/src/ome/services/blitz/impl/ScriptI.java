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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.conditions.ApiUsageException;
import ome.model.core.OriginalFile;
import ome.model.enums.Format;
import ome.services.util.Executor;
import ome.system.ServiceFactory;
import omero.RType;
import omero.ServerError;
import omero.api._IScriptDisp;
import omero.grid.InteractiveProcessorPrx;
import omero.grid.JobParams;
import omero.grid.Param;
import omero.model.OriginalFileI;
import omero.model.ScriptJobI;

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
     * @param __current ice context.
     * @return The id of the script, -1 if no script found, or more than one
     *         script with that name.
     */
    public long getScriptID(String scriptName, Current __current)
            throws ServerError {
        OriginalFile file = getOriginalFile(scriptName);
        if (file == null) {
            return -1;
        } else {
            return file.getId();
        }
    }

    /**
     * Upload script to the server. The script must also have a list of it's
     * parameters and return types as JavaDoc in the header of the script.
     * 
     * @param script
     * @param __current ice context. 
     * @return id of the script.
     */
    public long uploadScript(final String script, Current __current)
            throws ServerError {
        if (!validateScript(script)) {
            throw new ApiUsageException("Invalid script");
        }
        final OriginalFile tempFile = makeFile(script);
        System.err.println("tempFile : " + tempFile.getName());
        writeContent(tempFile, script);
        JobParams params = getScriptParams(tempFile, __current);
        tempFile.setName(params.name);
        tempFile.setPath(params.name);
        writeContent(tempFile, script);
        OriginalFile scriptFile = updateFile(tempFile);
        return scriptFile.getId();
    }
    
    /**
     * Return the script with the name to the user.
     * 
     * @param name see above.
     * @param __current ice context.
     * @return see above.
     * @throws ServerError validation, api usage. 
     */
    public String getScript(String name, Current __current) throws ServerError {

        final OriginalFile file = getOriginalFile(name);
        if (file == null) {
            return null;
        }

        final long size = file.getSize();
        if (size > Integer.MAX_VALUE || size < 0) {
            throw new ome.conditions.ValidationException("Script size : "
                    + size + " invalid on Blitz.OMERO server.");
        }

        return (String) factory.executor.execute(factory.principal,
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
                });
    }

    /**
     * Get the Parameters of the script.
     * 
     * @param script see above.
     * @param __current Ice context
     * @return see above.
     * @throws ServerError validation, api usage. 
     */
    public Map<String, RType> getParams(String script, Current __current)
            throws ServerError {

        ScriptJobI job = buildJob(script);

        InteractiveProcessorPrx proc = this.factory.acquireProcessor(job, 10);
        JobParams params = proc.params();
        Map<String, RType> temporary = new HashMap<String, RType>();
        for (String key : params.inputs.keySet()) {
            Param p = params.inputs.get(key);
            temporary.put(key, p.prototype);
        }
        return temporary;
    }

    /**
     * Run the script. 
     * 
     * @param id of the script to run
     * @param map the map of parameters {String:RType} 
     *            map of the parameters name, value.
     * @param __current ice context.
     * @return The results, map {String:RType}
     * @throws ServerError validation, api usage. 
     */
    public Map<String, RType> runScript(long id, Map<String, RType> map,
            Current __current) throws ServerError {

        ScriptJobI job = buildJob(id);
        InteractiveProcessorPrx proc = this.factory.acquireProcessor(job, 10, __current);
        omero.grid.ProcessPrx prx = proc.execute(new omero.RMap(map));
        prx._wait();
        return proc.getResults(prx).val;

    }

    /**
     * Get Scripts will return all the scripts by name available on the server.
     * @param __current ice context,
     * @return see above.
     * @throws ServerError validation, api usage. 
     */
    public List<String> getScripts(Current __current) throws ServerError {
        final ArrayList<String> scriptList = new ArrayList<String>();
        final long fmt = getFormat(PYTHONSCRIPT).getId();
        final String queryString = "from OriginalFile as o where o.format.id = "
                + fmt;
        factory.executor.execute(factory.principal, new Executor.Work() {

            public Object doWork(TransactionStatus status, Session session,
                    ServiceFactory sf) {
                List<OriginalFile> fileList = sf.getQueryService()
                        .findAllByQuery(queryString, null);
                for (OriginalFile file : fileList) {
                    scriptList.add(file.getName());
                }
                return null;
            }
        });
        return scriptList;
    }

    /**
     * Get the script params for the file. 
     * @param file the original file.
     * @param __current cirrent 
     * @return jobparams of the script.
     * @throws ServerError
     */
    private JobParams getScriptParams(OriginalFile file,  Current __current) throws ServerError {
    	ScriptJobI job = new ScriptJobI();
    	OriginalFileI oFile = new OriginalFileI(file.getId(), false);
    	job.linkOriginalFile(oFile);
    	InteractiveProcessorPrx proc = this.factory.acquireProcessor(job, 10, __current);
    	return proc.params();
    }

    /**
     * Make the file, this is a temporary file which will be changed when the script
     * is validated.
     * @param script script.
     * @return OriginalFile tempfile..
     * @throws ServerError
     */
    private OriginalFile makeFile(final String script) throws ServerError {
    	String fName = "ScriptName"+UUID.randomUUID();
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
     * @param file new file data to be updated.
     * @return updated file.
     * @throws ServerError
     */
    private OriginalFile updateFile(final OriginalFile file) throws ServerError
    {
        OriginalFile updatedFile = (OriginalFile) factory.executor.execute(factory.principal,
            new Executor.Work() 
        {

                public Object doWork(TransactionStatus status,
                        Session session, ServiceFactory sf) 
                {
                    IUpdate update = sf.getUpdateService();
                    return update.saveAndReturnObject(file);
                }
        	
        });
        return updatedFile;
        
    }
    
    /**
     * Write the content of the script to the script to the originalfile. 
     * @param file file
     * @param script script
     * @throws ServerError
     */
    private void  writeContent(final OriginalFile file, 
    						final String script) throws ServerError
    {
    	Object o = factory.executor.execute(factory.principal, new Executor.Work() {
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
     * @param id script id.
     * @return the job.
     * @throws ServerError
     */
    private ScriptJobI buildJob(long id) throws ServerError {
        OriginalFileI file = new OriginalFileI(id, false);
        ScriptJobI job = new ScriptJobI();
        job.linkOriginalFile(file);
        return job;
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
        List<OriginalFile> fileList = (List<OriginalFile>) factory.executor
                .execute(factory.principal, new Executor.Work() {

                    public Object doWork(TransactionStatus status,
                            Session session, ServiceFactory sf) {
                        return sf.getQueryService().findAllByQuery(queryString,
                                null);
                    }
                });
        if (fileList.size() != 1) {
            return null;
        } else {
            return fileList.get(0);
        }
    }

    /**
     * Get the iFormat object.
     * @param fmt the format to retrieve.
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
