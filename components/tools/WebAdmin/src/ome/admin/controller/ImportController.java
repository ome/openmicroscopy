/*
 * ome.admin.controller
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.controller;

// Java imports
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

// Third-party libraries
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.validator.ValidatorException;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.tree2.HtmlTree;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.apache.myfaces.custom.tree2.TreeNodeBase;

// Application-internal dependencies
import ome.admin.logic.ImportManagerDelegate;
import ome.admin.logic.UpdateManagerDelegate;
import ome.admin.model.User;
import ome.conditions.ApiUsageException;
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
public class ImportController implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * log4j logger
     */
    static Logger logger = Logger.getLogger(ImportController.class.getName());

    /**
     * {@link org.apache.myfaces.custom.tree2.HtmlTree}
     */
    private HtmlTree tree;

    /**
     * {@link ome.admin.logic.UpdateManagerDelegate}
     */
    private UpdateManagerDelegate update = new UpdateManagerDelegate();

    /**
     * {@link java.lang.String}
     */
    private String nodePath;

    /**
     * {@link org.apache.myfaces.custom.tree2.TreeNode}
     */
    private TreeNode selectedNode;

    /**
     * Sort item default value "lastName".
     */
    private String sortItem = "lastName";

    /**
     * Sort attribute default value is "asc"
     */
    private String sort = "asc";

    /**
     * {@link java.lang.String}
     */
    private String fileName;

    /**
     * {@link javax.faces.model.ListDataModel} The data collection wrapped by
     * this {@link javax.faces.model.DataModel}.
     */
    private DataModel userModel = new ListDataModel();

    /**
     * {@link ome.admin.logic.ImportManagerDelegate}.
     */
    private ImportManagerDelegate imp = new ImportManagerDelegate();

    /**
     * Gets data for generating tree structure
     * 
     * @return {@link org.apache.myfaces.custom.tree2.TreeNode}
     */
    public TreeNode getTreeData() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        String usersListsDir = facesContext.getExternalContext()
                .getInitParameter("usersListsDir");

        update.setPath(usersListsDir);

        List<String> dirs = update.getDirs();

        TreeNode treeData = new TreeNodeBase("list-folder", usersListsDir,
                false);

        for (String dirName : dirs) {
            TreeNodeBase personNode = new TreeNodeBase("person", dirName, false);
            update.setPath(usersListsDir + "/" + dirName);
            List<String> files = update.getFiles();
            for (String fileName : files) {
                personNode.getChildren().add(
                        new TreeNodeBase("document", fileName, usersListsDir
                                + "/" + dirName + "/" + fileName, true));
            }
            treeData.getChildren().add(personNode);
        }
        return treeData;
    }

    /*
     * public TreeModel getExpandedTreeData() { return new
     * TreeModelBase(getTreeData()); }
     */

    /**
     * Sets {@link ome.admin.controller.ImportController#tree}
     * 
     * @param tree
     *            {@link org.apache.myfaces.custom.tree2.HtmlTree}
     */
    public void setTree(HtmlTree tree) {
        this.tree = tree;
    }

    /**
     * Gets {@link org.apache.myfaces.custom.tree2.HtmlTree}
     * 
     * @return {@link ome.admin.controller.ImportController#tree}
     */
    public HtmlTree getTree() {
        return this.tree;
    }

    /**
     * Expand whole tree
     * 
     * @return {@link java.lang.String}
     */
    public String expandAll() {
        this.tree.expandAll();
        return null;
    }

    /**
     * Sets {@link ome.admin.controller.ImportController#nodePath}
     * 
     * @param nodePath
     * 
     */
    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    /**
     * Gets {@link ome.admin.controller.ImportController#nodePath}
     * 
     * @return {@link java.lang.String}
     */
    public String getNodePath() {
        return this.nodePath;
    }

    /**
     * Checks path inside the tree
     * 
     * @param context
     *            {@link javax.faces.context.FacesContext}
     * @param component
     *            {@link javax.faces.component.UIComponent}
     * @param value
     *            {@link java.lang.Object}
     */
    public void checkPath(FacesContext context, UIComponent component,
            java.lang.Object value) {
        // make sure path is valid (leaves cannot be expanded or renderer will
        // complain)
        FacesMessage message = null;

        String[] path = this.tree.getPathInformation(value.toString());

        for (int i = 0; i < path.length; i++) {
            String nodeId = path[i];
            try {
                this.tree.setNodeId(nodeId);
            } catch (Exception e) {
                throw new ValidatorException(message, e);
            }

            if (this.tree.getNode().isLeaf()) {
                message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Invalid node path (cannot expand a leaf): " + nodeId,
                        "Invalid node path (cannot expand a leaf): " + nodeId);

                context.addMessage("clientTreeForm", message);
            }
        }
    }

    /**
     * Expand path by parameter
     * 
     * @param event
     *            {@link javax.faces.event.ActionEvent}
     */
    public void expandPath(ActionEvent event) {
        this.tree.expandPath(this.tree.getPathInformation(this.nodePath));
    }

    /**
     * Selets node when action is called and wraps data from selected file
     * 
     * @return String "success" or "false"
     */
    public String selectedNode() {
        try {
            this.selectedNode = this.tree.getNode();
            this.fileName = selectedNode.getIdentifier();
            this.imp.setFilePath(fileName);
            this.userModel.setWrappedData(imp.sortItems("firstname", "asc"));
            return NavigationResults.SUCCESS;
        } catch (ApiUsageException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "IO exception: "
                            + e.getMessage(), "IO exception: " + e.getMessage());
            context.addMessage("clientTree", message);
            return NavigationResults.FALSE;
        } catch (FileNotFoundException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("File not found : "
                    + e.getMessage());
            context.addMessage("clientTree", message);
            return NavigationResults.FALSE;
        } catch (IOException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("IO exception: "
                    + e.getMessage());
            context.addMessage("clientTree", message);
            return NavigationResults.FALSE;
        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("IO exception: "
                    + e.getMessage());
            context.addMessage("clientTree", message);
            return NavigationResults.FALSE;
        }

    }

    /**
     * Is called by action for saving datas. Selected users are created.
     * 
     * @return {@link java.lang.String} "success" or "false"
     */
    public String saveItems() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            imp.createExperimenters((List<User>) this.userModel
                    .getWrappedData());

            IAdminExperimenterController ia = (IAdminExperimenterController) context
                    .getApplication().getVariableResolver().resolveVariable(
                            context, "IAEManagerBean");
            ia.setEditMode(true);

            FacesMessage message = new FacesMessage(
                    "Imported succesful. Go to Scientist.");
            context.addMessage("clientTree", message);
        } catch (Exception e) {
            FacesMessage message = new FacesMessage("Exception: "
                    + e.getMessage());
            context.addMessage("clientTree", message);
            return NavigationResults.FALSE;
        }

        return NavigationResults.SUCCESS;
    }

    /**
     * Gets default wrapped {@link java.util.List} of
     * {@link ome.admin.model.User}
     * 
     * @return {@link java.util.List}
     */
    public DataModel getExperimenters() {
        return this.userModel;
    }

    /**
     * Gets file name.
     * 
     * @return String
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets attribute from {@link javax.faces.component.UIComponent}.
     * 
     * @param event
     *            {@link javax.faces.event.ActionEvent} object from the
     *            specified source component and action command.
     * @param name
     *            name of attribute as {@link java.lang.String}.
     * @return {@link java.lang.String}
     */
    private static String getAttribute(ActionEvent event, String name) {
        return (String) event.getComponent().getAttributes().get(name);
    }

    /**
     * Sorts items on the data collection of
     * {@link ome.model.meta.ExperimenterGroup} wrapped by this
     * {@link javax.faces.model.DataModel}.
     * 
     * @param event
     *            {@link javax.faces.event.ActionEvent} object from the
     *            specified source component and action command.
     * @return {@link ome.admin.controller.IAdminExperimenterController#sort}
     */
    public String sortItems(ActionEvent event) {
        try {
            this.sortItem = getAttribute(event, "sortItem");
            this.sort = getAttribute(event, "sort");
            this.userModel.setWrappedData(imp.sortItems(sortItem, sort));
        } catch (FileNotFoundException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("File not found : "
                    + e.getMessage());
            context.addMessage("clientTreeForm", message);
        } catch (IOException e) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("IO exception: "
                    + e.getMessage());
            context.addMessage("clientTreeForm", message);
        } catch (Exception e) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Unexpected exception. "
                    + e.getMessage());
            context.addMessage("clientTree", message);
        }
        return this.sort;
    }

}
