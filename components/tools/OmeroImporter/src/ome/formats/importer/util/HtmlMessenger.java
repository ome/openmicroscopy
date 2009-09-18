/*
 * ome.formats.importer.util.HtmlMessenger
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

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import ome.formats.importer.util.FileUploadCounter.ProgressListener;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpClientParams;

/**
 * @author TheBrain
 *
 * This class allows you to submit a URL and a String hashmap of 
 * key-value pairs to be posted to the url in question.
 */ 
public class HtmlMessenger
{

	/** proxy host. */
	static final String        PROXY_HOST = "http.proxyHost";

	/** proxy port. */
	static final String        PROXY_PORT = "http.proxyPort";

	/** connection_timeout **/
	static final int           CONN_TIMEOUT = 10000;

	HttpClient client = null;
	PostMethod method = null;

	public HtmlMessenger(String url,  List<Part> postList) throws HtmlMessengerException
	{
		try 
		{
			HostConfiguration cfg = new HostConfiguration();
			cfg.setHost(url);
			String proxyHost = System.getProperty(PROXY_HOST);
			String proxyPort = System.getProperty(PROXY_PORT);
			if (proxyHost != null && proxyPort != null) {
				int port = Integer.parseInt(proxyPort);
				cfg.setProxy(proxyHost, port);
			}

			client = new HttpClient();
			client.setHostConfiguration(cfg);
			HttpClientParams params = new HttpClientParams();
			params.setConnectionManagerTimeout(CONN_TIMEOUT);
			params.setSoTimeout(CONN_TIMEOUT);
			client.setParams(params);

			method = new PostMethod( url );

			Part[] parts = new Part[postList.size()];

			int i = 0;
			for (Part part : postList)
			{
				parts[i] = part;
				i++;
			}

			MultipartRequestEntity mpre = 
				new MultipartRequestEntity(parts, method.getParams());

			ProgressListener listener = new ProgressListener(){

				public void update(long bytesRead)
				{
				}
			};

			FileUploadCounter hfre = new FileUploadCounter(mpre, listener);

			method.setRequestEntity(hfre);

		} 
		catch (Exception e)
		{
			e.printStackTrace();
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

	 public HttpClient getHttpClient()
	 {
		 return client;
	 }

	 public String login(String url, String username, String password) throws HtmlMessengerException {
		 String serverReply = "";
		 try {
			 // Execute the POST method
			 PostMethod loginMethod = new PostMethod( url );

			 Part[] parts = {
					 new StringPart("username", username), 
					 new StringPart("password", password)
			 };

			 MultipartRequestEntity mpre = 
				 new MultipartRequestEntity(parts, loginMethod.getParams());

			 ProgressListener listener = new ProgressListener(){

				 public void update(long bytesRead)
				 {
				 }
			 };        	

			 FileUploadCounter hfre = new FileUploadCounter(mpre, listener);

			 loginMethod.setRequestEntity(hfre);

			 int statusCode = client.executeMethod( loginMethod );
			 if( statusCode != -1 ) {
				 Reader reader = new InputStreamReader(
						 loginMethod.getResponseBodyAsStream(), 
						 loginMethod.getRequestCharSet());
				 char[] buf = new char[32678];
				 StringBuilder str = new StringBuilder();
				 for (int n; (n = reader.read(buf)) != -1;)
					 str.append(buf, 0, n);
				 loginMethod.releaseConnection();
				 serverReply = str.toString();
			 }
			 return serverReply;
		 } catch( Exception e ) {
			 throw new HtmlMessengerException("Cannot Connect", e);
		 }
	 }
}