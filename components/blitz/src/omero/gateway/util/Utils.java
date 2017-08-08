/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015-2017 University of Dundee. All rights reserved.
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

package omero.gateway.util;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.PropertyUtils;

import omero.RString;
import omero.model.IObject;

/**
 * Collection of methods used to set the name of files to import and to handle enumerations.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class Utils {

    /**
     * Returns the name as container if option is on.
     *
     * @return See above.
     */
    public static String getFolderAsContainerName(File file) {
        if (file.isFile()) {
            File parentFile = file.getParentFile();
            if (parentFile == null)
                return null;
            return parentFile.getName();
        }
        return file.getName();
    }
    
    /**
     * Returns the name to display for a file.
     * 
     * @param fullPath
     *            The file's absolute path.
     * @param number
     *            The number of folder to set the name.
     * @return See above.
     */
    public static String getDisplayedFileName(String fullPath, Integer number) {
        if (fullPath == null)
            return fullPath;
        if (number == null || number.intValue() < 0)
            return fullPath;
        String[] l = splitString(fullPath);
        String extension = null;
        if (fullPath.endsWith("\\"))
            extension = "\\";
        else if (fullPath.endsWith("/"))
            extension = "/";
        String start = null;
        if (fullPath.startsWith("\\"))
            start = "\\";
        else if (fullPath.startsWith("/"))
            start = "/";
        String sep = getStringSeparator(fullPath);
        if (sep == null)
            sep = "";
        String text = "";
        int folder = -1;
        if (number != null && number >= 0)
            folder = (Integer) number;
        if (folder == -1)
            return null;
        if (l != null && l.length > 1) {
            int n = 0;
            if (folder < l.length)
                n = l.length - folder - 2;
            if (n < 0)
                n = 0;
            int m = l.length - 1;
            for (int i = l.length - 1; i > n; i--) {
                if (i == m)
                    text = l[i];
                else
                    text = l[i] + sep + text;
            }
            if (n == 0 && start != null)
                text = start + text;
            if (extension != null)
                text = text + extension;
            return text;
        }
        return null;
    }

    /**
     * Returns the partial name of the image's name
     * 
     * @param originalName
     *            The original name.
     * @return See above.
     */
    public static String[] splitString(String originalName) {
        String[] l = null;
        if (originalName == null)
            return l;
        if (Pattern.compile("/").matcher(originalName).find()) {
            l = originalName.split("/", 0);
        } else if (Pattern.compile("\\\\").matcher(originalName).find()) {
            l = originalName.split("\\\\", 0);
        }
        return l;
    }

    /**
     * Returns the separator or <code>null</code>.
     * 
     * @param originalName
     *            The original name.
     * @return See above.
     */
    public static String getStringSeparator(String originalName) {
        if (originalName == null)
            return null;
        String[] l = null;
        if (Pattern.compile("/").matcher(originalName).find()) {
            l = originalName.split("/", 0);
            if (l.length > 0)
                return "/";
        } else if (Pattern.compile("\\\\").matcher(originalName).find()) {
            l = originalName.split("\\\\", 0);
            if (l.length > 0)
                return "\\";
        }
        return null;
    }

    /**
     * Throws if the given class is clearly not an enumeration in the OMERO model.
     * There is no {@code omero.model.IEnum} analog of {@link ome.model.IEnum}.
     * @param enumClass a type that should be an enumeration
     */
    private static void assertEnumType(Class<? extends IObject> enumClass) {
        if ("omero.model".equals(enumClass.getPackage().getName())) {
            for (final PropertyDescriptor descriptor : PropertyUtils.getPropertyDescriptors(enumClass)) {
                if ("value".equals(descriptor.getName()) && descriptor.getPropertyType() == omero.RString.class) {
                    return;
                }
            }
        }
        throw new IllegalArgumentException("requires OMERO.blitz API enumeration type, not " + enumClass.getName());
    }

    /**
     * Convert a list of {@code String} values to their corresponding {@code omero.model.enums} instances.
     * @param enumClass the desired enumeration type, never {@code null}
     * @param instanceClass the type whose no-argument constructor is used to provide instances of {@code enumClass}
     * @param enumValues values of the enumeration type, never {@code null} nor with a {@code null} value
     * @return enumeration instances of the given type with the given values
     * @throws ReflectiveOperationException if the instances could not be created
     */
    public static <E extends IObject, I extends E> List<E> toEnum(Class<E> enumClass, Class<I> instanceClass,
            Collection<String> enumValues) throws ReflectiveOperationException {
        assertEnumType(enumClass);
        final List<E> enumInstances = new ArrayList<>(enumValues.size());
        for (final String enumValue : enumValues) {
            final E enumInstance = instanceClass.newInstance();
            PropertyUtils.setProperty(enumInstance, "value", omero.rtypes.rstring(enumValue));
            enumInstances.add(enumInstance);
        }
        return enumInstances;
    }

    /**
     * Convert a list of {@code omero.model.enums} instances to their corresponding {@code String} values.
     * @param enumInstances enumeration instances, never {@code null} nor with a {@code null} value
     * @return values of the given instances, never {@code null}
     * @throws ReflectiveOperationException if the values could not be determined
     */
    public static <E extends IObject> List<String> fromEnum(Collection<E> enumInstances) throws ReflectiveOperationException {
        final List<String> enumValues = new ArrayList<>(enumInstances.size());
        for (final IObject enumInstance : enumInstances) {
            assertEnumType(enumInstance.getClass());
            final RString enumValue = (RString) PropertyUtils.getProperty(enumInstance, "value");
            enumValues.add(enumValue.getValue());
        }
        return enumValues;
    }
}
