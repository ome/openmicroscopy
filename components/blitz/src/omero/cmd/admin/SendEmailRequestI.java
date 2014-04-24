/*
 *  $Id$
 *  
 *   Copyright 2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.SendEmailRequest;
import omero.cmd.SendEmailResponse;
import omero.model.Experimenter;



/**
 * Send Email.
 *
 * @author Aleksandra Tarkowska, A (dot) Tarkowska at dundee.ac.uk
 * @since 5.1.0
 */
public class SendEmailRequestI extends SendEmailRequest implements
		IRequest {

	private final static Logger log = LoggerFactory.getLogger(SendEmailRequestI.class);
	
	private static final long serialVersionUID = -1L;

	private final SendEmailResponse rsp = new SendEmailResponse();


	protected final MailSender mailSender;

	protected final SimpleMailMessage templateMessage;
    
	private Helper helper;


	public SendEmailRequestI(MailSender mailSender, SimpleMailMessage templateMessage) {
		this.mailSender = mailSender;
		this.templateMessage = templateMessage;
	}
    
	
	
	//
	// CMD API
	//

	public Map<String, String> getCallContext() {
		Map<String, String> all = new HashMap<String, String>();
		all.put("omero.group", "-1");
		log.error("getCallContext ");
		return all;
	}

	public void init(Helper helper) {
		this.helper = helper;
		this.helper.setSteps(1);
	}

	public Object step(int step) {
		helper.assertStep(step);
		sendEmail();
		return null;
	}

	@Override
	public void finish() throws Cancel {
		// no-op
	}

	public void buildResponse(int step, Object object) {
		helper.assertResponse(step);
		if (helper.isLast(step)) {
			helper.setResponseIfNull(rsp);
		}
	}

	public Response getResponse() {
		return helper.getResponse();
	}
	
	private String[] getRecepiest() {
		List<String> receipiest = new ArrayList<String>();
		for (Experimenter u : users) {
			if (u.getEmail() != null)
				receipiest.add(u.getEmail().getValue());
		}
		String[] simpleArray = new String[receipiest.size()];
		return receipiest.toArray( simpleArray );
	}
	
	private void sendEmail() {
        // Create a thread safe "copy" of the template message and customize it
        SimpleMailMessage msg = new SimpleMailMessage(this.templateMessage);
        msg.setSubject(subject);
        msg.setTo(getRecepiest());
        msg.setText(body);
        try {
            this.mailSender.send(msg);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Exception: "
                            + ex.getMessage()
                            + ". "
                            + ". Please turn on the debug "
                            + "mode in omero.properties by the: omero.resetpassword.mail.debug=true");
        }
	}
}