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
 */

package ome.formats.importer;

/**
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 *
 */
public interface IObservable
{
    /**
     * Add observer for notification
     *
     * @param object - observer object
     * @return true if added
     */
    boolean addObserver(IObserver object);

    /**
     * Delete observer
     *
     * @param object - observer to delete
     * @return true if deleted
     */
    boolean deleteObserver(IObserver object);

    /**
     * Notify observers of event
     *
     * @param event - event that happened
     */
    void notifyObservers(ImportEvent event);
}
