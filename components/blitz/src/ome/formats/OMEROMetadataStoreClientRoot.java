/*
 * ome.formats.OMEROMetadataStoreClient
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

import java.util.ArrayList;
import java.util.List;
import ome.xml.meta.MetadataRoot;
import omero.model.Pixels;

/**
 * {@link MetadataRoot} implementation for the client-side
 * implementation of the Bio-Formats {@link loci.formats.meta.MetadataStore}.
 * This class merely provides access to the pixels list.
 *
 * @author Roger Leigh, rleigh at lifesci.dundee.ac.uk
 */
public class OMEROMetadataStoreClientRoot extends ArrayList<Pixels> implements MetadataRoot {

  /** Default constructor. */
  public OMEROMetadataStoreClientRoot()
  {
    super();
  }

  /**
   * Copy constructor. 
   * @param list the list of {@link Pixels} objects for this instance
   */
  public OMEROMetadataStoreClientRoot(List<Pixels> list)
  {
    super(list);
  }

}
