/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.config;


//Java imports
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * Holds the configuration information for an <i>AddOn</i> tag in the
 * container's configuration file.
 * The content of each tag is stored by a member field.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class AddOnInfo {

    /** The value of the <code>name</code> tag. */
    private String name;

    /** The value of the <code>description</code> tag. */
    private String description;

    /** The value of the <code>download</code> tag. */
    private String download;

    /** The value of the <code>script</code> tag. */
    private List<String> scripts;

    /**
     * Sets the name.
     *
     * @param name The value to set.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Sets the description.
     *
     * @param description The value to set.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * Sets the download.
     *
     * @param download The value to set.
     */
    public void setDownload(String download)
    {
        this.download = download;
    }

    /**
     * Sets the scripts.
     *
     * @param scripts The value to set.
     */
    public void setScripts(String script)
    {
        if (StringUtils.isBlank(script)) return;
        String[] values = script.split(",");
        scripts = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            if (StringUtils.isNotBlank(values[i])) {
                scripts.add(values[i].trim());
            }
        }
    }

    /**
     * Returns the name of the application.
     *
     * @return See above.
     */
    public String getName() { return name; }

    /**
     * Returns the description of the application.
     *
     * @return See above.
     */
    public String getDescription() { return description; }

    /**
     * Returns the list of scripts if any.
     *
     * @return See above.
     */
    public List<String> getScripts() { return scripts; }


}
