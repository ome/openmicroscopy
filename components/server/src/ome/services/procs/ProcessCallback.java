/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.procs;

import ome.parameters.Parameters;
import static ome.parameters.Parameters.*;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public interface ProcessCallback {

    void processFinished(Process proc);
    void processCancelled(Process proc);
    
}
