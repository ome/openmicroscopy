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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.internal.NamedValue;
import ome.model.meta.ExperimenterGroup;
import ome.model.screen.Plate;
import ome.model.screen.PlateAcquisition;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import ome.security.ACLVoter;



/**
 *  Policy which should be checked anytime access to original binary files in
 *  OMERO is being attempted. This check is <em>in addition</em> to the
 *  standard permission permission and is intended to allow customizing who
 *  has access to widely shared data.
 */
public class BinaryAccessPolicy extends BasePolicy {

    /**
     * This string can also be found in the Constants.ice file in the
     * blitz package.
     */
    public final static String NAME = "RESTRICT-BINARY-ACCESS";

    private final ACLVoter voter;

    private final Set<String> global;

    public BinaryAccessPolicy(Set<Class<IObject>> types, ACLVoter voter) {
        this(types, voter, null);
    }

    public BinaryAccessPolicy(Set<Class<IObject>> types, ACLVoter voter,
            String[] config) {
        super(types);
        this.voter = voter;
        if (config == null) {
            this.global = Collections.emptySet();
        } else {
            this.global = new HashSet<String>(Arrays.asList(config));
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isRestricted(IObject obj) {
        final Set<String> group= groupRestrictions(obj);

        if (notAorB("+write", "-write", group)) {
            // effectively "None"
            return true;
        } else if (notAorB("+read", "-read", group)) {
            if (!voter.allowUpdate(obj, obj.getDetails())) {
                return true;
            }
        }

        final boolean noImage = notAorB("+image", "-image", group);
        final boolean noPlate = notAorB("+plate", "-plate", group);

        if (obj instanceof Image) {
            if (noImage) {
                return true;
            }
            // If an Image has a WellSample, then *also* perform the plate check
            // Note: checking noPlate first since it doesn't need to hit the DB.
            if (noPlate) {
                Image img = (Image) obj;
                if (img.sizeOfWellSamples() > 1) {
                    return true;
                }
            }
        } else if (obj instanceof Plate ||
            obj instanceof PlateAcquisition ||
            obj instanceof Well ||
            obj instanceof WellSample) {

            if (noImage || noPlate) {
                return true;
            }
        }

        return false;
    }

    protected Set<String> groupRestrictions(IObject obj) {
        ExperimenterGroup grp = obj.getDetails().getGroup();
        if (grp != null && grp.getConfig() != null && grp.getConfig().size() > 0) {
            Set<String> rv = null;
            for (NamedValue nv : grp.getConfig()) {
                if ("omero.policy.binary_access".equals(nv.getName())) {
                    if (rv == null) {
                        rv = new HashSet<String>();
                    }
                    String setting = nv.getValue();
                    rv.add(setting);
                }
            }
            if (rv != null) {
                return rv;
            }
        }
        return Collections.emptySet();
    }

    /**
     * Returns true if the minus argument is present in the configuration
     * collections <em>or</em> if the plus argument is not present.
     */
    private final boolean notAorB(String plus, String minus, Collection<String> group) {
        if (global.contains(minus) || group.contains(minus)) {
            return true;
        } else if (global.contains(plus) || group.contains(plus)) {
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
