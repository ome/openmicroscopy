package ome.admin.validator;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.myfaces.custom.fileupload.UploadedFile;

public class FileValidator {

	public static boolean checkDirectory(File uploadPath) throws IOException {
		boolean exists = (uploadPath).exists();
		if (!exists) {
			// Create a directory; all ancestor directories must exist
			boolean success = (uploadPath).mkdir();
			if (!success) {
				throw new IOException("Could not create directory: "
						+ uploadPath.getPath());
			}
		}
		return true;
	}

	public static boolean validFileName(String fileName) {
		if (fileName.length() > 4) {
			ArrayList<String> supportedSuffix = new ArrayList<String>();
			supportedSuffix.add("xls");
			//supportedSuffix.add("xml");
			supportedSuffix.add("csv");

			String suffix = getTypeOfFile(fileName);
			if (supportedSuffix.contains(suffix))
				return true;

		}
		return false;
	}

	public static boolean validFileType(UploadedFile uploadedFile) {
		if(uploadedFile == null) 
			throw new RuntimeException("Uploaded file does not exist.");
		if (uploadedFile.getContentType().equals("application/octet-stream")
				|| uploadedFile.getContentType().equals("text/plain")
				//|| uploadedFile.getContentType().equals("text/xml")
				) {

			return validFileName(uploadedFile.getName());
		}
		return false;
	}

	public static String changeNameOfFile(String oldName) {
		return getNameOfFile(oldName) + "." + getCurrentDate() + "."
				+ getTypeOfFile(oldName);
	}

	private static String getNameOfFile(String oldName) {
		return oldName.substring(0, (oldName.length() - 4));
	}

	private static String getTypeOfFile(String oldName) {
		return oldName.substring((oldName.length() - 3));
	}

	private static String getCurrentDate() {
		Calendar cal = Calendar.getInstance();
		cal.setLenient(true);
		cal.setTime(new Date());
		cal.add(Calendar.DATE, 0);
		Date yesterday = cal.getTime();
		SimpleDateFormat currentDate = new SimpleDateFormat(
				"yyyy-MM-dd-hh-mm-ss");
		return currentDate.format(yesterday);
	}

}
