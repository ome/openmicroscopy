/*
 * org.openmicroscopy.shoola.agents.iviewer.view.ImViewerControl
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.agents.imviewer.view;


//Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import javax.swing.event.MenuListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.actions.ChannelMovieAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ChannelsSelectionAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ClearHistoryAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorModelAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ColorPickerAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.CompressionAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.CopyRndSettingsAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.CreateMovieAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ExportImageAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.HistoryAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.LensAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.MetadataAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.MovieAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.PasteRndSettingsAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.PlayMovieAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.PreferencesAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ProjectionPreviewAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ProjectionProjectAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ROIToolAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.RendererAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ResetRndSettingsAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.SaveAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.SaveRndSettingsAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.SetOriginalRndSettingsAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ShowViewAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.TextVisibleAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.UnitBarSizeAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.UserAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ViewerAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomAction;
import org.openmicroscopy.shoola.agents.imviewer.actions.ZoomGridAction;
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelColorMenuItem;
import org.openmicroscopy.shoola.agents.imviewer.util.PlaneInfoComponent;
import org.openmicroscopy.shoola.agents.imviewer.util.PreferencesDialog;
import org.openmicroscopy.shoola.agents.imviewer.util.UnitBarSizeDialog;
import org.openmicroscopy.shoola.agents.imviewer.util.player.MoviePlayerDialog;
import org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjSavingDialog;
import org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionRef;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.agents.util.ui.ChannelButton;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneComponent;
import org.openmicroscopy.shoola.util.ui.LoadingWindow;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.colourpicker.ColourPicker;
import org.openmicroscopy.shoola.util.ui.lens.LensComponent;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.WellSampleData;


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
  	implements ActionListener, ChangeListener, ComponentListener,
  		PropertyChangeListener, WindowFocusListener
{

	/** Identifies the <code>Close</code> action in the menu. */
	static final Integer     CLOSE = Integer.valueOf(0);

	/** Identifies the <code>Renderer</code> action in the menu. */
	static final Integer     RENDERER = Integer.valueOf(1);     

	/** Identifies the <code>Movie</code> action in the menu. */
	static final Integer     MOVIE = Integer.valueOf(2);

	/** Identifies the <code>Save</code> action in the menu. */
	static final Integer     SAVE = Integer.valueOf(3);

	/** Identifies the <code>Zooming 25%</code> action in the menu. */
	static final Integer     ZOOM_25 = Integer.valueOf(4);

	/** Identifies the <code>Zooming 50%</code> action in the menu. */
	static final Integer     ZOOM_50 = Integer.valueOf(5);

	/** Identifies the <code>Zooming 75%</code> action in the menu. */
	static final Integer     ZOOM_75 = Integer.valueOf(6);

	/** Identifies the <code>Zooming 100%</code> action in the menu. */
	static final Integer     ZOOM_100 = Integer.valueOf(7);

	/** Identifies the <code>Zooming 125%</code> action in the menu. */
	static final Integer     ZOOM_125 = Integer.valueOf(8);

	/** Identifies the <code>Zooming 150%</code> action in the menu. */
	static final Integer     ZOOM_150 = Integer.valueOf(9);

	/** Identifies the <code>Zooming 175%</code> action in the menu. */
	static final Integer     ZOOM_175 = Integer.valueOf(10);

	/** Identifies the <code>Zooming 200%</code> action in the menu. */
	static final Integer     ZOOM_200 = Integer.valueOf(11);

	/** Identifies the <code>Zooming 225%</code> action in the menu. */
	static final Integer     ZOOM_225 = Integer.valueOf(12);

	/** Identifies the <code>Zooming 250%</code> action in the menu. */
	static final Integer     ZOOM_250 = Integer.valueOf(13);

	/** Identifies the <code>Zooming 275%</code> action in the menu. */
	static final Integer     ZOOM_275 = Integer.valueOf(14);

	/** Identifies the <code>Zooming 300%</code> action in the menu. */
	static final Integer     ZOOM_300 = Integer.valueOf(15);

	/** Identifies the <code>Zooming Fit to Window</code> action in the menu. */
	static final Integer     ZOOM_FIT_TO_WINDOW = Integer.valueOf(16);

	/** Identifies the <code>Lens</code> action in the menu. */
	static final Integer     LENS = Integer.valueOf(17);

	/** Identifies the <code>Grey Scale</code> action in the menu. */
	static final Integer     GREY_SCALE_MODEL = Integer.valueOf(18);

	/** Identifies the <code>RGB</code> action in the menu. */
	static final Integer     RGB_MODEL = Integer.valueOf(19);

	/** Identifies the <code>HSB</code> action in the menu. */
	static final Integer     HSB_MODEL = Integer.valueOf(20);
	
	/** 
	 * Identifies the <code>Channel movie</code> action in the 
	 * menu. 
	 */
	static final Integer     CHANNEL_MOVIE = Integer.valueOf(26);

	/** Identifies the <code>UnitBar</code> action in the menu. */
	static final Integer     UNIT_BAR = Integer.valueOf(27);

	/** Identifies the <code>Size one of the unit bar</code> action. */
	static final Integer     UNIT_BAR_ONE = Integer.valueOf(28);

	/** Identifies the <code>Size two of the unit bar</code> action. */
	static final Integer     UNIT_BAR_TWO = Integer.valueOf(29);

	/** Identifies the <code>Size five of the unit bar</code> action. */
	static final Integer     UNIT_BAR_FIVE = Integer.valueOf(30);

	/** Identifies the <code>Size ten of the unit bar</code> action. */
	static final Integer     UNIT_BAR_TEN = Integer.valueOf(31);

	/** Identifies the <code>Size twenty of the unit bar</code> action. */
	static final Integer     UNIT_BAR_TWENTY = Integer.valueOf(32);

	/** Identifies the <code>Size fifty of the unit bar</code> action. */
	static final Integer     UNIT_BAR_FIFTY = Integer.valueOf(33);

	/** Identifies the <code>Size hundred of the unit bar</code> action. */
	static final Integer     UNIT_BAR_HUNDRED = Integer.valueOf(34);

	/** Identifies the <code>customized size of the unit bar</code> action. */
	static final Integer     UNIT_BAR_CUSTOM = Integer.valueOf(35);

	/** Identifies the <code>color Picker</code> action. */
	static final Integer     COLOR_PICKER = Integer.valueOf(36);

	/** Identifies the <code>text visible</code> action in the menu. */
	static final Integer     TEXT_VISIBLE = Integer.valueOf(38);

	/** Identifies the <code>Measurement tool</code> action in the menu. */
	static final Integer     MEASUREMENT_TOOL = Integer.valueOf(39);

	/** Identifies the <code>Play movie across T</code> action. */
	static final Integer     PLAY_MOVIE_T = Integer.valueOf(44);

	/** Identifies the <code>Play movie across Z</code> action. */
	static final Integer     PLAY_MOVIE_Z = Integer.valueOf(45);

	/** Identifies the <code>Preferences</code> action. */
	static final Integer     PREFERENCES = Integer.valueOf(47);
	
	/** Identifies the <code>User</code> action. */
	static final Integer     USER = Integer.valueOf(48);

	/** 
	 * Identifies the <code>Zooming 25%</code> action of the grid image
	 * in the menu.
	 */
	static final Integer     ZOOM_GRID_25 = Integer.valueOf(49);
	
	/** 
	 * Identifies the <code>Zooming 50%</code> action of the grid image
	 * in the menu.
	 */
	static final Integer     ZOOM_GRID_50 = Integer.valueOf(50);
	
	/** 
	 * Identifies the <code>Zooming 75%</code> action of the grid image
	 * in the menu.
	 */
	static final Integer     ZOOM_GRID_75 = Integer.valueOf(51);
	
	/** 
	 * Identifies the <code>Zooming 100%</code> action of the grid image
	 * in the menu.
	 */
	static final Integer     ZOOM_GRID_100 = Integer.valueOf(52);

	/** Identifies the <code>View tab</code> action. */
	static final Integer     TAB_VIEW = Integer.valueOf(54);
	
	/** Identifies the <code>Projection tab</code> action. */
	static final Integer     TAB_PROJECTION = Integer.valueOf(55);
	
	/** Identifies the <code>Grid tab</code> action. */
	static final Integer     TAB_GRID = Integer.valueOf(56);
	
	/** Identifies the <code>History</code> action. */
	static final Integer     HISTORY = Integer.valueOf(57);
	
	/** Identifies the <code>Paste rendering settings</code> action. */
	static final Integer     PASTE_RND_SETTINGS = Integer.valueOf(58);
	
	/** Identifies the <code>Copy rendering settings</code> action. */
	static final Integer     COPY_RND_SETTINGS = Integer.valueOf(59);
	
	/** Identifies the <code>Save rendering settings</code> action. */
	static final Integer     SAVE_RND_SETTINGS = Integer.valueOf(60);
	
	/** Identifies the <code>Reset rendering settings</code> action. */
	static final Integer     RESET_RND_SETTINGS = Integer.valueOf(61);
	
	/** 
	 * Identifies the <code>Set the original rendering settings</code> action. 
	 */
	static final Integer     SET_ORIGINAL_RND_SETTINGS = Integer.valueOf(62);
	
	/** Identifies the <code>Projection preview</code> action. */
	static final Integer     PROJECTION_PREVIEW = Integer.valueOf(63);
	
	/** Identifies the <code>Projection project</code> action. */
	static final Integer     PROJECTION_PROJECT = Integer.valueOf(64);
	
	/** Identifies the <code>Compression</code> action. */
	static final Integer     COMPRESSION = Integer.valueOf(65);
	
	/** Identifies the <code>Clear history</code> action. */
	static final Integer     CLEAR_HISTORY = Integer.valueOf(66);

	/** Identifies the <code>Metadata</code> action in the menu. */
	static final Integer     METADATA = Integer.valueOf(67);    
	
	/** Identifies the <code>Create Movie</code> action in the menu. */
	static final Integer     CREATE_MOVIE = Integer.valueOf(68); 
	
	/** Identifies the <code>Play movie across life time bin</code> action. */
	static final Integer     PLAY_LIFETIME_MOVIE = Integer.valueOf(69);
	
	/** Identifies the <code>Channels On</code> action. */
	static final Integer     CHANNELS_ON = Integer.valueOf(70);
	
	/** Identifies the <code>Channels Off</code> action. */
	static final Integer     CHANNELS_OFF = Integer.valueOf(71);
	
	/** Identifies the <code>Export</code> action. */
	static final Integer     EXPORT = Integer.valueOf(72);
	
	/** 
	 * Reference to the {@link ImViewer} component, which, in this context,
	 * is regarded as the Model.
	 */
	private ImViewer    				model;

	/** Reference to the View. */
	private ImViewerUI  				view;

	/** Maps actions ids onto actual <code>Action</code> object. */
	private Map<Integer, ViewerAction>	actionsMap;

	/** Keep track of the old state.*/
	private int         				historyState;

	/** Index of the channel invoking the color picker. */
	private int         				colorPickerIndex;

	/** Reference to the movie player. */
	private MoviePlayerDialog			moviePlayer;
	
	/** Helper method to create all the UI actions. */
	private void createActions()
	{
		actionsMap.put(RENDERER, new RendererAction(model));
		actionsMap.put(MOVIE, new MovieAction(model));
		actionsMap.put(SAVE, new SaveAction(model));
		actionsMap.put(ZOOM_25, new ZoomAction(model, ZoomAction.ZOOM_25));
		actionsMap.put(ZOOM_50, new ZoomAction(model, ZoomAction.ZOOM_50));
		actionsMap.put(ZOOM_75, new ZoomAction(model, ZoomAction.ZOOM_75));
		actionsMap.put(ZOOM_100, new ZoomAction(model, ZoomAction.ZOOM_100));
		actionsMap.put(ZOOM_125, new ZoomAction(model, ZoomAction.ZOOM_125));
		actionsMap.put(ZOOM_150, new ZoomAction(model, ZoomAction.ZOOM_150));
		actionsMap.put(ZOOM_175, new ZoomAction(model, ZoomAction.ZOOM_175));
		actionsMap.put(ZOOM_200, new ZoomAction(model, ZoomAction.ZOOM_200));
		actionsMap.put(ZOOM_225, new ZoomAction(model, ZoomAction.ZOOM_225));
		actionsMap.put(ZOOM_250, new ZoomAction(model, ZoomAction.ZOOM_250));
		actionsMap.put(ZOOM_275, new ZoomAction(model, ZoomAction.ZOOM_275));
		actionsMap.put(ZOOM_300, new ZoomAction(model, ZoomAction.ZOOM_300));
		actionsMap.put(ZOOM_FIT_TO_WINDOW, 
				new ZoomAction(model, ZoomAction.ZOOM_FIT_TO_WINDOW));
		actionsMap.put(LENS, new LensAction(model));
		actionsMap.put(GREY_SCALE_MODEL, 
				new ColorModelAction(model, ColorModelAction.GREY_SCALE_MODEL));
		actionsMap.put(RGB_MODEL, 
				new ColorModelAction(model, ColorModelAction.RGB_MODEL));
		actionsMap.put(HSB_MODEL, 
				new ColorModelAction(model, ColorModelAction.HSB_MODEL));
		actionsMap.put(CHANNEL_MOVIE, new ChannelMovieAction(model));
		actionsMap.put(UNIT_BAR, new UnitBarAction(model));
		actionsMap.put(UNIT_BAR_ONE, new UnitBarSizeAction(model, 
				UnitBarSizeAction.ONE));
		actionsMap.put(UNIT_BAR_TWO, new UnitBarSizeAction(model, 
				UnitBarSizeAction.TWO));
		actionsMap.put(UNIT_BAR_FIVE, new UnitBarSizeAction(model, 
				UnitBarSizeAction.FIVE));
		actionsMap.put(UNIT_BAR_TEN, new UnitBarSizeAction(model, 
				UnitBarSizeAction.TEN));
		actionsMap.put(UNIT_BAR_TWENTY, new UnitBarSizeAction(model, 
				UnitBarSizeAction.TWENTY));
		actionsMap.put(UNIT_BAR_FIFTY, new UnitBarSizeAction(model, 
				UnitBarSizeAction.FIFTY));
		actionsMap.put(UNIT_BAR_HUNDRED, new UnitBarSizeAction(model, 
				UnitBarSizeAction.HUNDRED));
		actionsMap.put(UNIT_BAR_CUSTOM, new UnitBarSizeAction(model, 
				UnitBarSizeAction.CUSTOMIZED));
		actionsMap.put(COLOR_PICKER, new ColorPickerAction(model));
		actionsMap.put(TEXT_VISIBLE, new TextVisibleAction(model));
		actionsMap.put(MEASUREMENT_TOOL, new ROIToolAction(model));
		actionsMap.put(PLAY_MOVIE_T, 
				new PlayMovieAction(model, PlayMovieAction.ACROSS_T));
		actionsMap.put(PLAY_MOVIE_Z, 
				new PlayMovieAction(model, PlayMovieAction.ACROSS_Z));
		actionsMap.put(PREFERENCES, new PreferencesAction(model));
		actionsMap.put(USER, new UserAction(model));
		actionsMap.put(ZOOM_GRID_25, new ZoomGridAction(model, 
									ZoomGridAction.ZOOM_25));
		actionsMap.put(ZOOM_GRID_50, new ZoomGridAction(model, 
				ZoomGridAction.ZOOM_50));
		actionsMap.put(ZOOM_GRID_75, new ZoomGridAction(model, 
				ZoomGridAction.ZOOM_75));
		actionsMap.put(ZOOM_GRID_100, new ZoomGridAction(model, 
				ZoomGridAction.ZOOM_100));
		actionsMap.put(TAB_VIEW, new ShowViewAction(model, ShowViewAction.VIEW));
		actionsMap.put(TAB_PROJECTION, new ShowViewAction(model, 
											ShowViewAction.PROJECTION));
		actionsMap.put(TAB_GRID, new ShowViewAction(model, 
									ShowViewAction.SPLIT));
		actionsMap.put(HISTORY, new HistoryAction(model));
		actionsMap.put(PASTE_RND_SETTINGS, new PasteRndSettingsAction(model));
		actionsMap.put(COPY_RND_SETTINGS, new CopyRndSettingsAction(model));
		actionsMap.put(SAVE_RND_SETTINGS, new SaveRndSettingsAction(model));
		actionsMap.put(RESET_RND_SETTINGS, new ResetRndSettingsAction(model));
		actionsMap.put(SET_ORIGINAL_RND_SETTINGS, 
						new SetOriginalRndSettingsAction(model));
		actionsMap.put(PROJECTION_PREVIEW, new ProjectionPreviewAction(model));
		actionsMap.put(PROJECTION_PROJECT, new ProjectionProjectAction(model));
		actionsMap.put(COMPRESSION, new CompressionAction(model));
		actionsMap.put(CLEAR_HISTORY, new ClearHistoryAction(model));
		actionsMap.put(METADATA, new MetadataAction(model));
		actionsMap.put(CHANNELS_ON, new ChannelsSelectionAction(model, true));
		actionsMap.put(CHANNELS_OFF, new ChannelsSelectionAction(model, false));
		actionsMap.put(EXPORT, new ExportImageAction(model));
		actionsMap.put(CREATE_MOVIE, new CreateMovieAction(model));
		//TO be modified.
		actionsMap.put(PLAY_LIFETIME_MOVIE, new PlayMovieAction(model, 
				PlayMovieAction.ACROSS_T));
	}

	/** 
	 * Attaches a window listener to the view to discard the model when 
	 * the user closes the window.
	 */
	private void attachListeners()
	{
		model.addChangeListener(this);   
		model.addPropertyChangeListener(this);
		JMenu menu = ImViewerFactory.getWindowMenu();
		menu.addMenuListener(new MenuListener() {

			public void menuSelected(MenuEvent e)
			{ 
				Object source = e.getSource();
				if (source instanceof JMenu)
					ImViewerFactory.register((JMenu) source);
			}

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
				Object source = e.getSource();
				if (source instanceof JMenu)
					ImViewerFactory.register((JMenu) source);
			}

			/** 
			 * Required by I/F but not actually needed in our case, 
			 * no-op implementation.
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
			//public void windowOpened(WindowEvent e) 
			//{ view.addWindowFocusListener(this); }
		});
		view.getLoadingWindow().addPropertyChangeListener(
				LoadingWindow.CLOSED_PROPERTY, this);
		view.addWindowFocusListener(this);
	}

	/**
	 * Creates a new instance.
	 * The {@link #initialize(ImViewerComponent, ImViewerUI) initialize} 
	 * method should be called straight 
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
		actionsMap = new HashMap<Integer, ViewerAction>();
		createActions();
		attachListeners();
		ImViewerFactory.attachWindowMenuToTaskBar();
	}

	/**
	 * Returns the action corresponding to the specified id.
	 * 
	 * @param id One of the flags defined by this class.
	 * @return The specified action.
	 */
	ViewerAction getAction(Integer id) { return actionsMap.get(id); }

	/**
	 * Renders the specified XY-Plane.
	 * 
	 * @param z   The selected z-section.
	 * @param t   The selected timepoint.
	 * @param bin The selected bin, only used for lifetime.
	 */
	void setSelectedXYPlane(int z, int t, int bin)
	{ 
		model.setSelectedXYPlane(z, t, bin); 
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

	/**
	 * Brings up the color picker. Initializes the color picker with
	 * the color associated to the selected channel.
	 * 
	 * @param index The index of the selected channel. 
	 */
	void showColorPicker(int index) 
	{
		colorPickerIndex = index;
		Color c = model.getChannelColor(index);
		ColourPicker dialog = new ColourPicker(view, c);
		dialog.addPropertyChangeListener(this);
		UIUtilities.setLocationRelativeToAndShow(view, dialog);
	}

	/** 
	 * Initializes the movie player if required.
	 * or recycles it.
	 * 
	 * @return See above.
	 */
	MoviePlayerDialog getMoviePlayer()
	{
		if (moviePlayer != null) return moviePlayer;
		moviePlayer = new MoviePlayerDialog(view, model);
		moviePlayer.addPropertyChangeListener(MoviePlayerDialog.CLOSE_PROPERTY, 
				this);
		return moviePlayer;
	}

	/** 
	 * Sets the zoom factor corresponding to the passed index.
	 * 
	 * @param zoomIndex The index to handle.
	 */
	void setZoomFactor(int zoomIndex)
	{
		double f = ZoomAction.getZoomFactor(zoomIndex);
		model.setZoomFactor(f, zoomIndex);
	}

	/** 
	 * Moves the view to the front, to avoid loops, first removes the 
	 * WindowFocusListener.
	 */
	void toFront()
	{
		if (view.getExtendedState() != Frame.NORMAL) return;
		if (!view.isFocused()) {
			view.removeWindowFocusListener(this);
			view.setVisible(true);
			//view.addWindowFocusListener(this);
		}
	}
	
	/** Sets the preferences before closing. */
	void setPreferences()
	{
		ViewerPreferences pref = ImViewerFactory.getPreferences();
		if (pref == null) 
			pref = new ViewerPreferences();
		Rectangle bounds = view.getBounds();
		if (bounds != null)
			pref.setViewerBounds(bounds);
		int index = view.getZoomIndex();
		if (index > 0) pref.setZoomIndex(index);
		pref.setRenderer(view.isRendererShown());
		pref.setHistory(view.isHistoryShown());
		Color c = model.getUnitBarColor();
		if (c != null)
			pref.setScaleBarColor(c);
		index = view.getScaleBarIndex();
		if (index > 0) pref.setScaleBarIndex(index);
		ImViewerFactory.setPreferences(pref);
	}
	
	/** 
	 * Sets the zoom factor for the grid view.
	 * 
	 * @param factor The value to set.
	 */
	void setGridMagnificationFactor(double factor)
	{
		model.setGridMagnificationFactor(factor);
	}
	
	/**
	 * Loads the rendering control for the passed pixels ID.
	 * 
	 * @param pixelsID The id of the pixels set.
	 */
	void loadRenderingControl(long pixelsID)
	{
		model.loadRenderingControl(pixelsID);
	}

	/** 
	 * Sets the interval of z-sections to project. 
	 * 
	 * @param released 	Pass <code>true</code> if the knob is released, 
	 * 					<code>false</code> otherwise.
	 */
    void setProjectionRange(boolean released)
    {
    	view.setLeftStatus();
    	view.setPlaneInfoStatus();
    	if (released) model.renderXYPlane();
    }

	/**
	 * Renders the overlays. 
	 * 
	 * @param selected  Pass <code>true</code> if the overlays have to be 
	 * 					displayed, <code>false</code> otherwise.
	 */
	void renderOverlays(boolean selected)
	{ 
		model.renderOverlays(-1, selected); 
	}
	
	/**
	 * Reacts to change fired by buttons used to select the color
	 * models.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String m = model.getColorModel();
		ViewerAction a = null;
		if (ImViewer.RGB_MODEL.equals(m)) {
			a = getAction(ImViewerControl.GREY_SCALE_MODEL);
			a.actionPerformed(e);
		} else if (ImViewer.GREY_SCALE_MODEL.equals(m)) {
			a = getAction(ImViewerControl.RGB_MODEL);
			a.actionPerformed(e);
		} 
	}

	/**
	 * Reacts to state changes in the {@link ImViewer}.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() instanceof JTabbedPane) {
			JTabbedPane pane = (JTabbedPane) e.getSource();
			Component c = pane.getSelectedComponent();
			if (c instanceof ClosableTabbedPaneComponent) {
				int index = ((ClosableTabbedPaneComponent) c).getIndex();
				model.setSelectedPane(index);
			}
			return;
		}
		int state = model.getState();
		switch (state) {
			case ImViewer.DISCARDED:
				LoadingWindow window = view.getLoadingWindow();
				window.setVisible(false);
				window.dispose();
				view.setVisible(false);
				if (view.isLensVisible())
					view.setLensVisible(false, model.getSelectedIndex());
				view.dispose();
				historyState = state;
				break;
			case ImViewer.LOADING_IMAGE:
				if (historyState == ImViewer.LOADING_METADATA)
					view.getLoadingWindow().setVisible(false);
				view.setStatus(true);
				view.onStateChange(false);
				historyState = state;
				break;  
			case ImViewer.PROJECTING:
			case ImViewer.PROJECTION_PREVIEW:
			case ImViewer.PASTING:
				view.setStatus(true);
				view.onStateChange(false);
				break;
			case ImViewer.READY:
				view.setStatus(false);
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
	}

	/**
	 * Reacts to property changes in the {@link ImViewer}.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent pce)
	{
		String pName = pce.getPropertyName(); 
		if (ImViewer.Z_SELECTED_PROPERTY.equals(pName)) {
			view.setZSection(((Integer) pce.getNewValue()).intValue());
		} else if (ImViewer.T_SELECTED_PROPERTY.equals(pName)) {
			view.setTimepoint(((Integer) pce.getNewValue()).intValue());
		} else if (ChannelButton.CHANNEL_SELECTED_PROPERTY.equals(pName)) {
			Map map = (Map) pce.getNewValue();
			if (map == null) return;
			if (map.size() != 1) return;
			Set set = map.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Integer index;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				index = (Integer) entry.getKey();
				model.setChannelSelection(index.intValue(), 
						(Boolean) entry.getValue());
			}
		} else if (ChannelButton.CHANNEL_OVERLAY_PROPERTY.equals(pName)) {
			Map map = (Map) pce.getNewValue();
			if (map == null) return;
			if (map.size() != 1) return;
			Set set = map.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Integer index;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				index = (Integer) entry.getKey();
				model.renderOverlays(index.intValue(), 
						(Boolean) entry.getValue());
			}
		} else if (LoadingWindow.CLOSED_PROPERTY.equals(pName)) {
			model.discard();
		} else if (MetadataViewer.RENDER_PLANE_PROPERTY.equals(pName)) {
			model.renderXYPlane();
		} else if (MetadataViewer.RND_LOADED_PROPERTY.equals(pName)) {
			boolean b = (Boolean) pce.getNewValue();
			model.onRndLoaded(b);
		/*
		} else if (Renderer.RENDER_PLANE_PROPERTY.equals(pName)) {
			model.renderXYPlane();
		} else if (Renderer.SELECTED_CHANNEL_PROPERTY.equals(pName)) {
			Map map = (Map) pce.getNewValue();
			if (map == null) return;
			if (map.size() != 1) return;
			Set set = map.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Integer index;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				index = (Integer) entry.getKey();
				model.setChannelSelection(index.intValue(), 
						(Boolean) entry.getValue());
			}
			*/
		} else if (ChannelColorMenuItem.CHANNEL_COLOR_PROPERTY.equals(pName)) {
			model.showColorPicker(((Integer) pce.getNewValue()).intValue());
		} else if (ChannelButton.CHANNEL_COLOR_PROPERTY.equals(pName) ||
			ChannelColorMenuItem.CHANNEL_COLOR_PROPERTY.equals(pName)) {
			if (view.isSourceDisplayed(pce.getSource()))
				model.showColorPicker(((Integer) pce.getNewValue()).intValue());
		} else if (ColourPicker.COLOUR_PROPERTY.equals(pName)) { 
			Color c = (Color) pce.getNewValue();
			if (colorPickerIndex != -1) {
				model.setChannelColor(colorPickerIndex, c);
			}
		} else if (UnitBarSizeDialog.UNIT_BAR_VALUE_PROPERTY.equals(pName)) {
			double v = ((Double) pce.getNewValue()).doubleValue();
			model.setUnitBarSize(v);
		} else if (ImViewer.ICONIFIED_PROPERTY.equals(pName)) {
			if (moviePlayer != null)
				model.playMovie(false, false, -1);
			view.onIconified();
		} else if (LensComponent.LENS_LOCATION_PROPERTY.equals(pName)) {
			view.scrollToNode((Rectangle) pce.getNewValue());
		} else if (MoviePlayerDialog.CLOSE_PROPERTY.equals(pName)) {
			model.playMovie(false, false, -1);
		} else if (MoviePlayerDialog.MOVIE_STATE_CHANGED_PROPERTY.equals(pName)) 
		{
			//when movie player stop
			boolean b = ((Boolean) pce.getNewValue()).booleanValue();
			if (!b) {
				if (!getMoviePlayer().isVisible()) {
					PlayMovieAction action = 
						(PlayMovieAction) getAction(PLAY_MOVIE_T);
					action.setActionIcon(true);
					action = (PlayMovieAction) getAction(PLAY_MOVIE_Z);
					action.setActionIcon(true);
					model.playMovie(false, false, -1);
				}
			}
		} else if (PreferencesDialog.VIEWER_PREF_PROPERTY.equals(pName)) {
			Map  map = (Map) pce.getNewValue();
			if (map == null) ImViewerFactory.setPreferences(null);
			ViewerPreferences pref = ImViewerFactory.getPreferences();
			if (pref == null) pref = new ViewerPreferences();
			pref.setSelectedFields(map);
			ImViewerFactory.setPreferences(pref);
		} else if (UsersPopupMenu.USER_RNDSETTINGS_PROPERTY.equals(pName)) {
			ExperimenterData exp = (ExperimenterData) pce.getNewValue();
			model.setUserRndSettings(exp);
		} else if (ProjSavingDialog.PROJECTION_PROPERTY.equals(pName)) {
			model.projectImage((ProjectionRef) pce.getNewValue());
		} else if (PlaneInfoComponent.PLANE_INFO_PROPERTY.equals(pName)) {
			view.showPlaneInfoDetails((PlaneInfoComponent) pce.getNewValue());
		} else if (TinyDialog.CLOSED_PROPERTY.equals(pName)) {
			view.hidePlaneInfoDetails();
		} else if (ProjSavingDialog.LOAD_ALL_PROPERTY.equals(pName)) {
			model.loadAllContainers();
		} else if (MetadataViewer.SELECTED_CHANNEL_PROPERTY.equals(pName)) {
			int index = (Integer) pce.getNewValue();
			model.onChannelSelection(index);
		} else if (MetadataViewer.CLOSE_RENDERER_PROPERTY.equals(pName)) {
			Object ref = pce.getNewValue();
			ImageData image = null;
			if (ref instanceof ImageData) {
				image = (ImageData) ref;
			} else if (ref instanceof WellSampleData) {
				image = ((WellSampleData) ref).getImage();
			}
			if (image != null) {
				PixelsData pixs = image.getDefaultPixels();
				if (pixs != null && pixs.getId() == view.getPixelsID()) 
					model.discard();
			}
		} else if (MetadataViewer.CHANNEL_COLOR_CHANGED_PROPERTY.equals(
				pName)) {
			int index = (Integer) pce.getNewValue();
			model.onChannelColorChnaged(index);
		}
	}

	/**
	 * Captures the resize event of the {@link ImViewerUI}, if the user has 
	 * selected the zoom to fit to the window then resize the image to fit to
	 * the new size of the image. 
	 * @see ComponentListener#componentResized(ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) 
	{ 
		if (model.isZoomFitToWindow()) 
			model.setZoomFactor(-1, ZoomAction.ZOOM_FIT_TO_WINDOW); 
		view.maximizeWindow();
		setPreferences();
	}

	/**
	 * Posts an event to bring the related window to the front.
	 * @see WindowFocusListener#windowGainedFocus(WindowEvent)
	 */
	public void windowGainedFocus(WindowEvent e)
	{
		/*
		System.err.println(e.getOppositeWindow());
		EventBus bus = ImViewerAgent.getRegistry().getEventBus();
		bus.post(new FocusGainedEvent(view.getPixelsID(), 
				FocusGainedEvent.VIEWER_FOCUS));
				*/
	}
	
	/**
	 * Required by the I/F but no-op implementation in our case.
	 * @see WindowFocusListener#windowLostFocus(WindowEvent)
	 */
	public void windowLostFocus(WindowEvent e)
	{
	}

	/**
	 * Required by the {@link ComponentListener} I/F but no-op implementation 
	 * in our case.
	 * @see ComponentListener#componentShown(ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {}

	/**
	 * Required by the {@link ComponentListener} I/F but no-op implementation 
	 * in our case.
	 * @see ComponentListener#componentHidden(ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {}

	/**
	 * Required by the {@link ComponentListener} I/F but no-op implementation 
	 * in our case.
	 * @see ComponentListener#componentMoved(ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e) {}
	
}
