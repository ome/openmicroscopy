/*
 * pojos.PermissionData
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package pojos;

// Java imports

// Third-party libraries

// Application-internal dependencies
import omero.model.Permissions;
import omero.model.PermissionsI;

/**
 * Simple data object to wrap a {@link Permissions} instance.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.more@gmx.de"> josh.moore@gmx.de</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision$ $Date$) </small>
 * @since 3.0-M3
 */
public class PermissionData {

    private final Permissions p;

    public PermissionData() {
        this.p = new PermissionsI();
    }

    public PermissionData(Permissions permissions) {
        this.p = permissions;
    }

    // ~ Rights
    // =====================================================================

    /**
     * Indicates if the group has read access.
     * 
     * @return the groupRead
     */
    public boolean isGroupRead() {
        return p.isGroupRead();
    }

    /**
     * Indicates if the group has write access.
     * 
     * @return the groupWrite
     */
    public boolean isGroupWrite() {
        return p.isGroupWrite();
    }

    /**
     * Indicates if the user has read access.
     * 
     * @return the userRead
     */
    public boolean isUserRead() {
        return p.isUserRead();
    }

    /**
     * Indicates if the user has write access.
     * 
     * @return the userWrite
     */
    public boolean isUserWrite() {
        return p.isUserWrite();
    }

    /**
     * Indicates if the world has read access.
     * 
     * @return the worldRead
     */
    public boolean isWorldRead() {
        return p.isWorldRead();
    }

    /**
     * Indicates if the world has write access.
     * 
     * @return the worldWrite
     */
    public boolean isWorldWrite() {
        return p.isWorldWrite();
    }

    /**
     * @param groupRead
     *            the groupRead to set
     */
    public void setGroupRead(boolean groupRead) {
        p.setGroupRead(groupRead);
    }

    /**
     * @param groupWrite
     *            the groupWrite to set
     */
    public void setGroupWrite(boolean groupWrite) {
        p.setGroupWrite(groupWrite);
    }

    /**
     * @param userRead
     *            the userRead to set
     */
    public void setUserRead(boolean userRead) {
        p.setUserRead(userRead);
    }

    /**
     * @param userWrite
     *            the userWrite to set
     */
    public void setUserWrite(boolean userWrite) {
        p.setUserWrite(userWrite);
    }

    /**
     * @param worldRead
     *            the worldRead to set
     */
    public void setWorldRead(boolean worldRead) {
        p.setWorldRead(worldRead);
    }

    /**
     * @param worldWrite
     *            the worldWrite to set
     */
    public void setWorldWrite(boolean worldWrite) {
        p.setWorldWrite(worldWrite);
    }

    // ~ Flags
    // =====================================================================

    /**
     * Indicates if the instance is locked.
     * 
     * @return locked
     */
    public boolean isLocked() {
        return p.isLocked();
    }

    /**
     * @param groupRead
     *            the groupRead to set
     */
    public void setLocked(boolean locked) {
        p.setLocked(locked);
    }

}
