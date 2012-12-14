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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 4.5
 */
public class MakePathComponentSafe implements StringTransformer {
	static final Map<Integer, Integer> transformationMatrix;
	static final Set<String> unsafePrefixes;
	static final Set<String> unsafeSuffixes;
	static final Set<String> unsafeNames;
	
	static final char safeCharacter = '_';
	
	private static int getCodePoint(char character) {
		final char[] singletonCharacterArray = new char[] {character};
		return Character.codePointAt(singletonCharacterArray, 0);
	}
	
	static {
		/* Some information on Windows naming strategies is to be found at
		 * http://msdn.microsoft.com/en-us/library/aa365247(VS.85).aspx */
		
		transformationMatrix = new HashMap<Integer, Integer>();
		final int underscore = getCodePoint(safeCharacter);
		for (int codePoint = 0; codePoint < 0x100; codePoint++)
			if (Character.getType(codePoint) == Character.CONTROL)
				transformationMatrix.put(codePoint, underscore);
		transformationMatrix.put(0x22, getCodePoint('\'')); // "
		transformationMatrix.put(0x2A, getCodePoint('x'));  // *
		transformationMatrix.put(0x2F, getCodePoint('!'));  // /
		transformationMatrix.put(0x3A, getCodePoint(';'));  // :
		transformationMatrix.put(0x3C, getCodePoint('['));  // <
		transformationMatrix.put(0x3E, getCodePoint(']'));  // >
		transformationMatrix.put(0x3F, getCodePoint('%'));  // ?
		transformationMatrix.put(0x5C, getCodePoint('!'));  // \
		transformationMatrix.put(0x7C, getCodePoint('!'));  // |
		
		unsafePrefixes = new HashSet<String>();
		unsafePrefixes.add(".");
		unsafePrefixes.add("$");
		
		unsafeSuffixes = new HashSet<String>();
		unsafeSuffixes.add(".");
		unsafeSuffixes.add(" ");
		
		unsafeNames = new HashSet<String>();
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
	}
	
	// @Override only since Java SE 6
	public String apply(String string) {
		final String ucString = string.toUpperCase();
		for (final String unsafeName : unsafeNames)
			if (ucString.equals(unsafeName))
				string = string + safeCharacter;
		for (final String unsafePrefix : unsafePrefixes)
			if (ucString.startsWith(unsafePrefix))
				string = safeCharacter + string;
		for (final String unsafeSuffix : unsafeSuffixes)
			if (ucString.endsWith(unsafeSuffix))
				string = string + safeCharacter;
		final int codePointCount = string.codePointCount(0, string.length());
		final int[] codePointArray = new int[codePointCount];
		for (int codePointIndex = 0; codePointIndex < codePointCount; codePointIndex++) {
			final int originalCodePoint = string.codePointAt(string.offsetByCodePoints(0, codePointIndex));
			final Integer substituteCodePoint = transformationMatrix.get(originalCodePoint);
			if (substituteCodePoint == null)
				codePointArray[codePointIndex] = originalCodePoint;
			else
				codePointArray[codePointIndex] = substituteCodePoint;
		}
		return new String(codePointArray, 0, codePointCount);
	}
}
