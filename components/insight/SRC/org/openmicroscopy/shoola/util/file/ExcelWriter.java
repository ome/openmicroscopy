/*
 * org.openmicroscopy.shoola.util.file.MSWriter 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.table.TableModel;



//Third-party libraries
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.image.io.EncoderException;
import org.openmicroscopy.shoola.util.image.io.WriterImage;


/** 
 * Writes to Excel.
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
	
	/** Hyper-link fonts. */
	public final  static String HYPERLINK = "hyperlink";
	
	/** default font. */
	public final static String DEFAULT = "default";

	/** italic font. */
	public final static String ITALIC_DEFAULT = "italic_default";
	
	/** bold font. */
	public final static String BOLD_DEFAULT = "bold_default";
	
	/** underline font. */
	public final static String UNDERLINE_DEFAULT = "underline_default";
	
	/** bold underline font. */
	public final static String BOLD_UNDERLINE_DEFAULT = 
									"bold_underline_default";
	
	/** bold italic font. */
	public final static String BOLD_ITALIC_DEFAULT = "bold_italic_default";
	
	/** bold italic underline font. */
	public final static String BOLD_ITALIC_UNDERLINE_DEFAULT = 
										"bold_italic_underline_default";
	
	/** italic underline font. */
	public final static String ITALIC_UNDERLINE_DEFAULT = 
									"italic_underline_default";

	/** plain 12 point font. */
	public final static String PLAIN_12 = "plain_12";

	/** plain 14 point font. */
	public final static String PLAIN_14 = "plain_14";
	
	/** bold 14 point font. */
	public final static String BOLD_14 = "bold_14";
	
	/** plain, 18 point font. */
	public final static String PLAIN_18 = "plain_18";
	
	/** bold, 18 point font. */
	public final static String BOLD_18 = "bold_18";
	
	/** two decimal point format. */
	public final static String TWODECIMALPOINTS = "2 decimal points";
	
	/** integer number format. */
	public final static String INTEGER = "integer";
	
	/** Cell Border Underline. */
	public final static String CELLBORDER_UNDERLINE = "cellborder_underline";
	
	/** Cell Border Topline. */
	public final static String CELLBORDER_TOPLINE = "cellborder_topline";

	/** Cell Border Underline and top line. */
	public final static String CELLBORDER_UNDERLINE_TOPLINE =
												"cellborder_underline_topline";

	/** The default name of a sheet. */
	private static final String		DEFAULT_NAME = "Sheet";
	
	/** The name of the excel spreadsheet to write to. */
	private String 					filename;

	/** The output stream to the Excel spreadsheet. */
	private FileOutputStream 		out;
	
	/** The current workbook of the spreadsheet. */
	private HSSFWorkbook			workbook;

	/** The map of all the styles created in the workbook. */
	private Map<String, HSSFCellStyle> styleMap;

	/** The map of all fonts used by the cell styles. */
	private Map<String, HSSFFont> fontMap;
	
	/** Map of the sheet names vs sheetInfos. */
	private Map<String, SheetInfo>	sheetMap;
	
	/** Map of the sheet names vs sheetInfos. */
	private Map<String, Integer> 	imageMap;
	
	/** The number of sheets in the workbook. */
	private int 					numSheets;
	
	/** The current sheet being worked on. */
	private SheetInfo				currentSheet;

	/** Sets the different styles used in the output of the excel. */
	private void setStyles()
	{
		styleMap = new HashMap<String, HSSFCellStyle>();
		fontMap = new HashMap<String, HSSFFont>();
		createFonts();
		createStyles();
	}
	
	/** Creates the default styles. */
	private void createStyles()
	{
		HSSFCellStyle style;
				
		Iterator<String> fontIterator = fontMap.keySet().iterator();
		String fontName;
		while (fontIterator.hasNext())
		{
			fontName = fontIterator.next();
			style = workbook.createCellStyle();
			style.setFont(fontMap.get(fontName));
			styleMap.put(fontName, style);
		}
		HSSFDataFormat df;
		style = workbook.createCellStyle();
		style.setFont(fontMap.get(DEFAULT));
		df = workbook.createDataFormat();
		style.setDataFormat(df.getFormat("#.##"));
		styleMap.put(TWODECIMALPOINTS, style);

		style = workbook.createCellStyle();
		style.setFont(fontMap.get(DEFAULT));
		df = workbook.createDataFormat();
		style.setDataFormat(df.getFormat("0"));
		styleMap.put(INTEGER, style);
		
		
		style = workbook.createCellStyle();
		style.setFont(fontMap.get(DEFAULT));
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBottomBorderColor(HSSFColor.BLACK.index);
		styleMap.put(CELLBORDER_UNDERLINE, style);

		style = workbook.createCellStyle();
		style.setFont(fontMap.get(DEFAULT));
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setTopBorderColor(HSSFColor.BLACK.index);
		styleMap.put(CELLBORDER_TOPLINE, style);

		style = workbook.createCellStyle();
		style.setFont(fontMap.get(DEFAULT));
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setTopBorderColor(HSSFColor.BLACK.index);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBottomBorderColor(HSSFColor.BLACK.index);
		styleMap.put(CELLBORDER_UNDERLINE_TOPLINE, style);
	}

	/** Creates the fonts that are going to be used in the styles. */
	private void createFonts()
	{
		HSSFFont font;
		/* Hyperlink font. */
		font = workbook.createFont();
		font.setUnderline(HSSFFont.U_SINGLE);
		font.setColor(HSSFColor.BLUE.index);
    	fontMap.put(HYPERLINK, font);
    	
    	/* Default Font. */
    	font = workbook.createFont();
    	fontMap.put(DEFAULT, font);
    	
    	/* Bold Font. */
    	font = workbook.createFont();
    	font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    	fontMap.put(BOLD_DEFAULT, font);
    	
    	/* Underline Font. */
    	font = workbook.createFont();
    	font.setUnderline(HSSFFont.U_SINGLE);
    	fontMap.put(UNDERLINE_DEFAULT, font);
    	
    	/* Italic Font. */
    	font = workbook.createFont();
    	font.setItalic(true);
    	fontMap.put(ITALIC_DEFAULT, font);
    	
    	/* Italic, underline Font. */
    	font = workbook.createFont();
    	font.setItalic(true);
    	font.setUnderline(HSSFFont.U_SINGLE);
    	fontMap.put(ITALIC_UNDERLINE_DEFAULT, font);

    	/* Italic, bold Font. */
    	font = workbook.createFont();
    	font.setItalic(true);
    	font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    	fontMap.put(BOLD_ITALIC_DEFAULT, font);

    	/* Italic, bold, underline Font. */
    	font = workbook.createFont();
    	font.setItalic(true);
    	font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    	font.setUnderline(HSSFFont.U_SINGLE);
    	fontMap.put(BOLD_ITALIC_UNDERLINE_DEFAULT, font);

    	/* Italic, bold, underline Font. */
    	font = workbook.createFont();
    	font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    	font.setUnderline(HSSFFont.U_SINGLE);
    	fontMap.put(BOLD_UNDERLINE_DEFAULT, font);
    	
    	/* 12 point font. */
    	font = workbook.createFont();
    	font.setFontHeightInPoints((short) 12);
    	fontMap.put(PLAIN_12, font);

    	/* 14 point font. */
    	font = workbook.createFont();
    	font.setFontHeightInPoints((short) 14);
    	fontMap.put(PLAIN_14, font);

    	/* 14 point font. */
    	font = workbook.createFont();
    	font.setFontHeightInPoints((short) 14);
    	font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    	fontMap.put(BOLD_14, font);
    	
    	/* 18 point font. */
    	font = workbook.createFont();
    	font.setFontHeightInPoints((short) 18);
    	fontMap.put(PLAIN_18, font);

    	/* 18 point font. */
    	font = workbook.createFont();
    	font.setFontHeightInPoints((short) 18);
    	font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    	fontMap.put(BOLD_18, font);
	}
	
	/**
	 * Returns the style with name style.
	 * 
	 * @param style See above.
	 * @return See above.
	 */
	private HSSFCellStyle getCellStyle(String style)
	{
		if (!styleMap.containsKey(style))
			throw new IllegalArgumentException("No such style");
		return styleMap.get(style);
	}
	
	/**
	 * Sets the cell Style for cell.
	 * 
	 * @param cell see above.
	 * @param style see above.
	 */
	private void setCellStyle(HSSFCell cell, String style)
	{
		HSSFCellStyle cellStyle = getCellStyle(style);
		cell.setCellStyle(cellStyle);
	}
	
	/**
	 * Autosize column, columnIndex to fit contents. 
	 * @param columnIndex see above.
	 */
	public void sizeColumnToFit(int columnIndex)
	{
		currentSheet.sizeColumnToFit(columnIndex);
	}
	
	/**
	 * Autosize all columns to fit contents. 
	 */
	public void sizeAllColumnsToFit()
	{
		currentSheet.sizeAllColumnsToFit();
	}
	
	/**
	 * Set the height of the rows [rowStart, rowEnd] to rowHeight in px
	 * @param rowStart see above.
	 * @param rowEnd see above.
	 * @param rowHeight see above.
	 */
	public void setRowHeight(int rowStart, int rowEnd, int rowHeight)
	{
		currentSheet.setRowHeight(rowStart, rowEnd, rowHeight);
	}
	
	/**
	 * Set the height of the row rowIndex to rowHeight in pixels.
	 * 
	 * @param rowIndex see above.
	 * @param rowHeight see above.
	 */
	public void setRowHeight(int rowIndex, int rowHeight)
	{
		currentSheet.setRowHeight(rowIndex, rowHeight);
	}
	
	/**
	 * Returns the size of the image of size [width, height] in pixels in cells 
	 * [width, height].
	 * 
	 * @param width see above.
	 * @param height see above.
	 * @return see above.
	 */
	public Dimension getImageSize(int width, int height)
	{
		Dimension d = new Dimension();
		//double remainderWidth =  (width%68)/68.0;
		int widthInCells = width/68;
		//double remainderHeight = (height%18)/18.0;
		int heightInCells = (height/18);
		d.setSize(widthInCells, heightInCells);
		return d;
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
	 * Writes the current row to the spreadsheet. 
	 * 
	 * @param startColumn see above.
	 * @param rowCount The current row in the table model.
	 * @param tableModel the table model to write.
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
				if (element != null) {
					writeElement(currentSheet.getCurrentRow()+
							elementRowCount,startColumn + columnCount, element);
				}
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
	 * @throws EncoderException Exception thrown if an error occurred during the
     * encoding process.
	 */
	public int addImageToWorkbook(String imageName, BufferedImage image) 
		throws EncoderException
	{
		byte[] array = WriterImage.imageToByteStream(image);
		if (array == null) return -1;
		int index = workbook.addPicture(array, HSSFWorkbook.PICTURE_TYPE_JPEG);
		imageMap.put(imageName, index);
		return index;
	}
	
	/**
	 * Writes the image with name imageName to the work sheet, at the 
	 * specified location (rowStart, colStart)->(rowEnd, colEnd).
	 * Returns the (colEnd, rowEnd).
	 * 
	 * @param rowStartIndex The index of the start row.
	 * @param colStartIndex The index of the start column.
	 * @param width 		The width of the image.
	 * @param height 		The height of the image.
	 * @param imageName 	The name of the image.
	 */
	public Point writeImage(int rowStartIndex, int colStartIndex, 
							int width, int height, String imageName)
	{
		HSSFPatriarch patriarch = currentSheet.getDrawingPatriarch();
		HSSFClientAnchor anchor;
		double remainderWidth =  (width%68)/68.0;
		int widthInCells = width/68;
		double remainderHeight = (height%18)/18.0;
		int heightInCells = (height/18);
		int rowEnd = rowStartIndex+heightInCells;
		int colEnd = colStartIndex+widthInCells;
		anchor = new HSSFClientAnchor(0, 0, (int) (remainderWidth*1023),
				 (int) (remainderHeight*255), (short) colStartIndex,
				 (short) rowStartIndex, (short) colEnd, (short) rowEnd);
		
		anchor.setAnchorType(3);
		int index = imageMap.get(imageName);
		patriarch.createPicture(anchor, index);
		return new Point(colEnd, rowEnd); 
	}
	
	/**
	 * Returns the cell corresponding to the passed row and column 
	 * from the current sheet.
	 * 
	 * @param row 		The selected row.
	 * @param column 	The selected column.
	 * @return See above.
	 */
	private HSSFCell getCell(int row , int column)
	{
		if (currentSheet == null) return null;
		return currentSheet.getCell(row, column);
	}
	
	/**
	 * Return the maximum column a value has been entered in for row. 
	 * 
	 * @param row see above.
	 * @return see above.
	 */
	public int getMaxColumn(int row)
	{
		if (currentSheet == null) return 0;
		return currentSheet.getMaxColumn(row);
	}
	
	/**
	 * Returns the current row.
	 * 
	 * @return See above.
	 */
	public int getCurrentRow()
	{
		if (currentSheet == null) return 0;
		return currentSheet.getCurrentRow();
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
		Iterator it = map.entrySet().iterator();
		Object key, value;
		Entry entry;
		while (it.hasNext())
		{
			entry = (Entry) it.next();
			key = entry.getKey();
			value = entry.getValue();
			if (key != null) writeElement(rowIndex, columnIndex, key);
			if (value != null) writeElement(rowIndex, columnIndex+1, value);
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
		if (isNumber(value))
		{
			if (value instanceof Integer)
				cell.setCellValue((Integer) value);
			else if (value instanceof Double)
				cell.setCellValue((Double) value);
			else if(value instanceof Float)
				cell.setCellValue((Float) value);
			else if (value instanceof Long)
				cell.setCellValue((Long)value);
		} else if (value instanceof Boolean)
			cell.setCellValue((Boolean) value);
		else
			cell.setCellValue(new HSSFRichTextString(value.toString()));
		currentSheet.setCurrentRow(rowIndex);
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
		currentSheet.setCurrentRow(rowIndex+1);
		for (int i = 0 ; i < values.length; i++)
			writeElement(rowIndex, columnIndex+i,values[i]);
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
		for (int i = 0 ; i < values.length; i++)
			writeElement(rowIndex+i, columnIndex,values[i]);
		currentSheet.setCurrentRow(rowIndex+values.length);
		return currentSheet.getCurrentRow();
	}
	
	/**
	 * Sets the cell Style for cells [startRow, startCol] to [endRow, endCol].
	 * 
	 * @param startRow see above.
	 * @param startCol see above.
	 * @param endRow see above.
	 * @param endCol see above.
	 * @param style see above.
	 */
	public void setCellStyle(int startRow, int startCol, int endRow, int endCol, 
			String style)
	{
		HSSFCellStyle cellStyle = getCellStyle(style);
		HSSFCell cell;
		for (int y = startRow; y <= endRow; y++)
			for (int x = startCol; x <= endCol ; x++)
			{
				cell = getCell(y, x);
				cell.setCellStyle(cellStyle);
			}
	}

	/**
	 * Adds hyperlink to the cell.
	 * 
	 * @param row see above.
	 * @param col see above.
	 * @param description contents of cell.
	 * @param URL address.
	 */
	public void addHyperlink(int row, int col, String description, String URL)
	{
		HSSFCell cell;
	    cell = getCell(row, col);
	    cell.setCellValue(new HSSFRichTextString(description));

	    HSSFHyperlink link = new HSSFHyperlink(HSSFHyperlink.LINK_URL);
	    link.setAddress(URL);
	    cell.setHyperlink(link);
	    //setCellStyle(cell, HYPERLINK);
	}
	
	/**
	 * Adds the font to the fontMap.
	 * 
	 * @param fontName see above.
	 * @param font add above.
	 */
	public void addFont(String fontName, HSSFFont font)
	{
		fontMap.put(fontName, font);
	}

	/**
	 * Adds the style to the styleMap.
	 * 
	 * @param styleName see above.
	 * @param style add above.
	 */
	public void addStyle(String styleName, HSSFCellStyle style)
	{
		styleMap.put(styleName, style);
	}
	
	/**
	 * Gets all the cell styles available in the workbook.
	 *  
	 * @return list of all cell styles in the map.
	 */
	public String[] getCellStyles()
	{
		Set<String> keys = styleMap.keySet();
		String[] array = new String[keys.size()];
		Iterator<String> i = keys.iterator();
		int index = 0;
		while (i.hasNext()) {
			array[index] = i.next();
			index++;
		}
		return array;
	}
	
	/**
	 * Gets all the fonts available in the workbook. 
	 * 
	 * @return list of all fonts in the workbook.
	 */
	public String[] getFonts()
	{
		Set<String> keys = fontMap.keySet();
		String[] array = new String[keys.size()];
		Iterator<String> i = keys.iterator();
		int index = 0;
		while (i.hasNext()) {
			array[index] = i.next();
			index++;
		}
		return array;
	}

	/**
	 * Sets the cell Style for cell at row and col.
	 * 
	 * @param row see above.
	 * @param col see above.
	 * @param style see above.
	 */
	public void setCellStyle(int row, int col, String style)
	{
		HSSFCell cell = getCell(row, col);
		setCellStyle(cell, style);
	}
	
}


