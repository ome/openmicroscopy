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
package omero.gateway.model;

import java.util.Collection;

/**
 * Defines the scope of a search, i.e. in which fields to search (name, description, etc.);
 * See {@link SearchParameters}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */
public enum SearchScope {

    //TODO: Check if annotation would be enough for ANNOTATION enum
    NAME("name"), DESCRIPTION("description"), ANNOTATION("annotation, file.name, file.path, file.contents, file.format");

    private String stringRepresentation;

    /**
     * Creates a new scope.
     *
     * @param stringRepresentation The string to search for.
     */
    SearchScope(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    /**
     * Get a String representation of this scope which can be used in a
     * lucene search
     * @return
     */
    public String getStringRepresentation() {
        return stringRepresentation;
    }

    /**
     * Get a String representation of the given scopes which can be used in a
     * lucence search
     * @param scopes The scopes
     * @return See above
     */
    public static String getStringRepresentation(Collection<SearchScope> scopes) {
        StringBuilder result = new StringBuilder();

        for (SearchScope scope : scopes) {
            if (result.length() > 0)
                result.append(',');
            result.append(scope.getStringRepresentation());
        }

        return result.toString();
    }

}
