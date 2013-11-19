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
require_once(realpath(__DIR__."/../php/sql.php"));
require_once(realpath(__DIR__."/../php/functions.php"));
/**
 * Constructs the SSE data format and flushes that data to the client.
 *
 * @param string $id Timestamp/id of this connection.
 * @param string $msg Line of text that should be transmitted.
 */
function sendMsg($id, $msg) {
	$json = createJson(array(JSON_ATTRIBUTE_DATA => $msg));
	echo $json;
}
try{
	//it makes no sense, taht the client sends his timestamp. You run into BIG trouble, if you do so. 
	//time on clients can vary up to 15 seconds, and this results in strange effects...
	$client_timestamp = time();
	$res_id = $_GET["resourceId"];
	
	$steps = 0;
	while ($steps < 100) {
		$sql = "SELECT SQL_CACHE max(DB_LAST_CHANGE) as DB_LAST_CHANGE FROM z_meta where RESOURCE_ID =:id";
		$stmt= $dbh->prepare($sql);
		$stmt->bindParam(":id",$res_id);
		$stmt->execute();
		$result = $stmt->fetch(PDO::FETCH_ASSOC);
		$stmt->closeCursor();
		$stmt = null;
		if ($result["DB_LAST_CHANGE"] > $client_timestamp){
		sendMsg(time(), "update");		
		$client_timestamp = $result["DB_LAST_CHANGE"];
		$steps = 100;
		}
		
		sleep(1);
		$steps++;
	}
}
catch(Exception $e){
	sendMsg(time(), $e->getMessage());		
}
?>
