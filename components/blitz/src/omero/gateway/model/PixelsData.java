/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2017 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------*/

package omero.gateway.model;

import ome.model.units.BigResult;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.PixelsType;
import omero.model.enums.UnitsLength;

/**
 * The data that makes up an <i>OME</i> Pixels object along with a back pointer
 * to the Image that owns this Pixels. A Pixels object represents a 5D raw data
 * array that stores the Image pixels.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class PixelsData extends DataObject {

    /** Identifies the {@link PixelsI#IMAGE} field. */
    public final static String IMAGE = PixelsI.IMAGE;

    /**
     * Identifies the type used to store pixel values. Maps onto the <i>OME</i>
     * <code>int8</code> string identifier.
     */
    public static final String INT8_TYPE = omero.model.enums.PixelsTypeint8.value;

    /**
     * Identifies the type used to store pixel values. Maps onto the <i>OME</i>
     * <code>int16</code> string identifier.
     */
    public static final String INT16_TYPE = omero.model.enums.PixelsTypeint16.value;

    /**
     * Identifies the type used to store pixel values. Maps onto the <i>OME</i>
     * <code>"int32</code> string identifier.
     */
    public static final String INT32_TYPE = omero.model.enums.PixelsTypeint32.value;

    /**
     * Identifies the type used to store pixel values. Maps onto the <i>OME</i>
     * <code>uint8</code> string identifier.
     */
    public static final String UINT8_TYPE = omero.model.enums.PixelsTypeuint8.value;

    /**
     * Identifies the type used to store pixel values. Maps onto the <i>OME</i>
     * <code>uint16</code> string identifier.
     */
    public static final String UINT16_TYPE = omero.model.enums.PixelsTypeuint16.value;

    /**
     * Identifies the type used to store pixel values. Maps onto the <i>OME</i>
     * <code>uint32</code> string identifier.
     */
    public static final String UINT32_TYPE = omero.model.enums.PixelsTypeuint32.value;

    /**
     * Identifies the type used to store pixel values. Maps onto the <i>OME</i>
     * <code>float</code> string identifier.
     */
    public static final String FLOAT_TYPE = omero.model.enums.PixelsTypefloat.value;

    /**
     * Identifies the type used to store pixel values. Maps onto the <i>OME</i>
     * <code>double</code> string identifier.
     */
    public static final String DOUBLE_TYPE = omero.model.enums.PixelsTypedouble.value;

    /**
     * Identifies the type used to store pixel values. Maps onto the <i>OME</i>
     * <code>complex</code> string identifier.
     */
    public static final String COMPLEX_TYPE = omero.model.enums.PixelsTypecomplex.value;

    /**
     * Identifies the type used to store pixel values. Maps onto the <i>OME</i>
     * <code>double-complex</code> string identifier.
     */
    public static final String DOUBLE_COMPLEX_TYPE = omero.model.enums.PixelsTypedoublecomplex.value;

    /** The Image these Pixels belong to. */
    private ImageData image;

    /** Creates a new instance. */
    public PixelsData() {
        setDirty(true);
        setValue(new PixelsI());
    }

    /**
     * Creates a new instance.
     *
     * @param pixels
     *            Back pointer to the {@link Pixels} model object. Mustn't be
     *            <code>null</code>.
     * @throws IllegalArgumentException
     *             If the object is <code>null</code>.
     */
    public PixelsData(Pixels pixels) {
        if (pixels == null) {
            throw new IllegalArgumentException("The object cannot be null.");
        }
        setValue(pixels);
    }

    /**
     * Sets the number of pixels along the X-axis.
     *
     * @param sizeX
     *            The number of pixels along the X-axis.
     */
    public void setSizeX(int sizeX) {
        setDirty(true);
        asPixels().setSizeX(omero.rtypes.rint(sizeX));
    }

    /**
     * Returns the number of pixels along the X-axis.
     *
     * @return See above.
     */
    public int getSizeX() {
        return nullSafe(asPixels().getSizeX());
    }

    /**
     * Sets the number of pixels along the Y-axis.
     *
     * @param sizeY
     *            The number of pixels along the Y-axis.
     */
    public void setSizeY(int sizeY) {
        setDirty(true);
        asPixels().setSizeY(omero.rtypes.rint(sizeY));
    }

    /**
     * Returns the number of pixels along the Y-axis.
     *
     * @return See above.
     */
    public int getSizeY() {
        return nullSafe(asPixels().getSizeY());
    }

    /**
     * Sets the number of pixels along the Z-axis.
     *
     * @param sizeZ
     *            The number of pixels along the Z-axis.
     */
    public void setSizeZ(int sizeZ) {
        setDirty(true);
        asPixels().setSizeZ(omero.rtypes.rint(sizeZ));
    }

    /**
     * Returns the number of pixels along the Z-axis.
     *
     * @return See above.
     */
    public int getSizeZ() {
        return nullSafe(asPixels().getSizeZ());
    }

    /**
     * Sets the number of channels.
     *
     * @param sizeC
     *            The number of channels.
     */
    public void setSizeC(int sizeC) {
        setDirty(true);
        asPixels().setSizeC(omero.rtypes.rint(sizeC));
    }

    /**
     * Returns the number of channels.
     *
     * @return See above.
     */
    public int getSizeC() {
        return nullSafe(asPixels().getSizeC());
    }

    /**
     * Sets the number of time-points.
     *
     * @param sizeT
     *            The number of time-points.
     */
    public void setSizeT(int sizeT) {
        setDirty(true);
        asPixels().setSizeT(omero.rtypes.rint(sizeT));
    }

    /**
     * Returns the number of time-points.
     *
     * @return See above.
     */
    public int getSizeT() {
        return nullSafe(asPixels().getSizeT());
    }

    /**
     * Sets the dimension of a pixel along the X-axis
     *
     * @param pixelSizeX
     *            The dimension of a pixel along the X-axis.
     */
    public void setPixelSizeX(Length pixelSizeX) {
        setDirty(true);
        asPixels().setPhysicalSizeX(pixelSizeX);
    }

    /**
     * Returns the dimension of a pixel along the X-axis.
     *
     * @param unit
     *            The unit (may be null, in which case no conversion will be
     *            performed)
     * @return See above.
     * @throws BigResult If an arithmetic under-/overflow occurred 
     */
    public Length getPixelSizeX(UnitsLength unit) throws BigResult {
        Length l = asPixels().getPhysicalSizeX();
        if (l == null || l.getUnit().equals(UnitsLength.PIXEL))
            return null;
        return unit == null ? l : new LengthI(l, unit);
    }

    /**
     * Sets the dimension of a pixel along the Y-axis.
     *
     * @param pixelSizeY
     *            The dimension of a pixel along the Y-axis.
     */
    public void setPixelSizeY(Length pixelSizeY) {
        setDirty(true);
        asPixels().setPhysicalSizeY(pixelSizeY);
    }

    /**
     * Returns the dimension of a pixel along the Y-axis.
     *
     * @param unit
     *            The unit (may be null, in which case no conversion will be
     *            performed)
     * @return See above.
     * @throws BigResult If an arithmetic under-/overflow occurred 
     */
    public Length getPixelSizeY(UnitsLength unit) throws BigResult {
        Length l = asPixels().getPhysicalSizeY();
        if (l == null || l.getUnit().equals(UnitsLength.PIXEL))
            return null;
        return unit == null ? l : new LengthI(l, unit);
    }

    /**
     * Sets the dimension of a pixel along the Z-axis
     *
     * @param pixelSizeZ
     *            The dimension of a pixel along the Z-axis.
     */
    public void setPixelSizeZ(Length pixelSizeZ) {
        setDirty(true);
        asPixels().setPhysicalSizeZ(pixelSizeZ);
    }

    /**
     * Returns the dimension of a pixel along the Z-axis
     *
     * @param unit
     *            The unit (may be null, in which case no conversion will be
     *            performed)
     * @return See above.
     * @throws BigResult If an arithmetic under-/overflow occurred 
     */
    public Length getPixelSizeZ(UnitsLength unit) throws BigResult {
        Length l = asPixels().getPhysicalSizeZ();
        if (l == null || l.getUnit().equals(UnitsLength.PIXEL))
            return null;
        return unit == null ? l : new LengthI(l, unit);
    }

    /**
     * Returns the pixels type.
     *
     * @return See above.
     */
    public String getPixelType() {
        PixelsType type = asPixels().getPixelsType();
        return type == null ? null : (type.getValue() == null ? null : type
                .getValue().getValue());
    }

    /**
     * Returns the image linked to this pixels' set.
     *
     * @return See above.
     */
    public ImageData getImage() {
        if (image == null && asPixels().getImage() != null) {
            image = new ImageData(asPixels().getImage());
        }
        return image;
    }

    /**
     * Sets the image linked to this pixels' set.
     *
     * @param image
     *            The linked image.
     */
    public void setImage(ImageData image) {
        setDirty(true);
        this.image = image;
        if (image == null) {
            asPixels().setImage(null);
        } else {
            asPixels().setImage(image.asImage());
        }
    }

}
