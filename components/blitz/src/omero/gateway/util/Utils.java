/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
package omero.gateway.util;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Collection of methods used to set the name of files to import.
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

}
