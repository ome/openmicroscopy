/*
 * org.openmicroscopy.shoola.agents.chainbuilder.piccolo.Link
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 Open Microscopy Environment
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
 *    but WITH
 * OUT ANY WARRANTY; without even the implied warranty of
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
 * Written by:    Harry Hochheiser <hsh@nih.gov>
 *
 *------------------------------------------------------------------------------
 */


package org.openmicroscopy.shoola.agents.chainbuilder.piccolo;

//Java Imports
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.Shape;
import java.util.ArrayList;

//Third-party libraries
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.chainbuilder.ChainBuilderAgent;
import org.openmicroscopy.shoola.util.ui.Constants;

/** 
 * A Piccolo node for links between inputs and outputs. Link extends
 * PPath by adding facilities for tracking a series of points.
 *
 * @author  Harry Hochheiser &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:hsh@nih.gov">hsh@nih.gov</a>
 *
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 */

public abstract class Link extends  PPath implements NodeEventListener {
	
	

	/**
	 * 
	 *  degree for splines.
	 **/
	private static final int DEGREE = 3;
	
	/** 
	 * The list of points in the link
	 */	
	protected ArrayList points  = new ArrayList();
	
	/**
	 * The circle at the end of a link
	 */
	protected PPath bulb;
	
	/**
	 * The array containing the points in the link. This is an array version of
	 * points
	 */
	protected Point2D pts[];
	
	
	/** 
	 * the number of points in the link.
	 */
	protected int pointCount = 0;	
	
	
    /**
     * The {@link LinkLayer} that is the parent of this link.
     * 
     */
	protected LinkLayer  linkLayer = null;
	
	private PNode targets = new PNode();
		
	
	public Link() {		
		super();
		setStroke(Constants.LINK_STROKE);
		setStrokePaint(Constants.DEFAULT_COLOR);
		buildBulb();
		addChild(targets);
		targets.setVisible(false);
		setPickable(true);
	}
		
	/**
	 * Add a circle to the end of the link.
	 *
	 */
	protected void buildBulb() {
	
		bulb = PPath.createEllipse(0,0,Constants.LINK_BULB_SIZE,
			Constants.LINK_BULB_SIZE);
		bulb.setPaint(Constants.DEFAULT_COLOR);
		addChild(bulb);
	}
	
	public void setPickable(boolean v) {
		super.setPickable(v);
		bulb.setPickable(v);
	}
	
	/**
	 * Set the starting coordinates of this link. Note that the starting and
	 * ending coordinates are defined in terms of the order in which the points 
	 * were added to the link - the starting coordinate was the first added, 
	 * and the ending coordinate is the last. The type of parameter at either
	 * end is irrelevant as far as definitions of starting and ending 
	 * coordinates are concerned
	 *
	 * @param x
	 * @param y
	 */	
	public void setStartCoords(float x,float y) {

		setPoint(0,new Point2D.Float(x,y));
		if (pointCount == 0)
			pointCount =1;
	}

	/***
	 * Add an intermediate point. This is used when the user makes a click 
	 * during the process of creating a link but not on a formal parameter. 
	 * The new point replaces the last point and is duplicated, so that the next
	 * point will overwrite the second copy, but not the first.
	 */
	public void setIntermediatePoint(float x,float y) {
		setPoint(pointCount++,new Point2D.Float(x,y)); 
		setEndCoords(x,y);
	}
	
	/**
	 * Changes the last point in the link 
	 * @param x
	 * @param y
	 */
	public void setEndCoords(float x,float y) {
		if (ChainBuilderAgent.DEBUG)
			System.err.println("adding link end point "+x+","+y);
		setPoint(pointCount,new Point2D.Float(x,y));
		setLine();
	}
	
	protected abstract void setLine();
	
	/**
	 * SEt the point at a specific index.
	 * @param index
	 * @param pt
	 */
	public void setPoint(int index,Point2D pt) {
		if (ChainBuilderAgent.DEBUG)
		System.err.println("setting point # "+index+", # of points is "+points.size());
		if (points.size() <= index) {
			points.add(index,pt);
		}
		else
			points.set(index,pt);
		updateBounds();
	}
	
	/**
	 * Set the point at index i. This is used when adjusting the links during 
	 * the process of laying out a chain.
	 * @param i
	 * @param x
	 * @param y
	 */
	public void insertIntermediatePoint(int i,float x,float y) {
		Point2D pt =  new Point2D.Double(x,y);
		points.add(i,pt);
		pointCount++;
		updateBounds();
	}
	
	/**
	 * To paint the link, get its shape and draw it.
	 * 
	 */
	public void paint(PPaintContext aPaintContext) {
		Graphics2D g = aPaintContext.getGraphics();
		
		g.setStroke(Constants.LINK_STROKE);
		g.setPaint(Constants.DEFAULT_COLOR);
		
		Shape p = getLinkShape();
		if (p != null)
			g.draw(p);
	}
	
	/**
	 * If there are two points in the link, it is a straight line.
	 * 3 points leads to a QuadraticCurve2D. More points leads to a bezier.
	 * 
	 * @return
	 */
	private Shape getLinkShape() {
		Shape s=null;
		Line2D line  = new Line2D.Double();
		QuadCurve2D quad = new QuadCurve2D.Double();
				
		Point2D pts[] = (Point2D []) points.toArray(new Point2D[0]);
		int size = pts.length;
		if (size == 2) {
			line.setLine(pts[0],pts[1]);
			s= line;	
		}
		else if (size == 3) {
			quad.setCurve(pts[0],pts[1],pts[2]);
			s= quad;
		}
		
		else if (size > 3){
			s= drawBezierCurve(pts);	
		}
		return s;
	}
			
	protected GeneralPath drawBezierCurve(Point2D[] pts) {
		GeneralPath p = new GeneralPath();
	
		int n = pts.length;
		// width of space?
		int w2=100;
		double width =w2;
		double step = 1./width;
		double t = step;
		float x;
		float y;
		double newx=0;
		double newy=0;
		
		double[] pxi = new double[n];
		double[] pyi = new double[n];
		double[] px = new double[n];
		double[] py = new double[n];
		
		//more convenient notation...
		for (int i = 0; i < n; i++) {
			px[i] = pts[i].getX();
			py[i]=pts[i].getY();
		}
		
		p.moveTo((float) px[0],(float) py[0]);
		for (int k = 1; k < w2; k++){
			System.arraycopy(px,0,pxi,0,n);
			System.arraycopy(py,0,pyi,0,n);
		 	for (int j = n-1; j > 0; j--) {       //  points calculation
				for (int i = 0; i < j; i++) {
			 		px[i]=(1-t)*pxi[i]+t*pxi[i+1];
					py[i]=(1-t)*pyi[i]+t*pyi[i+1];
				}
		 	}	
			p.lineTo((float)pxi[0],(float)pyi[0]); 
		 	t += step;
		}
		return p;
	}	
		
	/**
	 * Set the bounds based on the shape of the link.
	 *
	 */
	protected void updateBounds() {
		Shape s = getLinkShape();
		if (s != null) {
			Rectangle2D b = Constants.LINK_STROKE.createStrokedShape(s).getBounds2D();
			super.setBounds(b.getX(), b.getY(), b.getWidth(), b.getHeight());
		}
	}
	
	
	public boolean setBounds(double x,double y,double width,double height) {
		return false;
	}
	
	public boolean intersects(Rectangle2D aBounds) {
		
		PNode parent = getParent();
		parent = parent.getParent();
		if (parent.intersects(aBounds))
			return false;
		else 
			return getLinkShape().intersects(aBounds);
	}
		
 	
 	/**
 	 * Draw the bulb at the end of the link
 	 * @param x
 	 * @param y
 	 */
	protected void drawLinkEnd(float x,float y) {
		bulb.setOffset(x-Constants.LINK_BULB_RADIUS,
			y-Constants.LINK_BULB_RADIUS);
		invalidateFullBounds(); 
		
		repaint(); 
	}
	
	/**
	 * Called when the formal parameters for this link change
	 */
	public abstract void nodeChanged(NodeEvent e); 
	
	public void remove() {
		removeFromParent();
	}

	public abstract LinkTarget getStartLinkTarget();
	
	public abstract LinkTarget getEndLinkTarget();

 	/**
 	 * Set the link to be selected, adding {@link LinkSelectionTarget}s as
 	 * necessary
 	 * @param v
 	 */
	public void setSelected(boolean v) {
		if (ChainBuilderAgent.DEBUG)
			System.err.println(" setting a link to be selected..");
		LinkTarget startTarget = getStartLinkTarget();
		LinkTarget endTarget = getEndLinkTarget();
		startTarget.setSelected(v);
		endTarget.setSelected(v);
		targets.removeAllChildren();
		if (v == true) { // set up children 
			bulb.setPaint(Constants.LINK_HIGHLIGHT_COLOR);
			targets.setVisible(true);
			int count =  points.size();
			for (int i = 1; i < count-1; i++) {
				LinkSelectionTarget t = new LinkSelectionTarget(this,i);
				targets.addChild(t);
				Point2D pt = (Point2D) points.get(i);
				t.setOffset((float)pt.getX()-Constants.LINK_TARGET_HALF_SIZE,
					(float)pt.getY()-Constants.LINK_TARGET_HALF_SIZE);
				t.setSelected(v);
			}
		}
		else
			bulb.setPaint(Constants.DEFAULT_COLOR);
		repaint();
	}
	
	public void setLinkLayer(LinkLayer linkLayer) {
		this.linkLayer = linkLayer;
	}
	
	public int pointCount() {
		return points.size();
	}
	
	public Point2D getPoint(int i) {
		if (i < points.size()) {
			return (Point2D) points.get(i);
		}
		else return null;
	}
	
	public void dumpPoints() {
		int n = points.size();
		for (int i = 0; i <n; i++) {
			Point2D pt = getPoint(i);
			System.err.println(i+")"+ pt.getX()+","+pt.getY());
		}
	}
}