/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import ome.model.IEnum;
import ome.model.IObject;
import ome.model.jobs.Job;
import ome.model.meta.DBPatch;
import ome.model.meta.Event;
import ome.model.meta.EventLog;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.GroupExperimenterMap;

/**
 * Defines what {@link IObject} classes are considered "system" types. System
 * types have special meaning with regard to ACL. They cannot be created except
 * by an administrator, primarily.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public class SystemTypes {

    /**
     * classes which cannot be created by regular users.
     * 
     * @see <a
     *      href="https://trac.openmicroscopy.org.uk/omero/ticket/156">ticket156</a>
     */
    public boolean isSystemType(Class<? extends IObject> klass) {
        if (klass == null) {
            return false;
        } else if (ome.model.meta.Session.class.isAssignableFrom(klass)) {
            return true;
        } else if (Experimenter.class.isAssignableFrom(klass)) {
            return true;
        } else if (ExperimenterGroup.class.isAssignableFrom(klass)) {
            return true;
        } else if (GroupExperimenterMap.class.isAssignableFrom(klass)) {
            return true;
        } else if (Event.class.isAssignableFrom(klass)) {
            return true;
        } else if (EventLog.class.isAssignableFrom(klass)) {
            return true;
        } else if (IEnum.class.isAssignableFrom(klass)) {
            return true;
        } else if (Job.class.isAssignableFrom(klass)) {
            return true;
        } else if (DBPatch.class.isAssignableFrom(klass)) {
            return true;
        }
        return false;
    }
}