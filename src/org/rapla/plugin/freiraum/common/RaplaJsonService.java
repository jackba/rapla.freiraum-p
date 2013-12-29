package org.rapla.plugin.freiraum.common;

import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import com.google.gwtjsonrpc.common.AllowCrossSiteRequest;
import com.google.gwtjsonrpc.common.AsyncCallback;

@WebService
public interface RaplaJsonService extends com.google.gwtjsonrpc.common.RemoteJsonService
{
	@AllowCrossSiteRequest
	/** returns a List of ResourceDescriptor 
	 * @param resourceType can be "rooms","courses","persons" or the key of a dynamic type specified in rapla can be null
	 * @param categoryId the id of the abteilung/studiengang category can be null 
	 * @param language specify a language for the category or attribute names. Rapla will try to find a translation if it exists (if null default server language will be used)
	 */
	void getResources(
			@WebParam(name="resourceType")String resourceType
			,@WebParam(name="categoryId")String categoryId
			,@WebParam(name="language") String language
			,AsyncCallback<List<ResourceDescriptor>> callback);
	
	/** Returns the details of a given resource
	 * 
	 * @param resourceId
	 * @param language specify a language for the category or attribute names. Rapla will try to find a translation if it exists (if null default server language will be used)
	 */
	@AllowCrossSiteRequest
	void getResource(
			@WebParam(name="resourceId")String resourceId
			,@WebParam(name="language") String language
			,AsyncCallback<ResourceDetail> callback);
	/**
	 * returns the child categories to a given categoryId. If id is null all children of the root category are returned
	 * @param categoryId  
	 * @param language specify a language for the category or attribute names. Rapla will try to find a translation if it exists (if null default server language will be used)
	 */
	@AllowCrossSiteRequest
	void getOrganigram(
			@WebParam(name="categoryId")String categoryId
			,@WebParam(name="language") String language
			,AsyncCallback<List<CategoryDescription>> callback);
	/**
	 *Returns all resources which are open for public and have no booked reservations for the given time interval. If end parameter is not set the max allocatable time will be returned as value in the free resource map.  
	 * @param start start of the interval. If not set, then current time will be used
	 * @param end end of the interval. If not set max bookable time will be returned
	 * @param resourceType you can only query resources of a specified type (e.g. room)
	 * @param language specify a language for the category or attribute names. Rapla will try to find a translation if it exists (if null default server language will be used)
	 */
	@AllowCrossSiteRequest
	void getFreeResources(
			@WebParam(name="start")String start
			,@WebParam(name="end") String end
			,@WebParam(name="resourceType") String resourceType
			,@WebParam(name="language") String language
			,AsyncCallback<Map<ResourceDescriptor,String>> callback);
	
	/**Returns the events for a given resource in the given time interval.
	 * @param start start of the interval
	 * @param end end of the interval
	 * @param resourceId id of the resource 
	 * @param language specify a language for the category or attribute names. Rapla will try to find a translation if it exists (if null default server language will be used)
	 */
	@AllowCrossSiteRequest
	void getEvents(
			@WebParam(name="start")String start
			,@WebParam(name="end") String end
			,@WebParam(name="resourceId")String resourceId
			,@WebParam(name="language") String language
			,AsyncCallback<List<Event>> callback);
}