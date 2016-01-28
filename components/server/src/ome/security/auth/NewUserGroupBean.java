/*
 *   $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.List;

import ome.security.SecuritySystem;

import org.springframework.ldap.core.LdapOperations;

/**
 * Strategy for finding the appropriate groups for a given user in LDAP.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @since Beta4.2
 */
public interface NewUserGroupBean {

    List<Long> groups(String username,
            LdapConfig config, LdapOperations ldap, RoleProvider provider,
            AttributeSet attrSet);

}
