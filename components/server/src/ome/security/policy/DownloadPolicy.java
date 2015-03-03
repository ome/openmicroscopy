/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.security.policy;

import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.security.ACLVoter;



/**
 * 
 *
 */
public class DownloadPolicy extends BasePolicy {

    /**
     * This string can also be found in the Constants.ice file in the
     * blitz package.
     */
    public final static String NAME = "RESTRICT-DOWNLOAD";

    private final ACLVoter voter;

    public DownloadPolicy(ACLVoter voter) {
        this.voter = voter;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isRestricted(IObject obj) {
        if (voter.allowUpdate(obj, obj.getDetails())) {
            return false;
        }
        return true;
    }

    @Override
    public void checkRestriction(IObject obj) {
        if (isRestricted(obj)) {
            throw new SecurityViolation(String.format(
                    "Download is restricted for %s",
                    obj));
        }
    }
}