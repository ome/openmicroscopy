/*
 * ome.rules.DroolsEngine
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

/*------------------------------------------------------------------------------
 *
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

package ome.rules;

//Java imports

//Third-party libraries
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.drools.event.DebugWorkingMemoryEventListener;

//Application-internal dependencies


/**
 * 
 * @author Josh Moore, <a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
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

	public void evaluate(Object... assertions)
			throws Exception {

		WorkingMemory workingMemory = businessRules.newWorkingMemory();
		workingMemory.getApplicationDataMap().put("cache",new HashSet());
		
		if (debug){
			workingMemory.addEventListener(new DebugWorkingMemoryEventListener());
		}

		for (Object o : assertions) {
			workingMemory.assertObject(o);
		}

		log.debug("Firing rules...");
		workingMemory.fireAllRules();
	}

}
