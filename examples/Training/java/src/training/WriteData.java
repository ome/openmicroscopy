/*
 * training.WriteData 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
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
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import omero.api.IMetadataPrx;
import omero.api.IQueryPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.model.Annotation;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
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
	extends ConnectToOMERO
{

	/** Information to edit.*/
	/** The id of an image.*/
	private long imageId = 27544;
	
	/** The id of a project.*/
	private long projectId = 3109;
	
	/** Path to the file to upload.*/
	private String fileToUpload = "path/to/fileToUpload.txt";// This file should already exist
	
	/** Path to the file. The file should already be there.*/
	private String downloadFileName = "path/to/download.txt";
	
	
	/** Maximum size of data read at once. */
	private static final int INC = 262144;
	
	/** The image.*/
	private ImageData image;

	private String generatedSha1 = "pending";
	
	private String fileMimeType = "application/octet-stream";
	
	private String description = "description";
	
	/** Load the image.*/
	private void loadImage()
		throws Exception
	{
		image = loadImage(imageId);
		if (image == null)
			throw new Exception("Image does not exist. Check ID.");
	}
	
	/** 
	 * Create a new dataset and link it to existing project.
	 * 
	 */
	private void createNewDataset()
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
		link.setParent(new ProjectI(projectId, false));
		IObject r = entryUnencrypted.getUpdateService().saveAndReturnObject(link);
		//With pojo
		link = new ProjectDatasetLinkI();
		link.setChild(datasetData.asDataset());
		link.setParent(new ProjectI(projectId, false));
		r = entryUnencrypted.getUpdateService().saveAndReturnObject(link);
	}
	
	/** 
	 * Create a new tag and link it to existing project.
	 */
	private void createNewTag()
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
		link.setParent(new ProjectI(projectId, false));
		IObject r = entryUnencrypted.getUpdateService().saveAndReturnObject(link);
		//With pojo
		link = new ProjectAnnotationLinkI();
		link.setChild(tagData.asAnnotation());
		link.setParent(new ProjectI(projectId, false));
		r = entryUnencrypted.getUpdateService().saveAndReturnObject(link);
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
		File file = new File(fileToUpload);
		String name = file.getName();
		String absolutePath = file.getAbsolutePath();
		String path = absolutePath.substring(0, 
				absolutePath.length()-name.length());
		
		IUpdatePrx iUpdate = entryUnencrypted.getUpdateService(); // service used to write object
		// create the original file object.
		OriginalFile originalFile = new OriginalFileI();
		originalFile.setName(omero.rtypes.rstring(name));
		originalFile.setPath(omero.rtypes.rstring(path));
		originalFile.setSize(omero.rtypes.rlong(file.length()));
		originalFile.setSha1(omero.rtypes.rstring(generatedSha1));
		originalFile.setMimetype(omero.rtypes.rstring(fileMimeType)); // or "application/octet-stream"
		// now we save the originalFile object
		originalFile = (OriginalFile) iUpdate.saveAndReturnObject(originalFile);

		// Initialize the service to load the raw data
		RawFileStorePrx rawFileStore = entryUnencrypted.createRawFileStore();
		rawFileStore.setFileId(originalFile.getId().getValue());
		// open file and read stream.
		FileInputStream stream = new FileInputStream(file);
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
		stream.close();
		originalFile = rawFileStore.save();
		// Important to close the service
		rawFileStore.close();
		//now we have an original File in DB and raw data uploaded.
		// We now need to link the Original file to the image using 
		// the File annotation object. That's the way to do it.
		FileAnnotation fa = new FileAnnotationI();
		fa.setFile(originalFile);
		fa.setDescription(omero.rtypes.rstring(description)); // The description set above e.g. PointsModel
		fa.setNs(omero.rtypes.rstring(trainingNameSpace)); // The name space you have set to identify the file annotation.

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
		long userId = entryUnencrypted.getAdminService().getEventContext().userId;
		List<String> nsToInclude = new ArrayList<String>();
		nsToInclude.add(trainingNameSpace);
		List<String> nsToExclude = new ArrayList<String>();
		ParametersI param = new ParametersI();
		param.exp(omero.rtypes.rlong(userId)); //load the annotation for a given user.
		IMetadataPrx proxy = entryUnencrypted.getMetadataService();
		// retrieve the annotations linked to images, for datasets use: omero.model.Dataset.class
		List<Annotation> annotations = proxy.loadSpecifiedAnnotations(
				FileAnnotation.class.getName(), nsToInclude, nsToExclude, param);
		//Do something with annotations.
		Iterator<Annotation> j = annotations.iterator();
		Annotation annotation;
		FileAnnotationData fa;
		RawFileStorePrx store = entryUnencrypted.createRawFileStore();
		int index = 0;
		File file = new File(downloadFileName);
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
		store.close();
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
		IQueryPrx svc = entryUnencrypted.getQueryService();
		return (OriginalFile) svc.findByQuery(
				"select p from OriginalFile as p " +
				"where p.id = :id", param);
	}
	
	/**
	 * Connects and invokes the various methods.
	 */
	WriteData()
	{
		try {
			connect();
			loadImage();
			createFileAnnotationAndLinkToImage();
			loadAnnotationsLinkedToImage();
			createNewDataset();
			createNewTag();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				disconnect(); // Be sure to disconnect
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		new WriteData();
		System.exit(0);
	}

}
