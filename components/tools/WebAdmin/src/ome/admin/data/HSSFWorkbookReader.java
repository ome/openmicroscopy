/*
 * ome.admin.data.HSSFWorkbookReader
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.admin.data;

// Java imports
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

// Third-party libraries
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

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
public class HSSFWorkbookReader {

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
     * HSSFSheet sheet
     */
    private HSSFSheet sheet;

    /**
     * {@link ome.admin.data.ConnectionDB}
     */
    private ConnectionDB db;

    /**
     * Consatructer
     * 
     * @param filePath
     * @throws FileNotFoundException
     * @throws IOException
     */
    public HSSFWorkbookReader(String filePath) throws FileNotFoundException,
            IOException {
        logger.info("HSSFWorkbookReader opens a file: " + filePath);
        this.filePath = filePath;
        InputStream input = new BufferedInputStream(new FileInputStream(
                this.filePath));
        POIFSFileSystem fs = new POIFSFileSystem(input);
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        sheet = wb.getSheetAt(0);
        db = new ConnectionDB();
    }

    /**
     * Gets sheet.
     * 
     * @return
     */
    public HSSFSheet getSheet() {
        return sheet;
    }

    /**
     * Sets sheet.
     * 
     * @param sheet
     */
    public void setSheet(HSSFSheet sheet) {
        this.sheet = sheet;
    }

    /**
     * Gets header of the table from XLS file.
     * 
     * @return String [] with element that are matched on the DB.
     */
    public String[] getHeader() {
        String[] tHeader = null;

        HSSFRow header = (HSSFRow) getSheet().getRow(
                getSheet().getFirstRowNum());
        Iterator hcells = header.cellIterator();
        tHeader = new String[header.getPhysicalNumberOfCells()];
        int k = 0;
        while (hcells.hasNext()) {
            HSSFCell hcell = (HSSFCell) hcells.next();
            if (hcell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                tHeader[k++] = hcell.getRichStringCellValue().toString();
            }
        }
        logger.info("HSSF Header of file: " + Arrays.toString(tHeader));
        return tHeader;
    }

    /**
     * Removes header.
     */
    public void removeHeader() {
        HSSFRow header = (HSSFRow) getSheet().getRow(
                getSheet().getFirstRowNum());
        getSheet().removeRow(header);
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
                throw new ApiUsageException("HSSF Wrong header set.");
        }

        User mexp = new User();
        mexp.setExperimenter(exp);
        // check existing experimenter
        if (db.checkExperimenter(exp.getOmeName())) {
            logger.info("HSSF setDetails: Experimenter "
                    + mexp.getExperimenter().getOmeName() + " exist.");
            mexp.setSelectBooleanCheckboxValue(false);
        } else if (db.checkEmail(exp.getEmail())) {
            logger.info("HSSF setDetails: Email "
                    + mexp.getExperimenter().getEmail() + " exist.");
            mexp.setSelectBooleanCheckboxValue(false);
        } else
            mexp.setSelectBooleanCheckboxValue(true);

        logger.info("HSSF setDetails: Experimenter ["
                + mexp.getExperimenter().getOmeName() + ", "
                + mexp.getExperimenter().getFirstName() + ", "
                + mexp.getExperimenter().getMiddleName() + ", "
                + mexp.getExperimenter().getLastName() + ", "
                + mexp.getExperimenter().getEmail() + ", "
                + mexp.getExperimenter().getInstitution() + "]");
        return mexp;
    }

    /**
     * Gets cell value.
     * 
     * @param cell
     * @return
     * @throws IOException
     */
    public String getCellValue(HSSFCell cell) throws IOException {
        String str;
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
        case HSSFCell.CELL_TYPE_NUMERIC:
            str = String.valueOf(cell.getNumericCellValue());
            double dnum = Double.parseDouble(str);
            str = String.valueOf(dnum);
            break;
        case HSSFCell.CELL_TYPE_STRING:
            str = cell.getRichStringCellValue().getString();
            break;
        case HSSFCell.CELL_TYPE_BLANK:
            str = "";
            break;
        case HSSFCell.CELL_TYPE_BOOLEAN:
            str = String.valueOf(cell.getBooleanCellValue());
            break;
        case HSSFCell.CELL_TYPE_FORMULA:
            str = cell.getCellFormula();
            break;
        default:
            str = null;
            throw new IOException("IOException: Not a supported cell type");

        }
        return str;
    }

    /**
     * Creates a list of experimenters to present on the page.
     * 
     * @return List of users
     * @throws IOException
     */
    public List<User> importingExperimenters() throws IOException {
        String[] header = getHeader();
        removeHeader();

        // Iterate over each row in the sheet
        Iterator rows = getSheet().rowIterator();

        List<User> expList = new ArrayList<User>();

        while (rows.hasNext()) {
            HSSFRow row = (HSSFRow) rows.next();

            String[] tExp = new String[row.getLastCellNum()];

            // Iterate over each cell in the row and print out the cell's
            // content
            int k = 0;
            for (int j = 0; j < row.getLastCellNum(); j++) {
                HSSFCell cell = (HSSFCell) row.getCell(Short.parseShort(String
                        .valueOf(j)));
                tExp[k++] = getCellValue(cell);
            }
            expList.add(setDetails(header, tExp));
        }
        return expList;
    }

}
