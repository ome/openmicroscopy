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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

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

import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;
import org.jboss.annotation.ejb.RemoteBindings;
import org.jboss.annotation.security.SecurityDomain;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

/**
 * implementation of the ITypes service interface.
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date:
 *          2008-01-04 14:17:02 +0000 (Fri, 04 Jan 2008) $) </small>
 * @since OMERO 3.0
 */
@TransactionManagement(TransactionManagementType.BEAN)
@Transactional
@Stateless
@Remote(ITypes.class)
@RemoteBindings( {
        @RemoteBinding(jndiBinding = "omero/remote/ome.api.ITypes"),
        @RemoteBinding(jndiBinding = "omero/secure/ome.api.ITypes", clientBindUrl = "sslsocket://0.0.0.0:3843") })
@Local(ITypes.class)
@LocalBinding(jndiBinding = "omero/local/ome.api.ITypes")
@SecurityDomain("OmeroSecurity")
@Interceptors( { OmeroAroundInvoke.class, SimpleLifecycle.class })
public class TypesImpl extends AbstractLevel2Service implements ITypes {

    protected transient SessionFactory sf;

    /** injector for usage by the container. Not for general use */
    public final void setSessionFactory(SessionFactory sessions) {
        getBeanHelper().throwIfAlreadySet(this.sf, sessions);
        sf = sessions;
    }

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
        newEnum.getDetails().copy(d);
        return getSecuritySystem().doAction(new SecureAction() {
            public IObject updateObject(IObject... iObjects) {
                return up.saveAndReturnObject(iObjects[0]);
            }
        }, newEnum);
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
        iUpdate.deleteObject(oEnum);
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
    public <T extends IEnum> List<Class<T>> getEnumerationTypes() {

        List<Class<T>> list = new ArrayList<Class<T>>();

        Map<String, ClassMetadata> m = sf.getAllClassMetadata();
        for (String key : m.keySet()) {
            try {
                Class klass = Class.forName(m.get(key).getEntityName());
                boolean r = IEnum.class.isAssignableFrom(klass);
                if (r) {
                    list.add(klass);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class not found. Exception: "
                        + e.getMessage());
            }
        }
        return list;
    }

    @RolesAllowed("system")
    public <T extends IEnum> Map<Class<T>, List<T>> getEnumerationsWithEntries() {
        Map<Class<T>, List<T>> map = new HashMap<Class<T>, List<T>>();
        for (Class klass : getEnumerationTypes()) {
            List<T> entryList = allEnumerations(klass);
            map.put(klass, entryList);
        }
        return map;
    }

    @RolesAllowed("system")
    public <T extends IEnum> List<T> getOryginalEnumerations() {
        List<IEnum> orygin = new ArrayList<IEnum>();
        InputStream in = null;
        try {
            URL file = ResourceUtils.getURL("classpath:enums.properties");
            URL jar = ResourceUtils.extractJarFileURL(file);
            JarFile jarFile = new JarFile(jar.getPath());
            JarEntry entry = jarFile.getJarEntry("enums.properties");
            in = jarFile.getInputStream(entry);
            Properties property = new Properties();
            property.load(in);
            Set keys = property.keySet();
            for (Iterator it = keys.iterator(); it.hasNext();) {
                String key = (String) it.next();
                String[] keySplit = key.split("\\.");
                String className = "";
                for (int i = 0; i < keySplit.length - 1; i++)
                    className = className + keySplit[i] + ".";
                className = className.substring(0, (className.length() - 1));
                String val = property.getProperty(key);
                Class klass = Class.forName(className);
                IEnum orygninEntry = (IEnum) klass.getConstructor(String.class)
                        .newInstance(val);
                orygin.add(orygninEntry);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found. " + e.getMessage());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found. " + e.getMessage());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL. " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("IO exception. " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Illegal argument. " + e.getMessage());
        } catch (SecurityException e) {
            throw new RuntimeException("Security exception. " + e.getMessage());
        } catch (InstantiationException e) {
            throw new RuntimeException("Instantiation exception. "
                    + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access. " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Invocation Target. " + e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No such method. " + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
        return (List<T>) orygin;
    }

    @RolesAllowed("system")
    public <T extends IEnum> void resetEnumerations(Class<T> klass) {
        InputStream in = null;
        try {
            URL file = ResourceUtils.getURL("classpath:enums.properties");
            URL jar = ResourceUtils.extractJarFileURL(file);
            JarFile jarFile = new JarFile(jar.getPath());
            JarEntry entry = jarFile.getJarEntry("enums.properties");
            in = jarFile.getInputStream(entry);
            Properties property = new Properties();
            property.load(in);
            Properties newProp = new Properties();
            Set keys = property.keySet();
            for (Iterator it = keys.iterator(); it.hasNext();) {
                String key = (String) it.next();
                if (key.contains(klass.getName())) {
                    newProp.setProperty(key, property.getProperty(key));
                }
            }
            property.clear();

            List<IEnum> listOnDB = (List<IEnum>) allEnumerations(klass);
            List<IEnum> listToDel = new ArrayList<IEnum>();
            List<IEnum> newList = new ArrayList<IEnum>();

            for (Long i = 1L; i < newProp.size() + 1; i++) {

                String val = newProp.getProperty(klass.getName() + "."
                        + i.toString());
                if (listOnDB.size() >= newProp.size()) {

                    boolean flag = false;
                    for (IEnum oldOb : listOnDB) {
                        if (val.equals(oldOb.getValue())) {
                            if (i.intValue() == oldOb.getId().intValue()) {
                                newList.add(oldOb);
                                listToDel.add(oldOb);
                                flag = true;
                            }
                        }
                    }

                    if (!flag) {
                        IEnum newEntry = (IEnum) klass.getConstructor(
                                String.class).newInstance(val);
                        newList.add(i.intValue() - 1, newEntry);
                    }

                } else {
                    IEnum newEntry = (IEnum) klass.getConstructor(String.class)
                            .newInstance(val);
                    newList.add(i.intValue() - 1, newEntry);
                }
            }

            listOnDB.removeAll(listToDel);
            for (IEnum en : listOnDB)
                deleteEnumeration(en);

            if (newList.size() > 0)
                updateEnumerations(newList);

        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found. " + e.getMessage());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL. " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("IO exception. " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Illegal argument. " + e.getMessage());
        } catch (SecurityException e) {
            throw new RuntimeException("Security exception. " + e.getMessage());
        } catch (InstantiationException e) {
            throw new RuntimeException("Instantiation exception. "
                    + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal access. " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Invocation Target. " + e.getMessage());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No such method. " + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }
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
