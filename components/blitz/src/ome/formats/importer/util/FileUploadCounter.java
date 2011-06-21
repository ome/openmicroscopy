/*
 * ome.formats.importer.util.ErrorContainer
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ome.formats.importer.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.RequestEntity;

/**
 * @author "Brian W. Loranger"
 */
public class FileUploadCounter implements RequestEntity
{

    private final RequestEntity entity;
    private final ProgressListener listener;

    /**
     * Initialize class
     *
     * @param entity - request entity
     * @param listener - progress listener
     */
    public FileUploadCounter(
            final RequestEntity entity,
            final ProgressListener listener) {
        super();
        this.entity = entity;
        this.listener = listener;
    }

    /**
     * @author "Brian W. Loranger"
     */
    public static class OutputStreamCounter extends FilterOutputStream
    {

        private final ProgressListener listener;
        private long bytes_transferred;

        /**
         * counter in bytes
         *
         * @param out - output stream
         * @param listener - progress listener
         */
        public OutputStreamCounter(
                final OutputStream out,
                final ProgressListener listener) {
            super(out);
            this.listener = listener;
            this.bytes_transferred = 0;
        }

        /* (non-Javadoc)
         * @see java.io.FilterOutputStream#write(byte[], int, int)
         */
        public void write(byte[] b, int off, int len) throws IOException
        {
            super.write(b, off, len);
            this.bytes_transferred += len;
            this.listener.update(this.bytes_transferred);
        }

        /* (non-Javadoc)
         * @see java.io.FilterOutputStream#write(int)
         */
        public void write(int b) throws IOException {
            super.write(b);
            this.bytes_transferred++;
            this.listener.update(this.bytes_transferred);
        }

    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.RequestEntity#writeRequest(java.io.OutputStream)
     */
    public void writeRequest(final OutputStream out) throws IOException
    {
        this.entity.writeRequest(
                new OutputStreamCounter(out, this.listener));
    }

    /**
     * @author "Brian W. Loranger"
     */
    public static interface ProgressListener
    {
        void update(long num);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.RequestEntity#getContentLength()
     */
    public long getContentLength()
    {
        return this.entity.getContentLength();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.RequestEntity#getContentType()
     */
    public String getContentType()
    {
        return this.entity.getContentType();
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.RequestEntity#isRepeatable()
     */
    public boolean isRepeatable()
    {
        return this.entity.isRepeatable();
    }
}