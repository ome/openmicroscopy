package ome.model;

public interface ILink extends IObject{
    
	public IObject getParent();
    public void setParent(IObject parent);
    
    public IObject getChild();
    public void setChild(IObject child);
    
}
    
