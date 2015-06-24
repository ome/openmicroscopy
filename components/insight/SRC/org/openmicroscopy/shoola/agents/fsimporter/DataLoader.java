/*
 * org.openmicroscopy.shoola.agents.fsimporter.DataLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter;


//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.treeviewer.DataBrowserLoader;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Reloads the hierarchies when new containers have been created during an
 * import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class DataLoader
	extends DataImporterLoader
{

    /** The root type, either <code>Project</code> or <code>Screen</code>.*/
    private Class rootType;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle; 

    /** Flag indicating to refresh the on-going import.*/
    private boolean refreshImport;

    /** Flag indicating that the group has been modified.*/
    private boolean changeGroup;

    /**
     * Creates a new instance.
     * 
     * @param viewer The Importer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param rootType Either Project or Screen.
     * @param refreshImport Flag indicating to refresh the on-going import.
     * @param changeGroup Flag indicating that the group has been modified
     * if <code>true</code>, <code>false</code> otherwise.
     */
    public DataLoader(Importer viewer, SecurityContext ctx, Class<?> rootType,
            boolean refreshImport, boolean changeGroup)
    {
        super(viewer, ctx);
        if (!(ProjectData.class.equals(rootType) ||
                ScreenData.class.equals(rootType)))
            throw new IllegalArgumentException("Type not supported.");
        this.rootType = rootType;
        this.refreshImport = refreshImport;
        this.changeGroup = changeGroup;
        userID = getCurrentUserID();
        if (ctx.getExperimenterData() != null) {
            userID = ctx.getExperimenterData().getId();
        }
    }

    /** 
     * Loads the data.
     * @see DataImporterLoader#load()
     */
    public void load()
    {
        handle = dmView.loadContainerHierarchy(ctx, rootType, null, false,
                -1, this);
    }

    /** 
     * Cancels the data loading.
     * @see DataImporterLoader#load()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Importer.DISCARDED)
            return;
        int type = Importer.PROJECT_TYPE;
        if (ScreenData.class.equals(rootType))
            type = Importer.SCREEN_TYPE;
        viewer.setContainers((Collection) result, refreshImport, changeGroup,
                type, userID);
    }

}
