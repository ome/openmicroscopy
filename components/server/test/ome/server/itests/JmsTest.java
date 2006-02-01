/*
 * ome.server.itests.JmsTest
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
package ome.server.itests;

//Java imports

//Third-party libraries
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

// FIXME import org.activemq.message.ActiveMQQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

//Application-internal dependencies



/** 
 * tests for a HQL join bug.
 *  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class JmsTest
        extends
            AbstractDependencyInjectionSpringContextTests implements MessageListener {

    private static Log log = LogFactory.getLog(JmsTest.class);

    JmsTemplate jt;
    public void setJmsTemplate(JmsTemplate template){
    	this.jt=template;
    }
    
    ConnectionFactory factory;
    public void setConnectionFactory(ConnectionFactory factory){
    	this.factory=factory;
    }
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    protected void onSetUp() throws Exception {
    }
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return new String[] { 
                "WEB-INF/jms.xml"
        };
    }

    public void testWithNewQueue() throws Throwable{
    	// FIXME ActiveMQQueue q = new ActiveMQQueue("TEST");
        Queue q = null;
    	jt.send(q,new MessageCreator(){
    	      public Message createMessage(Session session) throws JMSException {
    	          return session.createTextMessage("hello queue world");
    	      }});
    	
    	TextMessage txt = (TextMessage) jt.receive(q);
    	System.out.println(txt.getText());
    }
    
    public void testWithDefaultQueue() throws Throwable{
    	jt.send(new MessageCreator(){
    	      public Message createMessage(Session session) throws JMSException {
    	          return session.createTextMessage("hello queue world");
    	      }});
    	
    	TextMessage txt = (TextMessage) jt.receive();
    	System.out.println(txt.getText());
    }
    
    public void testAsynch() throws InterruptedException{
    	// see http://www.oracle.com/technology/sample_code/tech/java/jms/unidomain/AsyncClient.java.html or
    	/* http://www.jguru.com/faq/view.jsp?EID=1214
			# Implement javax.jms.MessageListener:
			
			public interface MessageListener {
			  void onMessage(Message message);
			}
			
			# Register the message handler using setMessageListener():
			
			  ...
			  subscriber = session.createSubscriber(topic);
			  SubscriberMessageThread smt = new SubscriberMessageThread(this);
			  subscriber.setMessageListener(smt);
			  con.start(); // start connection
			  smt.start();
    	 */
    	MessageCreator mc = new MessageCreator() {
	        public Message createMessage(Session session)
            throws JMSException {
          MapMessage message = session.createMapMessage();
            message.setStringProperty("name", "Josh");
            message.setJMSCorrelationID("nano"+System.nanoTime());
          return message;
        }
      }; 
      	for (int i = 0; i < 5; i++) {
      		jt.send("ASYN",mc);	
		}
    	Thread.sleep(1000L);
    }

	public void onMessage(Message msg) {
	      try {
	        String name = msg.getStringProperty("name");
	        if(name == null) {
	          name = "World";
	        }

	        System.out.println(this.hashCode()+" says Hello " + name + " in message "+msg.getJMSCorrelationID());
	        
	        Thread.sleep(20L);
	      } catch (JMSException e) {
	        // handle exception?
	      } catch (InterruptedException e) {
			e.printStackTrace();
		}
	    }

}

