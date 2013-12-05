package org.rapla.plugin.freiraum.common;

import java.util.List;

import com.google.gwtjsonrpc.common.AllowCrossSiteRequest;
import com.google.gwtjsonrpc.common.AsyncCallback;

public interface RaplaJsonService extends com.google.gwtjsonrpc.common.RemoteJsonService
{
	@AllowCrossSiteRequest
	/**
	 * @param type is rooms,courses,persons or the key of a dynamic type specified in rapla can be null
	 * @param categoryId the id of the abteilung/studiengang category can be null 
	 * @param callback
	 */
	void getResources(String type,String categoryId,AsyncCallback<List<ResourceDescriptor>> callback);
	@AllowCrossSiteRequest
	void getResource(String id,AsyncCallback<ResourceDetail> callback);
	/**
	 * returns the child categories to a given categoryId. If id is null all children of the root category are returned
	 * @param categoryId  
	 * @param callback
	 */
	@AllowCrossSiteRequest
	void getOrganigram(String categoryId,AsyncCallback<List<CategoryDescription>> callback);
}