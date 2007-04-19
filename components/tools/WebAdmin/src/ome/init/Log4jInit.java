/*
 * Start.java
 *
 * Created on April 5, 2007, 2:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ome.init;

import org.apache.log4j.PropertyConfigurator;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.IOException;

public class Log4jInit extends HttpServlet {

	public void init() {
		String prefix = getServletContext().getRealPath("/");
		String file = getInitParameter("log4j-init-file");
		if (file != null) {
			PropertyConfigurator.configure(prefix + file);
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) {
	}
}
