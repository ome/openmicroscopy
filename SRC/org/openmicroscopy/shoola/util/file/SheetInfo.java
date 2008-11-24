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

//Third-party libraries

//Application-internal dependencies
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

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
public class SheetInfo
{	
	/** The sheet this info relates to. */
	HSSFSheet			sheet;
	
	/** The name of the sheet. */
	String 				name;
	
	/** current position in workbook. */
	int 				index;
	
	/** The current row in the spreadsheet. */
	int 				currentRow;
	
	/** the current drawing context of the sheet. */
	HSSFPatriarch		drawingPatriarch;
	
	public SheetInfo(String name, int sheetIndex, HSSFSheet sheet)
	{
		this.sheet = sheet;
		this.name = name;
		this.index = sheetIndex;
		currentRow = 0;
	}
	
	public HSSFCell getCell(int rowIndex, int columnIndex)
	{
		HSSFRow row;
		HSSFCell cell;
		row = sheet.getRow(rowIndex);
		if(row==null)
			row = sheet.createRow(rowIndex);
		cell = row.getCell(columnIndex);
		if(cell==null)
			cell = row.createCell(columnIndex);
		return cell;
	}

	public int getIndex()
	{
		return index;
	}

	public int getCurrentRow()
	{
		return currentRow;
	}
	
	public void setCurrentRow(int row)
	{
		currentRow = row;
	}
	
	public HSSFPatriarch getDrawingPatriarch()
	{
		if(drawingPatriarch == null) 
			drawingPatriarch = sheet.createDrawingPatriarch();
	
		return drawingPatriarch;
	}
	
}


