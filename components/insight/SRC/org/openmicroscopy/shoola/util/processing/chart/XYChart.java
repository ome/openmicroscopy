package org.openmicroscopy.shoola.util.processing.chart;
import java.util.List;

import org.gicentre.utils.colour.ColourTable;
import org.gicentre.utils.stat.AbstractChart;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

// *****************************************************************************************
/** Class for representing X-Y charts such as scatterplots or line charts.
 *  @author Jo Wood, giCentre, City University London.
 *  @version 3.1, 18th February, 2011.
 */ 
// *****************************************************************************************

/* This file is part of giCentre utilities library. gicentre.utils is free software: you can 
 * redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * gicentre.utils is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this
 * source code (see COPYING.LESSER included with this source code). If not, see 
 * http://www.gnu.org/licenses/.
 */

public class XYChart extends AbstractChart
{
    // The XY chart stores the 'x' variable in dimension 0 of the chart, the 'y' variable
    // in dimension 1, the optional colour variable in dimension 2 and the optional 
    // size variable in dimension 3.
    
    // ----------------------------- Object variables ------------------------------
    
    protected int pointColour,lineColour;         // Colour of point and line symbols to draw.
    protected float pointSize, lineWidth;         // Size of point and line symbols to draw in pixel units.
    protected ColourTable cTable;                 // Used to colour data points.
    protected float maxPointSize;                 // Maximum point size (used when sizing points by data value).
    protected Float xAxisPosition,yAxisPosition;  // Positions of the axes (if null, defaults to min values).
    protected String xLabel, yLabel;              // Axis labels.
    protected float top,left,bottom,right;        // Bounds of the data area (excludes axes and axis labels).
  
    // ------------------------------- Constructors --------------------------------

    /** Initialises an XY chart.
     *  @param parent Parent sketch in which this chart is to be drawn.
     */
    public XYChart(PApplet parent)
    {
        super(parent);
        pointColour   = parent.color(120);
        lineColour    = parent.color(120);
        pointSize     = 4;
        lineWidth     = 0;
        maxPointSize  = 0;
        cTable        = null;
        xAxisPosition = null;
        yAxisPosition = null;
        xLabel        = null;
        yLabel        = null;
        top           = 0;
        bottom        = 0;
        left          = 0;
        right         = 0;
    }
    
    // ---------------------------------- Methods ----------------------------------
    
    
    /** Sets the data to be plotted in the X-Y chart. The arrays of x and y values supplied
     *  must be the same length for this method to have any effect.
     *  @param xValues Array of x values to plot.
     *  @param yValues Array of y values to plot.
     */
    public void setData(float[] xValues, float[]yValues)
    {
        if (xValues.length != yValues.length)
        {
            System.err.println("Warning: Number of x and y coordinates given to XYChart.setData() do not match ("+xValues.length+" and "+yValues.length+").");
            return;
        }
        
        setData(0,xValues);
        setData(1,yValues);
    }
    
    /** Sets the data to be plotted in the X-Y chart
     *  @param data Collection of pairs of (x,y) data values, each stored in a PVector.
     */
    public void setData(List<PVector> data)
    {
        float[] xVals = new float[data.size()];
        float[] yVals = new float[data.size()];
        
        for (int i=0; i<data.size(); i++)
        {
            PVector dataPair = data.get(i);
            xVals[i] = dataPair.x;
            yVals[i] = dataPair.y;
        }
        setData(0,xVals);
        setData(1,yVals);
    }
    
    /** Draws the X-Y chart within the given bounds.
     *  @param xOrigin left-hand pixel coordinate of the area in which to draw the chart.
     *  @param yOrigin top pixel coordinate of the area in which to draw the chart.
     *  @param width Width in pixels of the area in which to draw the chart.
     *  @param height Height in pixels of the area in which to draw the chart.
     */
    public void draw(float xOrigin, float yOrigin, float width, float height)
    {
        if ((data[0] == null) || (data[1] == null))
        {
            return;
        }

        parent.pushMatrix();
        parent.pushStyle();
        
        // Use a local coordinate system with origin at top-left of drawing area.
        parent.translate(xOrigin,yOrigin);
        
        // Extra spacing required to fit axis labels. This can't be handled by the AbstractChart
        // because not all charts label their axes in the same way.
        
        float extraLeftBorder   =2;
        float extraRightBorder  =2;
        float extraTopBorder    =2;
        float extraBottomBorder =2;
         
        // Allow space to the right of the horizontal axis to accommodate right-hand tic label.
        if ((getShowAxis(0)) || ((transposeAxes) && (getShowAxis(1))))
        {
            int axis = transposeAxes?1:0;
            String lastLabel = axisFormatter[axis].format(tics[axis][tics[axis].length-1]);
            extraRightBorder += parent.textWidth(lastLabel)/2f;
        }
        
        // Allow space above the vertical axis to accommodate the top tic label.
        if ((getShowAxis(1)) || ((transposeAxes) && (getShowAxis(0))))
        {   
            extraTopBorder += parent.textAscent()/2f+2;
        }
        
        // Allow space to the left of the vertical axis to accommodate its label.
        if (((yLabel != null) && getShowAxis(1)) || ((transposeAxes) && (xLabel != null) && getShowAxis(0)))
        {
            extraLeftBorder += parent.textAscent()+parent.textDescent();
        }
        
        // Allow space below the horizontal axis to accommodate its label.
        if (((xLabel != null) && getShowAxis(0)) || ((transposeAxes) && (yLabel != null) && getShowAxis(1)))
        {
            extraBottomBorder +=parent.textAscent()+parent.textDescent();
        }  
        
        left   = getBorder(Side.LEFT) + extraLeftBorder;
        right  = width - (getBorder(Side.RIGHT)+extraRightBorder);
        bottom = height-(getBorder(Side.BOTTOM)+extraBottomBorder);
        top    = getBorder(Side.TOP)+extraTopBorder;
        float hRange = right-left;
        float vRange = bottom-top;
                      
        // Draw line if requested.
        if (lineWidth > 0)
        {
            parent.noFill();
            parent.stroke(lineColour);
            parent.strokeWeight(lineWidth);
            
            parent.beginShape();
            
            for (int i=0; i<data[0].length; i++)
            {
                float x,y;
                float xValue = Math.max(Math.min(data[0][i],getMax(0)),getMin(0));
                float yValue = Math.max(Math.min(data[1][i],getMax(1)),getMin(1));
                
                if (getIsLogScale(0))
                {
                    x = convertToLog(xValue, getMinLog(0), getMaxLog(0));
                }
                else
                {
                    x = (xValue-getMin(0))/(getMax(0)-getMin(0));
                }
                if (getIsLogScale(1))
                {
                    y = convertToLog(yValue, getMinLog(1), getMaxLog(1));
                }
                else
                {
                    y = (yValue-getMin(1))/(getMax(1)-getMin(1));
                }
                
                if (transposeAxes)
                {
                    parent.vertex(left + hRange*y, bottom-vRange*x);
                }
                else
                {
                    parent.vertex(left + hRange*x, bottom - vRange*y);
                }
            }
            parent.endShape();
        }
        
        // Draw points if requested. 
        if ((pointSize > 0) || (maxPointSize > 0))
        {
            if (cTable == null)
            {
                parent.fill(pointColour);
            }
            parent.noStroke();
            
            for (int i=0; i<data[0].length; i++)
            {
                if (cTable != null)
                {
                    parent.fill(cTable.findColour(data[2][i]));
                }
                
                float x,y;        
                float xValue = Math.max(Math.min(data[0][i],getMax(0)),getMin(0));
                float yValue = Math.max(Math.min(data[1][i],getMax(1)),getMin(1));
                
                if (getIsLogScale(0))
                {
                    x = convertToLog(xValue, getMinLog(0), getMaxLog(0));
                }
                else
                {
                    x = (xValue-getMin(0))/(getMax(0)-getMin(0));
                }
                if (getIsLogScale(1))
                {
                    y = convertToLog(yValue, getMinLog(1), getMaxLog(1));
                }
                else
                {
                    y = (yValue-getMin(1))/(getMax(1)-getMin(1));
                }
                
                if (maxPointSize > 0)
                {
                    // Size by data.
                    float radius = (float)(maxPointSize*Math.sqrt(data[3][i]/getMax(3)));
                    
                    if (transposeAxes)
                    {
                        parent.ellipse(left + hRange*y, bottom-vRange*x,radius,radius);
                    }
                    else
                    {
                        parent.ellipse(left + hRange*x, bottom - vRange*y,radius,radius);
                    }
                }
                else
                {
                    // Uniform size.
                    if (transposeAxes)
                    {
                        parent.ellipse(left + hRange*y, bottom - vRange*x,pointSize,pointSize);
                    }
                    else
                    {
                        parent.ellipse(left + hRange*x, bottom - vRange*y,pointSize,pointSize);
                    }
                }
            }
        }
        
        float textHeight = parent.textAscent();
                
        // Check to see if we have special case where x and y axes have the same origin, so only display it once.
        boolean showSingleOriginValue = false;
        if ((getShowAxis(0) && getShowAxis(1)) && (axisFormatter[0].format(tics[0][0]).equals(axisFormatter[1].format(tics[1][0]))))
        {
            showSingleOriginValue = true;
        }
        int firstTic = showSingleOriginValue?1:0;
             
        // Draw axes if requested.
        if (getShowAxis(0))
        {
            parent.strokeWeight(0.5f);
            parent.stroke(120);
            parent.fill(0,150);
            
            // Calculate position of axis.
            float axisPosition;
            if (transposeAxes)
            {
                axisPosition = left;
                if (xAxisPosition != null)
                {
                    if (getIsLogScale(0))
                    {
                        axisPosition = (float)(left +hRange*(Math.log10(xAxisPosition.doubleValue())-getMinLog(1))/(getMaxLog(1)-getMinLog(1)));
                    }
                    else
                    {
                        axisPosition = (float)(left +hRange*(xAxisPosition.doubleValue()-getMin(1))/(getMax(1)-getMin(1))); 
                    }
                }
                parent.line(axisPosition,bottom,axisPosition,top);
            }
            else
            {
                axisPosition = bottom;
                if (xAxisPosition != null)
                {
                    if (getIsLogScale(0))
                    {
                        axisPosition = (float)(top +vRange*(getMaxLog(1)-Math.log10(xAxisPosition.doubleValue()))/(getMaxLog(1)-getMinLog(1)));
                    }
                    else
                    {
                        axisPosition = (float)(top +vRange*(getMax(1)-xAxisPosition.doubleValue())/(getMax(1)-getMin(1))); 
                    }
                }
                parent.line(left,axisPosition,right,axisPosition);   
            }

            // Draw axis labels.
            if (getIsLogScale(0))
            {                         
                for (int i=firstTic; i<logTics[0].length; i++)
                {
                    float logTic = logTics[0][i];
                    float tic = (float)Math.pow(10,logTic);
                    
                    if (tic <= getMax(0))
                    {
                        if (transposeAxes)
                        {
                            parent.textAlign(PConstants.RIGHT, PConstants.CENTER);
                            parent.text(axisFormatter[0].format(tic),axisPosition-2,top +vRange*(getMaxLog(0)-logTic)/(getMaxLog(0)-getMinLog(0)));
                        }
                        else
                        {
                            parent.textAlign(PConstants.CENTER, PConstants.TOP);
                            parent.text(axisFormatter[0].format(tic),left +hRange*(logTic-getMinLog(0))/(getMaxLog(0)-getMinLog(0)),axisPosition+2);
                        }
                    }
                }   
            }
            else
            {
                for (int i=firstTic; i<tics[0].length; i++)
                {
                    float tic = tics[0][i];
                    if (tic <= getMax(0))
                    {
                        if (transposeAxes)
                        {
                            parent.textAlign(PConstants.RIGHT, PConstants.CENTER);
                            parent.text(axisFormatter[0].format(tic),axisPosition-2,top +vRange*(getMax(0)-tic)/(getMax(0)-getMin(0)));
                        }
                        else
                        {
                            parent.textAlign(PConstants.CENTER, PConstants.TOP);
                            parent.text(axisFormatter[0].format(tic),left +hRange*(tic-getMin(0))/(getMax(0)-getMin(0)),axisPosition+2);
                        }
                    }
                }
            }
            
            // Draw axis label if requested
            if (xLabel != null)
            {
                if (transposeAxes)
                {
                    parent.textAlign(PConstants.CENTER,PConstants.BOTTOM);
                    // Rotate label.
                    parent.pushMatrix();
                     parent.translate(axisPosition-(getBorder(Side.LEFT)+1),(top+bottom)/2f);
                     parent.rotate(-PConstants.HALF_PI);
                     parent.text(xLabel,0,0);
                    parent.popMatrix();
                }
                else
                {
                    parent.textAlign(PConstants.CENTER,PConstants.TOP);
                    parent.text(xLabel,(left+right)/2f,axisPosition+getBorder(Side.BOTTOM)+2);
                }
            }
        }
        
        if (getShowAxis(1))
        {
            parent.strokeWeight(0.5f);
            parent.stroke(120);
            parent.fill(0,150);
            
            // Calculate position of axis.
            float axisPosition;
            if (transposeAxes)
            {
                axisPosition = bottom;
                if (yAxisPosition != null)
                {
                    if (getIsLogScale(1))
                    {
                        axisPosition = (float)(top +vRange*(getMaxLog(0)-Math.log10(yAxisPosition.doubleValue()))/(getMaxLog(0)-getMinLog(0)));
                    }
                    else
                    {
                        axisPosition = (float)(top +vRange*(getMax(0)-yAxisPosition.doubleValue())/(getMax(0)-getMin(0))); 
                    }
                }
                parent.line(left,axisPosition,right,axisPosition);
            }
            else
            {
                axisPosition = left;
                if (yAxisPosition != null)
                {
                    if (getIsLogScale(1))
                    {
                        axisPosition = (float)(left +hRange*(Math.log10(yAxisPosition.doubleValue())-getMinLog(0))/(getMaxLog(0)-getMinLog(0)));
                    }
                    else
                    {
                        axisPosition = (float)(left +hRange*(yAxisPosition.doubleValue()-getMin(0))/(getMax(0)-getMin(0))); 
                    }
                }
                parent.line(axisPosition,bottom,axisPosition,top);
            }
            
            for (int i=firstTic; i<tics[1].length; i++)
            {
                float tic = tics[1][i];
                if (tic <= getMax(1))
                {
                    if (transposeAxes)
                    {
                        parent.textAlign(PConstants.CENTER, PConstants.TOP);
                        parent.text(axisFormatter[1].format(tic),left +hRange*(tic-getMin(1))/(getMax(1)-getMin(1)),axisPosition+textHeight/2 -2);
                    }
                    else
                    {
                        parent.textAlign(PConstants.RIGHT, PConstants.CENTER);
                        parent.text(axisFormatter[1].format(tic),axisPosition-2,top +vRange*(getMax(1)-tic)/(getMax(1)-getMin(1)));
                    }
                }
            }
            
            // Draw axis label if requested.
            if (yLabel != null)
            {
                if (transposeAxes)
                {
                    parent.textAlign(PConstants.CENTER,PConstants.TOP);
                    parent.text(yLabel,(left+right)/2f,axisPosition+getBorder(Side.BOTTOM)+2);
                }
                else
                {
                    parent.textAlign(PConstants.CENTER,PConstants.BOTTOM);
                    // Rotate label.
                    parent.pushMatrix();
                     parent.translate(axisPosition-(getBorder(Side.LEFT)+1),(top+bottom)/2f);
                     parent.rotate(-PConstants.HALF_PI);
                     parent.text(yLabel,0,0);
                    parent.popMatrix();
                }
            }
        }
        
        if (showSingleOriginValue)
        {
            parent.textAlign(PConstants.RIGHT, PConstants.TOP);
            parent.text(axisFormatter[1].format(tics[0][0]),left-2,bottom+textHeight/2);
        }
                
        parent.popStyle();
        parent.popMatrix();
    }
    
    /** Converts given data point into its screen location. This location will be based on 
     *  the last time the data were drawn with the <code>draw()</code> method. If this is
     *  called before any call to <code>draw()</code> has been made, it will return null.
     *  @param dataPoint (x,y) pair representing an item of data.
     *  @return Screen coordinates corresponding to the given data point or null if screen space undefined.
     */
    public PVector getDataToScreen(PVector dataPoint)
    {
        float hRange = right-left;
        float vRange = bottom-top;
        
        if ((vRange <= 0) || (hRange <=0))
        {
            return null;
        }
        
        float x,y;
        
        //Scale data points between 0-1.
        if (getIsLogScale(0))
        {
            x = convertToLog(dataPoint.x, getMinLog(0), getMaxLog(0));
        }
        else
        {
            x = (dataPoint.x-getMin(0))/(getMax(0)-getMin(0));
        }
        if (getIsLogScale(1))
        {
            y = convertToLog(dataPoint.y, getMinLog(1), getMaxLog(1));
        }
        else
        {
            y = (dataPoint.y-getMin(1))/(getMax(1)-getMin(1));
        }
        
        if (transposeAxes)
        {
            return new PVector(left + hRange*y, bottom - vRange*x);
        }
        return new PVector(left + hRange*x, bottom - vRange*y);
    }
    
    /** Converts given screen coordinate into its equivalent data value. This value will
     *  be based on the last time the data were drawn with the <code>draw()</code> method. 
     *  If this is called before any call to <code>draw()</code> has been made, it will return null.
     *  @param screenPoint Screen coordinates to convert into data pair.
     *  @return (x,y) pair representing an item of data that would be displayed at the given screen
     *          location or null if screen space not defined or screenPoint is outside of the 
     *          visible chart space.
     */
    public PVector getScreenToData(PVector screenPoint)
    {
        float hRange = right-left;
        float vRange = bottom-top;
        
        if ((vRange <= 0) || (hRange <=0))
        {
            return null;
        }
        
        if ((screenPoint.x < left) || (screenPoint.x > right) || (screenPoint.y < top) || (screenPoint.y > bottom))
        {
            return null;
        }
        
        // Scale the screen coordinates between 0-1.
        float x,y;
        if (transposeAxes)
        {
            y = (screenPoint.x - left)/hRange;
            x = (bottom - screenPoint.y)/vRange;
        }
        else
        {
            x = (screenPoint.x - left)/hRange;
            y = (bottom - screenPoint.y)/vRange;
        }
        
        if (getIsLogScale(0))
        {
            x = convertFromLog(x, getMinLog(0), getMaxLog(0));
        }
        else
        {
            x = x*(getMax(0)-getMin(0)) + getMin(0);
        }
        
        if (getIsLogScale(1))
        {
            y = convertFromLog(y, getMinLog(1), getMaxLog(1));
        }
        else
        {
            y = y*(getMax(1)-getMin(1)) + getMin(1);
        }
        
        return new PVector(x,y);
    }
        
    /** Determines the colour of the points to be displayed on the chart. This method
     *  will give a uniform colour to all points.
     *  @param colour Colour of points to be displayed.
     */
    public void setPointColour(int colour)
    {
        this.pointColour = colour;
        cTable = null;
        data[2] = null;
    }
    
    /** Provides the data and colour table from which to colour points. Each data item
     *  should by in the same order as the data provided to <code>setData()</code>.
     *  @param colourData Data used to colour points.
     *  @param cTable Colour table that translates data values into colours.
     */
    public void setPointColour(float[]colourData, ColourTable cTable)
    {
        if (colourData.length != data[0].length)
        {
            System.err.println("Warning: Number of items in point colour data ("+colourData.length+") does not match number of x,y pairs ("+data[0].length+").");
            return;
        }
        
        this.cTable = cTable;
        
        // Store colour data in dimension 2 of the chart.
        setData(2,colourData);
    }
    
    /** Determines the colour of the line to be displayed on the chart.
     *  @param colour Colour of line to be displayed.
     */
    public void setLineColour(int colour)
    {
        this.lineColour = colour;
    }
    
    /** Determines the size of the points to be displayed on the chart. This method
     *  will give a uniform size to all points. If set to 0 or less, no points are drawn.
     *  @param size Size of points to be displayed in pixel units.
     */
    public void setPointSize(float size)
    {
        this.pointSize = size;
        data[3] = null;
        maxPointSize = 0;
    }
    
    /** Sizes points to be displayed on the chart according to the given set of data. Each 
     *  data item should by in the same order as the data provided to <code>setData()</code>.
     *  @param sizeData Data used to size points.
     *  @param maxSize Size in pixel units of the largest point.
     */
    public void setPointSize(float[] sizeData, float maxSize)
    {
        if (sizeData.length != data[0].length)
        {
            System.err.println("Warning: Number of items in point size data ("+sizeData.length+") does not match number of x,y pairs ("+data[0].length+").");
            return;
        }
        
        this.maxPointSize = maxSize;
        
        // Store size data in dimension 3 of the chart.
        setData(3,sizeData);
    }
    
    /** Determines the width of the line to be displayed on the chart.
     *  If set to 0 or less, no line is drawn.
     *  @param width Width of line to be displayed in pixel units.
     */
    public void setLineWidth(float width)
    {
        this.lineWidth = width;
    }
        
    /** Determines whether or not the axis representing the x data is shown.
     *  @param showAxis x-axis is shown if true.
     */
    public void showXAxis(boolean showAxis)
    {
        showAxis(0,showAxis,transposeAxes?Side.LEFT:Side.BOTTOM);
    }
    
    /** Determines whether or not the axis representing the y data is shown.
     *  @param showAxis y-axis is shown if true.
     */
    public void showYAxis(boolean showAxis)
    {
        showAxis(1,showAxis,transposeAxes?Side.BOTTOM:Side.LEFT);
    }
    
    /** Sets the minimum value for x values to be represented. This can be used to force
     *  the x-axis to start at 0 or some other value. If the given value is <code>Float.NaN</code>,
     *  then the minimum x-value will be set to the minimum of the data x-values in the chart.
     *  @param minX Minimum x-value to be represented on the x-axis.
     */
    public void setMinX(float minX)
    {
       setMin(0,minX);
    }
    
    /** Sets the minimum value for y values to be represented. This can be used to force
     *  the y-axis to start at 0 or some other value. If the given value is <code>Float.NaN</code>,
     *  then the minimum y-value will be set to the minimum of the data y-values in the chart.
     *  @param minY Minimum y-value to be represented on the y-axis.
     */
    public void setMinY(float minY)
    {
       setMin(1,minY);
    }

    /** Sets the maximum value for x values to be represented. This can be used to force
     *  multiple charts to share the same axis range independently of data to be represented.
     *  If the given value is <code>Float.NaN</code>, then the maximum x-value will be set 
     *  to the maximum of the data x-values in the chart.
     *  @param maxX Maximum x-value to be represented on the x-axis or <code>Float.NaN</code>
     *              to use data maximum.
     */
    public void setMaxX(float maxX)
    {
       setMax(0,maxX);
    }
    
    /** Sets the maximum value for y values to be represented. This can be used to force
     *  multiple charts to share the same axis range independently of data to be represented.
     *  If the given value is <code>Float.NaN</code>, then the minimum y-value will be set 
     *  to the maximum of the data y-values in the chart.
     *  @param maxY Maximum y-value to be represented on the y-axis or <code>Float.NaN</code>
     *              go use the data maximum.
     */
    public void setMaxY(float maxY)
    {
        setMax(1,maxY);
    }
    
    /** Reports the data values that are displayed in the chart along the X axis.
     *  @return Sequence of data values represented by their position along the x-axis.
     */
    public float[] getXData()
    {
        return getData(0);
    }
    
    /** Reports the data values that are displayed in the chart along the Y axis.
     *  @return Sequence of data values represented by their position along the y-axis.
     */
    public float[] getYData()
    {
        return getData(1);
    }

    /** Reports the minimum x value that can be displayed by the XY chart. Note that this need not
     *  necessarily be the same as the minimum data value being displayed since axis rounding or
     *  calls to <code>setMinX()</code> can affect the value.
     *  @return Minimum value that can be represented by the XY chart on the x axis.
     */
    public float getMinX()
    {
        return getMin(0);
    }
    
    /** Reports the maximum x value that can be displayed by the XY chart. Note that this need not
     *  necessarily be the same as the maximum data value being displayed since axis rounding or
     *  calls to <code>setMaxX()</code> can affect the value.
     *  @return Maximum value that can be represented by the XY chart on the x axis.
     */
    public float getMaxX()
    {
        return getMax(0);
    }
    
    /** Reports the minimum y value that can be displayed by the XY chart. Note that this need not
     *  necessarily be the same as the minimum data value being displayed since axis rounding or
     *  calls to <code>setMinY()</code> can affect the value.
     *  @return Minimum value that can be represented by the XY chart on the y axis.
     */
    public float getMinY()
    {
        return getMin(1);
    }
    
    /** Reports the maximum y value that can be displayed by the XY chart. Note that this need not
     *  necessarily be the same as the maximum data value being displayed since axis rounding or
     *  calls to <code>setMaxY()</code> can affect the value.
     *  @return Maximum value that can be represented by the XY chart on the y axis.
     */
    public float getMaxY()
    {
        return getMax(1);
    }
    
    /** Sets the x-axis label. If null, no label is drawn.
     *  @param label x-axis label to draw or null if no label to be drawn.
     */
    public void setXAxisLabel(String label)
    {
        this.xLabel = label;
    }
    
    /** Sets the y-axis label. If null, no label is drawn.
     *  @param label y-axis label to draw or null if no label to be drawn.
     */
    public void setYAxisLabel(String label)
    {
        this.yLabel = label;
    }
    
    /** Sets the position of the x-axis. Note that this position will be somewhere on along the y-range.
     *  @param yValue Position of axis in data units.
     */
    public void setXAxisAt(float yValue)
    {
        this.xAxisPosition = new Float(yValue);

        // Update range if the axis lies outside of existing range.
        // Note that the x-axis is placed somewhere on the y-range.
        if (yValue < getMin(1))
        {
            setMinY(yValue);
        }
        else if (yValue >getMax(1))
        {
            setMaxY(yValue);
        }
    }
    
    /** Sets the position of the y-axis. Note that this position will be somewhere on along the x-range.
     *  @param xValue Position of axis in data units.
     */
    public void setYAxisAt(float xValue)
    {
        this.yAxisPosition = new Float(xValue);

        // Update range if the axis lies outside of existing range.
        // Note that the y-axis is placed somewhere on the x-range.
        if (xValue < getMin(0))
        {
            setMinX(xValue);
        }
        else if (xValue >getMax(0))
        {
            setMaxX(xValue);
        }
    }
    
    /** Sets the numerical format for numbers shown on the x-axis.
     *  @param format Format for numbers on the x-axis.
     */
    public void setXFormat(String format)
    {
        setFormat(0, format);
    }
    
    /** Sets the numerical format for numbers shown on the y-axis.
     *  @param format Format for numbers on the y-axis.
     */
    public void setYFormat(String format)
    {
        setFormat(1, format);
    }
    
    /** Determines whether or not the values on the x axis are log10 scaled.
     *  @param isLog True if x-axis values are to be log10-scaled or false if linear.
     */
    public void setLogX(boolean isLog)
    {
        setIsLogScale(0, isLog);
    }
    
    /** Determines whether or not the values on the y axis are log10 scaled.
     *  @param isLog True if y-axis values are to be log10-scaled or false if linear.
     */
    public void setLogY(boolean isLog)
    {
        setIsLogScale(1, isLog);
    }
    
    /** Determines if the axes should be transposed (so that the x-axis is vertical 
     *  and y-axis is horizontal).
     *  @param transpose Axes are transposed if true.
     */
    public void transposeAxes(boolean transpose)
    {
        this.transposeAxes = transpose;
        
        // This is a bit of a kludge to ensure that new axis borders are calculated
        // when the graph is transposed. By changing the axis visibility and then changing
        // them back again, it ensures the new borders are calculated correctly.
        boolean showXAxis = getShowAxis(0);
        boolean showYAxis = getShowAxis(1);
        showXAxis(!showXAxis);
        showYAxis(!showYAxis);
        
        showXAxis(showXAxis);
        showYAxis(showYAxis);
    }
}
