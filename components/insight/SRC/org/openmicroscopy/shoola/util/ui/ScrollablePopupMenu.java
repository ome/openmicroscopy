/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui;


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

/**
 * Scrollable menu.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class ScrollablePopupMenu
    extends JPopupMenu
{

    /** The default number of visible rows.*/
    public static final int ROWS = 30;

    /** The number of visible rows.*/
    protected int maximumVisibleRows;

    /** The vertical scroll bar.*/
    private JScrollBar verticalBar;
    
    /** Initializes the listeners for the menu.*/
    private void initListeners()
    {
        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent event) {
                JScrollBar bar = getVerticalScrollBar();
                int amount;
                if (event.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                    amount = event.getUnitsToScroll()*bar.getUnitIncrement();
                } else {
                    if (event.getWheelRotation() < 0) {
                        amount = -bar.getBlockIncrement();
                    } else {
                        amount = bar.getBlockIncrement();
                    }
                }
                bar.setValue(bar.getValue()+amount);
                event.consume();
            }
        });
    }

    /** Creates a new instance.*/
    public ScrollablePopupMenu()
    {
        this(ROWS, SwingUtilities.RIGHT);
    }

    /**
     * Creates a new instance.
     *
     * @param location The location of the scrollbar.
     */
    public ScrollablePopupMenu(int location)
    {
        this(ROWS, location);
    }

    /**
     * Creates a new instance.
     *
     * @param visibleRows The number of visible rows.
     * @param location The location of the scrollbar.
     */
    public ScrollablePopupMenu(int visibleRows, int location)
    {
        super.add(getVerticalScrollBar());
        if (visibleRows <= 0) visibleRows = ROWS;
        maximumVisibleRows = visibleRows;
        setLayout(new ScrollPopupMenuLayout(location));
        initListeners();
    }

    /**
     * Initializes or recycles the vertical scroll bar.
     *
     * @return See above.
     */
    protected JScrollBar getVerticalScrollBar()
    {
        if (verticalBar == null) {
            verticalBar = new JScrollBar(JScrollBar.VERTICAL);
            verticalBar.addAdjustmentListener(new AdjustmentListener() {

                public void adjustmentValueChanged(AdjustmentEvent arg0) {
                    doLayout();
                    repaint();
                }
            });
            verticalBar.setVisible(false);
        }
        return verticalBar;
    }

    /**
     * Returns the maximum number of visible rows.
     *
     * @return See above
     */
    public int getMaximumVisibleRows() { return maximumVisibleRows; }

    /**
     * Sets the maximum number of rows.
     *
     * @param rows The value to set.
     */
    public void setMaximumVisibleRows(int rows)
    {
        this.maximumVisibleRows = rows;
    }

    /**
     * Overridden so the scrollbar is not removed.
     */
    public void removeAll()
    {
        super.removeAll();
        super.add(getVerticalScrollBar());
    }

    /**
     * Overridden to the paint the components.
     * @see JPopupMenu#paintChildren(Graphics)s
     */
    public void paintChildren(Graphics g)
    {
        super.paintChildren(g);
        Insets insets = getInsets();
        g.clipRect(insets.left, insets.top, getWidth(),
                getHeight()-insets.top-insets.bottom);
    }

    /**
     * Overridden to make the scrollbar visible.
     * @see JPopupMenu#addImpl(Component, Object, int)
     */
    protected void addImpl(Component comp, Object constraints, int index)
    {
        super.addImpl(comp, constraints, index);
        if (maximumVisibleRows < getComponentCount()-1) {
            getVerticalScrollBar().setVisible(true);
        }
    }

    /**
     * Overridden to size the components.
     * @see JPopupMenu#show(Component invoker, int x, int y)
     */
    public void show(Component invoker, int x, int y)
    {
        JScrollBar scrollBar = getVerticalScrollBar();
        if (scrollBar.isVisible()) {
            int extent = 0;
            int max = 0;
            int i = 0;
            int unit = -1;
            int width = 0;
            for (Component comp : getComponents()) {
                if (!(comp instanceof JScrollBar)) {
                    Dimension preferredSize = comp.getPreferredSize();
                    width = Math.max(width, preferredSize.width);
                    if (unit < 0) {
                        unit = preferredSize.height;
                    }
                    if (i++ < maximumVisibleRows) {
                        extent += preferredSize.height;
                    }
                    max += preferredSize.height;
                }
            }

            Insets insets = getInsets();
            int widthMargin = insets.left+insets.right;
            int heightMargin = insets.top+insets.bottom;
            scrollBar.setUnitIncrement(unit);
            scrollBar.setBlockIncrement(extent);
            scrollBar.setValues(0, heightMargin+extent, 0, heightMargin+max);

            width += scrollBar.getPreferredSize().width+widthMargin;
            int height = heightMargin + extent;

            setPopupSize(new Dimension(width, height));
        }

        super.show(invoker, x, y);
    }

    /**
     * Inner class used to layout the components.
     */
    protected static class ScrollPopupMenuLayout
        implements LayoutManager
   {

        /** The location of the scrollbar.*/
        private int location = SwingUtilities.RIGHT;

        /**
         * Creates a default instance.
         * The scrollbar is displayed on the right.
         */
        ScrollPopupMenuLayout()
        {
            this(SwingUtilities.RIGHT);
        }

        /**
         * Creates a new instance.
         *
         * @param location The location of the scrollbar.
         */
        ScrollPopupMenuLayout(int location)
        {
            switch (location) {
            case SwingUtilities.RIGHT:
            case SwingUtilities.LEFT:
                this.location = location;
                break;
            default:
                location = SwingUtilities.RIGHT;
            }
        }

        /**
         * Required by the {@link LayoutManager} I/F but no-operation in
         * our case.
         * @see LayoutManager#addLayoutComponent(String, Component)
         */
        public void addLayoutComponent(String name, Component comp) {}

        /**
         * Required by the {@link LayoutManager} I/F but no-operation in
         * our case.
         * @see LayoutManager#removeLayoutComponent(Component)
         */
        public void removeLayoutComponent(Component comp) {}

        /**
         * Sets the preferred size.
         * @see LayoutManager#preferredLayoutSize(Container)
         */
        public Dimension preferredLayoutSize(Container parent)
        {
            int visibleAmount = Integer.MAX_VALUE;
            Dimension dim = new Dimension();
            for (Component comp :parent.getComponents()){
                if (comp.isVisible()) {
                    if (comp instanceof JScrollBar){
                        JScrollBar scrollBar = (JScrollBar) comp;
                        visibleAmount = scrollBar.getVisibleAmount();
                    }
                    else {
                        Dimension pref = comp.getPreferredSize();
                        dim.width = Math.max(dim.width, pref.width);
                        dim.height += pref.height;
                    }
                }
            }

            Insets insets = parent.getInsets();
            dim.height = Math.min(dim.height+insets.top+insets.bottom,
                    visibleAmount);
            return dim;
        }

        /**
         * Sets the minimum size.
         * @see LayoutManager#minimumLayoutSize(Container)
         */
        public Dimension minimumLayoutSize(Container parent)
        {
            int visibleAmount = Integer.MAX_VALUE;
            Dimension dim = new Dimension();
            for (Component comp : parent.getComponents()) {
                if (comp.isVisible()){
                    if (comp instanceof JScrollBar) {
                        JScrollBar scrollBar = (JScrollBar) comp;
                        visibleAmount = scrollBar.getVisibleAmount();
                    } else {
                        Dimension min = comp.getMinimumSize();
                        dim.width = Math.max(dim.width, min.width);
                        dim.height += min.height;
                    }
                }
            }

            Insets insets = parent.getInsets();
            dim.height = Math.min(dim.height+insets.top+insets.bottom,
                    visibleAmount);
            return dim;
        }

        /**
         * Lays out the components.
         * @see LayoutManager#layoutContainer(Container)
         */
        public void layoutContainer(Container parent)
        {
            Insets insets = parent.getInsets();

            int width = parent.getWidth()-insets.left-insets.right;
            int height = parent.getHeight()-insets.top-insets.bottom;

            int x = insets.left;
            int y = insets.top;
            int position = 0;

            for (Component comp : parent.getComponents()) {
                if (comp instanceof JScrollBar && comp.isVisible()) {
                    JScrollBar scrollBar = (JScrollBar) comp;
                    Dimension dim = scrollBar.getPreferredSize();
                    switch (location) {
                    case SwingUtilities.RIGHT:
                        scrollBar.setBounds(x+width-dim.width, y, dim.width,
                                height);
                        break;
                    case SwingUtilities.LEFT:
                        scrollBar.setBounds(x+width-dim.width, y, dim.width,
                                height);
                    }
                    width -= dim.width;
                    position = scrollBar.getValue();
                }
            }

            y -= position;
            for (Component comp : parent.getComponents()) {
                if (!(comp instanceof JScrollBar) && comp.isVisible()) {
                    Dimension pref = comp.getPreferredSize();
                    comp.setBounds(x, y, width, pref.height);
                    y += pref.height;
                }
            }
        }
   }
}
