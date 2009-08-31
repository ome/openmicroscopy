package ome.formats.importer.util;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.util.FileUploadCounter.ProgressListener;


public class FileUploader implements IObservable
{   

    private static Log         log = LogFactory.getLog(FileUploader.class);

    private String url;
    private String[] files;

    private String session_id;


    ArrayList<IObserver> observers = new ArrayList<IObserver>();

    private HttpClient client;

    public FileUploader()
    {
    }
    
    public FileUploader(HttpClient httpClient)
    {
        this.client = httpClient;
        
    }

    public void setSessionId(String sessionId)
    {
        if (sessionId != null)
            this.session_id = sessionId;
        else
        {
            this.session_id = java.util.UUID.randomUUID().toString().replace("-", "");
            log.warn("FileUploadContainer has not set session_id, autogenerating session id of: " + session_id);
        }


        System.err.println(session_id);
    }

    public String getSessionId()
    {
        return this.session_id;
    }
    
    public void uploadFiles(String url, int timeout, ErrorContainer upload)
    {
        if (client == null)
            client = new HttpClient();
        
        this.url = url;
        this.files = upload.getFiles();
        System.err.println(files.length);
        setSessionId(upload.getToken());

        PostMethod method = null;

        // observer arguements passed to any observers
        // [0] is filename
        // [1] is file index from files
        // [2] is file total from files
        // [3] is uploaded bytes
        // [4] is content length
        // [5] is any errors passed back from error capture
        final Object[] observerArgs;
        observerArgs = new Object[6];
        int fileCount = 0;
        
        for (String f : files)
        {
            fileCount++;

            System.err.println(f);

            File file = new File(f);

            observerArgs[0] = file.getName();
            observerArgs[1] = fileCount;
            observerArgs[2] = files.length;
            observerArgs[3] = null;
            observerArgs[4] = null;
            observerArgs[5] = null;

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
                    
                
                Part[] parts ={ 
                        new StringPart("token", upload.getToken()), 
                        new StringPart("file_format", format), 
                        new FilePart("Filedata", file) 
                        };
                
                final long fileLength = file.length();

                MultipartRequestEntity mpre = 
                    new MultipartRequestEntity(parts, method.getParams());

                ProgressListener listener = new ProgressListener(){

                    private long parts = -1;

                    public void update(long bytesRead)
                    {
                        long partsDone = bytesRead / (fileLength/10);
                        if (parts == partsDone) {
                            return;
                        }
                        parts = partsDone;

                        notifyObservers(Actions.FILE_UPLOAD_STARTED, observerArgs);

                        //System.out.println("We are currently reading item " + observerArgs[1] + " of " + observerArgs[2]);
                        long uploadedBytes = bytesRead/2;
                        if (fileLength == -1) {

                            observerArgs[3] = uploadedBytes;
                            notifyObservers(Actions.FILE_UPLOAD_BYTES, observerArgs);

                            //System.out.println("So far, " + uploadedBytes + " have been sent.");


                        } else {

                            observerArgs[3] = uploadedBytes;
                            observerArgs[4] = fileLength;
                            notifyObservers(Actions.FILE_UPLOAD_BYTES, observerArgs);

                            //System.out.println("So far, " + uploadedBytes + " of " + fileLength" have been sent.");
                        }
                    }
                };

                FileUploadCounter hfre = new FileUploadCounter(mpre, listener);

                method.setRequestEntity(hfre);

                int status = client.executeMethod(method);

                if (status == HttpStatus.SC_OK) {            
                    notifyObservers(Actions.FILE_UPLOAD_COMPLETE, observerArgs);

                    System.err.println("Upload complete");
                    upload.setStatus(1);

                } else {
                    notifyObservers(Actions.FILE_UPLOAD_FAILED, observerArgs);
                }
            } catch (Exception ex) {
                observerArgs[5] = ex.getMessage();
                notifyObservers(Actions.FILE_UPLOAD_ERROR, observerArgs);
                ex.printStackTrace();
            } finally {
                method.releaseConnection();
            }
        }

        notifyObservers(Actions.FILE_UPLOAD_FINSIHED, observerArgs);
        System.err.println("Upload finished. Token: " + upload.getToken());
    }

    // Observable methods
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }

    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);

    }

    public void notifyObservers(Object message, Object[] args)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, message, args);
        }
    }


    public static void main(String[] args)
    {

        String url = "http://mage.openmicroscopy.org.uk/qa/processing/";
        String dvPath = "/Users/TheBrain/test_images_shortrun/dv/";
        String[] files = {dvPath + "CFPNEAT01_R3D.dv", dvPath + "IAGFP-Noc01_R3D.dv"};

        FileUploader uploader = new FileUploader();
        ErrorContainer upload = new ErrorContainer();
        upload.setFiles(files);
        uploader.uploadFiles(url, 5000, upload);
    }
}
