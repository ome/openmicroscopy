/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.Spots3DCanvas
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

package org.openmicroscopy.shoola.agents.spots.ui.java3d;

//Java imports
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.universe.SimpleUniverse;
import javax.media.j3d.Background;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Switch;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.View;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3d;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet;
import org.openmicroscopy.shoola.agents.spots.range.AxisBoundedRangeModel;

/** 
 * The canvas that contains the 3d view 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */


public class Spots3DCanvas extends Canvas3D  implements ChangeListener  {

	public static final int CUBE=1;
	public  static final int OUTLINE=2;
	private BranchGroup objRoot;
	private TransformGroup objRotate;
	private MouseRotate mouseRotate;
	
	private PickCanvas pickCanvas;
	private LabelGroup axisLabels;
	
	private WorldCube cube;
	Transform3D trans = new Transform3D();
	
	Matrix3f xyrot = new Matrix3f( 1.0f, 0.0f, 0.0f,
            					   0.0f,-1.0f, 0.0f,
			                       0.0f,0.0f,-1.0f);

    Matrix3f yzrot = new Matrix3f( 0.0f, 1.0f, 0.0f,
	                     		   0.0f, 0.0f, 1.0f,
			                       1.0f, 0.0f, 0.0f);

    Matrix3f xzrot = new Matrix3f( 0.0f, 0.0f, 1.0f,
	                     		   1.0f, 0.0f, 0.0f,
                                   0.0f, 1.0f, 0.0f);
    
    private SpotsTrajectorySet tSet;
    
    private Extents extents;
	
	public Spots3DCanvas(SpotsTrajectorySet tSet) {
		super(SimpleUniverse.getPreferredConfiguration());
		
		this.tSet = tSet;
		extents = new Extents(tSet);
		   // SimpleUniverse is a Convenience Utility class
        SimpleUniverse simpleU = new SimpleUniverse(this);
        View view = simpleU.getViewer().getView();
        view.setProjectionPolicy(View.PARALLEL_PROJECTION);
        
        createSceneGraph(tSet.getTrajectories());
        // picking
        pickCanvas = new PickCanvas(this,objRoot);
        pickCanvas.setMode(PickTool.BOUNDS);
        pickCanvas.setTolerance(1.0f);
        addMouseListener(new MousePickListener(pickCanvas));
        
        simpleU.getViewingPlatform().setNominalViewingTransform();
        simpleU.addBranchGraph(objRoot);

        setTransform(xyrot);
	}
	
	  public void createSceneGraph(List paths) {
		// Create the root of the branch graph
	 	objRoot = new BranchGroup();
	 	objRoot.setCapability(Group.ALLOW_CHILDREN_WRITE);
	 	 // make background white
	    BoundingSphere bounds =
	        new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
	
	    Background background = new Background(1.0f, 1.0f, 1.0f);
	    background.setApplicationBounds(bounds);
	    objRoot.addChild(background);
	    
	 	// rotate
	 	Transform3D rotate = new Transform3D();
	
	    objRotate = new TransformGroup(rotate);
	    objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	    objRotate.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
	    objRotate.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
	    objRotate.setCapability(Node.ENABLE_PICK_REPORTING);
	    objRotate.setCapability(GeometryArray.ALLOW_INTERSECT);
	    objRotate.setCapability(Primitive.ENABLE_GEOMETRY_PICKING);
	    objRoot.addChild(objRotate);
	 
	 	// cube
	 	cube = new WorldCube(extents);
	 	
	    objRotate.addChild(cube);
	    
	    //axis labels
	    axisLabels = new LabelGroup(tSet,extents);
	    objRotate.addChild(axisLabels);
	    
	    // don't want to be able to rotate if there's no z -extent -
	    // this means only one plane, so no point in 3d view
	    if (tSet.getExtentZ() > 0) {
	    		mouseRotate = new MouseRotate();
	    
	    		mouseRotate.setTransformGroup(objRotate);
	    		mouseRotate.setSchedulingBounds(new BoundingSphere());
	    		mouseRotate.setFactor(.1,.1);
	    		objRotate.addChild(mouseRotate);
	    }
	    
	    buildPoints(paths);
	    objRoot.compile();
	}
	  

	  
	private void buildPoints(List v) {
	
		Iterator iter = v.iterator();
		while (iter.hasNext()) {
			SpotsTrajectory t = (SpotsTrajectory) iter.next();
	        Switch s = new TrajSwitch(t,tSet,extents);
	        objRotate.addChild(s);	
		}
	}
    

    public void setXYTransform() {
    	setTransform(xyrot);
    }
    
    public void setYZTransform() {
    	setTransform(yzrot);
    }
    
    public void setZXTransform() {
    	setTransform(xzrot);
    }
    private void setTransform(Matrix3f mat) {
    	trans.setRotation(mat);
    	objRotate.setTransform(trans);
    }
    
    public void setRotateXOnly() {
    	mouseRotate.setFactor(.1,0);
    }
    
    public void setRotateYOnly() {
    	mouseRotate.setFactor(0,0.1);
    }
    
    public void setRotateBoth() {
    	mouseRotate.setFactor(0.1,0.1);
    }
    
    public void stateChanged(ChangeEvent e) {
     	AxisBoundedRangeModel model = (AxisBoundedRangeModel) e.getSource();
  
 		int val = model.getValue();
 		int extent = model.getExtent();
		int axis = model.getAxis();
		int high = val+extent;
 	
//		 iterate over children. of group
     	Enumeration enum = objRotate.getAllChildren();
     	TrajSwitch s;
     	
     	while(enum.hasMoreElements()) {
     		Object obj = enum.nextElement();
     		if (obj instanceof TrajSwitch) {
     			s = (TrajSwitch) obj;
     			s.adjustToFit(axis,val,high);
     		}
     	}
     	axisLabels.adjustToFit(axis,model.getLowTickString(),
     		model.getHighTickString());
 	}
    
    public void drawCubes() {
    	drawShapes(Spots3DCanvas.CUBE);
    }
    
    public void drawOutline() {
    	drawShapes(Spots3DCanvas.OUTLINE);
    }
    
    public void drawShapes(int shape) {
    	Enumeration enum = objRotate.getAllChildren();
     	TrajSwitch s;
     	
     	while(enum.hasMoreElements()) {
     		Object obj = enum.nextElement();
     		if (obj instanceof TrajSwitch) {
     			s = (TrajSwitch) obj;
     			
     			s.drawShape(shape);
     		}
     	}
    	
    }
    
    /** not implemented. In particular, have to work on the 
     * axis labels to make this work out right. */
    public  void setScaledAspects(boolean v) {
    		extents.setScaledAspects(v);
    		// 	redraw cube
    		cube.resetCube(extents);
    }
    
}
