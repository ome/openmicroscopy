package example.web;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a sample servlet, typically you would not use this, but it is useful
 * for testing the sanity of your web application configuration.
 * 
 * @web.servlet name="HelloWorld"
 * @web.servlet-mapping url-pattern="/HelloWorld"
 * 
 * @author <a href="trajano@yahoo.com">Archimedes Trajano</a>
 * @version $Id: HelloWorldServlet.java,v 1.1 2004/03/07 00:21:19 evenisse Exp $
 */
public class HelloWorldServlet extends HttpServlet {
    /**
     * This prints out the standard "Hello world" message with a date stamp.
     * 
     * @param request
     *                   the HTTP request object
     * @param response
     *                   the HTTP response object
     * @throws IOException
     *                    thrown when there is a problem getting the writer
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.getWriter().println("Hello world on " + new Date());
    }
}
