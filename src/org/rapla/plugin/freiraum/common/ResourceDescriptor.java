package org.rapla.plugin.freiraum.common;

public class ResourceDescriptor
{
	private String name;
	private String link;
	public ResourceDescriptor( ) {
	}
	
	public ResourceDescriptor( String name, String link) {
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
	
	public String toString()
	{
		return name;
	}

}