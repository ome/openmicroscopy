/*
 *   $Id$
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

import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ome.annotations.PermitAll;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.annotations.RolesAllowed;
import ome.api.IConfig;
import ome.api.ServiceInterface;
import ome.api.local.LocalConfig;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.OptimisticLockException;
import ome.conditions.SecurityViolation;
import ome.security.basic.CurrentDetails;
import ome.services.db.DatabaseIdentity;
import ome.system.PreferenceContext;
import ome.util.SqlAction;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

/**
 * implementation of the IConfig service interface.
 * 
 * Also used as the main developer example for developing (stateless) ome.logic
 * implementations. See source code documentation for more.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @since 3.0-M3
 * @see IConfig
 */

/*
 * Developer notes: --------------- The two annotations below are activated by
 * setting the subversion properties on this class file. They can be accessed
 * via ome.system.Version
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
/*
 * Developer notes: --------------- The annotations below (and on the individual
 * methods) are central to the definition of this service. They are used in
 * place of XML configuration files (though the XML deployment descriptors, as
 * they are called, can be used to override the annotations), and will influence
 */
// ~ Service annotations
// =============================================================================
/*
 * Source: Spring Purpose: Used by EventHandler#checkReadyOnly(MethodInvocation)
 * to deteremine if a method is read-only. No annotation implies ready-only, so
 * it is essential to have this annotation on all write methods.
 */
@Transactional
/*
 * Stateless. This class implements ServiceInterface but not
 * StatefulServiceInterface making it stateless. This means that the entire
 * server will most likely only contain one of these instances. No mutable
 * fields should be present unlessvery carefully synchronized.
 * 
 * Local configurations are not exposed to clients, and are typically only used
 * within a server instance.
 */
public class ConfigImpl extends AbstractLevel2Service implements LocalConfig {

    /*
     * Stateful differences: -------------------- A stateful service must be
     * marked as Serializable and all fields must be either marked transient, be
     * serializable themselves, or be set to null before serialization. Here
     * we've marked the jdbc field as transient out of habit.
     * 
     * @see http://trac.openmicroscopy.org.uk/ome/ticket/173
     */
    private transient SqlAction sql;

    private transient PreferenceContext prefs;

    private transient CurrentDetails currentDetails;

    private transient DatabaseIdentity db;

    /**
     * Protects all access to the configuration properties.
     */
    private final transient ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * {@link SqlAction} setter for dependency injection.
     * 
     * @param sql
     * @see ome.services.util.BeanHelper#throwIfAlreadySet(Object, Object)
     */
    /*
     * Developer notes: --------------- Because of the complicated lifecycle of
     * EJBs it is not possible to fully configure them with constructor
     * injection (which is safer). Instead, we have to provide public setters
     * for all properties which need to be injected. And since Java doesn't have
     * the concept of "friends" (yet), this opens up our classes for some weird
     * manipulations. Therefore we've made all bean setters "final" and added a
     * call to "throwIfAlreadySet" which will only allow previously null fields
     * to be set.
     */
    public final void setSqlAction(SqlAction sql) {
        getBeanHelper().throwIfAlreadySet(this.sql, sql);
        this.sql = sql;
    }

    /**
     * {@link PreferenceContext} setter for dependency injection.
     * 
     * @param prefs
     * @see ome.services.util.BeanHelper#throwIfAlreadySet(Object, Object)
     */
    public final void setPreferenceContext(PreferenceContext prefs) {
        getBeanHelper().throwIfAlreadySet(this.prefs, prefs);
        this.prefs = prefs;
    }

    /**
     * {@link PreferenceContext} setter for dependency injection.
     * 
     * @param prefs
     * @see ome.services.util.BeanHelper#throwIfAlreadySet(Object, Object)
     */
    public final void setCurrentDetails(CurrentDetails currentDetails) {
        getBeanHelper().throwIfAlreadySet(this.currentDetails, currentDetails);
        this.currentDetails = currentDetails;
    }

    public final void setDatabaseIdentity(DatabaseIdentity db) {
        this.db = db;
    }

    /*
     * Developer notes: --------------- This method provides the lookup value
     * needed for finding services within the Spring context and, by convention,
     * the value which is to be returned can be found in the file
     * "ome/services/service-<class name>.xml"
     */
    public final Class<? extends ServiceInterface> getServiceInterface() {
        return IConfig.class;
    }

    // ~ Service methods
    // =========================================================================

    /*
     * Source: ome.annotations package
     * 
     * Purpose: as opposed to RolesAllowed (below), permits this method to be
     * called by anyone, regardless of their group membership. As of the move to
     * OmeroBlitz, the user will still have to have created a session to gain
     * access (unlike under JavaEE).
     */
    /**
     * see {@link IConfig#getServerTime()}
     */
    @PermitAll
    public Date getServerTime() {
        return new Date();
    }

    /**
     * see {@link IConfig#getDatabaseTime()}
     */
    @PermitAll
    // see above
    public Date getDatabaseTime() {
        Date date = sql.now();
        return date;
    }

    /*
     * Source: ome.annotations package
     * 
     * Purpose: defines the role which must have been obtained during
     * authentication and authorization in order to access this method. This
     * works in combination with the BasicMethodSecurity to fully define
     * security semantics.
     */
    /**
     * see {@link IConfig#getConfigValue(String)}
     */
    @PermitAll
    // see above
    public String getConfigValue(String key) {

        if (key == null) {
            return "";
        }

        key = prefs.resolveAlias(key);

        if (!prefs.canRead(currentDetails.getCurrentEventContext(), key)) {
            throw new SecurityViolation("Cannot read configuration: " + key);
        }

        return getInternalValue(key);
    }

    @PermitAll
    public Map<String, String> getConfigValues(String keyRegex) {
        if (keyRegex == null) {
            return Collections.emptyMap();
        }

        Pattern p = Pattern.compile(keyRegex);
        Map<String, String> rv = new HashMap<String, String>();
        Set<String> keys = prefs.getKeySet();
        // Not resolving aliases since these come straight-from the prefs
        for (String key : keys) {
            if (p.matcher(key).find()) {
                if (prefs.canRead(
                        currentDetails.getCurrentEventContext(), key)) {
                    rv.put(key, getInternalValue(key));
                }
            }
        }
        return rv;
    }

    @RolesAllowed("system")
    public Map<String, String> getConfigDefaults() {
        File etc = new File("etc");
        File omero = new File(etc, "omero.properties");
        Properties p = new Properties();
        Map<String, String> rv = new HashMap<String, String>();
        try {
            FileReader r = new FileReader(omero);
            p.load(r);
            for (Entry<Object, Object> entry : p.entrySet()) {
                    rv.put(entry.getKey().toString(),
                            entry.getValue().toString());
            }
            return rv;
        } catch (Exception e) {
            InternalException ie = new InternalException(e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }

    @PermitAll
    public Map<String, String> getClientConfigValues() {
        return getConfigValues("^omero\\.client\\.");
    }

    @PermitAll
    public Map<String, String> getClientConfigDefaults() {
        Map<String, String> rv = getConfigDefaults();
        Map<String, String> copy = new HashMap<String, String>();
        for (Map.Entry<String, String> e : rv.entrySet()) {
            if (e.getKey().startsWith("omero.client")) {
                copy.put(e.getKey(), e.getValue());
            }
        }
        return copy;
    }

    public String getInternalValue(String key) {

        key = prefs.resolveAlias(key);

        lock.readLock().lock();
        try {
            String value = null;
            if (prefs.checkDatabase(key)) {
                value = fromDatabase(key);
            }

            if (value != null) {
                return value;
            } else {
                return prefs.getProperty(key);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * see {@link IConfig#setConfigValue(String, String)}
     */
    @RolesAllowed("system")
    // see above
    public void setConfigValue(String key, String value) {

        key = prefs.resolveAlias(key);

        lock.writeLock().lock();
        try {
            
            boolean set = false;

            // If the value comes from the db, then set it there and return
            if (prefs.checkDatabase(key)) {
                String current = fromDatabase(key);
                if (current != null && current.length() > 0) {
                    int count = sql.updateConfiguration(key, value);
                    if (count != 1) {
                        throw new OptimisticLockException(
                                "Configuration tabled during modification of : "
                                        + key);
                    }
                    set = true;
                }
            }

            // Otherwise set it in the preferences.
            if (!set) {
                // With #800, we will need to raise some form of event here
                // to notify consumers. Also, this will be a temporary value
                // not saved on restart nor shared across JVMs.
                System.setProperty(key, value);
            }

        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * see {@link IConfig#setConfigValueIfEquals(String, String, String)}
     */
    @RolesAllowed("user")
    // see above
    public boolean setConfigValueIfEquals(String key, String value, String test)
            throws ApiUsageException, SecurityViolation {

        key = prefs.resolveAlias(key);

        lock.writeLock().lock();
        try {
            String current = getInternalValue(key);
            if (test == null) {
                if (current != null) {
                    return false;
                }
            } else {
                if (!test.equals(current)) {
                    return false;
                }
            }
            setConfigValue(key, value);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * see {@link IConfig#getVersion()}
     */
    @PermitAll
    // see above
    public String getVersion() {
        String version = getInternalValue("omero.version");
        Pattern p = Pattern.compile(VERSION_REGEX);
        Matcher m = p.matcher(version);
        if (!m.matches()) {
            throw new InternalException("Bad version format:" + version);
        }
        return m.group(1);
    }
    
    @PermitAll
    // see above
    public String getDatabaseVersion() {
        return sql.dbVersion();    }

    @PermitAll
    // see above
    public String getDatabaseUuid() {
        return db.getUuid();
    }

    // Helpers
    // =========================================================================

    private String fromDatabase(String key) {
        String value = null;
        try {
            value = sql.configValue(key);
        } catch (EmptyResultDataAccessException erdae) {
            // ok returning null
        }
        return value;
    }

}
