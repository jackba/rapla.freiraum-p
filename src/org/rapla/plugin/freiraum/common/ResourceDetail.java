package org.rapla.plugin.freiraum.common;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceDetail {
	Map<String,ResourceDetailRow> attributeMap;
	public ResourceDetail() {
		attributeMap = new LinkedHashMap<String,ResourceDetailRow>();
	}

	public ResourceDetail(Map<String,ResourceDetailRow> map) {
		this.attributeMap = map;
	}

	public ResourceDetailRow getValue(String key)
	{
		return attributeMap.get( key);
	}
	
	public Collection<String> getKeys() {
		return attributeMap.keySet();
	}
	
	public String toString()
	{
		return attributeMap.toString();
	}
}