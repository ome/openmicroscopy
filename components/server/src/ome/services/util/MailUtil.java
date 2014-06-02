/*
 *  $Id$
 *  
 *   Copyright 2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.util;

import java.util.ArrayList;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

/**
 * Mail util
 *
 * @author Aleksandra Tarkowska, A (dot) Tarkowska at dundee.ac.uk
 * @since 5.1.0
 */

public class MailUtil {
	
	private final static Logger log = LoggerFactory.getLogger(MailUtil.class);
	
	private static final long serialVersionUID = -1L;

	protected final String sender;
	protected final JavaMailSender mailSender;

	public MailUtil(String sender, JavaMailSender mailSender) {
		this.sender = sender;
		this.mailSender = mailSender;
	}
	
	public String getSender() {
		return sender;
	}

	public void sendEmail(final String from, final String to,
			final String topic, final String body, final boolean html,
			final ArrayList<String> ccrecipients, final ArrayList<String> bccrecipients) {
		
		MimeMessagePreparator preparator = new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
				message.setFrom(from);
				message.setSubject(topic);
				message.setTo(to);
				if (null != ccrecipients && !ccrecipients.isEmpty()) message.setCc(ccrecipients.toArray(new String[ccrecipients.size()]));
				if (null != bccrecipients && !bccrecipients.isEmpty()) message.setCc(bccrecipients.toArray(new String[bccrecipients.size()]));
				message.setText(body, html);
			}

		};
		
		this.mailSender.send(preparator);
	}
}
