 /*
 * wiki.WikiEdit 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package wiki;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.FailedLoginException;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class WikiEdit {
	
	public static void edit(String text, String summary, boolean minor)
		throws IOException, ClassNotFoundException 
	{
		edit("User:Will-moore-dundee", text, summary, minor);
	}
	
	private static void edit(String title, String text, String summary, boolean minor) 
		throws IOException, ClassNotFoundException 
	{
		
		Wiki wiki = null;

	    try {
	           wiki = new Wiki("en.wikipedia.org"); // create a new wiki connection to en.wikipedia.org
	           wiki.setThrottle(5000); // set the edit throttle to 0.2 Hz
	           wiki.login("Will-moore-dundee", new char[]{'l','i','o','n','e','s','s'}); // log in as user ExampleBot, with the specified password
	    } 
	    catch (FailedLoginException ex)
	       {
	          // deal with failed login attempt
	       }
	   
	   
	       
           try
           {
               
        	   System.out.println("WikiEdit edit " + text);
        	   wiki.edit(title, text, summary, minor);
        	  
           }
           catch (Exception ex)
           {
               if (ex.getClass().equals(CredentialException.class))
                   // deal with protected page
            	   System.err.println("CredentialException");
               else if (ex.getClass().equals(CredentialNotFoundException.class))
                   // deal with trying to do something we can't
            	   System.err.println("CredentialNotFoundException");
               else if (ex instanceof AccountLockedException)
            	   System.err.println("AccountLockedException");
                   // deal with being blocked
               else if (ex instanceof IOException)
            	   System.err.println("IOException");
                   // deal with I/O error
           }
	           
	   
	}

}
