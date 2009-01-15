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
 * Security annotation which allows a method to be called by all users
 * regardless of their status. This includes the "guest" role or even
 * a disabled user account (i.e. not in "user"). Use sparingly.
 *
 * This annotation replaces the previously used version with the same
 * semantics from the JavaEE spec.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PermitAll {

}
