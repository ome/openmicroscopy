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
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableModel;

//Third-party libraries
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.image.io.EncoderException;
import org.openmicroscopy.shoola.util.image.io.WriterImage;


/** 
 * Writes to excel.
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
	
	/** The default name of a sheet. */
	private static final String		DEFAULT_NAME = "Sheet";
	
	/** The name of the excel spreadsheet to write to. */
	private String 					filename;

	/** The output stream to the Excel spreadsheet. */
	private FileOutputStream 		out;
	
	/** The current workbook of the spreadsheet. */
	private HSSFWorkbook			workbook;

	/** The cell style for numbers, format to 2 dec. places.*/
	private HSSFCellStyle 			numberStyle;

	/** Map of the sheet names vs sheetInfos. */
	private Map<String, SheetInfo>	sheetMap;
	
	/** Map of the sheet names vs sheetInfos. */
	private Map<String, Integer> 	imageMap;
	
	/** The number of sheets in the workbook. */
	private int 					numSheets;
	
	/** The current sheet being worked on. */
	private SheetInfo				currentSheet;
	
	/**
	 * Formats the cell for Number values.
	 * 
	 * @param wb The wrokbook to format.
	 * @return See above.
	 */
	private HSSFCellStyle createNumberFormatStyle(HSSFWorkbook wb)
	{
		if (wb == null) 
			throw new IllegalArgumentException("No workbook to format.");
		HSSFCellStyle cellStyle = wb.createCellStyle();
		HSSFDataFormat df = wb.createDataFormat();
		cellStyle.setDataFormat(df.getFormat("#0.##"));
		return cellStyle;
	}
	
	/** Sets the different styles used in the output of the excel. */
	private void setStyles()
	{
		numberStyle = createNumberFormatStyle(workbook);
	}
	
	/**
	 * Writes the column headers to the spreadsheet.
	 * 
	 * @param startColumn see above.
	 * @param tableModel see above.
	 */
	private void writeHeader(int startColumn, TableModel tableModel)
	{
		HSSFCell cell;
		
		for (short cellnum = (short) startColumn; 
			cellnum < tableModel.getColumnCount(); cellnum++)
		{
			cell = currentSheet.getCell(currentSheet.getCurrentRow(), cellnum);
		    cell.setCellValue(new HSSFRichTextString(
		    		tableModel.getColumnName(cellnum)));
		}
		currentSheet.setCurrentRow(currentSheet.getCurrentRow()+1);
	}
		
	/**
	 * Writes the table contents(data, not column header) to the spreadsheet.
	 * 
	 * @param startColumn the starting column to write the table to.
	 * @param tableModel see above.
	 */
	private void writeTableContents(int startColumn, TableModel tableModel)
	{
		for (int rowCount = 0 ; rowCount < tableModel.getRowCount(); rowCount++)
			writeRow(startColumn, rowCount, tableModel);
	}

	/**
	 * Returns the element at position row from the element. 
	 * If the row is the first row and the element is a single object, 
	 * then returns the object, otherwise if the element if a list 
	 * returns the object at position row in the list. 
	 * 
	 * @param element see above.
	 * @param row see above.
	 * @return see above.
	 */
	private Object getElement(Object element, int row)
	{
		if (element instanceof List)
		{
			List elementList = (List)element;
			if (row<elementList.size())
				return elementList.get(row);
		}
		else
			if (row == 0)
				return element;
		return "";
	}
	
	/**
	 * Returns <code>true</code> if the element is a number, <code>false</code>
	 * otherwise.
	 * 
	 * @param element The element to handle.
	 * @return See above.
	 */
	private boolean isNumber(Object element)
	{
		return (element instanceof Double || element instanceof Integer ||
			element instanceof Float || element instanceof Long);
	}
	
	/**
	 * Converts the element to a number.
	 * 
	 * @param element The element to handle.
	 * @return See above.
	 */
	private double toNumber(Object element)
	{
		if (element instanceof Double)
			return (Double) element;
		if (element instanceof Integer)
			return (Integer) element;
		if (element instanceof Float)
			return (Float) element;
		else if (element instanceof Long)
			return (Long) element;
		return 0;
	}
	
	/**
	 * Writes the current row to the spreadsheet. 
	 * 
	 * @param startColumn see above.
	 * @param rowCount The current row in the tablemodel.
	 * @param tableModel the tablemodel to write.
	 */
	private void writeRow(int startColumn, int rowCount, TableModel tableModel)
	{
		int maxRows = 1;
		Object element;
		List elementList;
		HSSFCell cell;
		for (int columnCount = 0 ; columnCount < tableModel.getColumnCount(); 
				columnCount++)
		{
			element = tableModel.getValueAt(rowCount, columnCount);
			if (element instanceof List)
			{
				elementList = (List) element;
				maxRows = Math.max(maxRows, elementList.size());
			}
		}
		
		for (int elementRowCount = 0; elementRowCount < maxRows; 
			elementRowCount++)
		{
			for (int columnCount = 0; columnCount < tableModel.getColumnCount(); 
				columnCount++)
			{
				cell = currentSheet.getCell(currentSheet.getCurrentRow()+
						elementRowCount, startColumn + columnCount);
				element = getElement(tableModel.getValueAt(rowCount, 
						columnCount), elementRowCount);
				cell.setCellStyle(numberStyle);
				if (isNumber(element))
					cell.setCellValue(toNumber(element));
				else
					cell.setCellValue(new HSSFRichTextString(
							element.toString()));
			}
		}
		currentSheet.setCurrentRow(currentSheet.getCurrentRow()+maxRows);
	}

	/** Creates the workbook and sets up all the styles associated with it. */
	private void createWorkbook()
	{
		workbook = new HSSFWorkbook();
		setStyles();
	}
	
	/**
	 * Creates a new instance. 
	 * 
	 * @param filename The name of the file. Mustn't be <code>null</code>.
	 */
	public ExcelWriter(String filename)
	{
		if (filename == null) throw new IllegalArgumentException("No name."); 
		this.filename = filename;
		sheetMap = new HashMap<String, SheetInfo>();
		imageMap = new HashMap<String, Integer>();
		numSheets = 0;
		currentSheet = null;
	}
	
	/**
	 * Opens the filename for writing and creates workbook.
	 * @throws FileNotFoundException 	Thrown if the output stream cannot 
	 * 									be created.
	 */
	public void openFile() 
		throws FileNotFoundException
	{
		out = new FileOutputStream(filename);
		createWorkbook();
	}
	
	/**
	 * Writes the workbook to excel and closes file.
	 * @throws IOException Thrown if an error occurs while writing the output.
	 */
	public void close() 
		throws IOException
	{
		if (out == null || workbook == null) return;
		workbook.write(out);
		out.close();
	}
	
	/**
	 * Creates a new sheet with name sheet and adds it to the map. 
	 * 
	 * @param name The name of the sheet
	 */
	public void createSheet(String name)
	{
		if (name == null || name.trim().length() == 0)
			name = DEFAULT_NAME+numSheets;
		HSSFSheet sheet = workbook.createSheet();
		currentSheet = new SheetInfo(name, numSheets, sheet);
		sheetMap.put(name, currentSheet);
		workbook.setSheetName((numSheets), name);
		numSheets++;
	}
	
	/**
	 * Sets the current sheet to the sheet with name sheet name in the map.
	 * 
	 * @param sheetname The name of the sheet.
	 * @return See above.
	 */
	public SheetInfo setCurrentSheet(String sheetname)
	{
		if (sheetMap.containsKey(sheetname))
		{
			currentSheet = sheetMap.get(sheetname);
			return currentSheet;
		}
		return null;
	}
	
	/**
	 * Sets the current sheet to the sheet with name sheet name in the map.
	 * 
	 * @param sheetIndex The index of the sheet.
	 * @return See above.
	 */
	public SheetInfo setCurrentSheet(int sheetIndex)
	{
		Iterator<SheetInfo> sheetIterator = sheetMap.values().iterator();
		SheetInfo sheetInfo;
		while (sheetIterator.hasNext())
		{
			sheetInfo = sheetIterator.next();
			if (sheetInfo.getIndex() == sheetIndex)
			{
				currentSheet = sheetInfo;
				return sheetInfo;
			}
		}
		return null;
	}

	
	
	/**
	 * Adds a new image image to the workbook, with imageName. 
	 * Returns the index corresponding to the image name.
	 * 
	 * @param imageName The name of the image.
	 * @param image 	The image to add to the workbook
	 * @return index of new image.
	 * @throws EncoderException Exception thrown if an error occured during the
     * encoding process.
	 */
	public int addImageToWorkbook(String imageName, BufferedImage image) 
		throws EncoderException
	{
		byte[] array = WriterImage.imageToByteStreamAsJPEG(image);
		if (array == null) return -1;
		int index = workbook.addPicture(array, HSSFWorkbook.PICTURE_TYPE_JPEG);
		imageMap.put(imageName, index);
		return index;
	}
	
	/**
	 * Writes the image with name imageName to the work sheet, at the 
	 * specified location 
	 * (rowStart, colStart)->(rowEnd, colEnd)
	 * 
	 * @param rowStartIndex The index of the start row.
	 * @param colStartIndex The index of the start column.
	 * @param width 		The width of the image.
	 * @param height 		The height of the image.
	 * @param imageName 	The name of the image.
	 */
	public void writeImage(int rowStartIndex, int colStartIndex, 
							int width, int height, String imageName)
	{
		HSSFPatriarch patriarch = currentSheet.getDrawingPatriarch();
		HSSFClientAnchor anchor;
		double remainderWidth =  (width%68)/68.0;
		int widthInCells = width/68;
		double remainderHeight = (height%18)/18.0;
		int heightInCells = (height/18);
		anchor = new HSSFClientAnchor(0, 0, (int) (remainderWidth*1023),
				 (int) (remainderHeight*255),
				 (short) colStartIndex,
				 (short) rowStartIndex,
				 (short) (colStartIndex+widthInCells), 
				 (short) (rowStartIndex+heightInCells));
		
		anchor.setAnchorType(3);
		int index = imageMap.get(imageName);
		patriarch.createPicture(anchor, index);
	}
	
	/**
	 * Returns the cell corresponding to the passed row and column 
	 * from the current sheet.
	 * 
	 * @param row 		The selected row.
	 * @param column 	The selected column.
	 * @return Ssee above.
	 */
	public HSSFCell getCell(int row , int column)
	{
		if (currentSheet == null) return null;
		return currentSheet.getCell(row, column);
	}
	
	/**
	 * Modifies the name of the sheet from old name to new name.
	 * 
	 * @param oldName The previous name of the sheet.
	 * @param newName The new name of the sheet.
	 */
	public void changeSheetName(String oldName, String newName)
	{
		if (oldName == null || newName == null) return;
		SheetInfo sheetInfo = setCurrentSheet(oldName);
		int index = sheetInfo.getIndex();
		workbook.setSheetName(index, newName);
		sheetInfo.setName(newName);
	}
	
	/**
	 * Writes the table to the spread sheet, starting the headings at rowIndex, 
	 * and columnIndex. Returns the next free row after the table.
	 * 
	 * @param rowIndex 		The index of the row.
	 * @param columnIndex 	The index of the column.
	 * @param tableModel 	The table model to write.
	 * @return See above.
	 */
	public int writeTableToSheet(int rowIndex, int columnIndex, 
					TableModel tableModel)
	{
		if (rowIndex < 0 || columnIndex < 0) 
			throw new IllegalArgumentException("Index not valid.");
		if (tableModel == null)
			throw new IllegalArgumentException("No table to write.");
		int startColumn = columnIndex;
		currentSheet.setCurrentRow(rowIndex);
		writeHeader(startColumn, tableModel);
		writeTableContents(startColumn, tableModel);
		return currentSheet.getCurrentRow();
	}
	
	/**
	 * Writes a map to the spreadsheet starting at rowIndex and columnIndex.
	 * Returns the next free row after the map.
	 * 
	 * @param rowIndex 		The index of the row.
	 * @param columnIndex 	The index of the column.
	 * @param map 			The map to write.
	 * @return See above.
	 */
	public int writeMapToSheet(int rowIndex, int columnIndex, Map map)
	{
		if (rowIndex < 0 || columnIndex < 0) 
			throw new IllegalArgumentException("Index not valid.");
		if (map == null)
			throw new IllegalArgumentException("No map to write.");
		Iterator it = map.keySet().iterator();
		Object key, value;
		while (it.hasNext())
		{
			key = it.next();
			value = map.get(key);
			writeElement(rowIndex, columnIndex, key.toString());
			writeElement(rowIndex, columnIndex+1, value.toString());
			rowIndex++;
		}
		currentSheet.setCurrentRow(rowIndex);
		return rowIndex;
	}
	
	/**
	 * Writes a single object to rowIndex, columnIndex.
	 * Returns the next free row after the map.
	 * 
	 * @param rowIndex 		The index of the row.
	 * @param columnIndex 	The index of the column.
	 * @param value 		The object to write.
	 * @return See above.
	 */
	public int writeElement(int rowIndex, int columnIndex, Object value)
	{
		if (rowIndex < 0 || columnIndex < 0) 
			throw new IllegalArgumentException("Index not valid.");
		if (value == null)
			throw new IllegalArgumentException("No object to write.");
		HSSFCell cell = currentSheet.getCell(rowIndex, columnIndex);
		cell.setCellValue(new HSSFRichTextString(value.toString()));
		currentSheet.setCurrentRow(rowIndex+1);
		return currentSheet.getCurrentRow();
	}
	
	/**
	 * Writes an array to a row starting at columnIndex.
	 * Returns the next free row after the map.
	 * 
	 * @param rowIndex 		The index of the row.
	 * @param columnIndex 	The index of the column.
	 * @param values 		The objects to write.
	 * @return See above.
	 */
	public int writeArrayToRow(int rowIndex, int columnIndex, Object[] values)
	{
		if (rowIndex < 0 || columnIndex < 0) 
			throw new IllegalArgumentException("Index not valid.");
		if (values == null)
			throw new IllegalArgumentException("No object to write.");
		HSSFCell cell; 
		currentSheet.setCurrentRow(rowIndex+1);
		for (int i = 0 ; i < values.length; i++)
		{
			cell = currentSheet.getCell(rowIndex, columnIndex+i);
			cell.setCellValue(new HSSFRichTextString(values[i].toString()));
		}
		return currentSheet.getCurrentRow();
	}
	
	/**
	 * Writes an array to a column starting at rowIndex.
	 * 
	 * Returns the next free row after the map.
	 * 
	 * @param rowIndex 		The index of the row.
	 * @param columnIndex 	The index of the column.
	 * @param values 		The objects to write.
	 * @return See above.
	 */
	public int writeArrayToColumn(int rowIndex, int columnIndex, 
								Object[] values)
	{
		if (rowIndex < 0 || columnIndex < 0) 
			throw new IllegalArgumentException("Index not valid.");
		if (values == null)
			throw new IllegalArgumentException("No object to write.");
		HSSFCell cell; 
		for (int i = 0 ; i < values.length; i++)
		{
			cell = currentSheet.getCell(rowIndex+i, columnIndex);
			cell.setCellValue(new HSSFRichTextString(values[i].toString()));
		}
		currentSheet.setCurrentRow(rowIndex+values.length);
		return currentSheet.getCurrentRow();
	}
	
}


