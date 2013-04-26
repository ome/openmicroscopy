/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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

import javax.swing.DefaultComboBoxModel;

/** 
 * Provides support for having unselectable items in the list.
 * @author Scott Littlewood, <a href="mailto:sylittlewood@dundee.ac.uk">sylittlewood@dundee.ac.uk</a>
 * @since 4.4
 */
public class SelectableComboBoxModel extends DefaultComboBoxModel
{
	/**
	 * Check whether the item chosen is selectable and prevents unselectable 
	 * items from being selected.
	 */
    public void setSelectedItem(Object anObject) {

        if (anObject != null && anObject instanceof Selectable<?>) {
        	Selectable<?> entry = (Selectable<?>) anObject;
            if (entry.isSelectable()) {
                super.setSelectedItem(anObject);
            }
        } else {
            super.setSelectedItem(anObject);
        }
    }

} 
