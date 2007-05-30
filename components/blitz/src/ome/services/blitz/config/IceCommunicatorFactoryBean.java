/*   $Id: Server.java 1201 2007-01-18 21:54:35Z jmoore $
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import omero.util.ObjectFactoryRegistrar;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.util.ResourceUtils;

import Ice.Util;

/**
 * FactoryBean that creates an ICE {@lik Ice.Communicator} instance (or a
 * decorator that implements that interface).
 * 
 * @author Josh Moore
 * @since 3.0-Beta2
 */
public class IceCommunicatorFactoryBean extends IceLocalObjectFactoryBean {

    public final static String DEFAULT_CONFIG = "classpath:ice.config";

    private final static String CONFIG_KEY = "--Ice.Config=";

    private String[] arguments;

    private String configFile;

    private Properties properties;

    private Ice.Logger iceLogger;

    private Map<String, String> defaultContext;
    
    private String[] adapterNames;
    
    private List<Ice.ObjectAdapter> adapters = new ArrayList<Ice.ObjectAdapter>();
    
    // Currently uncreated injectors
    private Ice.ThreadNotification notification;

    private boolean defaultConfig;

    public void setArgs(String[] args) {
        this.arguments = args;
    }

    public void setConfig(String config) {
        this.configFile = config;
    }

    public void setProperties(Properties p) {
        this.properties = p;
    }

    public void setLogger(Ice.Logger log) {
        this.iceLogger = log;
    }

    public void setDefaultContext(Map context) {
        this.defaultContext = context;
    }

    public void setAdapters(String[] names) {
        this.adapterNames = names;
    }
    
    public void afterPropertiesSet() throws Exception {

        Ice.InitializationData id = new Ice.InitializationData();

        if (this.configFile == null && this.defaultConfig) {
            if (logger.isDebugEnabled()) {
                logger.debug("Using default config file '" + DEFAULT_CONFIG
                        + "'");
            }
            this.configFile = DEFAULT_CONFIG;
        }

        // Doing clean up for possible jar-contained config files.
        if (this.configFile != null) {
            // FIXME need better copy. and perhaps ice can read from stream.
            URL file = ResourceUtils.getURL(this.configFile);
            if (ResourceUtils.isJarURL(file)) {
                URL jar = ResourceUtils.extractJarFileURL(file);
                // http://java.sun.com/developer/TechTips/txtarchive/2003/Jan03_JohnZ.txt
                JarFile jarFile = new JarFile(jar.getPath());
                JarEntry entry = jarFile.getJarEntry("ice.config");// FIXME
                InputStream is = jarFile.getInputStream(entry);

                // FIXME Hack.
                File tmp = File.createTempFile("omero.", ".tmp");
                tmp.deleteOnExit();
                FileOutputStream fos = new FileOutputStream(tmp);
                byte[] buf = new byte[1024];
                int len;
                while ((len = is.read(buf)) > 0) {
                    fos.write(buf, 0, len);
                }
                is.close();
                fos.close();

                this.configFile = tmp.getAbsolutePath();
            } else {
                this.configFile = file.getPath();
            }

            if (logger.isInfoEnabled()) {
                logger.info("Reading config file:" + this.configFile);
            }

        }

        if (this.arguments == null) {
            id.properties = Util.createProperties(new String[] {});
        } else {
            for (int i = 0; i < arguments.length; i++) {
                String s = arguments[i];
                if (s != null && s.startsWith(CONFIG_KEY)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(
                                "Overriding args setting %s with %s", s,
                                configFile));
                    }
                    arguments[i] = CONFIG_KEY + configFile;
                }
            }
            id.properties = Util.createProperties(arguments);
        }

        if (properties != null) {
            for (Object o : properties.keySet()) {
                String key = (String) o;
                String val = (String) properties.getProperty(key);
                String old = id.properties.getProperty(key);
                if (old != null && old.length() > 0 && val != null
                        && val.length() > 0) {
                    if (logger.isWarnEnabled()) {
                        logger.warn(String.format("Overriding property %s. "
                                + "'%s' ==> '%s'", key, old, val));
                    }
                }
                id.properties.setProperty(key, val);
            }
        }

        if (this.iceLogger != null) {
            id.logger = this.iceLogger;
        }

        Ice.Communicator ic = Util.initialize(id);

        // Using implicit context, starting at Ice 3.2.0
        if (this.defaultContext != null) {
            ic.getImplicitContext().setContext( defaultContext );
        }

        ObjectFactoryRegistrar.registerObjectFactory(ic, 
                ObjectFactoryRegistrar.INSTANCE);
        
        if (adapterNames != null) {
            for (String name : adapterNames) {
                IceObjectAdapterFactoryBean oaf = (IceObjectAdapterFactoryBean) context.getBean("&"+name);
                oaf.initialize(ic);
                adapters.add((Ice.ObjectAdapter)oaf.getObject());
            }
        }
        
        obj = ic;
    }

    public Class getObjectType() {
        return (this.obj != null ? this.obj.getClass() : Ice.Communicator.class);
    }

    /**
     * Since Ice.ObjectAdapter has a dependency on Ice.Communicator in Spring,
     * the destruction of Ice.Communicator takes place first, even though that
     * causes a hang.
     * 
     * @see BeanFactoryUtils#beanNamesForTypeIncludingAncestors(ListableBeanFactory,
     *      Class)
     */
    @Override
    public void doDestroy() throws Exception {
        if (this.obj != null) {
            Ice.Communicator ic = (Ice.Communicator) this.obj;
            for (Ice.ObjectAdapter oa : adapters) {
                oa.deactivate();
            }
            ic.destroy();
        }
    }
}
