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
require_once(realpath(__DIR__."/sql.php"));
/**
 * Diese Funktion transformiert die Daten, die von der Funktion getAppointmentByResource erstellt wurden,
 * in eine Datenstruktur, die auf der Oberfläche geparsed und angezeigt werden kann.
 */
function transformAppointmentsToHtml($result,$exclude_id,$class,$tag_ende,$tag_offset){
	$data = "";
	foreach ($result as $values) {
	$appointment_id = $values["ID"];
	
	$output = getEventOfAppointment($appointment_id);
	$output .= getRessourcesOfAppointment($appointment_id,$exclude_id);
	$duration = ($values["end_ts"] - $values["begin_ts"])/3600;			
	//Achtung: begin_ts ist der timestamp des ersten TERMINS! Wir erhalten also in der Differenz mit dem Anfragedatum mehrere Tage Unterschied
	//AUSSERDEM: Zeitzonen-Problem!!!!
		//Erster Schritt: Berechne die Anzahl der Tage die zwischen dem ersten Serientermin und heute liegt:
		$anzahlTage = floor( ($tag_ende-$values["begin_ts"]) / SECS_PER_DAY);
		//Zweiter Schritt: Prüfe die Gleichheit der Zeitzonen!				
		$objDateTimeZone = timezone_open(date_default_timezone_get());
		$date = date("Y-m-d h:i:s", $values["begin_ts"]);
		
		$timezoneDiffOriginal = $strZeitzoneUnterschiedZuGMT =  timezone_offset_get($objDateTimeZone, date_create($date, $objDateTimeZone));
		$timezoneDiffCurrent = $strZeitzoneUnterschiedZuGMT =  timezone_offset_get($objDateTimeZone, date_create("now", $objDateTimeZone));
	//echo "begin_ts:".$values["begin_ts"]."anzahlTage:".$anzahlTage."tag_offset:".$tag_offset."tag_ende:".$tag_ende;
	$start = ((( $values["begin_ts"]+($anzahlTage*SECS_PER_DAY)) - $tag_offset)/3600)+($timezoneDiffOriginal-$timezoneDiffCurrent)/3600; 
	
	$data .= "<li class='".$class."' data-start='" . $start . "' data-duration='" . $duration . "'><ul class='ttEventDetails'>" . $output . "</ul></li>";
	}
	return $data;
}
		


 /**
 * Diese Fuktion ermittelt die ID einer Resource, abhängig von Ihrem Namen und den beiden TYPE/KEY Werten
 */
function getResourceIdByName($name_appointment, $attribute_key,$type_key){
	global $dbh;
	$queryexec = "select RESOURCE_ID from rapla_resource rr inner join resource_attribute_value ra on";
	$queryexec .= " rr.ID = ra.RESOURCE_ID";
	$queryexec .= " Where ra.ATTRIBUTE_KEY = :attribute_key and ".DBCOL_RESOURCE_ATTRIBUTE_VALUE." = :name and rr.TYPE_KEY = :type_key";
	
	$stmt = $dbh->prepare($queryexec);
	$stmt->bindParam(":name",$name_appointment);
	$stmt->bindParam(":attribute_key",$attribute_key);
	$stmt->bindParam(":type_key",$type_key);
	$stmt->execute();
			
	if ($GLOBALS["debug"] == 1) echo "<br><br>".$queryexec;
	
	$row = $stmt->fetch(PDO::FETCH_BOTH);
	$stmt = null;
	return $row["RESOURCE_ID"];
}


/**
 * Diese Funktion ermittelt alle detaillierten Daten zu einem Appointment (Professor, Raum,...)
 * und formatiert diese entsprechend
 */
function getRessourcesOfAppointment($id_appointment, $id_exclude){
	global $dbh;
	//Prüfe Übergabeparameter: beide müssen mit einem Wert belegt sein
	if(!isset($id_appointment) || $id_appointment == ""){
		throw new Exception("Der Übergabeparameter Appointment-ID ist leer!");
	}
	if(!isset($id_exclude) || $id_exclude == ""){
		throw new Exception("Der Übergabeparameter Ausschluss-ID ist leer!");
	}
	//initialisiere Werte
	$name = $titel = $raumtyp = $raumname = $rooms = $titeltermin = $vorname = $output = $begin = $end = "";
	//################################################################
	//zu jedem Appointment, das den Termin darstellt gibt es X ressourcen (Dozent, Raum, ...)
	//################################################################
	
	//hole alle Ressourcen, die von dem Termin benötigt werden,aus der Kreuzungstabelle, wo die Resource-ID nicht die ID des Kurses ist
	$queryexec = "select * from allocation where APPOINTMENT_ID= :id_appointment and RESOURCE_ID != :id_resource";
	$stmt = $dbh->prepare($queryexec);
	$stmt->bindParam(':id_appointment', $id_appointment);
	$stmt->bindParam(':id_resource', $id_exclude);
	$stmt->execute();

	if ($GLOBALS["debug"] == 1) echo "<br><br>".$queryexec;
	

	 while ($row = $stmt->fetch(PDO::FETCH_BOTH)) {
		//hole alle daten zur jeweiligen ressource (Professor, Raum, ...)
		//Die Spalte, die mit der Konstante DBCOL_RESOURCE_ATTRIBUTE_VALUE refenziert wird, wird im select statement umbenannt. Dies ist nötig, um sie danach
		//einheitlich über $row["value"] abfragen zu können.
		$queryexec = "select TYPE_KEY,ATTRIBUTE_KEY,ra.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." as VALUE from rapla_resource rr right outer join resource_attribute_value ra on";
		$queryexec .= " rr.ID = ra.RESOURCE_ID";
		$queryexec .= " Where rr.ID = :id_resource " ;
		
		$stmt2 = $dbh->prepare($queryexec);
		$stmt2->bindParam(':id_resource',$row["RESOURCE_ID"]);
		$stmt2->execute();

		if ($GLOBALS["debug"] == 1) echo "<br><br>".$queryexec;
		$output = "";

		while ($row2 = $stmt2->fetch(PDO::FETCH_BOTH)){
			//####################################################################
			//Problematisch ist, dass eine Resource komplett generisch ist.
			//Dozent hat TYPE KEY: surname, forename, titel
			//Raum hat TYPE KEY: a1 (=typ), name
			//####################################################################
			$tk = $row2["TYPE_KEY"];
			$ak = $row2["ATTRIBUTE_KEY"];
			if (CALENDAR_SHOW_PROFESSOR_NAMES == "true" && ($tk == DBKEY_DHBW_PROFESSOR || $tk == DBKEY_EXTERNAL_PROFESSOR )) {
					if ($ak == DBKEY_DHBW_PROFESSOR_SURNAME || $ak == DBKEY_EXTERNAL_PROFESSOR_SURNAME ) {$name = $row2["VALUE"] . " ";}
					if ($ak == DBKEY_DHBW_PROFESSOR_FORENAME || $ak ==  DBKEY_EXTERNAL_PROFESSOR_FORENAME ) {$vorname = $row2["VALUE"] . " ";	}
					if ($ak == DBKEY_DHBW_PROFESSOR_TITLE || $ak == DBKEY_EXTERNAL_PROFESSOR_TITLE ) {$titel = $row2["VALUE"] . " ";	}
			}
			else if ($tk == DBKEY_ROOM || $tk == DBKEY_COURSE || $tk == DBKEY_DEVICE){
					if ($ak == DBKEY_ROOM_NAME || $ak == DBKEY_COURSE_NAME || $ak == DBKEY_DEVICE_NAME) {$raumname.= $row2["VALUE"] . " ";}
			}				
		}
		$stmt2 = null;
		//Werte zusammensetzen, die in der oberen while-Schleife gesetzt wurden (jeder Wert eine Variable)
		//muss in der while Schleife passieren, da es mehrere Räume geben kann
		if($raumtyp != "" || $raumname != "")$rooms .= "<li class='eventInfo'>".$raumtyp . $raumname."</li>";
		$raumtyp = "";
		$raumname = "";
	}
	$stmt = null;

	//Werte aller Resourcen EINES Termines zusammensetzen (Raum,Professor,...)
	
	if($titeltermin != "") $output .= "<li class='eventInfo'>".$titeltermin . "</li>";
	$output .= $rooms;
	if ($titel != "" || $vorname != "" || $name != ""){$output .= "<li class='eventInfo'>".$titel . $vorname . $name ."</li>";}
	
	
	return $output;
}

/**
 * Diese Funktion ermittelt alle detailierten Daten zu einem Event eines Appointments wie Pause, Name der Vorlesung, Art der Klausur,...
 */
function getEventOfAppointment($id_appointment){
	global $dbh;
	//Prüfe Übergabeparameter: Muss mit einem Wert belegt sein
	if(!isset($id_appointment) || $id_appointment == ""){
		throw new Exception("Der Übergabeparameter Appointment-ID ist leer!");
	}
	$name = $title = "";
	
	//selektiere das event, das zum Appointment gehört
	$queryexec = "select * from appointment where ID = :id_appointment";
	
	$stmt = $dbh->prepare($queryexec);
	$stmt->bindParam(":id_appointment",$id_appointment);
	$stmt->execute();
	while ($row = $stmt->fetch(PDO::FETCH_BOTH)){
	$event_id = $row["EVENT_ID"];	 
	}
	$stmt = null;
	
	//uhrzeiten des appointments auslesen und anhängen
	$queryexec = "select *,DATE_FORMAT(APPOINTMENT_START, '%H:%i') as begin, DATE_FORMAT(APPOINTMENT_END, '%H:%i') as end from appointment where ID= :id_appointment";

	$stmt = $dbh->prepare($queryexec);
	$stmt->bindParam(":id_appointment",$id_appointment);
	$stmt->execute();
	
	while($row = $stmt->fetch(PDO::FETCH_BOTH)){
	$begin = $row["begin"];
	$end = $row["end"];	
	}
	$stmt = null;
		
	if ($GLOBALS["debug"] == 1) echo "<br><br>".$queryexec;

	//selektiere alle werte zum event, die benötigt werden (Titel der Vorlesung, Pause, Prüfungsart,... )
	//Die Spalte, die mit der Konstante DBCOL_RESOURCE_ATTRIBUTE_VALUE refenziert wird, wird im select statement umbenannt. Dies ist nötig, um sie danach
	//einheitlich über $row["value"] abfragen zu können.
	$queryexec = "select TYPE_KEY,ATTRIBUTE_KEY,eav.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." as VALUE from event e right outer join event_attribute_value eav on e.id = eav.EVENT_ID where e.ID= :id_event";

	$stmt=$dbh->prepare($queryexec);
	$stmt->bindParam(":id_event",$event_id);
	$stmt->execute();
	
	
	if ($GLOBALS["debug"] == 1) echo "<br><br>".$queryexec;
	$output = "";
	
	while($row = $stmt->fetch(PDO::FETCH_BOTH)){
		
		$tk = $row["TYPE_KEY"];
			if ($tk == DBKEY_DEFAULT_RESERVATION ){
					if ($row["ATTRIBUTE_KEY"] == "name") {$name = $row["VALUE"] . " ";}
			}
			//Für jeden Studiengang gibt es eigenen reservation typen (reservation1-28) mit dem ATT_KEY title
			else if (preg_match("/reservation([0-9]||[0-9][0-9])/i", $tk) ){
				if ($row["ATTRIBUTE_KEY"] == "title") {$title = $row["VALUE"] . " ";}
			}
			else if($tk == DBKEY_COURSE){
				if ($row["ATTRIBUTE_KEY"] == DBKEY_COURSE_NAME){$name = $row["VALUE"]." ";}
			}
	}
	$stmt = null;
	//vorsichtig: falls beide Werte gefüllt sind, gibt es Unsinn zurück	
	$output .= "<li class='eventTime'>".$begin." - ".$end. "</li>";
	if ($name != "") $output.="<li class='eventTitle'>".$name."</li>";
	if ($title != "") $output.="<li class='eventTitle'>".$title."</li>";
	return $output;	
}

/**
 * Diese Funktion ermittelt alle Termine zu einer bestimmten Resource, wie Raum,Kurs,Dozent,...
 * Dabei werden die Serientermine von RAPLA berücksichtigt (weekly,daily)
 */
function getAppointmentByResource($start,$ende,$id_resources){
	global $dbh;
	$listappointments = "";	
	//Prüfe Übergabeparameter: Muss mit einem Wert belegt sein
	if(!isset($id_resources) || $id_resources == ""){
		throw new Exception("Der Übergabeparameter Ressourcen-ID ist leer!");
	}
	
	$listappointments .= getAppointmentsWithoutRepetition($start,$ende,$id_resources);
	
	$listappointments .= getWeeklyAppointmentsWithEnd($start,$ende,$id_resources);
	
	$listappointments .= getWeeklyAppointmentsWithoutEnd($start,$ende,$id_resources);
	
	$listappointments .= getDailyAppointments($start,$ende,$id_resources);


	$listappointments = substr($listappointments, 0, -1);
	
	if (!isset($listappointments) || $listappointments == ""){
		$listappointments = "0";
	}
	
	$queryexec = "select *, unix_timestamp(APPOINTMENT_START) as begin_ts, unix_timestamp(APPOINTMENT_END) as end_ts 
	from appointment where id in($listappointments) order by hour(APPOINTMENT_START),minute(APPOINTMENT_START) asc";
	
	$stmt = $dbh->prepare($queryexec);
	$stmt->execute();
	if ($GLOBALS["debug"] == 1) echo "<br><br>".$queryexec;
	$result = $stmt->fetchAll();
	$stmt = null;
	return $result;
}


function createJson($arr){
	$obj = new stdClass();
	//ermittle alle keys des assoziativen Arrays		
	$keys = array_keys($arr);
	
	//für jeden key wird der key und der value des keys in das json obejct geschrieben
	for($i = 0; $i<sizeof($keys); $i++){
		$key = $keys[$i];
		if ($arr[$key] == null){
		$obj->$key = "";			
		}else{
			$obj->$key = $arr[$key];
		}
	}
	
	$output = json_encode($obj);
	
	return $output;	
}

function getAppointmentsWithoutRepetition($start,$ende,$listrooms){
	global $dbh;
	//################################################################################
	//Selektiere alle Appointments, !die gerade aktiv sind! mit Wiederholungstyp NULL und hänge sie an die Liste
	//################################################################################
	$queryexec = "select *, hour(ap.APPOINTMENT_START) as begin_h, hour(ap.APPOINTMENT_END) as end_h,";
	$queryexec .= " unix_timestamp(ap.APPOINTMENT_START) as begin_ts, unix_timestamp(ap.APPOINTMENT_END) as end_ts ";	
	$queryexec .= " from allocation al inner join appointment ap on";
	$queryexec .= " al.APPOINTMENT_ID = ap.ID ";
	$queryexec .= " where al.RESOURCE_ID IN($listrooms)";
	$queryexec .= " and REPETITION_TYPE is NULL ";
	
	$queryexec .= " and 
	
	(
		(
		unix_timestamp(ap.APPOINTMENT_START) >= :start and unix_timestamp(ap.APPOINTMENT_START) <= :ende
		)
		or
		(
		unix_timestamp(ap.APPOINTMENT_END) > :start  and unix_timestamp(ap.APPOINTMENT_END) <= :ende
		)
		or
		(
		unix_timestamp(ap.APPOINTMENT_START) <= :start  and unix_timestamp(ap.APPOINTMENT_END) >= :ende
		)
	)";
		
	if ($GLOBALS["debug"] == 1) echo "<br><br>".$queryexec;
		
	$stmt = $dbh->prepare($queryexec);
	$stmt->bindParam(":start",$start);
	$stmt->bindParam(":ende",$ende);
	$stmt->execute();
	
	$listappointments = "";
	while($row = $stmt->fetch(PDO::FETCH_BOTH)){
		//Hier muss ID angehängt werden, da durch den Join mit der Exception Tabelle die Spalte APOINTMENT_ID mit NULL überschrieben wird und nur ID weiterhin eindeutig ist
		$listappointments .= $row["ID"].",";
	}
	$stmt = null;
	
	return $listappointments;	
}

/**
 * Selektion für weekly und repetition end is not null
 */
function getWeeklyAppointmentsWithEnd($start,$ende,$listrooms){
	global $dbh;
	//################################################################################
	// Mann müsste noch auseinander steuern, was es für wiederholungsarten sind: unendlich wiederholung: dann ists immer dabei
	// * ODER endliche Wiederholung, dann muss REPETITION_END aber größer als $ende sein!
	//Selektiere alle Appointments !die gerade aktiv sind! mit Wiederholungstyp Weekly und hänge sie an die Liste. 
	//!!!!!!!!!!!!!!!! 1. Unterscheidung: Nimm nur Termine mit unendlicher Wiederholung! (s. ganz unten) !!!!!!!!!!!!!!!!!!
	//################################################################################
	$queryexec = "SELECT *, hour(ap.APPOINTMENT_START) as begin_h, hour(ap.APPOINTMENT_END) as end_h, minute(ap.APPOINTMENT_START), 
	minute(from_unixtime(:ende)),hour(from_unixtime(:start))";
	$queryexec .= " FROM allocation al";
	$queryexec .= " INNER JOIN appointment ap ON al.APPOINTMENT_ID = ap.ID ";
	//prüft gleich noch mit, ob für den anfragetag eine exception gepflegt ist
	$queryexec .= " left outer join appointment_exception ae on";
	$queryexec .= " (ae.APPOINTMENT_ID = ap.ID and year(EXCEPTION_DATE) = year(FROM_UNIXTIME(:start))";
	$queryexec .= " and month(EXCEPTION_DATE) = month(FROM_UNIXTIME(:start)) and day(EXCEPTION_DATE) = day(FROM_UNIXTIME(:start)))";
	$queryexec .= " WHERE al.RESOURCE_ID IN($listrooms)";
	$queryexec .= " and UNIX_TIMESTAMP( FROM_UNIXTIME( UNIX_TIMESTAMP(  ap.APPOINTMENT_START ) ,  '%Y-%m-%d 0:0:0' ) ) <= (:start)";
	//Zum Glück gibts Serientermine ohne Ende -> REPETITION_END ist dann NULL.... HIER muss es jetzt mit rein, da es immer NOT NULL ist!!!!
	 $queryexec .= " and UNIX_TIMESTAMP( FROM_UNIXTIME( UNIX_TIMESTAMP(  ap.REPETITION_END ) ,  '%Y-%m-%d 0:0:0' ) ) >= :ende";
	$queryexec .= " AND dayname( appointment_start ) = dayname(date(FROM_UNIXTIME(:start)))"; 
	//nochmal lustig: Da man sich nicht auf REPETITION END verlassen kann, da es NULL sein kann, muss man das Ende selbst berechnen.
	//Ansonsten werden hier alle Serientermine gefunden, die jemals angelegt wurden (ca 300, auch von 2009)
	// $queryexec .= " AND (unix_timestamp(APPOINTMENT_START)+(REPETITION_NUMBER*REPETITION_INTERVAL*604800)) > ".$ende;

	//appointment start muss innerhalb der zeitspanne liegen:
	//1.1) entweder die stunde ist größer, also 12 >10 oder
	//1.2) die stunde ist gleich wie der anfang, aber die minute des starts ist größer als der anfragestart 40>30
	
	//UND zusätzlich!!!!! muss der Start auch kleiner sein als das ende, da der termin sonst anfängt, wenn der suchzeitraum vorbei ist
	//2.1) entweder die stunde von ap.Start ist kleiner als die stunde des endes 11 < 13 oder
	//2.2) die stunde von ap ende ist gleich wie die stunde von zeitraum ende aber die minute von ap.start ist kleiner/gleich als die minute von zeitraum ende
	$queryexec .= " and (
	(
		(
			hour(ap.APPOINTMENT_START) > hour(from_unixtime(:start))	
			or
			(
				hour(ap.APPOINTMENT_START) = hour(from_unixtime(:start)) and minute(ap.APPOINTMENT_START) >= minute(from_unixtime(:start))
			)
		)
		and
		(	hour(ap.APPOINTMENT_START) < hour(from_unixtime(:ende))
			or
			(
				hour(ap.APPOINTMENT_START) = hour(from_unixtime(:ende)) and minute(ap.APPOINTMENT_START) <= minute(from_unixtime(:ende))
			)
		)
	)
	or(
		(	
			hour(ap.APPOINTMENT_END) > hour(from_unixtime(:start))	
			or
			(
				hour(ap.APPOINTMENT_END) = hour(from_unixtime(:start)) and minute(ap.APPOINTMENT_END) > minute(from_unixtime(:start))
			)
		)
		and
		(
			hour(ap.APPOINTMENT_END) < hour(from_unixtime(:ende))
			or
			(
				hour(ap.APPOINTMENT_END) = hour(from_unixtime(:ende)) and minute(ap.APPOINTMENT_END) <= minute(from_unixtime(:ende))
			)
		)
	)
	or(
		(
			hour(ap.APPOINTMENT_START) < hour(from_unixtime(:start))
			or
			(
				hour(ap.APPOINTMENT_START) = hour(from_unixtime(:start)) and minute(ap.APPOINTMENT_START) <= minute(from_unixtime(:start)) 
			)
		)
		and
		(
			hour(ap.APPOINTMENT_END) > hour(from_unixtime(:ende))
			or
			(
			hour(ap.APPOINTMENT_END) = hour(from_unixtime(:ende)) and minute(ap.APPOINTMENT_END) >= minute(from_unixtime(:ende))
			)
		)
	)
	)";
	//die Wiederholungsart weekly setzt voraus, dass der Anfangstag des Serientermins gleich ist,
	//wie der Tag der Anfrage (Serie beginnt jede Woche Montags -> Treffer nur, wenn Montags angefragt wird)
	$queryexec .= " AND REPETITION_TYPE = 'weekly' and REPETITION_END IS NOT NULL"; 
	
	$stmt = $dbh->prepare($queryexec);
	$stmt->bindParam(":ende",$ende);
	$stmt->bindParam(":start",$start);
	$stmt->execute();
	
	if ($GLOBALS["debug"] == 1) echo "<br><br>".$queryexec;
	
	$listappointments = "";
	while($row = $stmt->fetch(PDO::FETCH_BOTH)){
	// while ($row = mysql_fetch_array($result)){
		//Füge ID des Appointments hinzu, wenn es keine Ausnahme gibt (NULL ist)
		if(is_null($row["EXCEPTION_DATE"])){
			//Hier muss ID angehängt werden, da durch den Join mit der Exception Tabelle die Spalte APOINTMENT_ID mit NULL überschrieben wird und nur ID weiterhin eindeutig ist
			$listappointments .= $row["ID"].",";
		}
	}
	$stmt = null;
	
	return $listappointments;	
}




/**
 * Selektion für weekly und repetition end IS NULL
 */
function getWeeklyAppointmentsWithoutEnd($start,$ende,$listrooms){
	global $dbh;
	//################################################################################
	// Mann müsste noch auseinander steuern, was es für wiederholungsarten sind: unendlich wiederholung: dann ists immer dabei
	// * ODER endliche Wiederholung, dann muss REPETITION_END aber größer als $ende sein!
	//Selektiere alle Appointments !die gerade aktiv sind! mit Wiederholungstyp Weekly und hänge sie an die Liste. 
	//!!!!!!!!!!!!!!!! 1. Unterscheidung: Nimm nur Termine mit unendlicher Wiederholung! (s. ganz unten) !!!!!!!!!!!!!!!!!!
	//################################################################################
	$queryexec = "SELECT *, hour(ap.APPOINTMENT_START) as begin_h, hour(ap.APPOINTMENT_END) as end_h, minute(ap.APPOINTMENT_START), 
	minute(from_unixtime(:ende)),hour(from_unixtime(:start))";
	$queryexec .= " FROM allocation al";
	$queryexec .= " INNER JOIN appointment ap ON al.APPOINTMENT_ID = ap.ID ";
	//prüft gleich noch mit, ob für den anfragetag eine exception gepflegt ist
	$queryexec .= " left outer join appointment_exception ae on";
	$queryexec .= " (ae.APPOINTMENT_ID = ap.ID and year(EXCEPTION_DATE) = year(FROM_UNIXTIME(:start))";
	$queryexec .= " and month(EXCEPTION_DATE) = month(FROM_UNIXTIME(:start)) and day(EXCEPTION_DATE) = day(FROM_UNIXTIME(:start)))";
	$queryexec .= " WHERE al.RESOURCE_ID IN($listrooms)";
	$queryexec .= " and UNIX_TIMESTAMP( FROM_UNIXTIME( UNIX_TIMESTAMP(  ap.APPOINTMENT_START ) ,  '%Y-%m-%d 0:0:0' ) ) <= (:start)";
	$queryexec .= " AND dayname( appointment_start ) = dayname(date(FROM_UNIXTIME(:start)))"; 
	//nochmal lustig: Da man sich nicht auf REPETITION END verlassen kann, da es NULL sein kann, muss man das Ende selbst berechnen.
	//Ansonsten werden hier alle Serientermine gefunden, die jemals angelegt wurden (ca 300, auch von 2009)
	// $queryexec .= " AND (unix_timestamp(APPOINTMENT_START)+(REPETITION_NUMBER*REPETITION_INTERVAL*604800)) > ".$ende;

	//appointment start muss innerhalb der zeitspanne liegen:
	//1.1) entweder die stunde ist größer, also 12 >10 oder
	//1.2) die stunde ist gleich wie der anfang, aber die minute des starts ist größer als der anfragestart 40>30
	
	//UND zusätzlich!!!!! muss der Start auch kleiner sein als das ende, da der termin sonst anfängt, wenn der suchzeitraum vorbei ist
	//2.1) entweder die stunde von ap.Start ist kleiner als die stunde des endes 11 < 13 oder
	//2.2) die stunde von ap ende ist gleich wie die stunde von zeitraum ende aber die minute von ap.start ist kleiner/gleich als die minute von zeitraum ende
	$queryexec .= " and (
	(
		(
			hour(ap.APPOINTMENT_START) > hour(from_unixtime(:start))	
			or
			(
				hour(ap.APPOINTMENT_START) = hour(from_unixtime(:start)) and minute(ap.APPOINTMENT_START) >= minute(from_unixtime(:start))
			)
		)
		and
		(	hour(ap.APPOINTMENT_START) < hour(from_unixtime(:ende))
			or
			(
				hour(ap.APPOINTMENT_START) = hour(from_unixtime(:ende)) and minute(ap.APPOINTMENT_START) <= minute(from_unixtime(:ende))
			)
		)
	)
	or(
		(	
			hour(ap.APPOINTMENT_END) > hour(from_unixtime(:start))	
			or
			(
				hour(ap.APPOINTMENT_END) = hour(from_unixtime(:start)) and minute(ap.APPOINTMENT_END) > minute(from_unixtime(:start))
			)
		)
		and
		(
			hour(ap.APPOINTMENT_END) < hour(from_unixtime(:ende))
			or
			(
				hour(ap.APPOINTMENT_END) = hour(from_unixtime(:ende)) and minute(ap.APPOINTMENT_END) <= minute(from_unixtime(:ende))
			)
		)
	)
	or(
		(
			hour(ap.APPOINTMENT_START) < hour(from_unixtime(:start))
			or
			(
				hour(ap.APPOINTMENT_START) = hour(from_unixtime(:start)) and minute(ap.APPOINTMENT_START) <= minute(from_unixtime(:start)) 
			)
		)
		and
		(
			hour(ap.APPOINTMENT_END) > hour(from_unixtime(:ende))
			or
			(
			hour(ap.APPOINTMENT_END) = hour(from_unixtime(:ende)) and minute(ap.APPOINTMENT_END) >= minute(from_unixtime(:ende))
			)
		)
	)
	)";
	//die Wiederholungsart weekly setzt voraus, dass der Anfangstag des Serientermins gleich ist,
	//wie der Tag der Anfrage (Serie beginnt jede Woche Montags -> Treffer nur, wenn Montags angefragt wird)
	$queryexec .= " AND REPETITION_TYPE = 'weekly' and REPETITION_END IS NULL and REPETITION_NUMBER IS NULL"; 
			
	$stmt = $dbh->prepare($queryexec);
	$stmt->bindParam(":ende",$ende);
	$stmt->bindParam(":start",$start);
	$stmt->execute();
	if ($GLOBALS["debug"] == 1) echo "<br><br>".$queryexec;
	
	$listappointments = "";
	while($row = $stmt->fetch(PDO::FETCH_BOTH)){
		//Füge ID des Appointments hinzu, wenn es keine Ausnahme gibt (NULL ist)
		if(is_null($row["EXCEPTION_DATE"])){
			//Hier muss ID angehängt werden, da durch den Join mit der Exception Tabelle die Spalte APOINTMENT_ID mit NULL überschrieben wird und nur ID weiterhin eindeutig ist
			$listappointments .= $row["ID"].",";
		}
	}
	$stmt = null;
	return $listappointments;	
}


/**
 * Selektion für weekly und repetition end IS NULL
 */
function getDailyAppointments($start,$ende,$listrooms){
	global $dbh;
	//################################################################################
	// Mann müsste noch auseinander steuern, was es für wiederholungsarten sind: unendlich wiederholung: dann ists immer dabei
	// * ODER endliche Wiederholung, dann muss REPETITION_END aber größer als $ende sein!
	//Selektiere alle Appointments !die gerade aktiv sind! mit Wiederholungstyp Weekly und hänge sie an die Liste. 
	//!!!!!!!!!!!!!!!! 1. Unterscheidung: Nimm nur Termine mit unendlicher Wiederholung! (s. ganz unten) !!!!!!!!!!!!!!!!!!
	//################################################################################
	$queryexec = "SELECT *, hour(ap.APPOINTMENT_START) as begin_h, hour(ap.APPOINTMENT_END) as end_h, minute(ap.APPOINTMENT_START), 
	minute(from_unixtime(:ende)),hour(from_unixtime(:start))";
	$queryexec .= " FROM allocation al";
	$queryexec .= " INNER JOIN appointment ap ON al.APPOINTMENT_ID = ap.ID ";
	//prüft gleich noch mit, ob für den anfragetag eine exception gepflegt ist
	$queryexec .= " left outer join appointment_exception ae on";
	$queryexec .= " (ae.APPOINTMENT_ID = ap.ID and year(EXCEPTION_DATE) = year(FROM_UNIXTIME(:start))";
	$queryexec .= " and month(EXCEPTION_DATE) = month(FROM_UNIXTIME(:start)) and day(EXCEPTION_DATE) = day(FROM_UNIXTIME(:start)))";
	$queryexec .= " WHERE al.RESOURCE_ID IN($listrooms)";
	$queryexec .= " and UNIX_TIMESTAMP( FROM_UNIXTIME( UNIX_TIMESTAMP(  ap.APPOINTMENT_START ) ,  '%Y-%m-%d 0:0:0' ) ) <= (:start)";
	$queryexec .= " and UNIX_TIMESTAMP( FROM_UNIXTIME( UNIX_TIMESTAMP(  ap.REPETITION_END ) ,  '%Y-%m-%d 0:0:0' ) ) >= :ende";
	$queryexec .= " and (
	(
		(
			hour(ap.APPOINTMENT_START) > hour(from_unixtime(:start))	
			or
			(
				hour(ap.APPOINTMENT_START) = hour(from_unixtime(:start)) and minute(ap.APPOINTMENT_START) >= minute(from_unixtime(:start))
			)
		)
		and
		(	hour(ap.APPOINTMENT_START) < hour(from_unixtime(:ende))
			or
			(
				hour(ap.APPOINTMENT_START) = hour(from_unixtime(:ende)) and minute(ap.APPOINTMENT_START) <= minute(from_unixtime(:ende))
			)
		)
	)
	or(
		(	
			hour(ap.APPOINTMENT_END) > hour(from_unixtime(:start))	
			or
			(
				hour(ap.APPOINTMENT_END) = hour(from_unixtime(:start)) and minute(ap.APPOINTMENT_END) > minute(from_unixtime(:start))
			)
		)
		and
		(
			hour(ap.APPOINTMENT_END) < hour(from_unixtime(:ende))
			or
			(
				hour(ap.APPOINTMENT_END) = hour(from_unixtime(:ende)) and minute(ap.APPOINTMENT_END) <= minute(from_unixtime(:ende))
			)
		)
	)
	or(
		(
			hour(ap.APPOINTMENT_START) < hour(from_unixtime(:start))
			or
			(
				hour(ap.APPOINTMENT_START) = hour(from_unixtime(:start)) and minute(ap.APPOINTMENT_START) <= minute(from_unixtime(:start)) 
			)
		)
		and
		(
			hour(ap.APPOINTMENT_END) > hour(from_unixtime(:ende))
			or
			(
			hour(ap.APPOINTMENT_END) = hour(from_unixtime(:ende)) and minute(ap.APPOINTMENT_END) >= minute(from_unixtime(:ende))
			)
		)
	)
	)";
	//die Wiederholungsart weekly setzt voraus, dass der Anfangstag des Serientermins gleich ist,
	//wie der Tag der Anfrage (Serie beginnt jede Woche Montags -> Treffer nur, wenn Montags angefragt wird)
	$queryexec .= " AND REPETITION_TYPE = 'daily'"; 
			
	$stmt = $dbh->prepare($queryexec);
	$stmt->bindParam(":ende",$ende);
	$stmt->bindParam(":start",$start);

	$stmt->execute();
	
	if ($GLOBALS["debug"] == 1) echo "<br><br>".$queryexec;
	
	$listappointments = "";
	
	while($row = $stmt->fetch(PDO::FETCH_BOTH)){
		//Füge ID des Appointments hinzu, wenn es keine Ausnahme gibt (NULL ist)
		if(is_null($row["EXCEPTION_DATE"])){
			//Hier muss ID angehängt werden, da durch den Join mit der Exception Tabelle die Spalte APOINTMENT_ID mit NULL überschrieben wird und nur ID weiterhin eindeutig ist
			$listappointments .= $row["ID"].",";
			}
	}
	$stmt = null;
	return $listappointments;	
}



?>