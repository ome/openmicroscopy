/*
 * Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
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

import com.google.common.base.Function;

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public class MakePathComponentSafe implements Function<String, String> {
    protected final FilePathRestrictions rules;

    /**
     * @param rules the rules to apply in making path components safe
     */
    public MakePathComponentSafe(FilePathRestrictions... rules) {
        this.rules = FilePathRestrictions.combineFilePathRestrictions(rules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String apply(String string) {
        final String ucString = string.toUpperCase();
        for (final String unsafeName : rules.unsafeNames)
            if (ucString.equals(unsafeName))
                string = string + rules.safeCharacter;
        for (final String unsafePrefix : rules.unsafePrefixes)
            if (ucString.startsWith(unsafePrefix))
                string = rules.safeCharacter + string;
        for (final String unsafeSuffix : rules.unsafeSuffixes)
            if (ucString.endsWith(unsafeSuffix))
                string = string + rules.safeCharacter;
        final int codePointCount = string.codePointCount(0, string.length());
        final int[] codePointArray = new int[codePointCount];
        for (int codePointIndex = 0; codePointIndex < codePointCount; codePointIndex++) {
            final int originalCodePoint = string.codePointAt(string.offsetByCodePoints(0, codePointIndex));
            final Integer substituteCodePoint = rules.transformationMap.get(originalCodePoint);
            if (substituteCodePoint == null)
                codePointArray[codePointIndex] = originalCodePoint;
            else
                codePointArray[codePointIndex] = substituteCodePoint;
        }
        return new String(codePointArray, 0, codePointCount);
    }
}
