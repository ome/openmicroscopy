/*
 * LoginBean.java
 *
 * Created on March 19, 2007, 1:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ome.admin.controller;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import ome.connection.ConnectionDB;
import ome.system.EventContext;

import javax.servlet.http.HttpSession;

import javax.ejb.EJBAccessException;

import org.apache.log4j.Logger;

/**
 * 
 * @author Ola
 */

public class LoginBean {
	
	private String username;

	private String password;

	private String id;

	private boolean role;

	private boolean mode = false;

	private String server;

	private int port;

	static Logger logger = Logger.getLogger(LoginBean.class.getName());

	public LoginBean() {
	}

	public boolean getRole() {
		return this.role;
	}

	public void setRole(boolean role) {
		this.role = role;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getServer() {
		FacesContext fc = FacesContext.getCurrentInstance();
		this.server = fc.getExternalContext().getInitParameter(
				"defaultServerHost");
		return this.server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getPort() {
		FacesContext fc = FacesContext.getCurrentInstance();
		this.port = Integer.parseInt(fc.getExternalContext().getInitParameter(
				"defaultServerPort"));
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setMode(boolean em) {
		this.mode = em;
	}

	public boolean isMode() {
		return mode;
	}

	public String login() {

		logger.info("User " + this.username + " has started to log in to app.");

		this.mode = false;

		try {
			ConnectionDB db = new ConnectionDB(this.username, this.password,
					this.server, this.port);
			EventContext ctx = db.getCurrentEventContext();
			this.id = ctx.getCurrentUserId().toString();
			this.role = ctx.isCurrentUserAdmin();
			this.mode = true;
			logger.info("Authentication succesfule");
			return "success";
		} catch (EJBAccessException e) {
			logger.info("Authentication not succesfule - invalid log in params");
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Invalid Login Params");
			context.addMessage("loginForm", message);
			this.mode = false;
			return "false";
		} catch (Exception e) {
			logger.info("Authentication not succesfule - connection failure");
			FacesContext context = FacesContext.getCurrentInstance();
			FacesMessage message = new FacesMessage("Connection failure");
			context.addMessage("loginForm", message);
			this.mode = false;
			return "false";
		}

	}

	public String logout() {

		FacesContext fc = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
		session.invalidate();
		return "success";
	}
}
