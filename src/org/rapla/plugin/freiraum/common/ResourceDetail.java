package org.rapla.plugin.freiraum.common;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResourceDetail {
	Map<String,ResourceDetailRow> attributeMap;
	List<Event> events;
	public ResourceDetail() 
	{
		attributeMap = new LinkedHashMap<String,ResourceDetailRow>();
	}

	public ResourceDetail(Map<String,ResourceDetailRow> map, List<Event> events) 
	{
		this.attributeMap = map;
		this.events = events;
	}

	public ResourceDetailRow getValue(String key)
	{
		return attributeMap.get( key);
	}
	
	public Collection<String> getKeys() 
	{
		return attributeMap.keySet();
	}
	
	public String toString()
	{
		return attributeMap.toString() + " " + events.toString();
	}
}