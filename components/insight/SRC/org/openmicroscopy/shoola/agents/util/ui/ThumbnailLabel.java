/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.ui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.Border;

import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;

/**
 * Component displaying the thumbnail.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ThumbnailLabel
	extends JLabel
{

    /** Bound property indicating to browse the specified plate. */
    public static final String BROWSE_PLATE_PROPERTY = "browsePlate";

    /** Bound property indicating to view the image. */
    public static final String VIEW_IMAGE_PROPERTY = "viewImage";

    /** The border of the thumbnail label. */
    private static final Border LABEL_BORDER =
            BorderFactory.createLineBorder(Color.black, 1);

    /** The text displayed in the tool tip when the image has been imported. */
    static final String IMAGE_LABEL_TOOLTIP = "Click to view the image.";

    /** The text displayed in the tool tip when the plate has been imported. */
    static final String PLATE_LABEL_TOOLTIP = "Click to browse the plate.";

    /** The thumbnail or the image to host. */
    private Object data;

    /** Posts an event to view the object. */
    private void view()
    {
        if (data instanceof ThumbnailData) {
            ThumbnailData thumbnail = (ThumbnailData) data;
            firePropertyChange(VIEW_IMAGE_PROPERTY, null,
                    thumbnail.getImageID());
        } else if (data instanceof ImageData) {
            firePropertyChange(VIEW_IMAGE_PROPERTY, null,
                    ((ImageData) data).getId());
        } else if (data instanceof PlateData) {
            firePropertyChange(BROWSE_PLATE_PROPERTY, null, data);
        }
    }

    /** 
     * Sets the thumbnail to view.
     * 
     * @param data The value to set.
     */
    private void setThumbnail(ThumbnailData data)
    {
        if (data == null) return;
        BufferedImage img  = Factory.magnifyImage(0.25, data.getThumbnail());
        ImageIcon icon = null;
        if (img != null) icon = new ImageIcon(img);
        this.data = data;
        setToolTipText(IMAGE_LABEL_TOOLTIP);
        setBorder(LABEL_BORDER);
        setIcon(icon);
        addMouseListener(new MouseAdapter() {

            /**
             * Views the image.
             * @see MouseListener#mousePressed(MouseEvent)
             */
            public void mousePressed(MouseEvent e)
            {
                if (e.getClickCount() == 1)
                    view();
            }
        });
    }

    /** Creates a default new instance. */
    public ThumbnailLabel() {}

    /**  
     * Creates a new instance. 
     * 
     * @param icon The icon to display.
     */
    public ThumbnailLabel(Icon icon)
    {
        super(icon);
    }

    /** 
     * Sets the object that has been imported.
     * 
     * @param data The imported image.
     */
    public void setData(Object data)
    {
        if (data == null) return;
        this.data = data;
        if (data instanceof ImageData) {
            setToolTipText(IMAGE_LABEL_TOOLTIP);
        } else if (data instanceof PlateData) {
            setToolTipText(PLATE_LABEL_TOOLTIP);
        } else if (data instanceof ThumbnailData) {
            setThumbnail((ThumbnailData) data);
            return;
        }
        addMouseListener(new MouseAdapter() {

            /**
             * Views the image.
             * @see MouseListener#mousePressed(MouseEvent)
             */
            public void mousePressed(MouseEvent e)
            {
                if (e.getClickCount() == 1)
                    view(); 
            }
        });
    }

}
