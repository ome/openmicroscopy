/*
 * ome.formats.model.TargetProcessor
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
 */

package ome.formats.model;

import java.util.List;

import ome.util.LSID;
import omero.metadatastore.IObjectContainer;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Plate;
import omero.model.Screen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes the IObjectContainerStore and populates references for the
 * linkage target object of choice.
 *
 * @author Chris Allan <callan at blackcat dot ca>
 *
 */
public class TargetProcessor implements ModelProcessor {

  /** Logger for this class */
  private Logger log = LoggerFactory.getLogger(TargetProcessor.class);

  /**
   * Processes the OMERO client side metadata store.
   * @param store OMERO metadata store to process.
   * @throws ModelException If there is an error during processing.
   */
  public void process(IObjectContainerStore store) throws ModelException {
    IObject target = store.getUserSpecifiedTarget();
    if (target == null) {
      return;
    }

    List<IObjectContainer> containers = null;

    if (target instanceof Dataset) {
      containers = store.getIObjectContainers(Image.class);
    } else if (target instanceof Screen) {
      containers = store.getIObjectContainers(Plate.class);
    } else {
      throw new ModelException("Unable to handle target: " + target);
    }

    for (IObjectContainer container : containers) {
      LSID targetLSID = new LSID(container.LSID);
      LSID referenceLSID =
          new LSID(String.format("%s:%d", target.getClass().getName(),
                                 target.getId().getValue()));
      store.addReference(targetLSID, referenceLSID);
    }
  }
}
