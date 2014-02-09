package org.rapla.plugin.freiraum.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.rapla.components.util.ParseDateException;
import org.rapla.facade.RaplaComponent;
import org.rapla.framework.Configuration;
import org.rapla.framework.RaplaContext;
import org.rapla.framework.RaplaException;
import org.rapla.plugin.freiraum.common.ResourceDescriptor;
import org.rapla.servletpages.RaplaPageGenerator;

public class FreiraumExportPageGenerator extends RaplaComponent implements RaplaPageGenerator {
    private Configuration config;

    public FreiraumExportPageGenerator(RaplaContext context, Configuration config) {
		super(context);
        this.config=config;
	}

	public void generatePage(ServletContext context,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException 
	{
		
		//###############################################
		//GET Parameter auslesen und Queries zusammenbauen. Die Parameter sind in der Dokumentation unter Anfragen zu finden 
		//###############################################
		String query = request.getParameter("query");
		String layout = request.getParameter("layout");
		String debug = request.getParameter("debug");
		String name = request.getParameter("name");
		String resourceId = request.getParameter("resourceId");
		String starttime = request.getParameter("starttime");
		String endtime = request.getParameter("endtime");
		String elementclass = request.getParameter("elementclass");
		String durationString = request.getParameter("duration");
		String dayOffset = request.getParameter("dayOffset");
		String error = request.getParameter("error");
		String roomCategory = request.getParameter("roomCategory");
		Date start;
		Date end = null;
		try
		{
			if ( starttime == null){
				start = getQuery().today();
			}
			else
			{
				start = getRaplaLocale().getSerializableFormat().parseDate( starttime, false);
			}
			if ( endtime != null)
			{
				end = getRaplaLocale().getSerializableFormat().parseDate( endtime, false);
			}
			else if ( durationString != null )
			{
				end = new Date( start.getTime() + Integer.parseInt(durationString) * 3600 * 1000);
			}
			else
			{
				end = null;
			}
		}
		catch (ParseDateException ex)
		{
			throw new ServletException( ex);
		}

		/*
		//Prüfung, ob die Anfragezeit noch erlaubt ist. Die Zeitspanne für Zukunft und Vergangenheit kann in der Konfigurationsdatei geändert werden
		//weiterhin prüfen, ob die query die roomfree query ist. dann soll das zeitlimit nicht gelten!
		if ($query != "roomfree" && ($start > $tag_anfang+ CALENDAR_DAYS_FUTURE*SECS_PER_DAY || $dayOffset > CALENDAR_DAYS_FUTURE)){	
			$json = createJson(array(JSON_ATTRIBUTE_DATA => null, "error" =>"Der gewählte Tag liegt zu weit in der Zukunft. Es sind maximal ".CALENDAR_DAYS_FUTURE." Tage erlaubt." ));
			echo $json;
			exit();
		}

		//Prüfung ob Termine in der Vergangenheit noch erlaubt sind
		//weiterhin prüfen, ob die query die roomfree query ist. dann soll das zeitlimit nicht gelten!
		if ($query != "roomfree" && ($start < $tag_anfang-(CALENDAR_DAYS_PAST*SECS_PER_DAY)  || $dayOffset < CALENDAR_DAYS_PAST*(-1))){
			$json = createJson(array(JSON_ATTRIBUTE_DATA => null, "error" => "Der gewählte Tag liegt in der Vergangenheit. Es sind maximal ".CALENDAR_DAYS_PAST." Tage erlaubt."));
			echo $json;
			exit();
		}


		if (!isset($duration)) $duration =  0;
		//Offset-Berechnung für den Zeitraum der Termine. Ist der Offset 1, so wird der Anfang der Zeitspanne auf den Beginn des nächsten Tages gelegt
		//Für dayOffset = 0 ergibt sich keine Änderung, da 0 hinzuaddiert wird
		//Für dayOffset <= 0 wird der rechte Teil automatisch negativ und somit abgezogen
		//Für dayOffset > 0 wird die passende Anzahl an Sekunden hinzuaddiert
		$tag_anfang += SECS_PER_DAY * $dayOffset;
		$tag_offset += SECS_PER_DAY * $dayOffset;
		$tag_ende   += SECS_PER_DAY * $dayOffset;

		if($GLOBALS["debug"] ==1){echo "Tag_Anfang:".$tag_anfang."<br>Tag_Ende:".$tag_ende;}
*/
		//###############################################
		//Auswertungen der verschiedenen Abfragen, die in query gespeichert sind
		//###############################################
		/*
		switch (query) {
			//Die Termine eines Raumes sollen angefragt werden. Detaillierte Parameter sind in der Dokumentation unter Datenermittlung beschrieben
			case "room":
				try{
					//Mit den RessourcenSchlüsseln für Räume DBKEY_ROOM_NAME und DBKEY_ROOM wird die ID des Raumes ermittelt
					//$roomid = getResourceIdByName($name,DBKEY_ROOM_NAME,DBKEY_ROOM);
					$roomid = $resourceId;
					//Mit Hilfe der ID und den Timestamps des Zeitraums werden alle Termine der verschiedenen Terminarten ermittelt (Einzeltermine, wöchentliche Termine, Tägliche Termine)
					$result = getAppointmentByResource($tag_anfang,$tag_ende,$roomid);
					$data = "";
					//Die ermittelten Ergebnisse werden in HTML-Tags eingebunden
					$data = transformAppointmentsToHtml($result,$roomid,$class,$tag_ende,$tag_offset);
				}
				catch(Exception $e){
					$json = createJson(array(JSON_ATTRIBUTE_DATA => null, JSON_ATTRIBUTE_ERROR => $e->getMessage()));
		    		echo $json;
					return;
				}
				//Die formatierten Daten werden als JSON Objekt an den Client zurückgeliefert
				$json = createJson(array(JSON_ATTRIBUTE_DATA => $data, JSON_ATTRIBUTE_ERROR => null));
				echo $json;
				break;
			//Die Termine eines Kurses sollen angefragt werden. Detaillierte Parameter sind in der Dokumentation unter Datenermittlung beschrieben
			case "course":
				try{
				//Mit den RessourcenSchlüsseln für Kurse DBKEY_COURSE_NAME und DBKEY_COURSE wird die ID des Kurses ermittelt
				//$course_id = getResourceIdByName($name,DBKEY_COURSE_NAME,DBKEY_COURSE);
				$course_id = $resourceId;
				//Mit Hilfe der ID und den Timestamps des Zeitraums werden alle Termine der verschiedenen Terminarten ermittelt (Einzeltermine, wöchentliche Termine, Tägliche Termine)
				$result = getAppointmentByResource($tag_anfang,$tag_ende,$course_id);		
				$data = "";
				//Die ermittelten Ergebnisse werden in HTML-Tags eingebunden
				$data = transformAppointmentsToHtml($result,$course_id,$class,$tag_ende,$tag_offset);
				}
				catch(Exception $e){
					$json = createJson(array(JSON_ATTRIBUTE_DATA => null, JSON_ATTRIBUTE_ERROR => $e->getMessage()));
		    		echo $json;
					return;
				}
				//Die formatierten Daten werden als JSON Objekt an den Client zurückgeliefert		
				$json = createJson(array(JSON_ATTRIBUTE_DATA => $data, JSON_ATTRIBUTE_ERROR => null));
				echo $json;
				break;
			//Die Termine eines Professors (extern oder intern) sollen angefragt werden. Detaillierte Parameter sind in der Dokumentation unter Datenermittlung beschrieben
			case "professor":
				//Die Anzeige und Abfrage von Professoren kann aufgrund des Datenschutzes deaktiviert werden (Dokumentation siehe Konstanten)
				if (MENUE_PROFESSOR_VISIBLE  != "true"){
					echo "Anzeige von Professoren ist deaktiviert";	
					exit();
				}
				try{

				//Mit Hilfe der ID, die in diesem Falle bereits als Parameter bereitgestellt wird und den Timestamps des Zeitraums 
				//werden alle Termine der verschiedenen Terminarten ermittelt (Einzeltermine, wöchentliche Termine, Tägliche Termine)
				$result = getAppointmentByResource($tag_anfang,$tag_ende,$resourceId);
				$data = "";
				//Die ermittelten Ergebnisse werden in HTML-Tags eingebunden
				$data = transformAppointmentsToHtml($result,$resourceId,$class,$tag_ende,$tag_offset);
				}
				catch(Exception $e){
					$json = createJson(array(JSON_ATTRIBUTE_DATA => null, JSON_ATTRIBUTE_ERROR => $e->getMessage()));
		    		echo $json;
					return;
				}
				//Die formatierten Daten werden als JSON Objekt an den Client zurückgeliefert				
				$json = createJson(array(JSON_ATTRIBUTE_DATA => $data, JSON_ATTRIBUTE_ERROR => null));
				echo $json;
				break;
				
			//Alle freien Räume während eines bestimmten Zeitraums sind zu finden. Detaillierte Parameter sind in der Dokumentation unter Datenermittlung beschrieben		
			case "roomfree":
				try{
				//Vorgehensweise: 1)Selektiere alle Appointments, die im Zielzeitraum (start bis start+duration) stattfinden
				//2)Selektiere alle Räume, die von diesen Appointments verwendet werden
				//3)Selektiere alle Räume, die nicht in der vorher ermittelten Menge enthalten sind --> fertig
				//Bei den Terminen sind die verschiedenen Terminarten zu berücksichtigen (Einzeltermin, wöchtenlich, täglich)
				
				//Bestimmte Räume können mithilfe eines Filters ausgeschlossen werden
				$filterRooms = filterRooms();
				
				

				//selektiere alle räume, die nicht ausgefiltert sind und erstelle eine Liste, um die Liste an getAppointmentByResource zu übergeben
				//es sind 2 joins nötig, da 2 attribute name und kategorie nötig sind, diese aber jeweils in einer zeile der Tabelle ressource_attribute_value gespeichert sind
				$queryexec =  " select distinct(ID) from rapla_resource rr ";
				$queryexec .= " inner join resource_attribute_value rav on (rr.ID = rav.RESOURCE_ID and rav.ATTRIBUTE_KEY = 'name') ";
				$queryexec .= " inner join resource_attribute_value rav2 on (rr.ID = rav2.RESOURCE_ID and rav2.ATTRIBUTE_KEY = 'roomtype')";
				$queryexec .= " where rav.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." not in ($filterRooms)";
				if ($roomCategory != "" && isset($roomCategory)){
					if($debug == 1){ echo"<br>ROOM_CATEGORIE:".$roomCategory."    ///   Constant DBCOL_RESOURCE_ATTRIBUTE_VALUE = ".DBCOL_RESOURCE_ATTRIBUTE_VALUE."<br>";}
					//now distinguish between RAPLA version lower 1.7 and higher 1.7 (lower -> XML string, higher -> ID)
					if ( DBCOL_RESOURCE_ATTRIBUTE_VALUE == "attribute_value"){
					$queryexec .= " and rav2.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." = ".$roomCategory;				
					}
					else{
					$queryexec .= " and rav2.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." = 'org.rapla.entities.Category_".$roomCategory."'";		
					}
				}
				//based on the RAPLA version you have to filter the ID of the category (i.e. 7 ) or the XML string of the category (i.e. )
				
				$stmt = $dbh->prepare($queryexec);
				$stmt->execute();
				
				if ($debug == 1) echo "<br><br>".$queryexec;
				
				$listrooms = "";
				while ($row = $stmt->fetch(PDO::FETCH_BOTH)){
					$listrooms .= $row["ID"].",";
				}
				$stmt = null;
				$listrooms = substr($listrooms, 0, -1);
				
				
				//SONDERFALL: Es kann vorkommen, dass Räume im Filter stehen, diese aber nicht existieren. Dann würdeen die folgenden Queries einen Teil absetzen, der "... where id IN () " enthält.
				//Die leere Klammer führt aber zu einem Fehler in SQL, da mindestens ein Argutment erwartet wird. Deshalb muss im Falle einer leeren $listrooms einfach 0 übergeben werden (Id 0 existiert nicht)
				if ($listrooms == ""){
					$listrooms = 0;
				}
				
				
				//Selektiere alle IDs der verschiedenen Terminarten während des Zeitraums und erzeuge eine Liste
				$listappointments = "";		
				$listappointments .= getAppointmentsWithoutRepetition($start,$ende,$listrooms);
				if ($debug ==1){echo $listappointments;}
				$listappointments .= getWeeklyAppointmentsWithEnd($start,$ende,$listrooms);
				if ($debug ==1){echo $listappointments;}
				$listappointments .= getWeeklyAppointmentsWithoutEnd($start,$ende,$listrooms);
				if ($debug ==1){echo $listappointments;}
				$listappointments .= getDailyAppointments($start,$ende,$listrooms);
				if ($debug ==1){echo $listappointments;}
				//letztes Komma abschneiden		
				$listappointments = substr($listappointments, 0, -1);
				
				//Selektiere alle Räume, in denen im Zielzeitraum keine Vorlesung ist. --> Raum ist dann frei
				if ($listappointments == "") $listappointments = "''"; //das IN Statement darf nicht leer sein, sonst kommt ein Fehler: Passiert wenn keine Vorlesung stattfindet momentan(Nachts,Abends)
				
					$queryexec = "select ID,rav.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." as VALUE from rapla_resource rr right outer join resource_attribute_value rav on 
				rr.ID = rav.RESOURCE_ID
				inner join resource_attribute_value rav2 on (rr.ID = rav2.RESOURCE_ID)
				where rav.ATTRIBUTE_KEY = 'name' and 
				id not in  (
						SELECT RESOURCE_ID FROM allocation a inner join rapla_resource rr on a.RESOURCE_ID = rr.ID where TYPE_KEY = '".DBKEY_ROOM."' and APPOINTMENT_ID IN (
						'$listappointments'
						)
				)
				and type_key = '".DBKEY_ROOM."' and rav.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." not in ($filterRooms) ";
				
					//distinguish between newer and older RAPLA versions
					if ( DBCOL_RESOURCE_ATTRIBUTE_VALUE == "attribute_value"){
					$queryexec .= " and rav2.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." = ".$roomCategory;				
					}
					else{
					$queryexec .= " and rav2.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." = 'org.rapla.entities.Category_".$roomCategory."'";		
					}
				
				$queryexec.= " order by rav.".DBCOL_RESOURCE_ATTRIBUTE_VALUE;
				
				$stmt = $dbh->prepare($queryexec);
				$stmt->execute();
				if ($debug == 1) echo "<br><br>".$queryexec;
				$data = "";
				//formatiere alle Räume als Liste für die Oberfläche		
				while($row = $stmt->fetch(PDO::FETCH_BOTH)){
					$data .="<li><a class='".$class."' href='#roomDetailInfo' data-rel='popup' data-position-to='window' data-transition='fade'  data-room-id='".$row["ID"]."' data-room='".$row["VALUE"]."'>" . $row["VALUE"] . "</a></li>";		
				}
				$stmt = null;
				}
				catch(Exception $e){
					$json = createJson(array(JSON_ATTRIBUTE_DATA => null, JSON_ATTRIBUTE_ERROR => $e->getMessage()));
		    		echo $json;
					return;
				}
				//Die formatierten Daten werden als JSON Objekt an den Client zurückgeliefert				
				$json = createJson(array(JSON_ATTRIBUTE_DATA => $data, JSON_ATTRIBUTE_ERROR => null));
				echo $json;
				break;

				
			case "roomdetail":
				$data = "";
				try{
				//$name muss die ID des Raumes enthalten!
				if(!is_numeric($name)){
					$json = createJson(array(JSON_ATTRIBUTE_DATA => null, "error" => "Bei der Raum-Detail Anfrage wurde der Parameter name nicht mit der ID des Raumes übergeben."));
		    		echo $json;
					exit();
				}
				
				//Die Spalte, die mit der Konstante DBCOL_RESOURCE_ATTRIBUTE_VALUE refenziert wird, wird im select statement umbenannt. Dies ist nötig, um sie danach
				//einheitlich über $row["VALUE"] abfragen zu können. Eine Abfrage über $row[DBCOL_RESOURCE_ATTRIBUTE_VALUE] ist NICHT möglich!
				$queryexec = "select ATTRIBUTE_KEY, ra.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." as VALUE from resource_attribute_value ra where resource_id = :name and attribute_key in (".ROOMDETAILS_ATTRIBUTE_LIST.")";
				$stmt = $dbh->prepare($queryexec);
				$stmt->bindParam(":name",$name);
				$stmt->execute();
				
				if ($debug == 1) echo "<br><br>".$queryexec;
					
				$id_category = 0;
				while($row = $stmt->fetch(PDO::FETCH_BOTH)){
					if ($row["ATTRIBUTE_KEY"] == "roomtype"){
						$id_category = $row["VALUE"];
					}
					else{
					$data.= "<tr><td class='".$class."'>".$row["ATTRIBUTE_KEY"]."</td><td>".$row["VALUE"]."</td></tr>";				
					}	
				}
				$stmt = null;
				
				//ermittle noch die Kategorie, falls RAPLA eine Version >= 1.7 hat (Quick and Dirty: Vergleich auf Konstante)
				 if (DBCOL_RESOURCE_ATTRIBUTE_VALUE == "attribute_value"){
					 $queryexec = "select * from category where id = :id";
					 $stmt = $dbh->prepare($queryexec);
					 $stmt->bindParam(":id",$id_category);
					 $stmt->execute();
					 
					 while ($row = $stmt->fetch(PDO::FETCH_BOTH)){
					 	if ($row["LABEL"] != "" and $row["LABEL"] !="-"){
					 	$data.= "<tr><td class='".$class."'>roomtype</td><td>".$row["LABEL"]."</td></tr>";
						}
					 }
					 $stmt = null;
				 }
				}
				catch(Exception $e){
					$json = createJson(array(JSON_ATTRIBUTE_DATA => null, JSON_ATTRIBUTE_ERROR => $e->getMessage()));
		    		echo $json;
					return;
				}
				$json = createJson(array(JSON_ATTRIBUTE_DATA => $data, JSON_ATTRIBUTE_ERROR => null));
				echo $json;
				break;
			default :
				break;
		}

		switch (layout) {

			case "courses":
				try{
				//Der Filter blendet einzelne Kurse, die in einer Liste definiert sind, aus:
				$filterCourses = filterCourses();
				//Room ist in Tabelle rapla_resource gespeichert mit TYPE_KEY=resource1
				//Name des Raums ist in Tabelle resource_attribute_value gespeichert mit id und ATTRIBUTE_KEY = name
				
				//Die Spalte, die mit der Konstante DBCOL_RESOURCE_ATTRIBUTE_VALUE refenziert wird, wird im select statement umbenannt. Dies ist nötig, um sie danach
				//einheitlich über $row["course_name"] abfragen zu können. Eine Abfrage über $row[DBCOL_RESOURCE_ATTRIBUTE_VALUE] ist NICHT möglich!
				$queryexec = "select *,ra.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." as course_name from rapla_resource rr inner join resource_attribute_value ra on";
				$queryexec .= " rr.ID = ra.RESOURCE_ID";
				$queryexec .= " Where rr.TYPE_KEY = '".DBKEY_COURSE."' and ra.ATTRIBUTE_KEY = '".DBKEY_COURSE_NAME."' ";
				$queryexec .= " and ".DBCOL_RESOURCE_ATTRIBUTE_VALUE." not IN ($filterCourses)";
				$queryexec .= " order by ".DBCOL_RESOURCE_ATTRIBUTE_VALUE." asc";
				
				$stmt = $dbh->prepare($queryexec);
				$stmt->execute();
				
				if ($debug == 1) echo "<br><br>".$queryexec;
				
				$array = array();
				$index = 0;
					while($row = $stmt->fetch(PDO::FETCH_BOTH)){	 	
					$data[$index] = array("name" => $row["course_name"], "id" => $row["ID"]); 
					$index++;
					//$data .="<li><a class='".$class."' href='' data-course='" . $row["course_name"] . "' data-resource-id='".$row["ID"]."'>" . $row["course_name"] . "</a></li>";
					}
					$stmt = null;
			
				}
				catch(Exception $e){
					$json = createJson(array(JSON_ATTRIBUTE_DATA => null, JSON_ATTRIBUTE_ERROR => $e->getMessage()));
		    		echo $json;
					return;
				}
				$json = createJson(array(JSON_ATTRIBUTE_DATA => $data, JSON_ATTRIBUTE_ERROR => null));
				echo $json;
				break;


			case "rooms":
				try{
				$filterRooms = filterRooms();
				//Room ist in Tabelle rapla_resource gespeichert mit TYPE_KEY=resource1
				//Name des Raums ist in Tabelle resource_attribute_value gespeichert mit id und ATTRIBUTE_KEY = name

				//Die Spalte, die mit der Konstante DBCOL_RESOURCE_ATTRIBUTE_VALUE refenziert wird, wird im select statement umbenannt. Dies ist nötig, um sie danach
				//einheitlich über $row["room_name"] abfragen zu können. Eine Abfrage über $row[DBCOL_RESOURCE_ATTRIBUTE_VALUE] ist NICHT möglich!
				$queryexec = "select *, ra.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." as room_name from rapla_resource rr inner join resource_attribute_value ra on";
				$queryexec .= " rr.ID = ra.RESOURCE_ID";
				$queryexec .= " Where rr.TYPE_KEY = '".DBKEY_ROOM."' and ra.ATTRIBUTE_KEY = '".DBKEY_ROOM_NAME."'";
				$queryexec .= " and ".DBCOL_RESOURCE_ATTRIBUTE_VALUE." not in ($filterRooms) order by ".DBCOL_RESOURCE_ATTRIBUTE_VALUE." asc";
				
				$stmt = $dbh->prepare($queryexec);
				$stmt->execute();
				
				if ($debug == 1) echo "<br><br>".$queryexec;		
				$data = "";
				while($row = $stmt->fetch(PDO::FETCH_BOTH)){
					$data .="<li><a class='".$class."' href='' data-room='" . $row["room_name"] . "' data-resource-id='".$row["ID"]."'>" . $row["room_name"] . "</a></li>";
				}
				$stmt = null;
				
				}
				catch(Exception $e){
					$json = createJson(array(JSON_ATTRIBUTE_DATA => null, JSON_ATTRIBUTE_ERROR => $e->getMessage()));
		    		echo $json;
					return;
				}
				
				$json = createJson(array(JSON_ATTRIBUTE_DATA => $data, JSON_ATTRIBUTE_ERROR => null));
		    	echo $json;
					
				
				break;
			
			case "professors":
				$filterProfessors = filterProfessors();
				try{
				//Die Spalten, die mit der Konstante DBCOL_RESOURCE_ATTRIBUTE_VALUE refenziert werden, werden im select statement umbenannt. Dies ist nötig, um sie danach
				//einheitlich über $row["surname"], $row["forename"], row["title"] abfragen zu können.
				$queryexec = "select *,ra1.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." as surname, ra2.".DBCOL_RESOURCE_ATTRIBUTE_VALUE." as forename, ra3.".DBCOL_RESOURCE_ATTRIBUTE_VALUE."  as title from rapla_resource rr 
				left outer join resource_attribute_value ra1 on (rr.ID = ra1.RESOURCE_ID and ra1.ATTRIBUTE_KEY = 'surname' ) 
				left outer join resource_attribute_value ra2 on (rr.ID = ra2.RESOURCE_ID and ra2.ATTRIBUTE_KEY = 'forename' ) 
				left outer join resource_attribute_value ra3 on (rr.ID = ra3.RESOURCE_ID and ra3.ATTRIBUTE_KEY = 'a1' ) ";
				$queryexec .= " Where rr.TYPE_KEY = 'defaultPerson' ";
				$queryexec .= " and ra1.".DBCOL_RESOURCE_ATTRIBUTE_VALUE."  NOT IN ($filterProfessors)";
				$queryexec .= " union all 
				select *,ra1.".DBCOL_RESOURCE_ATTRIBUTE_VALUE."  as surname, ra2.".DBCOL_RESOURCE_ATTRIBUTE_VALUE."  as forename, ra3.".DBCOL_RESOURCE_ATTRIBUTE_VALUE."  as title from rapla_resource rr 
				left outer join resource_attribute_value ra1 on (rr.ID = ra1.RESOURCE_ID and ra1.ATTRIBUTE_KEY = 'surname' ) 
				left outer join resource_attribute_value ra2 on (rr.ID = ra2.RESOURCE_ID and ra2.ATTRIBUTE_KEY = 'forename' ) 
				left outer join resource_attribute_value ra3 on (rr.ID = ra3.RESOURCE_ID and ra3.ATTRIBUTE_KEY = 'a1' ) 
				Where rr.TYPE_KEY = 'person1' 
				and ra1.".DBCOL_RESOURCE_ATTRIBUTE_VALUE."  not in ($filterProfessors)";
				$queryexec .= " order by surname asc";

				if ($debug == 1) echo "<br><br>".$queryexec;
				
				$stmt = $dbh->prepare($queryexec);
				$stmt->execute();
				
				$data = "";
					while($row = $stmt->fetch(PDO::FETCH_BOTH)){
					if ($row["title"] != ""){$title = ", ".$row["title"];}else{$title = "";}
					$data .="<li><a class='".$class."' href='' data-prof-surname='".$row["surname"]."' data-resource-id='".$row["ID"]."' data-prof-title='".$row["title"]."'>".$row["surname"] . " ".$row["forename"].$title."</a></li>";
					}
					$stmt = null;
				}
				catch(Exception $e){
					$json = createJson(array(JSON_ATTRIBUTE_DATA => null, JSON_ATTRIBUTE_ERROR => $e->getMessage()));
		    		echo $json;
					return;
				}
				
				$json = createJson(array(JSON_ATTRIBUTE_DATA => $data, JSON_ATTRIBUTE_ERROR => null));
				echo $json;
				break;
			
			case "roomchairs":
				try{
					$queryexecute = "select min(chairs) as min ,max(chairs) as max from 
					(
					SELECT CAST(value as SIGNED) as chairs FROM resource_attribute_value 
					where attribute_key = 'chairs_max' and value > 0 order by chairs desc
					) chairtable";
					
					if ($debug == 1) echo "<br><br>".$queryexecute;
					
					foreach ($dbh->query($queryexecute) as $row)
			        {
			        $min = $row['min'];
			        $max = $row['max'];
			        }
					if($GLOBALS["debug"] == 1){
						echo $queryexecute;
						echo "min:".$min."max:".$max;
					}
					$data = "";
					while ($min < $max){
						$data .="<li><a class='".$class."' href='' >".$min." - ".($min+ROOMFREE_STEPSIZE_CHAIRS)."</a></li>";
						$min = $min + ROOMFREE_STEPSIZE_CHAIRS;
					}
					$data .="<li><a class='".$class."' href='' >".$min." - ".($min+ROOMFREE_STEPSIZE_CHAIRS)."</a></li>";
				}
				catch(Exception $e){
					$json = createJson(array(JSON_ATTRIBUTE_DATA => null, JSON_ATTRIBUTE_ERROR => $e->getMessage()));
		    		echo $json;
					return;
				}
				
				$json = createJson(array(JSON_ATTRIBUTE_DATA => $data, JSON_ATTRIBUTE_ERROR => null));
				echo $json;
				break;
				
			case "monthoverview":
			try{
			$daysOfMonth = cal_days_in_month(CAL_GREGORIAN, date("m",time()), date("Y",time())); //28-31
			$lecturesPerDay = array_fill(0,$daysOfMonth,0);
			//!!!!!!!!!!!!!!!!Problem in der Logik:
			//man weiss zwar dass ein wochentermin in dem monat liegt, aber wie incrementiert man den zähler der tages?
			//bsp. montag wdh anzahl = 10 also müsste am 1.,2.,3.,4. montag in dem monat müssen als termin angezeigt werden! --> zusatzlogik in der applikation? 
			
			//selektiert alle normalen Termine zwischen monats anfang und ende
			$queryexecute= "SELECT day(appointment_start) as day,count(*) as count FROM 
			appointment ap inner join allocation a on ap.id = a.appointment_id 
			inner join rapla_resource r on 
			(a.resource_id = r.id and r.type_key='".DBKEY_COURSE."') 
			inner join resource_attribute_value ra on 
			(r.id = ra.resource_id  and ra.attribute_key = '".DBKEY_COURSE_NAME."' and ra.value = :course) 
			where unix_timestamp(appointment_start) >= unix_timestamp( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) ) and 
			unix_timestamp(appointment_end) <= unix_timestamp(LAST_DAY( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) ) )+".SECS_PER_DAY." and repetition_type is null group by day";
			
			if ($debug == 1) echo "<br><br>".$queryexecute;
			
			$stmt = $dbh->prepare($queryexecute);
			$stmt->bindParam(":course",$name);
			$stmt->execute();
			
			while($row = $stmt->fetch(PDO::FETCH_BOTH)){
				//Der 1. Tag des Monats fällt auf das erste Element des Arrays (0)
				$lecturesPerDay[($row["day"]-1)] = $row["count"];
			}
			$stmt = null;
			
			
			//bei der logik muss beachtet werden, dass manche start werte des appointments NACH dem Anfang des Monats und VOR dessen Ende liegen	
			$queryexecute = "SELECT *,unix_timestamp(appointment_start) as tsstart FROM appointment ap inner join allocation a on ap.id = a.appointment_id inner join rapla_resource r on 
			(a.resource_id = r.id and r.type_key='".DBKEY_COURSE."') inner join resource_attribute_value ra on 
			(r.id = ra.resource_id  and ra.attribute_key = '".DBKEY_COURSE_NAME."' and ra.value = :course) 
			where ap.repetition_type is not null and

			(
			 	(
				unix_timestamp(appointment_start) <= unix_timestamp( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) ) and 
				unix_timestamp(repetition_end) >= unix_timestamp(LAST_DAY( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) ) )
				)
				or 
				(
				unix_timestamp(appointment_start) <= unix_timestamp( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) ) and 
				unix_timestamp(repetition_end) <= unix_timestamp(LAST_DAY( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) ) ) and unix_timestamp(repetition_end) >= unix_timestamp( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) )
				)
				or
				(
				unix_timestamp(appointment_start) >= unix_timestamp( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) ) and unix_timestamp(appointment_start) <= unix_timestamp(LAST_DAY( DATE_FORMAT( NOW( ) , '%Y-%m-01' ))) and
				unix_timestamp(repetition_end) >= unix_timestamp(LAST_DAY( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) ) )
				)
				or
				(
				unix_timestamp(appointment_start) >= unix_timestamp( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) ) and unix_timestamp(appointment_start) <= unix_timestamp(LAST_DAY( DATE_FORMAT( NOW( ) , '%Y-%m-01' ))) and
				unix_timestamp(repetition_end) <= unix_timestamp(LAST_DAY( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) ) )
				
				)
			)";
			
			if ($debug == 1) echo "<br><br>".$queryexecute;
			
			$stmt = $dbh->prepare($queryexecute);
			$stmt->bindParam(":course",$name);
			$stmt->execute();
			
			//calculate the day offset of the first of the month, to compare the offset of each appointment and the first day of the month
			$firstDayOfMonth=mktime(12,0,0,date('m',time()),1,date('Y',time()));
			$dwref = date("w",$firstDayOfMonth);
			
			while($row = $stmt->fetch(PDO::FETCH_BOTH)){
				//es muss noch berücksichtig werden, dass ein serientermin auch ausnahmen haben kann (danke RAPLA an die Datenhaltung)
				$queryproof =  " select *, day(EXCEPTION_DATE) as day from appointment_exception where ";
				$queryproof .= " APPOINTMENT_ID = :id_appointment and unix_timestamp(EXCEPTION_DATE) >= unix_timestamp( DATE_FORMAT( NOW( ) , '%Y-%m-01' ) ) and ";
				$queryproof .= " unix_timestamp(EXCEPTION_DATE) <= unix_timestamp( LAST_DAY(now() ) ) ";
				
				if ($debug == 1) echo "<br><br>(".$row["APPOINTMENT_ID"].")  ".$queryproof;
				
				$stmt2 = $dbh->prepare($queryproof);
				$stmt2->bindParam(":id_appointment",$row["APPOINTMENT_ID"]);
				$stmt2->execute();
				if ($stmt2->rowCount() > 0){
					$t = 0;
					$exceptions = array_fill(0,$stmt2->rowCount(),"");
					while($row2 = $stmt2->fetch(PDO::FETCH_BOTH)){
						$exceptions[$t++] = $row2["day"];
					}
					
				}else{$exceptions = null; }
				//Hier beginnt der eigentliche Algorithmus------------------------------------------------
				
			 	$dw = date( "w", $row["tsstart"]); //dayofweek: 0-6
			 	$repetition = $row["REPETITION_NUMBER"];
				
				//wird negativ, falls das appointment vorher beginnt
				$startDifference = $row["tsstart"]-$firstDayOfMonth;
				$weeksDifference = floor($startDifference/(SECS_PER_DAY*7));
				
			 	$dayDistance = abs($dw-$dwref) +( 7*$weeksDifference);;
			 	
				while ($repetition-- > 0 && $dayDistance <= $daysOfMonth){
					//für die Termine, die mehrere Wochen vor dem Appointment_start beginnen, ist der Wert dayDistance negativ!!
					//Das Array darf dann nur gefüllt werden, wenn der Wert positiv oder 0 ist!
					//UND!!! Wenn es keine Exception an diesem Tag gibt!
					if ($dayDistance >= 0 && $exceptions == null){
					$lecturesPerDay[$dayDistance]++;
					//termin wird eine Woche später wiederholt
					$dayDistance = $dayDistance+7;
					}
					else if ($exceptions != null){
						//die tage in dem Array exceptions müssen geprüft werden, ob der aktuelle tage eine ausnahme ist, dann darf nicht inkrementiert werden!
						foreach ($exceptions as $e){
							//Wert des Tages auf Position im Array umrechnen (1.April -> 0.Element)	
							$e--;
							if ($e == $dayDistance){
								//Die aktuelle Ausnahme ist gefunden! Füge den Wert nicht hinzu, aber addiere trotzdem die 7 Tage, sodass in die nächste Woche gesprungen wird
								$dayDistance = $dayDistance+7;
							}else{
								$lecturesPerDay[$dayDistance]++;
								//termin wird eine Woche später wiederholt
								$dayDistance = $dayDistance+7;
							}
						}
					}
		 		} 	
			 }
			$stmt = null;
			
			
			}
			catch(Exception $e){
				$json = createJson(array(JSON_ATTRIBUTE_DATA => null, JSON_ATTRIBUTE_ERROR => $e->getMessage()));
				echo $json;
				return;
			}
			
				$json = createJson(array(JSON_ATTRIBUTE_DATA => $lecturesPerDay, JSON_ATTRIBUTE_ERROR => null));
		    	echo $json;
			

				break; 
				
				
			case "roomCategories":
				//CAUTION: This query is only supported by newer versions of RAPLA! If you are using a version older than 1.6 you could run into issues because in older versions
				//the categories are saved as a XML string in the database. You have to adjust this query by parsing this XML in the application layer then.
				$queryexecute = "select id from category where category_key = '".DBKEY_CATEGORY_ROOMS."'";
					if ($debug == 1) echo "<br><br>".$queryexecute;
					$stmt = $dbh->prepare($queryexecute);
					$stmt->execute();
					
					while($row = $stmt->fetch(PDO::FETCH_BOTH)){
						$parentId = $row["id"];
					}
					$stmt = null;
					
					$queryexecute = "select * from category where parent_id = :id";
					if ($debug == 1) echo "<br><br>".$queryexecute;
					$stmt = $dbh->prepare($queryexecute);
					$stmt->bindParam(":id",$parentId);
					$stmt->execute();
					
					$data = "";
					while($row = $stmt->fetch(PDO::FETCH_BOTH)){
						$data .="<option value='".$row["ID"]."' >".$row["LABEL"]."</option>";
					}
					$stmt = null;
					
					$json = createJson(array(JSON_ATTRIBUTE_DATA => $data, JSON_ATTRIBUTE_ERROR => null));
					echo $json;
				
				break;
				
			case "timetable":
				$data["start"] = CALENDAR_TIME_WINDOW_START;
				$data["hours"] = CALENDAR_TIME_WINDOW_HOURS;
				//return start of day to display (7 for start at 7 o clock): start
				//return period to display in hours (17 to display until 24 o clock when start is 7): hours

				$json = createJson(array(JSON_ATTRIBUTE_DATA => $data, JSON_ATTRIBUTE_ERROR => null));
				echo $json;
				
				break;	
			default :
				break;
		}



		$time2 = microtime(true);
		if($debug == 1)echo "<br><br>Zeit:".($time2-$time1);
		
		*/
//		String layout = request.getParameter("layout");
//	  	
		response.setCharacterEncoding("UTF-8");
		response.setContentType("content-type text/html");
		
		java.io.PrintWriter out = response.getWriter();
		
		try
		{
            AllocatableExporter allocatableExporter = new AllocatableExporter(getContext(), config);

            BufferedWriter buf = new BufferedWriter(out);
			StringBuffer a = request.getRequestURL();
			
			if ( layout != null)
			{
				boolean first = true;
				StringBuilder allocatableList = new StringBuilder();
				boolean courseLayout = layout.equals( "courses");
				if ( layout.equals( "professors"))
				{
					layout = "persons";
				}
				List<ResourceDescriptor> allAllocatables = allocatableExporter.getAllocatableList(layout, null,getLocale());
				for ( ResourceDescriptor allocatable : allAllocatables)
				{
				
					//String entry = "{\"name\":\"BWL\",\"id\":\"775\"}";
					String allocatableName = allocatable.getName();
					String id = allocatable.getId();
					
					String entry;
					if ( courseLayout)
					{
						entry = "{\"name\":\"" + allocatableName + "\",\"id\":\"" + id + "\"}";
						if ( !first)
						{
							allocatableList.append( ",");
						}
						else
						{
							first =false;
						}
					}
					else
					{
						// noch nicht im js angepasst
						entry = "<li><a class='elements' href='' data-resource-id='"+ id + "'>" + allocatableName + "</a></li>";
					}
					allocatableList.append( entry);
				}
				String result;
				if ( courseLayout)
				{
					result ="{\"data\":[" + allocatableList + "],\"error\":\"\"}";
				}
				else
				{
					result ="{\"data\":\"" + allocatableList + "\",\"error\":\"\"}";
				}
				
				out.print( result);
			}
			out.flush();
			int indexOf = a.lastIndexOf("/rapla");
			buf.close();
		} 
		catch (RaplaException ex) {
			//out.println( IOUtil.getStackTraceAsString( ex ) );
            getLogger().error(ex.getMessage(), ex);
            writeError(response, "Error in plugin configuration. Please contact administrator. See log files");
		}
		finally
		{
		    out.close();
		}
		
	}

    private void writeError( HttpServletResponse response, String message ) throws IOException
    {
        response.setStatus( 500 );
        response.setContentType( "text/html; charset=" + getRaplaLocale().getCharsetNonUtf() );
        java.io.PrintWriter out = response.getWriter();
        out.println( message );
        out.close();
    }
}
