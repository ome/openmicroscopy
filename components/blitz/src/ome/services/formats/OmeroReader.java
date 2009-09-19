/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.formats;

import java.io.IOException;
import java.util.StringTokenizer;

import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataStore;
import ome.api.RawPixelsStore;
import omero.model.Image;
import omero.model.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private final static Log log = LogFactory.getLog(OmeroReader.class);

    private final RawPixelsStore raw;

    private final Pixels pix;
    
    public final int sizeX, sizeY, sizeZ, sizeT, sizeC, planes;

    public OmeroReader(RawPixelsStore raw, Pixels pix) {
        super("OMERO", "*");
        this.raw = raw;
        this.pix = pix;
        sizeX = pix.getSizeX().getValue();
        sizeY = pix.getSizeY().getValue();
        sizeZ = pix.getSizeZ().getValue();
        sizeC = pix.getSizeC().getValue();
        sizeT = pix.getSizeT().getValue();
        planes = sizeZ * sizeC * sizeT;
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

        byte[] plane = raw.getPlane(zct[0], zct[1], zct[2]);
        int len = getSizeX() * getSizeY()
                * FormatTools.getBytesPerPixel(getPixelType());
        System.arraycopy((byte[]) plane, 0, buf, 0, len);

        return buf;
    }

    public void close() throws IOException {
        super.close();
    }

    protected void initFile(String id) throws FormatException, IOException {
        debug("OmeroReader.initFile(" + id + ")");

        super.initFile(id);

        String ptype = pix.getPixelsType().getValue().getValue();

        core[0].sizeX = sizeX;
        core[0].sizeY = sizeY;
        core[0].sizeZ = sizeZ;
        core[0].sizeC = sizeC;
        core[0].sizeT = sizeT;
        core[0].rgb = false;
        core[0].littleEndian = false;
        core[0].dimensionOrder = "XYZCT";
        core[0].imageCount = getSizeZ() * getSizeC() * getSizeT();
        core[0].pixelType = FormatTools.pixelTypeFromString(ptype);

        double px = pix.getSizeX().getValue();
        double py = pix.getSizeY().getValue();
        double pz = pix.getSizeZ().getValue();

        Image image = pix.getImage();

        String name = image.getName().getValue();
        String description = image.getDescription().getValue();

        MetadataStore store = getMetadataStore();
        store.setImageName(name, 0);
        store.setImageDescription(description, 0);
        MetadataTools.populatePixels(store, this);

        store.setDimensionsPhysicalSizeX(new Float(px), 0, 0);
        store.setDimensionsPhysicalSizeY(new Float(py), 0, 0);
        store.setDimensionsPhysicalSizeZ(new Float(pz), 0, 0);
    }

}
