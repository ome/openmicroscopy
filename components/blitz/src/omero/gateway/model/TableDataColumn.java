/*
 * Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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
package omero.gateway.model;

/**
 * Defines a column for a {@link TableData} object
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TableDataColumn {

    /** The header */
    private String name = "";

    /** A description */
    private String description = "";

    /** The index of the column */
    private int index = -1;

    /** The type of data in this column */
    private Class<?> type = null;

    /**
     * Creates a new instance
     * 
     * @param name
     *            The header
     * @param index
     *            The index
     * @param type
     *            The type of data in this column
     */
    public TableDataColumn(String name, int index, Class<?> type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }

    /**
     * Creates a new instance
     * 
     * @param name
     *            The header
     * @param description
     *            A description
     * @param index
     *            The index
     * @param type
     *            The type of data in this column
     */
    public TableDataColumn(String name, String description, int index,
            Class<?> type) {
        this.name = name;
        this.description = description;
        this.index = index;
        this.type = type;
    }

    /**
     * Get the header
     * 
     * @return See above
     */
    public String getName() {
        return name;
    }

    /**
     * Set the header
     * 
     * @param name
     *            The header
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the description
     * 
     * @return See description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the description
     * 
     * @param description
     *            The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the index
     * 
     * @return See above
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the index
     * 
     * @param index
     *            The index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Get the data type
     * 
     * @return See above
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Set the data type
     * 
     * @param type
     *            The data type
     */
    public void setType(Class<?> type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result + index;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((type == null) ? 0 : type.getCanonicalName().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TableDataColumn other = (TableDataColumn) obj;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (index != other.index)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.getCanonicalName().equals(
                other.type.getCanonicalName()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        String s = name;
        if (description.trim().length() > 0)
            s += " '" + description + "'";

        if (index > -1) {
            s += " (" + index + ")";
        }
        s += " [" + type.getSimpleName() + "]";
        return s;
    }

}
