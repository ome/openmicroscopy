/*
 * ome.util.tasks.SimpleTask
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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

package ome.util.tasks;

//Java imports
import java.util.Properties;

//Third-party libraries

//Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.ServiceFactory;

/** 
 * Simplest possible concrete subclass of {@link Task} which has null methods
 * for all of the required methods. Therefore, does nothing by default (though
 * it logs the nothing that it's doing).
 * 
 * {@link Task} writers can override any or all of the  4 methods. 
 * 
 * @author  Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date$
 * @see		Task
 * @since   3.0-M4
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
public class SimpleTask extends Task {

	/** Sole constructor. Delegates to {@link Task}
	 * @see Task#Task(ServiceFactory, Properties)
	 */
	public SimpleTask(ServiceFactory serviceFactory, Properties properties) {
		super(serviceFactory, properties);
	}

	/** 
	 * Does nothing.
	 */
	@Override
	public void init() {
		if (getLogger().isDebugEnabled())
			getLogger().debug("Initializing task:" + this);
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void doTask() {
		if (getLogger().isDebugEnabled())
			getLogger().debug("Running task:" + this);
	}

	/**
	 * Rethrows the {@link RuntimeException}.
	 */
	@Override
	public void handleException(RuntimeException re) {
		if (getLogger().isDebugEnabled())
			getLogger().debug("Handling exception in:" + this, re);
		throw re;
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void close() {
		if (getLogger().isDebugEnabled())
			getLogger().debug("Closing task:" + this);
	}

}
