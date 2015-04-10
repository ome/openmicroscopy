/*
 * training.WriteData 
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//Third-party libraries


import omero.api.IContainerPrx;
//Application-internal dependencies
import omero.api.IMetadataPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.model.Annotation;
import omero.model.ChecksumAlgorithm;
import omero.model.ChecksumAlgorithmI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.ProjectAnnotationLink;
import omero.model.ProjectAnnotationLinkI;
import omero.model.ProjectDatasetLink;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.enums.ChecksumAlgorithmSHA1160;
import omero.sys.ParametersI;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.TagAnnotationData;

/** 
 * Sample code showing how to write data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.3.2
 */
public class WriteData
{

	//The value used if the configuration file is not used. To edit*/
	/** The server address.*/
	private String hostName = "serverName";

	/** The username.*/
	private String userName = "userName";
	
	/** The password.*/
	private String password = "password";
	
	/** The id of an image.*/
	private long imageId = 1;
	
	/** The id of a project.*/
	private long projectId = 1;

	//end edit
	
	/** Maximum size of data read at once. */
	private static final int INC = 262144;
	
	/** The image.*/
	private ImageData image;
	
	private String fileMimeType = "application/octet-stream";
	
	private String description = "description";
	
	/** Reference to the connector.*/
	private Connector connector;
	
	/**
	 * Loads the image.
	 * 
	 * @param imageID The id of the image to load.
	 * @return See above.
	 */
	private ImageData loadImage(long imageID)
		throws Exception
	{
		IContainerPrx proxy = connector.getContainerService();
		List<Image> results = proxy.getImages(Image.class.getName(),
				Arrays.asList(imageID), new ParametersI());
		//You can directly interact with the IObject or the Pojos object.
		//Follow interaction with the Pojos.
		if (results.size() == 0)
			throw new Exception("Image does not exist. Check ID.");
		return new ImageData(results.get(0));
	}
	
	/** 
	 * Create a new dataset and link it to existing project.
	 * 
	 */
	private void createNewDataset(ConfigurationInfo info)
		throws Exception
	{
		//Using IObject directly
		Dataset dataset = new DatasetI();
		dataset.setName(omero.rtypes.rstring("new Name 1"));
		dataset.setDescription(omero.rtypes.rstring("new description 1"));
		
		//Using the pojo
		DatasetData datasetData = new DatasetData();
		datasetData.setName("new Name 2");
		datasetData.setDescription("new description 2");
		
		ProjectDatasetLink link = new ProjectDatasetLinkI();
		link.setChild(dataset);
		link.setParent(new ProjectI(info.getProjectId(), false));
		IObject r = connector.getUpdateService().saveAndReturnObject(link);
		//With pojo
		link = new ProjectDatasetLinkI();
		link.setChild(datasetData.asDataset());
		link.setParent(new ProjectI(info.getProjectId(), false));
		r = connector.getUpdateService().saveAndReturnObject(link);
	}
	
	/** 
	 * Create a new tag and link it to existing project.
	 */
	private void createNewTag(ConfigurationInfo info)
		throws Exception
	{
		TagAnnotation tag = new TagAnnotationI();
		tag.setTextValue(omero.rtypes.rstring("new tag 1"));
		tag.setDescription(omero.rtypes.rstring("new tag 1"));
		
		//Using the pojo
		TagAnnotationData tagData = new TagAnnotationData("new tag 2");
		tagData.setTagDescription("new tag 2");
		
		ProjectAnnotationLink link = new ProjectAnnotationLinkI();
		link.setChild(tag);
		link.setParent(new ProjectI(info.getProjectId(), false));
		IObject r = connector.getUpdateService().saveAndReturnObject(link);
		//With pojo
		link = new ProjectAnnotationLinkI();
		link.setChild(tagData.asAnnotation());
		link.setParent(new ProjectI(info.getProjectId(), false));
		r = connector.getUpdateService().saveAndReturnObject(link);
	}
	
	
	/**
	 * How to create a file annotation and link to an image. 
	 * 
	 * To attach a file to an object e.g. an image, few objects need to be created:
	 * 1. an OriginalFile
	 * 1 a FileAnnotation
	 * 1 a link between the Image and the FileAnnotation. 
	 */
	private void createFileAnnotationAndLinkToImage()
		throws Exception
	{
		// To retrieve the image see above.
		File file = File.createTempFile("temp-file-name_", ".tmp"); 
		String name = file.getName();
		String absolutePath = file.getAbsolutePath();
		String path = absolutePath.substring(0, 
				absolutePath.length()-name.length());
		
		IUpdatePrx iUpdate = connector.getUpdateService(); // service used to write object
		// create the original file object.
		OriginalFile originalFile = new OriginalFileI();
		originalFile.setName(omero.rtypes.rstring(name));
		originalFile.setPath(omero.rtypes.rstring(path));
		originalFile.setSize(omero.rtypes.rlong(file.length()));
		final ChecksumAlgorithm checksumAlgorithm = new ChecksumAlgorithmI();
        checksumAlgorithm.setValue(omero.rtypes.rstring(ChecksumAlgorithmSHA1160.value));
		originalFile.setHasher(checksumAlgorithm);
		originalFile.setMimetype(omero.rtypes.rstring(fileMimeType)); // or "application/octet-stream"
		// now we save the originalFile object
		originalFile = (OriginalFile) iUpdate.saveAndReturnObject(originalFile);

		// Initialize the service to load the raw data
		RawFileStorePrx rawFileStore = null;
		FileInputStream stream = null;
		try {
			rawFileStore = connector.getRawFileStore();
			rawFileStore.setFileId(originalFile.getId().getValue());
			// open file and read stream.
			stream = new FileInputStream(file);
			long pos = 0;
			int rlen;
			byte[] buf = new byte[INC];
			ByteBuffer bbuf;
			while ((rlen = stream.read(buf)) > 0) {
				rawFileStore.write(buf, pos, rlen);
				pos += rlen;
				bbuf = ByteBuffer.wrap(buf);
				bbuf.limit(rlen);
			}
			originalFile = rawFileStore.save();
		} catch (Exception e) {
			throw new Exception("Cannot read data", e);
		} finally {
			if (rawFileStore != null) rawFileStore.close();
			if (stream != null) stream.close();
			if (file != null) file.delete();
		}
		
		
		//now we have an original File in DB and raw data uploaded.
		// We now need to link the Original file to the image using 
		// the File annotation object. That's the way to do it.
		FileAnnotation fa = new FileAnnotationI();
		fa.setFile(originalFile);
		fa.setDescription(omero.rtypes.rstring(description)); // The description set above e.g. PointsModel
		fa.setNs(omero.rtypes.rstring(ConfigurationInfo.TRAINING_NS)); // The name space you have set to identify the file annotation.

		// save the file annotation.
		fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);

		// now link the image and the annotation
		ImageAnnotationLink link = new ImageAnnotationLinkI();
		link.setChild(fa);
		link.setParent(image.asImage());
		// save the link back to the server.
		link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);
		// To attach to a Dataset use DatasetAnnotationLink;
	}
	
	/**
	 * Load all the annotations with a given namespace linked to images.
	 * 
	 */
	private void loadAnnotationsLinkedToImage()
		throws Exception
	{
		long userId = connector.getAdminService().getEventContext().userId;
		List<String> nsToInclude = new ArrayList<String>();
		nsToInclude.add(ConfigurationInfo.TRAINING_NS);
		List<String> nsToExclude = new ArrayList<String>();
		ParametersI param = new ParametersI();
		param.exp(omero.rtypes.rlong(userId)); //load the annotation for a given user.
		IMetadataPrx proxy = connector.getMetadataService();
		// retrieve the annotations linked to images, for datasets use: omero.model.Dataset.class
		List<Annotation> annotations = proxy.loadSpecifiedAnnotations(
				FileAnnotation.class.getName(), nsToInclude, nsToExclude, param);
		//Do something with annotations.
		Iterator<Annotation> j = annotations.iterator();
		Annotation annotation;
		FileAnnotationData fa;
		RawFileStorePrx store = null;
		File file = File.createTempFile("temp-file-name_", ".tmp"); 
		try {
			store = connector.getRawFileStore();
			int index = 0;
			FileOutputStream stream = new FileOutputStream(file);
			OriginalFile of;
			while (j.hasNext()) {
				annotation = j.next();
				if (annotation instanceof FileAnnotation && index == 0) {
					fa = new FileAnnotationData((FileAnnotation) annotation);
					//The id of te original file
					of = getOriginalFile(fa.getFileID());
					store.setFileId(fa.getFileID());
					int offset = 0;
					long size = of.getSize().getValue();
					//name of the file
					of.getName().getValue();
					try {
						for (offset = 0; (offset+INC) < size;) {
							stream.write(store.read(offset, INC));
							offset += INC;
						}	
					} finally {
						stream.write(store.read(offset, (int) (size-offset))); 
						stream.close();
					}
					index++;
				}
			}
		} catch (Exception e) {
			throw new Exception("cannot read the data", e);
		} finally {
			if (store != null) store.close();
			if (file != null) file.delete();
		}
	}
	
	/**
	 * Returns the original file corresponding to the passed id.
	 * 
	 * @param id	The id identifying the file.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private OriginalFile getOriginalFile(long id)
		throws Exception
	{
		ParametersI param = new ParametersI();
		param.map.put("id", omero.rtypes.rlong(id));
		IQueryPrx svc = connector.getQueryService();
		return (OriginalFile) svc.findByQuery(
				"select p from OriginalFile as p " +
				"where p.id = :id", param);
	}
	
	/**
	 * Connects and invokes the various methods.
	 */
	WriteData(ConfigurationInfo info)
	{
		if (info == null) {
			info = new ConfigurationInfo();
			info.setHostName(hostName);
			info.setPassword(password);
			info.setUserName(userName);
			info.setImageId(imageId);
			info.setProjectId(projectId);
		}
		connector = new Connector(info);
		try {
			connector.connect();
			image = loadImage(info.getImageId());
			createFileAnnotationAndLinkToImage();
			loadAnnotationsLinkedToImage();
			createNewDataset(info);
			createNewTag(info);
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
		new WriteData(null);
		System.exit(0);
	}

}
