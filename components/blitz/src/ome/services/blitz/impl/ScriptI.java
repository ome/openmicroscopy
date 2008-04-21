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
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.conditions.ApiUsageException;
import ome.model.core.OriginalFile;
import ome.model.enums.Format;
import ome.parameters.ScriptParams;
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

    /** The start of the script element in script. */
    private final static String SCRIPTSTART = "<script>";

    /** The end tag of the script element type. */
    private final static String SCRIPTEND = "</script>";

    /** The script tag denoting the start of the script description. */
    private final static String SCRIPTTAG = "script";

    /** The name of the script. */
    private final static String NAMETAG = "name";

    /** The description element of the script and variable. */
    private final static String DESCRIPTIONTAG = "description";

    /**
     * The parameter block in the script, contains list of variables passed as
     * parameters.
     */
    private final static String PARAMETERTAG = "parameters";

    /**
     * The return block in the script, contains list of variables returned by
     * the script.
     */
    private final static String RETURNTAG = "return";

    /** The variable described in the return and parameter tags. */
    private final static String VARIABLETAG = "variable";

    /** The type of the variable, should be RType, or iceType. */
    private final static String TYPEATTRIBUTE = "type";

    /** Is the variable optional. */
    private final static String OPTIONALATTRIBUTE = "optional";

    /** the name of the variable. */
    private final static String NAMEATTRIBUTE = "name";

    /** A constant to represent an empty string in the attributes . */
    private final static String EMPTYSTRING = "";

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
     *            String containing the script
     * @return id of the script.
     */
    public long uploadScript(final String script, Current __current)
            throws ServerError {
        if (!validateScript(script)) {
            throw new ApiUsageException("Invalid script");
        }
        String paramString = extractXML(script);
        ScriptParams params = getScriptParams(paramString);
        final OriginalFile scriptFile = new OriginalFile();
        scriptFile.setName(params.getScriptName());
        scriptFile.setPath(params.getScriptName());
        scriptFile.setFormat(getFormat(PYTHONSCRIPT));
        scriptFile.setSize((long) script.getBytes().length);
        scriptFile.setSha1("FIXME"); // FIXME

        factory.executor.execute(factory.principal, new Executor.Work() {

            public Object doWork(TransactionStatus status, Session session,
                    ServiceFactory sf) {
                IUpdate update = sf.getUpdateService();
                OriginalFile object = update.saveAndReturnObject(scriptFile);

                RawFileStore rawFileStore = sf.createRawFileStore();
                rawFileStore.setFileId(object.getId());
                rawFileStore.write(script.getBytes(), 0,
                        script.getBytes().length);

                return object;
            }
        });

        /*
         * XmlAnnotation xmlAnnotation = new XmlAnnotation();
         * xmlAnnotation.setName("scriptParams");
         * xmlAnnotation.setTextValue(extractXML(script)); ILink link =
         * scriptFile.linkAnnotation(xmlAnnotation); link =
         * iUpdate.saveAndReturnObject(link);
         */
        return scriptFile.getId();
    }

    /**
     * Return the script with the name to the user.
     * 
     * @param name
     *            see above.
     * @return see above.
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
     * @param script
     *            see above.
     * @return see above.
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
     * Run the script. Does not work :)
     * 
     * @param script
     * @param parameters
     *            map of the parameters name, value.
     * @return The results.
     */
    public Map<String, RType> runScript(String script, Map<String, RType> map,
            Current __current) throws ServerError {

        ScriptJobI job = buildJob(script);
        InteractiveProcessorPrx proc = this.factory.acquireProcessor(job, 10);
        omero.grid.ProcessPrx prx = proc.execute(new omero.RMap(map));
        prx._wait();
        return proc.getResults(prx).val;

    }

    /**
     * Get Scripts will return all the scripts by name available on the server.
     * 
     * @return see above.
     */
    public List<String> getScripts(Current __current) throws ServerError {
        final ArrayList<String> scriptList = new ArrayList<String>();
        final long fmt = getFormat(PYTHONSCRIPT).getId();
        final String queryString = "from OriginalFile as o where o.format.value = "
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

    private ScriptJobI buildJob(String script) throws ServerError {
        long id = getScriptID(script);
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
        final String queryString = "from OriginalFile as o where o.format.value = "
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
     * 
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

    /**
     * Get the parameters from the script.
     * 
     * @param xmlHeader
     *            The xml extracted from the script containing the param
     *            information.
     * @return A map of the parameters from the script.
     */
    private ScriptParams getScriptParams(String xmlHeader) {
        ScriptParams scriptParams = new ScriptParams();
        scriptParams = parseScriptTag(xmlHeader);
        return scriptParams;
    }

    /**
     * Parse the scripts tag in the script and build the scriptParams object
     * which will contain name, descrption, param and return maps.
     * 
     * @param script
     *            see above.
     * @param params
     *            see above.
     * @return script params object.
     * @throws ApiUsageException
     */
    private ScriptParams parseScriptTag(String script) throws ApiUsageException {
        ScriptParams params = new ScriptParams();
        IXMLElement scriptElement;
        IXMLParser parser;
        IXMLReader reader;
        ByteArrayInputStream bin;
        try {
            parser = XMLParserFactory.createDefaultXMLParser();
        } catch (Exception ex) {
            ApiUsageException e = new ApiUsageException(
                    "Unable to instantiate NanoXML Parser");
            throw e;
        }
        try {
            bin = new ByteArrayInputStream(script.getBytes());
        } catch (Exception ex) {
            ApiUsageException e = new ApiUsageException(
                    "ByteArrayInputStream bin=new ByteArrayInputStream(script.getBytes());");
            throw e;
        }
        try {
            reader = new StdXMLReader(bin);
        } catch (Exception ex) {
            ApiUsageException e = new ApiUsageException(
                    "	reader=new StdXMLReader(bin);");
            throw e;
        }
        try {
            parser.setReader(reader);
            scriptElement = (IXMLElement) parser.parse();
        } catch (Exception ex) {
            ApiUsageException e = new ApiUsageException(
                    "scriptElement=(IXMLElement) parser.parse()" + script);
            throw e;
        }

        IXMLElement scriptName = scriptElement.getFirstChildNamed(NAMETAG);
        if (scriptName == null) {
            throw new ApiUsageException("No name supplied in the <script> tag");
        }
        IXMLElement scriptDescription = scriptElement
                .getFirstChildNamed(DESCRIPTIONTAG);
        if (scriptDescription == null) {
            throw new ApiUsageException("No name supplied in the <script> tag");
        }
        params.setScriptName(scriptName.getContent());
        params.setScriptDescription(scriptDescription.getContent());
        IXMLElement parameterElement = scriptElement
                .getFirstChildNamed(PARAMETERTAG);
        IXMLElement returnElement = scriptElement.getFirstChildNamed(RETURNTAG);
        if (parameterElement == null) {
            throw new ApiUsageException("No Parameter tags.");
        }
        if (returnElement == null) {
            throw new ApiUsageException("No Return tags.");
        }
        params.setParamMap(buildVariableMap(parameterElement));
        params.setReturnMap(buildVariableMap(returnElement));
        return params;
    }

    /**
     * Build a variable map from the param and return string block of code.
     * 
     * @param variableElements
     *            The xml element containing the params or return elements
     * @return see above.
     * @throws ApiUsageException
     */
    private Map<String, String> buildVariableMap(IXMLElement variableElements)
            throws ApiUsageException {
        HashMap<String, String> variableMap = new HashMap<String, String>();
        Vector<IXMLElement> elements = variableElements
                .getChildrenNamed(VARIABLETAG);
        for (IXMLElement parameter : elements) {
            String name = parameter.getAttribute(NAMEATTRIBUTE, EMPTYSTRING);
            if (name.equals(EMPTYSTRING)) {
                throw new ApiUsageException(
                        "No name attribute specified in variable");
            }

            String type = parameter.getAttribute(TYPEATTRIBUTE, EMPTYSTRING);
            if (type.equals(EMPTYSTRING)) {
                throw new ApiUsageException(
                        "No type attribute specified in variable");
            }
            String optional = parameter.getAttribute(OPTIONALATTRIBUTE,
                    EMPTYSTRING);
            String description;
            IXMLElement descriptionElement = parameter
                    .getFirstChildNamed(DESCRIPTIONTAG);
            if (descriptionElement != null) {
                description = descriptionElement.getContent();
            }
            variableMap.put(name, type);
        }
        return variableMap;
    }

    /**
     * Extract the XML from the script <script></script> tags and remove any
     * comment strings from the text.
     * 
     * @param str
     *            see above.
     * @return see above.
     * @throws ApiUsageException
     */
    private String extractXML(String str) throws ApiUsageException {
        int start = str.indexOf(SCRIPTSTART);
        int end = str.indexOf(SCRIPTEND) + SCRIPTEND.length();
        if (end == -1 || start == -1 || end < start) {
            throw new ApiUsageException("Script does not "
                    + "contain valid XML specification.");
        }
        String xmlStrWithComments = str.substring(start, end);
        String xmlStr = xmlStrWithComments.replace("#", " ");
        return xmlStr;
    }

}
