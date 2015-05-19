/*
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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

package ome.formats.importer.targets;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportContainer;
import omero.ServerError;
import omero.api.IQueryPrx;
import omero.model.IObject;
import omero.model.Project;
import omero.model.ProjectI;
import omero.sys.ParametersI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TemplateImportTarget implements ImportTarget {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String template;
    private String filename;
    private boolean isSPW;

    private String user = null;
    private String group = null;
    private String project = null;
    private String dataset = null;
    private String screen = null;

    @Override
    public void init(String target) {
        this.template = target;

        parseTemplate();

        log.info("Resolved from template:");
        log.info("User    = " + user);
        log.info("Group   = " + group);
        log.info("Project = " + project);
        log.info("Dataset = " + dataset);
        log.info("Screen  = " + screen);
    }

    @Override
    // TODO: turn OMSC into an interface
    public IObject load(OMEROMetadataStoreClient client, ImportContainer ic) {
        this.filename = ic.getUsedFiles()[0];
        this.isSPW = ic.getIsSPW();
        return null;
    }

    //
    // Helpers
    //

    private void parseTemplate()
    {
        String[] parts = template.split(":");
        group = parts[0];
        if (isSPW)
        {
            if (!parts[1].equals("") || !parts[2].equals(""))
                screen = parts[1] + "-" + parts[2];
        }
        else
        {
            if (!parts[1].equals(""))
                project = parts[1];
            if (!parts[2].equals(""))
                dataset = parts[2];
        }
    }

    // Various Getters //
    public String getTemplate() {
        return template;
    }

    public String getFilename() {
        return filename;
    }

    public String getUser() {
        return user;
    }

    public String getGroup() {
        return group;
    }

    public String getProject() {
        return project;
    }

    public String getDataset() {
        return dataset;
    }

    public String getScreen() {
        return screen;
    }

    /**
     * Helper method to retrieve an object from iQuery
     *
     * @param <T>
     * @param it
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends IObject> T getTarget(OMEROMetadataStoreClient client)
    {
        if (getScreen() == null && getDataset() == null)
        {
            throw new RuntimeException(String.format("No dataset or screen in template: %s",
                        getTemplate()));
        }

        try
        {
            IQueryPrx iQuery = client.getIQuery();
            Map<Long, String> groupMap = client.mapUserGroups();
            Long groupId = null;
            if (getGroup() != null)
            {
                for (Entry<Long, String> entry : groupMap.entrySet())
                {
                    if (entry.getValue().equals(getGroup()))
                    {
                        groupId = entry.getKey();
                        break;
                    }
                }
                if (groupId == null) {
                    throw new RuntimeException(String.format("Cannot resolve group for: %s",
                                getTemplate()));
                }
            }

            // Map<String, String> groups = new HashMap<String, String>();
            // if (groupId == null)
            // {
            //     log.info("No group found.");
            //     groups.put("omero.group", "-1");
            // }
            // else
            // {
            //     log.info("Group found, id = " + groupId.toString());
            //     groups.put("omero.group", groupId.toString());
            //     // setCurrentGroup(groupId.longValue());
            // }

            T obj = null;
            ParametersI p = new ParametersI();
            p.exp(rlong(client.getExperimenterID()));
            if (groupId != null)
            {
                p.grp(rlong(groupId.longValue()));
            }
            p.page(0, 10);

            if (getScreen() != null)
            {
                p.add("screen", rstring(getScreen()));
                List<IObject> screens = iQuery.findAllByQuery(
                    "select distinct s from Screen as s where s.name = :screen", p);
                if (screens.size() == 0)
                {
                    log.info("Creating screen");
                    obj = (T) client.addScreen(getScreen(), "");
                }
                else
                {
                    obj = (T) screens.get(0);
                    if (screens.size() > 1)
                    {
                        log.info(String.format("Multiple matching Screens found, using id=%d", obj.getId().getValue()));
                    }
                    log.info(String.format("%d Screens found:", screens.size()));
                    for (IObject o : screens)
                    {
                        log.info(String.format("Screen id = %d", o.getId().getValue()));
                        log.info(String.format("   owner id %d", o.getDetails().getOwner().getId().getValue()));
                        log.info(String.format("   group id %d", o.getDetails().getGroup().getId().getValue()));
                    }
                }
            }
            else
            {
                log.info("Resolving project/dataset");
                if (getProject() == null)
                {
                    log.info("Resolving dataset");
                    p.add("dataset", rstring(getDataset()));
                    List<IObject> datasets = iQuery.findAllByQuery(
                        "select distinct d from Dataset as d where d.name = :dataset", p);

                    if (datasets.size() == 0)
                    {
                        log.info("Creating dataset");
                        Project project = new ProjectI();
                        obj = (T) client.addDataset(getDataset(), "", project);
                    }
                    else
                    {
                        obj = (T) datasets.get(0);
                        if (datasets.size() > 1)
                        {
                            log.info(String.format("Multiple matching Datasets found, using id=%d", obj.getId().getValue()));
                        }
                        log.info(String.format("%d Datasets found:", datasets.size()));
                        for (IObject o : datasets)
                        {
                            log.info(String.format("Dataset id = %d", o.getId().getValue()));
                            log.info(String.format("   owner id %d", o.getDetails().getOwner().getId().getValue()));
                            log.info(String.format("   group id %d", o.getDetails().getGroup().getId().getValue()));
                        }
                    }
                }
                else
                {
                    log.info("Resolving project");
                    p.add("project", rstring(getProject()));
                    p.add("dataset", rstring(getDataset()));
                    List<IObject> datasets = iQuery.findAllByQuery(
                        "select distinct d from Project as p, "
                        + "ProjectDatasetLink as pdl, "
                        + "Dataset as d where p.id = pdl.parent "
                        + "and pdl.child = d.id "
                        + "and p.name = :project "
                        + "and d.name = :dataset", p);

                    if (datasets.size() > 0)
                    {
                        obj = (T) datasets.get(0);
                        if (datasets.size() > 1)
                        {
                            log.info(String.format("Multiple matching Datasets found, using id=%d", obj.getId().getValue()));
                        }
                        log.info(String.format("%d Datasets found:", datasets.size()));
                        for (IObject o : datasets)
                        {
                            log.info(String.format("Dataset id = %d", o.getId().getValue()));
                            log.info(String.format("   owner id %d", o.getDetails().getOwner().getId().getValue()));
                            log.info(String.format("   group id %d", o.getDetails().getGroup().getId().getValue()));
                        }
                    }
                    else
                    {
                        p.add("project", rstring(getProject()));
                        List<IObject> projects = iQuery.findAllByQuery(
                            "select distinct p from Project as p where p.name = :project", p);
                        Project project = null;
                        if (projects.size() == 0)
                        {
                            log.info("Creating project and dataset");
                            project = client.addProject(getProject(), "");
                        }
                        else
                        {
                            project = (Project) projects.get(0);
                            if (projects.size() > 1)
                            {
                                log.info(String.format("Multiple matching Projects found, using id=%d", project.getId().getValue()));
                            }
                            log.info(String.format("%d Projects found:", projects.size()));
                            for (IObject o : projects)
                            {
                                log.info(String.format("Projects id = %d", o.getId().getValue()));
                                log.info(String.format("   owner id %d", o.getDetails().getOwner().getId().getValue()));
                                log.info(String.format("   group id %d", o.getDetails().getGroup().getId().getValue()));
                            }
                        }
                        obj = (T) client.addDataset(getDataset(), "", project);

                    }
                }
            }
            if (obj == null) {
                throw new RuntimeException(String.format("Cannot resolve target for: %s",
                            getFilename()));
            }
            long grpID = obj.getDetails().getGroup().getId().getValue();
            client.setCurrentGroup(grpID);
            return obj;
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }
}
