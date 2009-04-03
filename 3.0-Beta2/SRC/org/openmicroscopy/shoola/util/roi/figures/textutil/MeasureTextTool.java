/*
 * org.openmicroscopy.shoola.util.roi.figures.textutil.MeasureTextTool 
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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Map;

//Third-party libraries
import org.jhotdraw.draw.CreationTool;
import org.jhotdraw.draw.DrawingEditor;
import org.jhotdraw.draw.Figure;
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
public 	class MeasureTextTool 
		extends CreationTool 
		implements ActionListener 
{

	private MeasureFloatingTextField   	textField;
    private TextHolderFigure  			typingTarget;
    
    /** Creates a new instance. */
    public MeasureTextTool(TextHolderFigure prototype) 
    {
        super(prototype);      
    }
    
    /** Creates a new instance. */
    public MeasureTextTool(TextHolderFigure prototype, Map attributes) 
    {
        super(prototype, attributes);
    }
    
    public void deactivate(DrawingEditor editor) 
    {
        endEdit();
        super.deactivate(editor);
    }
    
    /**
     * If the pressed figure is a TextHolderFigure it can be edited otherwise
     * a new text figure is created.
     */
    public void mousePressed(MouseEvent e) {
        TextHolderFigure textHolder = null;
        Figure pressedFigure = getDrawing().findFigureInside(getView().viewToDrawing(new Point(e.getX(), e.getY())));
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
            // update view so the created figure is drawn before the floating text
            // figure is overlaid. (Note, fDamage should be null in StandardDrawingView
            // when the overlay figure is drawn because a JTextField cannot be scrolled)
            //view().checkDamage();
            textHolder = (TextHolderFigure)getCreatedFigure();
            beginEdit(textHolder);
        }
    }
    
    public void mouseDragged(java.awt.event.MouseEvent e) {
    }
    
    protected void beginEdit(TextHolderFigure textHolder) {
        if (textField == null) {
            textField = new MeasureFloatingTextField();
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
    
    
    protected Rectangle getFieldBounds(TextHolderFigure figure) {
        Rectangle textBox = getView().drawingToView(figure.getBounds());
    
        int h = (int)Math.min(24, textBox.getHeight());
        int y = (int)textBox.getY()+(int)(textBox.getHeight()/2)-h/2;
        
        Rectangle box = new Rectangle((int)textBox.getX(), y, (int)textBox.getWidth(), h);
        Insets insets = textField.getInsets();
        return new Rectangle(
                box.x - insets.left, 
                box.y - insets.top, 
                box.width + insets.left + insets.right, 
                box.height + insets.top + insets.bottom
                );
    }
    
    public void mouseReleased(MouseEvent evt) {
        /*
        if (createdFigure != null) {
            Rectangle bounds = createdFigure.getBounds();
            if (bounds.width == 0 && bounds.height == 0) {
                getDrawing().remove(createdFigure);
            } else {
                getView().addToSelection(createdFigure);
            }
            createdFigure = null;
            getDrawing().fireUndoableEditHappened(creationEdit);
            fireToolDone();
        }*/
    }
    
    protected void endEdit() {
        if (typingTarget != null) {
            //typingTarget.willChange();
            if (textField.getText().length() > 0) {
                typingTarget.setText(textField.getText());
                if (createdFigure != null) {
                    getDrawing().fireUndoableEditHappened(creationEdit);
                    createdFigure = null;
                }
            } else {
                if (createdFigure != null) {
                    getDrawing().remove((Figure)getAddedFigure());
                } else {
                    typingTarget.setText("");
                }
            }
            // nothing to undo
            //	            setUndoActivity(null);
            //typingTarget.changed();
            typingTarget = null;
            
            textField.endOverlay();
        }
        //	        view().checkDamage();
    }
    
    public void actionPerformed(ActionEvent event) {
        endEdit();
        fireToolDone();
    }
}
