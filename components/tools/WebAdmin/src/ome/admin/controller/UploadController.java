package ome.admin.controller;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import ome.admin.validator.FileValidator;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

public class UploadController implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * log4j logger
	 */
	static Logger logger = Logger.getLogger(IAdminExperimenterController.class.getName());

	private UploadedFile uploadedNewFile;

	public UploadedFile getUploadedNewFile() {
		return uploadedNewFile;
	}

	public void setUploadedNewFile(UploadedFile uploadedFile) {
		try {
			this.uploadedNewFile = uploadedFile;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String uploadFile() {

		try {
			if(FileValidator.validFileType(uploadedNewFile)) {
				System.out.println("name: " + uploadedNewFile.getName() + " ct: "
						+ uploadedNewFile.getContentType() + " byte: "
						+ uploadedNewFile.getBytes());
			} else {
				FacesContext context = FacesContext.getCurrentInstance();
				FacesMessage message = new FacesMessage("File is not valid. Please chose another file.");
				context.addMessage("uploadedNewFileForm", message);
				return "false";
			}
			
		} catch (IOException e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("IOException: "+e.getMessage());
			context.addMessage("uploadedNewFileForm", message);
			return "false";
		}

		return "success";
	}


}
