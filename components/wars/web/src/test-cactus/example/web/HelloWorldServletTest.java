package example.web;

import org.apache.cactus.ServletTestCase;
import org.apache.cactus.WebResponse;

import example.web.HelloWorldServlet;

/**
 * This tests that the HelloWorld servlet is functioning
 *
 * @author <a href="trajano@yahoo.com">Archimedes Trajano</a>
 * @version $Id: HelloWorldServletTest.java,v 1.1 2004/03/07 00:21:19 evenisse Exp $
 */
public class HelloWorldServletTest extends ServletTestCase {

    public void testHelloWorld() throws Exception {
        HelloWorldServlet servlet = new HelloWorldServlet();        
        servlet.doGet(request,response);
    }
    
    public void endHelloWorld(WebResponse response) {
        assertTrue(response.getText().startsWith("Hello world on"));
    }
}
