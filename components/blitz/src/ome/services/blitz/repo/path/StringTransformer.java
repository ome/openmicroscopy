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

/**
 * Transform a string to another string. 
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0
 */
public interface StringTransformer {    
    /* Could have used com.google.common.base.Function
     * but will be superseded by Java 8 lambdas anyway./
     
    /**
     * Transform the given string. Thread-safe.
     * @param a string, not null
     * @return the transformed string, not null
     */
    public String apply(String from);
}
