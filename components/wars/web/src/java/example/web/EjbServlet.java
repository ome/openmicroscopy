package example.web;

import java.io.IOException;
import java.util.Date;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import example.ejb.ExampleFacadeLocal;
import example.ejb.ExampleFacadeUtil;

/**
 * This is a sample servlet, typically you would not use this, but it is useful
 * for testing the sanity of your web application configuration.
 * 
 * @web.servlet name="EjbConnect"
 * @web.servlet-mapping url-pattern="/ejb"
 * 
 * @web.ejb-local-ref 
 * 	type="Session"
 * 	home="example.ejb.ExampleFacadeHomeLocal"
 * 	local="example.ejb.ExampleFacadeLocal
 * 	name="ejb/ExampleFacadeLocal"
 *
 * @author <a href="trajano@yahoo.com">Archimedes Trajano</a>
 * @version $Id: EjbServlet.java,v 1.1 2004/03/07 00:21:19 evenisse Exp $
 */
public class EjbServlet extends HttpServlet {
    /**
     * This servlet makes a connection to the HTTP Server.
     * 
     * @param request
     *                   the HTTP request object
     * @param response
     *                   the HTTP response object
     * @throws IOException
     *                    thrown when there is a problem getting the writer
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
      try {
        ExampleFacadeLocal facade = ExampleFacadeUtil.getLocalHome().create();
        response.getWriter().println("Got ID " + facade.getId(request.getParameter("name")));
      } catch (NamingException e) {
        throw new ServletException(e);
      } catch (CreateException e) {
        throw new ServletException(e);
      }
    }
}
