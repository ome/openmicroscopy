/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.events.metadata;

import java.awt.Color;

import org.openmicroscopy.shoola.env.event.StateChangeEvent;

/**
 * Event indicating that the channel color or lookup table has been changed.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class ChannelColorChangedEvent extends StateChangeEvent {

    /** A reset event */
    public static final ChannelColorChangedEvent RESET_EVENT = new ChannelColorChangedEvent(
            true);

    private long imageId;
    private int index;
    private Color oldColor, newColor;
    private String oldLut, newLut;
    private boolean preview;
    private boolean reset;

    /**
     * Creates a new instance
     */
    public ChannelColorChangedEvent() {

    }

    /**
     * Creates a new 'reset' instance
     * 
     * @param reset
     */
    private ChannelColorChangedEvent(boolean reset) {
        this.reset = reset;
    }

    /**
     * Creates a new instance
     * 
     * @param imageId
     *            The image id
     * @param index
     *            The channel index
     * @param oldColor
     *            The previous color
     * @param newColor
     *            The new color
     * @param oldLut
     *            The previous lookup table
     * @param newLut
     *            The new lookup table
     * @param preview
     *            Preview changes
     */
    public ChannelColorChangedEvent(long imageId, int index, Color oldColor,
            Color newColor, String oldLut, String newLut, boolean preview) {
        this.imageId = imageId;
        this.index = index;
        this.oldColor = oldColor;
        this.newColor = newColor;
        this.oldLut = oldLut;
        this.newLut = newLut;
        this.preview = preview;
    }

    /**
     * Checks if this event changes the color
     * 
     * @return See above
     */
    public boolean colorChanged() {
        if (oldColor == null ^ newColor == null)
            return true;

        if (oldColor == null && newColor == null)
            return false;

        return oldColor.getRed() != newColor.getRed()
                || oldColor.getBlue() != newColor.getBlue()
                || oldColor.getGreen() != newColor.getGreen();
    }

    /**
     * Checks if this event changes the lookup table
     * 
     * @return See above
     */
    public boolean lutChanged() {
        if (oldLut != null && oldLut.trim().length() == 0)
            oldLut = null;

        if (newLut != null && newLut.trim().length() == 0)
            newLut = null;

        if (oldLut == null ^ newLut == null)
            return true;

        if (oldLut == null && newLut == null)
            return false;

        return !oldLut.equals(newLut);
    }

    /**
     * Get the image id
     * 
     * @return See above
     */
    public long getImageId() {
        return imageId;
    }

    /**
     * Set the image id
     * 
     * @param imageId
     *            The image id
     */
    public void setImageId(long imageId) {
        this.imageId = imageId;
    }

    /**
     * Get the channel index
     * 
     * @return See above
     */
    public int getIndex() {
        return index;
    }

    /**
     * Set the channel index
     * 
     * @param index
     *            The channel index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Get the previous color
     * 
     * @return See above.
     */
    public Color getOldColor() {
        return oldColor;
    }

    /**
     * Set the previous color
     * 
     * @param oldColor
     *            The previous color
     */
    public void setOldColor(Color oldColor) {
        this.oldColor = oldColor;
    }

    /**
     * Get the new color
     * 
     * @return See above
     */
    public Color getNewColor() {
        return newColor;
    }

    /**
     * Set the new color
     * 
     * @param newColor
     *            The new color
     */
    public void setNewColor(Color newColor) {
        this.newColor = newColor;
    }

    /**
     * Get the previous lookup table
     * 
     * @return See above
     */
    public String getOldLut() {
        return oldLut;
    }

    /**
     * Set the previous lookup table
     * 
     * @param oldLut
     *            The previous lookup table
     */
    public void setOldLut(String oldLut) {
        this.oldLut = oldLut;
    }

    /**
     * Get the new lookup table
     * 
     * @return See above
     */
    public String getNewLut() {
        return newLut;
    }

    /**
     * Set the new lookup table
     * 
     * @param newLut
     *            The new lookup table
     */
    public void setNewLut(String newLut) {
        this.newLut = newLut;
    }

    /**
     * Checks if the changes are meant to be previewed
     * 
     * @return See above
     */
    public boolean isPreview() {
        return preview;
    }

    /**
     * Set that the changes are meant to be previewed
     * 
     * @param preview
     *            Pass <code>true</code> if the changes are meant to be
     *            previewed
     */
    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    /**
     * Check if this is a 'reset' event
     * 
     * @return See above
     */
    public boolean isReset() {
        return reset;
    }

    @Override
    public String toString() {
        return "ChannelColorChangedEvent [imageId=" + imageId + ", index="
                + index + ", oldColor=" + oldColor + ", newColor=" + newColor
                + ", oldLut=" + oldLut + ", newLut=" + newLut + ", preview="
                + preview + ", reset=" + reset + "]";
    }
}
