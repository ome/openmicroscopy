package ome.admin.controller;

//Java imports
import java.io.IOException;

//Third-party libraries
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

//Application-internal dependencies
import ome.admin.validator.FileValidator;

/**
 * It's the Java bean with attributes and setter/getter and actions methods. The
 * bean captures login params entered by a user after the user clicks the submit
 * button. This way the bean provides a bridge between the JSP page and the
 * application logic.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
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
				logger.info("name: " + uploadedNewFile.getName() + " ct: "
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
