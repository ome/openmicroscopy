/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package omero.gateway.model;

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
 * @version 3.0
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

    /**
     * Returns the permissions level.
     *
     * @return See above.
     */
    public int getPermissionsLevel()
    {
        if (isGroupRead()) {
            if (isGroupAnnotate()) {
                if (isGroupWrite())
                    return GroupData.PERMISSIONS_GROUP_READ_WRITE;
                return GroupData.PERMISSIONS_GROUP_READ_LINK;
            }
            return GroupData.PERMISSIONS_GROUP_READ;
        }
        if (isWorldRead()) {
            if (isWorldWrite())
                return GroupData.PERMISSIONS_PUBLIC_READ_WRITE;
            return GroupData.PERMISSIONS_PUBLIC_READ;
        }
        return GroupData.PERMISSIONS_PRIVATE;
    }

    // ~ Rights
    // =====================================================================

    /**
     * Returns <code>true </code> if the group has read access
     * i.e. <code>RWR---</code>, <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isGroupRead() { return p.isGroupRead(); }

    /**
     * Returns <code>true </code> if the group has annotate access i.e.
     * <code>RWRA--</code>, <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isGroupAnnotate() { return p.isGroupAnnotate(); }

    /**
     * Returns <code>true </code> if the group has write access i.e.
     * <code>RWRW--</code>, <code>false</code> otherwise.
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
     * Sets to <code>true</code> if the group has annotate access,
     * <code>false</code> otherwise.
     *
     * @param groupAnnotate The value to set.
     */
    public void setGroupAnnotate(boolean groupAnnotate)
    {
        p.setGroupAnnotate(groupAnnotate);
    }

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
