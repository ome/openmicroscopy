/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.openmicroscopy.shoola.svc.transport.TransportException;
import org.openmicroscopy.shoola.util.CommonsLangUtils;


/**
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.2
 */
public class ImportRequest
    extends Request
{

    /** The string to post.*/
    private String json;

    /**
     * Creates a new instance.
     *
     * @param json The json string to post.
     */
    ImportRequest(String json)
    {
        this.json = json;
    }

    @Override
    public HttpUriRequest marshal(String path) throws TransportException {
        if (CommonsLangUtils.isBlank(path))
            throw new TransportException("No path specified.");
        HttpPost request = new HttpPost(path);
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-type", "application/json");
        try {
            StringEntity entity = new StringEntity(json);
            request.setEntity(entity);
        } catch (Exception e) {
            throw new TransportException("Cannot prepare parameters", e);
        }

        return request;
    }

}
