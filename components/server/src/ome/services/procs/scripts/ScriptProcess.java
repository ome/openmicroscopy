/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.procs.scripts;

import ome.services.procs.ProcessSkeleton;
import ome.services.procs.Processor;

import org.springframework.scripting.support.StaticScriptSource;

/**
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class ScriptProcess extends ProcessSkeleton {

    public ScriptProcess(Processor p) {
        super(p);
        StaticScriptSource script = new StaticScriptSource("test script");

    }

}
