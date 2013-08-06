/*
 * Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.keywords;

/**
 * Robot Framework SwingLibrary keyword library offering access to Java static fields.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.4.9
 */
public class StaticFieldLibrary
{
    /** Allow Robot Framework to instantiate this library only once. */
    public static final String ROBOT_LIBRARY_SCOPE = "GLOBAL";

    /** Automatic prefix for all given class names. */
    public static final String PREFIX = "org.openmicroscopy.shoola.";

    /**
     * <table>
     *   <td>Get Java String</td>
     *   <td>class.field</td>
     * </table>
     * @param classAndFieldName the class and static field name,
     * e.g., <code>keywords.StaticFieldLibrary.PREFIX</code>
     * @return the value of the field as a String
     * @throws ClassNotFoundException if the named class could not be found
     * @throws IllegalAccessException if the named field is not accessible
     * @throws NoSuchFieldException if the named class does not have the named field
     * @throws NullPointerException if the field value is <code>null</code>
     */
    public String getJavaString(String classAndFieldName)
    throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        final int lastPeriod = classAndFieldName.lastIndexOf('.');
        final String className = PREFIX + classAndFieldName.substring(0, lastPeriod);
        final String fieldName = classAndFieldName.substring(lastPeriod + 1);
        return Class.forName(className).getField(fieldName).get(null).toString();
    }
}
