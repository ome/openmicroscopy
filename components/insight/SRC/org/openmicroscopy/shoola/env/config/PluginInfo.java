/*
 * org.openmicroscopy.shoola.env.config.PluginInfo
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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

/**
 * Hosts information about 3rd party application when the client is used
 * as a plugin of that application.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class PluginInfo
{

    /** Indicate that only one item from the dependencies list has to be found.*/
    public static final int OR = 0;

    /** Indicate that all items from the dependencies list have to be found.*/
    public static final int AND = 1;

    /** The information about the plugin e.g. where to download it */ 
    private String info;

    /** The list of dependencies separated by <code>,</code>. */ 
    private String dependencies;

    /** The directory hosting the dependencies.*/
    private String directory;

    /** The name of the plugin if set.*/
    private String name;

    /** The identifier of the plugin.*/
    private int id;

    /** The default conjunction value.*/
    private int conjunction;

    /**
     * Creates a new instance.
     *
     * @param id The identifier of the plugin.
     * @param dependencies The list of dependencies separated by <code>,</code>.
     * @param directory The directory hosting the dependencies.
     */
    public PluginInfo(String id, String dependencies, String directory)
    {
        if (id != null) {
            try {
                this.id = Integer.parseInt(id);
            } catch (Exception e) {
                this.id = -1;
            }
        }
        this.dependencies = dependencies;
        this.directory = directory;
        conjunction = OR;
    }

    /**
     * Sets the conjunction. The default value is <code>OR</code>.
     *
     * @param value The value to set.
     */
    public void setConjunction(String value)
    {
        if (value == null) conjunction = OR;
        else {
            String v = value.toLowerCase().trim();
            if (v.equals("and")) conjunction = AND;
            else if (v.equals("or")) conjunction = OR;
        }
    }

    /**
     * Returns the conjunction. The default value is <code>OR</code>.
     *
     * @return See above.
     */
    public int getConjunction() { return conjunction; }

    /**
     * Set the information about the plugin e.g. where to download it.
     *
     * @param info The value to set.
     */
    public void setInfo(String info) { this.info = info; }

    /**
     * Sets the name of the plugin.
     *
     * @param name The value to set
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the name of the plugin.
     *
     * @return See above.
     */
    public String getName() { return name; }

    /**
     * Returns the list of dependencies if any.
     *
     * @return See above.
     */
    public String getDependencies() { return dependencies; }

    /**
     * Formats the list of dependencies depending on the conjunction used.
     *
     * @return See above.
     */
    public String formatDependencies()
    {
        String[] values = getDependenciesAsArray();
        if (values == null || values.length == 0) return "";
        StringBuffer buf = new StringBuffer();
        String text = "";
        switch (conjunction) {
        case AND:
            text = " and ";
            break;
        case OR:
        default:
            text = " or ";
        }
        int n = values.length-1;
        int j = 0;
        for (int i = 0; i < values.length; i++) {
            buf.append(values[i]);
            if (j != n) buf.append(text);
            j++;
        }
        return buf.toString();
    }

    /**
     * Returns the first dependency if the conjunction is <code>or</code>
     * otherwise returns all the dependencies.
     *
     * @return See above.
     */
    public String getFirstDependency()
    {
        if (conjunction == AND) return formatDependencies();
        String[] values = getDependenciesAsArray();
        if (values == null || values.length == 0) return "";
        return values[0];
    }

    /**
     * Returns the dependencies as an array.
     *
     * @return See above.
     */
    public String[] getDependenciesAsArray()
    {
        if (dependencies == null) return null;
        return dependencies.split(",");
    }

    /**
     * Returns the information about the plugin e.g. where to download it.
     *
     * @return See above.
     */
    public String getInfo() { return info; }

    /**
     * Returns the directory where to check for the dependencies.
     *
     * @return See above.
     */
    public String getDirectory() { return directory; }

    /**
     * Returns the identifier of the plugin.
     *
     * @return See above.
     */
    public int getId() { return id; }

}
