/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package ome.services.blitz.util;

import ome.services.blitz.impl.ServiceFactoryI;
import omero.ServerError;

/**
 * Servant which is aware of the {@link ServiceFactoryI}-instance which it
 * belongs to and will have it injected on instantiation. By definition, such
 * servants should be stateful and have "singleton=true" in the Spring
 * configuration.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public interface ServiceFactoryAware {

    void setServiceFactory(ServiceFactoryI sf) throws ServerError;

}
