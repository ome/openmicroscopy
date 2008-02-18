/*
 * ome.connection
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.data;

// Java imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.faces.context.FacesContext;

import ome.admin.controller.LoginBean;
import ome.api.IAdmin;
import ome.api.ILdap;
import ome.api.IQuery;
import ome.api.IRepositoryInfo;
import ome.api.ITypes;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.model.IEnum;
import ome.model.core.Pixels;
import ome.model.enums.PixelsType;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import org.apache.log4j.Logger;

/**
 * ConnectionDB providing access to user/admin-only functionality based server
 * access by {@link ome.system.ServiceFactory} and selected user functions. Most
 * methods require membership in privileged.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class ConnectionDB {

    /**
     * log4j logger
     */
    static Logger logger = Logger.getLogger(ConnectionDB.class.getName());

    /**
     * IAdmin
     */
    private IAdmin adminService;

    /**
     * IAdmin
     */
    private ITypes typesService;

    /**
     * IQuery
     */
    private IQuery queryService;

    /**
     * IRepositoryInfo
     */
    private IRepositoryInfo repService;

    /**
     * IRepositoryInfo
     */
    private ILdap ldapService;

    /**
     * Current {@link ome.model.meta.Experimenter#getId()} as
     * {@link java.lang.String}
     */
    private String userid;

    private FacesContext facesContext = FacesContext.getCurrentInstance();

    /**
     * Creates a new instance of ConnectionDB.
     */
    @SuppressWarnings("deprecation")
    public ConnectionDB() {
        LoginBean lb = (LoginBean) facesContext.getApplication()
                .getVariableResolver().resolveVariable(facesContext,
                        "LoginBean");

        this.userid = lb.getId();

        try {
            adminService = lb.getAdminServices();
            typesService = lb.getTypesServices();
            queryService = lb.getQueryServices();
            repService = lb.getRepServices();
            ldapService = lb.getLdapServices();
        } catch (Exception e) {
            logger.error("ConnectionDB exception: " + e.getMessage());

        }
    }

    // -----------------------------------------------------------------------

    /**
     * ILdap interface
     */

    /**
     * Finds experimenters by one ldap attribute under the base
     * 
     * @param base
     *            String (converted to {@link DistinguishedName})
     * @param attribute
     *            String
     * @param omeName
     *            String
     * @return {@link java.util.List}<{@link ome.model.meta.Experimenter}>.
     */
    public List<Experimenter> findExperimenters(String base, String attribute,
            String omeName) {
        return ldapService.searchByAttribute(base, attribute, omeName);
    }

    /**
     * Finds experimenters by many ldap attributes under the base
     * 
     * @param base
     *            String (converted to {@link DistinguishedName})
     * @param attributes
     *            String
     * @param values
     *            String
     * @return {@link java.util.List}<{@link ome.model.meta.Experimenter}>.
     */
    public List<Experimenter> findExperimentersByAttributes(String base,
            String[] attributes, String[] values) {
        return ldapService.searchByAttributes(base, attributes, values);
    }

    /**
     * Sets Dn for experimenter.
     * 
     * @param id
     *            {@link Experimenter#ID}
     * @param dn
     *            String
     */
    public void setDn(Long id, String dn) {
        ldapService.setDN(id, dn);
    }

    // -----------------------------------------------------------------------

    /**
     * ITypes interface
     */

    /**
     * Gets Enumerations' classes
     * 
     * @return {@link java.util.Map} of enumerations classes with values.
     */
    public Map<Class<IEnum>, List<IEnum>> getEnumerationsWithEntries() {
        logger.info("getEnumerationsWithEntries by user ID: '" + userid + "'");
        return typesService.getEnumerationsWithEntries();
    }
    
    public List<IEnum> getOriginalEnumerations() {
        logger.info("getOriginalEnumerations by user ID: '" + userid + "'");
        return typesService.getOriginalEnumerations();
    }

    /**
     * Gets Enumerations' classes
     * 
     * @return {@link java.lang.String} of enumerations classes with values.
     */
    public List<Class<IEnum>> getEnumerations() {
        logger.info("getEnumerations by user ID: '" + userid + "'");
        List<Class<IEnum>> list = typesService.getEnumerationTypes();
        logger.info("getEnumerations list: " + list);
        return list;
    }

    /**
     * Gets entries for enumeration class
     * 
     * @param klass
     *            Class
     * @return {@link java.util.List} of objects extend IEnum
     */
    public List<? extends IEnum> getEntries(Class klass) {
        logger.info("getEntries by user ID: '" + userid + "'");
        List<? extends IEnum> list = typesService.allEnumerations(klass);
        logger.info("getEntries list: " + list);
        return list;
    }

    /**
     * Create enumeration
     * 
     * @param en
     *            Object of class extends IEnum
     */
    public void createEnumeration(IEnum en) {
        logger.info("createEnumeration by user ID: '" + userid + "'");
        typesService.createEnumeration(en);
        logger.info("createEnumeration [Object: '" + en.getClass()
                + "', value: '" + en.getValue() + "']");
    }

    /**
     * Deletes enumeration
     * 
     * @param en
     *            Object of class extends IEnum
     */
    public void deleteEnumeration(IEnum en) {
        logger.info("deleteEnumeration by user ID: '" + userid + "'");
        typesService.deleteEnumeration(en);
    }

    /**
     * Updates enumerations
     * 
     * @param list
     *            {@link java.util.List} of Objects extends IEnum
     */
    public void updateEnumerations(List<? extends IEnum> list) {
        logger.info("updateEnumerations by user ID: '" + userid + "'");
        for (IEnum o : list) {
            logger.info("updateEnumerations: Entry[id: '" + o.getId()
                    + "', value: '" + o.getValue() + "']");
        }
        typesService.updateEnumerations(list);
    }

    /**
     * Checks that enumeration exists.
     * 
     * @param klass
     *            Class
     * @param value
     *            {@link java.lang.String} checking value
     * @return boolean
     * @throws Exception
     *             when object exists
     */
    public boolean checkEnumeration(Class klass, String value) throws Exception {
        logger.info("checkEnumeration by user ID: '" + userid + "'");
        try {
            typesService.getEnumeration(klass, value);
            logger.error("checkEnumeration: An " + klass.getName()
                    + " enum does not exist with the value '" + value + "'");
            throw new Exception("An '" + klass.getName()
                    + "' enum exists with the value '" + value + "'");
        } catch (ApiUsageException e) {
            logger.info("checkEnumeration can be added. " + e.getMessage());
        }
        return true;

    }

    /**
     * Resets value of enumerations specified by class
     * 
     * @param klass
     *            Class
     */
    public void resetEnumeration(Class klass) {
        typesService.resetEnumerations(klass);
    }        

    // -----------------------------------------------------------------------

    /**
     * IRepositoryInfo interface
     */

    /**
     * This method returns the total space in bytes for this file system
     * including nested subdirectories. The Java 6 J2SE provides this
     * functionality now using similar methods in the class java.io.File. A
     * refactoring of related classes should be performed when the later sdk is
     * adopted.
     * 
     * @return total space on this file system.
     * @throws InternalException
     */
    public long getUsedSpaceInKilobytes() throws InternalException {
        logger.info("getUsedSpaceInKilobytes by user ID: '" + userid + "'");
        long usedSpace = 0L;
        try {
            usedSpace = this.repService.getUsedSpaceInKilobytes();
        } catch (InternalException e) {
            logger.error(e.getMessage());
        }
        logger.info("usedSpace = '" + usedSpace + "'");
        return usedSpace;
    }

    /**
     * This method returns the free or available space on this file system
     * including nested subdirectories. The Java 6 J2SE provides this
     * functionality now using similar methods in the class java.io.File. A
     * refactoring of related classes should be performed when the later sdk is
     * adopted.
     * 
     * @return free space on this file system
     * @throws InternalException
     */
    public long getFreeSpaceInKilobytes() throws InternalException {
        logger.info("getFreeSpaceInKilobytes by user ID: '" + userid + "'");
        long freeSpace = 0L;
        try {
            freeSpace = this.repService.getFreeSpaceInKilobytes();
        } catch (InternalException e) {
            logger.error(e.getMessage());
        }
        logger.info("freeSpace = '" + freeSpace + "'");
        return freeSpace;
    }

    /**
     * Gets used space on this file system by users.
     * 
     * @return {@link java.util.HashMap} of used space on this file system by
     *         users in Bytes.
     */
    public HashMap getTopTen() {
        logger.info("getTopTen by user ID: '" + userid + "'");
        try {
            List<Pixels> pixels = queryService
                    .findAllByQuery(
                            "select p from Pixels as p left outer join fetch p.pixelsType",
                            null);
            HashMap<Long, Long> usage = new HashMap<Long, Long>();
            for (Pixels p : pixels) {
                Details d = p.getDetails();
                Long expid = d.getOwner().getId();
                Long bytesUsed = usage.get(expid);
                if (bytesUsed == null)
                    bytesUsed = 0L;
                bytesUsed += p.getSizeX() * p.getSizeY() * p.getSizeZ()
                        * p.getSizeC() * p.getSizeT()
                        * bytesPerPixel(p.getPixelsType());
                usage.put(expid, bytesUsed);
            }

            HashMap<String, Long> map = new LinkedHashMap<String, Long>();

            List mapKeys = new ArrayList(usage.keySet());
            List mapValues = new ArrayList(usage.values());
            usage.clear();

            TreeSet sortedSet = new TreeSet(mapValues);
            Object[] sortedArray = sortedSet.toArray();
            int size = sortedArray.length;

            int topTenVal = Integer.parseInt(facesContext.getExternalContext()
                    .getInitParameter("topTenValue"));
            logger.info("topTenVal: '" + topTenVal + "'");
            if (size <= topTenVal) {
                logger.info("topTenVal is greater then size of user list...");
                topTenVal = size;
                logger.info("topTenVal was reduced to '" + size + "'");
            }

            for (int i = size; i > (size - topTenVal);) {
                Long val = (Long) sortedArray[--i] / 1024;
                map.put(adminService.getExperimenter(
                        (Long) mapKeys.get(mapValues.indexOf(sortedArray[i])))
                        .getOmeName(), val);
                logger.info("topTenVal '"
                        + i
                        + "': '"
                        + ((Long) mapKeys
                                .get(mapValues.indexOf(sortedArray[i])))
                        + "' '" + sortedArray[i] + "'");
            }

            long rest = 0;
            for (int i = (size - topTenVal); i > 0;)
                rest += (Long) sortedArray[--i];

            logger.info("rest of space is: '" + rest + "'");
            if (rest > 0) {
                rest = rest / 1024;
                map.put("Rest of used space", Long.valueOf(rest));
            }
            return map;

        } catch (Exception e) {
            logger.error("getTopTen: " + e.getMessage());
            return null;
        }

    }

    // -----------------------------------------------------------------------

    /**
     * A static helper method to check if a type is one of the elements in an
     * array.
     * 
     * @param type
     *            A pixels type enumeration.
     * @param strings
     *            The strings for which you want to check against.
     * @return True on successful match and false on failure to match.
     */
    public static boolean in(PixelsType type, String[] strings) {
        String typeAsString = type.getValue();
        for (int i = 0; i < strings.length; i++) {
            if (typeAsString.equals(strings[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * A static helper method to retrieve pixel byte widths.
     * 
     * @param type
     *            The pixels type for which you want to know the byte width.
     * @return The number of bytes per pixel value.
     */
    static int bytesPerPixel(PixelsType type) {
        if (in(type, new String[] { "int8", "uint8" })) {
            return 1;
        } else if (in(type, new String[] { "int16", "uint16" })) {
            return 2;
        } else if (in(type, new String[] { "int32", "uint32", "float" })) {
            return 4;
        } else if (type.getValue().equals("double")) {
            return 8;
        } else {
            throw new RuntimeException("Unknown pixel type: '"
                    + type.getValue() + "'");
        }
    }

    // -----------------------------------------------------------------------

    /**
     * IAdmin interface
     */

    /**
     * Changs the password for current {@link ome.model.meta.Experimenter}.
     * 
     * @param password
     *            Not-null. Might must pass validation in the security
     *            sub-system.
     */
    public void changeMyPassword(String password) {
        logger.info("changeMyPassword by user ID: " + userid);
        adminService.changePassword(password);
    }

    /**
     * Changs the password for {@link ome.model.meta.Experimenter}.
     * 
     * @param username
     *            The {@link ome.model.meta.Experimenter#getOmeName()} . Not
     *            null.
     * @param password
     *            Not-null. Might must pass validation in the security
     *            sub-system.
     */
    public void changePassword(String username, String password) {
        logger.info("changePassword for user: " + username + "by user ID: "
                + userid);
        adminService.changeUserPassword(username, password);
    }

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup}
     * which was add for select default group list.
     * 
     * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     */
    public List<ExperimenterGroup> lookupGroupsAdd() {
        logger.info("lookupGroupsAdd by user ID: " + userid + "'");
        List<ExperimenterGroup> exgs = filterAdd(adminService.lookupGroups());
        for (ExperimenterGroup exg : exgs) {
            logger.info("Group details [id: '" + exg.getId() + "', name: '"
                    + exg.getName() + "'");
        }
        return exgs;
    }

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup}
     * for select others group list.
     * 
     * @return {@link java.util.List}<{@link ome.model.meta.ExperimenterGroup}>.
     */
    public List<ExperimenterGroup> lookupGroups() {
        logger.info("lookupGroups by user ID: " + userid + "'");
        List<ExperimenterGroup> exgs = filter(adminService.lookupGroups());
        for (ExperimenterGroup exg : exgs) {
            logger.info("Group details [id: '" + exg.getId() + "', name: '"
                    + exg.getName() + "'");
        }
        return exgs;
    }

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.Experimenter}.
     * 
     * @return {@link java.util.List}<{@link ome.model.meta.Experimenter}>.
     */
    public List<Experimenter> lookupExperimenters() {
        logger.info("lookupExperimenters by user ID: " + userid + "'");
        List<Experimenter> exps = adminService.lookupExperimenters();
        for (Experimenter exp : exps) {
            logger.info("Experimenter details [id: '" + exp.getId()
                    + "', Ome name: '" + exp.getOmeName() + "', email: '"
                    + exp.getEmail() + "', First name: '" + exp.getFirstName()
                    + "', Middle name: '" + exp.getMiddleName()
                    + "', Last name: '" + exp.getLastName()
                    + "', Institution: '" + exp.getInstitution() + "]");
        }
        return exps;
    }

    public List<Map<String, Object>> lookupLdapAuthExperimenters() {
        logger.info("lookupLdapAuthExperimenters by user ID: " + userid + "'");
        return adminService.lookupLdapAuthExperimenters();
    }

    public String lookupLdapAuthExperimenter(Long id) {
        logger.info("lookupLdapAuthExperimenter for id = " + id
                + " by user ID: " + userid + "'");
        return adminService.lookupLdapAuthExperimenter(id);
    }

    /**
     * Gets {@link ome.model.meta.Experimenter} details.
     * 
     * @param omename
     *            {@link ome.model.meta.Experimenter#getOmeName()}.
     * @return {@link ome.model.meta.Experimenter}.
     */
    public Experimenter lookupExperimenter(String omename) {
        logger.info("lookupExperimenter by String '" + omename
                + "' by user ID: " + userid + "'");
        Experimenter exp = adminService.lookupExperimenter(omename);
        logger.info("Experimenter details [id: '" + exp.getId()
                + "', Ome name: '" + exp.getOmeName() + "', email: '"
                + exp.getEmail() + "', First name: '" + exp.getFirstName()
                + "', Middle name: '" + exp.getMiddleName() + "', Last name: '"
                + exp.getLastName() + "', Institution: '"
                + exp.getInstitution() + "]");
        return exp;
    }

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup} details by
     * {@link ome.model.meta.ExperimenterGroup#getId()}.
     * 
     * @param id
     *            {@link ome.model.meta.ExperimenterGroup#getId()}.
     * @return {@link ome.model.meta.ExperimenterGroup}.
     */
    public ExperimenterGroup getGroup(Long id) {
        logger.info("getGroup by ID '" + id + "' by user ID: " + userid + "'");
        ExperimenterGroup exg = adminService.getGroup(id);
        logger.info("Group details [id: '" + exg.getId() + "', name: '"
                + exg.getName() + "'");
        return exg;
    }

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup} details by
     * {@link ome.model.meta.ExperimenterGroup#getName()}.
     * 
     * @param name
     *            {@link ome.model.meta.ExperimenterGroup#getName()}.
     * @return {@link ome.model.meta.ExperimenterGroup}.
     */
    public ExperimenterGroup getGroup(String name) {
        logger.info("getGroup by String '" + name + "' by user ID: " + userid
                + "'");
        ExperimenterGroup exg = queryService.findByString(
                ExperimenterGroup.class, "name", name);
        logger.info("Group details [id: '" + exg.getId() + "', name: '"
                + exg.getName() + "'");
        return exg;
    }

    /**
     * Updates {@link ome.model.meta.ExperimenterGroup}.
     * 
     * @param experimenterGroup
     *            {@link ome.model.meta.ExperimenterGroup}
     */
    public void updateGroup(ExperimenterGroup experimenterGroup, Long ownerId) {
        logger.info("updateGroup by user ID: " + userid);
        logger.info("ExperimenterGroup details [id: '"
                + experimenterGroup.getId() + "', name: '"
                + experimenterGroup.getName() + "', desc: '"
                + experimenterGroup.getDescription() + "', owner: '" + ownerId
                + "']");
        Experimenter exp = adminService.getExperimenter(ownerId);
        adminService.updateGroup(experimenterGroup);
        adminService.setGroupOwner(experimenterGroup, exp);
    }

    /**
     * Deletes {@link ome.model.meta.ExperimenterGroup}.
     * 
     * @param id
     *            {@link ome.model.meta.ExperimenterGroup#getId()}
     */
    public void deleteGroup(Long id) {
        logger.info("deleteGroup ID: '" + id + "' by user ID: " + userid);
        logger.error("ERROR: no method in adminService");
    }

    /**
     * Deletes {@link ome.model.meta.Experimenter}
     * 
     * @param id
     *            {@link ome.model.meta.Experimenter#getId()}
     */
    public void deleteExperimenter(Long id) {
        logger.info("deleteExperimenter  ID: '" + id + "' by user ID: "
                + userid);
        adminService.deleteExperimenter(adminService.getExperimenter(id));

    }

    /**
     * Updates {@link ome.model.meta.Experimenter}
     * 
     * @param experimenter
     *            {@link ome.model.meta.Experimenter}. Not null.
     */
    public void updateExperimenter(Experimenter experimenter) {
        logger.info("updateExperimenter by user ID: " + userid);
        logger.info("Experimenter details [id: '" + experimenter.getId()
                + "', Ome name: '" + experimenter.getOmeName() + "', email: '"
                + experimenter.getEmail() + "', First name: '"
                + experimenter.getFirstName() + "', Middle name: '"
                + experimenter.getMiddleName() + "', Last name: '"
                + experimenter.getLastName() + "', Institution: '"
                + experimenter.getInstitution() + "]");
        adminService.updateExperimenter(experimenter);

    }

    /**
     * Updates {@link ome.model.meta.Experimenter} only for himself
     * 
     * @param experimenter
     *            {@link ome.model.meta.Experimenter}. Not null.
     */
    public void updateSelf(Experimenter experimenter) {
        logger.info("updateSelf by user ID: " + userid);
        logger.info("Experimenter details [id: '" + experimenter.getId()
                + "', Ome name: '" + experimenter.getOmeName() + "', email: '"
                + experimenter.getEmail() + "', First name: '"
                + experimenter.getFirstName() + "', Middle name: '"
                + experimenter.getMiddleName() + "', Last name: '"
                + experimenter.getLastName() + "', Institution: '"
                + experimenter.getInstitution() + "]");
        adminService.updateSelf(experimenter);

    }

    /**
     * Gets {@link ome.model.meta.Experimenter} details by
     * {@link ome.model.meta.Experimenter#getId()}
     * 
     * @param id
     *            {@link ome.model.meta.Experimenter#getId()}. Not null.
     * @return {@link ome.model.meta.Experimenter}
     */
    public Experimenter getExperimenter(Long id) {
        logger.info("getExperimenter by user ID: '" + userid + "'");
        Experimenter exp = adminService.getExperimenter(id);
        logger.info("Experimenter details [id: '" + exp.getId()
                + "', Ome name: '" + exp.getOmeName() + "', email: '"
                + exp.getEmail() + "', First name: '" + exp.getFirstName()
                + "', Middle name: '" + exp.getMiddleName() + "', Last name: '"
                + exp.getLastName() + "', Institution: '"
                + exp.getInstitution() + "]");
        return exp;
    }

    /**
     * Creates {@link ome.model.meta.ExperimenterGroup}
     * 
     * @param group
     *            {@link ome.model.meta.ExperimenterGroup}
     * @return {@link ome.model.meta.ExperimenterGroup#getId()}
     */
    public Long createGroup(ExperimenterGroup group, Long ownerId) {
        logger.info("createGroup by user ID: '" + userid + "'");
        Long id = 0L;
        logger.info("ExperimenterGroup details [name: '" + group.getName()
                + "', desc: '" + group.getDescription() + "', owner: '"
                + ownerId + "']");
        Experimenter exp = adminService.getExperimenter(ownerId);
        id = adminService.createGroup(group);
        ExperimenterGroup exgp = adminService.getGroup(id);
        adminService.setGroupOwner(exgp, exp);
        logger.info("ExperimenterGroup created with ID: '" + id + "'");
        return id;
    }

    /**
     * Creates {@link ome.model.meta.Experimenter}
     * 
     * @param experimenter
     *            {@link ome.model.meta.Experimenter}. Not null.
     * @param defaultGroup
     *            {@link ome.model.meta.ExperimenterGroup}. Not null.
     * @param groups
     *            {@link ome.model.meta.ExperimenterGroup} []
     * @return {@link ome.model.meta.Experimenter#getId()}
     */
    public Long createExperimenter(Experimenter experimenter,
            ExperimenterGroup defaultGroup, ExperimenterGroup... groups) {
        logger.info("createExperimenter by user ID: " + userid);
        Long id = 0L;

        logger.info("Experimenter details [Ome name: '"
                + experimenter.getOmeName() + "', email: '"
                + experimenter.getEmail() + "', First name: '"
                + experimenter.getFirstName() + "', Middle name: '"
                + experimenter.getMiddleName() + "', Last name: '"
                + experimenter.getLastName() + "', Institution: '"
                + experimenter.getInstitution() + "]");
        logger.info("DefaultGroup details [id: '" + defaultGroup.getId()
                + "', name: '" + defaultGroup.getName() + "'");
        for (int i = 0; i < groups.length; i++)
            logger.info("Group " + i + " details [id: '" + groups[i].getId()
                    + "', name: '" + groups[i].getName() + "'");

        id = adminService
                .createExperimenter(experimenter, defaultGroup, groups);

        logger.info("Experimenter created with ID: '" + id + "'");
        return id;
    }

    /**
     * Checks existing {@link ome.model.meta.Experimenter#getOmeName()} on the
     * database.
     * 
     * @param omeName
     *            {@link ome.model.meta.Experimenter#getOmeName()}
     * @return boolean
     */
    public boolean checkExperimenter(String omeName) {
        logger.info("checkExperimenter by String '" + omeName
                + "' by user ID: '" + userid + "'");
        if (queryService.findByString(Experimenter.class, "omeName", omeName) != null) {
            logger.info("Experimenter '" + omeName + "' exist");
            return true;
        }
        logger.info("Experimenter '" + omeName + "' doesn't exist");
        return false;
    }

    /**
     * Checks existing {@link ome.model.meta.ExperimenterGroup#getName()} on the
     * database.
     * 
     * @param name
     *            {@link ome.model.meta.ExperimenterGroup#getName()}
     * @return boolean
     */
    public boolean checkExperimenterGroup(String name) {
        logger.info("checkExperimenterGroup by String '" + name
                + "' by user ID: '" + userid + "'");
        if (queryService.findByString(ExperimenterGroup.class, "name", name) != null) {
            logger.info("ExperimenterGroup '" + name + "' exist");
            return true;
        }
        logger.info("ExperimenterGroup '" + name + "' doesn't exist");
        return false;
    }

    /**
     * Checks existing {@link ome.model.meta.Experimenter#getEmail()} in the
     * database.
     * 
     * @param email
     *            {@link ome.model.meta.Experimenter#getEmail()}
     * @return boolean
     */
    public boolean checkEmail(String email) {
        logger.info("checkEmail by String '" + email + "' by user ID: '"
                + userid + "'");
        if (queryService.findByString(Experimenter.class, "email", email) != null) {
            logger.info("Email '" + email + "' exist");
            return true;
        }
        logger.info("Email '" + email + "' doesn't exist");
        return false;
    }

    /**
     * Checks System permition for{@link ome.model.meta.Experimenter#getId()}.
     * 
     * @param experimenterId
     *            {@link ome.model.meta.Experimenter#getId()}
     * @return boolean
     */
    public boolean isAdmin(Long experimenterId) {
        logger.info("isAdmin by ID '" + experimenterId + "' by user ID: '"
                + userid + "'");
        boolean role = false;
        ExperimenterGroup[] exg = adminService.containedGroups(experimenterId);
        for (int i = 0; i < exg.length; i++) {
            if (exg[i].getName().equals("system"))
                role = true;
        }
        logger.info("isAdmin return: '" + role + "'");
        return role;
    }

    /**
     * Checks User perimition for {@link ome.model.meta.Experimenter#getId()}.
     * 
     * @param experimenterId
     *            {@link ome.model.meta.Experimenter#getId()}
     * @return boolean
     */
    public boolean isUser(Long experimenterId) {
        logger.info("isUser by ID '" + experimenterId + "' by user ID: '"
                + userid + "'");
        boolean role = false;
        ExperimenterGroup[] exg = adminService.containedGroups(experimenterId);
        for (int i = 0; i < exg.length; i++) {
            if (exg[i].getName().equals("user"))
                role = true;
        }
        logger.info("isUser return: '" + role + "'");
        return role;
    }

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup} [] for all of the
     * {@link ome.model.meta.Experimenter#getId()} without "system", default"
     * and "user" groups.
     * 
     * @param experimenterId
     *            {@link ome.model.meta.Experimenter#getId()}
     * @return {@link ome.model.meta.ExperimenterGroup} []
     */
    public ExperimenterGroup[] containedGroups(Long experimenterId) {
        logger.info("containedGroups by ID: '" + experimenterId
                + "' by user ID: '" + userid + "'");
        ExperimenterGroup[] exgs = filter(adminService
                .containedGroups(experimenterId));
        for (int i = 0; i < exgs.length; i++) {
            logger.info("Group details [id: '" + exgs[i].getId() + "', name: '"
                    + exgs[i].getName() + "'");
        }
        return exgs;
    }

    /**
     * Gets {@link ome.model.meta.Experimenter} [] for all of the
     * {@link ome.model.meta.ExperimenterGroup#getId()}
     * 
     * @param groupId
     *            {@link ome.model.meta.ExperimenterGroup#getId()}
     * @return {@link ome.model.meta.Experimenter} []
     */
    public Experimenter[] containedExperimenters(Long groupId) {
        logger.info("containedExperimenters by ID: '" + groupId
                + "' by user ID: '" + userid + "'");
        Experimenter[] exps = adminService.containedExperimenters(groupId);
        for (int i = 0; i < exps.length; i++) {
            logger.info("Experimenter details [Ome name: '"
                    + exps[i].getOmeName() + "', email: '" + exps[i].getEmail()
                    + "', First name: '" + exps[i].getFirstName()
                    + "', Middle name: '" + exps[i].getMiddleName()
                    + "', Last name: '" + exps[i].getLastName()
                    + "', Institution: '" + exps[i].getInstitution() + "]");
        }
        return exps;
    }

    /**
     * Gets {@link java.util.List} of {@link ome.model.meta.ExperimenterGroup}
     * for all of the {@link ome.model.meta.Experimenter#getId()} without
     * "system", default" and "user" groups.
     * 
     * @param experimenterId
     *            {@link ome.model.meta.Experimenter#getId()}
     * @return List of {@link ome.model.meta.ExperimenterGroup}
     */
    public List<ExperimenterGroup> containedGroupsList(Long experimenterId) {
        logger.info("containedGroupsList by ID: '" + experimenterId
                + "' by user ID: '" + userid + "'");
        ExperimenterGroup[] exg = adminService.containedGroups(experimenterId);
        List<ExperimenterGroup> groups = Arrays.asList(exg);
        for (ExperimenterGroup group : groups) {
            logger.info("Group details[id: '" + group.getId() + "', name: '"
                    + group.getName() + "']");
        }
        return groups;

    }

    /**
     * Gets {@link java.util.List} of
     * {@link ome.model.meta.Experimenter#getId()} as String for all of the
     * {@link ome.model.meta.Experimenter#getId()} without "system", default"
     * and "user" groups.
     * 
     * @param experimenterId
     *            {@link ome.model.meta.Experimenter#getId()}
     * @return List of String
     */
    public List<String> containedGroupsListString(Long experimenterId) {
        logger.info("containedGroupsListString by ID: '" + experimenterId
                + "' by user ID: '" + userid + "'");
        ExperimenterGroup[] exgs = adminService.containedGroups(experimenterId);
        List<String> groups = new ArrayList<String>();
        for (ExperimenterGroup exg : exgs) {
            groups.add(exg.getId().toString());
        }
        for (String group : groups)
            logger.info("Group details[id: '" + group + "]");

        return groups;

    }

    /**
     * Gets {@link ome.model.meta.ExperimenterGroup} [] for all of the
     * {@link ome.model.meta.Experimenter#getId()} without "system" and "user"
     * groups.
     * 
     * @param experimenterId
     *            {@link ome.model.meta.Experimenter#getId()}
     * @return {@link ome.model.meta.ExperimenterGroup} []
     */
    public ExperimenterGroup[] containedMyGroups(Long experimenterId) {
        logger.info("containedMyGroups by ID: '" + experimenterId
                + "' by user ID: '" + userid + "'");
        ExperimenterGroup[] exgs = filterMy(adminService
                .containedGroups(experimenterId));
        for (int i = 0; i < exgs.length; i++) {
            logger.info("Group details [id: '" + exgs[i].getId() + "', name: '"
                    + exgs[i].getName() + "'");
        }
        return exgs;
    }

    /**
     * Gets "default group" for {@link ome.model.meta.Experimenter#getId()}
     * 
     * @param experimenterId
     *            {@link ome.model.meta.Experimenter#getId()}
     * @return {@link ome.model.meta.ExperimenterGroup}
     */
    public ExperimenterGroup getDefaultGroup(Long experimenterId) {
        logger.info("getDefaultGroup by ID: '" + experimenterId
                + "' by user ID: '" + userid + "'");
        ExperimenterGroup exg = adminService.getDefaultGroup(experimenterId);
        logger.info("Default ExperimenterGroup details [id: '" + exg.getId()
                + "', name: '" + exg.getName() + "'");
        return exg;
    }

    /**
     * Set "default group" for {@link ome.model.meta.Experimenter}
     * 
     * @param experimenter
     *            {@link ome.model.meta.Experimenter}
     * @param defaultGroup
     *            {@link ome.model.meta.ExperimenterGroup}
     */
    public void setDefaultGroup(Experimenter experimenter,
            ExperimenterGroup defaultGroup) {
        logger.info("setDefaultGroup for Experimenter [id: '"
                + experimenter.getId() + "', name: '"
                + experimenter.getOmeName() + "'] by user ID: " + userid);

        logger.info("Default ExperimenterGroup details [id: '"
                + defaultGroup.getId() + "', name: '" + defaultGroup.getName()
                + "'");
        adminService.setDefaultGroup(experimenter, defaultGroup);

        logger.info("default defaultGroup " + defaultGroup.getName() + "[id:"
                + defaultGroup.getId() + "] for user: "
                + experimenter.getOmeName() + "[id:" + experimenter.getId()
                + "] was set");
    }

    public void setExperimenters(List<Experimenter> addExps,
            List<Experimenter> rmExps, ExperimenterGroup group) {
        logger.info("setExperimenters for group [id: '" + group.getId()
                + "', name: '" + group.getName() + "'] by user ID: " + userid);
        for (int i = 0; i < addExps.size(); i++) {
            Experimenter exp = addExps.get(i);
            logger.info("add Experimenter [id: '" + exp.getId()
                    + "', OmeName: '" + exp.getOmeName() + "'] to group [id: '"
                    + group.getId() + "']");
            adminService.addGroups(exp, group);
        }

        for (int i = 0; i < rmExps.size(); i++) {
            Experimenter exp = rmExps.get(i);
            logger.info("remove Experimenter [id: '" + exp.getId()
                    + "', OmeName: '" + exp.getOmeName()
                    + "'] from group [id: '" + group.getId() + "']");
            adminService.removeGroups(exp, group);
        }
    }

    /**
     * Set "other groups" for {@link ome.model.meta.Experimenter}
     * 
     * @param experimenter
     *            {@link ome.model.meta.Experimenter}
     * @param addGroups
     *            {@link ome.model.meta.ExperimenterGroup} []
     * @param rmGroups
     *            {@link ome.model.meta.ExperimenterGroup} []
     * @param defaultGroup
     *            {@link ome.model.meta.ExperimenterGroup}
     */
    public void setOtherGroups(Experimenter experimenter,
            ExperimenterGroup[] addGroups, ExperimenterGroup[] rmGroups,
            ExperimenterGroup defaultGroup) {
        logger.info("setOtherGroups for Experimenter [id: '"
                + experimenter.getId() + "', name: '"
                + experimenter.getOmeName() + "'] by user ID: " + userid);
        if (addGroups.length > 0) {
            for (int i = 0; i < addGroups.length; i++)
                logger.info("Add ExperimenterGroup " + i + " [id: '"
                        + addGroups[i].getId() + "', name: '"
                        + addGroups[i].getName() + "']");
            adminService.addGroups(experimenter, addGroups);
        } else {
            logger.info("No ExperimenterGroup to add");
        }

        logger.info("Default ExperimenterGroup details [id: '"
                + defaultGroup.getId() + "', name: " + defaultGroup.getName()
                + "']");
        adminService.setDefaultGroup(experimenter, defaultGroup);

        if (rmGroups.length > 0) {
            for (int i = 0; i < rmGroups.length; i++)
                logger.info("Rm ExperimenterGroup " + i + " [id: '"
                        + rmGroups[i].getId() + "', name: '"
                        + rmGroups[i].getName() + "']");
            adminService.removeGroups(experimenter, rmGroups);
        } else {
            logger.info("No ExperimenterGroup to remove");
        }
    }

    // -----------------------------------------------------------------------

    /**
     * 
     * @param groups
     * @return
     */
    private ExperimenterGroup[] filter(ExperimenterGroup[] groups) {
        List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
        for (int i = 0; i < groups.length; i++) {
            if (// !groups[i].getName().equals("default") &&
            !groups[i].getName().equals("user")
            // && !groups[i].getName().equals("system")
            ) {
                filteredGroups.add(groups[i]);
            }
        }
        return filteredGroups.toArray(new ExperimenterGroup[filteredGroups
                .size()]);
    }

    /**
     * 
     * @param groups
     * @return
     */
    private ExperimenterGroup[] filterMy(ExperimenterGroup[] groups) {
        List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
        for (int i = 0; i < groups.length; i++) {
            if (!groups[i].getName().equals("user") // &&
            // !groups[i].getName().equals("system")
            ) {
                filteredGroups.add(groups[i]);
            }
        }
        return filteredGroups.toArray(new ExperimenterGroup[filteredGroups
                .size()]);
    }

    /**
     * 
     * @param groups
     * @return
     */
    private List<ExperimenterGroup> filter(List<ExperimenterGroup> groups) {
        List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
        for (int i = 0; i < groups.size(); i++) {
            if (!groups.get(i).getName().equals("user") // &&
            // !groups.get(i).getName().equals("system")
            // && !groups.get(i).getName().equals("default")
            ) {
                filteredGroups.add(groups.get(i));
            }
        }
        return filteredGroups;
    }

    /**
     * 
     * @param groups
     * @return
     */
    private List<ExperimenterGroup> filterAdd(List<ExperimenterGroup> groups) {
        List<ExperimenterGroup> filteredGroups = new ArrayList<ExperimenterGroup>();
        for (int i = 0; i < groups.size(); i++) {
            if (!groups.get(i).getName().equals("user") // &&
            // !groups.get(i).getName().equals("system")
            ) {
                filteredGroups.add(groups.get(i));
            }
        }
        return filteredGroups;
    }

}
