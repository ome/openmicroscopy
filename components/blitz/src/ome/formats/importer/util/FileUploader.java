/*
 * ome.formats.importer.gui.History
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
package ome.formats.importer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.common.io.Files;

/**
 * @author Brian W. Loranger
 *
 */
public class FileUploader implements IObservable
{

    /** The extension used for the zip.*/
    public static final String ZIP_EXTENSION = ".zip";

    /** Identifies the token returned by the server. */
    private static final String TOKEN = "token";

    /** Identifies the reader used. */
    private static final String READER = "file_format";

    /** Identifies the <code>file</code> to send. */
    private static final String FILE = "Filedata";


    /** The collection of observers.*/
    private List<IObserver> observers = new ArrayList<IObserver>();

    /** The http client.*/
    private CloseableHttpClient client;

    /**
     * Initialize upload with httpClient
     *
     * @param httpClient
     */
    public FileUploader(CloseableHttpClient httpClient)
    {
        this.client = httpClient;
    }

    /**
     * Zips directory.
     * 
     * @param directory The directory to zip.
     * @param out The output stream.
     * @throws Exception Thrown if an error occurred during the operation.
     */
    private void zipDir(File directory, ZipOutputStream out,
            String parentDirectoryName)
            throws Exception
    {
        File[] entries = directory.listFiles();
        byte[] buffer = new byte[4096]; // Create a buffer for copying
        int bytesRead;
        FileInputStream in;
        File f;
        for (int i = 0; i < entries.length; i++) {
            f = entries[i];
            if (f.isHidden())
                continue;
            if (f.isDirectory()) {
                zipDir(f, out, f.getName());
                continue;
            }
            in = new FileInputStream(f); // Stream to read file
            String zipName = f.getName();
            if (!StringUtils.isEmpty(parentDirectoryName)) {
                zipName = FilenameUtils.concat(parentDirectoryName, zipName);
            }
            out.putNextEntry(new ZipEntry(zipName)); // Store entry
            while ((bytesRead = in.read(buffer)) != -1)
                out.write(buffer, 0, bytesRead);
            out.closeEntry();
            in.close();
        }
    }

    private File zipDirectory(File zip, boolean compress)
            throws Exception
    {
        if (zip == null)
            throw new IllegalArgumentException("No name specified.");
        if (!zip.isDirectory() || !zip.exists())
            throw new IllegalArgumentException("Not a valid directory.");
        //Check if the name already has the extension
        String extension = FilenameUtils.getExtension(zip.getName());
        String name = zip.getName();
        if (StringUtils.isEmpty(extension) ||
                !ZIP_EXTENSION.equals("."+extension)) {
            name += ZIP_EXTENSION;
        }
        File file = new File(zip.getParentFile(), name);
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new FileOutputStream(file));
            if (!compress) out.setLevel(ZipOutputStream.STORED);
            zipDir(zip, out, null);
        } catch (Exception e) {
            throw new Exception("Cannot create the zip.", e);
        } finally {
            if (out != null) out.close();
        }
        return file;
    }

    /**
     * Upload files from error container to url
     *
     * @param url The URL to use.
     * @param timeout - timeout
     * @param upload - error container with files in it
     */
    public void uploadFiles(String path, int timeout, ErrorContainer upload)
        throws HtmlMessengerException
    {
        String[] files = upload.getFiles();
        if (files == null || files.length == 0) return;
        File directory = Files.createTempDir();
        File file = null;
        //Create request.
        String r ="unknown";
        if (upload.getFileFormat() != null) {
            r = upload.getFileFormat();
        }
        InputStreamReader reader;
        try {
            if (files.length > 1) {
                for (String f : files) {
                    FileUtils.copyFileToDirectory(new File(f),
                            directory, true);
                }
                //submit the zip
                file = zipDirectory(directory, false);
            } else {
                file = new File(files[0]);
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            builder.addPart(FILE, new FileBody(file,
                    ContentType.APPLICATION_OCTET_STREAM, file.getName()));
            builder.addPart(TOKEN, new StringBody(upload.getToken(),
                    ContentType.TEXT_PLAIN));
            builder.addPart(READER, new StringBody(r, ContentType.TEXT_PLAIN));
            HttpPost request = new HttpPost(path);
            request.setEntity(builder.build());
            // Execute the POST method
            CloseableHttpResponse response = client.execute(request);
            //response, not sure what we want to do with it.
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                reader = new InputStreamReader(entity.getContent());
                char[] buf = new char[32678];
                StringBuilder str = new StringBuilder();
                for (int n; (n = reader.read(buf)) != -1;)
                    str.append(buf, 0, n);
                String s = str.toString();
                //Decide v
                notifyObservers(new ImportEvent.FILE_UPLOAD_FINISHED(
                        null, 0, 0, null, null, null));
            }
        } catch( Exception e ) {
            throw new HtmlMessengerException("Cannot Connect", e);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception ex) {}
            }
            try {
                FileUtils.deleteDirectory(directory);
            } catch (Exception ex) {}
            if (file != null && files.length > 1) file.delete();
        }
    }

    // Observable methods


    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#addObserver(ome.formats.importer.IObserver)
     */
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#deleteObserver(ome.formats.importer.IObserver)
     */
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);

    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#notifyObservers(ome.formats.importer.ImportEvent)
     */
    public void notifyObservers(ImportEvent event)
    {
        for (IObserver observer:observers)
        {
            observer.update(this,event);
        }
    }

}
