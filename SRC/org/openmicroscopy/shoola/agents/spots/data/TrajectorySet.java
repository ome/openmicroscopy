/*
 * org.openmicroscopy.shoola.agents.spots.data.TrajectorySet
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

package org.openmicroscopy.shoola.agents.spots.data;

//Java imports
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.st.Trajectory;
import org.openmicroscopy.ds.st.TrajectoryEntry;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.ChainExecutionData;
import org.openmicroscopy.shoola.env.data.model.ModuleExecutionData;
import org.openmicroscopy.shoola.env.data.model.ModuleData;
import org.openmicroscopy.shoola.env.data.model.NodeExecutionData;
import org.openmicroscopy.shoola.env.data.DSAccessException;
import org.openmicroscopy.shoola.env.data.DSOutOfServiceException;
import org.openmicroscopy.shoola.env.data.SemanticTypesService;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * A set of trajectories to be displayed
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */

public class TrajectorySet 
{
	
	private static final String TRACK_SPOTS_NAME="Track spots";
	private static final String TRAJECTORY_ST_NAME="Trajectory";
	private static final String ENTRY_ST_NAME="TrajectoryEntry";
	private ChainExecutionData execution;
	private Registry registry;
	
	/** the dataset I'm looking at */
	private int datasetID;
	

	
	public TrajectorySet(Registry registry,ChainExecutionData execution) {
		this.registry = registry;
		this.execution = execution;
		
		Iterator iter  = execution.getNodeExecutions().iterator();
		NodeExecutionData nex;
		
		// save the mexes I'm looking for
		Vector mexes = new Vector();
		
		while (iter.hasNext()) {
			nex = (NodeExecutionData) iter.next();
			
			ModuleExecutionData mex = nex.getModuleExecution();
			ModuleData mod = mex.getModule();
			if (mod.getName().compareTo(TRACK_SPOTS_NAME) == 0) {
				System.err.println("node execution "+nex.getID());
				System.err.println("module execution..."+mex.getID());
				System.err.println("module.."+mod.getID()+", "+mod.getName());
				mexes.add(new Integer(mex.getID()));  		
			}
		}
		
		// get the instances of trajectory for this dataset.
		Collection trajectories = findTrajectories(mexes);
		// filter them by the given mexes.
		dumpTrajectories(trajectories);
		
	}
	
	private Collection findTrajectories(Vector mexes) {

		SemanticTypesService sts = registry.getSemanticTypesService();
		try {
			Collection instances = 			
				//sts.retrieveAttributesByMEXs(TRAJECTORY_ST_NAME,mexes);
				//sts.retrieveTrajectoriesByMEXs(mexes);
				sts.retrieveTrajectoryEntriesByMEXs(mexes);
			System.err.println("# of trajectories..."+instances.size());
			return instances;
	   }
	   catch(DSOutOfServiceException dso)
        {
            dso.printStackTrace();
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Connection Error",dso.getMessage(),dso);
        }
        catch(DSAccessException dsa)
        {
            dsa.printStackTrace();
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Server Error",dsa.getMessage(),dsa);
        }
        return null;
	}
	
	private Collection findEntries(Vector mexes) {

		SemanticTypesService sts = registry.getSemanticTypesService();
		try {
			Collection instances = 			
				sts.retrieveAttributesByMEXs(ENTRY_ST_NAME,mexes);
			System.err.println("# of entries..."+instances.size());
			return instances;
	   }
	   catch(DSOutOfServiceException dso)
        {
            dso.printStackTrace();
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Connection Error",dso.getMessage(),dso);
        }
        catch(DSAccessException dsa)
        {
            dsa.printStackTrace();
            UserNotifier un = registry.getUserNotifier();
            un.notifyError("Server Error",dsa.getMessage(),dsa);
        }
        return null;
	}
	
	private void dumpTrajectories(Collection trajectories) {
		if (trajectories == null)
			return;
		Iterator iter = trajectories.iterator();
		while (iter.hasNext()) {
			TrajectoryEntry t = (TrajectoryEntry) iter.next();
			System.err.println("found trajectory entry..."+t.getID());
			System.err.println("order."+t.getOrder());
			System.err.println("delta x is "+t.getDeltaX());
			System.err.println("feature id is "+t.getFeature().getID());
			System.err.println("image is "+t.getFeature().getImage().getID());
			Trajectory tr = t.getTrajectory();
			System.err.println("trajectory... "+tr.getID());
			System.err.println("total distance.."+tr.getTotalDistance());
			System.err.println("ave velocity..."+tr.getAverageVelocity()+"\n\n");
		}
	}
}