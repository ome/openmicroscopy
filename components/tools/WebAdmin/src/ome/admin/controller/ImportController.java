/*
 * ome.admin.controller.ImportController
 *
 *   Copyright 2007 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.admin.controller;

// Java imports
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

// Third-party libraries
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.tree2.HtmlTree;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.apache.myfaces.custom.tree2.TreeNodeBase;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

// Application-internal dependencies
import ome.admin.data.ConnectionDB;
import ome.admin.logic.ImportManagerDelegate;
import ome.admin.logic.UpdateManagerDelegate;
import ome.admin.model.User;
import ome.conditions.ApiUsageException;
import ome.model.meta.Experimenter;
import ome.utils.HttpJSFUtil;
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
                logger.error(e.getMessage(), e.fillInStackTrace());
                message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "ValidatorException: " + e.getMessage() + nodeId,
                        "ValidatorException: " + e.getMessage() + nodeId);

                context.addMessage("clientTreeForm", message);
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
            logger.error(e.getMessage(), e.fillInStackTrace());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Api usage exception: "
                    + e.getMessage(), "Api usage exception: " + e.getMessage());
            context.addMessage("clientTree", message);
            return NavigationResults.FALSE;
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("File not found : "
                    + e.getMessage());
            context.addMessage("clientTree", message);
            return NavigationResults.FALSE;
        } catch (IOException e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("IO exception: "
                    + e.getMessage());
            context.addMessage("clientTree", message);
            return NavigationResults.FALSE;
        } catch (Exception e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
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

        } catch (Exception e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
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
            logger.error(e.getMessage(), e.fillInStackTrace());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("File not found : "
                    + e.getMessage());
            context.addMessage("clientTreeForm", message);
        } catch (IOException e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("IO exception: "
                    + e.getMessage());
            context.addMessage("clientTreeForm", message);
        } catch (Exception e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Unexpected exception. "
                    + e.getMessage());
            context.addMessage("clientTree", message);
        }
        return this.sort;
    }

    public void createList(ActionEvent event) {
        try {
            ConnectionDB db = new ConnectionDB();
            List<Experimenter> users = db.lookupExperimenters();

            HSSFWorkbook wb = new HSSFWorkbook();
            HSSFSheet sheet = wb.createSheet("experimenters");

            // Create a row and put some cells in it. Rows are 0 based.
            HSSFRow header = sheet.createRow((short) 0);
            // Or do it on one line.
            header.createCell((short) 0).setCellValue(
                    new HSSFRichTextString("Omename"));
            header.createCell((short) 1).setCellValue(
                    new HSSFRichTextString("FirstName"));
            header.createCell((short) 2).setCellValue(
                    new HSSFRichTextString("Middlename"));
            header.createCell((short) 3).setCellValue(
                    new HSSFRichTextString("Lastname"));
            header.createCell((short) 4).setCellValue(
                    new HSSFRichTextString("Email"));
            header.createCell((short) 5).setCellValue(
                    new HSSFRichTextString("Institution"));

            for (int i = 0; i < users.size(); i++) {
                Experimenter exp = (Experimenter) users.get(i);
                if (exp.getOmeName().equals("root"))
                    continue;
                HSSFRow row = sheet.createRow((short) i + 1);
                row.createCell((short) 0).setCellValue(
                        new HSSFRichTextString(exp.getOmeName()));
                row.createCell((short) 1).setCellValue(
                        new HSSFRichTextString(exp.getFirstName()));
                row.createCell((short) 2).setCellValue(
                        new HSSFRichTextString(exp.getMiddleName()));
                row.createCell((short) 3).setCellValue(
                        new HSSFRichTextString(exp.getLastName()));
                row.createCell((short) 4).setCellValue(
                        new HSSFRichTextString(exp.getEmail()));
                row.createCell((short) 5).setCellValue(
                        new HSSFRichTextString(exp.getInstitution()));
            }

            // Write the output to a file
            String fileName = "/OMERO/WebAdmin/experimenters.xls";
            FileOutputStream fileOut = new FileOutputStream(fileName);
            wb.write(fileOut);
            fileOut.close();

            HttpServletResponse response = HttpJSFUtil.getResponse();

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=experimenters.xls");
            response.setHeader("Cache-Control", "no-cache");

            // byte [] bytes = wb.getBytes();
            InputStream in = new FileInputStream(fileName);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int i;
            while ((i = in.read()) != -1)
                out.write(i);
            in.close();
            byte[] bytes = out.toByteArray();

            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
            response.getOutputStream().close();

            FacesContext context = FacesContext.getCurrentInstance();
            context.responseComplete();

        } catch (IOException e) {
            logger.error(e.getMessage(), e.fillInStackTrace());
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage("Cannot download file : "
                    + e.getMessage());
            context.addMessage("downloadForm", message);
        }

    }

    /**
     * Checks direcotry exists.
     * 
     * @return boolean
     */
    public boolean isDirectory() {
        boolean result = checkDirectory();
        return result;
    }

    /**
     * Checks that directory where uploaded file is stored /OMERO/UsersLists
     * exists.
     * 
     * @return
     */
    private boolean checkDirectory() {
        FacesContext context = FacesContext.getCurrentInstance();
        File dir = new File(context.getExternalContext().getInitParameter(
                "usersListsDir"));

        if (!dir.exists()) {
            if (dir.mkdir()) {
                logger.info("isDirectory: directory " + dir.getAbsolutePath()
                        + " did not exist and was created successful.");
                FacesMessage message = new FacesMessage("Directory "
                        + dir.getAbsolutePath() + " created successful.");
                context.addMessage("uploadedNewFileForm", message);
                return true;
            } else {
                logger.info("isDirectory: directory " + dir.getAbsolutePath()
                        + " did not exist and could not be created.");
                FacesMessage message = new FacesMessage(
                        "IOException: Could not create directory "
                                + dir.getAbsolutePath() + ".");
                context.addMessage("uploadedNewFileForm", message);
                return false;
            }
        } else
            return true;
    }
}
