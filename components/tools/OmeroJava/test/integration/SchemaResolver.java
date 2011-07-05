/*
 * $Id$
 *
 *   Copyright 2006-2011 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package integration;

import java.io.InputStream;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

/** 
 * A resolver for Schema locations that pulls them from jar resources.
 *
 * @author Andrew Patterson &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:ajpatterson@lifesci.dundee.ac.uk">ajpatterson@lifesci.dundee.ac.uk</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class SchemaResolver implements LSResourceResolver 
{
    private DOMImplementationLS theDOMImplementationLS;

    // the static string to strip when mapping schema locations
    private static String GIT_MASTER_PATH  = "http://git.openmicroscopy.org/src/master/components/specification/Released-Schema";
    private static String GIT_DEVELOP_PATH = "http://git.openmicroscopy.org/src/develop/components/specification/Released-Schema";

    public SchemaResolver() throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        // Create the objects necessary to make the correct LSInput return types
        System.setProperty(
            DOMImplementationRegistry.PROPERTY, 
            "org.apache.xerces.dom.DOMImplementationSourceImpl");
        DOMImplementationRegistry theDOMImplementationRegistry = 
            DOMImplementationRegistry.newInstance();
        theDOMImplementationLS = 
            (DOMImplementationLS) theDOMImplementationRegistry.getDOMImplementation("LS");
    }


    /**
     * Resolves known namespace locations to their appropriate jar resource 
     * 
     * @param type Not used by function.
     * @param namespaceURI Not used by function.
     * @param publicId Not used by function.
     * @param systemId The schema location that will be used to choose the resource to return.
     * @param baseURI Not used by function.
     * @return The requested resource.
     */
     public LSInput  resolveResource(
        String type, String namespaceURI, String publicId, 
        String systemId, String baseURI) 
    {
        LSInput theResult = null;
        
        // Match the requested schema locations and create the appropriate LSInput object
        if (systemId.equals("http://www.w3.org/2001/xml.xsd")) 
        {
            theResult = makeSubstutionStream("/additions/jar/xml.xsd", systemId);
        } 
        else if (systemId.startsWith(GIT_MASTER_PATH)) 
        {
            theResult = makeSubstutionStream(systemId.substring(GIT_MASTER_PATH.length()), systemId);
        } 
        else if (systemId.startsWith(GIT_DEVELOP_PATH)) 
        {
            theResult = makeSubstutionStream(systemId.substring(GIT_DEVELOP_PATH.length()), systemId);
        } 
        else
        {
            throw new RuntimeException("SchemaResolver does not know path to resolve: [" + systemId + "] from OME specification jar.");
        }
        
        return theResult;
    }
    
    /**
     * Creates the LSInput object from the resource path 
     * 
     * @param theResourcePath Path to the schema in the Specification jar.
     * @param systemId 
     * @return The requested LSInput object.
     */
    private LSInput makeSubstutionStream(
        String theResourcePath, String systemId)
    {
        LSInput theResult = null;
        theResult = theDOMImplementationLS.createLSInput();
        InputStream theResourcesStream = getClass().getResourceAsStream(theResourcePath);
        theResult.setByteStream(theResourcesStream);
        theResult.setSystemId(systemId);
        return theResult;
    }
}
