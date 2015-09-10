/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

package ome.services.graphs;

import org.hibernate.Session;

import ome.model.IObject;

/**
 * A plug-in for graph policy rule matches whereby an object may be matched against named values.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.3
 */
public interface GraphPolicyRulePredicate {
    /**
     * @return the name of this predicate
     */
    String getName();

    /**
     * Once this instance is submitted to {@link GraphPolicy#registerPredicate(GraphPolicyRulePredicate)} then this method is called by
     * {@link GraphPolicy#noteDetails(org.hibernate.Session, IObject, String, long)}.
     * @param session the Hibernate session, for obtaining more information about the object
     * @param object an unloaded model object that may satisfy this predicate
     * @param realClass the real class name of the object
     * @param id the ID of the object
     */
    void noteDetails(Session session, IObject object, String realClass, long id);

    /**
     * If this predicate is satisfied by the given object.
     * @param object a model object
     * @param parameter the parameter that the object must match
     * @return if the object satisfies this predicate
     * @throws GraphException if the predicate could not be tested
     */
    boolean isMatch(GraphPolicy.Details object, String parameter) throws GraphException;
}
