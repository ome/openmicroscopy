/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFrame;

import omero.model.OriginalFile;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.AnalysisActivityParam;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import omero.gateway.model.FileAnnotationData;

/** 
 * Activity to analyze data.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class AnalysisActivity 
	extends ActivityComponent
{

	/** The description of the activity. */
	private static final String		DESCRIPTION_CREATION = "Analysing";
	
	/** The description of the activity when finished. */
	private static final String		DESCRIPTION_CREATED = "Analysis finished";
	
	/** The description of the activity when cancelled. */
	private static final String		DESCRIPTION_CANCEL = "Analysis cancelled";
	
	/** The parameters hosting information about the figure to make. */
    private AnalysisActivityParam	parameters;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param registry Convenience reference for subclasses.
     * @param ctx The security context.
     * @param parameters The parameters used to analyze.
     */
	public AnalysisActivity(UserNotifier viewer, Registry registry,
			SecurityContext ctx, AnalysisActivityParam parameters)
	{
		super(viewer, registry, ctx);
		if (parameters == null)
			throw new IllegalArgumentException("Parameters not valid.");
		this.parameters = parameters;
		initialize(DESCRIPTION_CREATION, parameters.getIcon());
	}

	/**
	 * Creates a concrete loader.
	 * @see ActivityComponent#createLoader()
	 */
	protected UserNotifierLoader createLoader()
	{
		return null;
	}

	/**
	 * Modifies the text of the component. 
	 * @see ActivityComponent#notifyActivityEnd()
	 */
	protected void notifyActivityEnd()
	{
		type.setText(DESCRIPTION_CREATED);
	}
	
	/** Notifies to dowload the file. */
	protected void notifyDownload()
	{
		//Check name space.
		/*
		if (!(result instanceof FileAnnotationData)) {
			downloadButton.setEnabled(false);
			return;
		}
		*/
		final FileAnnotationData data = (FileAnnotationData) result;
		JFrame f = registry.getTaskBar().getFrame();
		FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
				"Download", "Select where to download the results.", null, 
				true);
		IconManager icons = IconManager.getInstance(registry);
		chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
		chooser.setSelectedFileFull(data.getFileName());
		chooser.setApproveButtonText("Download");
		chooser.addPropertyChangeListener(new PropertyChangeListener() {
		
			public void propertyChange(PropertyChangeEvent evt) {
				String name = evt.getPropertyName();
				if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
					File[] files = (File[]) evt.getNewValue();
					File folder = files[0];
					if (data == null) return;
					OriginalFile f = (OriginalFile) data.getContent();
					IconManager icons = IconManager.getInstance(registry);
					DownloadActivityParam activity = 
						new DownloadActivityParam(f,
							folder, icons.getIcon(IconManager.DOWNLOAD_22));
					activity.setLegend(data.getDescription());
					activity.setLegendExtension(
							DownloadActivity.LEGEND_TEXT_CSV);
					viewer.notifyActivity(ctx, activity);
				}
			}
		});
		chooser.centerDialog();
	}
	
	/**
	 * Modifies the text of the component. 
	 * @see ActivityComponent#notifyActivityCancelled()
	 */
	protected void notifyActivityCancelled()
	{
		type.setText(DESCRIPTION_CANCEL);
	}

	/** 
	 * No-operation in this case.
	 * @see ActivityComponent#notifyActivityError()
	 */
	protected void notifyActivityError() {}
	
}
