package ome.formats.importer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import loci.formats.ChannelFiller;
import loci.formats.ChannelSeparator;
import loci.formats.ClassList;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MinMaxCalculator;
import loci.formats.in.FlexReader;
import loci.formats.in.InCellReader;
import loci.formats.in.LeicaReader;
import loci.formats.in.MIASReader;
import loci.formats.meta.MetadataStore;
import omero.model.Channel;
import omero.model.Pixels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OMEROWrapper extends MinMaxCalculator {

    private ChannelSeparator separator;
    private ChannelFiller filler;
    public Boolean minMaxSet = null;

    /**
     * Reference copy of <i>reader</i> so that we can be compatible with the
     * IFormatReader/ReaderWrapper interface but still maintain functionality
     * that we require.
     */
    private ImageReader iReader;

    public OMEROWrapper(ImportConfig config)
    {
        if (config == null) {
            throw new IllegalArgumentException("An ImportConfig must be instantitated \n " +
            		"in order to properly configure all readers.");
        }
        try
        {
            String readers = config.readersPath.get();
            Class<?> k = getClass();
            if (new File(readers).exists()) {
                k = null;
            }
            iReader = new ImageReader(new ClassList(readers, IFormatReader.class, k));
            
//            // Now we apply the invocation handler
//            iReader =  (ImageReader) Proxy.newProxyInstance(
//                    getClass().getClassLoader(),
//                    new Class[]{IFormatReader.class},
//                    new ReaderInvocationHandler(iReader));
            
            filler = new ChannelFiller(iReader);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to load readers.txt.");
        }
        reader = separator  = new ChannelSeparator(filler);
        //reader = separator = new ChannelSeparator(iReader);
        
        // Force unreadable characters to be removed from metadata key/value pairs 
        iReader.setMetadataFiltered(true);
        filler.setMetadataFiltered(true);
        separator.setMetadataFiltered(true);

    };

    /**
     * Obtains an object which represents a given plane within the file.
     * 
     * @param id
     *            The path to the file.
     * @param planeNumber
     *            The plane or section within the file to obtain.
     * @param buf
     *            Pre-allocated buffer which has a <i>length</i> that can fit
     *            the byte count of an entire plane.
     * @return an object which represents the plane.
     * @throws FormatException
     *             if there is an error parsing the file.
     * @throws IOException
     *             if there is an error reading from the file or acquiring
     *             permissions to read the file.
     */
    public Plane2D openPlane2D(String id, int planeNumber, byte[] buf)
            throws FormatException, IOException {
        // FIXME: HACK! The ChannelSeparator isn't exactly what one would call
        // "complete" so we have to work around the fact that it still copies
        // all of the plane data (all three channels) from the file if the file
        // is RGB.
        ByteBuffer plane;
        if (iReader.isRGB() || isLeicaReader()) {
            // System.err.println("RGB, not using cached buffer.");
            byte[] bytePlane = openBytes(planeNumber);
            plane = ByteBuffer.wrap(bytePlane);
        } else {
            // System.err.println("Not RGB, using cached buffer.");
            plane = ByteBuffer.wrap(openBytes(planeNumber, buf));
        }
        return new Plane2D(plane, getPixelType(), isLittleEndian(), getSizeX(),
                getSizeY());
    }

    public boolean isLeicaReader() {
        if (iReader.getReader() instanceof LeicaReader) {
            return true;
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean isMinMaxSet() throws FormatException, IOException {
        if (minMaxSet == null) {
            MetadataStore store = reader.getMetadataStore();
            int series = reader.getSeries();
            List<Pixels> pixels = (List<Pixels>) store.getRoot();
            Pixels p = pixels.get(series);
            Channel c = p.getChannel(p.getSizeC().getValue() - 1);
            if (c.getStatsInfo() == null) {
                minMaxSet = false;
            } else {
                minMaxSet = true;
            }
        }
        return minMaxSet;
    }

    @Override
    protected void updateMinMax(byte[] b, int ndx) throws FormatException,
            IOException {
        if (isMinMaxSet() == false)
            super.updateMinMax(b, ndx);
    }

    public void close() throws IOException {
        minMaxSet = null;
        super.close(false);
    }

    /**
     * Return the base image reader
     * 
     * @return See above.
     */
    public ImageReader getImageReader() {
        return iReader;
    }
    
    public boolean isSPWReader()
    {
        String[] domains = reader.getDomains();
        return Arrays.asList(domains).contains(FormatTools.HCS_DOMAIN);
    }
    
}

class ReaderInvocationHandler implements InvocationHandler {

    private final IFormatReader reader;

    public ReaderInvocationHandler(IFormatReader reader) {
        reader.toString(); // NPE
        this.reader = reader;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        if ("equals".equals(method.getName())) {
            throw new UnsupportedOperationException();
        } else if ("hashCode".equals(method.getName())) {
            return new Integer(reader.hashCode());
        } else if ("toString".equals(method.getName())) {
            return "ReaderHandler [" + reader + "]";
        } else {
            try {
                return method.invoke(proxy, args);
            } catch (Exception e) {
                throw new FormatException(e);
            }
        }
    }
}
