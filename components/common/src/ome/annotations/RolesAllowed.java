/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Security annotation which specifies a list of user roles which will
 * be permitted to execute this method. A user role is obtained for an
 * Experimenter by being a member of an ExperimenterGroup. I.e. for:
 * <code> @RolesAllowed({"user","system"}) </code> an Experimenter
 * must be linked to either the ExperimenterGroup "user" or "system"
 * by a GroupExperimenterMap.
 *
 * This annotation replaces the previously used version with the same
 * semantics from the JavaEE spec.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RolesAllowed {
    String[] value();
}
