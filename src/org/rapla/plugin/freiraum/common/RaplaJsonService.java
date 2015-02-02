package org.rapla.plugin.freiraum.common;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.rapla.components.util.ParseDateException;
import org.rapla.framework.RaplaException;
import org.rapla.rest.gwtjsonrpc.common.FutureResult;
import org.rapla.rest.gwtjsonrpc.common.RemoteJsonService;
import org.rapla.rest.gwtjsonrpc.common.ResultType;

@WebService
public interface RaplaJsonService extends RemoteJsonService
{
	/** returns a List of ResourceDescriptor 
	 * @param resourceType can be "rooms","courses","persons" or the key of a dynamic type specified in rapla can be null
	 * @param categoryId the id of the abteilung/studiengang category can be null 
	 * @param language specify a language for the category or attribute names. Rapla will try to find a translation if it exists (if null default server language will be used)
	 * @throws RaplaException 
	 */
    @ResultType(value=ResourceDescription.class,container=List.class)
	FutureResult<List<ResourceDescription>> getResources(
			@WebParam(name="resourceType")String resourceType
			,@WebParam(name="categoryId")String categoryId
			,@WebParam(name="language") String language
			) ;
	
	/** Returns the details of a given resource
	 * 
	 * @param resourceId
	 * @param language specify a language for the category or attribute names. Rapla will try to find a translation if it exists (if null default server language will be used)
	 * @throws RaplaException 
	 */
    @ResultType(value=ResourceDetail.class)
	FutureResult<ResourceDetail> getResource(
			@WebParam(name="resourceId")String resourceId
			,@WebParam(name="language") String language
			);
	/**
	 * returns the child categories to a given categoryId. If id is null all children of the root category are returned
	 * @param categoryId  
	 * @param language specify a language for the category or attribute names. Rapla will try to find a translation if it exists (if null default server language will be used)
	 * @throws RaplaException 
	 */
	@ResultType(value=CategoryDescription.class,container=List.class)
	FutureResult<List<CategoryDescription>> getOrganigram(
			@WebParam(name="categoryId")String categoryId
			,@WebParam(name="language") String language
			);
	/**
	 *Returns all resources which are open for public and have no booked reservations for the given time interval. If end parameter is not set the max allocatable time will be returned as value in the free resource map.  
	 * @param start start of the interval. If not set, then current time will be used. format is "2013-12-30 12:30"
	 * @param end end of the interval. If not set max bookable time will be returned. format is "2013-12-30 12:30"
	 * @param resourceType you can only query resources of a specified type (e.g. room)
	 * @param language specify a language for the category or attribute names. Rapla will try to find a translation if it exists (if null default server language will be used)
	 * @throws RaplaException 
	 * @throws ParseDateException 
	 */
	@ResultType(value=Event.class,container=List.class)
	FutureResult<List<Event>> getFreeResources(
			@WebParam(name="start")String start
			,@WebParam(name="end") String end
			,@WebParam(name="resourceType") String resourceType
			,@WebParam(name="language") String language
			);
	
	/**Returns the events for a given resource in the given time interval.
	 * @param start start of the interval format is "2013-12-30 12:30" Note: use %20 for space and  %3A for : as url encoding
	 * @param end end of the interval format is "2013-12-30 12:30"
	 * @param resourceId id of the resource 
	 * @param language specify a language for the category or attribute names. Rapla will try to find a translation if it exists (if null default server language will be used)
	 * @throws ParseDateException 
	 * @throws RaplaException 
	 */
	@ResultType(value=Event.class,container=List.class)
	FutureResult<List<Event>> getEvents(
			@WebParam(name="start")String start
			,@WebParam(name="end") String end
			,@WebParam(name="resourceId")String resourceId
			,@WebParam(name="language") String language
			);
}