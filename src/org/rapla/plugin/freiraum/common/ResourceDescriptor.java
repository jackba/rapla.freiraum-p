package org.rapla.plugin.freiraum.common;

import java.util.ArrayList;
import java.util.List;

public class ResourceDescriptor
{
	private String name;
	private String link;
	private String id;
	private List<String> searchTerms = new ArrayList<String>();
	
	public ResourceDescriptor( ) {
	}
	
	public ResourceDescriptor( String id,String name, String link, List<String> searchTerms) {
		this.id = id;
		this.name = name;
		this.link = link;
		this.searchTerms = searchTerms;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public String getLink() 
	{
		return link;
	}
	
	public String getId()
	{
		return id;
	}
	
	public String toString()
	{
		return name;
	}
	
	public List<String> getSearchTerms() {
		return searchTerms;
	}

}