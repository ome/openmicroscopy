package ome.model;

import ome.model.internal.Details;
import ome.util.Filterable;

public interface IObject extends Filterable{
	
	public Long getId();
    public void setId(Long id);
	public Details getDetails();
    public void setDetails(Details details);
    
}
