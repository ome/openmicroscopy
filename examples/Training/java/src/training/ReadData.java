/*
 * training.ReadData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
 *------------------------------------------------------------------------------
 */
package training;


//Java imports
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

//Third-party libraries



//Application-internal dependencies
import omero.api.IContainerPrx;
import omero.api.IQueryPrx;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Length;
import omero.model.Plate;
import omero.model.Project;
import omero.model.Screen;
import omero.model.Well;
import omero.model.enums.UnitsLength;
import omero.sys.ParametersI;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.WellData;

/** 
 * Sample code showing how to load data from an OMERO server.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class ReadData
{

	//The value used if the configuration file is not used. To edit*/
	/** The server address.*/
	private String hostName = "serverName";

	/** The username.*/
	private String userName = "userName";
	
	/** The password.*/
	private String password = "password";
	
	/** The id of a dataset.*/
	private long datasetId = 1;
	
	/** The id of an image.*/
	private long imageId = 1;
	
	/** The id of a plate.*/
	private long plateId = 1;
	
	/** The id of the plate acquisition corresponding to the plate.*/
	private long plateAcquisitionId = 1;
	//end edit

	/** Reference to the connector.*/
	private Connector connector;
	
	/** 
	 * Retrieve the projects owned by the user currently logged in.
	 * 
	 * If a project contains datasets, the datasets will automatically be loaded.
	 */
	private void loadProjects()
		throws Exception
	{
		IContainerPrx proxy = connector.getContainerService();
		ParametersI param = new ParametersI();
		long userId = connector.getAdminService().getEventContext().userId;
		param.exp(omero.rtypes.rlong(userId));
		param.leaves(); //indicate to load the images
		//param.noLeaves(); //no images loaded, this is the default value.
		List<IObject> results = proxy.loadContainerHierarchy(
				Project.class.getName(), new ArrayList<Long>(), param);
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		Iterator<IObject> i = results.iterator();
		ProjectData project;
		Set<DatasetData> datasets;
		Iterator<DatasetData> j;
		DatasetData dataset;
		while (i.hasNext()) {
			project = new ProjectData((Project) i.next());
			System.err.println("Project:"+project.getId()+" "+project.getName());
			datasets = project.getDatasets();
			j = datasets.iterator();
			while (j.hasNext()) {
				dataset = j.next();
				System.err.println("dataset:"+dataset.getId()+" "+dataset.getName());
				//Do something here
				//If images loaded.
				//dataset.getImages();
			}
		}
	}
	
	/** 
	 * Retrieve the datasets owned by the user currently logged in.
	 */
	@SuppressWarnings("unchecked")
	private void loadDatasets()
		throws Exception
	{
		IContainerPrx proxy = connector.getContainerService();
		ParametersI param = new ParametersI();
		long userId = connector.getAdminService().getEventContext().userId;
		param.exp(omero.rtypes.rlong(userId));
		param.leaves(); //indicate to load the images
		//param.noLeaves(); //no images loaded, this is the default value.
		List<IObject> results = proxy.loadContainerHierarchy(
				Dataset.class.getName(), new ArrayList<Long>(), param);
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		Iterator<IObject> i = results.iterator();
		DatasetData dataset;
		Set<ImageData> images;
		Iterator<ImageData> j;
		ImageData image;
		while (i.hasNext()) {
			dataset = new DatasetData((Dataset) i.next());
			images = dataset.getImages();
			j = images.iterator();
			while (j.hasNext()) {
				image = j.next();
				System.err.println("image:"+image.getId()+" "+image.getName());
			}
		}
	}
	
	/** 
	 * Retrieve the images contained in a dataset.
	 * 
	 * In that case, we specify the dataset's id.
	 * @param info The configuration information.
	 */
	@SuppressWarnings("unchecked")
	private void loadImagesInDataset(ConfigurationInfo info)
		throws Exception
	{
		IContainerPrx proxy = connector.getContainerService();
		ParametersI param = new ParametersI();
		param.leaves(); //indicate to load the images
		List<IObject> results = proxy.loadContainerHierarchy(
				Dataset.class.getName(), Arrays.asList(info.getDatasetId()),
				param);
		
		if (results.size() == 0)
			throw new Exception("Dataset does not exist. Check ID");
		
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		DatasetData dataset = new DatasetData((Dataset) results.get(0));
		Set<ImageData> images = dataset.getImages();
		Iterator<ImageData> j = images.iterator();
		ImageData image;
		while (j.hasNext()) {
			image = j.next();
			System.err.println("image:"+image.getId()+" "+image.getName());
			//Do something
		}
	}
	
	/** 
	 * Retrieve an image if the identifier is known.
	 */
	private void loadImage(ConfigurationInfo info)
		throws Exception
	{
		IContainerPrx proxy = connector.getContainerService();
		List<Image> results = proxy.getImages(Image.class.getName(),
				Arrays.asList(info.getImageId()), new ParametersI());
		if (results.size() == 0)
			throw new Exception("Image does not exist. Check ID.");
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		ImageData image = new ImageData(results.get(0));
		PixelsData pixels = image.getDefaultPixels();
		System.err.println(pixels.getSizeZ()); // The number of z-sections.
		System.err.println(pixels.getSizeT()); // The number of timepoints.
		System.err.println(pixels.getSizeC()); // The number of channels.
		System.err.println(pixels.getSizeX()); // The number of pixels along the X-axis.
		System.err.println(pixels.getSizeY()); // The number of pixels along the Y-axis.

		//Get Pixel Size for the above Image
		Length sizeX = pixels.getPixelSizeX(null);
		System.err.println("Pixel Size X:" + sizeX.getValue() + sizeX.getSymbol());
		//To get the size the size with different units, E.g. Angstroms
		Length sizeXang = pixels.getPixelSizeX(UnitsLength.ANGSTROM);
		System.err.println("Pixel Size X:" + sizeXang.getValue() + sizeXang.getSymbol());
	}
	
	/** 
	 * Retrieve Screening data owned by the user currently logged in.
	 * 
	 * To learn about the model go to ScreenPlateWell.
	 * Note that the wells are not loaded.
	 */
	private void loadScreens()
		throws Exception
	{
		IContainerPrx proxy = connector.getContainerService();
		ParametersI param = new ParametersI();
		long userId = connector.getAdminService().getEventContext().userId;
		param.exp(omero.rtypes.rlong(userId));
		
		List<IObject> results = proxy.loadContainerHierarchy(
				Screen.class.getName(), new ArrayList<Long>(), param);
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		Iterator<IObject> i = results.iterator();
		ScreenData screen;
		Set<PlateData> plates;
		Iterator<PlateData> j;
		PlateData plate;
		while (i.hasNext()) {
			screen = new ScreenData((Screen) i.next());
			System.err.println("screen:"+screen.getId()+" "+screen.getName());
			plates = screen.getPlates();
			j = plates.iterator();
			while (j.hasNext()) {
				plate = j.next();
				System.err.println("plate:"+plate.getId()+" "+plate.getName());
			}
		}
	}
	
	/** 
	 * Retrieve Screening data owned by the user currently logged in.
	 * 
	 * To learn about the model go to ScreenPlateWell.
	 * Note that the wells are not loaded.
	 */
	private void loadWells(ConfigurationInfo info)
		throws Exception
	{
		IQueryPrx proxy = connector.getQueryService();
		StringBuilder sb = new StringBuilder();
		ParametersI param = new ParametersI();
		param.addLong("plateID", info.getPlateId());
		sb.append("select well from Well as well ");
		sb.append("left outer join fetch well.plate as pt ");
		sb.append("left outer join fetch well.wellSamples as ws ");
		sb.append("left outer join fetch ws.plateAcquisition as pa ");
		sb.append("left outer join fetch ws.image as img ");
		sb.append("left outer join fetch img.pixels as pix ");
        sb.append("left outer join fetch pix.pixelsType as pt ");
        sb.append("where well.plate.id = :plateID");
        if (info.getPlateAcquisitionId() > 0) {
        	 sb.append(" and pa.id = :acquisitionID");
        	 param.addLong("acquisitionID", info.getPlateAcquisitionId());
        }
        List<IObject> results = proxy.findAllByQuery(sb.toString(), param);
        Iterator<IObject> i = results.iterator();
        WellData well;
        while (i.hasNext()) {
			well = new WellData((Well) i.next());
			//Do something
		}
	}
	
	/** 
	 * Retrieve Screening data owned by the user currently logged in.
	 * 
	 * To learn about the model go to ScreenPlateWell.
	 * Note that the wells are not loaded.
	 */
	private void loadPlate(ConfigurationInfo info)
		throws Exception
	{
		IQueryPrx proxy = connector.getQueryService();
		StringBuilder sb = new StringBuilder();
		ParametersI param = new ParametersI();
		param.addLong("plateID", info.getPlateId());
		sb.append("select plate from Plate as plate ");
		sb.append("left outer join fetch plate.wells as well ");
		sb.append("left outer join fetch well.wellSamples as ws ");
		sb.append("left outer join fetch ws.plateAcquisition as pa ");
		sb.append("left outer join fetch ws.image as img ");
		sb.append("left outer join fetch img.pixels as pix ");
		sb.append("left outer join fetch pix.pixelsType as pt ");
        sb.append("where plate.id = :plateID");
        if (info.getPlateAcquisitionId() > 0) {
        	 sb.append(" and pa.id = :acquisitionID");
        	 param.addLong("acquisitionID", info.getPlateAcquisitionId());
        }
        List<IObject> results = proxy.findAllByQuery(sb.toString(), param);
        Iterator<IObject> i = results.iterator();
        PlateData plate;
        while (i.hasNext()) {
        	plate = new PlateData((Plate) i.next());
		}
	}
	
	/**
	 * Connects and invokes the various methods.
	 * 
	 * @param info The configuration information.
	 */
	ReadData(ConfigurationInfo info)
	{
		if (info == null) {
			info = new ConfigurationInfo();
			info.setHostName(hostName);
			info.setPassword(password);
			info.setUserName(userName);
			info.setImageId(imageId);
			info.setDatasetId(datasetId);
			info.setPlateAcquisitionId(plateAcquisitionId);
			info.setPlateId(plateId);
		}
		connector = new Connector(info);
		try {
			connector.connect(); //First connect.
			loadProjects();
			loadDatasets();
			loadImagesInDataset(info);
			loadImage(info);
			loadScreens();
			loadWells(info);
			loadPlate(info);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connector.disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runs the script without configuration options.
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		new ReadData(null);
		System.exit(0);
	}

}
