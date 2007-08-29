package xmlMVC;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ImportElementChooser extends JPanel{
	
	XMLView xmlView;
	
	ImportElementChooser(DataFieldNode rootNode, XMLView view) {
		
		xmlView = view;
		this.setLayout(new BorderLayout());
		
		Box buttonBox = Box.createVerticalBox();
		
		Icon addElementIcon = ImageFactory.getInstance().getIcon(ImageFactory.TWO_LEFT_ARROW_BIG);
		JButton addElementButton = new JButton("Add", addElementIcon);
		addElementButton.addActionListener(new InsertFieldsListener());
		buttonBox.add(addElementButton);
		
		Icon noIcon = ImageFactory.getInstance().getIcon(ImageFactory.N0);
		JButton doneButton = new JButton("Done", noIcon);
		doneButton.addActionListener(new DoneButtonListener());
		buttonBox.add(doneButton);
		
		JScrollPane importScrollPane = new JScrollPane(new FormDisplay(rootNode), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.add(buttonBox, BorderLayout.WEST);
		this.add(importScrollPane, BorderLayout.CENTER);
	}

	public class InsertFieldsListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			xmlView.importFieldsFromImportTree();
		}
	}
	
	public class DoneButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			xmlView.setEditingState(XMLView.EDITING_FIELDS);
			xmlView.updateFieldEditor();
		}
	}
}
