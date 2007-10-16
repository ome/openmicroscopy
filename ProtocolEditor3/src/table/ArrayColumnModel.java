package table;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

public class ArrayColumnModel extends DefaultTableColumnModel {

	  public void addColumn(TableColumn tc) {
	    super.addColumn(tc);
	    
	  }
	  
	  public void setHeaderValue(Object colName) {
		  
		  String newColName = (String)colName;
		  
		  System.out.println("ArrayColumnModel.setHeaderValue " + newColName);
	  }
}