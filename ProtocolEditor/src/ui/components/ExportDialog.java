package ui.components;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.beans.*; //property change stuff
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.awt.*;
import java.awt.event.*;

/* 1.4 example used by DialogDemo.java. */
public class ExportDialog extends JDialog
                   implements ActionListener,
                   PropertyChangeListener{

	LinkedHashMap<String, Boolean> booleanMap;
	JOptionPane optionPane;
	Object[] array;

    public ExportDialog(JFrame frame, JComponent parent, String title, LinkedHashMap<String, Boolean> booleanMap) {
        super(frame, true);
       
        this.setLocationRelativeTo(parent);
        this.booleanMap = booleanMap;
        setTitle(title);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Make your selections"), BorderLayout.NORTH);
        
        // initialize the array to hold checkBoxes for each boolean in the map (+1 for message)
        array = new Object[booleanMap.size() + 1];
        
        // put a message at the start of the array
        JLabel message = new JLabel("<html>The <i>name</i> and <i>value</i> of each visible field will be printed.<br>" +
        		"To print additional fields or attributes, select them below.</html>");
        array[0] = message;
        
        int arrayIndex = 1;
        for (Iterator i = booleanMap.keySet().iterator(); i.hasNext();){
			String key = (String) i.next();
			Boolean bool = booleanMap.get(key);
			
			array[arrayIndex] = new JCheckBox(key, bool);
			arrayIndex++;
        }

        
        Object[] options = {JOptionPane.OK_OPTION, JOptionPane.CANCEL_OPTION};
        
        optionPane = new JOptionPane(array,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);

       setContentPane(optionPane);
       
     //Register an event handler that reacts to option pane state changes.
       optionPane.addPropertyChangeListener(this);

        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                ExportDialog.this.setVisible(false);
            }
        });
    }

    /** This method handles events for the text field. */
    public void actionPerformed(ActionEvent e) {
        
    }

 

    /** This method hides it. */
    public void clearAndHide() {
        setVisible(false);
    }

	public void propertyChange(PropertyChangeEvent event) {
		
		Object value = optionPane.getValue();
		if (value.equals(JOptionPane.OK_OPTION)) {
			for (int i=0; i<array.length; i++) {
				try {
					JCheckBox checkBox = (JCheckBox)array[i];
					String name = checkBox.getText();
					boolean checked = checkBox.isSelected();
					booleanMap.put(name, checked);
				} catch (Exception e) {
					
				}
			}
		}
		setVisible(false);
	}
	public Object getValue() {
		return optionPane.getValue();
	}
	
	
	public LinkedHashMap<String, Boolean> getBooleanMap() {
		return booleanMap;
	}
}

