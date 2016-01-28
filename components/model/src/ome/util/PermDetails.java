/*
 * Copyright (C) 2015 University of Dundee
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

package ome.util;

import java.util.Set;

import ome.conditions.ApiUsageException;
import ome.model.IObject;
import ome.model.internal.Details;
import ome.model.internal.GraphHolder;

import org.hibernate.Hibernate;

/**
 * wrapper class which can be used in HQL queries to properly load
 * the full context for a permissions object. Rather than writing a
 * query of the form:
 *
 * <code>
 * select d.details.permissions from Dataset d
 * </code>
 *
 * which returns a {@link ome.model.internal.Permissions} object with none of the extended
 * restrictions (canRead, canAnnotate, etc) properly loaded, use:
 *
 * <code>
 * select new ome.util.PermDetails(d) from Dataset d
 * </code>
 *
 * The return value for each will be the same.
 *
 * @see <a href="https://hibernate.atlassian.net/browse/HHH-3868">HHH-3868</a>
 * @see <a href="https://trac.openmicroscopy.org/ome/ticket/12474">trac-12474</a>
 */
@SuppressWarnings("serial")
public class PermDetails implements IObject {

    private final IObject context;

    public PermDetails(IObject context) {
        Hibernate.initialize(context);
        this.context = context;
    }

    //
    // DELEGATE METHODS
    //

    public Long getId() {
        return context.getId();
    }

    public void setId(Long id) {
        context.setId(id);
    }

    public Details getDetails() {
        return context.getDetails();
    }

    public boolean isLoaded() {
        return context.isLoaded();
    }

    public void unload() throws ApiUsageException {
        context.unload();
    }

    public boolean isValid() {
        return context.isValid();
    }

    public Validation validate() {
        return context.validate();
    }

    public Object retrieve(String field) {
        return context.retrieve(field);
    }

    public void putAt(String field, Object value) {
        context.putAt(field, value);
    }

    public Set<?> fields() {
        return context.fields();
    }

    public GraphHolder getGraphHolder() {
        return context.getGraphHolder();
    }

    @Override
    public boolean acceptFilter(Filter filter) {
        return context.acceptFilter(filter);
    }

}
