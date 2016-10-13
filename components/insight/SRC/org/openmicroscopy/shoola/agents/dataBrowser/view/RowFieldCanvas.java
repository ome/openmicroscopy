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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import omero.gateway.model.DataObject;

import org.openmicroscopy.shoola.agents.dataBrowser.Colors;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Display all the fields for a given well; based on GridBagLayout
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
class RowFieldCanvas extends WellFieldsCanvas {

    /** The titles of the wells */
    List<String> titles;
    
    /**
     * Creates a new instance.
     * 
     * @param parent
     *            The parent of the canvas.
     */
    public RowFieldCanvas(WellFieldsView parent) {
        super(parent);
        setDoubleBuffered(true);
        setBackground(UIUtilities.BACKGROUND);
        setLayout(new GridBagLayout());
    }

    /**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * 
     * @see WellFieldsCanvas#clear(List, int, Dimension)
     */
    public void clear(List<String> titles, int nFields, final Dimension thumbDim) {
        removeAll();
        
        this.titles = titles;
        
        if(!titles.isEmpty() && nFields > 0) {
            // column header
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(2, 2, 0, 0);
            c.fill = GridBagConstraints.NONE;
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 0;
            c.weighty = 0;
            for (int i = 0; i < nFields; i++) {
                JLabel f = new JLabel("" + (i + 1), SwingConstants.CENTER) {
                    @Override
                    public Dimension getPreferredSize() {
                        Dimension d = super.getPreferredSize();
                        if (thumbDim != null)
                            d.width = thumbDim.width;
                        return d;
                    }
                    @Override
                    public Dimension getMinimumSize() {
                        return getPreferredSize();
                    }
                };
                f.setBackground(UIUtilities.BACKGROUND);
                add(f, c);
                c.gridx++;
            }
    
            // row header
            c = new GridBagConstraints();
            c.insets = new Insets(2, 2, 0, 0);
            c.fill = GridBagConstraints.NONE;
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 0;
            c.weighty = 0;
            for (String title : titles) {
                JLabel f = new JLabel(title, SwingConstants.CENTER) {
                    @Override
                    public Dimension getPreferredSize() {
                        Dimension d = super.getPreferredSize();
                        if (thumbDim != null)
                            d.height = thumbDim.height;
                        return d;
                    }
                    @Override
                    public Dimension getMinimumSize() {
                        return getPreferredSize();
                    }
                };
                f.setBackground(UIUtilities.BACKGROUND);
                add(f, c);
                c.gridy++;
            }
            
            // add empty component to fill up space
            c = new GridBagConstraints();
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = nFields+1;
            c.gridy = titles.size()+1;
            c.fill = GridBagConstraints.BOTH;
            add(UIUtilities.createComponent(JLabel.class,
                    UIUtilities.BACKGROUND), c);
            
            displayExistingThumbs();
        }
        
        revalidate();
        parent.revalidate();
    }
    
    private void displayExistingThumbs() {
        List<WellSampleNode> l = parent.getNodes();
        if (l == null)
            return;
        
        for (WellSampleNode n : l) {
            if(!n.getThumbnail().isThumbnailLoaded())
                continue;
            
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.NONE;
            c.gridx = n.getIndex()+1;
            c.gridy = titles.indexOf(n.getTitle())+1;
            c.weightx = 0;
            c.weighty = 0;
            c.insets = new Insets(2, 2, 0, 0);

            FieldDisplay fd = new FieldDisplay(n);
            add(fd, c);
        }
    }
    
    /**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * 
     * @see WellFieldsCanvas#updateFieldThumb(WellSampleNode)
     */
    public void updateFieldThumb(WellSampleNode node) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.gridx = node.getIndex()+1;
        c.gridy = titles.indexOf(node.getTitle())+1;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(2, 2, 0, 0);

        FieldDisplay fd = new FieldDisplay(node);
        add(fd, c);
        
        revalidate();
        System.out.println("updateFieldThumb "+c.gridx+" "+c.gridy);
    }

    /**
     * Implemented as specified by the {@link WellFieldsCanvas} interface.
     * 
     * @see WellFieldsCanvas#refreshUI()
     */
    public void refreshUI() {

            removeAll();
            
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

            LinkedHashMap<String, Integer> titles = new LinkedHashMap<String, Integer>();
            for (WellSampleNode n : l) {
                if (!titles.containsKey(n.getTitle()))
                    titles.put(n.getTitle(), 1);
                else
                    titles.put(n.getTitle(), (titles.get(n.getTitle()) + 1));
            }

            int nFields = 0;
            for (Integer i : titles.values()) {
                nFields = Math.max(nFields, i);
            }

            // column header
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.NONE;
            c.gridx = 1;
            c.gridy = 0;
            c.weightx = 0;
            c.weighty = 0;
            for (int i = 0; i < nFields; i++) {
                JLabel f = new JLabel("" + (i + 1));
                f.setBackground(UIUtilities.BACKGROUND);
                add(f, c);
                c.gridx++;
            }

            // row header
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.NONE;
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 0;
            c.weighty = 0;
            for (String title : titles.keySet()) {
                JLabel f = new JLabel(title);
                f.setBackground(UIUtilities.BACKGROUND);
                add(f, c);
                c.gridy++;
            }

            Map<String, GridBagConstraints> cs = new HashMap<String, GridBagConstraints>();
            int y = 1;
            c = null;
            for (WellSampleNode n : l) {
                String key = n.getRow() + "_" + n.getColumn();
                c = cs.get(key);
                if (c == null) {
                    c = new GridBagConstraints();
                    c.fill = GridBagConstraints.NONE;
                    c.gridx = 1;
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
            add(UIUtilities.createComponent(JLabel.class,
                    UIUtilities.BACKGROUND), c);

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
     * Simple panel displaying the field thumbnail
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
            setBorder(BorderFactory.createLineBorder(col, 2));
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (node.getThumbnail().getDisplayedImage() != null)
                g.drawImage(node.getThumbnail().getDisplayedImage(),
                        getInsets().left, getInsets().top, null);
        }

        @Override
        public Dimension getPreferredSize() {
            BufferedImage img = node.getThumbnail().getDisplayedImage();
            if (img == null)
                return new Dimension(0, 0);

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
