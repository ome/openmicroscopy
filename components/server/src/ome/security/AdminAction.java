/*
 * ome.security.AdminAction
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security;

/**
 * action for passing to {@link SecuritySystem#runAsAdmin(AdminAction)}. All
 * external input should be <em>carefully</em> checked or even better copied
 * before being passed to this method. A common idiom would be: <code>
 *   public void someApiMethod(IObject target, String someValue)
 *   {
 *         	AdminAction action = new AdminAction(){
 *   		public void runAsAdmin() {
 *   	    	IObject copy = iQuery.get( iObject.getClass(), iObject.getId() );
 *   	    	copy.setValue( someValue );
 *   	    	iUpdate.saveObject(copy);    
 *   		}
 *   	};
 *   }
 * </code>
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see SecuritySystem#runAsAdmin(AdminAction)
 * @since 3.0-M3
 */
public interface AdminAction {
    /**
     * executes with special privilegs within the {@link SecuritySystem}.
     * 
     * @see SecuritySystem#runAsAdmin(AdminAction)
     */
    void runAsAdmin();

}
