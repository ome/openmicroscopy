/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.formats;

import static ome.xml.model.Pixels.getPhysicalSizeXUnitXsdDefault;
import static ome.xml.model.Pixels.getPhysicalSizeYUnitXsdDefault;
import static ome.xml.model.Pixels.getPhysicalSizeZUnitXsdDefault;
import static ome.formats.model.UnitsFactory.makeLengthXML;

import java.io.IOException;
import java.util.StringTokenizer;

import loci.common.RandomAccessInputStream;
import loci.formats.CoreMetadata;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataStore;
import ome.api.RawPixelsStore;
import omero.model.Image;
import omero.model.Pixels;
import omero.api.RawPixelsStorePrx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link IFormatReader} for use in export. This is copied
 * from the OMERO 2.3 Reader available from: <a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/ome-io/src/loci/ome/io"
 * >https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/ome-
 * io/src/loci/ome/io</a>
 *
 * @since Beta4.1
 * @see <a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/utils/MinimumWriter.java">MinimumWriter</a>
 * @see <a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/tools/ImageConverter.java">ImageConverter</a>
 * @see <a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/utils/ReadWriteInMemory.java">ReadWriteInMemory</a>
 */
public class OmeroReader extends FormatReader {

    private final static Logger log = LoggerFactory.getLogger(OmeroReader.class);

    private final RawPixelsStore raw;

    private final RawPixelsStorePrx prx;

    private final Pixels pix;

    public final int sizeX, sizeY, sizeZ, sizeT, sizeC, planes;

    private OmeroReader(Pixels pix, RawPixelsStore raw, RawPixelsStorePrx prx) {
        super("OMERO", "*");
        this.pix = pix;
        this.prx = prx;
        this.raw = raw;
        sizeX = pix.getSizeX().getValue();
        sizeY = pix.getSizeY().getValue();
        sizeZ = pix.getSizeZ().getValue();
        sizeC = pix.getSizeC().getValue();
        sizeT = pix.getSizeT().getValue();
        planes = sizeZ * sizeC * sizeT;
        if ( (this.raw == null && this.prx == null) ||
             (this.raw != null && this.prx != null)) {
            throw new RuntimeException("Improperly configured");
        }
    }

    public OmeroReader(RawPixelsStore raw, Pixels pix) {
        this(pix, raw, null);
    }

    public OmeroReader(RawPixelsStorePrx prx, Pixels pix) {
        this(pix, null, prx);
    }

    public boolean isThisType(String name, boolean open) {
        StringTokenizer st = new StringTokenizer(name, "\n");
        return st.countTokens() == 5; // TODO what is this?
    }

    public boolean isThisType(RandomAccessInputStream stream)
            throws IOException {
        return true; // TODO reading from an input stream?
    }

    public byte[] openBytes(int no, byte[] buf, int x1, int y1, int w1, int h1)
            throws FormatException, IOException {

        FormatTools.assertId(currentId, true, 1);
        FormatTools.checkPlaneNumber(this, no);
        FormatTools.checkBufferSize(this, buf.length);

        int[] zct = FormatTools.getZCTCoords(this, no);

        byte[] plane = null;
        if (raw != null) {
            plane = raw.getPlane(zct[0], zct[1], zct[2]);
        } else if (prx != null) {
            try {
                plane = prx.getPlane(zct[0], zct[1], zct[2]);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Improperly configured");
        }

        int len = getSizeX() * getSizeY()
                * FormatTools.getBytesPerPixel(getPixelType());
        System.arraycopy((byte[]) plane, 0, buf, 0, len);

        return buf;
    }

    public void close() throws IOException {
        super.close();
    }

    protected void initFile(String id) throws FormatException, IOException {
        log.debug("OmeroReader.initFile(" + id + ")");

        super.initFile(id);

        String ptype = pix.getPixelsType().getValue().getValue();
        String dorder = pix.getDimensionOrder().getValue().getValue();
        CoreMetadata ms0 = core.get(0);

        ms0.sizeX = sizeX;
        ms0.sizeY = sizeY;
        ms0.sizeZ = sizeZ;
        ms0.sizeC = sizeC;
        ms0.sizeT = sizeT;
        ms0.rgb = false;
        ms0.littleEndian = false;
        ms0.dimensionOrder = dorder;
        ms0.imageCount = planes;
        ms0.pixelType = FormatTools.pixelTypeFromString(ptype);

        double px = pix.getSizeX().getValue();
        double py = pix.getSizeY().getValue();
        double pz = pix.getSizeZ().getValue();

        Image image = pix.getImage();

        String name = image.getName().getValue();
        String description = null;
        if (image.getDescription() != null) {
            description = image.getDescription().getValue();
        }

        MetadataStore store = getMetadataStore();
        store.setImageName(name, 0);
        store.setImageDescription(description, 0);
        MetadataTools.populatePixels(store, this);

        store.setPixelsPhysicalSizeX(makeLengthXML(px, getPhysicalSizeXUnitXsdDefault()), 0);
        store.setPixelsPhysicalSizeY(makeLengthXML(py, getPhysicalSizeYUnitXsdDefault()), 0);
        store.setPixelsPhysicalSizeZ(makeLengthXML(pz, getPhysicalSizeZUnitXsdDefault()), 0);
    }

}
