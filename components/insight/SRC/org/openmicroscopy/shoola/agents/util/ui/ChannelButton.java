/*
 * org.openmicroscopy.shoola.agents.util.ui.ChannelButton 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.util.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;

import org.openmicroscopy.shoola.util.CommonsLangUtils;

import org.openmicroscopy.shoola.util.ui.ColouredButton;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Customized button used to select the rendered channel.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ChannelButton
    extends ColouredButton
    implements ActionListener
{

    /** The default size of the component. */
    public static final Dimension DEFAULT_MIN_SIZE = new Dimension(30, 30);

    /** The default size of the component. */
    public static final Dimension DEFAULT_MAX_SIZE = new Dimension(60, 30);

    /**
     * Bound property name indicating that the channel is or is not selected.
     */
    public static final String CHANNEL_SELECTED_PROPERTY = "channelSelected";

    /**
     * Bound property name indicating that the channel is or is not selected.
     */
    public static final String CHANNEL_OVERLAY_PROPERTY = "channelOverlay";

    /**
     * Bound property name indicating that the channel is mapped to a new color.
     */
    public static final String CHANNEL_COLOUR_PROPERTY = "channelColour";

    /** The minimum size of the font. */
    public static final int MIN_FONT_SIZE = 10;

    /** The description associated to this channel. */
    private static final String DESCRIPTION = "Toggle channel on/off.";

    /** The index of the channel. */
    protected final int index;

    /** The pop up menu associated to this component. */
    private ChannelButtonPopupMenu  popupMenu;

    /** Flag indicating if right-click are supported. */
    private boolean rightClickSupported;

    /** Flag indicating that the button is an overlay. */
    private boolean overlay;

    /** The default font.*/
    private Font originalFont;
    
    /** Flat indicating that the text should be trimmed at the end of the String */
    private boolean trimEnd = true;

    /** Fires an event to select the channel. */
    private final void setChannelSelected()
    {
        if (!isEnabled()) return;
        Boolean value = Boolean.valueOf(true);
        if (isSelected()) value = Boolean.valueOf(false);
        Map<Integer, Boolean> map = new HashMap<Integer, Boolean>(1);
        map.put(Integer.valueOf(index), value);
        if (overlay) 
            firePropertyChange(CHANNEL_OVERLAY_PROPERTY, null, map);
        else
            firePropertyChange(CHANNEL_SELECTED_PROPERTY, null, map);
    }

    /**
     * Selects the channel or displays the pop up menu.
     *
     * @param e The mouse event to handle.
     */
    private void onClick(MouseEvent e)
    {
        boolean mask = (e.isControlDown() || e.isMetaDown());
        if (e.getButton() == MouseEvent.BUTTON1 && !(mask) )
            setChannelSelected();
        else if ((e.getButton() == MouseEvent.BUTTON2 || mask))
            onReleased(e);
    }

    /**
     * Handles the mouse released event because pop-up menus are triggered
     * differently depending on the platform.
     *
     * @param e The The mouse event to handle.
     */
    private void onReleased(MouseEvent e)
    {
        if (e.isPopupTrigger() && rightClickSupported) {
            if (popupMenu == null) 
                popupMenu = new ChannelButtonPopupMenu(this);
            popupMenu.show(this, e.getX(), e.getY());
        } 
    }

    /**
     * Returns the preferred dimension of the component.
     *
     * @param dimWidth The width of the component.
     */
    private Dimension setComponentSize(int dimWidth)
    {
        Font f = getFont();
        String text = getText();
        FontMetrics fm = getFontMetrics(f);
        int width = fm.stringWidth(text);
        Dimension d = DEFAULT_MIN_SIZE;
        if (width > DEFAULT_MIN_SIZE.width && width <= dimWidth) {
            d = new Dimension(width+10, DEFAULT_MIN_SIZE.height);
        } else if (width > dimWidth) {
            int size = fm.stringWidth(UIUtilities.DOTS);
            width += size;
            String s = "";
            int n = trimEnd ? 0 : text.length()-1;
            List l = new ArrayList();
            char ch;
            while (fm.stringWidth(s)+size < dimWidth-size) {
                ch = text.charAt(n);
                s += ch;
                l.add(ch);
                n = trimEnd ? n+1 : n-1;
            }
            if(!trimEnd)
                Collections.reverse(l);
            Iterator i = l.iterator();
            s = trimEnd ? "" : UIUtilities.DOTS;
            while (i.hasNext())
                s += i.next();
            if(trimEnd)
                s += UIUtilities.DOTS;
            super.setText(s);
            //reset text
            width = fm.stringWidth(s);
            d = new Dimension(width+10, DEFAULT_MIN_SIZE.height);
        }
        return d;
    }

    /**
     * Parses the text.
     *
     * @param text The text to parse.
     * @return See above.
     */
    private String parseText(String text)
    {
        if (CommonsLangUtils.isBlank(text)) return "";
        String[] values = text.split("\\(");
        if (values == null || values.length == 0) return text;
        return values[0].trim();
    }

    /**
     * Sets the text associated to the component.
     *
     * @param text The value to set.
     */
    private void setTextValue(String text)
    {
        super.setText(parseText(text));
        List<String> l = new ArrayList<String>(2);
        if (CommonsLangUtils.isNotBlank(text)) l.add(text);
        l.add(DESCRIPTION);
        setToolTipText(UIUtilities.formatToolTipText(l));
    }

    /**
     * Creates a new instance.
     * 
     * @param text The text of the button. The text should correspond to
     *             the label of the channel.
     * @param color The background color of the button. Corresponds to the
     *              color associated to the channel.
     * @param index The channel index.
     * @param selected Pass <code>true</code> to select the channel (i.e.
     *                 the channel is rendered), <code>false</code> otherwise
     *                 (i.e. the channel is not rendered.)
     * @param trimEnd  Pass <code>true</code> if the end of the text should be trimmed,
     *                 if it is too large to display; <code>false</code> to trim at
     *                 the beginning.
     */
    public ChannelButton(String text, Color color, int index, boolean selected, boolean trimEnd)
    {
        super(text, color);
        originalFont = getFont();
        setTextValue(text);
        //Need to parse the String.
        this.index = index;
        rightClickSupported = true;
        this.trimEnd = trimEnd;
        setSelected(selected);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e); }
            public void mouseReleased(MouseEvent e) { onReleased(e); }
        });
        setPreferredSize(setComponentSize(DEFAULT_MAX_SIZE.width));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param text The text of the button. The text should correspond to
     *             the label of the channel.
     * @param color The background color of the button. Corresponds to the
     *              color associated to the channel.
     * @param index The channel index.
     * @param selected Pass <code>true</code> to select the channel (i.e.
     *                 the channel is rendered), <code>false</code> otherwise
     *                 (i.e. the channel is not rendered.)
     */
    public ChannelButton(String text, Color color, int index, boolean selected)
    {
        this(text, color, index, selected, true);
    }

    /**
     * Creates a deselected button.
     *
     * @param text The text of the button. The text should correspond to
     *             the label of the channel.
     * @param color The background color of the button. Corresponds to the
     *              color associated to the channel.
     * @param index The channel index.
     */
    public ChannelButton(String text, Color color, int index)
    {
        this(text, color, index, false);
    }

    /**
     * Sets the overlay flag.
     * 
     * @param overlay Pass <code>true</code> to indicate that it is a button
     *                for overlay, <code>false</code> otherwise.
     */
    public void setOverlay(boolean overlay) { this.overlay = overlay; }

    /** Fires a property change to bring up on screen the color picker. */
    void showColorPicker()
    {
        firePropertyChange(CHANNEL_COLOUR_PROPERTY, null,
                Integer.valueOf(index));
    }

    /**
     * Returns the index of the channel.
     * 
     * @return See above.
     */
    public int getChannelIndex() { return index; }

    /**
     * Sets to <code>true</code> if right clicks are supported,
     * <code>false</code> otherwise.
     *
     * @param value The value to set.
     */
    public void setRightClickSupported(boolean value)
    {
        rightClickSupported = value;
    }

    /**
     * Overridden to set the border of the button.
     * @see ColouredButton#setSelected(boolean)
     */
    public void setSelected(boolean selected)
    {
        super.setSelected(selected);
        if (selected) setBorder(BorderFactory.createLoweredBevelBorder());
        else setBorder(BorderFactory.createRaisedBevelBorder());
    }

    /**
     * Overridden so the text can be parsed and the tool tip set.
     * @see ColouredButton#setText(String)
     */
    public void setText(String text)
    {
        setTextValue(text);
        if (originalFont != null) {
            setFont(originalFont);
            if (CommonsLangUtils.isNotBlank(text)) {
                int width = getFontMetrics(getFont()).stringWidth(text);
                if (width > DEFAULT_MAX_SIZE.width)
                    width = DEFAULT_MAX_SIZE.width;
                if (width < getPreferredSize().width)
                    width = getPreferredSize().width;
                Dimension d = setComponentSize(width);
                setSize(d);
                setPreferredSize(d);
            }
        }
        repaint();
    }

    /**
     * Handles click performed on the channel button.
     * @see ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { setChannelSelected(); }

}
