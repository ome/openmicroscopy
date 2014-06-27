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

package org.openmicroscopy.shoola.util.ui.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;

/**
 * Utility class for building lucene queries<br>
 * <br>
 * Example:<br>
 * Fields to search for: 'name', 'description'<br>
 * Input query: a b AND c AND d f<br>
 * <br>
 * will be transformed to this lucene expression:<br>
 * name:a description:a (name:b description:b) AND (name:c description:c) AND (name:d description:d) name:f description:f
 * <br>
 * <br>
 * @author  Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class LuceneQueryBuilder {

    static List<String> WILD_CARDS = new ArrayList<String>();
    static {
        WILD_CARDS.add("*");
        WILD_CARDS.add("?");
        WILD_CARDS.add("~");
    }
    
    public static String buildLuceneQuery(List<String> fields, String input) throws InvalidQueryException {
        StringBuilder result = new StringBuilder();
        
        input = replaceCommasWithSpaces(input);
        
        List<String> terms = split(input);
        
        if(!CollectionUtils.isEmpty(fields)) {
            terms = attachFields(fields, terms);
        }
        
        terms = assembleAndClauses(terms);
        
        for(String term : terms) {
            if(result.length()>0)
                result.append(" ");
            result.append(term);
        }
        
        return result.toString().trim();
    }
    
    /**
     * Attaches the field names to the different terms;
     * if there are multiple fields the expressions are joined
     * by OR
     * @param fields
     * @param terms
     * @return
     */
    static List<String> attachFields(List<String> fields, List<String> terms) {
        List<String> result = new ArrayList<String>();
        for(String term : terms) {
            if(term.equals("AND")) {
                result.add("AND");
                continue;
            }
            
            String newTerm = "";
            for(String field : fields) {
                if(newTerm.length()>0)
                    newTerm += " ";
                newTerm += field+":"+term;
            }
            result.add(newTerm);
        }
        return result;
    }
    
    static String removeNonAlphaNummeric(String s) {
        return s.replaceAll("[^\\p{Alnum}&&[^\\*\\?\\~]]", "");
    }
    
    static boolean isWildcardOnly(String s) {
        return s.matches("[\\*\\?\\~]+");
    }
    
    /**
     * Reassembles the AND expressions, i. e. creates the single term "a and b"
     * from the terms "a" "and" "b"
     * @param terms
     * @return
     */
    static List<String> assembleAndClauses(List<String> terms) throws InvalidQueryException {
        List<String> result = new ArrayList<String>();
        
        if(CollectionUtils.isEmpty(terms))
            return Collections.emptyList();
        
        if(terms.size()==1) {
            if(terms.get(0).equals("AND"))
                throw new InvalidQueryException("AND expression must be followed by a search term!");
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
                if(next.equals("AND")) {
                    // if next term is AND put this term to the end of the and terms list
                    // and indicate that we are within an AND expression
                    andTerms.add(term);
                    withinAndTerm = true;
                    i++;
                }
                else {
                    if(withinAndTerm) {
                        // if we're still within the AND expression put it to the end list
                        // and indicate that the end of this AND expression is reached
                        andTerms.add(term);
                        withinAndTerm = false;
                    }
                    else {
                        // end of AND reached or there was no AND expression at all
                        if(!andTerms.isEmpty()) {
                            // if there was one, built the expression
                            result.add(concatenateAndTerms(andTerms));
                            andTerms.clear();
                        }
                        result.add(term);
                    }
                }
            } else {
                // we reached the last search term
                if(withinAndTerm) {
                    andTerms.add(term);
                    withinAndTerm = false;
                }
                else {
                    if(!term.equals("AND"))
                        result.add(term);
                    else
                        throw new InvalidQueryException("AND expression must be followed by a search term!");
                }
            }
        }
        
        if(!andTerms.isEmpty())
            result.add(concatenateAndTerms(andTerms));
        
        return result;
    }
    
    
    /**
     * Just concatenates the Strings separated by AND
     * @param terms
     * @return
     */
    private static String concatenateAndTerms(List<String> terms) {
        String result = "";
        for(String t : terms) {
            if(result.length()>0) 
                result += " AND ";
            result += "("+t+")";
        }
        return result;
    }
    
    /**
     * Replaces commas outside of quotes with spaces
     * @param s
     * @return
     */
    static String replaceCommasWithSpaces(String s) {
        char[] result = new char[s.length()];
        
        boolean insideQuotes = false;
        for(int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if(c=='"') {
                insideQuotes = !insideQuotes;
            }
            if(c==',')
                result[i] = ' ';
            else
                result[i] = c;
        }
        
        return new String(result);
    }
    
    /**
     * Splits input string by whitespaces, taking quotes into account
     * @param input
     * @return
     */
    private static List<String> split(String input) {
        final String regex = "\"([^\"]*)\"|(\\S+)";
        
        List<String> result = new ArrayList<String>();
        
        Matcher m = Pattern.compile(regex).matcher(input);
        while (m.find()) {
            String s = m.group(1);
            if(s!=null) {
                // don't touch quoted terms
                result.add("\""+s.trim()+"\"");
            }
            else {
                s = m.group(2);
                s = removeNonAlphaNummeric(s);
                if(!isWildcardOnly(s))
                    result.add(s.trim());
            }
        }
        
        return result;
    }
    
    public static void main(String... args) throws InvalidQueryException {
       //String test = " a_bc d.ef *xyz \"1 2 3\" AND \"4 5 6\" AND \"7 89\" zyx* \"asdf ljk\" 123,456 \"ab\",\"cd\", *, ghj ? wer, ab AND cd";
       // String test = "\"1 2 3\" AND \"4 5 6\" AND \"7 89\" ";
        String test = "a b AND c AND d f";
        List<String> fields = new ArrayList<String>();
        fields.add("name");
        fields.add("description");
        
        String q = buildLuceneQuery(fields, test);
        System.out.println(q);
    }
}
