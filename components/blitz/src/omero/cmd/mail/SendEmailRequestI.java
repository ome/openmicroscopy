/*
 *  $Id$
 *  
 *   Copyright 2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import ome.api.IQuery;
import ome.api.local.LocalAdmin;
import ome.parameters.Parameters;
import ome.services.util.MailUtil;
import ome.system.EventContext;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.SendEmailRequest;
import omero.cmd.SendEmailResponse;
import ome.model.meta.Experimenter;

/**
 * Callback interface allowing to send email using JavaMailSender, supporting
 * MIME messages through preparation callbacks.
 * 
 * @author Aleksandra Tarkowska, A (dot) Tarkowska at dundee.ac.uk
 * @since 5.1.0
 */

public class SendEmailRequestI extends SendEmailRequest implements IRequest {

    private final static Logger log = LoggerFactory
            .getLogger(SendEmailRequestI.class);

    private static final long serialVersionUID = -1L;

    private final SendEmailResponse rsp = new SendEmailResponse();

    private String sender = null;

    private List<String> recipients = new ArrayList<String>();

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

        final EventContext ec = ((LocalAdmin) helper.getServiceFactory()
                .getAdminService()).getEventContextQuiet();
        if (!ec.isCurrentUserAdmin()) {
            throw helper.cancel(new ERR(), null, "no-permissions",
                    "ApiUsageException",
                    String.format("You have no permissions to send email."));
        }

        rsp.invalidusers = new ArrayList<Long>();
        rsp.invalidemails = new ArrayList<String>();

        if (!everyone && groupIds.isEmpty() && userIds.isEmpty())
            throw helper.cancel(new ERR(), null, "no-body",
                    "ApiUsageException",
                    String.format("No recipients specified."));
        this.sender = mailUtil.getSender();
        if (StringUtils.isBlank(this.sender))
            throw helper.cancel(new ERR(), null, "no-sender",
                    "ApiUsageException",
                    String.format("omero.mail.from cannot be empty."));
        if (StringUtils.isBlank(subject))
            throw helper.cancel(new ERR(), null, "no-subject",
                    "ApiUsageException",
                    String.format("Email must contain subject."));
        if (StringUtils.isBlank(body))
            throw helper.cancel(new ERR(), null, "no-body",
                    "ApiUsageException",
                    String.format("Email must contain body."));

        this.recipients = parseRecipients();
        this.recipients.addAll(parseExtraRecipients());

        rsp.total = this.recipients.size();
        rsp.success = 0;
        
        if (this.recipients.isEmpty())
            this.helper.setSteps(1);
        else
            this.helper.setSteps(this.recipients.size());
    }

    public Object step(int step) throws Cancel {
        helper.assertStep(step);

        // early exist
        try {
            this.recipients.get(step);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

        String email = this.recipients.get(step);

        try {
            mailUtil.sendEmail(this.sender, email, subject, body, html, null,
                    null);
        } catch (MailException me) {
            log.error(me.getMessage());
            rsp.invalidemails.add(email);
        }
        rsp.success+=1;
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

    private List<String> parseRecipients() {

        /*
         * Depends on which parameters are set variants of the following query
         * should be executed:
         * 
         * select distinct e from Experimenter as e join fetch
         * e.groupExperimenterMap as map join fetch map.parent g where 1=1 //
         * hack to avoid plenty of if statement in conditions below and g.id =
         * :active // active users by default, all = false and e.id in //
         * groupIds (select m.child from GroupExperimenterMap m where
         * m.parent.id in (:gids) ) or e.id in (:eids) // userIds
         */

        Parameters p = new Parameters();

        StringBuffer sql = new StringBuffer();

        sql.append("select distinct e from Experimenter e "
                + "left outer join fetch e.groupExperimenterMap m "
                + "left outer join fetch m.parent g where 1=1 ");

        if (!inactive) {
            sql.append(" and g.id = :active ");
            p.addLong("active", helper.getServiceFactory().getAdminService()
                    .getSecurityRoles().getUserGroupId());
        }

        if (!everyone) {

            if (groupIds.size() > 0) {
                sql.append(" and e.id in ");
                sql.append(" (select m.child from GroupExperimenterMap m "
                        + " where m.parent.id in (:gids) )");
                p.addSet("gids", new HashSet<Long>(groupIds));
            }

            if (userIds.size() > 0) {
                if (groupIds.size() > 0) {
                    sql.append(" or ");
                } else {
                    sql.append(" and ");
                }

                sql.append(" e.id in (:eids)");
                p.addSet("eids", new HashSet<Long>(userIds));
            }

        }

        IQuery iquery = helper.getServiceFactory().getQueryService();

        List<Experimenter> exps = iquery.findAllByQuery(sql.toString(), p);

        Set<String> recipients = new HashSet<String>();
        for (final Experimenter e : exps) {
            if (e.getEmail() != null && mailUtil.validateEmail(e.getEmail())) {
                recipients.add(e.getEmail());
            } else {
                rsp.invalidusers.add(e.getId());
            }
        }
        return new ArrayList<String>(recipients);
    }

    private List<String> parseExtraRecipients() {
        Set<String> extraRecipients = new HashSet<String>();
        for (final String e : extra) {
            if (mailUtil.validateEmail(e)) {
                extraRecipients.add(e);
            } else {
                rsp.invalidemails.add(e);
            }
        }
        return new ArrayList<String>(extraRecipients);
    }
 

}