package ui;

public interface SelectionObserver {
	
	// called when the list of highlighted fields (in Tree) changes
	// propagated through to View, to update FieldEditor display
	public void selectionChanged();
}
