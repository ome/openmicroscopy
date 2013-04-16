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
import java.util.ArrayList;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.FileUploadCounter.ProgressListener;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brian W. Loranger
 *
 */
public class FileUploader implements IObservable
{

    private static Logger         log = LoggerFactory.getLogger(FileUploader.class);

    private String[] files;

    private String session_id;

    private PostMethod method = null;


    ArrayList<IObserver> observers = new ArrayList<IObserver>();

    private HttpClient client;

    private boolean cancelUpload;

    /**
     * Initialize upload with httpClient
     *
     * @param httpClient
     */
    public FileUploader(HttpClient httpClient)
    {
        this.client = httpClient;
    }

    /**
     * Set specific sessionID
     *
     * @param sessionId
     */
    public void setSessionId(String sessionId)
    {
        if (sessionId != null)
            this.session_id = sessionId;
        else
        {
            this.session_id = java.util.UUID.randomUUID().toString().replace("-", "");
            log.warn("FileUploadContainer has not set session_id, autogenerating session id of: " + session_id);
        }

    }

    /**
     * @return session_id
     */
    public String getSessionId()
    {
        return this.session_id;
    }

    /**
     * Upload files from error container to url
     *
     * @param url - url to send to
     * @param timeout - timeout
     * @param upload - error container with files in it
     */
    public void uploadFiles(String url, int timeout, ErrorContainer upload)
    {
        if (client == null)
            client = new HttpClient();

        this.files = upload.getFiles();
        setSessionId(upload.getToken());

        int fileCount = 0;

        for (String f : files)
        {
            if (cancelUpload)
            {
                System.err.println(cancelUpload);
                continue;
            }

            fileCount++;
            final int count = fileCount;
            final File file = new File(f);

            try {
                HttpClientParams params = new HttpClientParams();
                params.setConnectionManagerTimeout(timeout);
                client.setParams(params);

                method = new PostMethod(url);

                String format = "";

                if (upload.getFileFormat() != null)
                    format = upload.getFileFormat();
                else
                    format = "unknown";


                final ErrorFilePart errorFilePart = new ErrorFilePart("Filedata", file);

                Part[] parts ={
                        new StringPart("token", upload.getToken()),
                        new StringPart("file_format", format),
                        errorFilePart
                        };

                final long fileLength = file.length();

                MultipartRequestEntity mpre =
                    new MultipartRequestEntity(parts, method.getParams());

                ProgressListener listener = new ProgressListener(){

                    private long partsTotal = -1;

                    /* (non-Javadoc)
                     * @see ome.formats.importer.util.FileUploadCounter.ProgressListener#update(long)
                     */
                    public void update(long bytesRead)
                    {

			if (cancelUpload) errorFilePart.cancel = true;

                        long partsDone = 0;
                        long parts = (long) Math.ceil(fileLength / 10.0f);
                        if (fileLength != 0) partsDone = bytesRead / parts;

                        if (partsTotal == partsDone) {
                            return;
                        }
                        partsTotal = partsDone;

                        notifyObservers(new ImportEvent.FILE_UPLOAD_STARTED(
                                file.getName(), count, files.length, null, null, null));

                        long uploadedBytes = bytesRead/2;
                        if (fileLength == -1) {

                            notifyObservers(new ImportEvent.FILE_UPLOAD_BYTES(
                                    file.getName(), count, files.length, uploadedBytes, null, null, null));

                        } else {

                            notifyObservers(new ImportEvent.FILE_UPLOAD_BYTES(
                                    file.getName(), count, files.length, uploadedBytes, fileLength, null, null));

                        }
                    }
                };

                FileUploadCounter hfre = new FileUploadCounter(mpre, listener);

                method.setRequestEntity(hfre);

                int status = client.executeMethod(method);

                if (status == HttpStatus.SC_OK) {

                    notifyObservers(new ImportEvent.FILE_UPLOAD_COMPLETE(
                            file.getName(), count, files.length, null, null, null));
                    log.info("Uploaded file '" + file.getName() + "' to QA system");
                    upload.setStatus(1);

                } else {
                    notifyObservers(new ImportEvent.FILE_UPLOAD_COMPLETE(
                            file.getName(), count, files.length, null, null, null));
                }
            } catch (Exception ex) {
                notifyObservers(new ImportEvent.FILE_UPLOAD_ERROR(
                        file.getName(), count, files.length, null, null, ex));
            }
        }

        notifyObservers(new ImportEvent.FILE_UPLOAD_FINISHED(
                null, 0, 0, null, null, null));

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

    /**
     * cancel's upload
     */
    public void cancel()
    {
        this.cancelUpload = true;
    }

    /**
     * Main for testing (debugging only)
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args)
    {

        String url = "http://mage.openmicroscopy.org.uk/qa/processing/";
        String dvPath = "/Users/TheBrain/test_images_shortrun/dv/";
        String[] files = {dvPath + "CFPNEAT01_R3D.dv", dvPath + "IAGFP-Noc01_R3D.dv"};


        FileUploader uploader = new FileUploader(new HttpClient());
        ErrorContainer upload = new ErrorContainer();
        upload.setFiles(files);
        uploader.uploadFiles(url, 5000, upload);
    }
}
