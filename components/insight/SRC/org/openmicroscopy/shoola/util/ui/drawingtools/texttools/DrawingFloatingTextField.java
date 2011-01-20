/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.texttools.DrawingFloatingTextField 
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
package org.openmicroscopy.shoola.util.ui.drawingtools.texttools;

//Java imports
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import javax.swing.JTextField;

//Third-party libraries
import org.jhotdraw.draw.DrawingView;
import org.jhotdraw.draw.TextHolderFigure;

//Application-internal dependencies

/** 
 * Embeds a text field to display text.
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
public class DrawingFloatingTextField 
{
	
	/** The default number of column for the text field. */
	private static final int DEFAULT_COLUMN = 20;
	
	/** The textfield to show the text and be written to. */
    private JTextField   editWidget;
    
    /** The parent drawing view of the object. */
    private DrawingView   view;
    
    /**
     * Create the floating textfield.
     *
     */
    public DrawingFloatingTextField() 
    {
        editWidget = new JTextField(DEFAULT_COLUMN);
        editWidget.setOpaque(false);
        editWidget.setHorizontalAlignment(JTextField.CENTER);
    }
    
    /**
     * Creates the overlay for the given Component.
     * 
     * @param view the view to create the overlay on.
     */
    public void createOverlay(DrawingView view)
    {
        createOverlay(view, null);
    }

    /**
     * Creates the overlay for the given Container using a
     * specific font.
     * 
     * @param view The drawing view to create the overlay on.
     * @param figure The figure from which the text belongs.
     */
    public void createOverlay(DrawingView view, TextHolderFigure figure)
    {
    	if (view == null)
    		throw new IllegalArgumentException("Drawing View cannot be null.");
    	 this.view = view;
        view.getComponent().add(editWidget, 0);
        if (figure == null) return;
        Font f = figure.getFont();
        // FIXME - Should scale with fractional value!
        f = f.deriveFont(f.getStyle(), 
        		(float) (figure.getFontSize()*view.getScaleFactor()));
        editWidget.setFont(f);
        editWidget.setForeground(figure.getTextColor());
        editWidget.setBackground(figure.getFillColor());
       
    }
    
    /** Gives the textfield focus. */
    public void requestFocus() { editWidget.requestFocus(); }
    
    /** 
     * Returns the insets of the object. 
     * 
     * @return See above.
     */
    public Insets getInsets() { return editWidget.getInsets(); }
    
    /**
     * Adds an action listener
     * 
     * @param listener The listener to add.
     */
    public void addActionListener(ActionListener listener)
    {
    	if (listener == null) return;
        editWidget.addActionListener(listener);
    }
    
    /**
     * Removes an action listener.
     * @param listener The listener to remove.
     */
    public void removeActionListener(ActionListener listener)
    {
    	if (listener == null) return;
        editWidget.removeActionListener(listener);
    }
    
    /**
     * Positions the overlay.
     * 
     * @param r 	The rectangle of the bounds.
     * @param text 	The text of the object.
     */
    public void setBounds(Rectangle r, String text)
    {
    	if (r == null) return;
        editWidget.setText(text);
        editWidget.setBounds(r.x, r.y, r.width, r.height);
        editWidget.setVisible(true);
        editWidget.selectAll();
        editWidget.requestFocus();
    }
    
    /**
     * Returns the text contents of the overlay.
     * 
     * @return See above.
     */
    public String getText() { return editWidget.getText(); }
    
    /**
     * Returns the preferred size of the overlay.
     * 
     * @param cols the number of columns in the widget.
     * @return See above.
     */
    public Dimension getPreferredSize(int cols)
    {
        editWidget.setColumns(cols);
        return editWidget.getPreferredSize();
    }
    
    /** Removes the overlay. */
    public void endOverlay()
    {
        view.getComponent().requestFocus();
        if (editWidget != null)
        {
        	editWidget.setVisible(false);
        	view.getComponent().remove(editWidget);
        	Rectangle r = editWidget.getBounds();
        	view.getComponent().repaint(r.x, r.y, r.width, r.height);
        }
    }
}