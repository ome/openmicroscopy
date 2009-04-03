/*
 * org.openmicroscopy.shoola.util.roi.io.util.SVGTransform 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.roi.io.util;


//Java imports
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class SVGTransform 
{
	public static AffineTransform toTransform(String str) throws IOException 
	{
        AffineTransform t = new AffineTransform();
        
        if (str != null && ! str.equals("none")) 
        {
            
            StreamTokenizer tt = new StreamTokenizer(new StringReader(str));
            tt.resetSyntax();
            tt.wordChars('a', 'z');
            tt.wordChars('A', 'Z');
            tt.wordChars(128 + 32, 255);
            tt.whitespaceChars(0, ' ');
            tt.whitespaceChars(',', ',');
            tt.parseNumbers();
            
            while (tt.nextToken() != StreamTokenizer.TT_EOF) 
            {
                if (tt.ttype != StreamTokenizer.TT_WORD) 
                {
                    throw new IOException("Illegal transform "+str);
                }
                String type = tt.sval;
                if (tt.nextToken() != '(') 
                {
                    throw new IOException("'(' not found in transform "+str);
                }
                if (type.equals("matrix")) 
                {
                    double[] m = new double[6];
                    for (int i=0; i < 6; i++) 
                    {
                        if (tt.nextToken() != StreamTokenizer.TT_NUMBER) 
                        {
                            throw new IOException("Matrix value "+i+" not found in transform "+str+" token:"+tt.ttype+" "+tt.sval);
                        }
                        m[i] = tt.nval;
                        if (tt.nextToken() == StreamTokenizer.TT_WORD &&
                                (tt.sval.startsWith("E") || tt.sval.startsWith("e"))) 
                        {
                            double mantissa = tt.nval;
                            m[i] = Double.valueOf(m[i] + tt.sval);
                        } 
                        else 
                        {
                            tt.pushBack();
                        }
                    }
                    t.concatenate(new AffineTransform(m));
                    
                } 
                else if (type.equals("translate")) 
                {
                    double tx, ty;
                    if (tt.nextToken() != StreamTokenizer.TT_NUMBER) 
                    {
                        throw new IOException("X-translation value not found in transform "+str);
                    }
                    tx = tt.nval;
                    if (tt.nextToken() == StreamTokenizer.TT_WORD &&
                            (tt.sval.startsWith("E") || tt.sval.startsWith("e"))) 
                    {
                        double mantissa = tt.nval;
                        tx = Double.valueOf(tx + tt.sval);
                    } 
                    else 
                    {
                        tt.pushBack();
                    }
                    if (tt.nextToken() == StreamTokenizer.TT_NUMBER) 
                    {
                        ty = tt.nval;
                        if (tt.nextToken() == StreamTokenizer.TT_WORD &&
                                (tt.sval.startsWith("E") || tt.sval.startsWith("e"))) 
                        {
                            double mantissa = tt.nval;
                            ty = Double.valueOf(ty + tt.sval);
                        } 
                        else 
                        {
                            tt.pushBack();
                        }
                    } 
                    else 
                    {
                        tt.pushBack();
                        ty = 0;
                    }
                    t.translate(tx, ty);
                    
                } 
                else if (type.equals("scale")) 
                {
                    double sx, sy;
                    if (tt.nextToken() != StreamTokenizer.TT_NUMBER) 
                    {
                        throw new IOException("X-scale value not found in transform "+str);
                    }
                    sx = tt.nval;
                    if (tt.nextToken() == StreamTokenizer.TT_WORD &&
                            (tt.sval.startsWith("E") || tt.sval.startsWith("e"))) 
                    {
                        double mantissa = tt.nval;
                        sx = Double.valueOf(sx + tt.sval);
                    } 
                    else 
                    {
                        tt.pushBack();
                    }
                    if (tt.nextToken() == StreamTokenizer.TT_NUMBER) 
                    {
                        sy = tt.nval;
                        if (tt.nextToken() == StreamTokenizer.TT_WORD &&
                                (tt.sval.startsWith("E") || tt.sval.startsWith("e"))) 
                        {
                            double mantissa = tt.nval;
                            sy = Double.valueOf(sy + tt.sval);
                        } 
                        else 
                        {
                            tt.pushBack();
                        }
                    } 
                    else 
                    {
                        tt.pushBack();
                        sy = sx;
                    }
                    t.scale(sx, sy);
                    
                } 
                else if (type.equals("rotate")) 
                {
                    double angle, cx, cy;
                    if (tt.nextToken() != StreamTokenizer.TT_NUMBER) 
                    {
                        throw new IOException("Angle value not found in transform "+str);
                    }
                    angle = tt.nval;
                    if (tt.nextToken() == StreamTokenizer.TT_WORD &&
                            (tt.sval.startsWith("E") || tt.sval.startsWith("e"))) 
                    {
                        double mantissa = tt.nval;
                        angle = Double.valueOf(angle + tt.sval);
                    } 
                    else 
                    {
                        tt.pushBack();
                    }
                    if (tt.nextToken() == StreamTokenizer.TT_NUMBER) 
                    {
                        cx = tt.nval;
                        if (tt.nextToken() == StreamTokenizer.TT_WORD &&
                                (tt.sval.startsWith("E") || tt.sval.startsWith("e"))) 
                        {
                            double mantissa = tt.nval;
                            cx = Double.valueOf(cx + tt.sval);
                        } 
                        else 
                        {
                            tt.pushBack();
                        }
                        if (tt.nextToken() != StreamTokenizer.TT_NUMBER) 
                        {
                            throw new IOException("Y-center value not found in transform "+str);
                        }
                        cy = tt.nval;
                        if (tt.nextToken() == StreamTokenizer.TT_WORD &&
                                (tt.sval.startsWith("E") || tt.sval.startsWith("e"))) 
                        {
                            double mantissa = tt.nval;
                            cy = Double.valueOf(cy + tt.sval);
                        } 
                        else 
                        {
                            tt.pushBack();
                        }
                    } 
                    else 
                    {
                        tt.pushBack();
                        cx = cy = 0;
                    }
                    t.rotate(angle * Math.PI / 180d, cx * Math.PI / 180d, cy * Math.PI / 180d);
                    
                    
                }
                else if (type.equals("skewX")) 
                {
                    double angle;
                    if (tt.nextToken() != StreamTokenizer.TT_NUMBER) 
                    {
                        throw new IOException("Skew angle not found in transform "+str);
                    }
                    angle = tt.nval;
                    if (tt.nextToken() == StreamTokenizer.TT_WORD &&
                            (tt.sval.startsWith("E") || tt.sval.startsWith("e"))) 
                    {
                        double mantissa = tt.nval;
                        angle = Double.valueOf(angle + tt.sval);
                    } 
                    else 
                    {
                        tt.pushBack();
                    }
                    t.concatenate(new AffineTransform(
                            1, 0, Math.tan(angle * Math.PI / 180), 1, 0, 0
                            ));
                    
                } 
                else if (type.equals("skewY")) 
                {
                    double angle;
                    if (tt.nextToken() != StreamTokenizer.TT_NUMBER) 
                    {
                        throw new IOException("Skew angle not found in transform "+str);
                    }
                    angle = tt.nval;
                    if (tt.nextToken() == StreamTokenizer.TT_WORD &&
                            (tt.sval.startsWith("E") || tt.sval.startsWith("e"))) 
                    {
                        double mantissa = tt.nval;
                        angle = Double.valueOf(angle + tt.sval);
                    } 
                    else 
                    {
                        tt.pushBack();
                    }
                    t.concatenate(new AffineTransform(
                            1, Math.tan(angle * Math.PI / 180), 0, 1, 0, 0
                            ));
                    
                }
                else 
                {
                    throw new IOException("Unknown transform "+type+" in "+str);
                }
                if (tt.nextToken() != ')') 
                {
                    throw new IOException("')' not found in transform "+str);
                }
            }
        }
        return t;
    }
    
}


