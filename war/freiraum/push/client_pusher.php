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
header('Content-Type: text/event-stream');
header('Cache-Control: no-cache');
// recommended to prevent caching of event data.

/**
 * Constructs the SSE data format and flushes that data to the client.
 *
 * @param string $id Timestamp/id of this connection.
 * @param string $msg Line of text that should be transmitted.
 */
function sendMsg($id, $msg) {
	echo "id: $id" . PHP_EOL;
	echo "data: $msg" . PHP_EOL;
	echo PHP_EOL;
	ob_flush();
	flush();
	//in case of debugging, uncomment these line
	// $sql = "insert into debug values (unix_timestamp(now()),:id)";
	// $q = $dbh->prepare($sql);
	// $q->execute(array(':id'=>$msg));	
}
try{
	require_once(realpath(__DIR__."/../php/sql.php"));
	require_once(realpath(__DIR__."/../php/functions.php"));
	//it makes no sense, taht the client sends his timestamp. You run into BIG trouble, if you do so. 
	//time on clients can vary up to 15 seconds, and this results in strange effects...
	$client_timestamp = time();
	$res_id = $_GET["resourceId"];
	
	$steps = 0;
	//when the client first registeres to the server, he is known with the timestamp of registering in the database
	//the while loop checks, if the client already connected to somewhere else (newer timestamp)
	$client_still_active = true;
	while ($steps < 30) {
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
		}
	
		//check if client has re-registered (you could do that every 3,4,5 sleep tick) by $steps%5 == 0
		// $sql = "select count(*) as anz from z_push_clients where id = :id and timestamp = :ts";
		// $stmt= $dbh->prepare($sql);
		// $stmt->bindParam(":id",$_COOKIE['freiraum-identifier']);
		// $stmt->bindParam(":ts",$client_timestamp);
		// $stmt->execute();
		// $result = $stmt->fetch(PDO::FETCH_ASSOC);
		// $stmt->closeCursor();	
		// if ($result["anz"] == 0){$client_still_active = false;}
		
		
		
		sleep(1);
		$steps++;
	}
}
catch(Exception $e){
	sendMsg(time(), $e->getMessage());		
}
?>
