package org.openmicroscopy.shoola.agents.datamng.editors.categoryGroup;



//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.datamng.DataManagerUIF;
import org.openmicroscopy.shoola.env.data.model.CategorySummary;
import org.openmicroscopy.shoola.util.ui.ExtendedDefaultListModel;
import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.table.TableComponent;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.table.TableComponentCellRenderer;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class CreateCategoryPane
    extends JPanel
{
    
    MultilineLabel                  nameArea, descriptionArea;
    
    JList                           existingCategories;
    
    CreateCategoryPane(CategorySummary[] data) 
    {
        initComponents(data);
        buildGUI();
    }

    /** Initializes the components. */
    private void initComponents(CategorySummary[] data)
    {
        existingCategories  = new JList(new ExtendedDefaultListModel(data));
        existingCategories.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        existingCategories.setLayoutOrientation(JList.VERTICAL);
        
        //textfields
        nameArea = new MultilineLabel("");
        nameArea.setForeground(DataManagerUIF.STEELBLUE);
        nameArea.setEditable(true);
        descriptionArea = new MultilineLabel("");
        descriptionArea.setForeground(DataManagerUIF.STEELBLUE);
        descriptionArea.setEditable(true);
    }
    
    /** Build and lay out the GUI. */
    private void buildGUI()
    {
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(buildSummaryPanel());
        add(Box.createRigidArea(DataManagerUIF.VBOX));
        add(buildListPanel());
    }
    
    private JPanel buildListPanel()
    {
        JPanel p = new JPanel();
        JLabel label = UIUtilities.setTextFont("Existing categories ");
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        p.setLayout(gridbag);
        c.weightx = 0.5;        
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.NORTHWEST;
        gridbag.setConstraints(label, c);
        p.add(label);
        JScrollPane scrollPane = new JScrollPane(existingCategories);
        scrollPane.setPreferredSize(CreateEditor.MAX_SCROLL);
        c.gridx = 1;
        gridbag.setConstraints(scrollPane, c);
        p.add(scrollPane);
        return UIUtilities.buildComponentPanel(p);
    }
    
    /** Build panel containing fields to fill in. */
    private JPanel buildSummaryPanel() 
    {   
        JPanel  p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(buildTable());
        p.setOpaque(false);
        return p;
    }
    
    /** 
     * A <code>2x2</code> table model to view dataset summary.
     * The first column contains the property names (name, description)
     * and the second column holds the corresponding values. 
     * <code>name</code> and <code>description</code> values
     * are marked as editable.
     */
    private TableComponent buildTable()
    {
        TableComponent table = new TableComponent(2, 2);
        setTableLayout(table);
        
        // Labels
        JLabel label = new JLabel(" Name");
        table.setValueAt(label, 0, 0);
        label = new JLabel(" Description");
        table.setValueAt(label, 1, 0);
        table.setValueAt(buildScrollPane(nameArea), 0, 1); 
        table.setValueAt(buildScrollPane(descriptionArea), 1, 1);
        return table;
    }
    
    private JScrollPane buildScrollPane(JTextArea area)
    {
        JScrollPane scrollPane  = new JScrollPane(area);
        scrollPane.setPreferredSize(DataManagerUIF.DIM_SCROLL_TABLE);
        return scrollPane;
    }
    
    /** Set the layout of the table. */
    private void setTableLayout(TableComponent table)
    {
        table.setTableHeader(null);
        table.setRowHeight(1, DataManagerUIF.ROW_TABLE_HEIGHT);
        table.setRowHeight(0, DataManagerUIF.ROW_NAME_FIELD);
        table.setDefaultRenderer(JComponent.class, 
                                new TableComponentCellRenderer());
        table.setDefaultEditor(JComponent.class, 
                                new TableComponentCellEditor());
    }
 
}
