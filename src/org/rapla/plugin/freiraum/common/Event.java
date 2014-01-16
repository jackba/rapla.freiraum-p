package org.rapla.plugin.freiraum.common;

import java.util.List;

public class Event
{
	String name;
	String startDate;
	String start;
	String endDate;
	String end;
	List<ResourceDescriptor> resources;
	
	public Event()
	{
	}

	public Event(String name,String startDate, String start, String endDate, String end, List<ResourceDescriptor> resources) {
		super();
		this.name = name;
		this.startDate = startDate;
		this.endDate = endDate;
		this.start = start;
		this.end = end;
		this.resources = resources;
	}

	public String getStart() {
		return start;
	}
	
	/**
	@deprecated use getStart instead
	*/
	@Deprecated
	public String getBegin() {
		return start;
	}

	public String getEnd() {
		return end;
	}

	public List<ResourceDescriptor> getResources() {
		return resources;
	}
	
	public String toString() 
	{
		return name + " " + start + "-" + end ;
	}
	
	
}