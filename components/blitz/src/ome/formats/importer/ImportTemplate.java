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

package ome.formats.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportTemplate
{
    private final static Logger log = LoggerFactory.getLogger(ImportTemplate.class);

    private String template;
    private String filename;
    private Boolean isSPW;

    private String user = null;
    private String group = null;
    private String project = null;
    private String dataset = null;
    private String screen = null;

    public ImportTemplate(ImportConfig config, ImportContainer ic) {
        this.template = config.template.get();
        this.filename = ic.getUsedFiles()[0];
        this.isSPW = ic.getIsSPW();

        parseTemplate();

        log.info("Resolved from template:");
        log.info("User    = " + user);
        log.info("Group   = " + group);
        log.info("Project = " + project);
        log.info("Dataset = " + dataset);
        log.info("Screen  = " + screen);
    }

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

}
