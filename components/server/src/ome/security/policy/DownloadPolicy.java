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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.security.ACLVoter;



/**
 *  Policy which should be checked anytime access to original binary files in
 *  OMERO is being attempted. This check is <em>in addition</em> to the
 *  standard permission permission and is intended to allow customizing who
 *  has access to widely shared data.
 */
public class DownloadPolicy extends BasePolicy {

    /**
     * This string can also be found in the Constants.ice file in the
     * blitz package.
     */
    public final static String NAME = "RESTRICT-DOWNLOAD";

    private final ACLVoter voter;

    private final List<String> config;

    public DownloadPolicy(Set<Class<IObject>> types, ACLVoter voter) {
        this(types, voter, null);
    }

    public DownloadPolicy(Set<Class<IObject>> types, ACLVoter voter,
            String[] config) {
        super(types);
        this.voter = voter;
        if (config == null) {
            this.config = Collections.emptyList();
        } else {
            this.config = Arrays.asList(config);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isRestricted(IObject obj) {
        if (config.isEmpty()) {
            return false;
        } else if (config.contains("none")) {
           return true;
        } else if (config.contains("repository")) {
            if (voter.allowUpdate(obj, obj.getDetails())) {
                return false;
            }
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