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

package ome.services.blitz.repo.path;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import omero.FilePathNamingException;

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public class FilePathNamingValidator {
    protected final FilePathRestrictions rules;

    /**
     * @param rules the rules to apply in validating file naming
     */
    public FilePathNamingValidator(FilePathRestrictions... rules) {
        this.rules = FilePathRestrictions.combineFilePathRestrictions(rules);
    }

    /**
     * Validate the given FS file.
     * @param fsFile the FS file
     * @throws FilePathNamingException if the FS file is invalidly named
     */
    public void validateFilePathNaming(FsFile fsFile) throws FilePathNamingException {
        final SortedSet<Integer> illegalCodePoints = new TreeSet<Integer>();
        final SortedSet<String> illegalPrefixes = new TreeSet<String>();
        final SortedSet<String> illegalSuffixes = new TreeSet<String>();
        final SortedSet<String> illegalNames = new TreeSet<String>();

        for (final String string : fsFile.getComponents()) {
            final String ucString = string.toUpperCase();
            for (final String unsafeName : rules.unsafeNames)
                if (ucString.equals(unsafeName)) {
                    illegalNames.add(unsafeName);
                }
            for (final String unsafePrefix : rules.unsafePrefixes)
                if (ucString.startsWith(unsafePrefix)) {
                    illegalPrefixes.add(unsafePrefix);
                }
            for (final String unsafeSuffix : rules.unsafeSuffixes)
                if (ucString.endsWith(unsafeSuffix)) {
                    illegalSuffixes.add(unsafeSuffix);
                }
            final int codePointCount = string.codePointCount(0, string.length());
            for (int codePointIndex = 0; codePointIndex < codePointCount; codePointIndex++) {
                final int codePoint = string.codePointAt(string.offsetByCodePoints(0, codePointIndex));
                if (rules.transformationMatrix.containsKey(codePoint)) {
                    illegalCodePoints.add(codePoint);
                }
            }
        }

        if (!(illegalCodePoints.isEmpty() && illegalPrefixes.isEmpty() && illegalSuffixes.isEmpty() && illegalNames.isEmpty())) {
            throw new FilePathNamingException(null, null, "illegal file path", fsFile.toString(),
                    new ArrayList<Integer>(illegalCodePoints), new ArrayList<String>(illegalPrefixes),
                    new ArrayList<String>(illegalSuffixes), new ArrayList<String>(illegalNames));
        }
    }

    /**
     * Validate the given file path.
     * @param filePath the file path
     * @throws FilePathNamingException if the file path is invalidly named
     */
    public void validateFilePathNaming(String filePath) throws FilePathNamingException {
        validateFilePathNaming(new FsFile(filePath));
    }
}
