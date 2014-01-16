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

	public ResourceDetailRow getRow(String key)
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
	
	/** @deprecated use RaplaJsonService.getEvents() instead and pass currentTime and date as start and the currentDate and 21:00 as end   */
	@Deprecated 
	public List<Event> getEvents() 
	{
		return events;
	}
}