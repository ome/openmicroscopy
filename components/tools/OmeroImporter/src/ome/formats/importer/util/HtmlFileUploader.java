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

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.util.CountingRequestEntity.ProgressListener;


public class HtmlFileUploader implements IObservable
{

    private String url;
    private String[] files;
    private String sessionId;

    ArrayList<IObserver> observers = new ArrayList<IObserver>();

    public void setSessionId(String sessionId)
    {
        if (sessionId != null)
            this.sessionId = sessionId;
        else
            this.sessionId = java.util.UUID.randomUUID().toString().replace("-", "");
        
        System.err.println(sessionId);
    }

    public String getSessionId()
    {
        return this.sessionId;
    }

    public void uploadFiles(String url, int timeout, String sessionId, String[] files)
    {
        this.url = url;
        this.files = files;
        setSessionId(sessionId);

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
            
            File file = new File(f);
            
            observerArgs[0] = file.getName();
            observerArgs[1] = fileCount;
            observerArgs[2] = files.length;
            observerArgs[3] = null;
            observerArgs[4] = null;
            observerArgs[5] = null;

            try {

                HttpClient client = new HttpClient();

                HttpClientParams params = new HttpClientParams();
                params.setConnectionManagerTimeout(timeout);
                client.setParams(params);


                method = new PostMethod(url);

                Part[] parts = {
                        new StringPart("sid", sessionId),
                        new StringPart("sessionid", sessionId),
                        new FilePart("Filedata", file)
                };

                final long fileLength = file.length();

                MultipartRequestEntity mpre = 
                    new MultipartRequestEntity(parts, method.getParams());

                ProgressListener listener = new ProgressListener(){

                    private long megaBytes = -1;

                    public void update(long pBytesRead, long pContentLength) {
                        long mBytes = pBytesRead / (pContentLength/10);
                        if (megaBytes == mBytes) {
                            return;
                        }
                        megaBytes = mBytes;

                        notifyObservers(Actions.FILE_UPLOAD_STARTED, observerArgs);

                        System.out.println("We are currently reading item " + observerArgs[1] + " of " + observerArgs[2]);

                        long uploadedBytes = pBytesRead/2;
                        if (pContentLength == -1) {

                            observerArgs[3] = uploadedBytes;
                            notifyObservers(Actions.FILE_UPLOAD_BYTES, observerArgs);

                            System.out.println("So far, " + uploadedBytes + " have been read.");


                        } else {

                            observerArgs[3] = uploadedBytes;
                            observerArgs[4] = pContentLength;
                            notifyObservers(Actions.FILE_UPLOAD_BYTES, observerArgs);

                            System.out.println("So far, " + uploadedBytes + " of " + pContentLength
                                    + " have been read.");
                        }
                    }


                    public void transferred(long num)
                    {
                        update(num, fileLength);
                    }
                };

                CountingRequestEntity hfre = new CountingRequestEntity(mpre, listener);

                method.setRequestEntity(hfre);

                int status = client.executeMethod(method);

                if (status == HttpStatus.SC_OK) {            
                    notifyObservers(Actions.FILE_UPLOAD_COMPLETE, observerArgs);
                    
                    System.err.println("Upload complete");
                    
                } else {
                    notifyObservers(Actions.FILE_UPLOAD_FAILED, observerArgs);
                }
            } catch (Exception ex) {
                observerArgs[5] = ex.getMessage();
                notifyObservers(Actions.FILE_UPLOAD_ERROR, observerArgs);
            } finally {
                method.releaseConnection();
            }
        }
        
        notifyObservers(Actions.FILE_UPLOAD_FINSIHED, observerArgs);
        System.err.println("Upload finished");
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

        String url = "http://mage.openmicroscopy.org.uk:8080/qa/processing/";
        String dvPath = "/Users/TheBrain/test_images_shortrun/dv/";
        String[] files = {dvPath + "CFPNEAT01_R3D.dv", dvPath + "IAGFP-Noc01_R3D.dv"};
        
        HtmlFileUploader uploader = new HtmlFileUploader();
        uploader.uploadFiles(url, 5000, "brian", files);
    }
}
