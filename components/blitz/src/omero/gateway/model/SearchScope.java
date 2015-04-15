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

//Java imports

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public enum SearchScope {

    //TODO: Check if annotation would be enough for ANNOTATION enum
    NAME("name"), DESCRIPTION("description"), ANNOTATION("annotation, file.name, file.path, file.contents, file.format");
    
    private String stringRepresenation;
    
    SearchScope(String stringRepresenation) {
        this.stringRepresenation = stringRepresenation;
    }

    public String getStringRepresenation() {
        return stringRepresenation;
    }
    
    public static String getStringRepresenation(Collection<SearchScope> scopes) {
        StringBuilder result = new StringBuilder();

        for (SearchScope scope : scopes) {
            if (result.length() > 0)
                result.append(',');
            result.append(scope.getStringRepresenation());
        }

        return result.toString();
    }
}
