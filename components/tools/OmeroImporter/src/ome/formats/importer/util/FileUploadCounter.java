package ome.formats.importer.util;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.RequestEntity;

public class FileUploadCounter implements RequestEntity {

    private final RequestEntity entity;
    private final ProgressListener listener;

    public FileUploadCounter(
            final RequestEntity entity,
            final ProgressListener listener) {
        super();
        this.entity = entity;
        this.listener = listener;
    }

    public static class OutputStreamCounter extends FilterOutputStream
    {

        private final ProgressListener listener;
        private long bytes_transferred;

        public OutputStreamCounter(
                final OutputStream out,
                final ProgressListener listener) {
            super(out);
            this.listener = listener;
            this.bytes_transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException
        {
            super.write(b, off, len);
            this.bytes_transferred += len;
            this.listener.update(this.bytes_transferred);
        }

        public void write(int b) throws IOException {
            super.write(b);
            this.bytes_transferred++;
            this.listener.update(this.bytes_transferred);
        }

    }

    public void writeRequest(final OutputStream out) throws IOException
    {
        this.entity.writeRequest(
                new OutputStreamCounter(out, this.listener));
    }

    public static interface ProgressListener {

        void update(long num);
    }
    
    public long getContentLength() {
        return this.entity.getContentLength();
    }

    public String getContentType() {
        return this.entity.getContentType();
    }

    public boolean isRepeatable() {
        return this.entity.isRepeatable();
    }
} 