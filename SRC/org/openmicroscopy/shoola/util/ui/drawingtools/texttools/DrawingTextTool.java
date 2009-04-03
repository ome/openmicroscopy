/*
 * org.openmicroscopy.shoola.util.ui.drawingtools.texttools.DrawingTextTool 
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
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

//Third-party libraries
import org.jhotdraw.draw.CreationTool;
import org.jhotdraw.draw.Drawing;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
import org.jhotdraw.draw.TextHolderFigure;

//Application-internal dependencies

/** 
 * A tool to create Text figure.
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
public class DrawingTextTool 
	extends CreationTool 
	implements ActionListener 
{

	/** The floating text field. */
	protected DrawingFloatingTextField   	textField;
	
	/** The figure of reference. */
	protected TextHolderFigure  			typingTarget;
    
    /** 
     * Begins the edition.
     * 
     * @param textHolder The figure to handle.
     */
    private void beginEdit(TextHolderFigure textHolder)
    {
        if (textField == null) {
            textField = new DrawingFloatingTextField();
            textField.addActionListener(this);
        }
        
        if (textHolder != typingTarget && typingTarget != null) {
            endEdit();
        }
        textField.createOverlay(getView(), textHolder);
        textField.setBounds(getFieldBounds(textHolder), textHolder.getText());
        textField.requestFocus();
        typingTarget = textHolder;
    }
    
    /**
     * Returns the bounds of the figure.
     * 
     * @param figure The figure to handle.
     * @return See above.
     */
    protected Rectangle getFieldBounds(TextHolderFigure figure)
    {
        Rectangle textBox = getView().drawingToView(figure.getBounds());
    
        int h = (int) Math.min(24, textBox.getHeight());
        int y = (int) textBox.getY()+(int)(textBox.getHeight()/2)-h/2;
        
        Rectangle box = new Rectangle((int) textBox.getX(), y, 
        							(int) textBox.getWidth(), h);
        Insets insets = textField.getInsets();
        return new Rectangle(
                box.x - insets.left, 
                box.y - insets.top, 
                box.width + insets.left + insets.right, 
                box.height + insets.top + insets.bottom
                );
    }
    
    /** Stops the edition. */
    private void endEdit()
    {
    	if (typingTarget == null) return;
    	 //typingTarget.willChange();
        if (textField.getText().length() > 0) {
            typingTarget.setText(textField.getText());
            if (createdFigure != null) {
            	final Figure addedFigure = createdFigure;
                final Drawing addedDrawing = getDrawing();
                getDrawing().fireUndoableEditHappened(new AbstractUndoableEdit() {
                   public String getPresentationName() {
                       return super.getPresentationName();
                   }
                   public void undo() throws CannotUndoException {
                       super.undo();
                       addedDrawing.remove(addedFigure);
                   }
                   public void redo() throws CannotRedoException {
                       super.redo();
                       addedDrawing.add(addedFigure);
                   }
               });    
                createdFigure = null;
            }
        } else {
            if (createdFigure != null) getDrawing().remove(getAddedFigure());
            else typingTarget.setText("");
        }
        // nothing to undo
        //	            setUndoActivity(null);
        //typingTarget.changed();
        typingTarget = null;
        
        textField.endOverlay();
    }
    
    /** 
     * Creates a new instance. 
     * 
     * @param prototype The figure.
     */
    public DrawingTextTool(TextHolderFigure prototype) 
    {
        super(prototype);      
    }

    /**
     * Overridden to check if we can edit the figure.
     * If the pressed figure is a TextHolderFigure it can be edited otherwise
     * a new text figure is created.
     * @see CreationTool#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent e)
    {
        TextHolderFigure textHolder = null;
        Point2D.Double  p = getView().viewToDrawing(e.getPoint());
        Figure pressedFigure = getDrawing().findFigureInside(p);
        if (pressedFigure instanceof TextHolderFigure) {
            textHolder = ((TextHolderFigure) pressedFigure).getLabelFor();
            if (!textHolder.isEditable())
                textHolder = null;
        }
        if (textHolder != null) {
            beginEdit(textHolder);
            return;
        }
        if (typingTarget != null) {
            endEdit();
            fireToolDone();
        } else {
            super.mousePressed(e);
            // update view so the created figure is drawn before the 
            // floating text figure is overlaid. (Note, Damage should be null in 
            //StandardDrawingView when the overlay figure is drawn because a 
            //JTextField cannot be scrolled)
            //view().checkDamage();
            textHolder = (TextHolderFigure) getCreatedFigure();
            beginEdit(textHolder);
        }
    }
    
    
    /** 
     * Overridden to end the edition.
     * @see CreationTool#deactivate(DrawingEditor)
     */
    public void deactivate(DrawingEditor editor) 
    {
        endEdit();
        super.deactivate(editor);
    }
    
    /**
     * Ends the edition.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
        endEdit();
        fireToolDone();
    }

}
