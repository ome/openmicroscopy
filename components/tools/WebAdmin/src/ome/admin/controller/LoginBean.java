/*
 * ome.admin.controller
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.controller;

// Java imports

// Third-party libraries
import javax.ejb.EJBAccessException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

// Application-internal dependencies
import ome.api.IAdmin;
import ome.api.ILdap;
import ome.api.IQuery;
import ome.api.IRepositoryInfo;
import ome.api.ITypes;
import ome.system.EventContext;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;
import ome.utils.NavigationResults;

/**
 * It's the Java bean with attributes and setter/getter and actions methods. The
 * bean captures login params entered by a user after the user clicks the submit
 * button. This way the bean provides a bridge between the JSP page and the
 * application logic.
 * 
 * @author Aleksandra Tarkowska &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:A.Tarkowska@dundee.ac.uk">A.Tarkowska@dundee.ac.uk</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Revision$Date: $)</small>
 * @since OME3.0
 */
public class LoginBean implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@link ome.model.meta.Experimenter#getId()}
     */
    private String username;

    /**
     * Not-null. Might must pass validation in the security sub-system.
     */
    private String password;

    /**
     * {@link ome.model.meta.Experimenter#getId()} as {@link java.lang.String}
     */
    private String id;

    /**
     * boolean
     */
    private boolean role;

    /**
     * boolean
     */
    private boolean mode = false;

    /**
     * Not null.
     */
    private String server;

    /**
     * Not null.
     */
    private int port;

    /**
     * IAdmin
     */
    private IAdmin adminService;

    /**
     * ITypes
     */
    private ITypes typesService;

    /**
     * IAdmin
     */
    private IQuery queryService;

    /**
     * IRepositoryInfo
     */
    private IRepositoryInfo repService;

    /**
     * ILdap
     */
    private ILdap ldapService;
    
    /**
     * log4j logger
     */
    static Logger logger = Logger.getLogger(LoginBean.class.getName());

    /**
     * Gets role of user loged in (true or false)
     * 
     * @return boolean
     */
    public boolean getRole() {
        return this.role;
    }

    /**
     * Set role for user loged in (true or false)
     * 
     * @param role
     *            boolean
     */
    public void setRole(boolean role) {
        this.role = role;
    }

    /**
     * Get {@link ome.model.meta.Experimenter#getOmeName()}
     * 
     * @return {@link ome.model.meta.Experimenter#getOmeName()}
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Set {@link ome.model.meta.Experimenter#getOmeName()}
     * 
     * @param username
     *            {@link ome.model.meta.Experimenter#getOmeName()}
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get password
     * 
     * @return Not-null. Might must pass validation in the security sub-system.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set password
     * 
     * @param password
     *            Not-null. Might must pass validation in the security
     *            sub-system.
     */
    public void setPassword(String password) {
        if (password != null)
            this.password = password;
        else
            this.password = "";
    }

    /**
     * Gets {@link ome.model.meta.Experimenter#getId()}
     * 
     * @return {@link ome.model.meta.Experimenter#getId()}
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set {@link ome.model.meta.Experimenter#getId()}
     * 
     * @param id
     *            {@link ome.model.meta.Experimenter#getId()}
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets server
     * 
     * @return {@link java.lang.String}
     */
    public String getServer() {
        if (this.server == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            this.server = fc.getExternalContext().getInitParameter(
                    "defaultServerHost");
        }

        return this.server;
    }

    /**
     * Sets server
     * 
     * @param server
     *            {@link java.lang.String}
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * Gets port
     * 
     * @return int
     */
    public int getPort() {
        if (this.port == 0) {
            FacesContext fc = FacesContext.getCurrentInstance();
            this.port = Integer.parseInt(fc.getExternalContext()
                    .getInitParameter("defaultServerPort"));
        }
        return this.port;
    }

    /**
     * Sets port
     * 
     * @param port
     *            int
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Set mode for JSPs
     * 
     * @param em
     *            boolean
     */
    public void setMode(boolean em) {
        this.mode = em;
    }

    /**
     * Checks mode
     * 
     * @return boolean
     */
    public boolean isMode() {
        return mode;
    }

    /**
     * Get {@link ome.api.IAdmin}
     * 
     * @return {@link ome.admin.controller.LoginBean#adminService}
     */
    public IAdmin getAdminServices() {
        return this.adminService;
    }

    /**
     * Get {@link ome.api.IAdmin}
     * 
     * @return {@link ome.admin.controller.LoginBean#adminService}
     */
    public ITypes getTypesServices() {
        return this.typesService;
    }

    /**
     * Get {@link ome.api.IQuery}
     * 
     * @return {@link ome.admin.controller.LoginBean#queryService}
     */
    public IQuery getQueryServices() {
        return this.queryService;
    }

    /**
     * Get {@link ome.api.IRepositoryInfo}
     * 
     * @return {@link ome.admin.controller.LoginBean#repService}
     */
    public IRepositoryInfo getRepServices() {
        return this.repService;
    }
    
    /**
     * Get {@link ome.api.ILdap}
     * 
     * @return {@link ome.admin.controller.LoginBean#ldapService}
     */
    public ILdap getLdapServices() {
        return this.ldapService;
    }

    /**
     * Provides action for navigation rule "login" what is described in the
     * faces-config.xml file.
     * 
     * @return {@link java.lang.String} "success" or "false"
     */
    public String login() {
        logger.info("User " + this.username + " has started to log in to app.");

        this.mode = false;

        logger.info("Login - Service Factory connection to " + server + ":"
                + port + " by " + username + " ...");

        try {
            String jsfnav = null;
            try {

                Login l = new Login(username, password, "system", "User");
                Server s = new Server(server, port);
                ServiceFactory sf = new ServiceFactory(s, l);
                this.adminService = sf.getAdminService();
                this.typesService = sf.getTypesService();
                this.queryService = sf.getQueryService();
                this.repService = sf.getRepositoryInfoService();
                this.ldapService = sf.getLdapService();
                                    
                jsfnav = NavigationResults.SUCCESS;
                logger.info("Admin role for user "
                        + adminService.getEventContext().getCurrentUserId());
            } catch (Exception e) {

                Login l = new Login(username, password, "user", "User");
                Server s = new Server(server, port);
                ServiceFactory sf = new ServiceFactory(s, l);
                this.adminService = sf.getAdminService();
                this.queryService = sf.getQueryService();
                jsfnav = NavigationResults.ACCOUNT;
                logger.info("User role for user "
                        + adminService.getEventContext().getCurrentUserId());

            }

            EventContext ctx = this.adminService.getEventContext();
            this.id = ctx.getCurrentUserId().toString();
            this.role = ctx.isCurrentUserAdmin();
            this.mode = true;
            if(!ldapService.getSetting())
                ldapService = null;
            logger.info("Authentication succesfule");
            return jsfnav;
        } catch (EJBAccessException e) {
            logger.info("Authentication not succesfule - invalid login params:"
                    + e.getMessage());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Invalid Login Params: "
                    + e.getMessage());
            context.addMessage("loginForm", message);
            this.mode = false;
            return NavigationResults.FALSE;
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Authentication not succesfule - connection failure: "
                    + e.getMessage());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Connection failure: "
                    + e.getMessage());
            context.addMessage("loginForm", message);
            this.mode = false;
            return NavigationResults.FALSE;
        }

    }

    public String getPage() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext
                .getExternalContext().getRequest();
        String[] s = request.getRequestURI().split("/");
        String menu = s[s.length - 1];
        return menu;
    }

}
