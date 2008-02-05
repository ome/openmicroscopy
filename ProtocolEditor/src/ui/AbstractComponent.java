package ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public abstract class AbstractComponent
	implements ObservableComponent
{

	/** Change notification registry for change observers. */
	private Set         changeRegistry;
	
    /** Used for all change notifications coming from this publisher. */
    private ChangeEvent changeEvent;

	public AbstractComponent() {
		changeRegistry = new HashSet();
		changeEvent = new ChangeEvent(this);
	}
	
	/**
     * Implemented as specified by the {@link ObservableComponent} interface.
     * @see ObservableComponent#addChangeListener(ChangeListener)
     */
    public void addChangeListener(ChangeListener observer)
    {
        if (observer == null) throw new NullPointerException("No observer.");
        changeRegistry.add(observer);
    }
    
    
    /**
     * Implemented as specified by the {@link ObservableComponent} interface.
     * @see ObservableComponent#removeChangeListener(ChangeListener)
     */
    public void removeChangeListener(ChangeListener observer)
    {
        if (observer == null) throw new NullPointerException("No observer.");
        changeRegistry.remove(observer);
    }
    
    
    /**
     * Supports reporting any state change.
     * All change listeners will be notified.
     */
    protected void fireStateChange()
    {
        ChangeListener observer;
        Iterator i = changeRegistry.iterator();
        while (i.hasNext()) {
            observer = (ChangeListener) i.next();
            observer.stateChanged(changeEvent);
        }
    }
}
