package ome.admin.listener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.faces.event.ValueChangeListener;

import ome.admin.controller.LoginBean;
import ome.admin.data.HSSFWorkbookReader;
import ome.admin.validator.FileValidator;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;

public class ImportListener implements ValueChangeListener {
	
	/**
	 * log4j logger
	 */
	static Logger logger = Logger.getLogger(HSSFWorkbookReader.class.getName());
	
	public void processValueChange(ValueChangeEvent event)
			throws AbortProcessingException {

		UploadedFile uploadedFile = (UploadedFile) event.getNewValue();

		// Check type of file, only: XML, XLS, CSV
		if (FileValidator.validFileType(uploadedFile)) {
			
			FacesContext facesContext = FacesContext.getCurrentInstance();
			// Write file to archiv
			String usersListsDir = facesContext
					.getExternalContext().getInitParameter(
							"usersListsDir");

			LoginBean lb = ((LoginBean) facesContext.getApplication()
			.getVariableResolver().resolveVariable(facesContext,
					"LoginBean"));
			
			logger.info("ImportedFile : '" + uploadedFile.getName() + "' by user ID: "+lb.getId());
			
			// Change name of the File
			String fileName = FileValidator.changeNameOfFile(uploadedFile.getName());

			File out = null;
			FileOutputStream fo = null;
			InputStream in = null;
			try {
				if (FileValidator.checkDirectory(new File(usersListsDir+"/"+lb.getUsername()))) {
					
					in = new BufferedInputStream(uploadedFile
							.getInputStream());

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						bos.write(buf, 0, len);
					}
					byte[] data = bos.toByteArray();

					out = new File(usersListsDir+"/" + lb.getUsername()+"/" + fileName);
					fo = new FileOutputStream(out, false);
					fo.write(data);


					logger.info("File saved on : '" + usersListsDir+"/" + lb.getUsername()+"/" + fileName + "' by user ID: "+lb.getId());
				}
			} catch (FileNotFoundException e) {
				logger.error("File was not found exception : "+e.getMessage());
			} catch (IOException e) {
				logger.error("IO Exception : "+e.getMessage());
			} finally {
				try {
					fo.flush();
					fo.close();
					in.close();
				} catch (IOException e) {
					logger.error("IO Exception : "+e.getMessage());
				}
			
			}
				
			
		} 
	}

}