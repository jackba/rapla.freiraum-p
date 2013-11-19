<?php
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
$ini = parse_ini_file(realpath(__DIR__."/../config/constants.ini"));
if($ini === false){echo "Fehler beim parsen der .ini-Datei :( ";}
//Date and Time Constants
define("SECS_PER_DAY", 86400);


//DBKEY Constants 
define("DBKEY_ROOM", $ini["DBKEY_ROOM"]);
define("DBKEY_ROOM_NAME", $ini["DBKEY_ROOM_NAME"]);


define("DBKEY_COURSE", $ini["DBKEY_COURSE"]);
define("DBKEY_COURSE_NAME", $ini["DBKEY_COURSE_NAME"]);

define("DBKEY_DEVICE", $ini["DBKEY_DEVICE"]);
define("DBKEY_DEVICE_NAME", $ini["DBKEY_DEVICE_NAME"]);

define("DBKEY_DHBW_PROFESSOR", $ini["DBKEY_DHBW_PROFESSOR"]);
define("DBKEY_DHBW_PROFESSOR_SURNAME", $ini["DBKEY_DHBW_PROFESSOR_SURNAME"]);
define("DBKEY_DHBW_PROFESSOR_FORENAME", $ini["DBKEY_DHBW_PROFESSOR_FORENAME"]);
define("DBKEY_DHBW_PROFESSOR_TITLE", $ini["DBKEY_DHBW_PROFESSOR_TITLE"]);


define("DBKEY_EXTERNAL_PROFESSOR", $ini["DBKEY_EXTERNAL_PROFESSOR"]);
define("DBKEY_EXTERNAL_PROFESSOR_SURNAME", $ini["DBKEY_EXTERNAL_PROFESSOR_SURNAME"]);
define("DBKEY_EXTERNAL_PROFESSOR_FORENAME", $ini["DBKEY_EXTERNAL_PROFESSOR_FORENAME"]);
define("DBKEY_EXTERNAL_PROFESSOR_TITLE", $ini["DBKEY_EXTERNAL_PROFESSOR_TITLE"]);

define("DBKEY_DEFAULT_RESERVATION",$ini["DBKEY_DEFAULT_RESERVATION"]);

define("DBKEY_CATEGORY_ROOMS",$ini["DBKEY_CATEGORY_ROOMS"]);


define("DBCOL_RESOURCE_ATTRIBUTE_VALUE",$ini["DBCOL_RESOURCE_ATTRIBUTE_VALUE"]);


define("MENUE_PROFESSOR_VISIBLE", $ini["MENUE_PROFESSOR_VISIBLE"]);
define("MENUE_BUG_VISIBLE", $ini["MENUE_BUG_VISIBLE"]);
define("MENUE_ROOMROW_SINGLE", $ini["MENUE_ROOMROW_SINGLE"]);



define("CALENDAR_DAYS_FUTURE", $ini["CALENDAR_DAYS_FUTURE"]);
define("CALENDAR_DAYS_PAST", $ini["CALENDAR_DAYS_PAST"]);
define("CALENDAR_SHOW_PROFESSOR_NAMES", $ini["CALENDAR_SHOW_PROFESSOR_NAMES"]);

define("CALENDAR_TIME_WINDOW_START", $ini["CALENDAR_TIME_WINDOW_START"]);
define("CALENDAR_TIME_WINDOW_HOURS", $ini["CALENDAR_TIME_WINDOW_HOURS"]);


 
define("ROOMDETAILS_ATTRIBUTE_LIST", $ini["ROOMDETAILS_ATTRIBUTE_LIST"]);
 
define("JSON_ATTRIBUTE_DATA", $ini["JSON_ATTRIBUTE_DATA"]);
define("JSON_ATTRIBUTE_ERROR", $ini["JSON_ATTRIBUTE_ERROR"]);

define("DB_LOGIN_DATABASE",$ini["DB_LOGIN_DATABASE"]);
define("DB_LOGIN_USER",$ini["DB_LOGIN_USER"]);
define("DB_LOGIN_PW",$ini["DB_LOGIN_PW"]);

define("ENVIRONMENT_PATH_ZEND_FRAMEWORK",$ini["ENVIRONMENT_PATH_ZEND_FRAMEWORK"]);
?>