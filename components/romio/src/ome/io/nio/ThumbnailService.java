/*
 * ome.io.nio.ThumbnailService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ome.conditions.ResourceError;
import ome.model.display.Thumbnail;
import ome.util.Utils;

/**
 * @author callan
 * 
 */
public class ThumbnailService extends AbstractFileSystemService {

	/* The logger for this class. */
	private transient static Logger log = LoggerFactory
			.getLogger(ThumbnailService.class);

	/**
	 * Constructor
	 * @param path
	 */
	public ThumbnailService(String path) {
		super(path);
	}

	/**
	 * Creates thumbnail on disk using byte array
	 * 
	 * @param thumbnail
	 * @param buf
	 * @throws IOException
	 */
	public void createThumbnail(Thumbnail thumbnail, byte[] buf)
			throws IOException {
		String path = getThumbnailPath(thumbnail.getId());
		createSubpath(path);

		FileOutputStream stream = new FileOutputStream(path);
		stream.write(buf);
		stream.close();
	}

	/**
	 * Returns length of Thumbnail on disk
	 * 
	 * @param thumbnail
	 */
	public long getThumbnailLength(Thumbnail thumbnail) {
		File f = new File(getThumbnailPath(thumbnail.getId()));
		return f.length();
	}

	/**
	 * Return byte array of Thumbnail
	 * 
	 * @param thumbnail
	 * @throws IOException
	 */
	public byte[] getThumbnail(Thumbnail thumbnail) throws IOException {
		byte[] buf = new byte[(int) getThumbnailLength(thumbnail)];
		return getThumbnail(thumbnail, buf);
	}

	/**
	 * Return byte array of Thumbnail, providing byte array
	 * 
	 * @param thumbnail
	 * @param buf
	 * @throws IOException
	 */
	public byte[] getThumbnail(Thumbnail thumbnail, byte[] buf)
			throws IOException {
		String path = getThumbnailPath(thumbnail.getId());
		FileInputStream stream = new FileInputStream(path);
		try {
		    stream.read(buf, 0, buf.length);
		} finally {
		    Utils.closeQuietly(stream);
		}
		return buf;
	}

	/**
	 * Return FileOutputStream of Thumbnail
	 * 
	 * @param thumbnail
	 * @throws IOException
	 */
	public FileOutputStream getThumbnailOutputStream(Thumbnail thumbnail)
			throws IOException {
		String path = getThumbnailPath(thumbnail.getId());
		createSubpath(path);
		return new FileOutputStream(path);
	}

    /**
     * Returns whether or not a thumbnail exists on disk.
     * 
     * @param thumbnail The thumbnail metadata.
     * @return See above.
     */
    public boolean getThumbnailExists(Thumbnail thumbnail)
            throws IOException {
        String path = getThumbnailPath(thumbnail.getId());
        return new File(path).exists();
    }

	/**
	 * Removes files from data repository based on a parameterized List of Long
	 * thumbnail ids
	 * 
	 * @param thumbnailIds -
	 *            Long file keys to be deleted
	 * @throws ResourceError If deletion fails.
	 */
	public void removeThumbnails(List<Long> thumbnailIds) {
		File file;
		boolean success = false;

		for (Long id : thumbnailIds)
		{
			String thumbnailPath = getThumbnailPath(id);
			file = new File(thumbnailPath);
			if (file.exists())
			{
				success = file.delete();
				if (!success)
				{
					throw new ResourceError("Thumbnail " + file.getName()
							+ " deletion failed");
				}
				else
				{
					if (log.isInfoEnabled())
					{
						log.info("INFO: Thumbnail " + file.getName()
								+ " deleted.");
					}
				}
			}
		}
	}
}
