package ome.ij;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import ij.IJ;

import ij.plugin.PlugIn;

import ij.gui.NewImage;
import ij.ImagePlus;
import i5d.Image5D;

import omero.ServerError;
import omero.client;
import omero.api.IContainerPrx;
import omero.api.IPixelsPrx;
import omero.api.RawPixelsStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Image;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.StatsInfo;
import omero.sys.ParametersI;

public class Omero_ImageJ implements PlugIn {
	public void run(String arg) {
		//		minconnect.connect();
		ImporterOptions options = new ImporterOptions();
		options.loadPreferences();
		options.parseArg("location=[OMERO server]");

		int status;
		status = options.promptLocation();
		status = options.promptId();
		// status = options.promptOptions();

		String id = options.getId();

		System.out.println("Id String ='" + id + "'");

		OMEROCredentials theCredentials = new OMEROCredentials(id);

		client theNewClient= new client(theCredentials.server, theCredentials.portInteger);
		ServiceFactoryPrx theNewServiceFactory = null;
		try {
			theNewServiceFactory = theNewClient.joinSession(theCredentials.session);
		} catch (CannotCreateSessionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PermissionDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Getting IContainer Proxy");
		IContainerPrx theContainer;
		IPixelsPrx iPixels;
		Image theImage = null;
		Pixels thePixels = null;
		long pixelsId = 0;
		try {
			theContainer = theNewServiceFactory.getContainerService();
			iPixels = theNewServiceFactory.getPixelsService();
			theImage = getImage(theContainer, theCredentials.imageID);
			pixelsId = theImage.getPrimaryPixels().getId().getValue();
			thePixels = iPixels.retrievePixDescription(pixelsId);
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int sizeX = thePixels.getSizeX().getValue();
		int sizeY = thePixels.getSizeY().getValue();
		int sizeZ = thePixels.getSizeZ().getValue();
		int sizeC = thePixels.getSizeC().getValue();
		int sizeT = thePixels.getSizeT().getValue();
		
        try {
    	    IJ.showStatus("Creating Image5D...");
    	    int imageCreationOptions = 0;
    	    
    	    // get the bit depth of the image
    	    PixelsType thePixelsType = theImage.getPrimaryPixels().getPixelsType();
    	    String theVal = thePixelsType.getValue().getValue();
    	    
    	    // TODO replace with GatewayUtils.getBytesPerPixels when getBytesPerPixels is public in gateway
            int theBitDepth = 8;
            switch (getBytesPerPixels(theVal)) {
                case 1:
                	theBitDepth = 8;
                    break;
                case 2:
                	theBitDepth = 16;
                    break;
                case 4:
                	theBitDepth = 32;
                    break;
                default:
                	// TODO what to do for double? 
                	theBitDepth = 8;
            }
        	Image5D i5d = createImage5D(
            		theImage.getName().getValue(),
            		sizeX, sizeY, sizeC, sizeZ, sizeT,
            		theBitDepth, /* 8, 16 or 32 */
            		imageCreationOptions
            		);
    		
    		IJ.showStatus("Reading Image...");				
    		RawPixelsStorePrx store = 
    			theNewServiceFactory.createRawPixelsStore();
    		store.setPixelsId(pixelsId, false);
    		for (int z = 0; z < sizeZ; z++)
    		{
    			for (int c = 0; c < sizeC; c++)
    			{
    				// We populate the minimum and maximum for each channel
    				// here from the database in order to improve visualization
    				// quality.
    				StatsInfo statsInfo = 
    					thePixels.getChannel(c).getStatsInfo();
    				double min = statsInfo.getGlobalMin().getValue();
    				double max = statsInfo.getGlobalMax().getValue();
    				// Channel "offsets" in ImageJ start at 1
    				i5d.setChannelMinMax(c + 1, min, max);
    				for (int t = 0; t < sizeT; t++)
    				{
    					i5d.setCurrentPosition(0, 0, c, z, t);
    					if (theBitDepth == 8)
    					{
    						i5d.setPixels(store.getPlane(z, c, t));
    					}
    					else if (theBitDepth == 16)
    					{
    						i5d.setPixels(asShort(store.getPlane(z, c, t)));
    					}
    					else if (theBitDepth == 32)
    					{
    						i5d.setPixels(asInt(store.getPlane(z, c, t)));
    					}
    				}
    			}
    		}

    		i5d.setCurrentPosition(0, 0, 0, (sizeZ / 2) + 1, 0);
    		i5d.show();
        }
        catch(OutOfMemoryError e)
        {
        	IJ.outOfMemory("New_Image5D");
        }
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	    IJ.showStatus("Saving Preferences...");
		options.savePreferences();
	    IJ.showStatus("Omero Reader - Done");
	}

	public Image getImage(IContainerPrx iContainer, Long theImageId)
	{
		try
		{
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(theImageId);
			ParametersI param = new ParametersI();
			param.leaves();
			List<Image> images = 
				iContainer.getImages(Image.class.getName(), ids, param);
			if (images.size() > 0)
			{
				Image image = (Image) images.get(0);
				return image;
			}
			return null;
		}
		catch (ServerError e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Transforms a byte array into a 16-bit integer array.
	 * @param plane Byte array to transform.
	 * @return Copy of <code>plane</code> as a 16-bit integer array.
	 */
	public short[] asShort(byte[] plane)
	{
		int pixelCount = plane.length / 2;
		short[] toReturn = new short[pixelCount];
		ShortBuffer source = ByteBuffer.wrap(plane).asShortBuffer();
		for (int i = 0; i < pixelCount; i++)
		{
			toReturn[i] = source.get(i);
		}
		return toReturn;
	}
	
	/**
	 * Transforms a byte array into a 32-bit integer array.
	 * @param plane Byte array to transform.
	 * @return Copy of <code>plane</code> as a 32-bit integer array.
	 */
	public int[] asInt(byte[] plane)
	{
		int pixelCount = plane.length / 4;
		int[] toReturn = new int[pixelCount];
		IntBuffer source = ByteBuffer.wrap(plane).asIntBuffer();
		for (int i = 0; i < pixelCount; i++)
		{
			toReturn[i] = source.get(i);
		}
		return toReturn;
	}

    public static Image5D createImage5D(
    		String title, 
    		int width, 
    		int height, 
    		int nChannels, 
    		int nSlicesZ, 
    		int nFramesT, 
    		int bitDepth, 
    		int options) 
    {
        int imageType = ImagePlus.GRAY8;
        switch (bitDepth) {
            case 8:
                imageType = ImagePlus.GRAY8;
                break;
            case 16:
                imageType = ImagePlus.GRAY16;
                break;
            case 32:
                imageType = ImagePlus.GRAY32;
                break;
            default:
                return null;
        }

        options |= NewImage.CHECK_AVAILABLE_MEMORY;

        // Create Image5D
        Image5D i5d = new Image5D(title, imageType, width, height, nChannels,
        		                  nSlicesZ, nFramesT, false);
        return i5d;        
    }

    // TODO remove this function when getBytesPerPixels is public in gateway
    
	/** Identifies the type used to store pixel values. */
	static final String INT_8 = "int8";

	/** Identifies the type used to store pixel values. */
	static final String UINT_8 = "uint8";

	/** Identifies the type used to store pixel values. */
	static final String INT_16 = "int16";

	/** Identifies the type used to store pixel values. */
	static final String UINT_16 = "uint16";

	/** Identifies the type used to store pixel values. */
	static final String INT_32 = "int32";

	/** Identifies the type used to store pixel values. */
	static final String UINT_32 = "uint32";

	/** Identifies the type used to store pixel values. */
	static final String FLOAT = "float";

	/** Identifies the type used to store pixel values. */
	static final String DOUBLE = "double";
		static private int getBytesPerPixels(String v)
	{
		if (INT_8.equals(v) || UINT_8.equals(v)) return 1;
		if (INT_16.equals(v) || UINT_16.equals(v)) return 2;
		if (INT_32.equals(v) || UINT_32.equals(v) || FLOAT.equals(v)) 
			return 4;
		if (DOUBLE.equals(v)) return 8;
		return -1;
	}
}

