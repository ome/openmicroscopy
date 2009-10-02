/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.importer.reactor;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a single selection by the user.
 * 
 * @DEV.TODO This class should be aligned with {@link ImportContainer}
 * @author Chris Allan <callan@blackcat.ca>
 */
public class Fileset
{
	private final static Log log = LogFactory.getLog(Fileset.class);
	
	private File target;
	
	private File[] usedFiles;
	
	private long bytesToUpload;
	
	/** Image name. */
	private String imageName;
	
	/** Image description. */
	private String imageDescription;
	
	/** Matching server side fileset's UUID. */
	private String filesetUUID;
	
	/** Upload state. */
	private FilesetState state;
	
	/** Exhaustive enumeration of upload states. */
	public enum FilesetState
	{
		QUEUED, ANALYZING, HANDLING, FINISHED, FAILED;
	}
	
	/**
	 * Instantiates a fileset upload context.
	 * @param connector Rollup server connector we're going to upload with.
	 * @param manuscript Manuscript this fileset is to be uploaded into.
	 * @param figure Figure this fileset is to be uploaded into.
	 * @param part Part this fileset is to be associated with.
	 * @param target Target file for upload.
	 * @param imageName Image name.
	 * @param imageDescription Image description.
	 */
	Fileset(File target, String imageName, String imageDescription)
	{
		this.target = target;
		this.imageName = imageName;
		this.imageDescription = imageDescription;
		this.state = FilesetState.QUEUED;
	}

	/**
	 * Returns the context's target file.
	 * @return See above.
	 */
	public File getTarget()
	{
		return target;
	}

	/**
	 * Returns the context's image name.
	 * @return See above.
	 */
	public String getImageName()
	{
		return imageName;
	}

	/**
	 * Returns the context's image description.
	 * @return See above.
	 */
	public String getImageDescription()
	{
		return imageDescription;
	}
	
	/**
	 * Returns the context's current state.
	 * @return See above.
	 */
	public FilesetState getState()
	{
		return state;
	}
	
	/**
	 * Promotes this context to the next state.
	 */
	public void promote()
	{
		switch (state)
		{
			case QUEUED:
			{
				state = FilesetState.ANALYZING;
				break;
			}
			case ANALYZING:
			{
				state = FilesetState.HANDLING;
				break;
			}
			case HANDLING:
			{
				state = FilesetState.FINISHED;
				break;
			}
			case FINISHED:
			{
				throw new IllegalStateException(
						"Cannot promote context's in the FINISHED state.");
			}
			case FAILED:
			{
				throw new IllegalStateException(
						"Cannot promote context's in the FAILED state.");
			}
		}
	}
	
	/**
	 * Retrieves a string for the context's current status.
	 * @return See above.
	 */
	public String getStatusString()
	{
		switch (state)
		{
			case QUEUED:
			{
				return "queued";
			}
			case ANALYZING:
			{
				return "analyzing";
			}
			case HANDLING:
			{
				return "uploading";
			}
			case FINISHED:
			{
				return "finished";
			}
			case FAILED:
			{
				return "failed";
			}
			default:
			{
				return "unknown";
			}
		}
	}

	/**
	 * Updates the uploads matching server side fileset UUID.
	 * @param filesetUUID The fileset UUID to set.
	 */
	public void setFilesetUUID(String filesetUUID)
	{
		this.filesetUUID = filesetUUID;
	}
	
	/**
	 * Retrieves the UUID of the matching server side fileset for this upload.
	 * @return See above.
	 */
	public String getFilesetUUID()
	{
		return filesetUUID;
	}
	
	/**
	 * Sets the Bio-Formats known used files for the target.
	 * @param usedFiles Array of used files to set.
	 */
	public void setUsedFiles(String[] usedFiles)
	{
		this.usedFiles = new File[usedFiles.length];
		for (int i = 0; i < usedFiles.length; i++)
		{
			File f = new File(usedFiles[i]);
			this.usedFiles[i] = f;
			bytesToUpload += f.length();
		}
	}
	
	/**
	 * Retrieves the current set of Bio-Formats known used files.
	 * @return See above.
	 */
	public File[] getUsedFiles()
	{
		return usedFiles;
	}
	
	/**
	 * Retrieves the current number of <b>total</b> bytes to upload. This is
	 * <code>0</code> until {@link setUsedFiles()} is called.
	 * @return See above.
	 */
	public long getBytesToUpload()
	{
		return bytesToUpload;
	}
	
	public boolean isFailed()
	{
	    return state.equals(FilesetState.FAILED);
	}
	
	/**
	 * Fails the upload context.
	 */
	public void fail()
	{
		if (state == FilesetState.FAILED)
		{
			log.warn("Trying to fail context that's already failed.");
		}
		state = FilesetState.FAILED;
	}
}
