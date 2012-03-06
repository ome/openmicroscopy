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

	/** The permissions object */
    private final Permissions p;

    /** Creates a new instance. */
    public PermissionData()
    {
        p = new PermissionsI();
    }

    /** 
     * Creates a new instance.
     * 
     * @param permissions The value to set.
     */
    public PermissionData(Permissions permissions)
    {
    	if (permissions == null) p = new PermissionsI();
    	else p = permissions;
    }

    // ~ Rights
    // =====================================================================

    /**
     * Returns <code>true </code> if the group has read access,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isGroupRead() { return p.isGroupRead(); }

    /**
     * Returns <code>true </code> if the group has write access,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isGroupWrite() { return p.isGroupWrite(); }

    /**
     * Returns <code>true </code> if the user has read access,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isUserRead() { return p.isUserRead(); }

    /**
     * Returns <code>true </code> if the user has write access,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isUserWrite() { return p.isUserWrite(); }

    /**
     * Returns <code>true </code> if the world has read access,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isWorldRead() { return p.isWorldRead(); }

    /**
     * Returns <code>true </code> if the world has write access,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isWorldWrite() { return p.isWorldWrite(); }

    /**
     * Sets to <code>true</code> if the group has read access,
     * <code>false</code> otherwise.
     * 
     * @param groupRead The value to set.
     */
    public void setGroupRead(boolean groupRead) { p.setGroupRead(groupRead); }

    /**
     * Sets to <code>true</code> if the group has write access,
     * <code>false</code> otherwise.
     * 
     * @param groupWrite The value to set.
     */
    public void setGroupWrite(boolean groupWrite) 
    {
        p.setGroupWrite(groupWrite);
    }

    /**
     * Sets to <code>true</code> if the user has read access,
     * <code>false</code> otherwise.
     * 
     * @param userRead The value to set.
     */
    public void setUserRead(boolean userRead) { p.setUserRead(userRead); }

    /**
     * Sets to <code>true</code> if the user has write access,
     * <code>false</code> otherwise.
     * 
     * @param userWrite The value to set.
     */
    public void setUserWrite(boolean userWrite)
    {
        p.setUserWrite(userWrite);
    }

    /**
     * Sets to <code>true</code> if the world has read access,
     * <code>false</code> otherwise.
     * 
     * @param worldRead The value to set.
     */
    public void setWorldRead(boolean worldRead)
    {
        p.setWorldRead(worldRead);
    }

    /**
     * Sets to <code>true</code> if the world has write access,
     * <code>false</code> otherwise.
     * 
     * @param worldWrite The value to set.
     */
    public void setWorldWrite(boolean worldWrite)
    {
        p.setWorldWrite(worldWrite);
    }

}
