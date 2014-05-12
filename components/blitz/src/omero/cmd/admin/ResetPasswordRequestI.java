package omero.cmd.admin;

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
import ome.conditions.ApiUsageException;
import ome.conditions.AuthenticationException;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;
import ome.security.AdminAction;
import ome.security.auth.PasswordUtil;
import omero.cmd.HandleI.Cancel;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.Response;
import omero.cmd.ResetPasswordRequest;
import omero.cmd.ResetPasswordResponse;


public class ResetPasswordRequestI extends ResetPasswordRequest implements
		IRequest {

	private final static Logger log = LoggerFactory.getLogger(ResetPasswordRequestI.class);
	
	private static final long serialVersionUID = -1L;

	private final ResetPasswordResponse rsp = new ResetPasswordResponse();

	private String sender = null;
	
	private Experimenter e = null;
	
	protected final JavaMailSender mailSender;
	protected final PasswordUtil passwordUtil;
    
	private Helper helper;
	
	public ResetPasswordRequestI(JavaMailSender mailSender, PasswordUtil passwordUtil) {
		this.mailSender = mailSender;
		this.passwordUtil = passwordUtil;
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
		this.sender = helper.getServiceFactory().getConfigService().getConfigValue("omero.mail.from");
		
		if (omename == null) 
			throw helper.cancel(new ERR(), null, "no-omename");
        if (email == null)
        	throw helper.cancel(new ERR(), null, "no-email");
        
        try {
        	this.e = helper.getServiceFactory().getAdminService().lookupExperimenter(omename);
        } catch (ApiUsageException ex) {
        	throw helper.cancel(new ERR(), null, "unknown-user", "ApiUsageException",
        			String.format(ex.getMessage()));
        }
        if (this.e.getEmail() == null)
        	throw helper.cancel(new ERR(), null, "unknown-email", "ApiUsageException",
        			String.format("User has no email address."));
        else if (!this.e.getEmail().equals(email))
        	throw helper.cancel(new ERR(), null, "not-match", "ApiUsageException",
        			String.format("Email address does not match."));
        else if (isDnById(this.e.getId()))
        	throw helper.cancel(new ERR(), null, "ldap-user", "ApiUsageException",
        			String.format("User is authenticated by LDAP server "
        					+ "you cannot reset this password."));
        
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
	
	private boolean isDnById(long id) {
        String dn = passwordUtil.getDnById(id);
        if (dn != null) {
            return true;
        } else {
            return false;
        }
    }
	
	private boolean resetPassword() {

        final String newPassword = passwordUtil.generateRandomPasswd();
        helper.getServiceFactory().getAdminService().changeUserPassword(this.e.getOmeName(), newPassword);
        
        String subject = "OMERO - Reset password";
        String body = "Dear " + this.e.getFirstName() + " " + this.e.getLastName() + " ("
                + this.e.getOmeName() + ")" + " your new password is: "
                + newPassword;
        
        return sendEmail(helper.prepareEmail(this.sender, this.e.getEmail(), 
        		subject, body, false, null, null));
        
	}
	
	private boolean sendEmail(final MimeMessagePreparator email) {
		
		try {
			this.mailSender.send(email);
		} catch (MailException me) {
			log.error(me.getMessage());
			throw helper.cancel(new ERR(), null, "mail-send-failed", "MailException",
                    String.format(me.getMessage()));
        }
		return true;
	}
	
}
