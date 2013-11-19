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

/** @const */ APP_NAME = 'Freiraum';

/******************************************************************************************************
 *
 * URL: All URLs that are used within freiraum
 *
 *****************************************************************************************************/

/** @const */ URL_PUSH_SERVICE = '../rapla/freiraum-push';
/** @const */ URL_LONG_POLLING_SERVICE = '../rapla/freiraum-poll';
/** @const */ URL_AJAX_REQUEST = '../rapla/freiraum-ajax';

/** @const  URL_PUSH_SERVICE = 'push/client_pusher.php';*/
/** @const  URL_LONG_POLLING_SERVICE = 'push/client_poller.php'; */
/** @const  URL_AJAX_REQUEST = 'php/ajaxresponse.php'; */


/******************************************************************************************************
 *
 * PUSH-Messages: Every push that is send to the client receives a unique message,
 * indicating whether the data changed or not
 *
 *****************************************************************************************************/

/** @const */ PUSH_DATA_UPDATED = 'update';

/******************************************************************************************************
 *
 * Action-names: All action-names that are defined.
 *
 *****************************************************************************************************/

/** @const */ ACTION_AJAX_ERROR_EVENT = 'ajaxError';

/** @const */ ACTION_AJAX_COURSE_LIST = 'courseListSuccess';
/** @const */ ACTION_AJAX_ELEMENTS_LIST = 'elementListSuccess';
/** @const */ ACTION_AJAX_ROOM_LIST = 'roomListSuccess';
/** @const */ ACTION_AJAX_TIMETABLE = 'timetableSuccess';
/** @const */ ACTION_AJAX_EVENT = 'eventSuccess';
/** @const */ ACTION_AJAX_CALENDAR = 'calendarSuccess';
/** @const */ ACTION_AJAX_FIND_ROOM_RESULT = 'findRoomResultSuccess';
/** @const */ ACTION_AJAX_GET_ROOM_DETAIL_INFO = 'getRoomDetailInfoSuccess';
/** @const */ ACTION_AJAX_ROOM_CATEGORIES = 'getRoomCategoriesSuccess';

/** @const */ ACTION_PUSH_RECEIVED = 'pushReceived';
/** @const */ ACTION_RE_LAYOUT = 'reLayout';
/******************************************************************************************************
 *
 * AJAX constants: These constants are used for the dynamic ajax requests
 *
 *****************************************************************************************************/

/** @const */ AJAX_QUERY_TIMETABLE_COURSE = 'course';
/** @const */ AJAX_QUERY_TIMETABLE_PROF = 'professor';
/** @const */ AJAX_QUERY_TIMETABLE_ROOM = 'room';
/** @const */ AJAX_QUERY_ROOM_FREE = 'roomfree';
/** @const */ AJAX_QUERY_ROOM_CATEGORIES = 'roomCategories';
/** @const */ AJAX_QUERY_ROOM_DETAIL_INFO = 'roomdetail';


/******************************************************************************************************
 *
 * Class names: These classes are applied on asynchronously fetched data
 * 
 *****************************************************************************************************/

/** @const */ CLASS_COURSE = 'course';
/** @const */ CLASS_ELEMENTS = 'elements';
/** @const */ CLASS_ROOM = 'room';
/** @const */ CLASS_EVENT = 'ttEvent';
/** @const */ CLASS_FREE_ROOMS = 'freeRooms';
/** @const */ CLASS_ROOM_DETAIL = 'roomDetailInfoDescr';

/******************************************************************************************************
 *
 * Switch toggles: All following constants are used to identify the appropriate case.
 *
 *****************************************************************************************************/

/** @const */ CASE_MINUTES = 'MINUTES';
/** @const */ CASE_HOURS = 'HOURS';
/** @const */ CASE_DAY = 'DAYS';
/** @const */ CASE_MONTH = 'MONTHS';
/** @const */ CASE_YEAR = 'YEARS';


/******************************************************************************************************
 *
 * Text messages: These text messages are used for the loading indicators and all error messages
 *
 *****************************************************************************************************/

/** @const */ LOAD_COURSE_LIST = 'Kursliste laden';
/** @const */ LOAD_PROF_LIST = 'Professorenliste laden';
/** @const */ LOAD_ROOM_LIST = 'Raumliste laden';
/** @const */ LOAD_TIMETABLE = 'Kalender laden';
/** @const */ LOAD_EVENTS = 'Events laden';
/** @const */ LOAD_FIND_ROOM = 'Räume werden gesucht';
/** @const */ LOAD_DETAIL_INFO = 'Detailinformation laden';
/** @const */ LOAD_ROOM_CATEGORIES = 'Raumkategorien laden';


/** @const */ WEEKDAY_SHORT = new Array('So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa');
/** @const */ WEEKDAY = new Array( 'Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag');
/** @const */ MONTH_SHORT = new Array('Jan', 'Feb', 'Mar', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez');
/** @const */ MONTH = new Array('Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember');
