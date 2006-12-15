/*
 * ome.rules.DroolsEngine
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.rules;

// Java imports

// Third-party libraries
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.drools.event.DebugWorkingMemoryEventListener;

// Application-internal dependencies

/**
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since OMERO 3.0
 */
public class DroolsEngine implements RulesEngine {

    private static Log log = LogFactory.getLog(DroolsEngine.class);

    private RuleBase businessRules;

    private boolean debug;

    public DroolsEngine(RuleBase rules, boolean addDebugListener) {
        this.businessRules = rules;
        debug = addDebugListener;
    }

    public void evaluate(Object... assertions) throws Exception {

        WorkingMemory workingMemory = businessRules.newWorkingMemory();
        workingMemory.getApplicationDataMap().put("cache", new HashSet());

        if (debug) {
            workingMemory
                    .addEventListener(new DebugWorkingMemoryEventListener());
        }

        for (Object o : assertions) {
            workingMemory.assertObject(o);
        }

        log.debug("Firing rules...");
        workingMemory.fireAllRules();
    }

}
