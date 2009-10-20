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
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FileUploader implements IObservable
{   

    private static Log         log = LogFactory.getLog(FileUploader.class);

    private String[] files;

    private String session_id;
    
    private PostMethod method = null;


    ArrayList<IObserver> observers = new ArrayList<IObserver>();

    private HttpClient client;

    private boolean cancelUpload;

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

    }

    public String getSessionId()
    {
        return this.session_id;
    }
    
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
                return;
            
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
                        long partsDone = 0;
                        if (fileLength != 0) partsDone = bytesRead / (fileLength/10);
                        if (parts == partsDone) {
                            return;
                        }
                        parts = partsDone;

                        notifyObservers(new ImportEvent.FILE_UPLOAD_STARTED(
                                file.getName(), count, files.length, null, null, null));

                        long uploadedBytes = bytesRead/2;
                        if (fileLength == -1) {

                            notifyObservers(new ImportEvent.FILE_UPLOAD_BYTES(
                                    file.getName(), count, files.length, uploadedBytes, null, null));

                        } else {

                            notifyObservers(new ImportEvent.FILE_UPLOAD_BYTES(
                                    file.getName(), count, files.length, uploadedBytes, fileLength, null));

                        }
                    }
                };

                FileUploadCounter hfre = new FileUploadCounter(mpre, listener);

                method.setRequestEntity(hfre);

                int status = client.executeMethod(method);

                if (status == HttpStatus.SC_OK) {   
                    
                    notifyObservers(new ImportEvent.FILE_UPLOAD_COMPLETE(
                            file.getName(), count, files.length, null, null, null));
                    upload.setStatus(1);

                } else {
                    notifyObservers(new ImportEvent.FILE_UPLOAD_COMPLETE(
                            file.getName(), count, files.length, null, null, null));
                }
            } catch (Exception ex) {
                notifyObservers(new ImportEvent.FILE_UPLOAD_ERROR(
                        file.getName(), count, files.length, null, null, ex));
                ex.printStackTrace();
            } finally {
                method.releaseConnection();
            }
        }

        notifyObservers(new ImportEvent.FILE_UPLOAD_FINISHED(
                null, 0, 0, null, null, null));
        
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

    public void notifyObservers(ImportEvent event)
    {
        for (IObserver observer:observers)
        {
            observer.update(this,event);
        }
    }

    public void cancel()
    {
        //this.cancelUpload = true;
    }

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
