/*
 * ome.logic.ScriptImpl
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

package ome.logic;

// Java imports
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

// Third-party libraries
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.model.IAnnotated;
import ome.model.ILink;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.core.OriginalFile;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.api.IScript;
import ome.api.RawFileStore;
import ome.api.ServiceInterface;
import ome.services.util.OmeroAroundInvoke;
import ome.system.Version;
import ome.parameters.Parameters;
import ome.parameters.ScriptParams;
import ome.model.enums.Format;
import ome.model.annotations.XmlAnnotation;

/**
 * implementation of the IScript service interface.
 * 
 * @author Donald MacDonald, donald@lifesci.dundee.ac.uk
 * @version $Revision: 1949 $, $Date: 2007-12-03 11:54:46 +0000 (Mon, 03 Dec 2007) $
 * @since 3.0-M3
 * @see IScript
 */

/*
 * Developer notes: --------------- The two annotations below are activated by
 * setting the subversion properties on this class file. They can be accessed
 * via ome.system.Version
 */
//@RevisionDate("$Date: 2007-12-03 11:54:46 +0000 (Mon, 03 Dec 2007) $")
//@RevisionNumber("$Revision: 1949 $")
/*
 * Developer notes: --------------- The annotations below (and on the individual
 * methods) are central to the definition of this service. They are used in
 * place of XML configuration files (though the XML deployment descriptors, as
 * they are called, can be used to override the annotations), and will influence
 */
// ~ Service annotations
// =============================================================================
/*
 * Source: EJB3 Specification Purpose: Prevents the Container from managing
 * transactions (CMT), and instead delegates commits and rollbacks to user code.
 * This is, however, managed by Spring (@Transactional below)
 * 
 * @see https://trac.openmicroscopy.org.uk/omero/ticket/427
 */
@TransactionManagement(TransactionManagementType.BEAN)
/*
 * Source: Spring Purpose: Used by EventHandler#checkReadyOnly(MethodInvocation)
 * to deteremine if a method is read-only. No annotation implies ready-only, so
 * it is essential to have this annotation on all write methods.
 * 
 * Only non-container annotation and is a superset of EJB3's
 * @TransactionAttribute. Currently, suffices for our TX needs, but we may
 * eventually have to provide both annotations or to switch to the EJB3 spec
 * annotations and write a new TX-interceptor for Spring.
 * 
 * @see http://www.interface21.com/pitchfork Project Pitchfork.
 */
@Transactional
/*
 * Source: EJB3 Specification Purpose: Marks this service as stateless, which
 * means that instances can be created as needed and given to any client.
 * Concurrent calls are permitted though they may be routed to different
 * servers. The stateful counterpart is simply @Stateful, but it imposes several
 * restrictions on the class, e.g. that all fields must be transient or
 * serializable. On the other hand, all fields for @Stateless instances must be
 * thread-safe, with no state being obviously thread-safetest.
 * 
 * @see https://trac.openmicroscopy.org.uk/omero/ticket/173
 */
@Stateless
/*
 * Source: EJB3 Specification Purpose: Defines which interface will be
 * represented to remote clients of this service.
 */
@Remote(IScript.class)
/*
 * Source: JBoss-speciifc Purpose: Defines a non-standard name for looking up
 * this service. During the early days of the EJB3 spec, the default value kept
 * changing and so it was easier to define our own. This is also somewhat
 * memorable in comparison to "[earFile]/[ejbName]/remote", however, this
 * doesn't allow Omero to be deployed multiple times in a single server.
 *
 * The second remote binding annotation permits transport over a secure
 * protocol. For more information on enabling this transport, see
 *
 *   https://trac.openmicroscopy.org.uk/omero/wiki/OmeroSecurity
 *
 */
@RemoteBindings({
    @RemoteBinding(jndiBinding = "omero/remote/ome.api.IScript"),
    @RemoteBinding(jndiBinding = "omero/secure/ome.api.IScript",
		   clientBindUrl="sslsocket://0.0.0.0:3843")
})
/*
 * Source: EJB3 Specification Purpose: Defines which interface will be
 * represented to remote clients of this service. There need be no relationship
 * between this interface and the @Remote interface. Currently unused, since
 * services don't look up dependencies from JNDI but rather have them injected
 * by Spring.
 */
@Local(IScript.class)
/*
 * Source: JBoss-specific Purpose: See @RemoteBinding.
 */
@LocalBinding(jndiBinding = "omero/local/ome.api.IScript")
/*
 * Source: JBoss-specific Purpose: Defines which security manager service is
 * responsible for calls to this service. This value is defined in:
 * 
 * components/app/resourcs/jboss-login.xml
 * 
 * and specifies where the manager should be found in JNDI.
 */
@SecurityDomain("OmeroSecurity")
/*
 * Source: EJB3 Specification Purpose: List of classes (with no-arg
 * constructors) which should serve as interceptors for all calls to this class.
 * Available interceptors are:
 * 
 * @AroundInvoke (interceptor - around every method call) @PostConstruct
 * (callback - after initialization) @PreDestroy (callback - before dropping
 * this instance)
 * 
 * For @Stateful services there are also: @PostActivate (callback - after the
 * instance is deserialized) @PrePassivate (callback - before the instance is
 * serialized)
 * 
 * The SimpleLifecycle does the minimum (calls create() after init and destroy()
 * before destruction) and saves a good deal of extra typing. This can also be
 * achieved by inheritance (i.e. AbstractBean.create() could be marked
 * @PostConstruct); however, only one class in an inheritance hierarchy can be
 * marked with callbacks, and this is overly restrictive for us.
 * 
 * OmeroAroundInvoke applies the OMERO security model to every method
 * invocation as well as applying the list of compile-time determined 
 * HardWiredInterceptors. All of this functionality should be trangential
 * to the functioning of the services *server-side*.
 */
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
/*
 * Stateful differences: -------------------- @Cache(NoPassivationCache.class) --
 * JBoss-specific Purpose: can be used to turn off passivation
 */
public class ScriptImpl extends AbstractLevel2Service implements IScript {


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
	
	/** The parameter block in the script, contains list of variables passed as 
	 * parameters. */
	private final static String PARAMETERTAG = "parameters";
	
	/** The return block in the script, contains list of variables returned by 
	 * the script. */
	private final static String RETURNTAG = "return";
	
	/** The variable described in the return and parameter tags. */
	private final static String VARIABLETAG = "variable";
	
	/** The type of the variable, should be RType, or iceType. */
	private final static String TYPEATTRIBUTE = "type";
	
	/** Is the variable optional. */
	private final static String OPTIONALATTRIBUTE = "optional";
	
	/** the name of the variable. */
	private final static String NAMEATTRIBUTE = "name";

	/** A constant to represent an empty string in the attributes .*/
	private final static String EMPTYSTRING = "";
	
	/** The rawFileStore service.*/
    protected transient RawFileStore rawFileStore;
    

    /** injector for usage by the container. Not for general use */
    public void setRawFileStore(RawFileStore fileStore) {
        getBeanHelper().throwIfAlreadySet(this.rawFileStore, fileStore);
        this.rawFileStore = fileStore;
    }
    
    
    /*
     * Developer notes: --------------- This method provides the lookup value
     * needed for finding services within the Spring context and, by convention,
     * the value which is to be returned can be found in the file
     * "ome/services/service-<class name>.xml"
     */
    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IScript.class;
    }

    // ~ Service methods
    // =========================================================================
  
    /**
     * Get Scripts will return all the scripts by name available on the server.
     * @return see above.
     */
	@RolesAllowed("user")
    public List<String> getScripts() 
	{
		ArrayList<String> scriptList = new ArrayList<String>();
		long fmt = getFormat(PYTHONSCRIPT).getId();
		String queryString = "from OriginalFile as o where o.format.value = "+fmt;
		List<OriginalFile> fileList = iQuery.findAllByQuery(queryString, null);
		for(OriginalFile file : fileList)
			scriptList.add(file.getName());
		return scriptList;
	}
	
	/**
	 * Get the id of the script with name scriptName.
	 * @param scriptName Name of the script to find id for.
	 * @return The id of the script, -1 if no script found, or more than 
	 * one script with that name.
	 */
	@RolesAllowed("user")
    public long getScriptID(String scriptName)
	{
		OriginalFile file = getOriginalFile(scriptName);
		if(file==null)
			return -1;
		else
			return file.getId();
	}
	
	/**
	 * Upload script to the server. The script must also have a list of it's
	 * parameters and return types as JavaDoc in the header of the script. 
	 * @param script String containing the script
	 * @return id of the script.
	 */
	@RolesAllowed("system")
    public long uploadScript(String script)
	{
		if(!validateScript(script))
			throw new ApiUsageException("Invalid script");
		String paramString = extractXML(script);
		ScriptParams params = getScriptParams(paramString);
		OriginalFile scriptFile = new OriginalFile();
		scriptFile.setName(params.getScriptName());
		scriptFile.setPath(params.getScriptName());
		scriptFile.setFormat(getFormat(PYTHONSCRIPT));
		scriptFile.setSize((long)script.getBytes().length);
		scriptFile.setSha1("FIXME"); //FIXME
		OriginalFile object = iUpdate.saveAndReturnObject(scriptFile);
		rawFileStore.setFileId(object.getId());
		rawFileStore.write(script.getBytes(), 0, script.getBytes().length);

		/*XmlAnnotation xmlAnnotation = new XmlAnnotation();
		xmlAnnotation.setName("scriptParams");
		xmlAnnotation.setTextValue(extractXML(script));
		ILink link = scriptFile.linkAnnotation(xmlAnnotation);
		link = iUpdate.saveAndReturnObject(link);*/
		return scriptFile.getId();
	}
	
	/**
	 * Return the script with the name to the user.
	 * @param name see above.
	 * @return see above.
	 */
    @RolesAllowed("user")
	public String getScript(String name) 
	{
		OriginalFile file = getOriginalFile(name);
		if(file==null)
			return null;
		rawFileStore.setFileId(file.getId());
		long size = file.getSize();
		if(size>Integer.MAX_VALUE || size<0)
			throw new ome.conditions.ValidationException("Script size : "+ 
				size + " invalid on Blitz.OMERO server.");
		String script = new String(rawFileStore.read(0L,(int)size));
		
		return script;
	}
	
    /**
     * Get the Parameters of the script.
     * @param script see above.
     * @return see above.
     */
    @RolesAllowed("user")
	public Map getParams(String script)
	{
		return null;
	}
	
    /**
     * Run the script. Does not work :)
     * @param script
     * @param parameters map of the parameters name, value.
     * @return The results. 
     */
    @RolesAllowed("user")
	public Map runScript(String script, Map parameters)
	{
		return null;
	}

    /**
     * Method to get the original file of the script with name
     * @param name See above.
     * @return original file or null if script does not exist or more than
     * one script with name exists.
     */
    private OriginalFile getOriginalFile(String name)
    {
    	String queryString = "from OriginalFile as o where o.format.value = " +
		 getFormat(PYTHONSCRIPT).getId() + " and o.name = '" + name +"'";
		List<OriginalFile> fileList = iQuery.findAllByQuery(queryString, null);
		if(fileList.size()!=1)
			return null;
		else
			return fileList.get(0);
    }
    
    /**
     * Get the iFormat object. 
     * @return see above.
     */
    Format getFormat(String fmt)
    {
    	List<Format> formatList = 
    		iQuery.findAllByQuery("from Format as f where f.value='"+PYTHONSCRIPT+"'", null);
    	return formatList.get(0);
    }
    
    /**
     * Validate the script, checking that the params are specified correctly 
     * and that the script does not contain any invalid commands. 
     * @param script
     * @return true if script valid.
     */
    private boolean validateScript(String script)
    {
    	return true;
    }
    
    /**
     * Get the parameters from the script. 
     * @param xmlHeader The xml extracted from the script containing the 
     * 					param information.
     * @return A map of the parameters from the script. 
     */
    private ScriptParams getScriptParams(String xmlHeader)
    {
    	ScriptParams scriptParams = new ScriptParams();
    	scriptParams = parseScriptTag(xmlHeader);
    	return scriptParams;
    }

    /**
     * Parse the scripts tag in the script and build the scriptParams object which 
     * will contain name, descrption, param and return maps. 
     * @param script see above.
     * @param params see above.
     * @return script params object.
	 * @throws ApiUsageException 
     */
	private ScriptParams parseScriptTag(String script) 
		throws ApiUsageException
    {
		ScriptParams params=new ScriptParams();
    	IXMLElement scriptElement;
		IXMLParser parser;
		IXMLReader reader;
		ByteArrayInputStream bin;
		try
		{
			parser=XMLParserFactory.createDefaultXMLParser();
		}
		catch (Exception ex)
		{
			ApiUsageException e=
					new ApiUsageException(
						"Unable to instantiate NanoXML Parser");
			throw e;
		}
		try
		{
			bin=
					new ByteArrayInputStream(script.getBytes());
		}
		catch (Exception ex)
		{
			ApiUsageException e=
					new ApiUsageException(
						"ByteArrayInputStream bin=new ByteArrayInputStream(script.getBytes());");
			throw e;
		}
		try
		{
			reader=new StdXMLReader(bin);
		}
		catch (Exception ex)
		{
			ApiUsageException e=
					new ApiUsageException(
						"	reader=new StdXMLReader(bin);");
			throw e;
		}
		try
		{
			parser.setReader(reader);
			scriptElement=(IXMLElement) parser.parse();
		}
		catch (Exception ex)
		{
			ApiUsageException e=new ApiUsageException("scriptElement=(IXMLElement) parser.parse()"+script);
			throw e;
		}
		
		IXMLElement scriptName=scriptElement.getFirstChildNamed(NAMETAG);
		if (scriptName==null) throw new ApiUsageException(
			"No name supplied in the <script> tag");
		IXMLElement scriptDescription=
				scriptElement.getFirstChildNamed(DESCRIPTIONTAG);
		if (scriptDescription==null) throw new ApiUsageException(
			"No name supplied in the <script> tag");
		params.setScriptName(scriptName.getContent());
		params.setScriptDescription(scriptDescription.getContent());
		IXMLElement parameterElement=
				scriptElement.getFirstChildNamed(PARAMETERTAG);
		IXMLElement returnElement=scriptElement.getFirstChildNamed(RETURNTAG);
		if (parameterElement==null) throw new ApiUsageException(
			"No Parameter tags.");
		if (returnElement==null) throw new ApiUsageException("No Return tags.");
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
	private Map<String, String> buildVariableMap(
			IXMLElement variableElements) throws ApiUsageException
	{
		HashMap<String, String> variableMap=new HashMap<String, String>();
		Vector<IXMLElement> elements=
				variableElements.getChildrenNamed(VARIABLETAG);
		for (IXMLElement parameter : elements)
		{
			String name=parameter.getAttribute(NAMEATTRIBUTE, EMPTYSTRING);
			if (name.equals(EMPTYSTRING)) throw new ApiUsageException(
				"No name attribute specified in variable");
			
			String type=parameter.getAttribute(TYPEATTRIBUTE, EMPTYSTRING);
			if (type.equals(EMPTYSTRING)) throw new ApiUsageException(
				"No type attribute specified in variable");
			String optional=
					parameter.getAttribute(OPTIONALATTRIBUTE, EMPTYSTRING);
			String description;
			IXMLElement descriptionElement=
					parameter.getFirstChildNamed(DESCRIPTIONTAG);
			if (descriptionElement!=null) description=
					descriptionElement.getContent();
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
	private String extractXML(String str) throws ApiUsageException
	{
		int start=str.indexOf(SCRIPTSTART);
		int end=str.indexOf(SCRIPTEND)+SCRIPTEND.length();
		if (end==-1||start==-1||end < start)
    		throw new ApiUsageException("Script does not " +
    				"contain valid XML specification.");
    	String xmlStrWithComments = str.substring(start, end);
    	String xmlStr = xmlStrWithComments.replace("#"," ");
    	return xmlStr;
    }
    
}
