/*
 * org.openmicroscopy.shoola.util.file.ExcelWriter 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.file;



//Java imports
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.table.TableModel;


//Third-party libraries
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ExcelWriter
{	
	/** The table model to be output to excel. */
	TableModel 		model;
	
	/** The filename to write out to. */
	String 			filename;
	
	/** The name of the excel sheet. */
	String 			sheetname;
	
	/** The current row in the excel spreadsheet. */
	int 			currentRow;
	
	/** The cell style for numbers, format to 2 dec. places.*/
	HSSFCellStyle 	numberStyle;
	
	/**
	 * Create the excel writer for the table model. 
	 * @param filename The name of 
	 * @param sheetname
	 * @param model
	 */
	public ExcelWriter(String filename, String sheetname, TableModel model) 
	{
		this.filename = filename;
		this.sheetname = sheetname;
		this.model = model;
	}
	
	/**
	 * Write the tablemodel to the file.
	 * @throws IOException 
	 */
	public void write() throws IOException
	{
		FileOutputStream out = new FileOutputStream(filename);
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet();
		numberStyle = createNumberFormatStyle(wb);
			
		wb.setSheetName(0, sheetname);
		
		currentRow = 0;
		writeHeader(sheet);
		writeTableContents(sheet);

		wb.write(out);
		out.close();
	}
	
	/**
	 * Format the cell for Number values.
	 * @param wb
	 * @return
	 */
	private HSSFCellStyle createNumberFormatStyle(HSSFWorkbook wb)
	{
		HSSFCellStyle cellStyle = wb.createCellStyle();
		HSSFDataFormat df = wb.createDataFormat();
		cellStyle.setDataFormat(df.getFormat("#0.##"));
		return cellStyle;
	}

	/**
	 * Get the element at position row from the element. If the row is the first
	 * row and the element is a single object, then return the object, otherwise
	 * if the element if a list return the object at position row in the list. 
	 * @param element see above.
	 * @param row see above.
	 * @return see above.
	 */
	private Object getElement(Object element, int row)
	{
		if(element instanceof List)
		{
			List elementList = (List)element;
			if(row<elementList.size())
				return elementList.get(row);
		}
		else
			if(row == 0)
				return element;
		return "";
	}
	
	/**
	 * Is the element a number.
	 * @param element see above.
	 * @return see above.
	 */
	private boolean isNumber(Object element)
	{
		if (element instanceof Double ||  
				   element instanceof Integer ||
				   element instanceof Float ||
				   element instanceof Long)
			return true;
		return false;
	}
	
	/**
	 * Convert the element to a number.
	 * @param element see above.
	 * @return see above.
	 */
	private double toNumber(Object element)
	{
		if (element instanceof Double)
			return (Double)element;
		if (element instanceof Integer)
			return (Integer)element;
		if (element instanceof Float)
			return (Float)element;
		else if (element instanceof Long)
			return (Long)element;
		return 0;
	}
	
	/**
	 * Write the current row to the spreadsheet. 
	 * @param sheet see above.
	 * @param rowCount The current row in the tablemodel.
	 */
	private void writeRow(HSSFSheet sheet, int rowCount)
	{
		int maxRows = 1;
		for(int columnCount = 0 ; columnCount < model.getColumnCount(); columnCount++)
		{
			Object element = model.getValueAt(rowCount, columnCount);
			if(element instanceof List)
			{
				List elementList = (List)element;
				maxRows = Math.max(maxRows, elementList.size());
			}
		}
		int startRow = currentRow;
		
		for(int elementRowCount = 0; elementRowCount < maxRows; elementRowCount++)
		{
			HSSFRow row = sheet.createRow(startRow+elementRowCount);
			for(int columnCount = 0; columnCount < model.getColumnCount(); columnCount++)
			{
				HSSFCell cell = row.createCell(columnCount);
				Object element = getElement(model.getValueAt(rowCount, columnCount), elementRowCount);
				cell.setCellStyle(numberStyle);
				if(isNumber(element))
					cell.setCellValue(toNumber(element));
				else
					cell.setCellValue(element.toString());
			}
		}
		currentRow = currentRow+maxRows;
	}
	
	/**
	 * Write the table contents(data, not column header) to the spreadsheet.
	 * @param sheet see above.
	 */
	private void writeTableContents(HSSFSheet sheet)
	{
		for(int rowCount = 0 ; rowCount < model.getRowCount(); rowCount++)
			writeRow(sheet, rowCount);
	}
	
	/**
	 * Write the column headers to the spreadsheet.
	 * @param sheet see above.
	 */
	private void writeHeader(HSSFSheet sheet)
	{
		HSSFCell cell;
		HSSFRow	 row;
		row = sheet.createRow(currentRow);
		for (short cellnum = (short) 0; cellnum < model.getColumnCount(); cellnum++)
		{
			cell = row.createCell(cellnum);
		    cell.setCellValue(model.getColumnName(cellnum));
		}
		currentRow++;
	}
	
}


