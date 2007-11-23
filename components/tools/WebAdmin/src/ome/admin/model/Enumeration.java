package ome.admin.model;

import java.util.List;

import ome.model.IEnum;

public class Enumeration {
	
	private List<IEnum> enumList;
	
	private String className;

	public List<IEnum> getEnumList() {
		return enumList;
	}

	public void setEnumList(List<IEnum> enumList) {
		this.enumList = enumList;
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

}
