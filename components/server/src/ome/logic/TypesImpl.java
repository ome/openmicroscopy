/*
 * ome.logic.TypesImpl
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.logic;

// Java imports
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ome.api.ITypes;
import ome.api.ServiceInterface;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.Permissions;
import ome.security.SecureAction;
import ome.services.util.OmeroAroundInvoke;

import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * implementation of the ITypes service interface.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional
@Stateless
@Remote(ITypes.class)
@RemoteBinding(jndiBinding = "omero/remote/ome.api.ITypes")
@Local(ITypes.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.ITypes")
@SecurityDomain("OmeroSecurity")
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
public class TypesImpl extends AbstractLevel2Service implements ITypes {

    public final Class<? extends ServiceInterface> getServiceInterface() {
        return ITypes.class;
    }

    // ~ Service methods
    // =========================================================================

    @RolesAllowed("user")
    public <T extends IEnum> T createEnumeration(T newEnum) {
        final LocalUpdate up = iUpdate;

        // TODO should this belong to root?
        Details d = getSecuritySystem().newTransientDetails(newEnum);
        newEnum.setDetails(d);
        return getSecuritySystem().doAction(newEnum, new SecureAction() {
            public IObject updateObject(IObject iObject) {
                return up.saveAndReturnObject(iObject);
            }
        });
    }
    
    @RolesAllowed("system")
    public <T extends IEnum> T updateEnumeration(T oEnum) {
        return iUpdate.saveAndReturnObject(oEnum);
    }
    
    @RolesAllowed("system")
    public <T extends IEnum> void updateEnumerations(List<T> listEnum) {
    	// should be changed to saveAndReturnCollection(Collection graph)
    	// when method is implemented
    	
    	Collection<IObject> colEnum = new ArrayList<IObject>();
    	for (Object o : listEnum) {
            IObject obj = (IObject) o;
            colEnum.add(obj);
        }
    	iUpdate.saveCollection(colEnum);
    }
    
    @RolesAllowed("system")
    public <T extends IEnum> void deleteEnumeration(T oEnum) {
        iUpdate.deleteObject((IObject) oEnum);
    }

    @RolesAllowed("user")
    public <T extends IEnum> List<T> allEnumerations(Class<T> k) {
        return iQuery.findAll(k, null);
    }

    @RolesAllowed("user")
    public <T extends IEnum> T getEnumeration(Class<T> k, String string) {
        IEnum e = iQuery.findByString(k, "value", string);
        iQuery.initialize(e);
        if (e == null) {
            throw new ApiUsageException(String.format(
                    "An %s enum does not exist with the value: %s",
                    k.getName(), string));
        }
        return k.cast(e);
    }
    
    @RolesAllowed("system")
    private List<String> getEnumerationSources(URLClassLoader sysloader) throws IOException {
    	List<String> list = new ArrayList<String>();
    	JarFile jarFile = null;

    	for(int i=0; i<sysloader.getURLs().length; i++){
    		if(sysloader.getURLs()[i].getPath().contains("common.jar")) { 
    			jarFile = new JarFile(sysloader.getURLs()[i].getPath());
    			break;
    		}
    	}
    	Enumeration en = jarFile.entries();
 		
 		while (en.hasMoreElements()) {
 			JarEntry entry = (JarEntry) en.nextElement();
 			if(entry.getName().contains(".ome.xml")) 
 				list.add(entry.getName());
 		}
	    return list;

    }
    
    @RolesAllowed("system")
    public <T extends IEnum> List<Class<? extends IEnum>> getAllEnumerationTypes()  {
    	
    	List<Class<? extends IEnum>> list = new ArrayList<Class<? extends IEnum>>();
    	URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	    	
		List<String> en = null;
		try {
			// gets list of files *.ome.xml from common.jar
			en = getEnumerationSources(sysloader);

			for(String e:en){
	 			InputStream in = sysloader.getResourceAsStream(e);
	 			list.addAll(parseTypes(in));
	 			in.close();
	 		}
		} catch (IOException e) {
			throw new RuntimeException("IOException: "+e.getMessage());
		}

    	return list;
    }
    
    @RolesAllowed("system")
    public <T extends IEnum> List<T> allOryginalEnumerations(Class<T> klass) {
    	List<T> list = new ArrayList<T>();
    	
    	URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    	List<String> en = null;
    	try {
			// gets list of files *.ome.xml from common.jar
			en = getEnumerationSources(sysloader);

			for(String e:en){
	 			InputStream in = sysloader.getResourceAsStream(e);
	 			list = parseValues(klass, in);
	 			in.close();
	 			if(list.size()>0) break;
	 		}
		} catch (IOException e) {
			throw new RuntimeException("IOException: "+e.getMessage());
		}
		
    	return list;
    }
    
    private <T extends IEnum> List<Class<T>> parseTypes(InputStream filename) {
    	List<Class<T>> list = new ArrayList<Class<T>>();
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filename);
			// normalize text representation
			doc.getDocumentElement().normalize();

			NodeList listOfEnums = doc.getElementsByTagName("enum");

			for (int s = 0; s < listOfEnums.getLength(); s++) {

				Node firstEnumNode = listOfEnums.item(s);
				String enumType = firstEnumNode.getAttributes().getNamedItem("id")
						.toString().split("\"")[1]; 
				Class enumClass = Class.forName(enumType);
				list.add(enumClass);
			}

		} catch (SAXParseException err) {
			throw new RuntimeException("SAXParseException: line " +err.getLineNumber()
					+ ", uri " + err.getSystemId() + " " + err.getMessage());
		} catch (SAXException e) {
			Exception x = e.getException();
			throw new RuntimeException("SAXException: line " 
					+ ((x == null) ? e : x).getMessage());
		} catch (Throwable t) {
			throw new RuntimeException(t.getMessage());
		}
		return list;

	}
      
	private <T extends IEnum> List<T> parseValues(Class<T> k,
			InputStream filename) {
		List<T> list = new ArrayList<T>();
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(filename);
			// normalize text representation
			doc.getDocumentElement().normalize();

			NodeList listOfEnums = doc.getElementsByTagName("enum");

			for (int s = 0; s < listOfEnums.getLength(); s++) {

				Node firstEnumNode = listOfEnums.item(s);
				String enumType = firstEnumNode.getAttributes().getNamedItem(
						"id").toString().split("\"")[1];
				
				if (enumType.equals(k.getName())
						&& firstEnumNode.getNodeType() == Node.ELEMENT_NODE) {

					Element firstPersonElement = (Element) firstEnumNode;
					NodeList firstNameList = firstPersonElement
							.getElementsByTagName("entry");
					for (int i = 0; i < firstNameList.getLength(); i++) {
						Element firstNameElement = (Element) firstNameList
								.item(i);
						list.add(getEnumeration(k, firstNameElement
								.getAttribute("name")));
					}
					break;
				}
			}

		} catch (SAXParseException err) {
			throw new RuntimeException("SAXParseException: line " +err.getLineNumber()
					+ ", uri " + err.getSystemId() + " " + err.getMessage());
		} catch (SAXException e) {
			Exception x = e.getException();
			throw new RuntimeException("SAXException: line " 
					+ ((x == null) ? e : x).getMessage());
		} catch (Throwable t) {
			throw new RuntimeException(t.getMessage());
		}

		return list;

	}
    
    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getResultTypes() {
        // TODO Auto-generated method stub
        return null;

    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getAnnotationTypes() {
        // TODO Auto-generated method stub
        return null;

    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getContainerTypes() {
        // TODO Auto-generated method stub
        return null;

    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getPojoTypes() {
        // TODO Auto-generated method stub
        return null;

    }

    @RolesAllowed("user")
    public <T extends IObject> List<Class<T>> getImportTypes() {
        // TODO Auto-generated method stub
        return null;

    }

    @RolesAllowed("user")
    public <T extends IObject> Permissions permissions(Class<T> k) {
        // TODO Auto-generated method stub
        return null;

    }

}
