package src.adminTool.ui.messenger;

/*
 * ome.formats.importer.HtmlMessenger
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

/**
 * @author TheBrain
 *
 * This class allows you to submit a URL and a String hashmap of 
 * key-value pairs to be posted to the url in question.
 */ 
public class HtmlMessenger
{

    HttpClient client = null;
    PostMethod method = null;
    
    public HtmlMessenger(String url,  Map <String, String> postHashMap) throws HtmlMessengerException
    {
        try {
            client = new HttpClient();
            method = new PostMethod( url );
        } catch (Exception e)
        {
            throw new HtmlMessengerException("Error creating client/method", e);
        }
        
        
        try {
            for (String key : postHashMap.keySet())
            {
                String value = postHashMap.get(key);
                method.addParameter(key, value);
            }
        } catch (Exception e)
        {
            throw new HtmlMessengerException("Error creating post parameters", e);
        }
        
    }
    
    /**
     * @return
     * @throws HtmlMessengerException
     * 
     * This method executes the post created when this class is instantiated
     * 
     */
    public String executePost() throws HtmlMessengerException
    {
        String serverReply = "";
        try {
            // Execute the POST method
            int statusCode = client.executeMethod( method );
            if( statusCode != -1 ) {
                Reader reader = new InputStreamReader(
                        method.getResponseBodyAsStream(), 
                        method.getRequestCharSet());
                char[] buf = new char[32678];
                StringBuilder str = new StringBuilder();
                for (int n; (n = reader.read(buf)) != -1;)
                    str.append(buf, 0, n);
                method.releaseConnection();
                serverReply = str.toString();
            }
            return serverReply;
        } catch( Exception e ) {
            throw new HtmlMessengerException("Cannot Connect", e);
        }
    }
}