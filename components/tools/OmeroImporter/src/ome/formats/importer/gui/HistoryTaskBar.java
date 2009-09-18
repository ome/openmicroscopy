package ome.formats.importer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import layout.TableLayout;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;


public class HistoryTaskBar extends JXPanel implements ActionListener
{
    JXTaskPaneContainer  tpContainer = new JXTaskPaneContainer();
    
    DefaultListModel today = new DefaultListModel(); 
    DefaultListModel yesterday = new DefaultListModel();
    DefaultListModel thisWeek = new DefaultListModel();
    DefaultListModel lastWeek = new DefaultListModel();
    DefaultListModel thisMonth = new DefaultListModel();
    
    
    public HistoryTaskBar() 
    {
        
        double table[][] =
        {{158}, // columns
        { TableLayout.PREFERRED}}; // rows
        
        TableLayout layout = new TableLayout(table);
       
        this.setLayout(layout);
        
        tpContainer.setBorder(null);
        tpContainer.setLayout(new VerticalLayout(0));                   
        this.add(tpContainer, "0,0");
    }
    
    public void addTaskPane( String name, JList list )
    {
        JXTaskPane taskPane = new JXTaskPane();       
        taskPane.setTitle(name);
       
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(list);
        taskPane.getContentPane().getParent().add(scrollPane);
        
        taskPane.setCollapsed(true);
        
        tpContainer.add(taskPane);
    }

    public JList getList(JList list)
    {
        list.setCellRenderer(new ImportCellRenderer());
        //list.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        
        // Add a listener for mouse clicks
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() > 0) {          // Double-click
                    // Get item index
                    int index = list.locationToIndex(evt.getPoint());
                    if (index >= 0)
                    {
                        ImportEntry entry = (ImportEntry) list.getModel().getElementAt(index);
                        firePropertyChange("QUICK_HISTORY", -1, entry.importKey);
                    }
                }
            }
        });
        return list;
    }
    

    public void updateList(JList list, DefaultListModel mine, DefaultListModel theirs)
    {
        mine.clear();
        for (int i =0; i < theirs.size(); i++)
        {
            mine.addElement(theirs.get(i));
        }
        list.validate();
        tpContainer.validate();
    }

    public void actionPerformed(ActionEvent e)
    {
    }
}

class ImportCellRenderer extends JLabel implements ListCellRenderer {

    public ImportCellRenderer() {
        setOpaque(true);
        setIconTextGap(0);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        ImportEntry entry = (ImportEntry) value;
        setText(entry.getTitle());
        setIcon(entry.getImage());
        setFont(UIManager.getFont("TableCell.font"));
        
        if (isSelected) {
            //setToolTipText("test");
            setBackground(UIManager.getColor("Table.selectionBackground"));
            setForeground(Color.black);
        } else {
            setBackground(Color.white);
            setForeground(Color.black);
        }
        return this;
    }
}

class ImportEntry {
    private final String title;
    private final String imagePath;
    private ImageIcon image;
    public final int importKey;

    public ImportEntry(String title, String imagePath, int importKey) {
        this.title = title;
        this.imagePath = imagePath;
        this.importKey = importKey;
    }

    public String getTitle() {
        return title;
    }
        
    public ImageIcon getImage() {
        if (imagePath != null)
        {
            java.net.URL imgURL = GuiImporter.class.getResource(imagePath);
            if (imgURL != null)
            {
               image = new ImageIcon(imgURL);
            } else
            {
                System.err.println("Couldn't find icon: " + imagePath);
            }
        }
        return image;
    }

    // Override standard toString method to give a useful result
    public String toString() {
        return title;
    }
}