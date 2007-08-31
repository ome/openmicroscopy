package omero.importer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.UIManager;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ome.formats.importer.Main;
import ome.formats.importer.util.Actions;
import omero.importer.thirdparty.ETable;

public class FileQView_JTable 
    extends JPanel 
    implements ActionListener 
{
    public FileQModel_JTable model = null;
    public ETable view = new ETable();

    private static final long serialVersionUID = -4239932269937114120L;


    JButton         refreshBtn;
    JButton         addBtn;
    JButton         removeBtn;
    JButton         importBtn;
    JButton         clearDoneBtn;
    JButton         clearFailedBtn;

    public boolean cancel = false;
    public boolean importing = false;
    
    public FileQView_JTable(FileQModel_JTable model) {
        this.model = model;
        view.setModel(model);
        
//      ----- Variables -----
        // Debug Borders
        Boolean debugBorders = false;
        
        // Size of the add/remove/refresh buttons (which are square).
        int buttonSize = 40;
        // Add graphic for the refresh button
        //String refreshIcon = "gfx/recycled.png";
        // Add graphic for add button
        String addIcon = "gfx/add.png";
        // Remove graphics for remove button
        String removeIcon = "gfx/remove.png";
        
        // Width of the status columns
        int statusWidth = 100;

// ----- GUI Layout Elements -----
        // Start layout here
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(6,5,9,8));
        
        JPanel buttonPanel = new JPanel();
        if (debugBorders == true) 
            buttonPanel.setBorder(BorderFactory.createLineBorder(Color.red, 1));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));

        addBtn = addButton(">>", addIcon, null);
        addBtn.setMaximumSize(new Dimension(buttonSize, buttonSize));
        addBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
        addBtn.setMinimumSize(new Dimension(buttonSize, buttonSize));
        addBtn.setSize(new Dimension(buttonSize, buttonSize));
        addBtn.setActionCommand(Actions.ADD);
        addBtn.addActionListener(this);
        
        removeBtn = addButton("<<", removeIcon, null);
        removeBtn.setMaximumSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setMinimumSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setActionCommand(Actions.REMOVE);
        removeBtn.addActionListener(this);
        
        buttonPanel.add(Box.createRigidArea(new Dimension(0,60)));
        //buttonPanel.add(refreshBtn);
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(addBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0,5)));
        buttonPanel.add(removeBtn);
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(Box.createRigidArea(new Dimension(0,60)));
        add(buttonPanel);
        add(Box.createRigidArea(new Dimension(5,0)));

        JPanel queuePanel = new JPanel();
        if (debugBorders == true)
            queuePanel.setBorder(BorderFactory.createLineBorder(Color.red, 1));
        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.PAGE_AXIS));
        queuePanel.add(Box.createRigidArea(new Dimension(0,10)));
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS)); 
        JLabel label = new JLabel("Import Queue:");
        labelPanel.add(label);
        labelPanel.add(Box.createHorizontalGlue());
        queuePanel.add(labelPanel);
        queuePanel.add(Box.createRigidArea(new Dimension(0,5)));

        TableColumnModel cModel =  view.getColumnModel();
               
        FileQView_JTableHeader tableHeader = new FileQView_JTableHeader();
              
        // Create a custom header for the table
        cModel.getColumn(0).setHeaderRenderer(tableHeader);
        cModel.getColumn(1).setHeaderRenderer(tableHeader);
        cModel.getColumn(2).setHeaderRenderer(tableHeader);
        cModel.getColumn(0).setCellRenderer(new FileQView_JTableLeftDot(view));
        cModel.getColumn(1).setCellRenderer(new FileQView_JTableCellCenter(view));
        cModel.getColumn(2).setCellRenderer(new FileQView_JTableCellCenter(view));            
        
        // Set the width of the status column
        TableColumn statusColumn = view.getColumnModel().getColumn(2);
        statusColumn.setPreferredWidth(statusWidth);
        statusColumn.setMaxWidth(statusWidth);
        statusColumn.setMinWidth(statusWidth);
              

        FileQView_JTableSelectionListener listener = 
            new FileQView_JTableSelectionListener(view);
        view.getSelectionModel().addListSelectionListener(listener);
        //queue.getColumnModel().getSelectionModel()
        //    .addListSelectionListener(listener);
        
        // Hide 4th to 8th columns
        TableColumnModel tcm = view.getColumnModel();
        
        for (int remove = 7; remove >= 3; remove --)
        {
            TableColumn removeColumn = tcm.getColumn(remove);
            tcm.removeColumn(removeColumn);
        }
       
        // Add the table to the scollpane
        JScrollPane scrollPane = new JScrollPane(view);
        
        queuePanel.add(scrollPane);
        
        JPanel importPanel = new JPanel();
        importPanel.setLayout(new BoxLayout(importPanel, BoxLayout.LINE_AXIS));
        clearDoneBtn = addButton("Clear Done", null, null);
        clearFailedBtn = addButton("Clear Failed", null, null);
        importBtn = addButton("Import", null, null);
        importPanel.add(Box.createHorizontalGlue());
        importPanel.add(clearDoneBtn);
        clearDoneBtn.setEnabled(false);
        clearDoneBtn.setActionCommand(Actions.CLEARDONE);
        clearDoneBtn.addActionListener(this);
        importPanel.add(Box.createRigidArea(new Dimension(0,5)));
        importPanel.add(clearFailedBtn);
        clearFailedBtn.setEnabled(false);
        clearFailedBtn.setActionCommand(Actions.CLEARFAILED);
        clearFailedBtn.addActionListener(this);
        importPanel.add(Box.createRigidArea(new Dimension(0,10)));
        importPanel.add(importBtn);
        importBtn.setEnabled(false);
        importBtn.setActionCommand(Actions.IMPORT);
        importBtn.addActionListener(this);
        queuePanel.add(Box.createRigidArea(new Dimension(0,5)));
        queuePanel.add(importPanel);
        add(queuePanel);        
    }
    
//    public ImportContainer[] getFilesAndDataset() {
//
//        int num = table.getRowCount();     
//        ImportContainer[] fads = new ImportContainer[num];
//
//        for (int i = 0; i < num; i++)
//        {
//            try {
//                boolean archive = (Boolean) table.getValueAt(i, 5);
//                File file = new File(table.getValueAt(i, 4).toString());
//                Dataset dataset = (Dataset) table.getValueAt(i, 3);
//                String imageName = table.getValueAt(i, 0).toString();
//                fads[i] = new ImportContainer(file, dataset, imageName, archive);            }
//            catch (ArrayIndexOutOfBoundsException e) {
//                e.printStackTrace();
//            }
//
//        }
//        return fads;
//    }

    // Helpers

    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();
        if (src == addBtn)
            firePropertyChange(Actions.ADD, false, true);
        if (src == removeBtn)
            firePropertyChange(Actions.REMOVE, false, true);
//        if (src == refreshBtn)
//            firePropertyChange(Actions.REFRESH, false, true);
        if (src == clearDoneBtn)
            firePropertyChange(Actions.CLEARDONE, false, true);
        if (src == clearDoneBtn)
            firePropertyChange(Actions.CLEARFAILED, false, true);
        if (src == importBtn)
        {
            view.clearSelection();
            firePropertyChange(Actions.IMPORT, false, true); 
        }
    }
    
    static JButton addButton(String name, String image, String tooltip)
    {
        JButton button = null;

        if (image == null) 
        {
            button = new JButton(name);
        } else {
            java.net.URL imgURL = Main.class.getResource(image);
            if (imgURL != null)
            {
                button = new JButton(null, new ImageIcon(imgURL));
            } else {
                button = new JButton(name);
                System.err.println("Couldn't find icon: " + image);
            }
        }
        return button;
    }

    // Main
    
    public static void main (String[] args) {

        String laf = UIManager.getSystemLookAndFeelClassName() ;
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) 
        { System.err.println(laf + " not supported."); }
        
        FileQModel_JTable model = new FileQModel_JTable();
        FileQView_JTable q = new FileQView_JTable(model); 
        JFrame f = new JFrame();   
        f.getContentPane().add(q);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }

}
