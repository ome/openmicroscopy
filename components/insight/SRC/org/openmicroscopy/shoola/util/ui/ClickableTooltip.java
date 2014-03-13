/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.util.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * Provides a clickable tooltip; Consists of a tooltip text and an action, 
 * which is displayed as 'link' the user can click on.
 * The behaviour is slightly different from the Swing toolip as it
 * stays open as long the mouse stays above the component the tooltip
 * is attached to or the tooltip itself.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ClickableTooltip {

    /** Delay until the tooltip is shown */
    private static final int DEFAULT_STARTUP_DELAY = 500;
    
    /** Delay until the tooltip vanishes again */
    private static final int DEFAULT_SHOW_TIME = 500;
    
    /** Default x axis offset to the cursor where the tooltip is shown */
    private static final int DEFAULT_X_OFFSET = 10;
    
    /** Default y axis offset to the cursor where the tooltip is shown */
    private static final int DEFAULT_Y_OFFSET = 10;
    
    /** The color for the 'link' text */
    private static final Color LINK_COLOR = Color.BLUE;
    
    /** The width of the empty border around the tooltip */
    private static final int INSETS = 3;
    
    /** The tooltip which is actually a JPopupmenu */
    private JPopupMenu popupMenu;
    
    /** The component the tooltip is attached to */
    private JComponent component;
    
    /** Reference to the current cursor position where the tooltip's getting to be shown */
    private Point location;

    /** The timers providing the show/hide delays */
    private Timer showTimer, hideTimer;
    
    /**
     * Creates a ClickableTooltip with default settings 
     *
     * @param text The tooltip text
     * @param action The action which should be provided to the user
     */
    public ClickableTooltip(String text, Action action) {
        this(text, action, DEFAULT_STARTUP_DELAY, DEFAULT_SHOW_TIME);
    }
    
    /**
     * Creates a ClickableTooltip with custom show/hide delays
     * 
     * @param text The tooltip text
     * @param action The action which should be provided to the user
     * @param startupDelay Delay in ms before the tooltip should be shown
     * @param showTime Delay in ms after which the tooltip vanishes when the cursor moved away from the component or the tooltip area
     */
    public ClickableTooltip(final String text, final Action action, final int startupDelay, final int showTime) {
        Color c = UIUtilities.TOOLTIP_COLOR;
        Font f = (FontUIResource) UIManager.get("ToolTip.font");
        
        popupMenu = new JPopupMenu();
        popupMenu.setLayout(new BoxLayout(popupMenu, BoxLayout.PAGE_AXIS));
        popupMenu.setBorder(BorderFactory.createEmptyBorder(INSETS, INSETS, INSETS, INSETS));
        popupMenu.setBackground(c);
        popupMenu.setFont(f);
        
        if(text!=null) {
            JLabel desc = new JLabel(text);
            desc.setBackground(c);
            desc.setFont(f);
            popupMenu.add(desc);
        }
        
        final JLabel label = new JLabel();
        label.setForeground(LINK_COLOR);
        label.setBackground(c);
        label.setFont(f);
        label.setText((String)action.getValue(Action.NAME));
        label.addMouseListener(new MouseAdapter() {
            Cursor defaultCursor = null;
            
            public void mouseReleased(MouseEvent e) {
                action.actionPerformed(new ActionEvent(this, -1, (String) action.getValue(Action.ACTION_COMMAND_KEY)));
                // if the user clicks on the 'link' remove tooltip
                popupMenu.setVisible(false);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                defaultCursor = label.getCursor();
                label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                hideTimer.stop();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if(defaultCursor!=null) {
                    label.setCursor(defaultCursor);
                }
                hideTimer.restart();
            }
            
        });
        
        
        popupMenu.add(label);
        
        showTimer  = new Timer(startupDelay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                popupMenu.show(component, location.x, location.y);
                showTimer.stop();
            }
        });
        
        hideTimer  = new Timer(startupDelay+showTime, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                popupMenu.setVisible(false);
                hideTimer.stop();
                
            }
        });
        
    }
    
    /**
     * Attaches this tooltip to a certain component
     * 
     * @param component See above.
     */
    public void attach(final JComponent component) {
        attach(component, DEFAULT_X_OFFSET, DEFAULT_Y_OFFSET);
    }
    
    /**
     * Attaches this tooltip to a certain component
     * 
     * @param component See above.
     * @param xOffset The x axis offset where the tooltip is to be shown
     * @param yOffset The y axis offset where the tooltiop is to be shown
     */
    public void attach(final JComponent component, final int xOffset, final int yOffset) {
        this.component = component;
        
        component.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                location = e.getPoint();
                location.translate(xOffset, yOffset);
                showTimer.restart();
                hideTimer.stop();
            }
            public void mouseExited(MouseEvent e) {
                    showTimer.stop();
                    hideTimer.restart();
            }
        });
        
        popupMenu.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                hideTimer.stop();
            }
            public void mouseExited(MouseEvent e) {
                Rectangle componentBounds = new Rectangle(component.getLocationOnScreen(), new Dimension(component.getWidth(), component.getHeight()));
                if(!componentBounds.contains(e.getLocationOnScreen())) {
                    hideTimer.restart();
                }
            }
        });
        
    }
    
}
