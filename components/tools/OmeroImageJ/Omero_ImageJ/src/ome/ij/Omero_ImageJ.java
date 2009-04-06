package ome.ij;

import java.util.ArrayList;
import java.util.List;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;
import ij.IJ;
import ij.plugin.PlugIn;
import ij.gui.NewImage;
import ij.ImagePlus;


import static omero.rtypes.*;
import omero.RString;
import omero.ServerError;
import omero.client;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.RawPixelsStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ThumbnailStorePrx;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Project;
import omero.model.Image;
import omero.sys.EventContext;
import omero.sys.PojoOptions;

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
		} catch (ServerError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		ImagePlus theIjImagePlus = NewImage.createByteImage(, , , , );
//		NewImage. theIjImage = null;
//		theIjImage.
/*        
        ru.exec("import i5d.Image5D");
        ru.setVar("title", imp.getTitle());
        ru.setVar("stack", imp.getStack());
        ru.setVar("sizeC", c);
        ru.setVar("sizeZ", z);
        ru.setVar("sizeT", t);
        ru.exec("i5d = new Image5D(title, stack, sizeC, sizeZ, sizeT)");
        ru.setVar("cal", imp.getCalibration());
        ru.setVar("fi", imp.getOriginalFileInfo());
        ru.exec("i5d.setCalibration(cal)");
        ru.exec("i5d.setFileInfo(fi)");
        //ru.exec("i5d.setDimensions(sizeC, sizeZ, sizeT)");
        ru.exec("i5d.show()");
*/
		options.savePreferences();
	}

	public Image getImage(IContainerPrx iContainer, Long theImageId)
	{
		try
		{
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(theImageId);
			PojoOptions po = new PojoOptions();
			po.leaves();
			List<Image> images = 
				iContainer.getImages(Image.class.getName(), ids, po.map());
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

}

