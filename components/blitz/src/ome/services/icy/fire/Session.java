/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.icy.fire;

import java.util.List;

import ome.logic.HardWiredInterceptor;
import ome.system.Principal;

public interface Session extends Glacier2.Session {

    void setPrincipal(Principal principal);
    void setInterceptors(List<HardWiredInterceptor> cptors);
}