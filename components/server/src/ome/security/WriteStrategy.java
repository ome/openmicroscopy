/*
 *   Copyright 2012 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import ome.model.internal.Details;
import ome.security.basic.AllGroupsSecurityFilter;
import ome.security.basic.CurrentDetails;
import ome.security.basic.OneGroupSecurityFilter;
import ome.system.EventContext;

/**
 * Responsible for determining which users
 * can modify which objects in which contexts.
 *
 * This interface plays a similar role to SecurityFilter
 * which determines which objects are readable by which
 * users in which contexts.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecurityFilter
 * @since 4.4
 */
public interface WriteStrategy {


}
