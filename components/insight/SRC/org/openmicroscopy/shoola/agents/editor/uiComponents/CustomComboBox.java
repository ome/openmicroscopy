
 /*
 * ui.components.CustomComboBox
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.editor.uiComponents;

// java imports

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.util.ui.OMEComboBox;
import org.openmicroscopy.shoola.util.ui.OMEComboBoxUI;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * This is a JComboBox that has custom size behaviour and 
 * display of long Strings.
 * Long strings are truncated (with a tool-tip of the full text).
 *  
 * @author will
 *
 */
public class CustomComboBox 
	extends OMEComboBox 
	{

	public static final int MAX_LINE_LENGTH = 52;
	
	private int maximumWidth = 0;
	private int maxPreferredWidth = 0;
	
	/**
	 * Creates an instance 
	 * 
	 * @param items		Items to display in the comboBox
	 */
	public CustomComboBox(String[] items) {
		super(items);
		initialise();
	}
	
	/**
	 * Creates an instance 
	 * 
	 * @param maxPreferredWidth		The width will be at least this value. 
	 */
	public CustomComboBox(int maxPreferredWidth) {
		super(new Object[0]);
		initialise();
		setMaxPreferredWidth(maxPreferredWidth);
	}
	
	/**
	 * Initialises the UI. Called by all constructors. 
	 */
	private void initialise() {
		this.setFont(new CustomFont());
		setUI(new OMEComboBoxUI());
		setBackground(UIUtilities.BACKGROUND_COLOR);
		setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1,
                UIUtilities.LIGHT_GREY));
		this.setRenderer(new TruncatedListItemRenderer());
		this.addActionListener(new TruncateSelectionListener());
	}
	
	/**
	 * Set an absolute maximum width for this ComboBox.
	 * getMaximumSize()) will not return a higher width
	 * than this, even if the list items are longer. 
	 * 
	 * @param maxWidth	The absolute max width
	 */
	public void setMaximumWidth(int maxWidth) {
		maximumWidth = maxWidth;
	}

	/**
	 * Set the maximum preferred width.
	 * getMaximumSize() will return this width unless the list items are wider.
	 * Using this method will allow the ComboBox to expand beyond 
	 * maxPreferredWidth if needed to display the list items fully.
	 * 
	 * @param maxWidth		The maximum preferred width
	 */
	public void setMaxPreferredWidth(int maxWidth) {
		maxPreferredWidth = maxWidth;
	}
	
	/**
	 * This overrides the component's getMaximumSize(), based on 
	 * whether setMaximumWidth() and setMaximumPreferredWidth() have
	 * been called. 
	 * 
	 * @see  setMaximumWidth()
	 * @see  setMaximumPreferredWidth();
	 */
	public Dimension getMaximumSize() {
		
		
		return getPreferredSize();
	}
	
	
	/**
	 * This overrides the component's getPreferredSize(), based on 
	 * whether setMaximumWidth() and setMaximumPreferredWidth() have
	 * been called. 
	 * 
	 * @see  setMaximumWidth()
	 * @see  setMaximumPreferredWidth();
	 */
	public Dimension getPreferredSize() {
		
		Dimension size = super.getPreferredSize();
		
		int prefWidth = 0;
		int height = 0;
		
		prefWidth = (int)size.getWidth() + 20;
		
		/*
		 * Make sure width is at least the max preferred width...
		 */
		if (maxPreferredWidth > 0) {
			prefWidth = Math.max(prefWidth, maxPreferredWidth);
		}
		/*
		 * But not more than the absolute maximum width.
		 */
		if (maximumWidth > 0) {
			prefWidth = Math.min(prefWidth, maximumWidth);
		} 
		
		height = (int)size.getHeight();
		
		// setMaximumSize(new Dimension(300, height));
		
		return new Dimension(prefWidth, height);
		
		
	}
	
	/**
	 * This is a list cell renderer that displays long items as multi-line
	 * items, by wrapping between words, using html in a JLabel.
	 * 
	 * WARNING. If the ComboBox isEditable(), display of multi-line list items
	 *  causes the Editor Component to resize to multiple lines.
	 *  If not Editable, multi-lined items are displayed in the selected box,
	 *  looking wrong! 
	 *  Therefore, this Renderer is not used currently. 
	 * 
	 * @author will
	 *
	 */
	public class MultiLineListItemRenderer 
		extends JLabel
    	implements ListCellRenderer {

		public MultiLineListItemRenderer() {
	        setOpaque(true);
	        setHorizontalAlignment(LEFT);
	        setVerticalAlignment(CENTER);
	    }
		
		public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) 
		{
			
			if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
			} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
			}
			
			//Set the text. 
			if (value == null) {
				return this;
			}
			
			String fulltext = value.toString();
			
			String htmlText = "<html>";
			
			while (fulltext.length() > 0) {
				
				int newLineLength = MAX_LINE_LENGTH;
				
				if (fulltext.length() > MAX_LINE_LENGTH) {
					newLineLength = fulltext.lastIndexOf(" ", MAX_LINE_LENGTH);
				} else {
					newLineLength = fulltext.length();
				}
				
				String newLine = fulltext.substring(0, newLineLength);
				
				htmlText = htmlText + newLine + "<br> &nbsp &nbsp &nbsp";
				
				fulltext = fulltext.substring(newLineLength, fulltext.length());
			}
			
			htmlText = htmlText.substring(0, htmlText.lastIndexOf("<br>"));
			
			htmlText = htmlText + "</html>";
			
			setText(htmlText);
			setFont(list.getFont());
			
			return this;
		}
		
	
	}
	
	
	/**
	 * This renderer causes each list item to be truncated above
	 * the length of MAX_LINE_LENGTH.
	 * And, if truncated, the full text is added as a tool tip.
	 * 
	 * @author will
	 */
	public class TruncatedListItemRenderer 
		extends CustomLabel
		implements ListCellRenderer {

		public TruncatedListItemRenderer() {
	        setOpaque(true);
	        setHorizontalAlignment(LEFT);
	        setVerticalAlignment(CENTER);
	    }
	
		public Component getListCellRendererComponent(
	            JList list,
	            Object value,
	            int index,
	            boolean isSelected,
	            boolean cellHasFocus) 
		{
			
			if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
			} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
			}
			
			//Truncate the text, setting tool tip if truncated. 
			if (value != null) {
				String fullText = value.toString();
				
				if (fullText.length() > MAX_LINE_LENGTH) {
					setToolTipText(fullText);
					fullText = fullText.substring(0, MAX_LINE_LENGTH) + "...";
				} else {
					setToolTipText(null);
				}
				
				setText(fullText);
				setBorder(new EmptyBorder(1,2,1,2));
			}
			
			return this;
		}
		
	
	}

	/**
	 * ActionListener added to the ComboBox, which 
	 * calls 
	 * truncateEditableItem() when a new selection is made, or 
	 * (in the case of and Editable CombBox) when setSelectedItem("some text")
	 * is called. 
	 * 
	 * @see truncateEditableItem()
	 * @author will
	 *
	 */
	public class TruncateSelectionListener 
		implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			truncateEditableItem();
		}
	}
	
	/** 
	 * This method truncates text if longer than MAX_LINE_LENGTH and, 
	 * if trunctated, adds a tool tip for the full text. 
	 */
	public void truncateEditableItem() {
		// if (isEditable()) {
			
			Object text = getSelectedItem();
			String fullText = "";
			if (text != null) {
				fullText = text.toString();
			}
			
			/*
			 * If the text is longer than allowed, truncate it. 
			 */
			if (fullText.length() > MAX_LINE_LENGTH) {
				/*
				 * First, add a tool tip so that users can see the full text
				 */
				setToolTipText(fullText);
				
				fullText = fullText.substring(0, MAX_LINE_LENGTH) + "...";
				this.setSelectedItem(fullText);
			} else {
				setToolTipText(null);
			}
		//}
	}

}
