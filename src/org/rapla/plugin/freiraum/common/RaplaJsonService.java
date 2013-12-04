package org.rapla.plugin.freiraum.common;

import java.util.List;

import com.google.gwtjsonrpc.common.AllowCrossSiteRequest;
import com.google.gwtjsonrpc.common.AsyncCallback;

public interface RaplaJsonService extends com.google.gwtjsonrpc.common.RemoteJsonService
{
	@AllowCrossSiteRequest
	void getResources(String type,String searchTerm,AsyncCallback<List<ResourceDescriptor>> callback);
	@AllowCrossSiteRequest
	void getResource(String id,AsyncCallback<ResourceDetail> callback);
	@AllowCrossSiteRequest
	void getOrganigram(AsyncCallback<CategoryDescription> callback);
	@AllowCrossSiteRequest
	void getResourcesFromCategory(String type,CategoryDescription category, AsyncCallback<List<ResourceDescriptor>> callback);
	
}