
/*
 * org.openmicroscopy.shoola.agents.rnd.pane.GraphicsRepresentation
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

package org.openmicroscopy.shoola.agents.rnd.pane;


//Java imports
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.rnd.quantum.QuantumFactory;

/** 
 * Builds a JPanel with the curve associated to the specified family.
 * <p>
 * Draw linear & polynomial, logarithmic, exponential curves.
 * The drawing system is divided in two families 
 * <code>LINEAR</code> and <code>EXPONENTIAL</code>.
 * Five control points are necessary to draw the requested curve.
 * </p>
 * The user can selected an inputWindow and outputWindow moving the
 * graphical sliders' knob.
 * The mouse's events are handled in {@link GraphicsRepresentationManager}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class GraphicsRepresentation
	extends JPanel
{
	
	/** Graphical constants. */
	static final int			width = 220, height = 250;
	
	static final int			topBorder = 20, leftBorder = 40, 
								rightBorder = 10, square = 140, 
								bottomBorder = 30, bottomBorderSupp = 50,
								lS = leftBorder+square, tS = topBorder+square,
								lS4 = leftBorder+square/4, 
								lS2 = leftBorder+square/2;
								
	/** cursor triangle. */							
	static final int			triangleW = 10, triangleH = 11;
	
	/** Range values for LINEAR graphics type. */						   
	static final int			INIT = 10, MIN = 1, MAX = 40,
								rangeMin = INIT-MIN+1, rangeMax = MAX-INIT;
								
	/** Range values for LINEAR graphics type. */
	static final int			rangeMinExpo = 6, rangeMaxExpo = 7;	
	
	/** output range values. */
	static final int            outputRange = 255;		
		
	/** Color of the inputStart knob. */
	static final Color			iStartColor = Color.RED;
	
	/** Color of the inputStart knob. */
	static final Color			iEndColor = Color.GREEN;
	
	/** Color of the lines. */
	private static final Color	lineColor = Color.RED;
	
	/** Color of the outputStart knob. */
	private static final Color	ostartColor = Color.BLACK;
	
	/** Color of the outputEnd knob. */
	private static final Color	oendColor = Color.GRAY;
	
	/** Axis color. */
	private static final Color	axisColor = Color.GRAY;
			   
	/** Size of the bin. */
   	private int					binMin, binMax;
   	
   	/** Input window miminum and maximum values. */
   	private String				start, end;
   	
   	/** Current input window values. */
   	private String 				curStart, curEnd;
   	
   	/** Control points. */
   	private Point2D				startPt, endPt, controlPt, staticStartPt,
								staticEndPt;
								
	private QuadCurve2D.Double	quad;
	
   	private int					coefficient, controlOutputStart, 
								controlOutputEnd;
								
   	private boolean				reverseIntensity;

	/** Input knob coordinates. */
   	private int					xStart1, xStart2, xStart3, yStart1, yStart2, 
   								yStart3,
								xEnd1, xEnd2, xEnd3, yEnd1, yEnd2, yEnd3;
								
	/** Output knob coordinates. */
	private int				   	xStartOutput1, xStartOutput2, xStartOutput3, 
								yStartOutput1, yStartOutput2, yStartOutput3,
								xEndOutput1, xEndOutput2, xEndOutput3,
								yEndOutput1, yEndOutput2, yEndOutput3;
   	
   	/** family type. */
   	private int					type;
   	
   	/** graphics controls. */
   	private int					xStartMax, xControl, range;
   	
   	private int					minimum, maximum;
   	
   	private int					family, cdStart, cdEnd;
   	
   	private double				curveCoefficient;
   	
   	/** Reference to the manager. */
	private GraphicsRepresentationManager       manager;
	
	GraphicsRepresentation(QuantumPaneManager control, int family, 
							double curveCoefficient, int cdStart, int cdEnd,
							int min, int max)
	{
		this.family = family;
		this.curveCoefficient = curveCoefficient;
		this.cdStart = cdStart;
		this.cdEnd = cdEnd;
		type = family;
		reverseIntensity = false; //TODO: retrieve user settings
		manager = new GraphicsRepresentationManager(this, control, type);
		setInputWindow(min, max);
		minimum = min;
		maximum = max;
		quad = new QuadCurve2D.Double();
		startPt = new Point2D.Double();
		controlPt = new Point2D.Double();
		endPt = new Point2D.Double();
		staticStartPt = new Point2D.Double();
		staticEndPt = new Point2D.Double();
		//control output window to be removed
		controlOutputStart = square;
		controlOutputEnd = 0;
		xStartMax = leftBorder;
	}
	
	/** Returns a reference to the manager. */
	GraphicsRepresentationManager getManager() { return manager; }
	
	/**  Returns the input range. */
	int getInputGraphicsRange() { return range;}
	
	/** Set the reverse Intensity control. */
	void setReverseIntensity(boolean reverseIntensity)
	{ 
		this.reverseIntensity = reverseIntensity;
	}
	
	/** 
	 * Sets the defaults for the <code>LINEAR</code> type 
	 * (i.e. linear, polynomial, logarithmic).
	 * Sets the location of the five control points and the position
	 * of the knobs.
	 *
	 * @param inputStart        inputWindow start, real value.
	 * @param inputEnd          inputWindow end, real value.
	 */ 
	void setDefaultLinear(int inputStart, int inputEnd)
	{
		setCurrentInputs(inputStart, inputEnd);
		xControl = lS;
		range  = square;
		binMin = (int) (square/rangeMin);
		binMax = (int) (square/rangeMax);
		double yStart, yEnd, xStart, xEnd;
		yStart = setOuputGraphics(cdStart);
		yEnd = setOuputGraphics(cdEnd);
		xStart = setInputGraphics(inputStart, square); 
		xEnd = setInputGraphics(inputEnd, square);
		//Size the rectangles used to control knobs.
		//Input knob.
		setKnobStart((int) xStart);
		setKnobEnd((int) xEnd);
		manager.setInputRectangles((int) xStart, (int) xEnd);
		
		//Output knob.
		setKnobOutputStart(leftBorder-10, (int) yStart);
		setKnobOutputEnd(leftBorder-10, (int) topBorder);
		manager.setOutputRectangles((int) yStart, (int) yEnd);
		        
		//Control points location.
		if (reverseIntensity) {
			staticStartPt.setLocation((double) lS, yStart);
			staticEndPt.setLocation((double) leftBorder, yEnd);
			startPt.setLocation(xEnd, yStart);
			endPt.setLocation(xStart, yEnd);
		} else {
			staticStartPt.setLocation((double) leftBorder, yStart);
			startPt.setLocation(xStart, yStart);
			staticEndPt.setLocation((double) lS, yEnd);
			endPt.setLocation(xEnd, yEnd);
		}
		//draw curve
		int k;
		if (family == QuantumFactory.LINEAR) k = INIT;
		else if (family == QuantumFactory.LOGARITHMIC) k = MIN;
		else k = (int) (curveCoefficient*10);
		setControlLocation(k); 
	}
	
	/** 
	 * Sets the defaults for the <code>EXPONENTIAL</code> family.
	 * Set the location of the five control points and the position
	 * of the knobs.
	 *
	 * @param inputStart        inputWindow start, real value.
	 * @param inputEnd          inputWindow end, real value.
	 */    
	void setDefaultExponential(int inputStart, int inputEnd)
	{
		setCurrentInputs(inputStart, inputEnd);
		int k = (int) (curveCoefficient*10);
		binMin = (int) ((square-40)/(2*rangeMinExpo)); 
		binMax = (int) ((square-40)/(2*rangeMaxExpo));
		double yStart, yEnd, xStart, xEnd, xStaticStart, xStaticEnd;
		xStaticStart = 0;
		xStaticEnd = 0;
		yStart = setOuputGraphics(cdStart);
		yEnd = setOuputGraphics(cdEnd);

		//output knob
		setKnobOutputStart(leftBorder-10,(int) yStart);
		setKnobOutputEnd(leftBorder-10, (int) topBorder);
		manager.setOutputRectangles((int) yStart, (int) yEnd); 

		if (k == INIT) {
			if (reverseIntensity) {
				 xStaticStart = (double) lS2;
				 xStaticEnd = (double) leftBorder;
				 range = (int) xStaticStart-leftBorder;
			} else {
				 xStaticStart = (double) leftBorder;
				 xStaticEnd = (double) lS2;
				 range = (int) xStaticEnd-leftBorder;
			}
		} else if (MIN <= k && k < INIT) { 
			k = INIT-k;
			if (reverseIntensity) {
				xStaticEnd = (double) leftBorder;
				xStaticStart = (double) (lS2+k*binMax);
				range = (int) xStaticStart-leftBorder;
			} else {
				xStaticEnd = (double) (lS2+k*binMax);
				xStaticStart = (double) leftBorder;
				range = (int) xStaticEnd-leftBorder;
			} 
		} else if (k > INIT && k <= MAX) {
			k = k-INIT;
			if (reverseIntensity) {
				xStaticStart = (double) (lS-k*binMin);
				xStaticEnd = (double) leftBorder;
				if (xStaticStart < leftBorder+20) xStaticStart = leftBorder+20;
				range = (int) xStaticStart-leftBorder;
			} else {
				xStaticEnd = (double) (lS-k*binMin);
				xStaticStart = (double) leftBorder;
				if (xStaticEnd < leftBorder+20) xStaticEnd = leftBorder+20;
				range = (int) xStaticEnd-leftBorder;
			}
		}
		staticStartPt.setLocation(xStaticStart, yStart);
		staticEndPt.setLocation(xStaticEnd, yEnd);
		xStart = setInputGraphics(inputStart, range);
		xEnd = setInputGraphics(inputEnd, range);
		setKnobStart((int) xStart);
		setKnobEnd((int) xEnd);
		
		manager.setInputRectangles((int) xStart, (int) xEnd);
		if (reverseIntensity) {
			xControl = (int) xStaticStart;
			controlPt.setLocation(xEnd, yEnd);
			startPt.setLocation(xEnd, yStart);
			endPt.setLocation(xStart, yEnd);
		} else {
			xControl = (int) xStaticEnd;
			controlPt.setLocation(xEnd, yStart);
			startPt.setLocation(xStart, yStart);
			endPt.setLocation(xEnd, yEnd);
		}
		manager.setMaxEndX(xControl);
		repaintCurve();     
	}  
	
	/** 
	 * Resets the control points locations. 
	 * This method is called when a new family is specified.
	 *
	 * @param t		family index.
	 */
	void setControlsPoints(int t)
	{
		type = t;
		double xStaticEnd, xStaticStart, xEnd, xStart;
		xStaticEnd = 0;
		xStaticStart = 0;
		xEnd = 0;
		xStart = 0;
		if (type == QuantumFactory.EXPONENTIAL) {
			xControl = lS2;
			range = square/2;
			binMin = (int) ((square-40)/(2*rangeMinExpo)); 
			binMax = (int) ((square-40)/(2*rangeMaxExpo));
			if (reverseIntensity) { 
				xStaticStart = (double) lS2;
				xStaticEnd = (double) leftBorder;
				xEnd = (endPt.getX()+leftBorder)/2;
				xStart = (startPt.getX()+leftBorder)/2;
			} else {
				xEnd = (endPt.getX()+leftBorder)/2;
				xStart = (startPt.getX()+leftBorder)/2;
				xStaticEnd = (double) lS2;
				xStaticStart = (double) leftBorder;
			}  
		} else {
			xControl = lS;
			range = square;
			double a;
			binMin = (int) (square/rangeMin);
			binMax = (int) (square/rangeMax);
			if (reverseIntensity) {
				a = (double) square/(staticStartPt.getX()-leftBorder);
				xStaticStart = (double) lS;
				xStaticEnd = (double) leftBorder;
				xEnd = a*(endPt.getX()-leftBorder)+leftBorder;
				xStart = a*(startPt.getX()-leftBorder)+leftBorder;
			} else { 
				a = (double) square/(staticEndPt.getX()-leftBorder); 
				xEnd = a*(endPt.getX()-leftBorder)+leftBorder;
				xStart = a*(startPt.getX()-leftBorder)+leftBorder;
				xStaticEnd = (double) lS;
				xStaticStart = (double) leftBorder;
			}  
		}
		manager.setType(type, xControl);
		staticEndPt.setLocation(xStaticEnd, staticEndPt.getY());
		staticStartPt.setLocation(xStaticStart, staticStartPt.getY());
		endPt.setLocation(xEnd, endPt.getY());
		startPt.setLocation(xStart, startPt.getY());
		
		//Set knob location
		if (reverseIntensity) {
			setKnobStart((int) xEnd);
			setKnobEnd((int) xStart);
			manager.setInputRectangles((int) xEnd, (int) xStart);
		} else {
			setKnobStart((int) xStart);
			setKnobEnd((int) xEnd);
			manager.setInputRectangles((int) xStart, (int) xEnd);
		}       
	}
	
	/** 
	 * Resets the start and End maximum value and positions the inputWindow 
	 * knobs. 
	 * This method is called when a new wavelength is specified.
	 *
	 * @param min			minimum value for the selected wavelength.
	 * @param max			maximum value for the selected wavelength.
	 * @param xStart		inputStart coordinate.
	 * @param xEnd			inputEnd coordinate.
	 * @param xRealStart	inputStart value;
	 * @param xRealEnd		inputEnd value.	 
	 */
	void setInputs(int min, int max, int xStart, int xEnd, int xRealStart,
				int xRealEnd)
	{
		setInputWindow(min, max);
		setCurrentInputs(xRealStart, xRealEnd);
		minimum = min;
		maximum = max;
		updateInputStartAndEnd(xStart, xEnd);
	} 
	  
	/** 
	 * Positions the control points. 
	 * The method is invoked when the family specified falls into 
	 * the graphical <code>LINEAR</code> type.
	 *
	 * @param k     curveCoefficient.
	 */
	void setControlLocation(int k)
	{
		double x = 0, y = 0;
		coefficient = k;
		double diff;
		if (reverseIntensity) diff = startPt.getX()-endPt.getX();
		else diff = endPt.getX()-startPt.getX();
		binMin = (int) (diff/rangeMin);
		binMax = (int) (diff/rangeMax);
		if (k == INIT) {
				x = startPt.getX();
				y = startPt.getY();
		} else if (MIN <= k && k < INIT) {
			 if (reverseIntensity) {
				x = endPt.getX()+(k-1)*binMin; 
				y = startPt.getY();
			} else {
				x = startPt.getX() + (k-1)*binMin; 
				y = endPt.getY();
			}
		} else if (k > INIT && k <= MAX) {
			 if (reverseIntensity) {
				x = endPt.getX()+(k-INIT)*binMax;
				y = endPt.getY();
			} else {
				x = startPt.getX()+(k-INIT)*binMax;  
				y = startPt.getY();
			}
		}
		controlPt.setLocation(x, y);
		repaintCurve();
	}
	
	/** 
	 * Positions the control points. 
	 * The method is invoked when the family specified falls into 
	 * the <code>EXPONENTIAL</code> type.
	 *
	 * @param k     curveCoefficient.
	 */
	void setControlAndEndLocation(int k)
	{
		double x = 0;
		double diffEnd;
		coefficient = k;
		if (k == INIT) {
			if (reverseIntensity) 
					x = (double) (staticEndPt.getX()+square/2); 
			else 	x = (double) (staticStartPt.getX()+square/2);  
		} else if (MIN <= k && k < INIT) { 
			k = INIT-k;
			if (reverseIntensity) 
					x = (double) (staticEndPt.getX()+square/2+k*binMax); 
			else 	x = (double) (staticStartPt.getX()+square/2+k*binMax); 
		} else if (k > INIT && k <= MAX) {
			k = k-INIT;
			if (reverseIntensity)
					x = (double) (staticEndPt.getX()+square/2-k*binMin);
			else 	x = (double) (staticStartPt.getX()+square/2-k*binMin);
		}
		xControl = (int) x;         
		if (x >= xStartMax+triangleW) {
			if (reverseIntensity) {
				diffEnd = staticStartPt.getX()-startPt.getX();
				startPt.setLocation(x-diffEnd, startPt.getY());
				controlPt.setLocation(x-diffEnd, endPt.getY());
				staticStartPt.setLocation(x, staticStartPt.getY());
				manager.setInputEndBox((int) (x-diffEnd));
				setKnobEnd((int) (x-diffEnd));     
			} else {
				diffEnd = staticEndPt.getX()-endPt.getX();
				endPt.setLocation(x-diffEnd, endPt.getY()); 
				controlPt.setLocation(x-diffEnd, startPt.getY());
				staticEndPt.setLocation(x, staticEndPt.getY());
				manager.setInputEndBox((int) (x-diffEnd));
				setKnobEnd((int) (x-diffEnd));
			}
			range = (int) x-leftBorder;
			manager.setMaxEndX(xControl);
			repaintCurve();
		}      
	}
	
	/** 
	 * Reverses the curve: re-positions the control points.
	 * <p>
	 * Example: by default if LINEAR and reverse == false,
	 * the staticStartControl is in the bottom-left corner, if reverse
	 * is false the location is bottom-right corner.
	 * </p>
	 * @param b     true/false.
	 */
	void reverse(boolean b)
	{
		double yStart, yEnd;
		double xStaticStart, xStart;
		reverseIntensity = b;
		yEnd = endPt.getY();
		yStart = startPt.getY();
		setKnobOutputStart((int) yStart);
		setKnobOutputEnd((int) yEnd);
		manager.setOutputRectangles((int) yStart, (int) yEnd);
		xStaticStart = staticStartPt.getX();
		xStart = startPt.getX();
		startPt.setLocation(endPt.getX(), yStart);
		endPt.setLocation(xStart, yEnd);
		if (reverseIntensity) {
			if (type == QuantumFactory.EXPONENTIAL) {
				staticStartPt.setLocation(staticEndPt.getX(), yStart);
				staticEndPt.setLocation(xStaticStart, yEnd);
				controlPt.setLocation(controlPt.getX(), yEnd);
				repaintCurve();
			} else {
				staticStartPt.setLocation((double) lS, yStart);
				staticEndPt.setLocation((double) leftBorder, yEnd);
				setControlLocation(coefficient);
			}
		} else {
			if (type == QuantumFactory.EXPONENTIAL) {
				staticStartPt.setLocation(staticEndPt.getX(), yStart);
				staticEndPt.setLocation(xStaticStart, yEnd);
				controlPt.setLocation(controlPt.getX(), yStart);
				repaintCurve();
			} else {
				staticStartPt.setLocation((double) leftBorder, yStart);
				staticEndPt.setLocation((double) lS, yEnd);
				setControlLocation(coefficient);
			}
		}  
	}
	
	/** 
	 * Modifies the control point positions and positions the 
	 * InputStart knob. Method invoked when the position is modified
	 * using the HistogramPanel.
	 *
	 * @param  x    x-coordinate.
	 */
	void updateInputStart(int x, int xReal)
	{
		curStart = "start: "+xReal;
		/* limit control b/c the method can be called
		   via the histogram Dialog widget	
		*/
		boolean b = true;
		/* control to synchronize the display of real value
		 using the Histogram widget the user can still size the inputWindow
		 but for graphics reasons the cursor in the curvePanel cannot be 
		 repainted (cf. limit control below.).
		*/
		int limit;
		if (reverseIntensity) limit = (int) startPt.getX()-triangleW;
		else limit = (int) endPt.getX()-triangleW;
		if (x <= limit) { 
			xStartMax = x;
			b = false;
			setKnobStart(x);
			if (reverseIntensity) endPt.setLocation((double) x, endPt.getY());
			else startPt.setLocation((double) x, startPt.getY());
			if (type == QuantumFactory.EXPONENTIAL) repaintCurve();
			else setControlLocation(coefficient);
		}
		if (b) repaint(0, tS+bottomBorder, width, bottomBorderSupp);
	}
	
	/** 
	 * Modifies the control point position and positions the 
	 * InputEnd knob. The method is invoked when the position is modified
	 * using the HistogramPanel.
	 *
	 * @param  x    x-coordinate.
	 */    
	void updateInputEnd(int x, int xReal)
	{
		curEnd = "end: "+xReal;
		/* limit control b/c the method can be called
		   via the histogram Dialog widget	
		*/
		boolean b = true;
		/* control to synchronize the display of real value
		 using the Histogram widget the user can still size the inputWindow
		 but for graphics reasons the cursor in the curvePanel cannot be 
		 repainted (cf. limit control below.).
		*/
		int limit;
		if (reverseIntensity) limit = (int) endPt.getX()-triangleW;
		else limit = (int) startPt.getX()+triangleW;
		if (x >= limit) {
			setKnobEnd(x);
			b = false;
			if (reverseIntensity) 
					startPt.setLocation((double) x, startPt.getY());
			else    endPt.setLocation((double) x, endPt.getY());
			if (type == QuantumFactory.EXPONENTIAL) {
				if (reverseIntensity)  
						controlPt.setLocation((double) x, endPt.getY());
				else    controlPt.setLocation((double) x, startPt.getY());
				repaintCurve();
			} else	setControlLocation(coefficient);
		}
		if (b) repaint(0, tS+bottomBorder, width, bottomBorderSupp);
	}
	
	/** 
	 * Modifies the position of the outputStart knob.
	 *
	 * @param y		y-coordinate.
	 */
	void updateOutputStart(int y)
	{
		controlOutputStart = y-topBorder;
		setKnobOutputStart(y);
		startPt.setLocation(startPt.getX(), (double) y);
		staticStartPt.setLocation(staticStartPt.getX(), (double) y);
		if (type == QuantumFactory.EXPONENTIAL) {
			if (reverseIntensity)
					controlPt.setLocation(controlPt.getX(), endPt.getY());
			else    controlPt.setLocation(controlPt.getX(), (double) y);
			repaintCurve();
		} else	setControlLocation(coefficient); 
	}
	
	/** 
	 * Modifies the position of the outputEnd knob.
	 *
	 * @param y     y-coordinate.
	 */
	void updateOutputEnd(int y)
	{
		controlOutputEnd = y-topBorder;
		setKnobOutputEnd(y);
		endPt.setLocation(endPt.getX(), (double) y);
		staticEndPt.setLocation(staticEndPt.getX(), (double) y);
		setControlLocation(coefficient);
		if (type == QuantumFactory.EXPONENTIAL) {
			if (reverseIntensity) controlPt.setLocation(startPt.getX(), 
														(double) y);
			else controlPt.setLocation(endPt.getX(), controlPt.getY());
			repaintCurve();
		} else	setControlLocation(coefficient);
	}
	
	/** 
	 * Sets the current input window string.
	 * @param s		input start value.
	 * @param e		input end value.
	 */
	private void setCurrentInputs(int s, int e)
	{
		curStart = "start: "+s;
		curEnd = "end: "+e;
	}

	/**
	 * 
	 * @param s		minimum value for the input window.
	 * @param e		maximum value for the input window.
	 */
	private void setInputWindow(int s, int e)
	{
		start = ""+s;
		end = ""+e;
	}
	
	/** 
	 * Modifies the control points positions and positions the knobs 
	 * InputStart/InpuEnd accordingly.
	 * The method is invoked when a new wavelength is specified. 
	 *
	 * @param  xStart	xStart-coordinate.
	 * @param  xEnd		xEnd-coordinate.
	 */    
	private void updateInputStartAndEnd(int xStart, int xEnd)
	{
		xStartMax = xStart;
		setKnobEnd(xEnd);
		setKnobStart(xStart);
		if (reverseIntensity) {
			endPt.setLocation((double) xStart, endPt.getY());
			startPt.setLocation((double) xEnd, startPt.getY());
		} else {
			startPt.setLocation((double) xStart, startPt.getY());
			endPt.setLocation((double) xEnd, endPt.getY()); 
		} 
		if (type == QuantumFactory.EXPONENTIAL) {
			if (reverseIntensity)  
					controlPt.setLocation((double) xEnd, endPt.getY());
			else    controlPt.setLocation((double) xEnd, startPt.getY());
			repaintCurve();
		} else	setControlLocation(coefficient);
	}
	
	/**
	 * Converts a real output value into a y-coordinate.
	 * 
	 * @param x		real value.
	 * @return a 	coordinate in the graphic system.
	 */    
	private double setOuputGraphics(int x)
	{
		double a = (double) square/outputRange;
		return (double) (tS-x*a);
	}
	
	/**
	 * Converts a real input value into graphical value.
	 * 
	 * @param x		real value.
	 * @return a 	coordinate in the graphic system.
	 */      
	private double setInputGraphics(int x, int r)
	{
		double b = (double) r/(maximum-minimum);
		return (double) (b*(x-minimum)+leftBorder); 
	}

	/** 
	 * Positions the inputStart knob, x-coordinate location.
	 *
	 * @param x     x-coordinate.
	 */
	private void setKnobStart(int x)
	{  
		xStart1 = x;
		xStart2 = x-triangleW;
		xStart3 = x+triangleW;
	}
	
	/** 
	 * Positions the inputEnd knob, x-coordinate location.
	 *
	 * @param x     x-coordinate.
	 */
	private void setKnobEnd(int x)
	{
		xEnd1 = x;
		xEnd2 = x-triangleW;
		xEnd3 = x+triangleW;
	}
	
	/** 
	 * Positions the inputStart/End knobs, y-coordinate location.
	 *
	 * @param y     y-coordinate.
	 */     
	private void setKnobStartEndY(int y)
	{
		yStart1 = y;
		yStart2 = y+triangleH;
		yStart3 = y+triangleH;
		yEnd1 = y;
		yEnd2 = y+triangleH;
		yEnd3 = y+triangleH;
	}
	
	/** 
	 * Positions the outputStart knob.
	 *
	 * @param x     x-coordinate.
	 * @param y     y-coordinate.
	 */    
	private void setKnobOutputStart(int x, int y)
	{  
		xStartOutput1 = x;
		xStartOutput2 = x-triangleH;
		xStartOutput3 = x-triangleH;
		yStartOutput1 = y;
		yStartOutput2 = y-triangleW;
		yStartOutput3 = y+triangleW;
	} 
	
	/** 
	 * Positions the outputEnd knob.
	 *
	 * @param x     x-coordinate.
	 * @param y     y-coordinate.
	 */        
	private void setKnobOutputEnd(int x, int y)
	{
		xEndOutput1 = x;
		xEndOutput2 = x-triangleH;
		xEndOutput3 = x-triangleH;
		yEndOutput1 = y;
		yEndOutput2 = y-triangleW;
		yEndOutput3 = y+triangleW;
	}
	
	/** 
	 * Positions the outputStart knob, y-coordinate location.
	 *
	 * @param y     y-coordinate.
	 */  
	private void setKnobOutputStart(int y)
	{
		yStartOutput1 = y;
		yStartOutput2 = y-triangleW;
		yStartOutput3 = y+triangleW;
	}
	
	/** 
	 * Positions the outputEnd knob, y-coordinate location.
	 *
	 * @param y     y-coordinate.
	 */
	private void setKnobOutputEnd(int y)
	{
		yEndOutput1 = y;
		yEndOutput2 = y-triangleW;
		yEndOutput3 = y+triangleW;
	}
	
	/** Method called when the family type is EXPONENTIAL. */
	private void repaintCurve()
	{
		quad.setCurve(startPt, controlPt, endPt);
		repaint();
	}
	
	/** Overrides the paintComponent() method. */ 
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2D = (Graphics2D) g;
		g2D.setColor(getBackground());
		g2D.fillRect(0, 0, width, height);
		//FONT settings.
		Font font = g2D.getFont();
		FontMetrics fontMetrics = g2D.getFontMetrics();
		int hFont = fontMetrics.getHeight();
		Rectangle2D rStart = font.getStringBounds(start,
									g2D.getFontRenderContext());
		int wStart = (int) rStart.getWidth();
		int hStart = (int) rStart.getHeight();
		Rectangle2D rEnd = font.getStringBounds(end, 
									g2D.getFontRenderContext());
		int wEnd = (int) rEnd.getWidth();
		Rectangle2D rInput = font.getStringBounds("Pixel intensity",
											g2D.getFontRenderContext());
		int wInput = (int) rInput.getWidth();
		int hInput = (int) rInput.getHeight();
	
		setKnobStartEndY(tS+hStart+5);
		int extra = 0;
		if (type == QuantumFactory.EXPONENTIAL) extra = hStart/2;
		//Grid
		AffineTransform transform = new AffineTransform();
		//140/10 = 14 then middle = 14/2
		transform.translate(leftBorder+70, topBorder+70); 
		transform.scale(1, -1);
		transform.scale(10, 10);       
		g2D.setPaint(Color.lightGray);
		GeneralPath path = new GeneralPath();
		for (int i = -7; i <= 7; i++) {
			path.moveTo(i, -7);
			path.lineTo(i, 7);
		}
		for (int i = -7; i <= 7; i++) {
			path.moveTo(-7, i);
			path.lineTo(7, i);
		}
		g2D.draw(transform.createTransformedShape(path));
		g2D.setColor(axisColor);
		
		//y-axis
		g2D.drawLine(leftBorder, topBorder-8, leftBorder, tS+5);
		g2D.drawLine(leftBorder, topBorder-8, leftBorder-3, topBorder-5);
		g2D.drawLine(leftBorder, topBorder-8, leftBorder+3, topBorder-5);
		g2D.drawLine(leftBorder-5, topBorder, leftBorder, topBorder);
		
		//x-axis
		g2D.drawLine(leftBorder-5, tS, lS+8, tS);
		g2D.drawLine(lS+5, tS-3, lS+8, tS);
		g2D.drawLine(lS+5, tS+3, lS+8, tS);
		g2D.drawLine(xControl, tS, xControl, tS+5);
		
		//input interval
		g2D.drawString(start, leftBorder-wStart/2, tS+hFont);
		g2D.drawString(end, xControl-wEnd/2, hFont+tS+extra);
		g2D.drawString("Pixels intensity", lS2-wInput/2, 
						hFont/2+tS+bottomBorder+hInput);
		//g2D.drawString(curStart, 10, hFont+tS+bottomBorder+2*hInput);
		//g2D.drawString(curEnd, lS2+10, hFont+tS+bottomBorder+2*hInput);
		
		//inputStart knob
		int xStartPoints[] = {xStart1, xStart2, xStart3};
		int yStartPoints[] = {yStart1+extra, yStart2+extra, yStart3+extra};
		GeneralPath filledPolygonStart = new GeneralPath();
		filledPolygonStart.moveTo(xStartPoints[0], yStartPoints[0]);
		for (int index = 1; index < xStartPoints.length; index++)
			filledPolygonStart.lineTo(xStartPoints[index], yStartPoints[index]);
		filledPolygonStart.closePath();
		g2D.setColor(iStartColor);
		g2D.fill(filledPolygonStart);
		//curStart value
		g2D.drawString(curStart, 10, hFont+tS+bottomBorder+2*hInput);
		
		
		//inputEnd knob
		int xEndPoints[] = {xEnd1, xEnd2, xEnd3};
		int yEndPoints[] = {yEnd1+extra, yEnd2+extra, yEnd3+extra};
		GeneralPath filledPolygonEnd = new GeneralPath();
		filledPolygonEnd.moveTo(xEndPoints[0], yEndPoints[0]);
		for (int index = 1; index < xEndPoints.length; index++)
			filledPolygonEnd.lineTo(xEndPoints[index], yEndPoints[index]);
		filledPolygonEnd.closePath();
		
		g2D.setColor(iEndColor);
		g2D.fill(filledPolygonEnd);
		//curEnd value.
		g2D.drawString(curEnd, lS2+10, hFont+tS+bottomBorder+2*hInput);
		
		//outputStart knob
		int xStartOutputPoints[] = {xStartOutput1, xStartOutput2, 
									xStartOutput3};
		int yStartOutputPoints[] = {yStartOutput1, yStartOutput2, 
									yStartOutput3};
		GeneralPath filledPolygonStartOutput = new GeneralPath();
		filledPolygonStartOutput.moveTo(xStartOutputPoints[0],
										 yStartOutputPoints[0]);
		for (int index = 1; index < xStartOutputPoints.length; index++)
			filledPolygonStartOutput.lineTo(xStartOutputPoints[index], 
											yStartOutputPoints[index]);
		filledPolygonStartOutput.closePath();
		g2D.setColor(ostartColor);
		g2D.fill(filledPolygonStartOutput);
		
		//outputEnd knob. 
		int xEndOutputPoints[] = {xEndOutput1, xEndOutput2, xEndOutput3};
		int yEndOutputPoints[] = {yEndOutput1, yEndOutput2, yEndOutput3};
		GeneralPath filledPolygonEndOutput = new GeneralPath();
		filledPolygonEndOutput.moveTo(xEndOutputPoints[0], yEndOutputPoints[0]);
		for (int index = 1; index < xEndOutputPoints.length; index++)
			filledPolygonEndOutput.lineTo(xEndOutputPoints[index], 
										yEndOutputPoints[index]);
		filledPolygonEndOutput.closePath();
		g2D.setColor(oendColor);
		g2D.fill(filledPolygonEndOutput); 
		g2D.setColor(lineColor);
		g2D.setStroke(new BasicStroke(1.5f));
		//draw line
		g2D.drawLine((int) staticStartPt.getX(), (int) staticStartPt.getY(), 
					(int) startPt.getX(), (int) startPt.getY());
		g2D.drawLine((int) endPt.getX(), (int) endPt.getY(),
					(int) staticEndPt.getX(), (int) staticEndPt.getY());
		//draw curve
		g2D.draw(quad);
	}
	
}

