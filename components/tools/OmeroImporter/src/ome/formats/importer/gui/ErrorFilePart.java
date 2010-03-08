package ome.formats.importer.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.PartSource;


public class ErrorFilePart extends FilePart
{

    public ErrorFilePart(String name, File file) throws FileNotFoundException
    {
        super(name, file);        
    }

    protected void sendData(OutputStream out) throws IOException
    {
        // Content slurped and modified from superclass.
        // Chris Allan <callan@glencoesoftware.com
        PartSource source = getSource();
        
        byte[] tmp = new byte[4096];
        InputStream instream = source.createInputStream();
        try {
            int rlen;
            while ((rlen = instream.read(tmp)) >= 0) {
                out.write(tmp, 0, rlen);
                System.err.println("ding");
            }
        } finally {
            // we're done with the stream, close it
            instream.close();
        }
    }
 
}
