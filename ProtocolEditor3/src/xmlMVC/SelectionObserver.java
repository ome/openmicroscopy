package xmlMVC;

public interface SelectionObserver {
	
	// called when the list of highlighted fields (in Tree) changes
	// propagated through to View, to update FieldEditor diplay
	public void selectionChanged();
}
