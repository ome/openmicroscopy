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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * Capture a set of rules by which local files may not be named on the file-system.
 * 
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public class FilePathRestrictions {
    /** the conservative rules that should work across many likely file-systems */
    public static final FilePathRestrictions CONSERVATIVE_RULES;

    /* the full rules */
    public final ImmutableSetMultimap<Integer, Integer> transformationMatrix;  /* values never empty */
    public final ImmutableSet<String> unsafePrefixes;
    public final ImmutableSet<String> unsafeSuffixes;
    public final ImmutableSet<String> unsafeNames;
    public final ImmutableSet<Character> safeCharacters;  /* never empty */

    /* quick lookups to characters that satisfy the above rules */
    public final char safeCharacter;
    public final ImmutableMap<Integer, Integer> transformationMap;

    /**
     * Convert the given character to the corresponding Unicode code point.
     * @param character a character
     * @return the character's code point
     */
    private static int getCodePoint(char character) {
        final char[] singletonCharacterArray = new char[] {character};
        return Character.codePointAt(singletonCharacterArray, 0);
    }

    static {
        /* Some information on Windows naming strategies is to be found at
         * http://msdn.microsoft.com/en-us/library/aa365247(VS.85).aspx */

        final Set<Character> safeCharacters = new HashSet<Character>();
        safeCharacters.add('_');

        final SetMultimap<Integer, Integer> transformationMatrix = HashMultimap.create();
        final int safeCodePoint = getCodePoint(safeCharacters.iterator().next());
        for (int codePoint = 0; codePoint < 0x100; codePoint++)
            if (Character.getType(codePoint) == Character.CONTROL)
                transformationMatrix.put(codePoint, safeCodePoint);
        transformationMatrix.put(0x22, getCodePoint('\'')); // "
        transformationMatrix.put(0x2A, getCodePoint('x'));  // *
        transformationMatrix.put(0x2F, getCodePoint('!'));  // /
        transformationMatrix.put(0x3A, getCodePoint(';'));  // :
        transformationMatrix.put(0x3C, getCodePoint('['));  // <
        transformationMatrix.put(0x3E, getCodePoint(']'));  // >
        transformationMatrix.put(0x3F, getCodePoint('%'));  // ?
        transformationMatrix.put(0x5C, getCodePoint('!'));  // \
        transformationMatrix.put(0x7C, getCodePoint('!'));  // |

        final Set<String> unsafePrefixes = new HashSet<String>();
        unsafePrefixes.add("$");

        final Set<String> unsafeSuffixes = new HashSet<String>();
        unsafeSuffixes.add(".");
        unsafeSuffixes.add(" ");

        final Set<String> unsafeNames = new HashSet<String>();
        unsafeNames.add("AUX");
        unsafeNames.add("CLOCK$");
        unsafeNames.add("CON");
        unsafeNames.add("NUL");
        unsafeNames.add("PRN");
        for (char number = '0'; number <= '9'; number++) {
            unsafeNames.add("COM" + number);
            unsafeNames.add("LPT" + number);
        }
        
        for (final String unsafeName : unsafeNames)
            unsafePrefixes.add(unsafeName + ".");

        CONSERVATIVE_RULES = new FilePathRestrictions(transformationMatrix, unsafePrefixes, unsafeSuffixes, unsafeNames, safeCharacters);
    }

    /**
     * Combine sets of rules to form a set that satisfies both.
     * @param rules at least one set of rules
     * @return the intersection of the given rules
     */
    public static FilePathRestrictions combine(FilePathRestrictions... rules) {
        if (rules.length == 0) {
            throw new IllegalArgumentException("cannot combine an empty list of rules");
        }

        int index = 0;
        FilePathRestrictions product = rules[index++];

        while (index < rules.length) {
            final FilePathRestrictions toCombine = rules[index++];

            final Set<Character> safeCharacters = Sets.intersection(product.safeCharacters, toCombine.safeCharacters);

            if (safeCharacters.isEmpty()) {
                throw new IllegalArgumentException("cannot combine safe characters");
            }

            final Set<Integer> allKeys = Sets.union(product.transformationMatrix.keySet(), toCombine.transformationMatrix.keySet());
            final ImmutableMap<Integer, Collection<Integer>> productMatrixMap = product.transformationMatrix.asMap();
            final ImmutableMap<Integer, Collection<Integer>> toCombineMatrixMap = toCombine.transformationMatrix.asMap();
            final SetMultimap<Integer, Integer> newTransformationMatrix = HashMultimap.create();

            for (final Integer key : allKeys) {
                final Collection<Integer> values;
                if (!productMatrixMap.containsKey(key)) {
                    values = toCombineMatrixMap.get(key);
                } else if (!toCombineMatrixMap.containsKey(key)) {
                    values = productMatrixMap.get(key);
                } else {
                    final Set<Integer> valuesSet = new HashSet<Integer>(productMatrixMap.get(key));
                    valuesSet.retainAll(toCombineMatrixMap.get(key));
                    if (valuesSet.isEmpty()) {
                        throw new IllegalArgumentException("cannot combine transformations for Unicode code point " + key);
                    }
                    values = valuesSet;
                }
                for (final Integer value : values) {
                    newTransformationMatrix.put(key, value);
                }
            }

            product = new FilePathRestrictions(newTransformationMatrix,
                    Sets.union(product.unsafePrefixes, toCombine.unsafePrefixes),
                    Sets.union(product.unsafePrefixes, toCombine.unsafePrefixes),
                    Sets.union(product.unsafePrefixes, toCombine.unsafePrefixes),
                    safeCharacters);
        }
        return product;
    }

    public FilePathRestrictions(SetMultimap<Integer, Integer> transformationMatrix,
            Set<String> unsafePrefixes, Set<String> unsafeSuffixes, Set<String> unsafeNames,
            Set<Character> safeCharacters) {
        this.transformationMatrix = ImmutableSetMultimap.copyOf(transformationMatrix);
        this.unsafePrefixes = ImmutableSet.copyOf(unsafePrefixes);
        this.unsafeSuffixes = ImmutableSet.copyOf(unsafeSuffixes);
        this.unsafeNames = ImmutableSet.copyOf(unsafeNames);
        this.safeCharacters = ImmutableSet.copyOf(safeCharacters);
        
        this.safeCharacter = safeCharacters.iterator().next();
        int safeCodePoint = getCodePoint(this.safeCharacter);
        final Builder<Integer, Integer> transformationMapBuilder = ImmutableMap.builder();
        for (final Entry<Integer, Collection<Integer>> transformation : transformationMatrix.asMap().entrySet()) {
            final Collection<Integer> values = transformation.getValue();
            final Integer selectedValue = values.contains(safeCodePoint) ? safeCodePoint : values.iterator().next();
            transformationMapBuilder.put(transformation.getKey(), selectedValue);
        }
        this.transformationMap = transformationMapBuilder.build();
    }
}
