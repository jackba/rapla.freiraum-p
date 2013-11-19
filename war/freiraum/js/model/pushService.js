/*--------------------------------------------------------------------------*
 | Copyright (c) 2013 Fabian Luft, Martin Wilhelm			   				|
 |                                                                          |
 | This program is free software; you can redistribute it and/or modify     |
 | it under the terms of the GNU General Public License as published by the |
 | Free Software Foundation. A copy of the license has been included with   |
 | these distribution in the COPYING file, if not go to www.fsf.org         |
 |                                                                          |
 | As a special exception, you are granted the permissions to link this     |
 | program with every library, which license fulfills the Open Source       |
 | Definition as published by the Open Source Initiative (OSI).             |
 *--------------------------------------------------------------------------*/

var longPollingData = null;
var longPollingPushEvent = null;
var executeLongPolling = false;

function subscribeToPushService (data, pushEvent)
{
    var source = null;

    if ( typeof (EventSource) !== "undefined")
    {
        var url = createURLWithData(URL_PUSH_SERVICE, data)
        source = new EventSource (url);
        source.onmessage = function (event)
        {
            console.log(event.data);

            if (event.data == PUSH_DATA_UPDATED)
            {
                $(window).trigger(pushEvent);
            }
        };
    }
    else
    {
        longPollingData = data;
        longPollingPushEvent = pushEvent;

        executeLongPolling = true;

        source = subscribeToLongPollingService();
    }

    return source;
}

function createURLWithData (baseURL, data)
{
    var url = baseURL;
    var index = 0;

    $.each(data, function (key, value)
    {
        var separationTag = '&';

        if (index == 0)
        {
            separationTag = '?';
        }

        url += separationTag + key + '=' + value;

        index++;
    });

    return url;
}

function subscribeToLongPollingService ()
{
    var xhr = $.ajax(
    {
        url : URL_LONG_POLLING_SERVICE,
        data : longPollingData,
        cache : false,
        success : function (data)
        {
            $(window).trigger(longPollingPushEvent);
        },
        dataType : "json",
        complete : function (data)
        {
            if (executeLongPolling)
            {
                subscribeToLongPollingService();
            }
        },
        timeout : 30000
    });

    return xhr;
}

function stopLongPolling ()
{
    executeLongPolling = false;
}
