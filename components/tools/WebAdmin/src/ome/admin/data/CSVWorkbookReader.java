package ome.admin.data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ome.admin.model.MyExperimenter;
import ome.conditions.ApiUsageException;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;

public class CSVWorkbookReader {

	/**
	 * log4j logger
	 */
	private static Logger logger = Logger.getLogger(HSSFWorkbookReader.class
			.getName());

	private String filePath;

	private CSVReader reader;

	private ConnectionDB db;

	public CSVWorkbookReader(String filePath) throws FileNotFoundException {
		logger.info("CSVWorkbookReader opens a file: " + filePath);
		this.filePath = filePath;
		reader = new CSVReader(new FileReader(this.filePath));
		db = new ConnectionDB();
	}

	public String[] getHeader() throws IOException {
		String[] header = reader.readNext();
		logger.info("HSSF Header of file: " + Arrays.toString(header));
		return header;
	}

	public MyExperimenter setDetails(String[] header, String[] value) {
		MyExperimenter exp = new MyExperimenter();
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

		// check existing experimenter
		if (db.checkExperimenter(exp.getOmeName())) {
			logger.info("CSV setDetails: Experimenter " + exp.getOmeName()
					+ " exist.");
			exp.setSelectBooleanCheckboxValue(false);
		} else if (db.checkEmail(exp.getEmail())) {
			logger.info("CSV setDetails: Email " + exp.getEmail() + " exist.");
			exp.setSelectBooleanCheckboxValue(false);
		} else
			exp.setSelectBooleanCheckboxValue(true);

		logger.info("CSV setDetails: Experimenter [" + exp.getOmeName() + ", "
				+ exp.getFirstName() + ", " + exp.getMiddleName() + ", "
				+ exp.getLastName() + ", " + exp.getEmail() + ", "
				+ exp.getInstitution() + "]");
		return exp;
	}

	public List<MyExperimenter> importingExperimenters() throws IOException {

		List<MyExperimenter> exps = new ArrayList<MyExperimenter>();
		String[] header = getHeader();
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null)
			exps.add(setDetails(header, nextLine));

		return exps;
	}

}
