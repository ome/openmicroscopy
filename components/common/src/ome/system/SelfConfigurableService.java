/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.system;

import org.springframework.context.ApplicationContextAware;

public interface SelfConfigurableService extends ApplicationContextAware {

    void selfConfigure();

}
