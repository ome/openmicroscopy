package ome.security;

import ome.model.meta.Experimenter;
import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.context.ContextHolder;
import net.sf.acegisecurity.context.security.SecureContext;

public class CurrentUser {
	
	public static Experimenter asExperimenter(){
		SecureContext ctx = (SecureContext) ContextHolder.getContext();
		Experimenter e;

		if (ctx == null) {
			e = new Experimenter();
			e.setOmeName("root");
		} else {
			Authentication auth = ctx.getAuthentication();
			e = new Experimenter();
			e.setOmeName(auth.getName());
			
		}
		return e;
	}
	
}
