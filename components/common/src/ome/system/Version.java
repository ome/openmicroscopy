/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.system;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.conditions.InternalException;

/**
 * static utililty for checking Omero classes for revision number and date.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date: 2006-12-15 09:44:52 +0100 (Fri, 15 Dec
 *          2006) $
 * @see RevisionDate
 * @see RevisionNumber
 * @see SimpleDateFormat
 * @since 3.0-M3
 */
@RevisionDate("$Date: 1970-01-01 00:00:01 +0100 (Thu, 01 Jan 1970) $")
@RevisionNumber("$Revision: 2500 $")
public abstract class Version {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("omero");

    /**
     * Current OMERO version. This is the value under which you can retrieve the
     * code from subversion as well as what will appear in the server console
     * upon startup.
     */
    public final static String OMERO = RESOURCE_BUNDLE.getString("omero.version"); //$NON-NLS-1$

    /**
     * Date format used by Subversions "Date" keyword. This can be used as the
     * constructor argument to a {@link SimpleDateFormat} in order to parse
     * {@link RevisionDate} values.
     */
    public final static String SVN_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss Z (EEE, dd MMM yyyy)";

    /**
     * Formatter for SVN date strings. Formatters are not synchronized, and
     * therefore this instance is volatile
     */
    private static volatile SimpleDateFormat formatter = new SimpleDateFormat(
            SVN_DATE_FORMAT);

    /**
     * Parses the given class for its {@link RevisionNumber} annotation. A null
     * class argument is tolerated and returns a null {@link Integer}.
     * Otherwise, the {@link String} contained in {@link RevisionNumber#value()}
     * will be parsed as an Integer after processing by
     * {@link #stringFromSvnString(String)} if possible. Otherwise a null is
     * returned.
     * 
     * @return Integer value of {@link RevisionNumber#value()}
     */
    public static Integer getRevision(Class klass) {
        if (klass == null) {
            return null;
        }
        RevisionNumber rev = (RevisionNumber) klass
                .getAnnotation(RevisionNumber.class);
        if (rev != null && rev.value() != null) {
            String sRevision = rev.value();
            sRevision = stringFromSvnString(sRevision);
            return Integer.valueOf(sRevision);
        }
        return null;

    }

    /**
     * Parses the given class for its {@link RevisionDate} annotation. A null
     * class argument is tolerated and returns a null {@link Date}. Otherwise,
     * the {@link String} contained in {@link RevisionDate#value()} will be
     * parsed as an Date after processing by
     * {@link #stringFromSvnString(String)} and a {@link SimpleDateFormat}
     * configured with {@link #SVN_DATE_FORMAT} if possible. Otherwise a null is
     * returned.
     * 
     * @return Date value of {@link RevisionDate#value()}
     */
    public static Date getDate(Class klass) {
        if (klass == null) {
            return null;
        }
        RevisionDate rev = (RevisionDate) klass
                .getAnnotation(RevisionDate.class);
        if (rev != null && rev.value() != null) {
            String sDate = rev.value();
            sDate = stringFromSvnString(sDate);
            Date date;
            try {
                date = formatter.parse(sDate);
            } catch (ParseException e) {
                throw new InternalException(String.format(
                        "Failed to parse date %s with formatter %s", sDate,
                        formatter));
            }
            return date;
        }
        return null;
    }

    /**
     * Parses the given {@link String} to remove the leading "$", keyword name,
     * and colons. This assumes that Subversion keywords are formatted such that
     * the first space and the last space in the String, directly surround the
     * value of interest. If this does not hold or if the argument is null, a
     * null is returned.
     */
    public static String stringFromSvnString(String keyword) {
        if (keyword == null) {
            return null;
        }
        int begin = keyword.indexOf(" ") + 1;
        int end = keyword.lastIndexOf(" ");

        if (begin < 0 || end < 0 || end - begin < 1) {
            return null;
        }

        String s = keyword.substring(begin, end);
        return s;
    }

}
