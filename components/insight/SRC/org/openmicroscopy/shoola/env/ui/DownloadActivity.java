/*
 * org.openmicroscopy.shoola.env.ui.DownloadActivity 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import omero.model.OriginalFile;

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.util.filter.file.CustomizedFileFilter;
import org.openmicroscopy.shoola.util.filter.file.HTMLFilter;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.OMETIFFFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.filter.file.TIFFFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Activity to download an image or file.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class DownloadActivity extends ActivityComponent {

	/** Open the file in the Browser. */
	private static final String FILE = "file://";

	/** The description of the activity when finished. */
	private static final String DESCRIPTION = "File downloaded";

	/** The description of the activity when cancelled. */
	private static final String DESCRIPTION_CANCEL = "Download cancelled";

	/** The text and extension added to the name of the file. */
	public static final String LEGEND_TEXT = "_legend.txt";

	/** The text and extension added to the name of the file. */
	public static final String LEGEND_TEXT_CSV = "_legend.csv";

	/** The parameters hosting information about the file to download. */
	protected DownloadActivityParam parameters;

	/** The name of the file. */
	protected String fileName;

	/** Reference to the file to load. */
	protected File file;

	/** The local name of the file. */
	private String localFileName;

	/** Overwrite if local file already exists */
	private boolean overwrite = false;
	
	/** The supported file filters. */
	private static final List<CustomizedFileFilter> FILTERS;

	static {
		FILTERS = new ArrayList<CustomizedFileFilter>();
		FILTERS.add(new JPEGFilter());
		FILTERS.add(new PNGFilter());
		FILTERS.add(new HTMLFilter());
		FILTERS.add(new TIFFFilter());
	}

	/**
	 * Returns the name of the file.
	 * 
	 * @return See above.
	 */
	private String getFileName() {
		File folder = parameters.getFolder();
		File directory = folder;

		directory = folder.getParentFile();

		File[] files = directory.listFiles();
		String dirPath = directory.getAbsolutePath() + File.separator;
		String value = folder.getName();

		if (parameters.getFileName() != null)
			value = parameters.getFileName();
		String extension = null;
		if (value != null && value.trim().length() > 0) {
			int lastDot = value.lastIndexOf(".");
			if (lastDot == -1) { // no extension specified.
				// get the extension from the file.
				String s = parameters.getOriginalFileName();
				if (s.endsWith(OMETIFFFilter.OME_TIF)
						|| s.endsWith(OMETIFFFilter.OME_TIFF))
					extension = OMETIFFFilter.OME_TIFF;
				else {
					lastDot = s.lastIndexOf(".");
					if (lastDot != -1)
						extension = s.substring(lastDot, s.length());

				}
				if (extension != null)
					value = value + extension;
			}
			return getFileName(files, value, value, dirPath, 1, extension);
		}
		value = parameters.getOriginalFileName();
		;
		if (value == null || value.length() == 0)
			return "";
		return getFileName(files, value, value, dirPath, 1, null);
	}

	/**
	 * Returns <code>true</code> if the file can be opened, <code>false</code>
	 * otherwise.
	 * 
	 * @param path
	 *            The path to handle.
	 * @return See above.
	 */
	private boolean canOpenFile(String path) {
		Iterator<CustomizedFileFilter> i = FILTERS.iterator();
		CustomizedFileFilter filter;
		while (i.hasNext()) {
			filter = i.next();
			if (filter.accept(path))
				return true;
		}
		return false;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer
	 *            The viewer this data loader is for. Mustn't be
	 *            <code>null</code>.
	 * @param registry
	 *            Convenience reference for subclasses.
	 * @param ctx
	 *            The security context.
	 * @param parameters
	 *            The parameters used to export the image.
	 */
	public DownloadActivity(UserNotifier viewer, Registry registry,
			SecurityContext ctx, DownloadActivityParam parameters) {

		super(viewer, registry, ctx);

		if (parameters == null)
			throw new IllegalArgumentException("Parameters not valid.");
		this.parameters = parameters;

		initialize("Download", parameters.getIcon());

		File folder = parameters.getFolder();

		fileName = getFileName();
		if (folder.isDirectory())
			localFileName = folder + File.separator + fileName;
		else
			localFileName = folder.toString();
		messageLabel.setText(localFileName);
		
		this.overwrite = parameters.isOverwrite();
	}

	/**
	 * Creates a concrete loader.
	 * 
	 * @see ActivityComponent#createLoader()
	 */
	protected UserNotifierLoader createLoader() {
		OriginalFile f = parameters.getFile();
		File folder = parameters.getFolder();

		if (folder.isDirectory()) file = new File(folder, fileName);
		else file = folder;

		registry.getLogger().debug(this, file.getAbsolutePath());

		boolean load = true;
		if (file.exists() && !overwrite)
			load = false;

		switch (parameters.getIndex()) {
			case DownloadActivityParam.FILE_ANNOTATION:
			case DownloadActivityParam.ORIGINAL_FILE:
			case DownloadActivityParam.METADATA_FROM_IMAGE:
				loader = new FileLoader(viewer, registry, ctx, file,
						parameters.getId(), parameters.getIndex(), load, this);
				break;
			default:
				loader = new FileLoader(viewer, registry, ctx, file, f.getId()
						.getValue(), load, this);
		}
		return loader;
	}

	/**
	 * Modifies the text of the component.
	 * 
	 * @see ActivityComponent#notifyActivityEnd()
	 */
	protected void notifyActivityEnd() {
		type.setText(DESCRIPTION);
		String name = null;
		String legend = parameters.getLegend();
		if (legend != null && legend.trim().length() > 0) {
			// Write the description if any
			File folder = parameters.getFolder();
			File directory = folder.getParentFile();
			BufferedWriter out = null;
			String n = UIUtilities.removeFileExtension(fileName);
			String ext = LEGEND_TEXT;
			String le = parameters.getLegendExtension();
			if (le != null && le.length() > 0)
				ext = le;
			try {
				name = directory + File.separator + n;
				name += ext;
				out = new BufferedWriter(new FileWriter(name));
				out.write(legend);
				out.close();
			} catch (Exception e) {
				try {
					if (out != null)
						out.close();
				} catch (Exception ex) {
				}
			}
		}
		if (localFileName == null)
			return;
		if (canOpenFile(localFileName)) {
			String url;
			if (UIUtilities.isMacOS())
				url = FILE + localFileName;
			else
				url = FILE + "/" + localFileName;
			registry.getTaskBar().openURL(url);
		}
		
		if (parameters.getToDelete() != null) {
			List<DeletableObject> tmp = new ArrayList<DeletableObject>();
			tmp.add(new DeletableObject(parameters.getToDelete()));
			DataObjectRemover eraser = new DataObjectRemover(viewer, registry,
					tmp, null);
			eraser.load();
		}
	}

	/**
	 * Modifies the text of the component.
	 * 
	 * @see ActivityComponent#notifyActivityCancelled()
	 */
	protected void notifyActivityCancelled() {
		type.setText(DESCRIPTION_CANCEL);
	}

	/**
	 * No-operation in this case.
	 * 
	 * @see ActivityComponent#notifyActivityError()
	 */
	protected void notifyActivityError() {}

}
