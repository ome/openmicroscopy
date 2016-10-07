/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.WellFieldsCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import omero.gateway.model.DataObject;

import org.jdesktop.swingx.JXBusyLabel;
import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Display all the fields for a given well; based on GridBagLayout
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
class GridFieldCanvas extends WellFieldsCanvas {

    /**
     * Creates a new instance.
     * 
     * @param parent
     *            The parent of the canvas.
     */
    public GridFieldCanvas(WellFieldsView parent) {
        super(parent);
        setDoubleBuffered(true);
        setBackground(UIUtilities.BACKGROUND);
        setLayout(new GridBagLayout());
        refreshUI();
    }

    /**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * 
     * @see WellFieldsCanvas#setLoading(boolean)
     */
    public void setLoading(boolean loading) {
        removeAll();
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        JXBusyLabel busyLabel = new JXBusyLabel(
                new Dimension(UIUtilities.DEFAULT_ICON_WIDTH,
                        UIUtilities.DEFAULT_ICON_HEIGHT));
        busyLabel.setBusy(true);
        add(busyLabel, c);
        // add empty component to fill up space
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(UIUtilities.createComponent(JPanel.class, UIUtilities.BACKGROUND),
                c);

        validate();
        repaint();
        parent.revalidate();
    }

    /**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * 
     * @see WellFieldsCanvas#refreshUI()
     */
    public void refreshUI() {
        removeAll();
        setLayout(new GridBagLayout());

        List<WellSampleNode> l = parent.getNodes();
        if (l == null)
            return;

        // filter out duplicates
        HashSet<Long> ids = new HashSet<Long>();
        Iterator<WellSampleNode> it = l.iterator();
        while (it.hasNext()) {
            WellSampleNode n = it.next();
            long id = ((DataObject) n.getHierarchyObject()).getId();
            if (ids.contains(id))
                it.remove();
            else
                ids.add(id);
        }

        // sort
        Collections.sort(l, new Comparator<WellSampleNode>() {
            @Override
            public int compare(WellSampleNode o1, WellSampleNode o2) {
                int res = 0;

                if (o1.getRow() == o2.getRow()) {
                    if (o1.getColumn() > o2.getColumn())
                        res = -1;
                    else if (o1.getColumn() < o2.getColumn())
                        res = 1;
                } else if (o1.getRow() > o2.getRow())
                    res = -1;
                else
                    res = 1;

                return -res;
            }
        });

        Map<String, GridBagConstraints> cs = new HashMap<String, GridBagConstraints>();
        int y = 0;
        GridBagConstraints c = null;
        for (WellSampleNode n : l) {
            String key = n.getRow() + "_" + n.getColumn();
            c = cs.get(key);
            if (c == null) {
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.NONE;
                c.gridx = 0;
                c.gridy = y++;
                c.weightx = 0;
                c.weighty = 0;
                c.insets = new Insets(2, 2, 4, 4);
                c.anchor = GridBagConstraints.NORTHWEST;
                cs.put(key, c);
            }
            FieldDisplay fd = new FieldDisplay(n);
            add(fd, c);
            c.gridx++;
        }

        // add empty component to fill up space
        c.gridy++;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(UIUtilities.createComponent(JPanel.class, UIUtilities.BACKGROUND),
                c);

        validate();
        repaint();
        parent.revalidate();
    }

    /**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * 
     * @see WellFieldsCanvas#getNode(Point)
     */
    public WellSampleNode getNode(Point p) {
        Component c = findComponentAt(p);
        if (c != null && c instanceof FieldDisplay) {
            return ((FieldDisplay) c).node;
        }
        return null;
    }
    
    boolean isSelected(WellSampleNode n) {
        return this.parent.isSelected(n);
    }

    /**
     * Simple panel displaying the field thumbnail at max scale
     * 
     * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
     *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
     *
     */
    class FieldDisplay extends JPanel {

        WellSampleNode node;

        final Colors colors = Colors.getInstance();

        double mag = 0;
        
        public FieldDisplay(WellSampleNode node) {
            this.node = node;
            
            this.mag = parent.getMagnification();
            this.node.getThumbnail().scale(mag);

            setBackground(UIUtilities.BACKGROUND);

            Color col = isSelected(node) ? colors
                    .getColor(Colors.TITLE_BAR_HIGHLIGHT) : colors
                    .getColor(Colors.TITLE_BAR);
            Border b = BorderFactory.createLineBorder(col, 2);
            setBorder(BorderFactory.createTitledBorder(b, node.getTitle(),
                    TitledBorder.LEFT, TitledBorder.ABOVE_TOP,
                    (new JLabel()).getFont(), col));
        }

        @Override
        public void paint(Graphics g) {
            if(parent.getMagnification()!=this.mag) {
                this.mag = parent.getMagnification();
                this.node.getThumbnail().scale(mag);
            }
            
            Color col = isSelected(node) ? colors
                    .getColor(Colors.TITLE_BAR_HIGHLIGHT) : colors
                    .getColor(Colors.TITLE_BAR);
            Border b = BorderFactory.createLineBorder(col, 2);
            setBorder(BorderFactory.createTitledBorder(b, node.getTitle(),
                    TitledBorder.LEFT, TitledBorder.ABOVE_TOP,
                    (new JLabel()).getFont(), col));
            super.paint(g);
            g.drawImage(node.getThumbnail().getDisplayedImage(),
                    getInsets().left, getInsets().top, null);
        }

        @Override
        public Dimension getPreferredSize() {
            BufferedImage img = node.getThumbnail().getDisplayedImage();
            Insets in = getInsets();
            return new Dimension(img.getWidth() + in.left + in.right,
                    img.getHeight() + in.top + in.bottom);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }
}
