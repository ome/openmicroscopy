/*
 * ome.formats.importer.gui.AddDatasetDialog
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

package ome.formats.importer.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.PartSource;

/**
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 * @author Chris Allan <callan@glencoesoftware.com>
 *
 */
public class ErrorFilePart extends FilePart
{

	public boolean cancel = false;

    /**
     * Calls parent 'FilePart' class
     *
     * @param name
     * @param file
     * @throws FileNotFoundException
     */
    public ErrorFilePart(String name, File file) throws FileNotFoundException
    {
        super(name, file);
    }

    /* (non-Javadoc)
     * @see org.apache.commons.httpclient.methods.multipart.FilePart#sendData(java.io.OutputStream)
     */
    protected void sendData(OutputStream out) throws IOException
    {
        // Content slurped and modified from superclass.
        // Chris Allan <callan@glencoesoftware.com>
        PartSource source = getSource();

        byte[] tmp = new byte[4096];
        InputStream instream = source.createInputStream();
        try {
            int rlen;
            while ((rlen = instream.read(tmp)) >= 0) {
		if (cancel) {
			//System.err.println("cancelled");
			break;
		}
                out.write(tmp, 0, rlen);
                //System.err.println("ding");
            }
        } finally {
            // we're done with the stream, close it
            instream.close();
        }
    }

}
