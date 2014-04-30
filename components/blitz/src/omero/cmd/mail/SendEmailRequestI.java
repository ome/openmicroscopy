/*
 *  $Id$
 *  
 *   Copyright 2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd.mail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import ome.api.IQuery;
import ome.parameters.Parameters;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.SendEmailRequest;
import omero.cmd.SendEmailResponse;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;


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

	protected final JavaMailSender mailSender;
    
	private Helper helper;
	
	
	public SendEmailRequestI(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	//
	// CMD API
	//

	public Map<String, String> getCallContext() {
		Map<String, String> all = new HashMap<String, String>();
		all.put("omero.group", "-1");
		return all;
	}

	public void init(Helper helper) {
		this.helper = helper;
		this.helper.setSteps(1);
	}

	public Object step(int step) {
		helper.assertStep(step);
		sendEmail(parseReceipients());
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
	
	private Set<String> parseReceipients(){
		
		IQuery iquery = helper.getServiceFactory().getQueryService();
		List<Experimenter> exps =  iquery.findAllByQuery(
				"select distinct e from Experimenter e "
				+ "left outer join fetch e.groupExperimenterMap m "
                + "left outer join fetch m.parent g where g.id in (:gids) or e.id in (:eids)",
				new Parameters().addSet("gids", new HashSet<Long>(groupIds))
							.addSet("eids", new HashSet<Long>(userIds)));
		
		Set<String> receipients = new HashSet<String>();
		for (final Experimenter e : exps) {
			if (e.getEmail() != null) {
				receipients.add(e.getEmail());
			}
		}		
		return receipients;
	}
	
	private void sendEmail(Set<String> receipiest) {
		for (final String r : receipiest) {
			if (r != null) {
				MimeMessagePreparator preparator = new MimeMessagePreparator() {
					public void prepare(MimeMessage mimeMessage) throws Exception {
						MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
						message.setFrom(new InternetAddress(
								helper.getServiceFactory().getConfigService().getConfigValue("omero.mail.from")));
						message.setSubject(subject);
						message.setTo(r);
						message.setText(body, true);
					}
				};
				try {
					this.mailSender.send(preparator);
				} catch (Exception ex) {
					log.error( ex.getMessage());
				}
			}
		}
	}
}