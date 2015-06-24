/*
 *  $Id$
 *  
 *   Copyright 2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package omero.cmd.admin;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import ome.conditions.ApiUsageException;
import ome.model.meta.Experimenter;
import ome.security.SecuritySystem;
import ome.security.auth.PasswordChangeException;
import ome.security.auth.PasswordProvider;
import ome.security.auth.PasswordUtil;
import ome.services.mail.MailUtil;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.ResetPasswordRequest;
import omero.cmd.ResetPasswordResponse;

/**
 * Callback interface allowing to reset password for the given user.
 * 
 * @author Aleksandra Tarkowska, A (dot) Tarkowska at dundee.ac.uk
 * @since 5.1.0
 */

public class ResetPasswordRequestI extends ResetPasswordRequest implements
        IRequest {

    private final static Logger log = LoggerFactory
            .getLogger(ResetPasswordRequestI.class);

    private static final long serialVersionUID = -1L;

    private final ResetPasswordResponse rsp = new ResetPasswordResponse();

    private String sender = null;

    private final MailUtil mailUtil;

    private final PasswordUtil passwordUtil;

    private final SecuritySystem sec;

    private final PasswordProvider passwordProvider;

    private Helper helper;

    public ResetPasswordRequestI(MailUtil mailUtil, PasswordUtil passwordUtil,
            SecuritySystem sec, PasswordProvider passwordProvider) {
        this.mailUtil = mailUtil;
        this.passwordUtil = passwordUtil;
        this.sec = sec;
        this.passwordProvider = passwordProvider;
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

        if (omename == null)
            throw helper.cancel(new ERR(), null, "no-omename");
        if (email == null)
            throw helper.cancel(new ERR(), null, "no-email");

        this.helper.setSteps(1);
    }

    public Object step(int step) throws Cancel {
        helper.assertStep(step);
        return resetPassword();
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

    private boolean resetPassword() {

        Experimenter e = null;
        try {
            e = helper.getServiceFactory().getAdminService()
                    .lookupExperimenter(omename);
        } catch (ApiUsageException ex) {
            throw helper.cancel(new ERR(), null, "unknown-user",
                    "ApiUsageException", ex.getMessage());
        }
        if (e.getEmail() == null)
            throw helper.cancel(new ERR(), null, "unknown-email",
                    "ApiUsageException",
                    String.format("User has no email address."));
        else if (!e.getEmail().equals(email))
            throw helper.cancel(new ERR(), null, "not-match",
                    "ApiUsageException",
                    String.format("Email address does not match."));
        else if (passwordUtil.getDnById(e.getId()))
            throw helper.cancel(new ERR(), null, "ldap-user",
                    "ApiUsageException", String
                            .format("User is authenticated by LDAP server. "
                                    + "You cannot reset this password."));
        else {
            final String newPassword = passwordUtil.generateRandomPasswd();
            // FIXME
            // workaround as sec.runAsAdmin doesn't execute with the root
            // context
            // helper.getServiceFactory().getAdminService().changeUserPassword(e.getOmeName(),
            // newPassword);
            try {
                passwordProvider.changePassword(e.getOmeName(), newPassword);
                log.info("Changed password for user: " + e.getOmeName());
            } catch (PasswordChangeException pce) {
                log.error(pce.getMessage());
                throw helper.cancel(new ERR(), null, "password-change-failed",
                        "PasswordChangeException",
                        String.format(pce.getMessage()));
            }

            String subject = "OMERO - Reset password";
            String body = "Dear " + e.getFirstName() + " " + e.getLastName()
                    + " (" + e.getOmeName() + ")" + " your new password is: "
                    + newPassword;

            try {
                mailUtil.sendEmail(sender, e.getEmail(), subject, body, false,
                        null, null);
            } catch (MailException me) {
                log.error(me.getMessage());
                throw helper.cancel(new ERR(), null, "mail-send-failed",
                        "MailException", String.format(me.getMessage()));
            }

        }

        return true;

    }

}
