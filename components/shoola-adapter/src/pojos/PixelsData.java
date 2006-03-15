/*
 * pojos.PixelsData
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

package pojos;

//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.enums.PixelsType;

/** 
 * The data that makes up an <i>OME</i> Pixels object along with a back pointer
 * to the Image that owns this Pixels.
 * A Pixels object represents a 5D raw data array that stores the Image pixels.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 1.1 $ $Date: 2005/05/09 19:50:41 $)
 * </small>
 * @since OME2.2
 */
public class PixelsData
    extends DataObject
{
    
    /** Identifies the {@link Pixels#IMAGE} field. */
    public final static String IMAGE = Pixels.IMAGE;
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>int8</code> string identifier. 
     */
    public static final String INT8_TYPE = "int8";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>int16</code> string identifier. 
     */
    public static final String INT16_TYPE = "int16";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"int32</code> string identifier. 
     */
    public static final String  INT32_TYPE = "int32";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>uint8</code> string identifier. 
     */
    public static final String  UINT8_TYPE = "uint8";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>uint16</code> string identifier. 
     */
    public static final String  UINT16_TYPE = "uint16";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>uint32</code> string identifier. 
     */
    public static final String  UINT32_TYPE = "uint32";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>float</code> string identifier. 
     */
    public static final String  FLOAT_TYPE = "float";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>double</code> string identifier. 
     */
    public static final String  DOUBLE_TYPE = "double";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>complex</code> string identifier. 
     */
    public static final String  COMPLEX_TYPE = "complex";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>double-complex</code> string identifier. 
     */
    public static final String  DOUBLE_COMPLEX_TYPE = "double-complex";
    
    /** The Image these Pixels belong to. */
    private ImageData    image;

    /** Creates a new instance. */
    public PixelsData()
    {
        setDirty(true);
        setValue(new Pixels());
    }
    
    /**
     * Creates a new instance.
     * 
     * @param pixels    Back pointer to the {@link Pixels} model object.
     *                  Mustn't be <code>null</code>.
     * @throws IllegalArgumentException If the object is <code>null</code>.
     */
    public PixelsData(Pixels pixels)
    {
        if (pixels == null)
            throw new IllegalArgumentException("The object cannot be null.");
        setValue(pixels);
    }
    
    /** 
     * Sets the number of pixels along the X-axis.
     * 
     * @param sizeX The number of pixels along the X-axis.
     */
    public void setSizeX(int sizeX)
    {
        setDirty(true);
        asPixels().setSizeX(new Integer(sizeX));
    }

    /**
     * Returns the number of pixels along the X-axis.
     * 
     * @return See above.
     */
    public int getSizeX() { return nullSafe(asPixels().getSizeX()); }

    /** 
     * Sets the number of pixels along the Y-axis.
     * 
     * @param sizeY The number of pixels along the Y-axis.
     */
    public void setSizeY(int sizeY)
    {
        setDirty(true);
        asPixels().setSizeY(new Integer(sizeY));
    }

    /**
     * Returns the number of pixels along the Y-axis.
     * 
     * @return See above.
     */
    public int getSizeY() { return nullSafe(asPixels().getSizeY()); }

    /** 
     * Sets the number of pixels along the Z-axis.
     * 
     * @param sizeZ The number of pixels along the Z-axis.
     */
    public void setSizeZ(int sizeZ)
    {
        setDirty(true);
        asPixels().setSizeZ(new Integer(sizeZ));
    }

    /**
     * Returns the number of pixels along the Z-axis.
     * 
     * @return See above.
     */
    public int getSizeZ() { return nullSafe(asPixels().getSizeZ()); }

    /** 
     * Sets the number of channels.
     * 
     * @param sizeC The number of channels.
     */
    public void setSizeC(int sizeC)
    {
        setDirty(true);
        asPixels().setSizeC(new Integer(sizeC));
    }

    /**
     * Returns the number of channels.
     * 
     * @return See above.
     */
    public int getSizeC() { return nullSafe(asPixels().getSizeC()); }

    /** 
     * Sets the number of time-points.
     * 
     * @param sizeT The number of time-points.
     */
    public void setSizeT(int sizeT)
    {
        setDirty(true);
        asPixels().setSizeT(new Integer(sizeT));
    }

    /**
     * Returns the number of time-points.
     * 
     * @return See above.
     */
    public int getSizeT() { return nullSafe(asPixels().getSizeT()); }

    /**
     * Sets the dimension of a pixel along the X-axis, dimension is in 
     * microns.
     * 
     * @param pixelSizeX The dimension of a pixel along the X-axis.
     */
    public void setPixelSizeX(double pixelSizeX)
    {
        setDirty(true);
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        if (dims != null) dims.setSizeX(new Float(pixelSizeX));
    }

    /**
     * Returns the dimension of a pixel along the X-axis, dimension is in 
     * microns. 
     * 
     * @return See above.
     */
    public double getPixelSizeX()
    {
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        return dims == null ? 1.0 : nullSafe(dims.getSizeX());
    }

    /**
     * Sets the dimension of a pixel along the Y-axis, dimension is in 
     * microns.
     * 
     * @param pixelSizeY The dimension of a pixel along the Y-axis.
     */
    public void setPixelSizeY(double pixelSizeY)
    {
        setDirty(true);
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        if (dims != null)  dims.setSizeY(new Float(pixelSizeY));
    }

    /**
     * Returns the dimension of a pixel along the Y-axis, dimension is in 
     * microns. 
     * 
     * @return See above.
     */
    public double getPixelSizeY()
    {
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        return dims == null ? 1.0 : nullSafe( dims.getSizeX() );
    }

    /**
     * Sets the dimension of a pixel along the Z-axis, dimension is in 
     * microns.
     * 
     * @param pixelSizeZ The dimension of a pixel along the Z-axis.
     */
    public void setPixelSizeZ(double pixelSizeZ) 
    {
        setDirty(true);
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        if (dims != null) dims.setSizeZ(new Float( pixelSizeZ));
    }

    /**
     * Returns the dimension of a pixel along the Z-axis, dimension is in 
     * microns. 
     * 
     * @return See above.
     */
    public double getPixelSizeZ()
    {
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        return dims == null ? 1.0 : nullSafe(dims.getSizeX());
    }

    /**
     * Returns the pixels type.
     * 
     * @return See above.
     */
    public String getPixelType() {
        PixelsType type = asPixels().getPixelsType();
        return type == null ? null : type.getValue();
    }

    /**
     * Returns the image linked to this pixels' set.
     * 
     * @return See above.
     */
    public ImageData getImage()
    {
        if (image == null && asPixels().getImage() != null)
            image = new ImageData(asPixels().getImage());
        return image;
    }
    
    /**
     * Sets the image linked to this pixels' set.
     * 
     * @param image The linked image.
     */
    public void setImage(ImageData image)
    {
        setDirty(true);
        this.image = image;
        if (image == null) asPixels().setImage(null);
        else asPixels().setImage(image.asImage());
    }

}
