/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldapsync;

import ome.services.ldap.LdapTest.Fixture;

/**
 * Modification SPI which will be performed on the contents
 * of the current LDAP store during processing of some
 * LDIF file.
 */
public interface Modification {

    void modify(Fixture fixture);
}
