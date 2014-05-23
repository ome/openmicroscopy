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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import ome.api.IQuery;
import ome.parameters.Parameters;
import ome.services.util.MailUtil;
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

	private String sender = null;
	private String [] recipients = null;
	private String [] ccrecipients = null;
	private String [] bccrecipients = null;
	
	private final MailUtil mailUtil;
    
	private Helper helper;
	
	
	public SendEmailRequestI(MailUtil mailUtil) {
		this.mailUtil = mailUtil;
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
		
		this.sender = mailUtil.getSender();
		if (this.sender.length() < 1) 
			throw helper.cancel(new ERR(), null, "no-sender");
		if (subject.length() < 1) 
			throw helper.cancel(new ERR(), null, "no-subject");
		if (body.length() < 1) 
			throw helper.cancel(new ERR(), null, "no-body");
		
		this.recipients = parseRecipients();
		this.ccrecipients = parseCCRecipients();
		this.bccrecipients = parseBccRecipients();
		if (this.recipients.length < 1)
			throw helper.cancel(new ERR(), null, "no-recipiest");
		
		this.helper.setSteps(this.recipients.length);
	}

	public Object step(int step) throws Cancel {
		helper.assertStep(step);
		
		try {
			mailUtil.sendEmail(this.sender, this.recipients[step], 
					subject, body, html, this.ccrecipients, this.bccrecipients);
		} catch (MailException me) {
			log.error(me.getMessage());
			throw helper.cancel(new ERR(), null, "mail-send-failed", "MailException",
                    String.format(me.getMessage()));
        }
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
	
	private String [] parseCCRecipients(){
		Set<String> ccrecipients = new HashSet<String>();
		for (final String e : cc) {
			if (e.length() > 5) {
				ccrecipients.add(e);
			}
		}
		return ccrecipients.toArray(new String[ccrecipients.size()]);
	}
	
	private String [] parseBccRecipients(){
		Set<String> bccrecipients = new HashSet<String>();
		for (final String e : bcc) {
			if (e.length() > 5) {
				bccrecipients.add(e);
			}
		}
		return bccrecipients.toArray(new String[bccrecipients.size()]);
	}

}