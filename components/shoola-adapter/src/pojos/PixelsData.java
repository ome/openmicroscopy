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
    
    public final static String IMAGE = Pixels.IMAGE;
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"INT8"</code> string identifier. 
     */
    public static final String INT8_TYPE = "int8";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"INT16"</code> string identifier. 
     */
    public static final String INT16_TYPE = "int16";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"INT32"</code> string identifier. 
     */
    public static final String  INT32_TYPE = "int32";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"UINT8"</code> string identifier. 
     */
    public static final String  UINT8_TYPE = "uint8";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"UINT16"</code> string identifier. 
     */
    public static final String  UINT16_TYPE = "uint16";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"UINT32"</code> string identifier. 
     */
    public static final String  UINT32_TYPE = "uint32";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"FLOAT"</code> string identifier. 
     */
    public static final String  FLOAT_TYPE = "float";
    
    /** 
     * Identifies the type used to store pixel values.
     * Maps onto the <i>OME</i> <code>"DOUBLE"</code> string identifier. 
     */
    public static final String  DOUBLE_TYPE = "double";
    public static final String  COMPLEX_TYPE = "complex";
    public static final String  DOUBLE_COMPLEX_TYPE = "double-complex";
    
    /** The Image these Pixels belong to. */
    private ImageData    image;

    public PixelsData()
    {
        setDirty( true );
        setValue( new Pixels() );
    }
    
    public PixelsData( Pixels value )
    {
        setValue( value );
    }
    
    public void setSizeX(int sizeX) {
        setDirty( true );
        asPixels().setSizeX( new Integer( sizeX ));
    }

    public int getSizeX() {
        return nullSafe( asPixels().getSizeX() );
    }

    public void setSizeY(int sizeY) {
        setDirty( true );
        asPixels().setSizeY( new Integer( sizeY ));
    }

    public int getSizeY() {
        return nullSafe( asPixels().getSizeY() );
    }

    public void setSizeZ(int sizeZ) {
        setDirty( true );
        asPixels().setSizeZ( new Integer( sizeZ ));
    }

    public int getSizeZ() {
        return nullSafe( asPixels().getSizeZ() );
    }

    public void setSizeC(int sizeC) {
        setDirty( true );
        asPixels().setSizeC( new Integer( sizeC ));
    }

    public int getSizeC() {
        return nullSafe( asPixels().getSizeC() );
    }

    public void setSizeT(int sizeT) {
        setDirty( true );
        asPixels().setSizeT( new Integer( sizeT ));

    }

    public int getSizeT() {
        return nullSafe( asPixels().getSizeT() );
    }

    public void setPixelSizeX(double pixelSizeX) {
        setDirty( true );
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        dims.setSizeX( new Float( pixelSizeX ));
    }

    public double getPixelSizeX() {
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        return nullSafe( dims.getSizeX() );
    }

    public void setPixelSizeY( double pixelSizeY ) {
        setDirty( true );
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        dims.setSizeY( new Float( pixelSizeY ));
    }

    public double getPixelSizeY() {
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        return dims == null ? 0.0 : nullSafe( dims.getSizeX() );
    }

    public void setPixelSizeZ( double pixelSizeZ ) 
    {
        setDirty( true );
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        if ( dims != null )
            dims.setSizeZ( new Float( pixelSizeZ ));
    }

    public double getPixelSizeZ() {
        PixelsDimensions dims = asPixels().getPixelsDimensions();
        return dims == null ? 0.0 : nullSafe( dims.getSizeX() );
    }

    // Entites
    public String getPixelType() {
        PixelsType type = asPixels().getPixelsType();
        return null == type ? null : type.getValue();
    }

    public ImageData getImage() {
        
        if ( image == null && asPixels().getImage() != null )
            image = new ImageData( asPixels().getImage() );
        
        return image;
    }
    
    public void setImage(ImageData image) {

        setDirty( true );
        this.image = image;
        if ( image == null)
            asPixels().setImage( null );
        else
            asPixels().setImage( image.asImage() );
        
    }


    
}
