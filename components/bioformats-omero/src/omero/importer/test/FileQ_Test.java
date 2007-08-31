package omero.importer.test;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.UIManager;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import omero.importer.engine.FileQController;
import omero.importer.engine.FileQModel;
import omero.importer.engine.FileQView;
import omero.importer.engine.IFileQRow;
import omero.importer.engine.IFileQRow.Status;
import omero.importer.gui.FileQController_JTable;
import omero.importer.gui.FileQModel_JTable;
import omero.importer.gui.FileQRow_JTable;
import omero.importer.gui.FileQView_JTable;

public class FileQ_Test
{
    ArrayList   fileQ = new ArrayList();
    
    // Basic model/view/controller using the console and arraylists
    FileQModel fileQModel = new FileQModel(fileQ);
    FileQController fileQController = new FileQController(fileQModel);
    FileQView fileQView_Console = new FileQView(fileQModel);

    // GUI based model/view/controller using JTables and DefaultTableModel
    FileQModel_JTable fileQModel_JTable = new FileQModel_JTable();
    FileQController_JTable fileQController_JTable = new FileQController_JTable(fileQModel_JTable);
    FileQView_JTable fileQView_JTable = new FileQView_JTable(fileQModel_JTable);
    
    FileQ_Test()
    {       
        File filePath = new File("Test Path");
        
        Project project = new Project();
        project.setName("Test Project");
        
        Dataset dataset = new Dataset();
        dataset.setName("Test Dataset");
        
        Status status = Status.FAILED;
        
        JFrame f = new JFrame();   
        f.getContentPane().add(fileQView_JTable);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        
        // set up rows
        FileQRow_JTable row0 = new FileQRow_JTable(filePath, "Test Row 0", project, dataset, status, false);
        FileQRow_JTable row1 = new FileQRow_JTable(filePath, "Test Row 1", project, dataset, status, false);
        
        // use simple data model
        fileQModel.addObserver(fileQView_Console);
        
        fileQController.addRow(row0);
        fileQController.addRow(row1);      
        
        IFileQRow returnRow0 = fileQController.getRow(0);
        IFileQRow returnRow1 = fileQController.getRow(1);
        
        fileQController.deleteRow(1);
        fileQController.deleteRow(0);
        
        // use JTable data model
        fileQController_JTable.addRow(row0);
        fileQController_JTable.addRow(row1);  
        
        returnRow0 = fileQController_JTable.getRow(0);
        returnRow1 = fileQController_JTable.getRow(1);
        
        //fileQController_JTable.deleteRow(1);
        //fileQController_JTable.deleteRow(0);
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String laf = UIManager.getSystemLookAndFeelClassName() ;
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) 
        { System.err.println(laf + " not supported."); }
        
        new FileQ_Test();
    }
}
