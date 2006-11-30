/*
 * org.openmicroscopy.shoola.agents.roi.pane.AssistantDialogMng
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.roi.pane;

//Java imports
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.AbstractButton;
import javax.swing.JTextField;
import javax.swing.table.TableColumn;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
import org.openmicroscopy.shoola.agents.roi.ROIFactory;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.math.geom2D.PlaneArea;
import org.openmicroscopy.shoola.util.ui.ColoredLabel;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class AssistantDialogMng
    implements ActionListener, FocusListener, MouseListener, MouseMotionListener
{
    
    /** Action ID, to copy segment. */
    private static final int        COPY_STACK = 0, START_T = 1, END_T = 2;

    private static final String     MSG = "To copy across Z, select two " +
            "points in the same column. To copy across T, select two " +
            "points in the same row.";
    
    private int                     curRow, curColumn, oldRow, oldColumn;
    
    private int                     curStart, curEnd;
    
    private Color                   selectedColor;
    
    private boolean                 dragging, pressed;
    
    private AssistantDialog         view;
    
    private ROIAgtCtrl              control;
    
    private int                     cellWidth, cellHeight, tableWidth, 
                                    tableHeight;
    
    private int                     numRows, numColumns;
    
    private Point                   anchor, staticAnchor;
    
    public AssistantDialogMng(AssistantDialog view, ROIAgtCtrl control, 
                            int maxT)
    {
        this.view = view;
        this.control = control;
        curStart = 0;
        curEnd = maxT;
        setDefaultLocation();
    }

    /** Select the active cell.*/
    public void mousePressed(MouseEvent e)
    {
        if (!pressed) {
            pressed = true;
            anchor = e.getPoint();
            staticAnchor = e.getPoint();
            onClick(); 
        }
    }
    
    /** Copy the selected segment. */
    public void mouseReleased(MouseEvent e)
    {
        PropagationPopupMenu popupMenu = new PropagationPopupMenu(this);
        if (dragging) popupMenu.setOps(false);
        //Erase
        if (!dragging && pressed) popupMenu.setOps(true);
        popupMenu.show(view.table, e.getX(), e.getY());
        dragging = false;
        pressed = false;
    }

    /** Handle mouseDragged event. Select the cell. */
    public void mouseDragged(MouseEvent e)
    {
        if (pressed) {
            dragging = true;
            Point p = e.getPoint();
            int lengthX, lengthY;
            int y = anchor.y-p.y, x = anchor.x-p.x;
            int absX = Math.abs(staticAnchor.x-p.x), 
                absY = Math.abs(staticAnchor.y-p.y);
            lengthX = Math.abs(x)/cellWidth;
            lengthY = Math.abs(y)/cellHeight;
            if (absY < cellHeight && absX > cellWidth) {
                if (lengthX >= 1) {
                    draggedAcrossT(lengthX, x);
                    anchor = p;
                }
            } else if (absX < cellWidth &&  absY > cellHeight) {
                if (lengthY >= 1) {
                    draggedAcrossZ(lengthY, y);
                    anchor = p;
                } 
            } else if (absX >= cellWidth && absY >= cellHeight) {
                if (lengthX >= 1 || lengthY >= 1) {
                    dragged(lengthX, lengthY, x, y);
                    anchor = p;
                }
            }
        }
    }
    /** 
     * Handles the lost of focus on the timepoint text field.
     * If focus is lost while editing, then we don't consider the text 
     * currently displayed in the text field and we reset it to the current
     * value.
     */
    public void focusLost(FocusEvent e)
    {
        String start = ""+curStart, end = ""+curEnd;
        String startVal = view.startT.getText(), endVal = view.endT.getText();
        if (startVal == null || !startVal.equals(start))
            view.startT.setText(start);        
        if (endVal == null || !endVal.equals(end)) 
            view.endT.setText(end);
    }
    
    /** Handle events fired by JButtons. */
    public void actionPerformed(ActionEvent e)
    {
        int index = -1;
        try {
            index = Integer.parseInt(e.getActionCommand());
            switch (index) {
                case START_T:
                    handleStartChange(); break;
                case END_T:
                    handleEndChange(); break;
                case COPY_STACK:
                    copyStack(); break; 
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }

    ROIAgtCtrl getControl() { return control; }

    Color getSelectedColor() { return selectedColor; }
    
    /** 
     * Initializes the <code>width</code> and <code>height</code> of a cell. 
     * Note that they are similar.
     * Also initializes the number of rows and columns.
     */
    void initDimensions()
    {
        TableColumn col = view.table.getColumnModel().getColumn(0);
        cellWidth = col.getPreferredWidth();
        cellHeight = view.table.getRowHeight();
        numRows = view.table.getRowCount();
        numColumns = view.table.getColumnCount();
        tableWidth = numColumns*cellWidth;
        tableHeight = numRows*cellHeight;
    }
    
    void setSelectedPlane(int z, int t, int shapeType)
    {
        int row = numRows-1-z;
        setSelectedCell(row, t, shapeType);
        //setDefaultLocation();
        //ONLY Painted the cell.
        view.table.repaint(t*numColumns, row*numRows,cellWidth, cellHeight);
    }

    /** Set the color of the selected cell. */
    void setSelectedColor(Color c)
    {
       if (c == null) selectedColor = AssistantDialog.SELECTED_COLOR;
       else selectedColor = c; 
   }
  
    /** Attach listeners to the GUI component. */
    void attachListeners()
    {
        attachFieldListeners(view.startT, START_T);
        attachFieldListeners(view.endT, END_T);
        attachButtonListeners(view.copyStack, COPY_STACK);
        //table listener
        view.table.addMouseListener(this);
        view.table.addMouseMotionListener(this);
    }
    
    /** Copy the PlaneArea from last selected 2D-plane onto the new 2D-plane. */
    void copy()
    {
        int rows = numRows-1;
        //old must have a selection.
        PlaneArea pa = control.getPlaneArea(rows-oldRow, oldColumn);
        if (pa != null) {
            control.copyPlaneArea(pa, rows-curRow, curColumn);
            setSelectedPlane(rows-curRow, curColumn, 
                    ROIFactory.getLabelShapeType(pa)); 
        } else {
            UserNotifier un = control.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid selection", "No ROI on selected plane."); 
        }
        cancel();
    }
    
    /** Copy the selected interval. */
    void copySegments()
    {
        if (oldRow == curRow) copyAcrossT();
        else if (oldColumn == curColumn) copyAcrossZ();
        else if (oldColumn != curColumn && oldRow != curRow)
            copyAcrossZAndT();
        else {
            removeSelected();
            UserNotifier un = control.getRegistry().getUserNotifier();
            un.notifyInfo("Invalid interval", MSG); 
        }
    }

    /** Remove the selected plane from the selection. */
    void removeSelected()
    {
        if (curRow != -1 && curColumn != -1) {
            int z = numRows-1-curRow;
            control.removePlaneArea(z, curColumn);
            setSelectedCell(curRow, curColumn, ColoredLabel.NO_SHAPE);
            cancel();
        } 
    }
    
    /** Cancel action. */
    void cancel()
    {
        setCellColor(curRow, curColumn, AssistantDialog.DEFAULT_COLOR);
        view.table.repaint(curColumn*numColumns, curRow*numRows, cellWidth,
                cellHeight);
        setDefaultLocation();
    }

    /** Attach listener to a AbstractButton. */
    private void attachButtonListeners(AbstractButton button, int id)
    {
        button.setActionCommand(""+id);
        button.addActionListener(this); 
    }
   
    /** Attach listeners to a JField. */
    private void attachFieldListeners(JTextField field, int id)
    {
        field.setActionCommand(""+id);  
        field.addActionListener(this);
        field.addFocusListener(this);
    }
    
   /** Handle mouse pressed event. */
   private void onClick()
   {
       int row = view.table.getSelectedRow();
       if (row != -1) {
           oldRow = curRow = row;
           oldColumn = curColumn = view.table.getSelectedColumn(); 
           paintTable(curRow, curColumn);
       }
   }

   /** Copy interval across time. */
   private void copyAcrossT()
   {
       int from = oldColumn, to = curColumn;
       int z = numRows-1-curRow;
       if (to < from) {
           from = curColumn;
           to = oldColumn;
       }
       PlaneArea pa = control.getPlaneArea(z, from);
       if (pa == null) pa = control.getPlaneArea(z, to);
       if (pa != null) {
           control.copyAcrossT(pa, from, to, z);
           PlaneArea paNew;
           for (int j = from; j <= to; j++) {
               paNew = control.getPlaneArea(z, j);
               setSelectedCell(paNew, curRow, j);
           }
           setCellColor(curRow, curColumn, AssistantDialog.DEFAULT_COLOR);
           view.table.repaint(0, curRow*cellHeight, tableWidth, cellHeight);
           setDefaultLocation();
       } else
           handleSelectionError("No ROI on selected plane ("+z+","+from+
                                   ") or ("+z+","+to+").");
   }
   
   /** Copy interval across sections. */
   private void copyAcrossZ()
   {
       int xMin = oldRow, xMax = curRow;
       int rows = numRows-1;
       if (xMax < xMin) {
           xMax = oldRow;
           xMin = curRow;
       }
       int from = rows-xMin, to = rows-xMax;
       if (from > to) {
           int s = from;
           from = to;
           to = s;
       }
       PlaneArea pa = control.getPlaneArea(from, curColumn);
       if (pa == null) pa = control.getPlaneArea(to, curColumn);
       if (pa != null) {
           control.copyAcrossZ(pa, from, to, curColumn);
           PlaneArea paNew;
           int z;
           for (int i = xMin; i <= xMax; i++) {
               z = rows-i;
               paNew = control.getPlaneArea(z, curColumn);
               setSelectedCell(paNew, i, curColumn);
           }
           setCellColor(curRow, curColumn, AssistantDialog.DEFAULT_COLOR);
           view.table.repaint(curColumn*cellWidth, 0, cellWidth, tableHeight);
           setDefaultLocation();
       } else handleSelectionError("No ROI on selected plane ("+from+","
                           +curColumn+") or ("+to+","+curColumn+").");
   }
   
   /** Copy across z-sections and timepoints. */
   private void copyAcrossZAndT()
   {
       //interval across Z
       int xMin = oldRow, xMax = curRow;
       int rows = numRows-1;
       if (xMax < xMin) {
           xMax = oldRow;
           xMin = curRow;
       }
       int fromZ = rows-xMin, toZ = rows-xMax;
       if (fromZ > toZ) {
           int s = fromZ;
           fromZ = toZ;
           toZ = s;
       }
       //interval across T
       int fromT = oldColumn, toT = curColumn;
       if (toT < fromT) {
           fromT = curColumn;
           toT = oldColumn;
       }
       int z = numRows-1-oldRow;
       PlaneArea pa = control.getPlaneArea(z, oldColumn);
       if (pa == null) {
           z =  numRows-1-curRow;
           pa = control.getPlaneArea(z, curColumn);
       }
       
       if (pa != null) {
           control.copyAcrossZAndT(pa, fromZ, toZ, fromT, toT);
           PlaneArea paNew;
           for (int j = fromT; j <= toT; j++) {
               for (int i = xMin; i <= xMax; i++) {
                   z = rows-i;
                   paNew = control.getPlaneArea(z, j);
                   setSelectedCell(paNew, i, j);
               }
           }
           setCellColor(curRow, curColumn, AssistantDialog.DEFAULT_COLOR);
           view.table.repaint(curColumn*cellWidth, 0, cellWidth, tableHeight);
           setDefaultLocation();
       } else handleSelectionError("No ROI on selected plane ("+fromZ+","
                           +fromT+") or ("+toZ+","+toT+").");
       //cancel();
   }
   
   /** Copy a selected stack across time. */
   private void copyStack()
   {
       int from = Integer.parseInt(view.startT.getText()),
           to = Integer.parseInt(view.endT.getText());
       if (view.allTimepoints.isSelected()) 
           copyStackAllTimepoints(from, to);
       else copyStackEndsTimepoints(from, to);
   }
   
   /** 
    * Copy stack for timepoints between <code>from</code> 
    * and <code>to</code>.
    */
   private void copyStackAllTimepoints(int from, int to)
   {
       //int rows = numRows;
       control.copyStackAcrossT(from, to);
       PlaneArea pa;
       int z;
       for (int i = 0; i < numRows; i++) {
           z = numRows-i-1;
           for (int j = from; j <= to; j++) {
               pa = control.getPlaneArea(z, j);
               setSelectedCell(pa, i, j);
           }
       }
       view.table.repaint(from*numColumns, 0, (to-from+1)*cellWidth, 
                       numRows*cellHeight);
   }

   /** Copy stack from timepoint <code>from</code> into <code>to</code>. */
   private void copyStackEndsTimepoints(int from, int to)
   {
       control.copyStack(from, to);
       int rows = numRows;
       PlaneArea pa;
       int z;
       for (int i = 0; i < rows; i++) {
           z = rows-i-1;
           pa = control.getPlaneArea(z, from);
           setSelectedCell(pa, i, from);
           setSelectedCell(pa, i, to);
       }
       view.table.repaint(from*numColumns, 0, (to-from+1)*cellWidth, 
                           numRows*cellHeight);
   }
   
   /** Paint the table according to the selected cell. */
   private void paintRowDraggedCell(int row, int column)
   {
       ColoredLabel cl;
       for (int i = 0; i < numColumns; i++) {
           cl = (ColoredLabel) (view.table.getValueAt(row, i));
           if (i != column) cl.setBackground(AssistantDialog.DEFAULT_COLOR);
           else cl.setBackground(selectedColor); 
       }
       view.table.repaint(0, row*cellHeight, tableWidth, cellHeight);
   }
   
   /** Paint the table according to the selected cell. */
   private void paintColumnDraggedCell(int row, int column)
   {
       ColoredLabel cl;
       for (int i = 0; i < numRows; i++) {
           cl = (ColoredLabel) (view.table.getValueAt(i, column));
           if (i != row) cl.setBackground(AssistantDialog.DEFAULT_COLOR);
           else cl.setBackground(selectedColor);
       }
       view.table.repaint(column*cellWidth, 0, cellWidth, tableHeight);
   }
   
   private void draggedAcrossT(int s, int direction)
   {
       curRow = oldRow;
       if (direction < 0) curColumn = curColumn+s;  
       else curColumn = curColumn-s; 
       paintRowDraggedCell(curRow, curColumn);
   }
   
   private void draggedAcrossZ(int s, int direction)
   {
       curColumn = oldColumn;
       if (direction < 0) curRow = curRow+s;
       else curRow = curRow-s;
       paintColumnDraggedCell(curRow, curColumn);
   }

   private void dragged(int x, int y, int directionX, int directionY) 
   {
       if (directionX < 0) curColumn = curColumn+x; 
       else  curColumn = curColumn-x; 
       if (directionY < 0) curRow = curRow+y; 
       else  curRow = curRow-y;
       paintTable(curRow, curColumn);
   }

   void setSelectedCell(PlaneArea pa, int row, int column)
   {
       ColoredLabel cl = (ColoredLabel) (view.table.getValueAt(row, column));
       cl.setShapeType(ROIFactory.getLabelShapeType(pa));
   }
  
   /** Set the color of a cell. */
   private void setSelectedCell(int row, int column, int shapeType)
   {
       ColoredLabel 
           cl = (ColoredLabel) (view.table.getValueAt(row, column));
       cl.setShapeType(shapeType);
   }
   
   private void setCellColor(int row, int column, Color c)
   {
       ColoredLabel 
           cl = (ColoredLabel) (view.table.getValueAt(row, column));
       cl.setBackground(c);
   }
   
   /** Paint the table according to the selected cell. */
   private void paintTable(int row, int column)
   {
       ColoredLabel cl;
       for (int i = 0; i < numRows; i++) {
           for (int j = 0; j < numColumns; j++) {
               cl = (ColoredLabel) (view.table.getValueAt(i, j));
               if (row == i && column == j) 
                   cl.setBackground(selectedColor);
               else  cl.setBackground(AssistantDialog.DEFAULT_COLOR);
           }
       }
       view.table.repaint();
   }

   /** Display message and reset the color. */
   private void handleSelectionError(String msg)
   {
       cancel();
       UserNotifier un = control.getRegistry().getUserNotifier();
       un.notifyInfo("Invalid selection", msg); 
   }
   
   /** Handle start text changed event. */
   private void handleStartChange()
   {
       boolean valid = false;
       int val = 0;
       int valEnd = view.table.getColumnCount();
       try {
           val = Integer.parseInt(view.startT.getText());
           valEnd = Integer.parseInt(view.endT.getText());
           if (0 <= val && val <= valEnd) valid = true;
       } catch(NumberFormatException nfe) {}
       if (!valid) {
           view.startT.selectAll();
           UserNotifier un = control.getRegistry().getUserNotifier();
           un.notifyInfo("Invalid timepoint", 
               "Please enter a value between 0 and "+valEnd);
       } else curStart = val;
   }
   
   /** Handle end text changed event. */
   private void handleEndChange()
   {
       boolean valid = false;
       int val = 0;
       int valStart = 0;
       int numColumns =  view.table.getColumnCount()-1;
       try {
           val = Integer.parseInt(view.endT.getText());
           valStart = Integer.parseInt(view.startT.getText());
           if (valStart < val && val <= numColumns) valid = true;
       } catch(NumberFormatException nfe) {}
       if (!valid) {
           view.endT.selectAll();
           UserNotifier un = control.getRegistry().getUserNotifier();
           un.notifyInfo("Invalid timepoint", 
               "Please enter a value between "+ valStart+" and "+numColumns);
       } else curEnd = val;
   }
   
   
   /** Set the default. */
   private void setDefaultLocation()
   {
       oldRow = oldColumn = -1;
       curRow = curColumn = -1;
   }
    
    /** 
     * Required by {@link FocusListener} I/F but not actually needed in 
     * our case, no op implementation.
     */ 
    public void focusGained(FocusEvent e) {}
    
    /** 
     * Required by {@link MouseMotionListener} I/F but not actually needed in 
     * our case, no op implementation.
     */ 
    public void mouseMoved(MouseEvent e) {}
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in 
     * our case, no op implementation.
     */   
    public void mouseClicked(MouseEvent e) {}
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in 
     * our case, no op implementation.
     */   
    public void mouseEntered(MouseEvent e) {}
    
    /** 
     * Required by {@link MouseListener} I/F but not actually needed in 
     * our case, no op implementation.
     */  
    public void mouseExited(MouseEvent e) {}

}
