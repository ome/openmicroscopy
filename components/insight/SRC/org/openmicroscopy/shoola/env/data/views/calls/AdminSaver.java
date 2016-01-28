/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.views.calls;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.openmicroscopy.shoola.env.data.AdminService;
import org.openmicroscopy.shoola.env.data.model.AdminObject;

import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;

import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;

/** 
 * Saves objects.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class AdminSaver
    extends BatchCallTree
{

    /** Indicates to delete the objects. */
    public static final int DELETE = 0;

    /** The result of the call. */
    private Object result;

    /** Loads the specified experimenter groups. */
    private BatchCall   loadCall;

    /**
     * Creates a {@link BatchCall} to delete the objects.
     * 
     * @param ctx The security context.
     * @param objects The objects to handle.
     * @return The {@link BatchCall}.
     */
    private BatchCall deleteObjects(final SecurityContext ctx,
            final List<DataObject> objects)
    {
        return new BatchCall("Delete objects") {
            public void doCall() throws Exception
            {
                AdminService os = context.getAdminService();
                List<DataObject> l = new ArrayList<DataObject>();
                List<GroupData> groups = new ArrayList<GroupData>();
                List<ExperimenterData> experimenters = 
                        new ArrayList<ExperimenterData>();
                Iterator<DataObject> i = objects.iterator();
                DataObject data;
                while (i.hasNext()) {
                    data = i.next();
                    if (data instanceof GroupData) {
                        groups.add((GroupData) data);
                    } else if (data instanceof ExperimenterData) {
                        experimenters.add((ExperimenterData) data);
                    }
                }
                if (groups.size() > 0)
                    l.addAll(os.deleteGroups(ctx, groups));
                if (experimenters.size() > 0)
                    l.addAll(os.deleteExperimenters(ctx, experimenters));
                result = l;
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to create experimenters.
     * 
     * @param ctx The security context.
     * @param object 	The experimenters to create.
     * @return The {@link BatchCall}.
     */
    private BatchCall createExperimenters(final SecurityContext ctx,
            final AdminObject object)
    {
        return new BatchCall("Create experimenters") {
            public void doCall() throws Exception
            {
                AdminService os = context.getAdminService();
                result = os.createExperimenters(ctx, object);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to reset the password of the experimenters.
     * 
     * @param ctx The security context.
     * @param object The object to handle.
     * @return The {@link BatchCall}.
     */
    private BatchCall resetExperimentersPassword(final SecurityContext ctx,
            final AdminObject object)
    {
        return new BatchCall("Reset password") {
            public void doCall() throws Exception
            {
                AdminService os = context.getAdminService();
                result = os.resetExperimentersPassword(ctx, object);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to activate or not the experimenters.
     * 
     * @param ctx The security context.
     * @param object The experimenters to handle.
     * @return The {@link BatchCall}.
     */
    private BatchCall activateExperimenters(final SecurityContext ctx,
            final AdminObject object)
    {
        return new BatchCall("Reset password") {
            public void doCall() throws Exception
            {
                AdminService os = context.getAdminService();
                result = os.activateExperimenters(ctx, object);
            }
        };
    }

    /**
     * Creates a {@link BatchCall} to create group.
     * 
     * @param ctx The security context.
     * @param object The experimenters to create.
     * @return The {@link BatchCall}.
     */
    private BatchCall createGroup(final SecurityContext ctx,
            final AdminObject object)
    {
        return new BatchCall("Create group") {
            public void doCall() throws Exception
            {
                AdminService os = context.getAdminService();
                result = os.createGroup(ctx, object);
            }
        };
    }

    /**
     * Adds the {@link #loadCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(loadCall); }

    /**
     * Returns, in a <code>Set</code>, the root nodes of the found trees.
     * @see BatchCallTree#getResult()
     */
    protected Object getResult() { return result; }

    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param object The object to handle. Mustn't be <code>null</code>.
     */
    public AdminSaver(SecurityContext ctx, AdminObject object)
    {
        if (object == null)
            throw new IllegalArgumentException("Object not valid.");
        switch (object.getIndex()) {
            case AdminObject.CREATE_EXPERIMENTER:
                loadCall = createExperimenters(ctx, object);
                break;
            case AdminObject.CREATE_GROUP:
                loadCall = createGroup(ctx, object);
                break;
            case AdminObject.RESET_PASSWORD:
                loadCall = resetExperimentersPassword(ctx, object);
                break;
            case AdminObject.ACTIVATE_USER:
                loadCall = activateExperimenters(ctx, object);
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param objects The objects to handle. Mustn't be <code>null</code>.
     * @param index One of the constants defined by this class.
     */
    public AdminSaver(SecurityContext ctx, List<DataObject> objects, int index)
    {
        if (CollectionUtils.isEmpty(objects))
            throw new IllegalArgumentException("No objects to handle");
        switch (index) {
            case DELETE:
                loadCall = deleteObjects(ctx, objects);
                break;
        }
    }

}
