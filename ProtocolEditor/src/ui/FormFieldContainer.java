package ui;

import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * This vertical box contains the parent FormField at the top, then the children.
 * (Each child is in it's own <code>FormFieldContainer</code>, with any children below it)
 * @author will
 *
 */
public class FormFieldContainer extends Box{

	public FormFieldContainer() {
		super(BoxLayout.Y_AXIS);
	}
	
	public FormFieldContainer(JPanel formFieldParent) {
		this();
		add(formFieldParent);
	}
	
	public boolean isRootContainer() {
		//System.out.println("FormFieldContainer  isRootContainer() parent = " + getParent());
		return (!(getParent() instanceof FormFieldContainer));
	}
	
	public int getYPositionWithinRootContainer() {
		if (isRootContainer()) {
			return getY();
		} else {
			return getY() + ((FormFieldContainer)getParent()).getYPositionWithinRootContainer();
		}
	}
	
	public void collapseAllFormFieldChildrn(boolean collapsed) {
		
		Component[] children = getComponents();
		for (int i=0; i< children.length; i++) {
			if (children[i] instanceof FormField) {
				((FormField)children[i]).setSubStepsCollapsed(collapsed);
			} 
			else if (children[i] instanceof FormFieldContainer) {
				((FormFieldContainer)children[i]).collapseAllFormFieldChildrn(collapsed);
			}
		}
	}
}
