/*
 * ome.formats.importer.gui.GuiCommonElements
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
 *
 */

package ome.formats;

import java.util.HashMap;
import java.util.Map;

import loci.formats.meta.DummyMetadata;

/**
 * Stores all Image names consumed by the interface.
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class ImageNameMetadataStore extends DummyMetadata
{
    /**
     * The Map of Image index vs. Image name. This is map because of the
     * potential out of order population via metadata store usage.
     */
    private Map<Integer, String> imageNames = new HashMap<Integer, String>();

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageName(java.lang.String, int)
     */
    @Override
    public void setImageName(String imageName, int imageIndex)
    {
        imageNames.put(imageIndex, imageName);
    }

    /**
     * Retrieves the current map of Image names held.
     * @return Map of Image index vs. Image name.
     */
    public Map<Integer, String> getImageNames()
    {
        return imageNames;
    }
}
