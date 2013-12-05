package org.rapla.plugin.freiraum.common;



public class CategoryDescription
{
	private String id;
	private String name;
	
	public CategoryDescription()
	{
		name = "";
	}
	
	public CategoryDescription(String id, String name) 
	{
		super();
		this.id = id;
		this.name = name;
	}

	public String getName() 
	{
		return name;
	}
	
	public String getId()
	{
		return id;
	}
	
	
	public String toString()
	{
		return name;
	}
	
	
}