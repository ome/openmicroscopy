/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.util.search;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for building lucene queries<br>
 * <br>
 * Example:<br>
 * Fields to search for: 'name', 'description'<br>
 * Input query: a b AND c AND d f<br>
 * <br>
 * will be transformed to this lucene expression:<br>
 * name:a description:a name:f description:f ((name:b description:b) AND (name:c
 * description:c) AND (name:d description:d)) <br>
 * <br>
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * 
 * @since 5.0
 * 
 * TODO: For versions > 5.0 this class can be moved from commons to the server
 */
public class LuceneQueryBuilder {

    private static final DateFormat DATEFORMAT = new SimpleDateFormat(
            "yyyyMMdd");

    public static final String DATE_IMPORT = "details.creationEvent.time";

    public static final String DATE_ACQUISITION = "acquisitionDate";

    /** Wild cards we support */
    private static final List<String> WILD_CARDS = new ArrayList<String>();
    static {
        WILD_CARDS.add("*");
        WILD_CARDS.add("?");
        WILD_CARDS.add("~");
    }

    /** Punctuation which will not be stripped */
    private static final List<String> NO_BREAK = new ArrayList<String>();
    static {
        NO_BREAK.add("_");
        NO_BREAK.add("-");
        NO_BREAK.add(":");
    }

    /**
     * Builds a query with the provided input terms over the given fields
     * 
     * @param fields
     * @param input
     * @return the query
     * @throws InvalidQueryException
     */
    public static String buildLuceneQuery(List<String> fields, Date from,
            Date to, String dateType, String input)
            throws InvalidQueryException {
        StringBuilder result = new StringBuilder();

        String basicQuery = buildLuceneQuery(fields, input);

        if (from == null && to == null)
            return basicQuery;

        if (basicQuery!=null && basicQuery.trim().length()>0)
            result.append("(" + basicQuery + ")");
        else
            result.append(basicQuery);

        // Lucence date range TO is exclusive, so have to add a day to it
        String dateFrom = beginOfTime();
        String dateTo = tomorrow();
        if (from != null)
            dateFrom = DATEFORMAT.format(from);
        if (to != null) {
            dateTo = DATEFORMAT.format(addOneDay(to));
        }

        if (result.length() > 0)
            result.append(" AND " + dateType + ":[" + dateFrom + " TO "
                    + dateTo + "]");
        else
            result.append(dateType + ":[" + dateFrom + " TO " + dateTo + "]");

        return result.toString();
    }

    /**
     * Builds a query with the provided input terms over the given fields
     * 
     * @param fields
     * @param input
     * @return the query
     * @throws InvalidQueryException
     */
    public static String buildLuceneQuery(List<String> fields, String input)
            throws InvalidQueryException {
        StringBuilder result = new StringBuilder();

        input = replaceNonAlphaNummeric(input);

        List<String> terms = split(input);

        if (fields!=null && !fields.isEmpty()) {
            terms = attachFields(fields, terms);
        }

        terms = assembleAndClauses(terms);

        for (String term : terms) {
            if (result.length() > 0)
                result.append(" ");
            result.append(term);
        }

        return result.toString().trim();
    }

    /**
     * Attaches the field names to the different terms;
     * 
     * @param fields
     * @param terms
     * @return
     */
    private static List<String> attachFields(List<String> fields,
            List<String> terms) {
        List<String> result = new ArrayList<String>();
        for (String term : terms) {
            if (term.equals("AND")) {
                result.add(term);
                continue;
            }

            if((term.indexOf(':')) == -1) {
                // only add fields, if the term is not already in form 'foo:bar'
                // (i. e. it is not a MapAnnotation specific search term)
                String newTerm = "";
                for (String field : fields) {
                    if (newTerm.length() > 0)
                        newTerm += " ";
                    
                    newTerm += field + ":" + term;
                }
                result.add(newTerm);
            }
            else {
                result.add(term);
            }
            
        }
        return result;
    }

    /**
     * Replaces non alpha-numeric characters (excluding underscore) with spaces
     * (which act like OR); will not replace any characters within quotes.
     * 
     * @param s
     * @return
     */
    private static String replaceNonAlphaNummeric(String s) {

        char[] result = new char[s.length()];

        boolean insideQuotes = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') {
                insideQuotes = !insideQuotes;
                result[i] = c;
                continue;
            }
            if (!insideQuotes && !Character.isLetterOrDigit(c)
                    && !WILD_CARDS.contains("" + c)
                    && !NO_BREAK.contains("" + c))
                result[i] = ' ';
            else
                result[i] = c;
        }

        return new String(result);
    }

    /**
     * Checks if a String just contains a wildcard character only
     * 
     * @param s
     * @return
     */
    private static boolean isWildcardOnly(String s) {
        return s.matches("[\\*\\?\\~]+");
    }

    /**
     * Reassembles the AND expressions, i. e. creates the single term "a and b"
     * from the terms "a" "and" "b"
     * 
     * @param terms
     * @return
     */
    private static List<String> assembleAndClauses(List<String> terms)
            throws InvalidQueryException {
        List<String> result = new ArrayList<String>();

        if (terms==null || terms.isEmpty())
            return Collections.emptyList();

        if (terms.size() == 1) {
            if (terms.get(0).equals("AND"))
                throw new InvalidQueryException(
                        "AND expression must be followed by a search term!");
            else
                return Collections.singletonList(terms.get(0));
        }

        // the AND terms gathered by now
        List<String> andTerms = new ArrayList<String>();

        // flag to indicate that the current term is part of an AND expression
        boolean withinAndTerm = false;

        for (int i = 0; i < terms.size(); i++) {
            String term = terms.get(i);
            if (i < terms.size() - 1) {
                String next = terms.get(i + 1);
                if (next.equals("AND")) {
                    // if next term is AND put this term to the end of the and
                    // terms list and indicate that we are within an AND
                    // expression
                    andTerms.add(term);
                    withinAndTerm = true;
                    i++;
                } else {
                    if (withinAndTerm) {
                        // if we're still within the AND expression put it to
                        // the end list and indicate that the end of this AND
                        // expression is reached
                        andTerms.add(term);
                        withinAndTerm = false;
                    } else {
                        // end of AND reached or there was no AND expression at
                        // all
                        if (!andTerms.isEmpty()) {
                            // if there was one, built the expression
                            result.add(concatenateAndTerms(andTerms));
                            andTerms.clear();
                        }
                        result.add(term);
                    }
                }
            } else {
                // we reached the last search term
                if (withinAndTerm) {
                    andTerms.add(term);
                    withinAndTerm = false;
                } else {
                    if (!term.equals("AND"))
                        result.add(term);
                    else
                        throw new InvalidQueryException(
                                "AND expression must be followed by a search term!");
                }
            }
        }

        if (!andTerms.isEmpty())
            result.add(concatenateAndTerms(andTerms));

        return result;
    }

    /**
     * Just concatenates the Strings separated by AND
     * 
     * @param terms
     * @return
     */
    private static String concatenateAndTerms(List<String> terms) {
        String result = "(";
        for (String t : terms) {
            if (result.length() > 1)
                result += " AND ";
            result += "(" + t + ")";
        }
        return result + ")";
    }

    /**
     * Splits input string by whitespaces, taking quotes into account
     * 
     * @param input
     * @return
     */
    private static List<String> split(String input) {
        final String regex = "\"([^\"]*)\"|(\\S+)";

        List<String> result = new ArrayList<String>();

        Matcher m = Pattern.compile(regex).matcher(input);
        while (m.find()) {
            String s = m.group(1);
            if (s != null) {
                // don't touch quoted terms
                result.add("\"" + s.trim() + "\"");
            } else {
                s = m.group(2);
                if (!isWildcardOnly(s))
                    result.add(s.trim());
            }
        }

        return result;
    }

    /**
     * Get tomorrow's date
     * 
     * @return
     */
    private static String tomorrow() {
        return DATEFORMAT.format(addOneDay(new Date()));
    }

    /**
     * Get the earliest possible date
     * 
     * @return
     */
    private static String beginOfTime() {
        return DATEFORMAT.format(new Date(0));
    }

    /**
     * Adds a day to a given date
     * 
     * @param date
     * @return
     */
    private static Date addOneDay(Date date) {
        Calendar tmp = Calendar.getInstance();
        tmp.setTime(date);
        tmp.add(Calendar.DAY_OF_MONTH, 1);
        return tmp.getTime();
    }

}
