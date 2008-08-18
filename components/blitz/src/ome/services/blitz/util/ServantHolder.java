/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package ome.services.blitz.util;

import java.util.HashMap;
import java.util.Map;

import ome.services.blitz.impl.ServiceFactoryI;

/**
 * Manager for all active servants in a single {@link ServiceFactoryI}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public class ServantHolder {

    final Map<String, Ice.Object> servants = new HashMap<String, Ice.Object>();

}
