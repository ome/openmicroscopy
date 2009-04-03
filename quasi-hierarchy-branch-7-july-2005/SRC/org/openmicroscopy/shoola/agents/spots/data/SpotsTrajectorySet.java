/*
 * org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.ds.dto.Feature;
import org.openmicroscopy.ds.st.Extent;
import org.openmicroscopy.ds.st.Location;
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

public class SpotsTrajectorySet 
{
	
	private static final String TRACK_SPOTS_NAME="Track spots";
	private static final String TRAJECTORY_ST_NAME="Trajectory";
	private static final String ENTRY_ST_NAME="TrajectoryEntry";
	
	private static final float EXTENT_MULTIPLIER=1.05f;
	
	private float maxX=-Float.MAX_VALUE;
	private float maxY=-Float.MAX_VALUE;
	private float maxZ=-Float.MAX_VALUE;
	private int maxSz=-Integer.MIN_VALUE;
	
	private ChainExecutionData execution;
	private Registry registry;
	
	private Vector trajectories = null;
	
	/** the dataset I'm looking at */
	private int datasetID;
	
	public SpotsTrajectorySet(Registry registry,ChainExecutionData execution) {
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
				mexes.add(new Integer(mex.getID()));  		
			}
		}
		
		// get the instances of trajectory for this dataset.
		List entries = findTrajectoryEntries(mexes);

		if (entries == null || entries.size() == 0)
			return;
		
		// Get the features list
		ArrayList featureList = getFeatures(entries);
		if (featureList == null || featureList.size() ==0 )
			return;
		
		//  retrieve locations
		List locations = getLocations(featureList);
		if (locations == null || locations.size() == 0)
			return;
	
		// get extents
		List extents = getExtents(featureList);
		if (extents == null || locations.size() == 0) 
			return;
		
		buildTrajectories(entries,locations,extents);
		//dumpTrajectories();
	}
	
	public boolean isEmpty() {
		if (trajectories == null) 
			return true;
		else
			return (trajectories.size() ==0);
	}
	
	private List findTrajectoryEntries(Vector mexes) {

		SemanticTypesService sts = registry.getSemanticTypesService();
		try {
			List instances = 			
				sts.retrieveTrajectoryEntriesByMEXs(mexes);
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
	
	private ArrayList getFeatures(Collection entries) {
		ArrayList features = new ArrayList();
		Iterator iter = entries.iterator();
		while (iter.hasNext()) {
			TrajectoryEntry t= (TrajectoryEntry) iter.next();
			Feature f = t.getFeature();
			features.add(new Integer(f.getID()));
		}
		return features;
	}
	
	private List getLocations(List features) {
		
		SemanticTypesService sts = registry.getSemanticTypesService();
		try {
			List locations = 			
				sts.retrieveLocationsByFeatureID(features);
			System.err.println("# of trajectories..."+locations.size());
			return locations;
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
	
	private List getExtents(List features) {
		
		SemanticTypesService sts = registry.getSemanticTypesService();
		try {
			List locations = 			
				sts.retrieveExtentsByFeatureID(features);
			System.err.println("# of trajectories..."+locations.size());
			return locations;
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
	private void dumpTrajectories() {
		if (trajectories == null) return;
		System.err.println("dumping trajectories...");
		Iterator iter = trajectories.iterator();
		
		while (iter.hasNext()) {
			SpotsTrajectory st = (SpotsTrajectory)iter.next();
			st.dump();
		}
		
		System.err.println("max x is "+getMaxX());
		System.err.println("max y is "+getMaxY());
		System.err.println("max z is "+getMaxZ());
	}
	
	private void buildTrajectories(List entries,List locations,List extents) {
		HashMap trajMap = new HashMap();
		Iterator iter = entries.iterator();
		while (iter.hasNext()) {
			TrajectoryEntry te = (TrajectoryEntry) iter.next();
			int feature = te.getFeature().getID();
			Location loc = findLocation(feature,locations);
			Extent ext = findExtent(feature,extents);
			if (loc != null) { 
				Trajectory t = te.getTrajectory();
				Object obj = trajMap.get(t);
				SpotsTrajectory st;
				if (obj == null) 
					st = new SpotsTrajectory();
				else 
					st = (SpotsTrajectory)obj;
				st.addPoint(te,loc,ext);
				
				if (st.getMaxX() > maxX)
					maxX = st.getMaxX();
				if (st.getMaxY() > maxY)
					maxY = st.getMaxY();
				if (st.getMaxZ() > maxZ)
					maxZ = st.getMaxZ();
				// max size.
				if (st.getMaxSz() > maxSz)
					maxSz=st.getMaxSz();
				
				trajMap.put(t,st); 
			}
		}
		trajectories = new Vector(trajMap.values());
	}
	
	private Location findLocation(int feature,List locations) {
		Iterator iter = locations.iterator();
		while (iter.hasNext()) {
			Location loc = (Location) iter.next();
			if (loc.getFeature().getID() == feature) 
				return loc;
		}
		return null;
	}
	
	private Extent findExtent(int feature,List extents) {
		Iterator iter = extents.iterator();
		while (iter.hasNext()) {
			Extent ext = (Extent) iter.next();
			if (ext.getFeature().getID() == feature) 
				return ext;
		}
		return null;
	}
	
	public int getTrajectoryCount() {
		return trajectories.size();
	}
	
	public Vector getTrajectories() {
		return trajectories;
	}
	
	public Iterator iterator() {
		return trajectories.iterator();
	}
	
	public float getExtentX() {
		return maxX*EXTENT_MULTIPLIER;
	}
	
	
	public float getExtentY() {
		return maxY*EXTENT_MULTIPLIER;
	}
	
	
	public float getExtentZ() {
		return maxZ*EXTENT_MULTIPLIER;
	}
	
	
	public float getExtent(int which) {
		switch(which) {
			case SpotsTrajectory.X:
				return getExtentX();
			case SpotsTrajectory.Y:
				return getExtentY();
			case SpotsTrajectory.Z:
				return getExtentZ();
			default:
				return -1;
		}
	}
	
	public String getLowLabel(int axis) {
		int val = (int) (SpotsTrajectory.LABEL_FACTOR*getExtent(axis));
		return Integer.toString(val);
	}
	
  	public String getHighLabel(int axis) {
  		int val = (int) ((1-SpotsTrajectory.LABEL_FACTOR)*getExtent(axis));
  		return Integer.toString(val);
  	}
  	
  	public String getLabel (int which) {
		switch(which) {
			case SpotsTrajectory.X:
					return new String("X");
			case SpotsTrajectory.Y:
					return new String("Y");
			case SpotsTrajectory.Z:
					return new String("Z");
			default:
					return null;
		}
	}
  	
  	public float getMaxSize() {
		return maxSz;
	}
	
	public float getLogMaxSize() {
		return (float) Math.log(maxSz);
	}
	
	public float getMaxX() {
		return maxX;
	}
	
	public float getMaxY() {
		return maxY;
	}
	
	public float getMaxZ() {
		return maxZ;
	}
}