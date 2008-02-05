package actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;

import ui.IModel;
import util.ImageFactory;

public class MultiplyValuesAction extends ProtocolEditorAction {
	
	public MultiplyValuesAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Multiply Values by...");
		putValue(Action.SHORT_DESCRIPTION, "Multiply selected Numerical values by a factor of...");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.EDU_MATHS)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		multiplyValueOfSelectedFields();
	}
	
	
	
	public void stateChanged(ChangeEvent e) {
		
		String[] fileList = model.getOpenFileList();
		
		this.setEnabled(!(fileList.length == 0));
	}
	
	
	public void multiplyValueOfSelectedFields() {
		String s = (String)JOptionPane.showInputDialog(
                frame,
                "Multiply all selected field values by:\n"
                + "(enter a number)\n NB: This will only apply to NUMBER fields \n"
                + "To divide, use /, eg '/3' ",
                "Enter a number",
                JOptionPane.QUESTION_MESSAGE);
		
		if (s != null && s.length() > 0) {
			boolean division = false;
			if (s.startsWith("/")) {
				s = s.substring(1);
				division = true;
			}
			
			float factor;
			try {
				factor = Float.parseFloat(s);
				if (division) model.multiplyValueOfSelectedFields(1/factor);
				else model.multiplyValueOfSelectedFields(factor);
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(frame, 
						"You didn't enter a valid number", 
						"Invalid multiplication factor", 
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
