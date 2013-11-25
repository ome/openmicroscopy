/*
 * org.openmicroscopy.shoola.svc.proxy.SubmittedFilePart 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.svc.proxy;



//Java imports
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//Third-party libraries
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.PartSource;

//Application-internal dependencies

/** 
 * Extends the <code>FilePart</code> so we can read chunck of the file.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class SubmittedFilePart
    extends FilePart
{

    /** The maximum size of a data block to read at a time.*/
    private static final int SIZE = 1024;
    /**
     * Creates a new instance.
     * 
     * @param name The name of the target.
     * @param file The file to send.
     * @throws FileNotFoundException Thrown if the file cannot be found.
     */
    SubmittedFilePart(String name, File file)
            throws FileNotFoundException
    {
        super(name, file);
    }

    /** 
     * Overridden to read chunk of the file.
     * @see FilePart#sendData(OutputStream)
     */
    protected void sendData(OutputStream out)
            throws IOException
    {
        PartSource source = getSource();
        byte[] tmp = new byte[SIZE];
        InputStream in = source.createInputStream();
        try {
            int l;
            while ((l = in.read(tmp)) >= 0)
                out.write(tmp, 0, l);
        } finally {
            in.close();
        }
    }

}
