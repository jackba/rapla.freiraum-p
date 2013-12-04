package org.rapla.plugin.freiraum.common;

public class ResourceDescriptor
{
	private String name;
	private String link;
	private int id;
	
	public ResourceDescriptor( ) {
	}
	
	public ResourceDescriptor( int id,String name, String link) {
		this.id = id;
		this.name = name;
		this.link = link;
	}
	
	public String getLabel() 
	{
		return name;
	}
	
	public String getLink() 
	{
		return link;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String toString()
	{
		return name;
	}

}