/*
 * org.openmicroscopy.shoola.agents.spots.ui.java3d.LabelGroup
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
import com.sun.j3d.utils.geometry.Text2D;
import java.awt.Font;
import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes; 
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Transform3D;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectory;
import org.openmicroscopy.shoola.agents.spots.data.SpotsTrajectorySet;


/** 
 * Labels on the various axes 
 * 
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */





public class LabelGroup extends TransformGroup {
	
	private static final Font f = new Font("Helvetica",Font.PLAIN,1);
	public static final float NAME_OFFSET=-.23f;
	public static final float NUMBER_OFFSET=-0.18f;
	private static final float LOW_LABEL_POS= -0.2f;
	private static final float HIGH_LABEL_POS =0.2f;
	private Appearance app;
	
	

	// angles of rotation for individual items on each axis
		
	// to interpret these correctly, we must realize that 
	// the cube has already been rotated 180 around x, to but the x/y plane i
	// in the front with y pointing _down_ the screen.
	// Thus, I scale each of the three labels by -1 - effectively "undoing" the
	// flip of the cube.
	// So, when I say "x rotates 180 around y", I've also post-multiplied by 
	// a scale of -1.
	// x rotates 180 around y
	
	// x rotates 180 around y
	Matrix3f xrots = new Matrix3f ( 1.0f, 0.0f, 0.0f,
			                         0.0f, -1.0f, 0.0f,
									 0.0f, 0.0f,1.0f);
	// on y axis, rotate -90 degrees around z
	Matrix3f yrots = new Matrix3f( 0.0f, -1.0f, 0.0f,
								  1.0f, 0.0f, 0.0f,
								   0.0f, 0.0f, -1.0f);
	
	// on z axis, 90 degrees around y
	Matrix3f zrots = new Matrix3f( 0.0f, 0.0f, -1.0f,
								   0.0f, -1.0f, 0.0f,
								  1.0f, 0.0f, 0.0f);
	
	// not quite sure why the following matrices work right, but they do.
	// These matrices - perhaps along with the whole class - should
	// be refactored and clarified.
	
	// now, rotate whole x thing  around x
	Matrix3f xangle = new Matrix3f( 1.0f, 0.0f, 0.0f,
						            0.0f, 0.707f, 0.707f,
									0.0f, 0.707f, 0.707f);	
	
	//	 now, rotate whole y thing  around y
	Matrix3f yangle = new Matrix3f( 0.707f, 0.0f, 0.707f,
					                0.0f,   1.0f, 0.0f,
								    0.707f, 0.0f, 0.707f);
	
     //	 now, rotate whole z  around z
	Matrix3f zangle = new Matrix3f( .707f,.707f, 0.0f,
								    .707f, .707f, 0.0f,
									0.0f, 0.0f, 1.0f);
	
	private Text2D xlow;
	private Text2D xhigh;

	private Text2D ylow;
	private Text2D yhigh;
	
	private Text2D zlow;
	private Text2D zhigh;

	private TransformGroup xgroup;
	private TransformGroup ygroup;
	private TransformGroup zgroup;
	
	private SpotsTrajectorySet tSet;
	
	public LabelGroup(SpotsTrajectorySet tSet,Extents extents) {
		super();

		this.tSet = tSet;
		app = new Appearance();
		PolygonAttributes atts = new PolygonAttributes();
		atts.setCullFace(PolygonAttributes.CULL_NONE);
		app.setPolygonAttributes(atts);
		Material mat = new Material();
		mat.setLightingEnable(true);
		app.setMaterial(mat);
		
		
		//add x,
	    xgroup = new TransformGroup();
	    xgroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	    xgroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    xgroup.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		addChild(xgroup);
		setAxisAngle(xgroup,xangle);
		Text2D xtext = getText2D("X");
		float zfront = (float)extents.getFrontOffset(SpotsTrajectory.Z);
		xgroup.addChild(buildAxisLabel(xtext,0.0f,NAME_OFFSET,
				zfront,xrots));
		xlow = getText2D(tSet.getLowLabel(SpotsTrajectory.X));
		xgroup.addChild(buildAxisLabel(xlow,-.2f,NUMBER_OFFSET,
				zfront,xrots));
		xhigh = getText2D(tSet.getHighLabel(SpotsTrajectory.X));
		xgroup.addChild(buildAxisLabel(xhigh,.2f,NUMBER_OFFSET,
				zfront,xrots));
		
		
		ygroup = new TransformGroup();
		ygroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	    ygroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    ygroup.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		addChild(ygroup);
		setAxisAngle(ygroup,yangle);
		Text2D ytext = getText2D("Y");
		
		ygroup.addChild(buildAxisLabel(ytext,NAME_OFFSET,0.0f,
				zfront,yrots));
		ylow = getText2D(tSet.getLowLabel(SpotsTrajectory.Y));
		ygroup.addChild(buildAxisLabel(ylow,NUMBER_OFFSET,
				-0.2f,zfront,yrots));
		yhigh = getText2D(tSet.getHighLabel(SpotsTrajectory.Y));
		
		ygroup.addChild(buildAxisLabel(yhigh,NUMBER_OFFSET,0.2f,
				zfront,yrots));
			
		
		zgroup = new TransformGroup();
		zgroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	    zgroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    zgroup.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
		addChild(zgroup);
        setAxisAngle(zgroup,zangle);
        Text2D ztext = getText2D("Z");
        float xfront = (float) extents.getFrontOffset(SpotsTrajectory.X);
        zgroup.addChild(buildAxisLabel(ztext,xfront,NAME_OFFSET,0.0f,
				zrots));
		zlow = getText2D(tSet.getLowLabel(SpotsTrajectory.Z));
		zgroup.addChild(buildAxisLabel(zlow,xfront,
				NUMBER_OFFSET,-0.2f,zrots));
		zhigh = getText2D(tSet.getHighLabel(SpotsTrajectory.Z));
		zgroup.addChild(buildAxisLabel(zhigh,xfront,
				NUMBER_OFFSET,0.2f,zrots));
		
		
	}
	
	
	private Text2D getText2D(String label) {

		Text2D text = new Text2D(label,new Color3f(0.0f,0.0f,0.0f),
				"Helvetica",12,Font.BOLD);;
		 text.setCapability(Text2D.ALLOW_APPEARANCE_READ);
		 text.setCapability(Text2D.ALLOW_APPEARANCE_WRITE);
		
	     Appearance textAppear = text.getAppearance();
	     textAppear.setCapability(Appearance.ALLOW_TEXTURE_READ);
	     textAppear.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
         PolygonAttributes polyAttrib = new PolygonAttributes();
         polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
         polyAttrib.setBackFaceNormalFlip(true);
         textAppear.setPolygonAttributes(polyAttrib);			
		return text;
	}
	
	private TransformGroup buildAxisLabel(Text2D text,float x,float y, float z,
			Matrix3f rots) {

		Transform3D t3 = new Transform3D();
		t3.setRotation(rots);
		t3.setTranslation(new Vector3f(x,y,z));
		
		TransformGroup xt = new TransformGroup(t3);
		xt.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		xt.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		xt.addChild(text);
		return xt;
	}
	
	
	
	private void setAxisAngle(TransformGroup tg,Matrix3f mat) {
		Transform3D t = new Transform3D();
		t.setRotation(mat);
		tg.setTransform(t);
	}
	
	public void adjustToFit(int axis,String lows,String highs) {
		switch(axis) {
			case SpotsTrajectory.X:
				xlow.setString(lows);
				xhigh.setString(highs);
				break;
			case SpotsTrajectory.Y:
				ylow.setString(lows);
				yhigh.setString(highs);
				break;
			case SpotsTrajectory.Z:
				zlow.setString(lows);
				zhigh.setString(highs);
				break;
		}
	}
	
	public void reposition() {

	}
}