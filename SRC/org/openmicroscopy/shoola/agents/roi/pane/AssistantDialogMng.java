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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.ROIAgtCtrl;
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
    implements ActionListener, FocusListener
{
    
    /** Action ID, to copy segment. */
    private static final int        COPY = 0, COPY_STACK = 1, UNDO = 2,
                                    START_T = 3, END_T = 4, CLOSE = 5,
                                    COPY_SEGMENT = 6, ERASE= 7;

    private static final String     MSG = "To copy across Z, select two " +
            "points in the same column. To copy across T, select two " +
            "points in the same row.";
    
    private int                     curRow, curColumn, oldRow, oldColumn;
    
    private int                     curStart, curEnd;
    
    private Color                   selectedColor, alphaSelectedColor;
    
    private AssistantDialog         view;
    
    private ROIAgtCtrl              control;
    
    public AssistantDialogMng(AssistantDialog view, ROIAgtCtrl control, 
                            int maxT)
    {
        this.view = view;
        this.control = control;
        curStart = 0;
        curEnd = maxT;
    }
    
    void setDefault(int z, int t, Color c)
    {
        setSelectedColor(c);
        oldRow = oldColumn = -1;
        curRow = z;
        curColumn = t;
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
        int index = Integer.parseInt(e.getActionCommand());
        try {
            switch (index) {
                case START_T:
                    handleStartChange(); break;
                case END_T:
                    handleEndChange(); break;
                case COPY:
                    copy(); break;
                case COPY_SEGMENT:
                    copySegment(); break;
                case COPY_STACK:
                    copyStack(); break; 
                case CLOSE:
                    closeWidget(); break;
                case ERASE:
                    removeSelected(); break;
                case UNDO:
                    setDefaultLocation();
                    colorAllCells(AssistantDialog.DEFAULT_COLOR); 
            }
        } catch(NumberFormatException nfe) { 
            throw new Error("Invalid Action ID "+index, nfe); 
        }
    }
   
    void removeCurrentPlane(int z, int t)
    {
        int row = view.table.getRowCount()-1-z;
        curColumn = t;
        setPlaneColor(row, t, AssistantDialog.DEFAULT_COLOR);
        curRow = oldRow;
        curColumn = oldColumn;
        if (curRow != -1 && curColumn != -1)
            setPlaneColor(curRow, curColumn, selectedColor);
        view.table.repaint();
    }
    
    void setCurrentPlane(int z, int t)
    {
        curRow = view.table.getRowCount()-1-z;
        curColumn = t;
        ColoredLabel 
        cl = (ColoredLabel) (view.table.getValueAt(curRow, curColumn));
        cl.setBackground(selectedColor);
        view.table.repaint();
    }
    
    /** Set the color of the selected cell. */
    void setSelectedColor(Color c)
    {
       if (c == null) {
           selectedColor = AssistantDialog.SELECTED_COLOR;
           alphaSelectedColor = AssistantDialog.ALPHA_SELECTED;
       } else {
           selectedColor = c;
           alphaSelectedColor = new Color(c.getRed(), c.getGreen(), 
                                   c.getBlue(), 100);
       } 
   }
   
   Color getAlphaSelectedColor() { return alphaSelectedColor; }
   
   Color getSelectedColor() { return selectedColor; }
   
   /** Attach listeners to the GUI component. */
   void attachListeners()
   {
       attachFieldListeners(view.startT, START_T);
       attachFieldListeners(view.endT, END_T);
       attachButtonListeners(view.close, CLOSE);
       attachButtonListeners(view.copy, COPY);
       attachButtonListeners(view.copyStack, COPY_STACK);
       attachButtonListeners(view.copySegment, COPY_SEGMENT);
       attachButtonListeners(view.undo, UNDO);
       attachButtonListeners(view.erase, ERASE);
       view.table.addMouseListener(new MouseAdapter() {
           public void mousePressed(MouseEvent e) { onClick(); }
       });
   }

   /** Attach listener to a JButton. */
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
           oldRow = curRow;
           oldColumn = curColumn;
           curRow = row;
           curColumn = view.table.getSelectedColumn(); 
           paintTable(curRow, curColumn);
       }
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

   /** Close the widget. */
   private void closeWidget()
   {
       view.dispose();
       view.setVisible(false);
   }
   
   /** Copy the PlaneArea from last selected 2D-plane onto the new 2D-plane. */
   private void copy()
   {
       if (oldRow == -1 || oldColumn == -1) {
           UserNotifier un = control.getRegistry().getUserNotifier();
           un.notifyInfo("Invalid selection", "You must fisrt select a plane."); 
       } else {
           int rows = view.table.getRowCount()-1;
           //old must have a selection.
           PlaneArea pa = control.getPlaneArea(rows-oldRow, oldColumn);
           if (pa != null)
               control.copyPlaneArea(pa, rows-curRow, curColumn);
           else {
               UserNotifier un = control.getRegistry().getUserNotifier();
               un.notifyInfo("Invalid selection", "No ROI on selected plane."); 
           }
       }
   }
   
   /** Copy the selected interval. */
   private void copySegment()
   {
       if (oldRow == curRow && oldRow != -1) copyAcrossT();
       else if (oldColumn == curColumn && oldColumn != -1) copyAcrossZ();
       else {
           removeSelected();
           UserNotifier un = control.getRegistry().getUserNotifier();
           un.notifyInfo("Invalid interval", MSG); 
       }
   }

   /** Copy interval across time. */
   private void copyAcrossT()
   {
       int xMin = oldColumn, xMax = curColumn;
       int z = view.table.getRowCount()-1-curRow;
       int from = xMin, to = xMax;
       if (xMax < xMin) {
           xMax = oldColumn;
           xMin = curColumn;
           from = xMax;
           to = xMin;
       }
       PlaneArea pa = control.getPlaneArea(z, from);
       if (pa == null) pa = control.getPlaneArea(z, to);
       if (pa != null) {
           ColoredLabel cl;
           for (int i = xMin+1; i < xMax; i++) {
               cl = ((ColoredLabel) (view.table.getValueAt(curRow, i)));
               cl.setBackground(alphaSelectedColor);
           } 
           view.table.repaint();
           control.copyAcrossT(pa, from, to, z);
       } else {
           UserNotifier un = control.getRegistry().getUserNotifier();
           un.notifyInfo("Invalid selection", "No ROI on selected plane " +
                   "("+z+","+from+") or ("+z+","+to+")."); 
           //remove the two selected planes.
           setPlaneColor(oldRow, oldColumn, AssistantDialog.DEFAULT_COLOR);
           setPlaneColor(curRow, curColumn, AssistantDialog.DEFAULT_COLOR);
           view.table.repaint();
           setDefaultLocation();
           
       }
   }
   
   /** Copy interval across sections. */
   private void copyAcrossZ()
   {
       int xMin = oldRow, xMax = curRow;
       int rows = view.table.getRowCount();
       if (xMax < xMin) {
           xMax = oldRow;
           xMin = curRow;
       }
       int from = rows-xMin-1, to = rows-xMax-1;
       if (from > to) {
           int s = from;
           from = to;
           to = s;
       }
       PlaneArea pa = control.getPlaneArea(from, curColumn);
       if (pa == null) pa = control.getPlaneArea(to, curColumn);
       if (pa != null) {
           ColoredLabel cl;
           for (int i = xMin+1; i < xMax; i++) {
               cl = ((ColoredLabel) (view.table.getValueAt(i, curColumn)));
               cl.setBackground(alphaSelectedColor);
           }
           view.table.repaint();
           control.copyAcrossZ(pa, from, to, curColumn);
       } else {
           UserNotifier un = control.getRegistry().getUserNotifier();
           un.notifyInfo("Invalid selection", "No ROI on selected plane " +
                "("+from+","+curColumn+") or ("+to+","+curColumn+")."); 
           //remove the two selected planes.
           setPlaneColor(oldRow, oldColumn, AssistantDialog.DEFAULT_COLOR);
           setPlaneColor(curRow, curColumn, AssistantDialog.DEFAULT_COLOR);
           view.table.repaint();
           setDefaultLocation();
       }
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
   
   private void copyStackAllTimepoints(int from, int to)
   {
       int numbRows = view.table.getRowCount()-1;
       control.copyStackAcrossT(from, to);
       PlaneArea pa;
       int z;
       for (int i = 0; i < numbRows; i++) {
           z = numbRows-i;
           for (int j = from; j <= to; j++) {
               pa = control.getPlaneArea(z, j);
               setCellColor(pa, i, j);
           }
       }
       view.table.repaint();
   }

   private void copyStackEndsTimepoints(int from, int to)
   {
       control.copyStack(from, to);
       int numbRows = view.table.getRowCount()-1;
       PlaneArea pa;
       int z;
       for (int i = 0; i < numbRows; i++) {
           z = numbRows-i;
           pa = control.getPlaneArea(z, from);
           setCellColor(pa, i, from);
           setCellColor(pa, i, to);
       }
       view.table.repaint(); 
   }
   
   private void setCellColor(PlaneArea pa, int i, int j)
   {
       ColoredLabel cl = (ColoredLabel) (view.table.getValueAt(i, j));
       if (pa != null)
           cl.setBackground(alphaSelectedColor);
       else cl.setBackground(AssistantDialog.DEFAULT_COLOR);
   }
   
   /** Remove the selected plane from the selection. */
   private void removeSelected()
   {
       if (curRow != -1 && curColumn != -1) {
           int z = view.table.getRowCount()-1-curRow;
           control.removePlaneArea(z, curColumn);
           setPlaneColor(curRow, curColumn, AssistantDialog.DEFAULT_COLOR);
           curRow = oldRow;
           curColumn = oldColumn;
           if (curRow != -1 && curColumn != -1)
               setPlaneColor(curRow, curColumn, selectedColor);
           view.table.repaint();
       }
   }
   
   private void setPlaneColor(int row, int column, Color c)
   {
       ColoredLabel 
           cl = (ColoredLabel) (view.table.getValueAt(row, column));
       cl.setBackground(c);
   }
   
   /** Color all the cells. */
   private void colorAllCells(Color c)
   {
       int numbRows = view.table.getRowCount(), 
           numColumns = view.table.getColumnCount();
       ColoredLabel cl;
       for (int i = 0; i < numbRows; i++) {
           for (int j = 0; j < numColumns; j++) {
               cl = (ColoredLabel) (view.table.getValueAt(i, j));
               cl.setBackground(c);
           }
       }
       view.table.repaint();
   }
   
   /** Paint the table according to the selected cell. */
   private void paintTable(int row, int column)
   {
       int numbRows = view.table.getRowCount(), 
       numColumns = view.table.getColumnCount();
       ColoredLabel cl;
       for (int i = 0; i < numbRows; i++) {
           for (int j = 0; j < numColumns; j++) {
               cl = (ColoredLabel) (view.table.getValueAt(i, j));
               if (cl.getBackground() != AssistantDialog.DEFAULT_COLOR) 
                   cl.setBackground(alphaSelectedColor);
               if (row == i && column == j)  
                   cl.setBackground(selectedColor);
           }
       }
       view.table.repaint();
   }
   
   private void setDefaultLocation()
   {
       oldRow = oldColumn = -1;
       curRow = curColumn = -1;
   }

   /** 
    * Required by I/F but not actually needed in our case, no op 
    * implementation.
    */ 
   public void focusGained(FocusEvent e) {}
   
}
