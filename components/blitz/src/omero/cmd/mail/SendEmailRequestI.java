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
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import ome.api.IQuery;
import ome.parameters.Parameters;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.SendEmailRequest;
import omero.cmd.SendEmailResponse;
import ome.model.meta.Experimenter;


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

	private String [] recipients = null;
	
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
		this.recipients = parseRecipients();
		if (this.recipients.length < 1)
			throw helper.cancel(new ERR(), null, "no-recipiest");
		this.helper.setSteps(this.recipients.length);
	}

	public Object step(int step) throws Cancel {
		helper.assertStep(step);
		return sendEmail(this.recipients[step]);
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
	
	private String [] parseRecipients(){
		StringBuffer sql = new StringBuffer();
		sql.append("select distinct e from Experimenter e "
				+ "left outer join fetch e.groupExperimenterMap m "
				+ "left outer join fetch m.parent g ");

		Parameters p = new Parameters();
		if (groupIds.size() > 0 && userIds.size() > 0) {
			sql.append(" where (g.id in (:gids) or e.id in (:eids)) ");
			p.addSet("gids", new HashSet<Long>(groupIds));
			p.addSet("eids", new HashSet<Long>(userIds));
		} else {
			if (groupIds.size() > 0) {
				sql.append(" where g.id in (:gids) ");
				p.addSet("gids", new HashSet<Long>(groupIds));
			}
			if (userIds.size() > 0) {
				sql.append("where e.id in (:eids) ");
				p.addSet("eids", new HashSet<Long>(userIds));
			}
		}
		
		if (activeonly) {
			if (groupIds.size() == 0 && userIds.size() == 0) sql.append(" where ");
			else sql.append(" and ");
			
			sql.append(" g.id = :active ");
			p.addLong("active", helper.getServiceFactory().getAdminService()
					.getSecurityRoles().getUserGroupId());
		}

		IQuery iquery = helper.getServiceFactory().getQueryService();
		
		List<Experimenter> exps = iquery.findAllByQuery(sql.toString(), p);
		
		Set<String> recipients = new HashSet<String>();
		for (final Experimenter e : exps) {
			if (e.getEmail() != null) {
				recipients.add(e.getEmail());
			}
		}
		return recipients.toArray(new String[recipients.size()]);
	}
	
	private boolean sendEmail(final String recipient) {
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
				message.setFrom(new InternetAddress(
						helper.getServiceFactory().getConfigService().getConfigValue("omero.mail.from")));
				message.setSubject(subject);
				message.setTo(recipient);
				message.setText(body, true);
			}
		};
		
		try {
			this.mailSender.send(preparator);
		} catch (MailException me) {
			log.error(me.getMessage());
			throw helper.cancel(new ERR(), null, "mail-send-failed", "MailException",
                    String.format(me.getMessage()));
        }
		return true;
	}
}