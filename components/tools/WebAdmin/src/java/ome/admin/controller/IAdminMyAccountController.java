/*
 * IAdminExperimenterController.java
 *
 * Created on March 14, 2007, 10:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ome.admin.controller;

import ome.admin.logic.IAdminExperimenterManagerDelegate;

import javax.faces.component.UIInput;
import javax.faces.component.UIComponent;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.event.ActionEvent;

import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;

import java.util.ArrayList;
import java.util.List;

import ome.admin.controller.LoginBean;
import javax.servlet.http.HttpSession;
/**
 *
 * @author Ola
 */
public class IAdminMyAccountController implements java.io.Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private Experimenter experimenter;

    private IAdminExperimenterManagerDelegate iadmin = new IAdminExperimenterManagerDelegate();
    
    private Long defaultGroup = 0L;
    
    private String password = "";
    private String password2 = "";
    
    /** Creates a new instance of IAdminMyAccountController */
    public IAdminMyAccountController() {  
        FacesContext facesContext = FacesContext.getCurrentInstance();
        LoginBean lb = (LoginBean) facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, "LoginBean");
        this.experimenter = iadmin.getExperimenterById(Long.parseLong(lb.getId()));
    }
    
    private static String getAttribute(ActionEvent event, String name) {
        return (String) event.getComponent().getAttributes().get(name);
    }
    
    public String changeMyPassword(ActionEvent event){
        try {
            this.experimenter = (Experimenter) this.iadmin.getExperimenterById(Long.parseLong(getAttribute(event, "userid")));
            return "success";
        } catch(Exception e) {
            FacesContext context = FacesContext.getCurrentInstance(); 
            FacesMessage message = new FacesMessage("You cannot change your password"); 
            context.addMessage("experimenterForm", message); 
            return "false";
        }
    }
    
    public void updateMyPassword() {
        if(!password2.equals(this.password)) {
            FacesContext context = FacesContext.getCurrentInstance(); 
            FacesMessage message = new FacesMessage("Confirmation has to be the same as password."); 
            context.addMessage("changePassword", message);                
        } else {
            iadmin.changeMyPassword(this.password);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            LoginBean lb = (LoginBean) facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, "LoginBean");
            lb.setPassword(this.password);
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            session.invalidate();
        }
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPassword() {
        return this.password;
    }
 
    public void setPassword2(String password2) {
        this.password2 = password2;
    }
    
    public String getPassword2() {
        return this.password2;
    }
    
    public String editMyAccount(ActionEvent event) {
        try {
            this.experimenter = (Experimenter) this.iadmin.getExperimenterById(Long.parseLong(getAttribute(event, "userid")));
            return "success";
        } catch(Exception e) {
            FacesContext context = FacesContext.getCurrentInstance(); 
            FacesMessage message = new FacesMessage("You cannot edit your account"); 
            context.addMessage("experimenterForm", message); 
            return "false";
        }
    }
    
    public List getMyGroups() {
        List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
        try {
            ExperimenterGroup[] exg = iadmin.containedMyGroups(this.experimenter.getId());

            for (int i = 0; i< exg.length; i++){
                groups.add(exg[i]);
            } 

        } catch(Exception e) {
            FacesContext context = FacesContext.getCurrentInstance(); 
            FacesMessage message = new FacesMessage("You cannot get your groups"); 
            context.addMessage("experimenterForm", message);      
        }
        return wrapAsGUIList(groups);
    }
        
    public void setDefaultGroup(Long id) {
        this.defaultGroup=id;
    }

    public Long getDefaultGroup() {
        try {
            ExperimenterGroup exg = iadmin.getDefaultGroup(this.experimenter.getId());
            this.defaultGroup = exg.getId();
        } catch(Exception e) {
            FacesContext context = FacesContext.getCurrentInstance(); 
            FacesMessage message = new FacesMessage("You cannot change details, baceuse user is loged in"); 
            context.addMessage("experimenterForm", message);  
        }
        return this.defaultGroup;
    }

    private static synchronized List wrapAsGUIList(List originalList) {
        ArrayList<SelectItem> items = new ArrayList<SelectItem>(originalList.size());
        for (int i = 0, n = originalList.size(); i < n; i++){
            ExperimenterGroup bean = (ExperimenterGroup)originalList.get(i);
            SelectItem item = new SelectItem(bean.getId().toString(), bean.getName());
            items.add(item);
        }
        return items;
    }
            
    public void setExperimenter(Experimenter exp) {
        this.experimenter = exp;
    }
    
    public Experimenter getExperimenter() {
        return this.experimenter;
    }

    
    public String editExperimenter() {
        try {
            this.experimenter = (Experimenter) iadmin.getExperimenterById(this.experimenter.getId());
            return "success";
        } catch(Exception e) {
            FacesContext context = FacesContext.getCurrentInstance(); 
            FacesMessage message = new FacesMessage("You cannot edit experimenter"); 
            context.addMessage("experimenterForm", message);  
            return "false";
        }
    }
    
    public String updateExperimenter() {
        try {
            iadmin.updateMyAccount(this.experimenter, this.defaultGroup);
            return "success";
        } catch(Exception e) {
            FacesContext context = FacesContext.getCurrentInstance(); 
            FacesMessage message = new FacesMessage("You cannot change details, baceuse user is loged in"); 
            context.addMessage("experimenterForm", message);  
            return "false";
        }
    }
    
    public void validateEmail(FacesContext context, UIComponent toValidate, Object value) {
        String email = (String) value;
        if (email.indexOf('@') == -1) {        
            ((UIInput)toValidate).setValid(false);
            FacesMessage message = new FacesMessage("Invalid Email");
            context.addMessage(toValidate.getClientId(context), message);
        }
        if(iadmin.checkEmail(email) && !email.equals(this.experimenter.getEmail())){
            ((UIInput)toValidate).setValid(false);
            FacesMessage message = new FacesMessage("Email exist");
            context.addMessage(toValidate.getClientId(context), message);
        }
    }
        
}
