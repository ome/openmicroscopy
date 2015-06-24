/*
 * org.openmicroscopy.shoola.agents.measurement.util.ui.ResultsCellRenderer 
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
package org.openmicroscopy.shoola.agents.measurement.util.ui;




//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.view.MeasurementTableModel;
import org.openmicroscopy.shoola.util.roi.model.annotation.AnnotationKeys;
import org.openmicroscopy.shoola.util.roi.model.util.FigureType;
import org.openmicroscopy.shoola.util.roi.model.util.MeasurementUnits;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.drawingtools.figures.FigureUtil;
import omero.model.Length;
import omero.model.enums.UnitsLength;

/** 
 * Basic cell renderer displaying analysis results.
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
public class ResultsCellRenderer 
	extends JComponent
	implements TableCellRenderer
{
	
	/** Reference to the <code>Scribble</code> icon. */
	private static final Icon SCRIBBLE;
	
	/** Reference to the <code>Line</code> icon. */
	private static final Icon LINE;
	
	/** Reference to the <code>Connection</code> icon. */
	private static final Icon CONNECTION;
	
	/** Reference to the <code>Polygon</code> icon. */
	private static final Icon POLYGON;
	
	/** Reference to the <code>Point</code> icon. */
	private static final Icon POINT;
	
	/** Reference to the <code>Rectangle</code> icon. */
	private static final Icon RECTANGLE;
	
	/** Reference to the <code>Ellipse</code> icon. */
	private static final Icon ELLIPSE;
	
	/** Reference to the <code>Text</code> icon. */
	private static final Icon TEXT;
	
	/** Reference to the <code>Mask</code> icon. */
	private static final Icon MASK;
	
	static { 
		IconManager icons = IconManager.getInstance();
		SCRIBBLE = icons.getIcon(IconManager.SCRIBBLE);
		LINE = icons.getIcon(IconManager.LINE_16);
		CONNECTION = icons.getIcon(IconManager.CONNECTION);
		POLYGON = icons.getIcon(IconManager.POLYGON_16);
		POINT = icons.getIcon(IconManager.POINT_16);
		RECTANGLE = icons.getIcon(IconManager.RECTANGLE);
		ELLIPSE = icons.getIcon(IconManager.ELLIPSE_16);
		TEXT = icons.getIcon(IconManager.TEXT_16);
		MASK = icons.getIcon(IconManager.MASK);
	}


	/**
	 * Adds the appropriate shape icon to the label.
	 * 
	 * @param shape above.
	 */
	private JComponent makeShapeIcon(JLabel label, String shape)
	{
		if (FigureUtil.SCRIBBLE_TYPE.equals(shape))
			label.setIcon(SCRIBBLE);
		else if (FigureUtil.LINE_TYPE.equals(shape)) 
			label.setIcon(LINE);
		else if (FigureUtil.LINE_CONNECTION_TYPE.equals(shape)) 
			label.setIcon(CONNECTION);
		else if (FigureUtil.POLYGON_TYPE.equals(shape)) 
			label.setIcon(POLYGON);
		else if (FigureUtil.POINT_TYPE.equals(shape)) 
			label.setIcon(POINT);
		else if (FigureUtil.RECTANGLE_TYPE.equals(shape)) 
			label.setIcon(RECTANGLE);
		else if (FigureUtil.ELLIPSE_TYPE.equals(shape)) 
			label.setIcon(ELLIPSE);
		else if (FigureUtil.TEXT_TYPE.equals(shape)) 
			label.setIcon(TEXT);
		else if (FigureUtil.MASK_TYPE.equals(shape)) 
			label.setIcon(MASK);
		else label.setText(shape);
		if (label.getIcon() != null) {
			JPanel p = new JPanel();
			FlowLayout layout = new FlowLayout(FlowLayout.CENTER);
			layout.setVgap(0);
			p.setBorder(BorderFactory.createEmptyBorder());
			p.setLayout(layout);
			p.setOpaque(true);
			p.setBackground(getBackground());
			p.add(label);
			return p;
		}
		return label;
	}
	
	/**
	 * Formats the passed object to two decimal places and returns as a string.
	 * 
	 * @param value The object to handle.
	 * @return See above.
	 */
	private String twoDecimalPlaces(Float value)
	{
		return UIUtilities.twoDecimalPlaces(value.doubleValue());
	}
	
	/**
	 * Formats the passed object to two decimal places and returns as a string.
	 * 
	 * @param value The object to handle.
	 * @return See above.
	 */
	private String twoDecimalPlaces(Double  value)
	{
		return UIUtilities.twoDecimalPlaces(value);
	}
		
	/**
	 * Creates and returns a {@link JList} from the passed object.
	 * 
	 * @param value The object to handle.
	 * @return See above.
	 */
	private JList createList(Object value)
	{
		List elementList = (List) value;
		JList list = new JList();
		DefaultListModel model = new DefaultListModel();
		String v;
		for (Object element : elementList)
		{
			if (element instanceof Float)
			{
				v = twoDecimalPlaces((Float) element);
				if (v == null) return list;
				model.addElement(v);
			}
			else if (element instanceof Double)
			{
				v = twoDecimalPlaces((Double) element);
				if (v == null)
					return list;
				model.addElement(v);
			}
			else if (element instanceof Length)
			{
				v = twoDecimalPlaces(((Length) element).getValue());
				if (v == null)
					return list;
				model.addElement(v);
			}
		}
		list.setModel(model);
		return list;
	}

    
	/**
	 * Creates a new instance. Sets the opacity of the label to 
	 * <code>true</code>.
	 */
	public ResultsCellRenderer()
	{
	    setOpaque(true);
	}
	
	/**
	 * @see TableCellRenderer#getTableCellRendererComponent(JTable, Object, 
	 * 										boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		Component thisComponent = new JLabel();
		JLabel label = new JLabel();
		label.setOpaque(true);
		
		
		MeasurementTableModel tm = (MeasurementTableModel) table.getModel();
		KeyDescription key = tm.getColumnNames().get(column);
        String k = key.getKey();
 
		if (value instanceof Length)
		{
	        MeasurementUnits units = tm.getUnitsType();
	        Length n = (Length) value;
		    String s;
		    if (!units.getUnit().equals(UnitsLength.PIXEL)) {
	            s = UIUtilities.formatValue(n, AnnotationKeys.AREA.getKey().equals(k));
	            
	            if (CommonsLangUtils.isNotBlank(s)) 
	                label.setText(s);
		    } else {
		        s = UIUtilities.twoDecimalPlaces(n.getValue());
                if (CommonsLangUtils.isNotBlank(s)) {
                    label.setText(s);
                }
		    }
    		thisComponent = label;
		}
		else if (value instanceof FigureType || value instanceof String) {
			thisComponent = makeShapeIcon(label, ""+value);
    	} else if (value instanceof Color)  {
    		label.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    		label.setBackground((Color) value);
    		thisComponent = label;
      	} else if( value instanceof Boolean) {
      		JCheckBox checkBox = new JCheckBox();
    		checkBox.setSelected((Boolean) value);
    		thisComponent = checkBox;
    	} else if(value instanceof ArrayList) {
    		thisComponent = createList(value);
    		//return list;
    	}
    	else if (value instanceof Number) {
    		String s;
    		if(value instanceof Integer || value instanceof Long) {
    			s = ""+value;
    		}
    		else {
    			s = UIUtilities.twoDecimalPlaces(((Number) value).doubleValue());
				if (s == null) {
					s = "0";
				}
    		}
    		
    		if(k.equals(AnnotationKeys.ANGLE.getKey())) {
    			s += UIUtilities.DEGREE_SYMBOL;
    		}
    		
    		label.setText(s);
    		thisComponent = label;
    	}
    	else if (value instanceof String) {
    		label.setText((String)value);
    		thisComponent = label;
    	}
		if (!(value instanceof Color)) {
			RendererUtils.setRowColor(thisComponent, table.getSelectedRow(), 
									row);
			if (label != null)
				label.setBackground(thisComponent.getBackground());
		}
		return thisComponent;
	}

}
