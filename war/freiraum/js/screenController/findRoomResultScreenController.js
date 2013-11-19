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
var findRoomResultScreenController = function (previousScreenDataRequestObject, activeScreen)
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

    var selectedRoom = null;
    var selectedRoomId = null;

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
        var data =
        {
            query : AJAX_QUERY_ROOM_DETAIL_INFO,
            name : selectedRoomId,
            elementclass : CLASS_ROOM_DETAIL
        };

        return data;
    }

    /**
     * Creates and layouts the screen. The data needed for this screen
     * is fetched asynchronously after all recognizers have been registered.
     */
    function createAndLayoutScreen ()
    {
        registerAjaxRequestRecognizer();
        registerUserActionRecognizer();
        layoutRoomDetailInfo();
        fetchData();
        
    }

    function reLayout ()
    {
        layoutRoomDetailInfo();
    }

    /**
     * Register the basic recognizers needed for the ajax request.
     * All request specific success recognizers are registered just in time.
     */
    function registerAjaxRequestRecognizer ()
    {
        $(window).one(ACTION_AJAX_ERROR_EVENT, requestFailedAction);
    }

    function registerUserActionRecognizer ()
    {
        activeScreen.find('.home').unbind('click');
        activeScreen.find('.home').bind('click', loadHomeScreenAction);
        
        activeScreen.find('.closePopup').unbind('click');
        activeScreen.find('.closePopup').bind('click', closePopup);

        activeScreen.find('.' + CLASS_FREE_ROOMS).unbind('click');
        activeScreen.find('.' + CLASS_FREE_ROOMS).bind('click', loadRoomDetailInfoAction)

        activeScreen.find('.ics').unbind('click');
        activeScreen.find('.ics').bind('click', getICS);
    }

    function fetchData ()
    {
        $(window).one(ACTION_AJAX_FIND_ROOM_RESULT, findRoomResultLoadedAction);
        var data = previousScreenDataRequestObject;
        sendAjaxRequest(data, LOAD_FIND_ROOM, ACTION_AJAX_FIND_ROOM_RESULT);
    }

    function fetchPopupData ()
    {
        $(window).one(ACTION_AJAX_GET_ROOM_DETAIL_INFO, getRoomDetailInfoLoadedAction);
        var data = getRequestDataObject();
        sendAjaxRequest(data, LOAD_DETAIL_INFO, ACTION_AJAX_GET_ROOM_DETAIL_INFO);
    }

    function requestFailedAction ()
    {
        activeScreen.find('.home').unbind('click');
    }

    function findRoomResultLoadedAction (event, data)
    {
        $('#listviewRoomFreeList').children().remove();
        $('#listviewRoomFreeList').append(data);
        $('#listviewRoomFreeList').listview('refresh');

        registerUserActionRecognizer();
    }

    function getRoomDetailInfoLoadedAction (event, data)
    {
        $('.roomDetailInfoHeader').html('');
        $('.roomDetailInfoHeader').append(selectedRoom);
        
        $('.roomDetailInfoTable').children().remove();
        $('.roomDetailInfoTable').append(data);
        
        reLayout();
    }

    function loadHomeScreenAction ()
    {
        $.mobile.changePage($('#homeScreen'),
        {
            transition : 'fade'
        });
    }

    function loadRoomDetailInfoAction ()
    {
        selectedRoom = $(this).data('room');
        selectedRoomId = $(this).data('room-id');

        fetchPopupData();
    }

    function layoutRoomDetailInfo ()
    {
        var roomDetailInfoPopup = activeScreen.find('#roomDetailInfo');

        var popupHeight = Math.floor($(window).height() / 2);

        var popupWidth = popupHeight;

        roomDetailInfoPopup.height(popupHeight);
        roomDetailInfoPopup.css('min-height', activeScreen.find('.roomDetailInfoTable').outerHeight(true) + activeScreen.find('#roomDetailInfo .header').outerHeight(true) + "px");

        if (popupWidth > $(window).width())
        {
            popupWidth = $(window).width() * 0.9;
        }
        roomDetailInfoPopup.width(popupWidth + "px");
        roomDetailInfoPopup.css('min-width', activeScreen.find('.roomDetailInfoTable').outerWidth(true) + "px");
        
        var headerHeight = roomDetailInfoPopup.find(".header").outerHeight(true);
        
        roomDetailInfoPopup.find(".contentArea").height((popupHeight - headerHeight) + "px");
        
        roomDetailInfoPopup.popup("reposition");
    }

    function getICS ()
    {
        var url = buildURL();
        
        downloadWindow = window.open(url);
    }

    function buildURL ()
    {
        var url = 'php/createCalendarEvent.php?starttime=' + previousScreenDataRequestObject['starttime'] + '&endtime=' + previousScreenDataRequestObject['endtime'] + '&location=' + selectedRoom;
        
        return url;
    }
    
    function closePopup ()
    {
        $('.roomDetailInfoHeader').html('');
    }

}