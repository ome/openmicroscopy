/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.CubeGeometry
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
import com.sun.j3d.utils.geometry.Primitive;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.IndexedQuadArray;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;

/** 
 * Basic geometry for drawing a cube that represents a point on a trajectory 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */


public class CubeGeometry extends IndexedQuadArray {

	  
      private int coordIndices[] =  {0,1,2,3,7,6,5,4,
      			0,3,7,4,5,6,2,1,0,4,5,1,6,7,3,2};
      private Vector3f[] normals = new Vector3f[] {
      		new Vector3f(0f,0f,1f),
      		new Vector3f(0f,0f,-1f),
			new Vector3f(1f,0f,0f),
			new Vector3f(-1f,0f,0f),
			new Vector3f(0f,1f,0f),
			new Vector3f(0f,-1f,0f),
      };
      
      private int normalIndices[] = {0,0,0,0,1,1,1,1,
      		2,2,2,2,3,3,3,3,4,4,4,4,5,5,5,5
      };
      
	  /**
	   * The <code>QuadBox</code> constructor creates a cube object made of quads
	   * which is centered on the virtual world axis with edge length of one Java3D
	   * meter.  The default color for the quad cube is white.
	   */
      
      public CubeGeometry(float side) {
      	this(side,side,side);
      }
      
      public CubeGeometry(Extents extents) {
      	this((float) extents.getExtent(SpotsTrajectory.X),
      		 (float) extents.getExtent(SpotsTrajectory.Y),
			 (float) extents.getExtent(SpotsTrajectory.Z));
      }
      
      public CubeGeometry(float xside,float yside,float zside)	  {
	  
	  	super( 8,IndexedQuadArray.COORDINATES|
		      			IndexedQuadArray.NORMALS,24);

	    
	    float xhalf = xside/2;
	    float yhalf = yside/2;
	    float zhalf = zside/2;
	  
	    Point3f[] coordinates = new Point3f[] {
	    	  	new Point3f( xhalf, yhalf, zhalf),
	            new Point3f(-xhalf, yhalf, zhalf),
	            new Point3f(-xhalf,-yhalf, zhalf),
	            new Point3f( xhalf,-yhalf, zhalf),
	            new Point3f( xhalf, yhalf,-zhalf),
	            new Point3f(-xhalf, yhalf,-zhalf),
	            new Point3f(-xhalf,-yhalf,-zhalf),
	            new Point3f( xhalf,-yhalf,-zhalf)};
	    
	    
	    setCoordinates(0,coordinates);
	    setCoordinateIndices(0,coordIndices);
	    setNormals(0,normals);
	    setNormalIndices(0,normalIndices);
	    setCapability(GeometryArray.ALLOW_REF_DATA_READ);
	    setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
	    setCapability(GeometryArray.ALLOW_INTERSECT);
	    setCapability(Primitive.ENABLE_GEOMETRY_PICKING);
	  }
}
