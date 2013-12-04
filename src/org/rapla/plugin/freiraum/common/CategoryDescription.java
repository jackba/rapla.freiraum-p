package org.rapla.plugin.freiraum.common;

import java.util.List;


public class CategoryDescription
{
	private List<CategoryDescription> children;
	private String name;
	
	public CategoryDescription()
	{
	}
	
	public CategoryDescription(List<CategoryDescription> children, String name) 
	{
		super();
		this.children = children;
		this.name = name;
	}

	public List<CategoryDescription> getChildren() 
	{
		return children;
	}

	public String getName() 
	{
		return name;
	}
	
	public String toString()
	{
		return name;
	}
	
	
}