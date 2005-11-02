package ome.security;


import java.io.Serializable;

import net.sf.acegisecurity.GrantedAuthority;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class GrantedAuthorityImpl implements GrantedAuthority, Serializable {
    //~ Instance fields ========================================================

    private String role;

    //~ Constructors ===========================================================

    public GrantedAuthorityImpl(String role) {
        this.role = role;
    }

    public GrantedAuthorityImpl(GrantedAuthority auth){
    	this.role = auth == null ? null : auth.getAuthority();
    }
    
    //~ Methods ================================================================

    public String getAuthority() {
        return this.role;
    }

    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return obj.equals(this.role);
        }

        if (obj instanceof GrantedAuthority) {
            GrantedAuthority attr = (GrantedAuthority) obj;

            return this.role.equals(attr.getAuthority());
        }

        return false;
    }

    public int hashCode() {
        return this.role.hashCode();
    }

    public String toString() {
        return this.role;
    }
}
