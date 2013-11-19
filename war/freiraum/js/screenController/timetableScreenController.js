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

var timetableScreenController = function (previousScreenDataRequestObject, previousTheme, activeScreen)
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

    var dayOffset = 0;
    var displayName = null;
    var multipleEventsSameTime = new Array ();
    var currentDisplayedEventSameTime = 0;
    var intervalWorkerProcess = null;

    var isCalendarViewActive = true;

    var pushService = null;
    
    var hoursStart;
    var hoursAmount;

    function getTheme ()
    {

    }

    function unregisterRecognizerAndServices ()
    {
        $(window).unbind(ACTION_PUSH_RECEIVED);

        if ( typeof (EventSource) !== "undefined")
        {
            pushService.close();
        }
        else
        {
            stopLongPolling();
            pushService.abort();
        }

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
        registerPushService();
        // fetchLayout has to run before the actual data in this case the events
        // can be fetched. fetchData is called within the action handler of fetchLayout
        fetchLayout();
    }

    function reLayout ()
    {

        var width = $(window).width();
        var height = calculateContentHeight();

        layoutDisplayDays(width, height, undefined);
        layoutDisplayHours(width, height, undefined, undefined);
        layoutEvents();

    }

    /**
     * Register the basic recognizers needed for the ajax request.
     * All request specific success recognizers are registered just in time.
     */
    function registerAjaxRequestRecognizer ()
    {
        $(window).bind(ACTION_AJAX_ERROR_EVENT, requestFailedAction);
    }

    function registerUserActionRecognizer ()
    {
        activeScreen.find('.home').unbind('click');
        activeScreen.find('.home').bind('click', loadHomeScreenAction);

        activeScreen.find('.toggleView').unbind('click');
        activeScreen.find('.toggleView').bind('click', toggleEventsView);

        $('.previousDay').unbind('click');
        $('.previousDay').bind('click', loadPreviousDayAction);

        $('.currentDay').unbind('click');
        $('.currentDay').bind('click', loadCurrentDayAction);

        $('.nextDay').unbind('click');
        $('.nextDay').bind('click', loadNextDayAction);
        
        $('.ttTimetable').unbind('swipeleft');
        $('.ttTimetable').bind('swipeleft', loadNextDayAction);
        
        $('.ttTimetable').unbind('swiperight');
        $('.ttTimetable').bind('swiperight', loadPreviousDayAction);
        
    }

    function registerPushService ()
    {
        $(window).bind(ACTION_PUSH_RECEIVED, pushReceivedAction);

        var data =
        {
            resourceId : previousScreenDataRequestObject['resourceId']
        }

        pushService = subscribeToPushService(data, ACTION_PUSH_RECEIVED);
    }

    function registerTimerAction ()
    {
        var seconds = 60 - new Date ().getSeconds();
        window.setTimeout(function ()
        {
            window.setInterval(function ()
            {
                setCurrentTime($('#timetable').height(), false);
            }, 60000);

            setCurrentTime($('#timetable').height(), false);
        }, seconds * 1000);
    }
    
    function fetchLayout ()
    {
    	$(window).one(ACTION_AJAX_TIMETABLE, timetableLoadedAction);

        displayName = previousScreenDataRequestObject['displayName'];

        var data =
        {
            layout : 'timetable'
        };

        sendAjaxRequest(data, LOAD_TIMETABLE, ACTION_AJAX_TIMETABLE);
    }

    function fetchData ()
    {
        $(window).one(ACTION_AJAX_EVENT, eventsLoadedAction);

        displayName = previousScreenDataRequestObject['displayName'];

        var data =
        {
            query : previousScreenDataRequestObject['query'],
            resourceId : previousScreenDataRequestObject['resourceId'],
            dayOffset : dayOffset,
            elementclass : previousScreenDataRequestObject['elementclass']
        };

        sendAjaxRequest(data, LOAD_EVENTS, ACTION_AJAX_EVENT);
    }

    function requestFailedAction ()
    {
        $(window).unbind(ACTION_AJAX_ERROR_EVENT);
    }

    function pushReceivedAction ()
    {
        fetchData();
    }
    
    function timetableLoadedAction (event, data)
    {
    	hoursStart = parseInt(data.start);
    	hoursAmount = parseInt(data.hours);
        layoutTimetableView();
        fetchData();
    }

    function eventsLoadedAction (event, data)
    {
        activeScreen.find('.ttEvents').children().remove();
        activeScreen.find('.ttEvents').append(data);
        layoutEvents();

        $('#ttInfoCourse').html(displayName);

        registerUserActionRecognizer();
        
        setDocumentTitle();
    }

    function loadHomeScreenAction ()
    {
        $.mobile.changePage($('#homeScreen'),
        {
            transition : 'fade'
        });
    }

    /**function loadCalendarAction ()
     {
     $.mobile.changePage($('#calendarScreen'),
     {
     transition : 'flip'
     });
     }*/

    function loadPreviousDayAction ()
    {
        dayOffset--;
        layoutTimetableView();
        fetchData();
    }

    function loadCurrentDayAction ()
    {
        dayOffset = 0;
        layoutTimetableView();
        fetchData();
    }

    function loadNextDayAction ()
    {
        dayOffset++;
        layoutTimetableView();
        fetchData();
    }

    function layoutTimetableView ()
    {

        var width = activeScreen.find('.contentArea').width();
        var height = activeScreen.find('.contentArea').height();

        toggleTimetableClasses();

        createAndLayoutDisplayDays(width, height);
        createAndLayoutDisplayHours(width, height);

        setDocumentTitle();

    }

    function createAndLayoutDisplayDays (width, height)
    {
        var daysNumber = activeScreen.find('.ttTimetable').data('days');

        createDisplayDays(daysNumber);
        layoutDisplayDays(width, height, daysNumber);

    }

    function createDisplayDays (daysNumber)
    {

        $('.ttDaysName').html('');

        var date = new Date ();
        date.setDate(date.getDate() + dayOffset);
        for (var i = 0; i < daysNumber; i++)
        {
            var displayDate = $('<div/>');
            displayDate.addClass('ttDay');
            
            var displayDateWrapper = $('<div/>');
            displayDateWrapper.addClass('ttDayWrapper');
            
            displayDateWrapper.html(WEEKDAY[date.getDay()] + ', ' + date.getDate() + '. ' + MONTH[date.getMonth()]);

			displayDate.append(displayDateWrapper);
            $('.ttDaysName').append(displayDate);
            
            date.setDate(date.getDate() + 1);
        }
    }

    function layoutDisplayDays (width, height, daysNumber)
    {
        if (daysNumber == undefined)
        {
            daysNumber = activeScreen.find('.ttTimetable').data('days');
        }

        var daysDisplayWidth = Math.floor(width * 0.96);

        if (!isCalendarViewActive)
        {
            daysDisplayWidth = width;
        }

        activeScreen.find('.ttDaysName').css(
        {
            'width' : daysDisplayWidth + 'px'
        });

        var dayDisplayWidth = daysDisplayWidth / daysNumber;

        var dayDisplayHeight = Math.floor(height * 0.04);

        if ($('.ttDays').height() > dayDisplayHeight)
        {
            dayDisplayHeight = $('.ttDays').height();
        }

        $('.ttDays').css(
        {
            'height' : dayDisplayHeight + 'px'
        });

        // margin-left is set to 5px therefor dayDisplayWidth has to be 5px
        // smaller
        $('.ttDay').css(
        {
            'width' : Math.floor(dayDisplayWidth - 2) + 'px'
        });

        if (!isCalendarViewActive)
        {
            $('.ttDay:first-child').css(
            {
                'width' : Math.floor(dayDisplayWidth) + 'px'
            });
        }

        if (isCalendarViewActive)
        {
            $('.ttDayTimeRect').css(
            {
                'width' : Math.round(width * 0.04) + 'px'
            });
        }

    }

    function createAndLayoutDisplayHours (width, height)
    {
        var ttTimetable = activeScreen.find('.ttTimetable');

        var daysNumber = ttTimetable.data('days');

        createDisplayHours(daysNumber, hoursStart, hoursAmount);
        layoutDisplayHours(width, height, daysNumber, hoursAmount);

    }

    function createDisplayHours (daysNumber, hoursStart, hoursAmount)
    {
        activeScreen.find('.ttTimes').html('');

        for (var i = 0; i < hoursAmount; i++)
        {
            var dayTime = $('<div/>',
            {
                'class' : 'ttDayTimes'
            });
            for (var j = 0; j < daysNumber; j++)
            {
                $('<div/>',
                {
                    'class' : 'ttDayTime'
                }).appendTo(dayTime);
            }

            var timeWithOffset = i + hoursStart;
            var time = $('<div class="ttTimeDisplay"><div class="ttTimeDisplayWrapper"><span class="ttTimeDisplayTextWrapper">' + timeWithOffset + '</span></div></div>');
            var ttTime = $('<div/>',
            {
                'class' : 'ttTime'
            });

            time.appendTo(ttTime);
            dayTime.appendTo(ttTime);

            ttTime.appendTo('.ttTimes');
        }

    }

    function layoutDisplayHours (width, height, daysNumber, hoursAmount)
    {
        activeScreen.find('.ttTimes').show();

        if (daysNumber == undefined || hoursAmount == undefined)
        {
            var ttTimetable = activeScreen.find('.ttTimetable');

            daysNumber = ttTimetable.data('days');
            hoursAmount = ttTimetable.data('hours');
        }

        var hoursDisplayWidth = activeScreen.find('.ttDayTimeRect').width();

        $('.ttTimeDisplay').css(
        {
            'width' : hoursDisplayWidth + 'px'
        });

        if ($('.ttTimeDisplayTextWrapper').last().width() > hoursDisplayWidth)
        {
            hoursDisplayWidth = $('.ttTimeDisplay').last().width() * 1.5;

            activeScreen.find('.ttDaysName').css(
            {
                'width' : (width - hoursDisplayWidth) + 'px'
            });

            var dayDisplayWidth = (width - hoursDisplayWidth) / daysNumber;

            // margin-left is set to 2px therefor dayDisplayWidth has to be 2px
            // smaller
            $('.ttDay').css(
            {
                'width' : Math.floor(dayDisplayWidth - 2) + 'px'
            });

            if (!isCalendarViewActive)
            {
                $('.ttDay:first-child').css(
                {
                    'width' : Math.floor(dayDisplayWidth) + 'px'
                });
            }

        }

        var hoursDisplayHeight = hoursDisplayWidth;
        if (calculateContentHeight() > (hoursDisplayHeight + 2) * hoursAmount)
        {
            hoursDisplayHeight = (calculateContentHeight() - (hoursAmount * 2)) / hoursAmount;
        }

        activeScreen.find('.ttTime').css(
        {
            'height' : hoursDisplayHeight + 'px'
        });

        activeScreen.find('.ttDayTimes').css(
        {
            'width' : activeScreen.find('.ttDaysName').width() + 'px'
        });

        activeScreen.find('.ttDayTime').css(
        {
            'width' : activeScreen.find('.ttDay').width() + 'px'
        });

        $('.ttDayTimeRect').css(
        {
            'width' : $('.ttDayTimes').position().left + 'px'
        });

        activeScreen.find('.ttTimeDisplay').css(
        {
            'width' : $('.ttDayTimes').position().left + 'px'
        });

        if (!isCalendarViewActive)
        {
            activeScreen.find('.ttTimes').hide();
        }
    }

    function layoutEvents ()
    {
        var marginLeft = $('.ttDayTimes').position().left;
        var marginTop = $('.ttDays').height();
        var width = $('.ttDayTimes').width();
        var height = calculateContentHeight() - marginTop;

        if (isCalendarViewActive)
        {
            marginLeft += 2;
            marginTop += 2;
            width -= 2;
            height = $('.ttTimes').height();
        }

        $('.ttEvents').css(
        {
            'margin-left' : marginLeft + 'px',
            'margin-top' : marginTop + 'px',
            'width' : width,
            'height' : height
        });

        var eventEnd = 0;

        multipleEventsSameTime = new Array ();

        $('.ttEvents').children().each(function ()
        {

            var duration = $(this).data('duration');
            var start = $(this).data('start');

            var eventPadding = 0;
            if (eventEnd == start)
            {
                eventPadding = 2;
            }

            var eventHeight = 'auto';
            var eventPositionTop = 'auto';
            var eventWidth = $(window).width();

            if (isCalendarViewActive)
            {
                eventHeight = (Math.round(($('.ttTime').outerHeight(true)) * duration) - eventPadding) + 'px';
                eventPositionTop = (Math.floor(($('.ttTime').outerHeight(true)) * start) + eventPadding) + 'px';
                eventWidth = $('.ttDay').width();
            }

            $(this).css(
            {
                'width' : eventWidth + 'px',
                'height' : eventHeight,
                'top' : eventPositionTop
            });

            if (eventEnd <= start)
            {
                multipleEventsSameTime.pop();
            }

            if ($(this).find('.multipleEventsSameTime').length > 0)
            {
                var multipleEventsSameTimeHeight = '100%'

                if (!isCalendarViewActive)
                {
                    multipleEventsSameTimeHeight = $(this).height() + 'px';
                }
                $(this).find('.multipleEventsSameTime').css(
                {
                    height : multipleEventsSameTimeHeight
                });
            }

            multipleEventsSameTime.push($(this));

            eventEnd = start + duration;
        });

        if (multipleEventsSameTime.length == 1)
        {
            multipleEventsSameTime = new Array ();
        }

        if (intervalWorkerProcess != null)
        {
            clearInterval(intervalWorkerProcess);
        }

        $('.ttEvents').children().show();

        $(multipleEventsSameTime).each(function (value)
        {
            if (isCalendarViewActive)
            {
                this.hide();
            }

            if (this.find('.multipleEventsSameTime').length == 0)
            {
                var div = '<div class="multipleEventsSameTime"><div class="multipleEventsSameTimeCell">!</div></div>';
                this.prepend(div);
            }
        });

        if (multipleEventsSameTime.length != 0 && isCalendarViewActive)
        {
            currentDisplayedEventSameTime = 0;

            intervalWorkerProcess = setInterval(function ()
            {
                animateEventsSameTime();
            }, 5000);
            multipleEventsSameTime[0].fadeIn('slow');
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

    function toggleEventsView ()
    {
        isCalendarViewActive = !isCalendarViewActive;

        toggleTimetableClasses();
        toggleToggleViewButtonClasses();

        $(window).trigger(ACTION_RE_LAYOUT);

    }

    function toggleTimetableClasses ()
    {
        $('.ttTimetable').removeClass('ttTimetableOff');

        if (!isCalendarViewActive)
        {
            $('.ttTimetable').addClass('ttTimetableOff');
        };
    }

    function toggleToggleViewButtonClasses ()
    {
        $('.toggleView .ui-icon').toggleClass('ui-icon-custom-list');
        $('.toggleView .ui-icon').toggleClass('ui-icon-custom-timetable');
    }

    function animateEventsSameTime ()
    {
        multipleEventsSameTime[currentDisplayedEventSameTime].fadeOut(
        {
            duration : 'slow',
            queue : true
        });

        currentDisplayedEventSameTime++;

        if (currentDisplayedEventSameTime > multipleEventsSameTime.length - 1)
        {
            currentDisplayedEventSameTime = 0;
        }

        multipleEventsSameTime[currentDisplayedEventSameTime].fadeIn(
        {
            duration : 'slow',
            queue : true
        });

    }

}