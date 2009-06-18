package ome.formats.importer.util;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.RequestEntity;

public class CountingRequestEntity implements RequestEntity {

    private final RequestEntity entity;
    private final ProgressListener listener;

    public CountingRequestEntity(
            final RequestEntity entity,
            final ProgressListener listener) {
        super();
        this.entity = entity;
        this.listener = listener;
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

    public void writeRequest(final OutputStream out) throws IOException
    {
        this.entity.writeRequest(
                new CountingOutputStream(out, this.listener));
    }

    public static interface ProgressListener {

        void transferred(long num);

    }

    public static class CountingOutputStream extends FilterOutputStream
    {

        private final ProgressListener listener;
        private long transferred;

        public CountingOutputStream(
                final OutputStream out,
                final ProgressListener listener) {
            super(out);
            this.listener = listener;
            this.transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException
        {
            super.write(b, off, len);
            this.transferred += len;
            this.listener.transferred(this.transferred);
        }

        public void write(int b) throws IOException {
            super.write(b);
            this.transferred++;
            this.listener.transferred(this.transferred);
        }

    }
} 