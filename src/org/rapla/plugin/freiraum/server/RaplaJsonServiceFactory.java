package org.rapla.plugin.freiraum.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.rapla.components.util.ParseDateException;
import org.rapla.components.util.TimeInterval;
import org.rapla.entities.Category;
import org.rapla.entities.EntityNotFoundException;
import org.rapla.entities.MultiLanguageName;
import org.rapla.entities.domain.Allocatable;
import org.rapla.entities.storage.EntityResolver;
import org.rapla.entities.storage.RefEntity;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaContextException;
import org.rapla.framework.RaplaException;
import org.rapla.framework.RaplaLocale;
import org.rapla.plugin.freiraum.common.CategoryDescription;
import org.rapla.plugin.freiraum.common.Event;
import org.rapla.plugin.freiraum.common.RaplaJsonService;
import org.rapla.plugin.freiraum.common.ResourceDescription;
import org.rapla.plugin.freiraum.common.ResourceDetail;
import org.rapla.rest.gwtjsonrpc.common.FutureResult;
import org.rapla.rest.gwtjsonrpc.common.ResultImpl;
import org.rapla.server.RemoteMethodFactory;
import org.rapla.server.RemoteSession;
import org.rapla.storage.RaplaSecurityException;
import org.rapla.storage.StorageOperator;

public class RaplaJsonServiceFactory extends RaplaComponent implements RemoteMethodFactory<RaplaJsonService>
{
	AllocatableExporter exporter;
	EntityResolver resolver;
	
	public RaplaJsonServiceFactory(RaplaContext context, Configuration config) throws RaplaException {
		super( context);
		exporter = new AllocatableExporter(context, config);
		resolver = getContext().lookup(StorageOperator.class);
	}
	
	@Override
	public RaplaJsonService createService(RemoteSession remoteSession)
			throws RaplaContextException {
		return new RaplaJsonService() {
			@Override
			public FutureResult<List<ResourceDescription>> getResources(String type, String categoryId, String language) {
				try
				{
					Category category = getCategoryForId(categoryId);
					Locale locale = getLocale( language);
					List<ResourceDescription> result = exporter.getAllocatableList(type, category, locale);
					return new ResultImpl<List<ResourceDescription>>(result);
				}
				catch (RaplaException ex)
				{
					return new ResultImpl<List<ResourceDescription>>(ex);
				}
			}

			@Override
			public FutureResult<ResourceDetail> getResource(String resourceId, String language)  
			{
				try
				{
					if ( resourceId == null)
					{
						throw new IllegalArgumentException("resourceId must be set");
					}
					// FIXME replace with correct link
	//				StringBuffer a = request.getRequestURL();
	//				int indexOf = a.lastIndexOf("/rapla");
	//				String linkPrefix = a.substring(0, indexOf);
					String linkPrefix = "rapla";
					Allocatable allocatable = resolver.resolve( resourceId, Allocatable.class);
					Locale locale = getLocale(language); 
					ResourceDetail detail = exporter.getAllocatable(allocatable, linkPrefix, locale);
					if ( detail != null)
					{
						return new ResultImpl<ResourceDetail>(detail);
					}
					else
					{
						return new ResultImpl<ResourceDetail>(new RaplaSecurityException("No permission to read resource"));
					}
				}
				catch (RaplaException ex)
				{
					return new ResultImpl<ResourceDetail>(ex);
				}
				
			}

			public FutureResult<List<Event>> getFreeResources(String start, String end,String resourceType, String language) 
			{
				try
				{
					TimeInterval interval = createInterval(start, end);
					Locale locale = getLocale(language); 
					List<Event> result = exporter.getFreeRooms(interval, resourceType, locale);
					return new ResultImpl<List<Event>>(result);
				}
				catch (RaplaException ex)
				{
					return new ResultImpl<List<Event>>(ex);
				}
			}

			public TimeInterval createInterval(String start, String end) throws RaplaException {
				try
				{
					Date startDate = null;
					Date endDate = null;
					
					if ( start != null)
					{
						startDate = getRaplaLocale().getSerializableFormat().parseTimestamp( start );
					}
					if ( end != null)
					{
						endDate = getRaplaLocale().getSerializableFormat().parseTimestamp( end );
					}
					TimeInterval interval = new TimeInterval(startDate, endDate);
					return interval;
				}
				catch (ParseDateException ex)
				{
					throw new RaplaException( ex.getMessage(), ex);
				}
			}
			
			@Override
			public FutureResult<List<Event>> getEvents(String start, String end, String resourceId, String language) 
			{
				try
				{
					if ( resourceId == null)
					{
						throw new IllegalArgumentException("resourceId must be set");
					}
					Locale locale = getLocale(language); 
					TimeInterval interval = createInterval(start, end);
					Allocatable allocatable = resolver.resolve( resourceId, Allocatable.class);
					List<Event> result = exporter.getEvents(allocatable, interval,  locale);
					return new ResultImpl<List<Event>>(result);
				}
				catch (RaplaException ex)
				{
					return new ResultImpl<List<Event>>(ex);
				}
			}
			
			public FutureResult<List<CategoryDescription>> getOrganigram(String categoryId, String language)  {
				try
				{
					Category category;
					if ( categoryId == null)
					{
						category = getOrganigram();
						if ( category == null)
						{
							throw new EntityNotFoundException("Category with name studiengang needed");
						}
					}
					else
					{
						category = getCategoryForId( categoryId);
					}
					Locale locale = getLocale( language);
					List<CategoryDescription> result = get( category, locale);
					return new ResultImpl<List<CategoryDescription>>(result);
				}
				catch (RaplaException ex)
				{
					return new ResultImpl<List<CategoryDescription>>(ex);
				}
			}

			private Category getOrganigram() {
				Category root = getQuery().getSuperCategory();
				// there is no given key for the organigram, so we use a fallback
				for (Category cat: root.getCategories())
				{
					MultiLanguageName name = cat.getName();
					Collection<String> availableLanguages = name.getAvailableLanguages();
					for ( String language: availableLanguages)
					{
						String translation = name.getName(language);
						// this captures all root categories with name studiengang or studienga"nge
						if (translation != null && translation.toLowerCase().indexOf("studieng")>= 0)
						{
							return cat;
						}
					}
				}
				return null;
			}
			
			private Category getCategoryForId(String categoryId)
					throws EntityNotFoundException, RaplaException {
				Category category = null;
				if ( categoryId != null && !categoryId.trim().isEmpty())
				{
					category = resolver.resolve( categoryId, Category.class);
				}
				return category;
			}

			private Locale getLocale(String language) {
				RaplaLocale raplaLocale = getRaplaLocale();
				if ( language == null  || language.trim().length() == 0)
				{
					return raplaLocale.getLocale();
				}
				return new Locale(language);
			}
			
			private List<CategoryDescription> get( Category cat, Locale locale)
			{
				List<CategoryDescription> children  =new ArrayList<CategoryDescription>();
				for (Category child : cat.getCategories())
				{
					String id = ((RefEntity)child).getId().toString();
					String name = child.getName( locale);
					CategoryDescription childDescription = new CategoryDescription(id, name);
					children.add( childDescription);
				}
				return children;
			}
		};
	}

}
