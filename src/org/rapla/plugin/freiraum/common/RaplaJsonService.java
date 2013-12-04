package org.rapla.plugin.freiraum.common;

import java.util.List;

import com.google.gwtjsonrpc.common.AsyncCallback;

public interface RaplaJsonService extends com.google.gwtjsonrpc.common.RemoteJsonService
{
	void getResources(String type,String searchTerm,AsyncCallback<List<ResourceDescriptor>> callback);
	
	void getResource(String id,AsyncCallback<ResourceDetail> callback);
	
	void getOrganigram(AsyncCallback<CategoryDescription> callback);
	
	void getResourcesFromCategory(String type,CategoryDescription category, AsyncCallback<List<ResourceDescriptor>> callback);
}