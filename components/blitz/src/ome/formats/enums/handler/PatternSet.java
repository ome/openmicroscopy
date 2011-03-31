/*
 * ome.formats.enums.CorrectionEnumHandler
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.enums.handler;

import java.util.regex.Pattern;

/**
 * A simple pattern set, which contains a pattern and an enumeration value.
 * Its real purpose is to provide a way for us to conveniently use an
 * array for definitions in enumeration handlers.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
class PatternSet
{
	/** Regular expression pattern. */
	public Pattern pattern;

	/** Value that should be used if the regular expression is matched. */
	public String value;

	public PatternSet(String pattern, String value)
	{
		this.pattern = Pattern.compile(pattern);
		this.value = value;
	}
}