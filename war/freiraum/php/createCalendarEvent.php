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
    //This is the most important coding.
header("Content-Type: text/Calendar");
header("Content-Disposition: inline; filename=freiraumEvent.ics");

$ts_begin = $_GET["starttime"];
$ts_end = $_GET["endtime"];
$location = $_GET["location"];

$dateBegin = date("Ymd",$ts_begin);  
$timeBegin = date("Gis",$ts_begin);  

$dateEnd = date("Ymd",$ts_end);  
$timeEnd = date("Gis",$ts_end);  

$dateCreated = date("Ymd",time());  
$timeCreated = date("Gis",time());  

echo "BEGIN:VCALENDAR\n
VERSION:2.0\n
METHOD:PUBLISH\n
BEGIN:VEVENT\n
CLASS:PUBLIC\n
CREATED:".$dateCreated."T".$timeCreated."\n

DESCRIPTION:Das ist keine Buchung des Termins
DTEND:".$dateBegin."T".$timeBegin."\n
DTSTART:".$dateEnd."T".$timeEnd."\n
LOCATION:".$location."\n
SUMMARY;LANGUAGE=de-de:freiraum - freie Raum suche\n
TRANSP:OPAQUE\n
//Here is to set the reminder for the event.
END:VEVENT\n
END:VCALENDAR\n";

?>