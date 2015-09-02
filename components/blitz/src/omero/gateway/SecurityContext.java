/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package omero.gateway;

import omero.gateway.model.ExperimenterData;

import com.google.common.base.Objects;

/**
 * Hosts information required to access correct connector.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class SecurityContext {

    /** The identifier of the group. */
    private long groupID = -1;

    /** The experimenterID if required. */
    private ExperimenterData experimenter;

    /** Indicates to generate session for another user. */
    private boolean sudo;

    private ServerInformation serverInformation;

    private float compression;

    /**
     * Creates a new instance.
     * 
     * @param groupID
     *            The identifier of the group.
     */
    public SecurityContext(long groupID) {
        this.groupID = groupID;
        experimenter = null;
        sudo = false;
    }

    /** Indicates to create a session for another user. */
    public void sudo() {
        this.sudo = true;
    }

    /**
     * Returns <code>true</code> if a session has to be created for another
     * user, <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isSudo() {
        return sudo;
    }

    /**
     * Sets the experimenter
     * 
     * @param experimenter
     *            The experimenter.
     */
    public void setExperimenter(ExperimenterData experimenter) {
        this.experimenter = experimenter;
    }

    /**
     * Returns the id of the experimenter.
     * 
     * @return See above.
     */
    public long getExperimenter() {
        if (experimenter == null)
            return -1;
        return experimenter.getId();
    }

    /**
     * Returns the experimenter.
     * 
     * @return See above.
     */
    public ExperimenterData getExperimenterData() {
        return experimenter;
    }

    /**
     * Returns the identifier of the group.
     * 
     * @return See above.
     */
    public long getGroupID() {
        return groupID;
    }

    public ServerInformation getServerInformation() {
        return serverInformation;
    }

    public void setServerInformation(ServerInformation serverInformation) {
        this.serverInformation = serverInformation;
    }

    public float getCompression() {
        return compression;
    }

    public void setCompression(float compression) {
        this.compression = compression;
    }

    /**
     * Returns a copy of the security context.
     * 
     * @return See above.
     */
    public SecurityContext copy() {
        SecurityContext ctx = new SecurityContext(groupID);
        ctx.setExperimenter(this.experimenter);
        ctx.setServerInformation(this.serverInformation);
        ctx.setCompression(compression);
        return ctx;
    }

    /**
     * Calculate the hashCode for the data.
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return Objects.hashCode(this.getGroupID(), this.getServerInformation(),
                this.getExperimenter());
    }

    /**
     * Overridden to control if the passed object equals the current one.
     * 
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        return Objects.equal(((SecurityContext) obj).getGroupID(),
                this.getGroupID())
                && Objects.equal(
                        ((SecurityContext) obj).getServerInformation(),
                        this.getServerInformation())
                && Objects.equal(((SecurityContext) obj).getExperimenter(),
                        this.getExperimenter());
    }
}
