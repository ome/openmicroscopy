package ui;

/**
 *  see org.openmicroscopy.shoola.util.ui.component.ObservableComponent
 */

import javax.swing.event.ChangeListener;

public interface ObservableComponent {
	
	/**
     * Registers an observer with this component.
     * The observer will be notified of <i>every</i> state change.
     * 
     * @param observer The observer to register.
     * @throws NullPointerException If <code>observer</code> is 
     *                              <code>null</code>.
     * @see #removeChangeListener(ChangeListener)
     */
    public void addChangeListener(ChangeListener observer);
    
    /**
     * Removes an observer from the change notification list.
     * 
     * @param observer The observer to remove.
     * @throws NullPointerException If <code>observer</code> is 
     *                              <code>null</code>.
     * @see #addChangeListener(ChangeListener)
     */
    public void removeChangeListener(ChangeListener observer);

}
