/*--------------------------------------------------------------------------*
 | Copyright (c) 2013 Fabian Luft, Martin Wilhelm			    			|
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
var calendarScreenController = function (previousScreenDataRequestObject, previousTheme, activeScreen)
{
    /**
     * Define all public functions. Every function listed here is accessible
     * from outside.
     */
    this.createAndLayoutScreen = createAndLayoutScreen;
    this.reLayout = reLayout;
    this.registerUserActionRecognizer = registerUserActionRecognizer;
    this.getRequestDataObject = getRequestDataObject;
    this.getTheme = getTheme;
    this.unregisterRecognizerAndServices = unregisterRecognizerAndServices;

    /**
     * The screenObject of the previous screen. This is needed to access
     * all relevant request data.
     */
    var previousScreenDataRequestObject = previousScreenDataRequestObject;
    var activeScreen = activeScreen;
    var previousTheme = previousTheme;

    var monthEventInfo = null;

    function getTheme ()
    {

    }
    
    function unregisterRecognizerAndServices ()
    {
        
    }

    /**
     * This function is called by the next screen to get all
     * collected and relevant data.
     *
     * Every screen controller has to implement getRequestRelevantData
     */
    function getRequestDataObject ()
    {
        return null;
    }

    /**
     * Creates and layouts the screen. The data needed for this screen
     * is fetched asynchronously after all recognizers have been registered.
     */
    function createAndLayoutScreen ()
    {
        setLayout();
        registerAjaxRequestRecognizer();
        registerUserActionRecognizer();
        fetchLayout();
    }

    function reLayout ()
    {
        layoutCalendar();
    }

    /**
     * Register the basic recognizers needed for the ajax request.
     * All request specific success recognizers are registered just in time.
     */
    function registerAjaxRequestRecognizer ()
    {

    }

    function registerUserActionRecognizer ()
    {
        activeScreen.find('.home').unbind('click');
        activeScreen.find('.home').bind('click', loadHomeScreenAction);

    }

    function fetchLayout ()
    {
        $(window).one(ACTION_AJAX_CALENDAR, calendarLoadedAction);

        //displayName = previousScreenDataRequestObject['displayName'];

        var data =
        {
            layout : 'monthoverview',
            name : 'TIT10'
        };

        sendAjaxRequest(data, 'TEST', ACTION_AJAX_CALENDAR);
    }

    function loadHomeScreenAction ()
    {
        $.mobile.changePage($('#homeScreen'),
        {
            transition : 'fade'
        });
    }

    function calendarLoadedAction (event, data)
    {
        monthEventInfo = data.split(',');
        layoutCalendar()
    }

    function layoutCalendar ()
    {
        var width = $(window).width();
        var height = calculateContentHeight();

        createCalendarDaysHeader();
        createCalendarDays();

        var actualWidth = width - (parseInt($('.daysHeader').css('margin-right')) * ($(WEEKDAY).length - 1) );
        var daysColumnWidth = Math.floor(actualWidth / $(WEEKDAY).length);

        $('.daysColumnWidth').css('width', daysColumnWidth + 'px');

        var actualHeight = height - (parseInt($('.day').css('margin-top')) * 5 ) - activeScreen.find('.daysHeader').outerHeight(true);

        $('.day').css('height', Math.floor(actualHeight / 5) + 'px');
        $('.day').css('max-height', daysColumnWidth + 'px');
        $('.day').css('font-size', Math.floor(actualHeight / 5) * 0.2 + 'px');

    }

    function createCalendarDaysHeader ()
    {
        var daysRowHeader = activeScreen.find('.daysRowHeader');

        if (daysRowHeader.children().length == 0)
        {
            $(WEEKDAY).each(function (index, value)
            {
                var div = '<div class="daysHeader daysColumnWidth">' + value + '</div>';

                daysRowHeader.append(div);
            });
        }
    }

    function createCalendarDays ()
    {
        var datesArray = new Array ();

        var daysOfMonth = new Date (2013, 4, 0).getDate();

        var firstOfMonth = new Date (2013, 3, 1).getDay();

        if (firstOfMonth > 0)
        {
            var daysOfPreviousMonth = new Date (2013, 3, 0).getDate();
            var beginDateOfPreviousMonth = daysOfPreviousMonth - firstOfMonth + 1;

            for (var i = beginDateOfPreviousMonth; i <= daysOfPreviousMonth; i++)
            {
                datesArray.push(i);
            }

        }

        for (var i = 1; i <= daysOfMonth; i++)
        {
            datesArray.push(i);
        }

        for (var i = 1; i <= 35 - daysOfMonth - firstOfMonth; i++)
        {
            datesArray.push(i);
        }

        var daysRows = activeScreen.find('.daysRows');

        daysRows.children().remove();

        for (var i = 0; i < 5; i++)
        {
            var daysRow = $('<div>');
            daysRow.addClass('daysRow');

            for (var j = 0; j < $(WEEKDAY).length; j++)
            {
                currentDayPosition = (i * $(WEEKDAY).length) + j;

                var day = $('<div>');
                day.addClass('day daysColumnWidth');

                var link = $('<a href="http://google.com" style="width: 100%; height: 100%; text-decoration: none;">');
                var dayHasAppointment = $('<div>');
                dayHasAppointment.addClass('dayHasAppointmentIndicator');
                var dayDateWrapper = $('<div>');
                dayDateWrapper.addClass('dayDateWrapper');
                var dayDate = $('<div>');
                dayDate.addClass('dayDate');

                dayDate.append(datesArray[currentDayPosition]);

                dayDateWrapper.append(dayDate);

                link.append(dayHasAppointment);
                link.append(dayDateWrapper);

                if (currentDayPosition < firstOfMonth || currentDayPosition > (firstOfMonth + daysOfMonth - 1))
                {
                    day.addClass('dayNotInCurMonth')
                }
                else
                {
                    if (monthEventInfo[currentDayPosition - firstOfMonth] > 0)
                    {
                        dayHasAppointment.addClass('dayHasAppointment');
                    }
                }

                if (currentDayPosition == (firstOfMonth + (new Date ().getDate()) - 1))
                {
                    day.addClass('today');
                }

                day.append(link);
                daysRow.append(day);
            }

            daysRows.append(daysRow);
        }

    }

    function setLayout ()
    {
        //set your new theme letter
        var theme = previousTheme;

        //reset all the buttons widgets
        $.mobile.activePage.find('.ui-btn').removeClass('ui-btn-up-a ui-btn-up-b ui-btn-up-c ui-btn-up-d ui-btn-up-e ui-btn-hover-a ui-btn-hover-b ui-btn-hover-c ui-btn-hover-d ui-btn-hover-e').addClass('ui-btn-up-' + theme).attr('data-theme', theme);

        //reset the header/footer widgets
        $.mobile.activePage.find('.ui-header, .ui-footer').removeClass('ui-bar-a ui-bar-b ui-bar-c ui-bar-d ui-bar-e').addClass('ui-bar-' + theme).attr('data-theme', theme);

        //reset the page widget
        $.mobile.activePage.removeClass('ui-body-a ui-body-b ui-body-c ui-body-d ui-body-e').addClass('ui-body-' + theme).attr('data-theme', theme);
    }

}