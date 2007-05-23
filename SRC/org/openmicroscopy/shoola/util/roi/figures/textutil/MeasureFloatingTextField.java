/*
 * org.openmicroscopy.shoola.util.roi.figures.textutil.MeasureFloatingTextField 
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
package org.openmicroscopy.shoola.util.roi.figures.textutil;

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
public class MeasureFloatingTextField 
{
    private JTextField   editWidget;
    private DrawingView   view;
    
    public MeasureFloatingTextField() {
        editWidget = new JTextField(20);
        editWidget.setOpaque(false);
        editWidget.setHorizontalAlignment(JTextField.CENTER);
    }
    
    /**
     * Creates the overlay for the given Component.
     */
    public void createOverlay(DrawingView view) {
        createOverlay(view, null);
    }
    
    public void requestFocus() {
        editWidget.requestFocus();
    }
    
    /**
     * Creates the overlay for the given Container using a
     * specific font.
     */
    public void createOverlay(DrawingView view, TextHolderFigure figure) {
        view.getComponent().add(editWidget, 0);
        Font f = figure.getFont();
        // FIXME - Should scale with fractional value!
        f = f.deriveFont(f.getStyle(), (float) (figure.getFontSize() * view.getScaleFactor()));
        editWidget.setFont(f);
        editWidget.setForeground(figure.getTextColor());
        editWidget.setBackground(figure.getFillColor());
        this.view = view;
    }
    
    public Insets getInsets() {
        return editWidget.getInsets();
    }
    
    /**
     * Adds an action listener
     */
    public void addActionListener(ActionListener listener) {
        editWidget.addActionListener(listener);
    }
    
    /**
     * Remove an action listener
     */
    public void removeActionListener(ActionListener listener) {
        editWidget.removeActionListener(listener);
    }
    
    /**
     * Positions the overlay.
     */
    public void setBounds(Rectangle r, String text) {
        editWidget.setText(text);
        editWidget.setBounds(r.x, r.y, r.width, r.height);
        editWidget.setVisible(true);
        editWidget.selectAll();
        editWidget.requestFocus();
    }
    
    /**
     * Gets the text contents of the overlay.
     */
    public String getText() {
        return editWidget.getText();
    }
    
    /**
     * Gets the preferred size of the overlay.
     */
    public Dimension getPreferredSize(int cols) {
        editWidget.setColumns(cols);
        return editWidget.getPreferredSize();
    }
    
    /**
     * Removes the overlay.
     */
    public void endOverlay() {
        view.getComponent().requestFocus();
        if (editWidget != null) {
            editWidget.setVisible(false);
            view.getComponent().remove(editWidget);
            
            Rectangle bounds = editWidget.getBounds();
            view.getComponent().repaint(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}