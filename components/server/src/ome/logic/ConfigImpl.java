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

import java.util.Date;

import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import ome.annotations.PermitAll;
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.annotations.RolesAllowed;
import ome.api.IConfig;
import ome.api.ServiceInterface;
import ome.system.Version;

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
 * fields should be present unless *very* carefully synchronized.
 */
public class ConfigImpl extends AbstractLevel2Service implements IConfig {

    /*
     * Stateful differences: -------------------- A stateful service must be
     * marked as Serializable and all fields must be either marked transient, be
     * serializable themselves, or be set to null before serialization. Here
     * we've marked the jdbc field as transient out of habit.
     *
     * @see https://trac.openmicroscopy.org.uk/omero/ticket/173
     */
    private transient SimpleJdbcTemplate jdbc;

    /**
     * {@link SimpleJdbcTemplate} setter for dependency injection.
     *
     * @param jdbcTemplate
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
    public final void setJdbcTemplate(SimpleJdbcTemplate jdbcTemplate) {
        getBeanHelper().throwIfAlreadySet(jdbc, jdbcTemplate);
        this.jdbc = jdbcTemplate;
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
     * Purpose: as opposed to RolesAllowed (below), permits this method to 
     * be called by anyone, regardless of their group membership. As of the
     * move to OmeroBlitz, the user will still have to have created a session
     * to gain access (unlike under JavaEE).
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
        Date date = jdbc.queryForObject("select now()", Date.class);
        return date;
    }

    /*
     * Source: ome.annotations package 
     *
     * Purpose: defines the role which must have been obtained during
     * authentication and authorization in order to access this
     * method. This works in combination with the BasicMethodSecurity
     * to fully define security semantics.
     */
    /**
     * see {@link IConfig#getConfigValue(String)}
     */
    @RolesAllowed("system")
    // see above
    public String getConfigValue(String key) {
        return null;
    }

    /**
     * see {@link IConfig#setConfigValue(String, String)}
     */
    @RolesAllowed("system")
    // see above
    public void setConfigValue(String key, String value) {
        return;
    }

    /**
     * see {@link IConfig#getVersion()}
     */
    @PermitAll
    // see above
    public String getVersion() {
        return Version.OMERO;
    }

}
