package org.rapla.plugin.freiraum.common;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceDetail {
	Map<String,ResourceDetailRow> attributeMap;
	Map<String,ResourceDescription> resourceLinks;
	
	public ResourceDetail() 
	{
		attributeMap = new LinkedHashMap<String,ResourceDetailRow>();
	}

	public ResourceDetail(Map<String,ResourceDetailRow> map,Map<String,ResourceDescription> resourceLinks) 
	{
		this.attributeMap = map;
		this.resourceLinks = resourceLinks;
	}

	public ResourceDetailRow getRow(String key)
	{
		return attributeMap.get( key);
	}
	
	public Map<String,ResourceDescription> getResourceLinks()
	{
	    return resourceLinks;
	}
	
	public Collection<String> getKeys() 
	{
		return attributeMap.keySet();
	}
	
	public String toString()
	{
		return attributeMap.toString();
	}
	
}