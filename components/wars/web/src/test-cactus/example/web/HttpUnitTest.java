package example.web;

import org.apache.cactus.ServletTestCase;

import com.meterware.httpunit.WebConversation;

/**
 * This tests the system using the HttpUnit
 * @author <a href="trajano@yahoo.com">Archimedes Trajano</a>
 * @version $Id: HttpUnitTest.java,v 1.1 2004/03/07 00:21:19 evenisse Exp $
 */
public class HttpUnitTest extends ServletTestCase {

    /**
     * This tests if the Hello World servlet provides the correct output
     * 
     * @throws Exception
     *                    thrown when there is a problem with the test
     */
    public void testHelloWorldServlet() throws Exception {
        WebConversation wc = new WebConversation();
        wc.getResponse(requestUrl("/HelloWorld"));
        assertTrue(wc.getCurrentPage().getText().startsWith("Hello world on"));
    }

    /**
     * This tests if the Hello World JSP provides the correct output
     * 
     * @throws Exception
     *                    thrown when there is a problem with the test
     */
    public void testHelloWorldJsp() throws Exception {
        WebConversation wc = new WebConversation();
        wc.getResponse(requestUrl("/sample.jsp"));
        assertTrue(wc.getCurrentPage().getText().indexOf("Hello world JSP on") != -1);
    }

    /**
     * This is a helper method to create the URL string for the initial web
     * conversation request
     * 
     * @param relativeUrl
     *                   the relative URL including the leading"/"
     * @return the context url with the relative URL appended to it
     */
    private String requestUrl(String relativeUrl) {
        StringBuffer url = request.getRequestURL();
        url.delete(url.lastIndexOf("/"), url.length());
        url.append(relativeUrl);
        return url.toString();
    }
}
