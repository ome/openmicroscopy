/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.StatusLabel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.util;


//Java imports
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ErrorHandler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;

/**
 * Component displaying the status of a specific import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class StatusLabel
	extends JPanel
	implements IObserver
{
	
	/** The text displayed when the file is already selected.*/
	public static final String DUPLICATE = "Already processed, skipping";
	
	/** The text displayed when loading the image to import. */
	public static final String PREPPING_TEXT = "prepping";
	
	/** The text indicating the scanning steps. */
	public static final String SCANNING_TEXT = "Scanning...";
	
	/** 
	 * Bound property indicating that the original container has been reset.
	 * */
	public static final String NO_CONTAINER_PROPERTY = "noContainer";
	
	/** Bound property indicating that children files have been set. */
	public static final String FILES_SET_PROPERTY = "filesSet";
	
	/** 
	 * Bound property indicating that the file has to be reset
	 * This should be invoked if the log file for example has been selected. 
	 */
	public static final String FILE_RESET_PROPERTY = "fileReset";
	
	/** Bound property indicating that the import of the file has started. */
	public static final String FILE_IMPORT_STARTED_PROPERTY =
		"fileImportStarted";
	
	/** Bound property indicating that the file is imported. */
	public static final String FILE_IMPORTED_PROPERTY = "fileImported";
	
	/** 
	 * Bound property indicating that the container corresponding to the
	 * folder has been created. 
	 * */
	public static final String CONTAINER_FROM_FOLDER_PROPERTY =
		"containerFromFolder";
	
	/** Bound property indicating that the status has changed.*/
	public static final String CANCELLABLE_IMPORT_PROPERTY =
		"cancellableImport";
	
	/** Bound property indicating that the status has changed.*/
	public static final String CANCELLED_IMPORT_PROPERTY = "cancelledImport";
	
	/** Bound property indicating that the debug text has been sent.*/
	public static final String DEBUG_TEXT_PROPERTY = "debugText";
	
	/** Bound property indicating that the fileset has finished uploading.*/
	public static final String FILESET_UPLOADED_PROPERTY = "filesetUploaded";
	
	/** Default text when a failure occurred. */
	private static final String FAILURE_TEXT = "failed";

	/** The default text of the component.*/
	private static final String DEFAULT_TEXT = "Pending...";

	/** Text to indicate that the import is cancelled. */
	private static final String CANCEL_TEXT = "cancelled";

	/** Text to indicate that no files to import. */
	private static final String NO_FILES_TEXT = "No Files to Import.";

	/** 
	 * The number of processing sets.
	 * 1. Metadata imported
	 * 2. Pixels Processed
	 * 3. Thumbnails generation
	 * 4. Metadata processed
	 */
	/** Map hosting the description of each steps.*/
	private static final Map<Integer, String> STEPS;
	
	static {
		STEPS = new HashMap<Integer, String>();
		STEPS.put(1, "Metadata imported");
		STEPS.put(2, "Pixels processed");
		STEPS.put(3, "Thumbnails generated");
		STEPS.put(4, "Processing complete");
	}
	
	/** The number of images in a series. */
	private int seriesCount;
	
	/** The type of reader used. */
	private String readerType;
	
	/** The files associated to the file that failed to import. */
	private String[] usedFiles;
	
	/** The text if an error occurred. */
	private String errorText;
	
	/** Flag indicating that the import has been cancelled. */
	private boolean markedAsCancel;
	
	/** Flag indicating that the import can or not be cancelled.*/
	private boolean cancellable;
	
	/** 
	 * Flag indicating that the file has already been imported or already
	 * in the queue.
	 */
	private boolean markedAsDuplicate;
	
	/** The size of the file.*/
	private String fileSize;
	
	/** The total size of uploaded files.*/
	private long totalUploadedSize;
	
	/** The label displaying the upload information.*/
	private JLabel uploadLabel;
	
	/** The label displaying the general import information.*/
	private JLabel generalLabel;
	
	/** Indicate the progress of the upload.*/
	private JProgressBar uploadBar;
	
	/** Indicate the progress of the processing.*/
	private JProgressBar processingBar;
	
	/** The label indicating information posted during the processing.*/
	private JLabel processingLabel;
	
	/** The size of the upload,*/
	private long sizeUpload;
	
	/**
	 * Builds and lays out the components indicating the status of the upload.
	 * 
	 * @return See above
	 */
	private JPanel buildUploadPane()
	{
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(uploadBar);
		p.add(uploadLabel);
		return p;
	}
	
	/**
	 * Builds and lays out the components indicating the status of the
	 * processing.
	 * 
	 * @return See above
	 */
	private JPanel buildProcessingPane()
	{
		JPanel p = new JPanel();
		p.setOpaque(false);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(processingBar);
		p.add(processingLabel);
		return p;
	}
	
	/** 
	 * Formats the size of the uploaded data.
	 * 
	 * @param value The value to display.
	 * @return See above.
	 */
	private String formatUpload(long value)
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(UIUtilities.formatFileSize(value));
		buffer.append("/");
		buffer.append(fileSize);
		return buffer.toString();
	}

	/** Builds and lays out the UI.*/
	private void buildUI()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(generalLabel);
		add(buildUploadPane());
		add(Box.createHorizontalStrut(5));
		add(buildProcessingPane());
		setOpaque(false);
	}

	/** Initializes the components.*/
	private void initiliaze()
	{
		sizeUpload = 0;
		fileSize = "";
		seriesCount = 0;
		readerType = "";
		errorText = FAILURE_TEXT;
		markedAsCancel = false;
		cancellable = true;
		totalUploadedSize = 0;
		generalLabel = new JLabel(DEFAULT_TEXT);
		Font f = generalLabel.getFont();
		Font derived = f.deriveFont(f.getStyle(), f.getSize()-2);
		uploadLabel = new JLabel();
		uploadLabel.setFont(derived);
		uploadBar = new JProgressBar(0, 100);
		uploadBar.setFont(derived);
		uploadBar.setStringPainted(true);
		processingBar = new JProgressBar(0, STEPS.size());
		processingBar.setStringPainted(true);
		processingBar.setFont(derived);
		processingLabel = new JLabel();
		processingLabel.setFont(derived);
		uploadLabel.setVisible(false);
		uploadBar.setVisible(false);
		processingBar.setVisible(false);
	}

	/** Handles error that occurred during the processing.*/
	private void handleProcessingError()
	{
		//Change the color of the processing bar.
		cancellable = false;
		firePropertyChange(CANCELLABLE_IMPORT_PROPERTY, null, this);
	}

	/** Creates a new instance. */
	public StatusLabel()
	{
		initiliaze();
		buildUI();
	}

	/**
	 * Sets the collection of files to import.
	 * 
	 * @param usedFiles The value to set.
	 */
	public void setUsedFiles(String[] usedFiles)
	{
		this.usedFiles = usedFiles;
		if (usedFiles == null) return;
		for (int i = 0; i < usedFiles.length; i++) {
			sizeUpload += (new File(usedFiles[i])).length();
		}
		fileSize = FileUtils.byteCountToDisplaySize(sizeUpload);
	}

	/** Marks the import has cancelled. */
	public void markedAsCancel()
	{
		generalLabel.setText(CANCEL_TEXT);
		this.markedAsCancel = true;
	}
	
	/**
	 * Returns <code>true</code> if the import is marked as cancel,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isMarkedAsCancel() { return markedAsCancel; }

	/** Marks the import has duplicate. */
	public void markedAsDuplicate() { this.markedAsDuplicate = true; }

	/**
	 * Returns <code>true</code> if the import is marked as duplicate,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isMarkedAsDuplicate() { return markedAsDuplicate; }

	/**
	 * Returns the text if an error occurred.
	 * 
	 * @return See above.
	 */
	public String getErrorText() { return errorText; }

	/**
	 * Returns the type of reader used.
	 * 
	 * @return See above.
	 */
	public String getReaderType() { return readerType; }

	/**
	 * Returns the files associated to the file failing to import.
	 * 
	 * @return See above.
	 */
	public String[] getUsedFiles() { return usedFiles; }

	/**
	 * Sets the status of the import.
	 * 
	 * @param value The value to set.
	 */
	public void setStatus(String value)
	{
		if (value == null) value = "";
		generalLabel.setText(value);
	}

	/**
	 * Fires a property indicating to import the files.
	 * 
	 * @param files The file to handle.
	 */
	public void setFiles(Map<File, StatusLabel> files)
	{
		generalLabel.setText(NO_FILES_TEXT);
		firePropertyChange(FILES_SET_PROPERTY, null, files);
	}

	/**
	 * Indicates that the original container has been reset.
	 */
	public void setNoContainer()
	{
		firePropertyChange(NO_CONTAINER_PROPERTY,
				Boolean.valueOf(false), Boolean.valueOf(true));
	}

	/**
	 * Sets the container corresponding to the folder.
	 * 
	 * @param container The container to set.
	 */
	public void setContainerFromFolder(DataObject container)
	{
		firePropertyChange(CONTAINER_FROM_FOLDER_PROPERTY, null, container);
	}

	/**
	 * Replaces the initial file by the specified one. This should only be
	 * invoked if the original file was an arbitrary one requiring to use the
	 * import candidate e.g. <code>.log</code>.
	 * 
	 * @param file The new file.
	 */
	public void resetFile(File file)
	{
		firePropertyChange(FILE_RESET_PROPERTY, null, file);
	}

	/**
	 * Returns the number of series.
	 * 
	 * @return See above.
	 */
	public int getSeriesCount() { return seriesCount; }

	/**
	 * Fires a property indicating that the file has been imported.
	 * 
	 * @param file The file to import.
	 * @param result The result.
	 */
	public void setFile(File file, Object result)
	{
		Object[] results = new Object[2];
		results[0] = file;
		results[1] = result;
		cancellable = false;
		firePropertyChange(FILE_IMPORTED_PROPERTY, null, results);
	}

	/**
	 * Returns <code>true</code> if the import can be cancelled,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isCancellable() { return cancellable; }

	/**
	 * Sets the text of the labels.
	 * 
	 * @param text The value to set.
	 */
	public void setText(String text)
	{
		generalLabel.setText(text);
		uploadLabel.setText("");
	}

	/**
	 * Displays the status of an on-going import.
	 * @see IObserver#update(IObservable, ImportEvent)
	 */
	public void update(IObservable observable, ImportEvent event)
	{
		if (event == null) return;
		cancellable = false;
		if (event instanceof ImportEvent.IMPORT_DONE) {
		} else if (event instanceof ImportCandidates.SCANNING) {
			if (!markedAsCancel) generalLabel.setText("scanning");
		} else if (event instanceof ErrorHandler.FILE_EXCEPTION) {
			ErrorHandler.FILE_EXCEPTION e = (ErrorHandler.FILE_EXCEPTION) event;
			readerType = e.reader;
			usedFiles = e.usedFiles;
		} else if (event instanceof ErrorHandler.UNKNOWN_FORMAT) {
			errorText = "unknown format";
			handleProcessingError();
		} else if (event instanceof ErrorHandler.MISSING_LIBRARY) {
			errorText = "missing required library";
			handleProcessingError();
		} else if (event instanceof ImportEvent.FILE_UPLOAD_BYTES) {
			ImportEvent.FILE_UPLOAD_BYTES e =
				(ImportEvent.FILE_UPLOAD_BYTES) event;
			long v = totalUploadedSize+e.uploadedBytes;
			uploadBar.setString(formatUpload(v));
			uploadBar.setValue((int) (v*100/sizeUpload));
			StringBuffer buffer = new StringBuffer();
			if (e.timeLeft != 0) {
				String s = UIUtilities.calculateHMSFromMilliseconds(e.timeLeft);
				buffer.append(s);
				if (!StringUtils.isBlank(s)) buffer.append(" left");
				else buffer.append("Almost complete");
			} else {
				if (!StringUtils.isBlank(uploadLabel.getText()))
					buffer.append("Almost complete");
			}
			uploadLabel.setText(buffer.toString());
		} else if (event instanceof ImportEvent.FILE_UPLOAD_COMPLETE) {
			ImportEvent.FILE_UPLOAD_COMPLETE e =
				(ImportEvent.FILE_UPLOAD_COMPLETE) event;
			totalUploadedSize += e.uploadedBytes;
		} else if (event instanceof ImportEvent.FILESET_UPLOAD_END) {
			uploadLabel.setText("uploaded");
		} else if (event instanceof ImportEvent.METADATA_IMPORTED) {
			processingBar.setVisible(true);
			processingLabel.setVisible(true);
			processingBar.setValue(1);
			processingLabel.setText(STEPS.get(1));
		} else if (event instanceof ImportEvent.PIXELDATA_PROCESSED) {
			processingBar.setValue(2);
			processingLabel.setText(STEPS.get(2));
		} else if (event instanceof ImportEvent.THUMBNAILS_GENERATED) {
			processingBar.setValue(3);
			processingLabel.setText(STEPS.get(3));
		} else if (event instanceof ImportEvent.METADATA_PROCESSED) {
			processingBar.setValue(4);
			processingLabel.setText(STEPS.get(4));
		} else if (event instanceof ImportEvent.FILESET_UPLOAD_START) {
			generalLabel.setText("");
			uploadLabel.setVisible(true);
			uploadBar.setVisible(true);
		}
	}

}
