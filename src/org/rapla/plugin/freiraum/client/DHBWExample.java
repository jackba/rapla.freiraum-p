package org.rapla.plugin.freiraum.client;

import java.net.URL;
import java.util.List;

import org.rapla.RaplaClient;
import org.rapla.plugin.freiraum.common.Event;
import org.rapla.plugin.freiraum.common.RaplaJsonService;
import org.rapla.plugin.freiraum.common.ResourceDescription;


public class DHBWExample {

    public static void main(String[] args) 
    {
        try
        {
            RaplaClient container = new RaplaClient(new URL("https://rapla.dhbw-karlsruhe.de/"));
            RaplaJsonService service = container.getInstance(RaplaJsonService.class);
            String categoryId = null;
            String language = "de";
            String resourceType = "raum";
            // get all resources that are available
            List<ResourceDescription> resources = service.getResources(resourceType, categoryId, language).get();
            for (ResourceDescription resource:resources)
            {
                System.out.println(resource.getName());
            }
            String currentDateAndTime = "2015-02-04T07:20";
            String endTime = null;
            // get the resources that are available from the currentDateAndTime
            List<Event> list = service.getFreeResources(currentDateAndTime ,endTime, resourceType, language).get();
            for (Event evt:list)
            {
                List<ResourceDescription> freeResource = evt.getResources();
                System.out.println(freeResource + " ist frei bis " + evt.getEnd());
                
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
