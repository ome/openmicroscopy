/*
 * org.openmicroscopy.shoola.util.file.MSWriter 
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.TableModel;

//Third-party libraries

//Application-internal dependencies
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

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
	/** The name of the excel spreadsheet to write to. */
	String 							filename;

	/** The output stream to the Excel spreadsheet. */
	FileOutputStream 				out;
	
	/** The current workbook of the spreadsheet. */
	HSSFWorkbook					workbook;

	/** The cell style for numbers, format to 2 dec. places.*/
	HSSFCellStyle 					numberStyle;

	/** Map of the sheetname vs sheet. */
	HashMap<String, SheetInfo>		sheetMap;
	
	/** The number of sheets in the workbook. */
	int 							numSheets;
	
	/** The current sheet being worked on. */
	SheetInfo						currentSheet;
	
	/**
	 * Instatiate the MSWriter with filename 
	 * @param filename see above.
	 */
	public ExcelWriter(String filename)
	{
		this.filename = filename;
	}
	
	/**
	 * Open the filename for writing and create workbook.
	 * @throws FileNotFoundException
	 */
	public void openFile() throws FileNotFoundException
	{
		out = new FileOutputStream(filename);
		sheetMap = new HashMap<String, SheetInfo>();
		numSheets = 0;
		currentSheet = null;
	}
	
	/**
	 * Write the workbook to excel and close file.
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		this.workbook.write(out);
		out.close();
	}
	
	/**
	 * Create the workbook and setup all the styles associated with it.
	 */
	public void createWorkbook()
	{
		workbook = new HSSFWorkbook();
		setStyles();
	}
	

	/**
	 * Format the cell for Number values.
	 * @param wb
	 * @return see above.
	 */
	private HSSFCellStyle createNumberFormatStyle(HSSFWorkbook wb)
	{
		HSSFCellStyle cellStyle = wb.createCellStyle();
		HSSFDataFormat df = wb.createDataFormat();
		cellStyle.setDataFormat(df.getFormat("#0.##"));
		return cellStyle;
	}
	
	/**
	 * Set the different styles used in the output of the excel.
	 * 
	 */
	private void setStyles()
	{
		numberStyle = createNumberFormatStyle(workbook);
	}
	
	/**
	 * Create a new sheet with name sheet and add it to the map. 
	 * @param name see above.
	 */
	public void createSheet(String name)
	{
		HSSFSheet sheet = workbook.createSheet();
		currentSheet = new SheetInfo(name, numSheets, sheet);
		sheetMap.put(name, currentSheet);
		workbook.setSheetName((numSheets), name);
		numSheets++;
	}
	
	/**
	 * Set the current sheet to the sheet with name sheet name in the map.
	 * @param sheetname see above.
	 * @return see above.
	 */
	public SheetInfo setCurrentSheet(String sheetname)
	{
		if(sheetMap.containsKey(sheetname))
		{
			currentSheet = sheetMap.get(sheetname);
			return currentSheet;
		}
		return null;
	}
	
	/**
	 * Set the current sheet to the sheet with name sheet name in the map.
	 * @param sheetname see above.
	 * @return see above.
	 */
	public SheetInfo setCurrentSheet(int sheetIndex)
	{
		Iterator<SheetInfo> sheetIterator = sheetMap.values().iterator();
		while (sheetIterator.hasNext())
		{
			SheetInfo sheetInfo=(SheetInfo) sheetIterator.next();
			if(sheetInfo.getIndex()==sheetIndex)
			{
				currentSheet = sheetInfo;
				return sheetInfo;
			}
		}
		return null;
	}

	
	/**
	 * Get the cell row and column from the current sheet.
	 * @param row see above.
	 * @param column see above.
	 * @return see above.
	 */
	public HSSFCell getCell(int row , int column)
	{
		if(currentSheet==null)
			return null;
		return currentSheet.getCell(row, column);
	}
	
	/**
	 * Change the name of the sheet from old name to newname.
	 * @param oldname see above.
	 * @param newname see above.
	 */
	public void changeSheetName(String oldname, String newname)
	{
		SheetInfo sheetInfo = setCurrentSheet(oldname);
		int index = sheetInfo.getIndex();
		workbook.setSheetName(index, newname);
	}
	
	/**
	 * Write the table to the spread sheet, starting the headings at rowIndex, 
	 * and columnIndex.
	 * @param rowIndex see above.
	 * @param columnIndex see above.
	 * @param tableModel see above.
	 * return the current row in the being written to. 
	 */
	public int writeTableToSheet(int rowIndex, int columnIndex, TableModel tableModel)
	{
		int startColumn = columnIndex;
		currentSheet.setCurrentRow(rowIndex);
		writeHeader(startColumn, tableModel);
		writeTableContents(startColumn, tableModel);
		return currentSheet.currentRow;
	}
	
	/**
	 * Write a single object to rowIndex, columnIndex.
	 * @param rowIndex see above.
	 * @param columnIndex see above.
	 * @param value see above.
	 */
	public int writeElement(int rowIndex, int columnIndex, Object value)
	{
		HSSFCell cell = currentSheet.getCell(rowIndex, columnIndex);
		cell.setCellValue(new HSSFRichTextString(value.toString()));
		return currentSheet.currentRow;
	}
	
	/**
	 * Write an array to a row starting at columnIndex.
	 * @param rowIndex The row to write to.
	 * @param columnIndex see above.
	 * @param values see above.
	 */
	public int writeArrayToRow(int rowIndex, int columnIndex, Object[] values)
	{
		HSSFCell cell; 
		for( int i = 0 ; i < values.length; i++)
		{
			cell = currentSheet.getCell(rowIndex, columnIndex+i);
			cell.setCellValue(new HSSFRichTextString(values[i].toString()));
		}
		return currentSheet.currentRow;
	}
	
	/**
	 * Write an array to a column starting at rowIndex.
	 * @param rowIndex see above.
	 * @param columnIndex see above.
	 * @param values see above.
	 */
	public int writeArrayToColumn(int rowIndex, int columnIndex, Object[] values)
	{
		HSSFCell cell; 
		for( int i = 0 ; i < values.length; i++)
		{
			cell = currentSheet.getCell(rowIndex+i, columnIndex);
			cell.setCellValue(new HSSFRichTextString(values[i].toString()));
		}
		return currentSheet.currentRow;
	}
	
	/**
	 * Write the column headers to the spreadsheet.
	 * @param startColumn see above.
	 * @param tableModel see above.
	 */
	private void writeHeader(int startColumn, TableModel tableModel)
	{
		HSSFCell cell;
		
		for (short cellnum = (short) startColumn; cellnum < tableModel.getColumnCount(); cellnum++)
		{
			cell = currentSheet.getCell(currentSheet.getCurrentRow(), cellnum);
		    cell.setCellValue(tableModel.getColumnName(cellnum));
		}
		currentSheet.setCurrentRow(currentSheet.getCurrentRow()+1);
	}
		
	/**
	 * Write the table contents(data, not column header) to the spreadsheet.
	 * @param startColumn the starting column to write the table to.
	 * @param tableModel see above.
	 */
	private void writeTableContents(int startColumn, TableModel tableModel)
	{
		for(int rowCount = 0 ; rowCount < tableModel.getRowCount(); rowCount++)
			writeRow(startColumn, rowCount, tableModel);
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
	 * @param startColumn see above.
	 * @param rowCount The current row in the tablemodel.
	 * @param tableModel the tablemodel to write.
	 */
	private void writeRow(int startColumn, int rowCount, TableModel tableModel)
	{
		int maxRows = 1;
		for(int columnCount = 0 ; columnCount < tableModel.getColumnCount(); columnCount++)
		{
			Object element = tableModel.getValueAt(rowCount, columnCount);
			if(element instanceof List)
			{
				List elementList = (List)element;
				maxRows = Math.max(maxRows, elementList.size());
			}
		}
		
		for(int elementRowCount = 0; elementRowCount < maxRows; elementRowCount++)
		{
			for(int columnCount = 0; columnCount < tableModel.getColumnCount(); columnCount++)
			{
				HSSFCell cell = currentSheet.getCell(currentSheet.getCurrentRow()+elementRowCount, startColumn + columnCount);
				Object element = getElement(tableModel.getValueAt(rowCount, columnCount), elementRowCount);
				cell.setCellStyle(numberStyle);
				if(isNumber(element))
					cell.setCellValue(toNumber(element));
				else
					cell.setCellValue(element.toString());
			}
		}
		currentSheet.setCurrentRow(currentSheet.getCurrentRow()+maxRows);
	}
	
}


