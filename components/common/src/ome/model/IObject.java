package ome.model;

import java.util.Set;

import ome.model.internal.Details;
import ome.util.Filterable;
import ome.util.Validation;

public interface IObject extends Filterable{
	
    
	public Long getId();
    public void setId(Long id);
    
    // ~ Security
    // ==============================================
	public Details getDetails();
    public void setDetails(Details details);
    
    // ~ Validation
    // ==============================================    
    public boolean isValid();
    public Validation validate();
    
    // ~ For dynamic/generic programming
    // ==============================================    
    public Object retrieve(String field);
    public void putAt(String field, Object value);
    public Set fields();
}
