/*
 * ome.admin.validator.FileValidator
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.admin.validator;

// Java imports
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

// Third-party libraries
import org.apache.myfaces.custom.fileupload.UploadedFile;

/**
 * Validates imported file.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class FileValidator {

    /**
     * Checks directory exist
     * 
     * @param uploadPath -
     *            {@link java.io.File}
     * @return boolean
     * @throws IOException
     */
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

    /**
     * Validates name of the file. Return true if file is XLS or CSV
     * 
     * @param fileName -
     *            {@link java.lang.String}
     * @return boolean
     */
    public static boolean validFileName(String fileName) {
        if (fileName.length() > 4) {
            ArrayList<String> supportedSuffix = new ArrayList<String>();
            supportedSuffix.add("xls");
            // supportedSuffix.add("xml");
            supportedSuffix.add("csv");

            String suffix = getTypeOfFile(fileName);
            if (supportedSuffix.contains(suffix))
                return true;

        }
        return false;
    }

    /**
     * Validates mime type of file. Return true if file is
     * application/vnd.ms-excel or text/csv
     * 
     * @param uploadedFile
     *            {@link org.apache.myfaces.custom.fileupload.UploadedFile}
     * @return boolean
     */
    public static boolean validFileType(UploadedFile uploadedFile) {
        if (uploadedFile == null)
            throw new RuntimeException("Uploaded file does not exist.");
        if (uploadedFile.getContentType().equals("application/octet-stream")
                || uploadedFile.getContentType().equals("text/plain")
        // || uploadedFile.getContentType().equals("text/xml")
        ) {

            return validFileName(uploadedFile.getName());
        }
        return false;
    }

    /**
     * Changes name of the file.
     * 
     * @param oldName
     *            {@link java.lang.String}
     * @return {@link java.lang.String}
     */
    public static String changeNameOfFile(String oldName) {
        return getNameOfFile(oldName) + "." + getCurrentDate() + "."
                + getTypeOfFile(oldName);
    }

    /**
     * Gets name of the file
     * 
     * @param oldName
     *            {@link java.lang.String}
     * @return {@link java.lang.String}
     */
    private static String getNameOfFile(String oldName) {
        return oldName.substring(0, (oldName.length() - 4));
    }

    /**
     * Gets type of the file. Returns suffix.
     * 
     * @param oldName
     *            {@link java.lang.String}
     * @return {@link java.lang.String}
     */
    private static String getTypeOfFile(String oldName) {
        return oldName.substring((oldName.length() - 3));
    }

    /**
     * Gets current date.
     * 
     * @return {@link java.lang.String}
     */
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
