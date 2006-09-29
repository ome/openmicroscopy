/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewerControl
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

package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.actions.ActivationAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ChannelMovieAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.LensAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.MovieAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.RateImageAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.RendererAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.SaveAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarSizeAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ViewerAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.rnd.Renderer;
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelButton;
import org.openmicroscopy.shoola.agents.imviewer.util.InfoDialog;
import org.openmicroscopy.shoola.env.data.model.ChannelMetadata;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.colourpicker.ColourPicker;

/** 
 * The ImViewer's Controller.
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
class ImViewerControl
    implements ChangeListener, PropertyChangeListener
{

    /** Identifies the <code>Close</code> action in the menu. */
    static final Integer     CLOSE = new Integer(0);
    
    /** Identifies the <code>Renderer</code> action in the menu. */
    static final Integer     RENDERER = new Integer(1);     
    
    /** Identifies the <code>Movie</code> action in the menu. */
    static final Integer     MOVIE = new Integer(2);
    
    /** Identifies the <code>Save</code> action in the menu. */
    static final Integer     SAVE = new Integer(3);
    
    /** Identifies the <code>Zooming 25%</code> action in the menu. */
    static final Integer     ZOOM_25 = new Integer(4);
    
    /** Identifies the <code>Zooming 50%</code> action in the menu. */
    static final Integer     ZOOM_50 = new Integer(5);
    
    /** Identifies the <code>Zooming 75%</code> action in the menu. */
    static final Integer     ZOOM_75 = new Integer(6);
    
    /** Identifies the <code>Zooming 100%</code> action in the menu. */
    static final Integer     ZOOM_100 = new Integer(7);
    
    /** Identifies the <code>Zooming 125%</code> action in the menu. */
    static final Integer     ZOOM_125 = new Integer(8);
    
    /** Identifies the <code>Zooming 150%</code> action in the menu. */
    static final Integer     ZOOM_150 = new Integer(9);
    
    /** Identifies the <code>Zooming 175%</code> action in the menu. */
    static final Integer     ZOOM_175 = new Integer(10);
    
    /** Identifies the <code>Zooming 200%</code> action in the menu. */
    static final Integer     ZOOM_200 = new Integer(11);
    
    /** Identifies the <code>Zooming 225%</code> action in the menu. */
    static final Integer     ZOOM_225 = new Integer(12);
    
    /** Identifies the <code>Zooming 250%</code> action in the menu. */
    static final Integer     ZOOM_250 = new Integer(13);
    
    /** Identifies the <code>Zooming 275%</code> action in the menu. */
    static final Integer     ZOOM_275 = new Integer(14);
    
    /** Identifies the <code>Zooming 300%</code> action in the menu. */
    static final Integer     ZOOM_300 = new Integer(15);
    
    /** Identifies the <code>Lens</code> action in the menu. */
    static final Integer     LENS = new Integer(16);
    
    /** Identifies the <code>Grey Scale</code> action in the menu. */
    static final Integer     GREY_SCALE_MODEL = new Integer(17);
    
    /** Identifies the <code>RGB</code> action in the menu. */
    static final Integer     RGB_MODEL = new Integer(18);
    
    /** Identifies the <code>HSB</code> action in the menu. */
    static final Integer     HSB_MODEL = new Integer(19);
    
    /** 
     * Identifies the <code>First</code> level of the rating action in the 
     * menu. 
     */
    static final Integer     RATING_ONE = new Integer(20);
    
    /** 
     * Identifies the <code>Second</code> level of the rating action in the 
     * menu. 
     */
    static final Integer     RATING_TWO = new Integer(21);
    
    /** 
     * Identifies the <code>Third</code> level of the rating action in the 
     * menu. 
     */
    static final Integer     RATING_THREE = new Integer(22);
    
    /** 
     * Identifies the <code>Fourth</code> level of the rating action in the 
     * menu. 
     */
    static final Integer     RATING_FOUR = new Integer(23);
    
    /** 
     * Identifies the <code>Fifth</code> level of the rating action in the 
     * menu. 
     */
    static final Integer     RATING_FIVE = new Integer(24);
    
    /** 
     * Identifies the <code>Channel movie</code> action in the 
     * menu. 
     */
    static final Integer     CHANNEL_MOVIE = new Integer(25);
    
    /** Identifies the <code>UnitBar</code> action in the menu. */
    static final Integer     UNIT_BAR = new Integer(26);
    
    /** Identifies the <code>Size plus unit bar</code> action. */
    static final Integer     UNIT_BAR_PLUS = new Integer(27);
    
    /** Identifies the <code>Size minus unit bar</code> action. */
    static final Integer     UNIT_BAR_MINUS = new Integer(28);
    
    /** 
     * Reference to the {@link ImViewer} component, which, in this context,
     * is regarded as the Model.
     */
    private ImViewer    model;
    
    /** Reference to the View. */
    private ImViewerUI  view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map         actionsMap;
    
    /** Keep track of the old state.*/
    private int         historyState;
    
    /** Index of the channel invoking the color picker. */
    private int         colorPickerIndex;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(RENDERER, new RendererAction(model));
        actionsMap.put(MOVIE, new MovieAction(model));
        actionsMap.put(SAVE, new SaveAction(model));
        ViewerAction action = new ZoomAction(model, ZoomAction.ZOOM_25);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_25, action);
        action = new ZoomAction(model, ZoomAction.ZOOM_50);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_50, action);
        action = new ZoomAction(model, ZoomAction.ZOOM_75);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_75, action);
        action = new ZoomAction(model, ZoomAction.ZOOM_100);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_100, action);
        action = new ZoomAction(model, ZoomAction.ZOOM_125);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_125, action);
        action = new ZoomAction(model, ZoomAction.ZOOM_150);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_150, action);
        action = new ZoomAction(model, ZoomAction.ZOOM_175);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_175, action);
        action = new ZoomAction(model, ZoomAction.ZOOM_200);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_200, action);
        action = new ZoomAction(model, ZoomAction.ZOOM_225);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_225, action);
        action = new ZoomAction(model, ZoomAction.ZOOM_250);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_250, action);
        action = new ZoomAction(model, ZoomAction.ZOOM_275);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_275, action);
        action = new ZoomAction(model, ZoomAction.ZOOM_300);
        action.addPropertyChangeListener(this);
        actionsMap.put(ZOOM_300, action);
        actionsMap.put(LENS, new LensAction(model));
        action = new ColorModelAction(model, ColorModelAction.GREY_SCALE_MODEL);
        actionsMap.put(GREY_SCALE_MODEL, action);
        action = new ColorModelAction(model, ColorModelAction.RGB_MODEL);
        actionsMap.put(RGB_MODEL, action);
        action = new ColorModelAction(model, ColorModelAction.HSB_MODEL);
        actionsMap.put(HSB_MODEL, action);
        action = new RateImageAction(model, RateImageAction.RATE_ONE);
        action.addPropertyChangeListener(this);
        actionsMap.put(RATING_ONE, action);
        action = new RateImageAction(model, RateImageAction.RATE_TWO);
        action.addPropertyChangeListener(this);
        actionsMap.put(RATING_TWO, action);
        action = new RateImageAction(model, RateImageAction.RATE_THREE);
        action.addPropertyChangeListener(this);
        actionsMap.put(RATING_THREE, action);
        action = new RateImageAction(model, RateImageAction.RATE_FOUR);
        action.addPropertyChangeListener(this);
        actionsMap.put(RATING_FOUR, action);
        action = new RateImageAction(model, RateImageAction.RATE_FIVE);
        action.addPropertyChangeListener(this);
        actionsMap.put(RATING_FIVE, action);
        actionsMap.put(CHANNEL_MOVIE, new ChannelMovieAction(model));
        actionsMap.put(UNIT_BAR, new UnitBarAction(model));
        actionsMap.put(UNIT_BAR_PLUS, new UnitBarSizeAction(model, true));
        actionsMap.put(UNIT_BAR_MINUS, new UnitBarSizeAction(model, false));
    }
    
    /** 
     * Attaches a window listener to the view to discard the model when 
     * the user closes the window.
     */
    private void attachWindowListeners()
    {
        JMenu menu = ImViewerFactory.getWindowsMenu();
        menu.addMenuListener(new MenuListener() {

            public void menuSelected(MenuEvent e) { createWindowsMenuItems(); }
            
            /** 
             * Required by I/F but not actually needed in our case, 
             * no-op implementation.
             * @see MenuListener#menuCanceled(MenuEvent)
             */ 
            public void menuCanceled(MenuEvent e) {}

            /** 
             * Required by I/F but not actually needed in our case, 
             * no-op implementation.
             * @see MenuListener#menuDeselected(MenuEvent)
             */ 
            public void menuDeselected(MenuEvent e) {}
            
        });
        
        //Listen to keyboard selection
        menu.addMenuKeyListener(new MenuKeyListener() {

            public void menuKeyReleased(MenuKeyEvent e)
            {
                createWindowsMenuItems();
            }
            
            /** 
             * Required by I/F but not actually needed in our case, 
             * no op implementation.
             * @see MenuKeyListener#menuKeyPressed(MenuKeyEvent)
             */
            public void menuKeyPressed(MenuKeyEvent e) {}
  
            /** 
             * Required by I/F but not actually needed in our case, 
             * no op implementation.
             * @see MenuKeyListener#menuKeyTyped(MenuKeyEvent)
             */
            public void menuKeyTyped(MenuKeyEvent e) {}
            
        });
        
        view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        view.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { model.discard(); }
            public void windowDeiconified(WindowEvent e) { 
                model.iconified(false);
            }
    
            public void windowIconified(WindowEvent e)
            { 
                model.iconified(true); 
            }
        });
        view.getLoadingWindow().addPropertyChangeListener(
                LoadingWindow.CLOSED_PROPERTY, this);
    }

    /** Creates the windowsMenuItems. */
    private void createWindowsMenuItems()
    {
        Set viewers = ImViewerFactory.getViewers();
        Iterator i = viewers.iterator();
        JMenu menu = ImViewerFactory.getWindowsMenu();
        menu.removeAll();
        ImViewer viewer;
        while (i.hasNext()) {
            viewer = (ImViewer) i.next();
            //if (!(viewer == model))
                menu.add(new JMenuItem(new ActivationAction(viewer)));
        }
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize(ImViewerComponent, ImViewerUI) initialize} 
     * method should be called straigh 
     * after to link this Controller to the other MVC components.
     */
    ImViewerControl() {}
    
    /**
     * Links this Controller to its Model and its View.
     * 
     * @param model  Reference to the {@link ImViewer} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     * @param view   Reference to the View.  Mustn't be <code>null</code>.
     */
    void initialize(ImViewer model, ImViewerUI view)
    {
        if (model == null) throw new NullPointerException("No model.");
        if (view == null) throw new NullPointerException("No view.");
        this.model = model;
        this.view = view;
        historyState = -1;
        colorPickerIndex = -1;
        actionsMap = new HashMap();
        createActions();
        model.addChangeListener(this);   
        model.addPropertyChangeListener(this);
        attachWindowListeners();
    }

    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    ViewerAction getAction(Integer id)
    {
        return (ViewerAction) actionsMap.get(id);
    }
    
    /**
     * Renders the specified XY-Plane.
     * 
     * @param z The selected z-section.
     * @param t The selected timepoint.
     */
    void setSelectedXYPlane(int z, int t) { model.setSelectedXYPlane(z, t); }
    
    /**
     * 
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        int state = model.getState();
        switch (state) {
            case ImViewer.DISCARDED:
                LoadingWindow window = view.getLoadingWindow();
                window.setVisible(false);
                window.dispose();
                view.setVisible(false);
                view.dispose();
                historyState = state;
                break;
            case ImViewer.LOADING_RENDERING_CONTROL:
                UIUtilities.centerAndShow(view.getLoadingWindow());
                historyState = state;
                break;
            case ImViewer.LOADING_IMAGE:
                //if (historyState == ImViewer.LOADING_METADATA)
                 if (historyState == ImViewer.LOADING_RENDERING_CONTROL)
                    view.getLoadingWindow().setVisible(false);
                view.onStateChange(false);
                historyState = state;
             break;  
            case ImViewer.READY:
                view.getLoadingWindow().setVisible(false);
                if (historyState == ImViewer.CHANNEL_MOVIE)
                    view.onStateChange(false);
                else {
                    view.onStateChange(true);
                    historyState = state;
                }
                break;
            case ImViewer.CHANNEL_MOVIE:
                historyState = ImViewer.CHANNEL_MOVIE;
                view.onStateChange(false);
                
        }
        //historyState = state;
    }

    /**
     * Reacts to property changes in the {@link ImViewer}.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String propName = pce.getPropertyName(); 
        if (ImViewer.Z_SELECTED_PROPERTY.equals(propName)) {
            view.setZSection(((Integer) pce.getNewValue()).intValue());
        } else if (ImViewer.T_SELECTED_PROPERTY.equals(propName)) {
            view.setTimepoint(((Integer) pce.getNewValue()).intValue());
        } else if (ChannelButton.CHANNEL_SELECTED_PROPERTY.equals(propName)) {
            Map map = (Map) pce.getNewValue();
            if (map == null) return;
            if (map.size() != 1) return;
            Iterator i = map.keySet().iterator();
            Integer index;
            while (i.hasNext()) {
                index = (Integer) i.next();
                model.setChannelSelection(index.intValue(), 
                                    ((Boolean) map.get(index)).booleanValue());
            }
        } else if (ZoomAction.ZOOM_PROPERTY.equals(propName)) {
            view.setZoomFactor((ViewerAction) pce.getNewValue());
        } else if (RateImageAction.RATE_IMAGE_PROPERTY.equals(propName)) {
            view.setRatingFactor((ViewerAction) pce.getNewValue());
        } else if (LoadingWindow.CLOSED_PROPERTY.equals(propName)) {
            model.discard();
        } else if (Renderer.RENDER_PLANE_PROPERTY.equals(propName)) {
            model.renderXYPlane();
        } else if (Renderer.SELECTED_CHANNEL_PROPERTY.equals(propName)) {
            if (model.getColorModel().equals(ImViewer.GREY_SCALE_MODEL)) {
                int c = ((Integer) pce.getNewValue()).intValue();
                for (int i = 0; i < model.getMaxC(); i++)
                    model.setChannelActive(i, i == c);
                model.displayChannelMovie();
            }
        } else if (ChannelButton.INFO_PROPERTY.equals(propName)) {
            int index = ((Integer) pce.getNewValue()).intValue();
            ChannelMetadata data = model.getChannelMetadata(index);
            InfoDialog dialog = new InfoDialog(model.getUI(), data);
            dialog.addPropertyChangeListener(this);
            UIUtilities.centerAndShow(dialog);
        } else if (ChannelButton.CHANNEL_COLOR_PROPERTY.equals(propName)) {
            colorPickerIndex = ((Integer) pce.getNewValue()).intValue();
            Color c = model.getChannelColor(colorPickerIndex);
            ColourPicker dialog = new ColourPicker(c);
            dialog.addPropertyChangeListener(this);
            UIUtilities.centerAndShow(dialog);
        } else if (ColourPicker.COLOUR_PROPERTY.equals(propName)) { 
            Color c = (Color) pce.getNewValue();
            if (colorPickerIndex != -1) {
                model.setChannelColor(colorPickerIndex, c);
            }
        } else if (InfoDialog.UPDATE_PROPERTY.equals(propName)) {
            //TODO: implement method
        }
    }

    /**
     * Returns the previous state.
     * 
     * @return See above.
     */
    int getHistoryState() { return historyState; }

    /**
     * Sets the previous state.
     * 
     * @param s The value to set.
     */
    void setHistoryState(int s) { historyState = s; }

}
