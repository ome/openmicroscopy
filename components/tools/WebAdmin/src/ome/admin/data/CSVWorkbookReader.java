/*
 * ome.admin.data.CSVWorkbookReader
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.data;

// Java imports
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Third-party libraries
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

// Application-internal dependencies
import ome.admin.model.User;
import ome.conditions.ApiUsageException;
import ome.model.meta.Experimenter;

/**
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class CSVWorkbookReader {

    /**
     * log4j logger
     */
    private static Logger logger = Logger.getLogger(HSSFWorkbookReader.class
            .getName());

    /**
     * String filePath
     */
    private String filePath;

    /**
     * CSVReader reader
     */
    private CSVReader reader;

    /**
     * {@link ome.admin.data.ConnectionDB}
     */
    private ConnectionDB db;

    /**
     * Constructer
     * 
     * @param filePath -
     *            String
     * @throws FileNotFoundException
     */
    public CSVWorkbookReader(String filePath) throws FileNotFoundException {
        logger.info("CSVWorkbookReader opens a file: " + filePath);
        this.filePath = filePath;
        reader = new CSVReader(new FileReader(this.filePath));
        db = new ConnectionDB();
    }

    /**
     * Gets header of the table from CSV file.
     * 
     * @return String [] with element that are matched on the DB.
     * @throws IOException
     */
    public String[] getHeader() throws IOException {
        String[] header = reader.readNext();
        logger.info("HSSF Header of file: " + Arrays.toString(header));
        return header;
    }

    /**
     * Sets details for single experimenter.
     * 
     * @param header
     * @param value
     * @return
     */
    public User setDetails(String[] header, String[] value) {
        Experimenter exp = new Experimenter();
        for (int j = 0; j < header.length; j++) {
            if (header[j].equalsIgnoreCase("omename"))
                exp.setOmeName(value[j]);
            else if (header[j].equalsIgnoreCase("firstname"))
                exp.setFirstName(value[j]);
            else if (header[j].equalsIgnoreCase("middlename"))
                exp.setMiddleName(value[j]);
            else if (header[j].equalsIgnoreCase("lastname"))
                exp.setLastName(value[j]);
            else if (header[j].equalsIgnoreCase("email"))
                exp.setEmail(value[j]);
            else if (header[j].equalsIgnoreCase("institution"))
                exp.setInstitution(value[j]);
            else
                throw new ApiUsageException("CSV Wrong header set.");
        }

        User mexp = new User();
        mexp.setExperimenter(exp);
        // check existing experimenter
        if (db.checkExperimenter(exp.getOmeName())) {
            logger.info("CSV setDetails: Experimenter "
                    + mexp.getExperimenter().getOmeName() + " exist.");
            mexp.setSelectBooleanCheckboxValue(false);
        } else if (db.checkEmail(exp.getEmail())) {
            logger.info("CSV setDetails: Email "
                    + mexp.getExperimenter().getEmail() + " exist.");
            mexp.setSelectBooleanCheckboxValue(false);
        } else
            mexp.setSelectBooleanCheckboxValue(true);

        logger.info("CSV setDetails: Experimenter ["
                + mexp.getExperimenter().getOmeName() + ", "
                + mexp.getExperimenter().getFirstName() + ", "
                + mexp.getExperimenter().getMiddleName() + ", "
                + mexp.getExperimenter().getLastName() + ", "
                + mexp.getExperimenter().getEmail() + ", "
                + mexp.getExperimenter().getInstitution() + "]");
        return mexp;
    }

    /**
     * Imports experimenters.
     * 
     * @return
     * @throws IOException
     */
    public List<User> importingExperimenters() throws IOException {

        List<User> exps = new ArrayList<User>();
        String[] header = getHeader();
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null)
            exps.add(setDetails(header, nextLine));

        return exps;
    }

}
