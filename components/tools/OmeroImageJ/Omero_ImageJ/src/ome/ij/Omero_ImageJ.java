package ome.ij;

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
import omero.api.RawPixelsStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.model.Image;
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
		status = options.promptOptions();

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
		Image theImage = null;
		try {
			theContainer = theNewServiceFactory.getContainerService();
			theImage = getImage(theContainer, theCredentials.imageID);
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    IJ.showStatus("Reading Image...");
		theImage.getPrimaryPixels().getSizeX();
		byte[] theArray = null;
		try {
			RawPixelsStorePrx theRawPixelsStore = theNewServiceFactory.createRawPixelsStore();
			theRawPixelsStore.setPixelsId(theImage.getPrimaryPixels().getId().getValue(), true);
			theArray = theRawPixelsStore.getPlane(0, 0, 0);
		    IJ.showStatus("Reading Image... (got pixel store)");
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        try {
    	    IJ.showStatus("Creating Image5D...");
    	    int imageCreationOptions = 0;
    	    
        	Image5D i5d = createImage5D(
            		theImage.getName().getValue(), 
            		theImage.getPrimaryPixels().getSizeX().getValue(), 
            		theImage.getPrimaryPixels().getSizeY().getValue(), 
            		theImage.getPrimaryPixels().getSizeC().getValue(), 
            		theImage.getPrimaryPixels().getSizeZ().getValue(), 
            		theImage.getPrimaryPixels().getSizeT().getValue(), 
            		8, /* 8, 16 or 32 */
            		imageCreationOptions
            		);
            i5d.setDefaultColors();
            i5d.setDefaultChannelNames();
            if (i5d!=null) {
                i5d.show();
            }

        }
        catch(OutOfMemoryError e) {
        	IJ.outOfMemory("New_Image5D");
        }
	    IJ.showStatus("Saving Preferances...");
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
        Image5D i5d = new Image5D(title, imageType, width, height, nChannels, nSlicesZ, nFramesT, true /* was fill */);

        for (int c=1; c<=nChannels; c++) {
            for (int s=1; s<=nSlicesZ; s++) {
                for (int f=1; f<=nFramesT; f++) {
                    ImagePlus imp = NewImage.createImage(title, width, height, 1, bitDepth, options);
                    i5d.setPixels(imp.getProcessor().getPixels(), c, s, f);
                }
            }
        }

        i5d.updateImageAndDraw();
        return i5d;        
    }

}

