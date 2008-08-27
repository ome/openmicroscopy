/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package ome.services.blitz.util;

/**
 * Marker interface which specifies that an servant should not be checked for
 * api consistency via {@link ApiConsistencyCheck}. This is necessary both
 * where there is no underlying ome.api.* service as well as when the method
 * arguments sufficiently differ, for example in the case of enums and value
 * objects.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3.1
 */
public interface BlitzOnly {

}
