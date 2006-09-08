/*
 * org.openmicroscopy.shoola.agents.imviewer.rnd.RendererControl
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer.rnd;



//Java imports
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import ome.model.display.CodomainMapContext;
import org.openmicroscopy.shoola.agents.imviewer.actions.ContrastStretchingAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.HistogramAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.NoiseReductionAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.PlaneSlicingAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ResetSettingsAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ReverseIntensityAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.SaveSettingsAction;
import org.openmicroscopy.shoola.agents.imviewer.util.CodomainMapContextDialog;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;

/** 
 * The Renderer's controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class RendererControl
    implements ChangeListener, PropertyChangeListener
{

    /** Identifies the Save settings action in the menu. */
    static final Integer    SAVE_SETTINGS = new Integer(0);
    
    /** Identifies the Reset settings action in the menu. */
    static final Integer    RESET_SETTINGS = new Integer(1);
    
    /** Identifies the action to select the bit resolution. */
    static final Integer    BIT_RESOLUTION = new Integer(2);
    
    /** Identifies the action to select the family. */
    static final Integer    FAMILY = new Integer(3);
    
    /** Identifies the action to select the coefficient. */
    static final Integer    COEFFICIENT = new Integer(4);
    
    /** Identifies the action to select the noise reduction algorithm. */
    static final Integer    NOISE_REDUCTION = new Integer(5);
    
    /** Identifies the action to select the reverse intensity transformation. */
    static final Integer    REVERSE_INTENSITY = new Integer(6);
    
    /** Identifies the action to select the plane slicing transformation. */
    static final Integer    PLANE_SLICING = new Integer(7);
    
    /**
     * Identifies the action to select the contrast stretching transformation.
     */
    static final Integer    CONTRAST_STRETCHING = new Integer(8);
    
    /** Identifies the action to bring up the histogram widget. */
    static final Integer    HISTOGRAM = new Integer(9);
    
    /**
     * Reference to the {@link Renderer} component, which, in this context,
     * is regarded as the Model.
     */
    private Renderer    model;
    
    /** Reference to the View. */
    private RendererUI  view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map         actionsMap;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(REVERSE_INTENSITY, new ReverseIntensityAction(model));
        actionsMap.put(PLANE_SLICING, new PlaneSlicingAction(model));
        actionsMap.put(CONTRAST_STRETCHING, 
                        new ContrastStretchingAction(model));
        actionsMap.put(SAVE_SETTINGS, new SaveSettingsAction(model));
        actionsMap.put(RESET_SETTINGS, new ResetSettingsAction(model));
        actionsMap.put(NOISE_REDUCTION, new NoiseReductionAction(model));
        actionsMap.put(HISTOGRAM, new HistogramAction(model));
    }
    
    /** 
     * Attaches a window listener to the view and a property listener to the 
     * model.
     */
    private void attachListeners()
    {
        model.addPropertyChangeListener(this);
        view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e)
            {
                view.setVisible(false);
            }
        });
    }
    
    /** 
     * Creates a new instance.
     * The {@link #initialize(Renderer, RendererUI) initialize} method should 
     * be called straight after to link this Controller to the other MVC
     * components.
     */
    RendererControl()
    {
        actionsMap = new HashMap();
    }
    
    /**
     * Links this Controller to its Model and View.
     * 
     * @param model Reference to the {@link Renderer} component, which, in this
     *              context, is regarded as the Model. Mustn't be
     *              <code>null</code>.
     * @param view  Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(Renderer model, RendererUI view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
        model.getParentModel().addPropertyChangeListener(this);
        createActions();
        attachListeners();
    }
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return (Action) actionsMap.get(id); }
    
    /**
     * Registers the specified observer.
     * 
     * @param observer  The observer to register.
     */
    void addPropertyListener(PropertyChangeListener observer)
    {
        model.addPropertyChangeListener(observer);
    }

    /** 
     * Sets the the pixels intensity interval for the
     * currently selected channel.
     * 
     * @param s         The lower bound of the interval.
     * @param e         The upper bound of the interval.
     * @param released  If <code>true</code>, we fire a property change event
     *                  to render a new plane.
     */
    void setInputInterval(double s, double e, boolean released)
    {
        model.setInputInterval(s, e, released);
    }
    
    /** 
     * Sets the sub-interval of the device space. 
     * 
     * @param s         The lower bound of the interval.
     * @param e         The upper bound of the interval.
     * @param released  If <code>true</code>, we fire a property change event
     *                  to render a new plane.
     */
    void setCodomainInterval(int s, int e, boolean released)
    {
        model.setCodomainInterval(s, e, released);
    }
    
    /**
     * Reacts to state changes in the <code>ImViewer</code>
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        switch (model.getParentModel().getState()) {
            case ImViewer.DISCARDED:
            case ImViewer.LOADING_IMAGE:
                view.onStateChange(false);
                break;
            case ImViewer.LOADING_PLANE_INFO:
            case ImViewer.READY:
            case ImViewer.READY_IMAGE:
                view.onStateChange(true);
        }  
    }

    /**
     * Reacts to property change events in the {@link ImViewer} and 
     * the {@link CodomainMapContextDialog}
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (name.equals(ImViewer.Z_SELECTED_PROPERTY) || 
            name.equals(ImViewer.T_SELECTED_PROPERTY)) {
            //retrieve plane statistics for specific channel.
        } else if (name.equals(
            CodomainMapContextDialog.UPDATE_MAP_CONTEXT_PROPERTY)) {
            CodomainMapContext ctx = (CodomainMapContext)  evt.getNewValue();
            model.updateCodomainMap(ctx);
        } else if (name.equals(ControlPane.FAMILY_PROPERTY)) {
            String oldValue = (String) evt.getOldValue();
            String newValue = (String) evt.getNewValue();
            if (newValue.equals(oldValue)) return;
            model.setFamily(newValue);
        } else if (name.equals(ControlPane.GAMMA_PROPERTY)) {
            Double oldValue = (Double) evt.getOldValue();
            Double newValue = (Double) evt.getNewValue();
            if (newValue.equals(oldValue)) return;
            model.setCurveCoefficient(newValue.doubleValue());
        } else if (name.equals(ControlPane.BIT_RESOLUTION_PROPERTY)) {
            Integer oldValue = (Integer) evt.getOldValue();
            Integer newValue = (Integer) evt.getNewValue();
            if (newValue.equals(oldValue)) return;
            model.setBitResolution(newValue.intValue());
        } else if (name.equals(ControlPane.CHANNEL_SELECTION_PROPERTY)) {
            int v = ((Integer) evt.getNewValue()).intValue();
            model.setSelectedChannel(v);
        } else if (name.equals(ImViewer.CHANNEL_ACTIVE_PROPERTY)) {
            int v = ((Integer) evt.getNewValue()).intValue();
            model.setSelectedChannel(v);
        } else if (name.equals(ImViewer.ICONIFIED_PROPERTY)) {
            if (((Boolean) evt.getNewValue()).booleanValue()) view.iconify();
            //else view.deIconify();
        } else if (name.equals(Renderer.INPUT_INTERVAL_PROPERTY)) {
            view.setInputInterval();
        }
    }
    
}
