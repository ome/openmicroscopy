/*
 * org.openmicroscopy.shoola.agents.roi.results.stats.graphic.SelectionPane
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

package org.openmicroscopy.shoola.agents.roi.results.stats.graphic;

//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.roi.IconManager;
import org.openmicroscopy.shoola.agents.roi.ROIAgtUIF;
import org.openmicroscopy.shoola.agents.roi.results.stats.StatsResultsPaneMng;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.util.ui.ColoredButton;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.table.TableComponent;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellRenderer;
import org.openmicroscopy.shoola.util.ui.table.TableHeaderTextAndIcon;
import org.openmicroscopy.shoola.util.ui.table.TableIconRenderer;
import org.openmicroscopy.shoola.util.ui.table.TableSorter;

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
class SelectionPane
    extends JPanel
    //implements IColorChooser
{
    
    private static final Dimension      SCROLLPANE_DIM = new Dimension(130, 70);
    
    public static final Color           BUTTON_BORDER = Color.BLACK;
    
    private static final    Color[]     defaultColors;
    
    private static final int            RED = 0, GREEN = 1, BLUE = 2, 
                                        PINK = 3;
    private static final int            MODULO = 4;
    
    TableComponent                      table;
    
    JButton                             selectAll, undo;
    
    private StatsResultsPaneMng         mng;
    
    private SelectionPaneMng            manager;

    static {
        defaultColors = new Color[MODULO];
        defaultColors[RED] = Color.RED;
        defaultColors[GREEN] = Color.GREEN;
        defaultColors[BLUE] = Color.BLUE;
        defaultColors[PINK] = Color.PINK;
    }
    
    SelectionPane(List toSelect, String name, StatsResultsPaneMng mng)
    {
        this.mng = mng;
        manager = new SelectionPaneMng(this);
        initComponents(name);
        buildTable(toSelect, name);
        buildGUI();
    }

    private void initComponents(String name)
    {
        IconManager im = IconManager.getInstance(mng.getRegistry());
        selectAll = new JButton(im.getIcon(IconManager.APPLY));
        selectAll.setToolTipText(
                UIUtilities.formatToolTipText("Select all "+name));
        undo = new JButton(im.getIcon(IconManager.UNDO));
        undo.setToolTipText(
                UIUtilities.formatToolTipText("Deselect all "+name));
        manager.attachButtonListener(undo, SelectionPaneMng.DESELECT_ALL);
        manager.attachButtonListener(selectAll, SelectionPaneMng.SELECT_ALL);
    }
    
    private void buildTable(List toSelect, String name)
    {
        table = new TableComponent(toSelect.size(), 3);
        JPanel p;
        ColoredButton button;
        int w = table.getColumnModel().getColumn(2).getPreferredWidth();
        int h = table.getRowHeight();
        Dimension d = new Dimension(Math.min(w, h)-3, Math.min(w, h)-3);
        Iterator i = toSelect.iterator();
        int j, k;
        k = 0;
        Integer l;
        while (i.hasNext()) {
            l = (Integer) i.next();
            table.setValueAt(l, k, ContextDialog.NAME);
            table.setValueAt(Boolean.FALSE, k, ContextDialog.BOOLEAN);
            j = k%MODULO;
            button = getButton(defaultColors[j]);
            manager.attachButtonListener(button, k);
            p = buildComponentPanel(button, d);
            table.setValueAt(p, k, ContextDialog.BUTTON);
            k++;
        }
        TableSorter sorter = new TableSorter(table.getModel());
        table.setModel(sorter);
        sorter.addMouseListenerToHeaderInTable(table);
        sorter.sortByColumn(0);
        setTableHeader(name);
    }
    
    private JPanel buildComponentPanel(ColoredButton c, Dimension d)
    {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.setOpaque(false);
        c.setPreferredSize(d);
        p.add(c);
        return p;
    }
    
    /** Set the layout of the table. */
    private void setTableHeader(String s)
    {
        IconManager im = IconManager.getInstance(getRegistry());
        TableIconRenderer rnd = new TableIconRenderer();
        TableColumnModel tcm = table.getTableHeader().getColumnModel();
        Icon up = im.getIcon(IconManager.UP), 
            down = im.getIcon(IconManager.DOWN);
        setTableColumnHeader(tcm.getColumn(ContextDialog.NAME), rnd, up, down, s);
        setTableColumnHeader(tcm.getColumn(ContextDialog.BOOLEAN), rnd, up, down,
                                "");
        setTableColumnHeader(tcm.getColumn(ContextDialog.BUTTON), rnd, null, null,
                                "");
        table.setDefaultRenderer(JComponent.class, 
                                new TableComponentCellRenderer());
        table.setDefaultEditor(JComponent.class, 
                                new TableComponentCellEditor());
    }
    
    private void setTableColumnHeader(TableColumn tc, TableIconRenderer rnd,
                                        Icon up, Icon down, String s)
    {
        tc.setHeaderRenderer(rnd);
        TableHeaderTextAndIcon txt = new TableHeaderTextAndIcon(s, up, down, 
                                "Order by "+s);
        tc.setHeaderValue(txt);
    }
    
    private ColoredButton getButton(Color c)
    {
        ColoredButton colorButton = new ColoredButton();
        colorButton.setBorder(BorderFactory.createLineBorder(BUTTON_BORDER));
        colorButton.setBackground(c);
        return colorButton;
    }

    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(SCROLLPANE_DIM);
        scrollPane.setSize(SCROLLPANE_DIM);
        add(UIUtilities.buildComponentPanel(scrollPane));
        add(UIUtilities.buildComponentPanel(buildBar()));
    }

    private JToolBar buildBar()
    {
        JToolBar bar = new JToolBar();
        bar.setBorder(BorderFactory.createEtchedBorder());
        bar.setFloatable(true);
        bar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        bar.add(selectAll);
        bar.add(Box.createRigidArea(ROIAgtUIF.HBOX));
        bar.add(undo);
        return bar;
    }
    
    /** Implemented as specified by I/F. */
    public JFrame getReferenceFrame()
    {
        return mng.getReferenceFrame();
    }

    /** Implemented as specified by I/F. */
    public void setColor(int i, Color color)
    {
       manager.setColor(i, color);
    }

    /** Implemented as specified by I/F. */
    public Registry getRegistry() { return mng.getRegistry(); }
    
}
