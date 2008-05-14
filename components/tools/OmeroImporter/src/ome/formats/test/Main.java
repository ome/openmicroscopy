package ome.formats.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import ome.formats.OMEROMetadataStore;
import ome.model.core.Pixels;

import loci.formats.ChannelFiller;
import loci.formats.ChannelSeparator;
import loci.formats.ClassList;
import loci.formats.DataTools;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.MinMaxCalculator;

public class Main
{

    //private String filename = "/workspace/Test Images/CT-MONO2-16-ort.dcm";
    //private String filename = "/workspace/Test Images/image6.dicom";
    private String filename = "/workspace/Test Images/Zeiss (.lsm)/FRAP12.lsm";
    
    private ImageReader iReader;
    private ChannelFiller filler;
    private ChannelSeparator separator;
    private MinMaxCalculator    reader;
    
    //private OMEROMetadataStore store;
    private Pixels pixels;
    
    private int                sizeC;
    private int                sizeT;
    private int                sizeX; 
    private int                sizeY;
    private int                sizeZ;
    
    private int                zSize;
    private int                tSize;
    private int                wSize;
    
    private Main() throws Exception
    {
        try {
            iReader = (ImageReader) new ImageReader(new ClassList("readers.txt", IFormatReader.class));
            filler = new ChannelFiller(iReader);
            separator = new ChannelSeparator(filler);
            reader = new MinMaxCalculator(separator);   

            //store = new OMEROMetadataStore("root", "omero", "localhost", "1099");

            reader.close();
            //reader.setMetadataStore(store);
            reader.setId(filename);
            
            sizeX = reader.getSizeX();
            sizeY = reader.getSizeY();
            sizeZ = reader.getSizeZ();
            sizeT = reader.getSizeT();
            sizeC = reader.getSizeC();
            
            System.err.println("Series count: " + reader.getSeriesCount());
            System.err.println("SizeT: " + reader.getSizeT());
            System.err.println("SizeC: " + reader.getSizeC());
            System.err.println("SizeZ: " + reader.getSizeZ());            
            //pixels = (Pixels) store.getRoot();
            
            //calculateCTXYZ();
            //setOffsetInfo();
            
            int i = 1;
            try {
                int bytesPerPixel = getBytesPerPixel(reader.getPixelType());
                byte[] arrayBuf = new byte[sizeX * sizeY * bytesPerPixel];
                
                for (int t = 0; t < sizeT; t++)
                {
                    for (int c = 0; c < sizeC; c++)
                    {
                        for (int z = 0; z < sizeZ; z++)
                        {
                            int planeNumber = getTotalOffset(z, c, t);
                            ByteBuffer buf =
                                openPlane2D(filename, planeNumber,
                                                   arrayBuf).getData();
                            arrayBuf = swapIfRequired(buf, filename);
                            i++;
                        }
                    }
                }
                
                Double[] preGlobalMin = null, preGlobalMax = null;
                Double[] preKnownMin = null, preKnownMax = null;
                
                preGlobalMin = new Double[sizeC];
                preGlobalMax = new Double[sizeC];
                preKnownMin = new Double[sizeC];
                preKnownMax = new Double[sizeC];
                for (int c=0; c<sizeC; c++) {
                    preGlobalMin[c] = reader.getChannelGlobalMinimum(c);
                    preGlobalMax[c] = reader.getChannelGlobalMaximum(c);

                    System.err.println("preGlobalMin: " + preGlobalMin[c]);
                    System.err.println("preGlobalMax: " + preGlobalMax[c]);

                    preKnownMin[c] = reader.getChannelKnownMinimum(c);
                    preKnownMax[c] = reader.getChannelKnownMaximum(c);

                    System.err.println("preKnownMin: " + preKnownMin[c]);
                    System.err.println("preKnownMax: " + preKnownMax[c]);              
                }
            } catch (FormatException e)
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
            } catch (IOException e)
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            throw new Exception();
        }        
    }
//    
//    /**
//     * calculates and returns the number of planes in this image. Also sets the
//     * offset info.
//     * 
//     * @param fileName filename for use in {@link #setOffsetInfo(String)}
//     * @return the number of planes in this image (z * c * t)
//     */
//    public void calculateCTXYZ()
//    {
//        this.sizeZ = pixels.getSizeZ().intValue();
//        this.sizeC = pixels.getSizeC().intValue();
//        this.sizeT = pixels.getSizeT().intValue();
//        this.sizeX = pixels.getSizeX().intValue();
//        this.sizeY = pixels.getSizeY().intValue();
//    }
//    
//    private void setOffsetInfo()
//    {
//        int order = 0;
//        order = getSequenceNumber(reader.getDimensionOrder());
//        setOffsetInfo(order, sizeZ, sizeC, sizeT);
//    }

//
//    private int getSequenceNumber(String dimOrder)
//    {
//        if (dimOrder.equals("XYZTC")) return 0;
//        if (dimOrder.equals("XYCZT")) return 1;
//        if (dimOrder.equals("XYZCT")) return 2;
//        if (dimOrder.equals("XYTCZ")) return 3;
//        throw new RuntimeException(dimOrder + " not represented in " +
//                "getSequenceNumber");
//    }
//    
//    /**
//     * This method calculates the size of a w, t, z section depending on which
//     * sequence is being used (either ZTW, WZT, or ZWT)
//     * 
//     * @param imgSequence
//     * @param numZSections
//     * @param numWaves
//     * @param numTimes
//     */
//    private void setOffsetInfo(int imgSequence, int numZSections, int numWaves,
//            int numTimes)
//    {
//        int smallOffset = 1;
//        switch (imgSequence)
//        {
//            // ZTW sequence
//            case 0:
//                zSize = smallOffset;
//                tSize = zSize * numZSections;
//                wSize = tSize * numTimes;
//                break;
//            // WZT sequence
//            case 1:
//                wSize = smallOffset;
//                zSize = wSize * numWaves;
//                tSize = zSize * numZSections;
//                break;
//            // ZWT sequence
//            case 2:
//                zSize = smallOffset;
//                wSize = zSize * numZSections;
//                tSize = wSize * numWaves;
//                break;
//            // TWZ sequence
//            case 3:
//                tSize = smallOffset;
//                wSize = tSize * numTimes;
//                zSize = wSize * numWaves;
//                break;
//        }
//    }
    
    
    /**
     * Obtains an object which represents a given plane within the file.
     * @param id The path to the file.
     * @param no The plane or section within the file to obtain.
     * @param buf Pre-allocated buffer which has a <i>length</i> that can fit
     * the byte count of an entire plane.
     * @return an object which represents the plane.
     * @throws FormatException if there is an error parsing the file.
     * @throws IOException if there is an error reading from the file or
     *   acquiring permissions to read the file.
     */
    private Plane2D openPlane2D(String id, int no, byte[] buf)
        throws FormatException, IOException
    {
        ByteBuffer plane;
        if (iReader.isRGB())
            plane = ByteBuffer.wrap(reader.openBytes(no));
        else
            plane = ByteBuffer.wrap(reader.openBytes(no, buf));

        return new Plane2D(plane, reader.getPixelType(), reader.isLittleEndian(),
                           reader.getSizeX(), reader.getSizeY());
    }
    
    /**
     * Retrieves how many bytes per pixel the current plane or section has.
     * @return the number of bytes per pixel.
     */
    private int getBytesPerPixel(int type) {
      switch(type) {
      case 0:
      case 1:
        return 1;  // INT8 or UINT8
      case 2:
      case 3:
        return 2;  // INT16 or UINT16
      case 4:
      case 5:
      case 6:
        return 4;  // INT32, UINT32 or FLOAT
      case 7:
        return 8;  // DOUBLE
      }
      throw new RuntimeException("Unknown type with id: '" + type + "'");
    }

    /**
     * Given any specific Z, W, and T, determine the totalOffset from the start
     * of the file
     * 
     * @param currentZ
     * @param currentW
     * @param currentT
     * @return
     */
    private int getTotalOffset(int currentZ, int currentW, int currentT)
    {
        return (zSize * currentZ) + (wSize * currentW) + (tSize * currentT);
    }
    
    /**
     * Examines a byte array to see if it needs to be byte swapped and modifies
     * the byte array directly.
     * @param byteArray The byte array to check and modify if required.
     * @return the <i>byteArray</i> either swapped or not for convenience.
     * @throws IOException if there is an error read from the file.
     * @throws FormatException if there is an error during metadata parsing.
     */
    private byte[] swapIfRequired(ByteBuffer buffer, String fileName)
      throws FormatException, IOException
    {
      int pixelType = reader.getPixelType();
      int bytesPerPixel = getBytesPerPixel(pixelType);

      // We've got nothing to do if the samples are only 8-bits wide or if they
      // are floating point.
      if (pixelType == FormatTools.FLOAT || pixelType == FormatTools.DOUBLE
                  || bytesPerPixel == 1) 
          return buffer.array();

      //System.err.println(fileName + " is Little Endian: " + isLittleEndian(fileName));
      if (reader.isLittleEndian()) {
        if (bytesPerPixel == 2) { // short
          ShortBuffer buf = buffer.asShortBuffer();
          for (int i = 0; i < (buffer.capacity() / 2); i++) {
          //short tmp = buf.get(i);
            buf.put(i, DataTools.swap(buf.get(i)));
          //if (tmp == 21253 || buf.get(i) == 21253) 
          //  {
                //System.err.println(tmp + " -> " + buf.get(i));
          //}
          }
        } else if (bytesPerPixel == 4) { // int/uint
            IntBuffer buf = buffer.asIntBuffer();
            for (int i = 0; i < (buffer.capacity() / 4); i++) {
              buf.put(i, DataTools.swap(buf.get(i)));
            }
        } else {
          throw new FormatException(
            "Unsupported sample bit width: '" + bytesPerPixel + "'");
        }
      }
      // We've got a big-endian file with a big-endian byte array.
      return buffer.array();
    }

    
    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        new Main();
        
        /*
        byte[] buf = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xF0 };
        int value = DataTools.bytesToInt(buf, 0, 4, false);
        System.err.println("DataTools value: " + value);
        int value2 = ByteBuffer.wrap(buf).getInt();
        System.err.println("Java NIO value: " + value2);
        */
    }
}
