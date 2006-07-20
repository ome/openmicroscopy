/*
 * ome.system.Version
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.system;

//Java imports
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

//Third-party libraries

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.InternalException;

/** static utililty for checking Omero classes for revision number and date.
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see 	RevisionDate
 * @see     RevisionNumber
 * @see     SimpleDateFormat
 * @since   3.0-M3
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public abstract class Version {

	/** date format used by Subversions "Date" keyword. This can be used
	 * as the constructor argument to a {@link SimpleDateFormat} in order
	 * to parse {@link RevisionDate} values. 
	 */ 
	public final static String SVN_DATE_FORMAT = 
		"yyyy-MM-dd hh:mm:ss Z (EEE, dd MMM yyyy)";
	
	/** formatter for SVN date strings. Formatters are not synchronized, and
	 * therefore this instance is volatile */
	 private static volatile SimpleDateFormat formatter
     = new SimpleDateFormat (SVN_DATE_FORMAT);
	
	/** parse the given class for its {@link RevisionNumber} annotation. A null
	 * class argument is tolerated and returns a null {@link Integer}. Otherwise,
	 * the {@link String} contained in {@link RevisionNumber#value()} will be 
	 * parsed as an Integer after processing by {@link #stringFromSvnString(String)}
	 * if possible. Otherwise a null is returned.
	 * 
	 * @return Integer value of {@link RevisionNumber#value()}
	 */ 
	public static Integer getRevision( Class klass )
	{
		if ( klass == null ) return null;
		RevisionNumber rev = (RevisionNumber)
		klass.getAnnotation(RevisionNumber.class);
		if ( rev != null && rev.value() != null )
		{
			String sRevision = rev.value();
			sRevision = stringFromSvnString(sRevision);
			return Integer.valueOf(sRevision);
		}
		return null;

	}

	/** parse the given class for its {@link RevisionDate} annotation. A null
	 * class argument is tolerated and returns a null {@link Date}. Otherwise,
	 * the {@link String} contained in {@link RevisionDate#value()} will be 
	 * parsed as an Date after processing by {@link #stringFromSvnString(String)}
	 * and a {@link SimpleDateFormat} configured with {@link #SVN_DATE_FORMAT}
	 * if possible. Otherwise a null is returned.
	 * 
	 * @return Date value of {@link RevisionDate#value()}
	 */ 
	public static Date getDate( Class klass )
	{
		if ( klass == null ) return null;
		RevisionDate rev = (RevisionDate)
		klass.getAnnotation(RevisionDate.class);
		if ( rev != null && rev.value() != null )
		{
			String sDate = rev.value();
			sDate = stringFromSvnString(sDate);
			Date date;
			try {
				date = formatter.parse(sDate);
			} catch (ParseException e) {
				throw new InternalException(String.format(
						"Failed to parse date %s with formatter %s",
						sDate, formatter));
			}
			return date;
		}
		return null;
	}
	
	/** parse the given {@link String} to remove the leading "$", keyword name,
	 * and colons. This assumes that Subversion keywords are formatted such that
	 * the first space and the last space in the String, directly surround the
	 * value of interest. If this does not hold or if the argument is null,
	 * a null is returned.
	 */
	public static String stringFromSvnString( String keyword )
	{
		if (keyword == null)
		{
			return null;
		}
		int begin = keyword.indexOf(" ") + 1; 
 	 	int end = keyword.lastIndexOf(" "); 
 	 	
 	 	if ( begin < 0 || end < 0 || end - begin < 1 )
 	 	{
 	 		return null;
 	 	}
 	 	
 	 	String s = keyword.substring(begin, end); 
 	 	return s;
	}

}
