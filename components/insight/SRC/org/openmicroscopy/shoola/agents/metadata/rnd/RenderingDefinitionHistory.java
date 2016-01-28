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

package org.openmicroscopy.shoola.agents.metadata.rnd;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import org.openmicroscopy.shoola.env.rnd.RndProxyDef;

/**
 * Keeps track of changes to the rendering settings by managing a list of
 * {@link RndProxyDef}s
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class RenderingDefinitionHistory {

    /** Property name for current undo ability */
    public static final String CAN_UNDO = "canUndo";

    /** Property name for current redo ability */
    public static final String CAN_REDO = "canRedo";

    /** Indicates that the previous action was an undo */
    private static final int PREV_ACTION_BACKWARD = 1;
    
    /** Indicates that the previous action was a redo */
    private static final int PREV_ACTION_FORWARD = 2;
    
    /** Reference to the PropertyChangeListeners */
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** Previous rendering settings */
    private List<RndProxyDef> history = new ArrayList<RndProxyDef>();

    /** Pointer to the 'current' previous rendering settings */
    private int pointer = -1;

    /** Tracks the last action */
    private int previousAction = 0;
    
    /**
     * Creates a new instance
     */
    public RenderingDefinitionHistory() {

    }

    /**
     * Clears the history
     */
    public void reset() {
        boolean oldU = canUndo();
        boolean oldR = canRedo();

        history.clear();
        pointer = -1;
        previousAction = 0;
        
        this.pcs.firePropertyChange(CAN_UNDO, oldU, canUndo());
        this.pcs.firePropertyChange(CAN_REDO, oldR, canRedo());
    }

    /**
     * Returns the previous rendering settings, <code>null</code> if the history
     * is empty
     * 
     * @return See above
     */
    public RndProxyDef getCurrent() {
        if (history.isEmpty() || pointer<0)
            return null;

        if (pointer >= history.size())
            throw new IllegalStateException(
                    "Pointer is not within the valid range: " + pointer
                            + " [-1;" + (history.size() - 1) + "]");

        return history.get(pointer);
    }

    /**
     * Adds a {@link RndProxyDef} to the end of the history
     * 
     * @param def
     *            The {@link RndProxyDef} to add
     */
    public void add(RndProxyDef def) {
        boolean oldU = canUndo();
        boolean oldR = canRedo();

        if (!history.isEmpty() && pointer < history.size() - 1)
            trim();

        history.add(def);
        pointer = history.size() - 1;
        
        this.pcs.firePropertyChange(CAN_UNDO, oldU, canUndo());
        this.pcs.firePropertyChange(CAN_REDO, oldR, canRedo());
    }
    
    /**
     * Resets the previous action flag to undefined
     */
    public void resetPrevAction() {
        previousAction = 0;
    }

    /**
     * Moves the the pointer forward in history and returns that settings
     * 
     * @return See above
     */
    public RndProxyDef forward() {
        boolean oldU = canUndo();
        boolean oldR = canRedo();

        // have to add an extra step when switching from undo to redo
        if (previousAction==PREV_ACTION_BACKWARD)
            pointer += 2;
        else
            pointer += 1;

        this.pcs.firePropertyChange(CAN_UNDO, oldU, canUndo());
        this.pcs.firePropertyChange(CAN_REDO, oldR, canRedo());
        
        previousAction = PREV_ACTION_FORWARD;
        
        return getCurrent();
    }

    /**
     * Returns the previous settings and moves the pointer backwards in history.
     * 
     * @param def
     *            The current state to add to the history before resetting to a
     *            previous one (is added to the end of the history)
     * @return See above
     */
    public RndProxyDef backward(RndProxyDef def) {
        if (def == null) {
            return backward();
        }

        boolean oldU = canUndo();
        boolean oldR = canRedo();

        if (!history.isEmpty() && pointer < history.size() - 1)
            trim();

        history.add(def);

        // have to move an extra step back when switching from redo to undo
        if (previousAction==PREV_ACTION_FORWARD)
            pointer--;
        
        RndProxyDef current = getCurrent();
        
        pointer--;

        this.pcs.firePropertyChange(CAN_UNDO, oldU, canUndo());
        this.pcs.firePropertyChange(CAN_REDO, oldR, canRedo());
        
        previousAction = PREV_ACTION_BACKWARD;
        
        return current;
    }

    /**
     * Returns the previous settings and moves the pointer backwards in history.
     * 
     * @return See above
     */
    public RndProxyDef backward() {
        boolean oldU = canUndo();
        boolean oldR = canRedo();

        if (previousAction==PREV_ACTION_FORWARD)
            pointer--;
        
        RndProxyDef def = getCurrent();
        
        pointer--;

        this.pcs.firePropertyChange(CAN_UNDO, oldU, canUndo());
        this.pcs.firePropertyChange(CAN_REDO, oldR, canRedo());
        
        previousAction = PREV_ACTION_BACKWARD;
        
        return def;
    }

    /**
     * Checks if an undo operation is possible
     * 
     * @return See above.
     */
    public boolean canUndo() {
        return !history.isEmpty() && pointer >= 0;
    }

    /**
     * Checks if a redo operation is possible
     * 
     * @return See above.
     */
    public boolean canRedo() {
        return !history.isEmpty() && pointer < history.size() - 1;
    }

    /**
     * Adds a {@link PropertyChangeListener}
     * 
     * @param listener
     *            The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a {@link PropertyChangeListener}
     * 
     * @param listener
     *            The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    /**
     * Removes all history entries succeeding the current pointer.
     */
    private void trim() {
        history = history.subList(0, pointer + 1);
    }

    @Override
    public String toString() {
        return "RenderingDefinitionHistory [history.size()=" + history.size()
                + ", historyPointer=" + pointer + ", previousAction="+previousAction+"]";
    }

}
