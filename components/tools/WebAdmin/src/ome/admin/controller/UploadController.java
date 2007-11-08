package ome.admin.controller;

//Java imports
import java.io.File;
import java.io.IOException;

//Third-party libraries
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

//Application-internal dependencies
import ome.admin.validator.FileValidator;
import ome.utils.NavigationResults;

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
public class UploadController {

	/**
	 * log4j logger
	 */
	static Logger logger = Logger.getLogger(IAdminExperimenterController.class
			.getName());

	/**
	 * Uploaded file
	 */
	private UploadedFile uploadedNewFile;

	/**
	 * boolean value for providing edit form in JSP.
	 */
	private boolean editMode = false;

	/**
	 * Gets {@link ome.admin.controller.UploadController#uploadedNewFile}
	 * 
	 * @return {@link org.apache.myfaces.custom.fileupload.UploadedFile}
	 */
	public UploadedFile getUploadedNewFile() {
		return uploadedNewFile;
	}

	/**
	 * Sets {@link ome.admin.controller.UploadController#uploadedNewFile}
	 * 
	 * @param uploadedFile -
	 *            {@link org.apache.myfaces.custom.fileupload.UploadedFile}
	 */
	public void setUploadedNewFile(UploadedFile uploadedFile) {
		this.uploadedNewFile = uploadedFile;
	}

	/**
	 * Checks {@link ome.admin.controller.UploadController#editMode}
	 * 
	 * @return boolean
	 */
	public boolean isEditMode() {
		return editMode;
	}

	/**
	 * Sets {@link ome.admin.controller.UploadController#editMode}
	 */
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
	}

	/**
	 * Uploads file with validation
	 * 
	 * @return String "success" or "false"
	 */
	public String uploadFile() {
		try {
			if (FileValidator.validFileType(uploadedNewFile)) {
				logger.info("Filename: " + uploadedNewFile.getName()
						+ " ContentType: " + uploadedNewFile.getContentType()
						+ " Byte: " + uploadedNewFile.getBytes().toString());
				return NavigationResults.SUCCESS;
			} else {
				FacesContext context = FacesContext.getCurrentInstance();
				FacesMessage message = new FacesMessage(
						"File is not valid. Please chose another file.");
				context.addMessage("uploadedNewFileForm", message);
				return NavigationResults.FALSE;
			}

		} catch (IOException e) {
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("IOException: "
					+ e.getMessage());
			context.addMessage("uploadedNewFileForm", message);
			return NavigationResults.FALSE;
		}

	}

	/**
	 * Checks direcotry exists.
	 * 
	 * @return boolean
	 */
	public boolean isDirectory() {
		boolean result = checkDirectory();
		this.editMode = result;
		return result;
	}

	/**
	 * Checks that directory where uploaded file is stored /OMERO/UsersLists
	 * exists.
	 * 
	 * @return
	 */
	private boolean checkDirectory() {
		FacesContext context = FacesContext.getCurrentInstance();
		File dir = new File(context.getExternalContext().getInitParameter(
				"usersListsDir"));

		if (!dir.exists()) {
			if (dir.mkdir()) {
				logger.info("isDirectory: directory " + dir.getAbsolutePath()
						+ " did not exist and was created successful.");
				FacesMessage message = new FacesMessage("Directory "
						+ dir.getAbsolutePath() + " created successful.");
				context.addMessage("uploadedNewFileForm", message);
				return true;
			} else {
				logger.info("isDirectory: directory " + dir.getAbsolutePath()
						+ " did not exist and could not be created.");
				FacesMessage message = new FacesMessage(
						"IOException: Could not create directory "
								+ dir.getAbsolutePath() + ".");
				context.addMessage("uploadedNewFileForm", message);
				return false;
			}
		} else
			return true;
	}

}
