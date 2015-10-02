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
import java.util.Iterator;
import java.util.Set;

import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.fs.Fileset;
import ome.model.fs.FilesetEntry;
import ome.model.internal.NamedValue;
import ome.model.meta.ExperimenterGroup;
import ome.model.screen.Plate;
import ome.model.screen.PlateAcquisition;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import ome.security.ACLVoter;

import org.hibernate.AssertionFailure;
import org.hibernate.Hibernate;



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

        // Possible performance impact!
        if (obj instanceof OriginalFile) {
            OriginalFile ofile = (OriginalFile) obj;

            // Quick short-cut for necessary files
            boolean isTxt = ofile.getName().endsWith(".txt");
            if (isTxt) {
                return false;
            }

            Iterator<FilesetEntry> it = ofile.iterateFilesetEntries();
            while (it.hasNext()) {
                FilesetEntry fe = it.next();
                 if (fe != null && fe.getFileset() != null) {
                    Fileset f = fe.getFileset();
                    if (has(f, Fileset.IMAGES)) {
                        if (noImage) {
                            return true;
                        } else if (noPlate) {
                            Iterator<Image> it2 = f.iterateImages();
                            while (it2.hasNext()) {
                                Image img = it2.next();
                                if (img != null) {
                                    if (has(img, Image.WELLSAMPLES)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (obj instanceof Image) {
            if (noImage) {
                return true;
            }
            // If an Image has a WellSample, then *also* perform the plate check
            // Note: checking noPlate first since it doesn't need to hit the DB.
            if (noPlate) {
                Image img = (Image) obj;
                if (has(img, Image.WELLSAMPLES)) {
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

    /**
     * Test if the size of the given collection is loadable and more than 0.
     *
     * If an {@link AssertionFailure} is thrown,  we assume that someone else
     * is trying to load the {@link IObject} at the same time. Since the
     * flag will be set on an earlier {@link IObject}, we assume that
     * an actual download won't be attempted. If it is, then the policy will
     * properly load this {@link IObject} and throw a SecurityViolation.
     */
    private boolean has(IObject obj, String field) {
        try {
            Collection<?> c = (Collection<?>) obj.retrieve(field);
            Hibernate.initialize(c);
            if (c != null && !c.isEmpty()) {
                return true;
            }
        } catch (AssertionFailure ae) {
            // pass
        }
        return false;
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
