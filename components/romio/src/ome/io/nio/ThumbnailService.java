/*
 * ome.io.nio.PixelsService
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import ome.model.display.Thumbnail;

/**
 * @author callan
 * 
 */
public class ThumbnailService extends AbstractFileSystemService {

    public ThumbnailService(String path) {
        super(path);
    }

    public void createThumbnail(Thumbnail thumbnail, byte[] buf)
            throws IOException {
        String path = getThumbnailPath(thumbnail.getId());
        createSubpath(path);

        FileOutputStream stream = new FileOutputStream(path);
        stream.write(buf);
        stream.close();
    }

    public long getThumbnailLength(Thumbnail thumbnail) {
        File f = new File(getThumbnailPath(thumbnail.getId()));
        return f.length();
    }

    public byte[] getThumbnail(Thumbnail thumbnail) throws IOException {
        byte[] buf = new byte[(int) getThumbnailLength(thumbnail)];
        return getThumbnail(thumbnail, buf);
    }

    public byte[] getThumbnail(Thumbnail thumbnail, byte[] buf)
            throws IOException {
        String path = getThumbnailPath(thumbnail.getId());
        FileInputStream stream = new FileInputStream(path);
        stream.read(buf, 0, buf.length);
        stream.close();
        return buf;
    }

    public FileOutputStream getThumbnailOutputStream(Thumbnail thumbnail)
            throws IOException {
        String path = getThumbnailPath(thumbnail.getId());
        createSubpath(path);
        return new FileOutputStream(path);
    }
}
