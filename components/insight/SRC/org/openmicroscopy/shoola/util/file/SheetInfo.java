/*
 * org.openmicroscopy.shoola.util.file.SheetInfo 
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

/** 
 * A sheet of an Excel document.
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
class SheetInfo
{	
	
	/** The sheet this info relates to. */
	private HSSFSheet		sheet;
	
	/** The name of the sheet. */
	private String			name;
	
	/** The current position in workbook. */
	private int				index;
	
	/** The current row in the spreadsheet. */
	private int				currentRow;
	
	/** The current drawing context of the sheet. */
	private HSSFPatriarch	drawingPatriarch;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param name			The name of the sheet.
	 * @param sheetIndex	The current position in workbook.
	 * @param sheet			The sheet this object is related to.
	 */
	SheetInfo(String name, int sheetIndex, HSSFSheet sheet)
	{
		this.sheet = sheet;
		this.name = name;
		this.index = sheetIndex;
		currentRow = 0;
	}
	
	/**
	 * Returns the cell corresponding to the row and column
	 * 
	 * @param rowIndex		The selected row.
	 * @param columnIndex	The selected column.
	 * @return See above.
	 */
	HSSFCell getCell(int rowIndex, int columnIndex)
	{
		
		HSSFRow row = sheet.getRow(rowIndex);
		if (row == null) {
			row = sheet.createRow(rowIndex);
			return row.createCell(columnIndex);
		}
		HSSFCell cell = row.getCell(columnIndex);
		if (cell == null) return row.createCell(columnIndex);
		return cell;
	}
	
	/**
	 * Auto-sizes a column.
	 * 
	 * @param columnIndex The index of the column.
	 */
	void sizeColumnToFit(int columnIndex)
	{
		sheet.autoSizeColumn((short) columnIndex);
	}
	
	/** Auto-sizes all columns to fit contents. */
	void sizeAllColumnsToFit()
	{
		Map<Integer, Integer> colMap = new HashMap<Integer,Integer>();
		Iterator rowIterator = sheet.iterator();//sheet.rowIterator();
		HSSFCell cell;
		HSSFRow row;
		Iterator k;
		while (rowIterator.hasNext())
		{
			row = (HSSFRow) rowIterator.next();
			k = row.cellIterator();
			while (k.hasNext())
			{
				cell = (HSSFCell) k.next();
				colMap.put(cell.getColumnIndex(), cell.getRowIndex());
			}
		}
		Iterator<Integer> colIterator = colMap.keySet().iterator();
		int col;
		while (colIterator.hasNext())
		{
			col = colIterator.next();
			sizeColumnToFit(col);
		}
	}
	
	/**
	 * Sets the height of the rows [rowStart, rowEnd] to rowHeight in pixels.
	 * 
	 * @param rowStart see above.
	 * @param rowEnd see above.
	 * @param rowHeight see above.
	 */
	void setRowHeight(int rowStart, int rowEnd, int rowHeight)
	{
		for (int index = rowStart ; index <= rowEnd ; index++)
			setRowHeight(index, rowHeight);
	}
	
	/**
	 * Sets the height of the row rowIndex to rowHeight in pixels.
	 * 
	 * @param rowIndex 	The index of the row.
	 * @param rowHeight The height of the row.
	 */
	void setRowHeight(int rowIndex, int rowHeight)
	{
		HSSFRow row = sheet.getRow(rowIndex);
		if (row == null) row = sheet.createRow(rowIndex);
		row.setHeight((short) rowHeight);
	}
	
	/**
	 * Returns the last cell column written to in row rowIndex.
	 * 
	 * @param rowIndex The index of the row.
	 * @return See above.
	 */
	int getMaxColumn(int rowIndex)
	{
		HSSFRow row = sheet.getRow(rowIndex);
		if (row == null) return 0;
		return row.getLastCellNum();
	}

	/**
	 * Returns the current position in workbook.
	 * 
	 * @return See above.
	 */
	int getIndex() { return index; }

	/**
	 * Returns the current row.
	 * 
	 * @return See above.
	 */
	int getCurrentRow() { return currentRow; }
	
	/**
	 * Sets the current row.
	 * 
	 * @param row The value to set.
	 */
	void setCurrentRow(int row) { currentRow = row; }
	
	/**
	 * Returns the current drawing context of the sheet. 
	 * 
	 * @return See above.
	 */
	HSSFPatriarch getDrawingPatriarch()
	{
		if (drawingPatriarch == null) 
			drawingPatriarch = sheet.createDrawingPatriarch();
		return drawingPatriarch;
	}
	
	/**
	 * Returns the name of the sheet.
	 * 
	 * @return See above.
	 */
	String getName() { return name; }
	
	/**
	 * Sets the name of the sheet.
	 * 
	 * @param name The name of the sheet.
	 */
	void setName(String name) { this.name = name; }
	
}