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
function filterCourses(){
//liest ein komplettes File in einen String und trennt die Werte mit einem Leerzeichen
$str = implode(" ",file(realpath(__DIR__."/../config/filterCourses.txt")));
  
//Splittet den String in ein Array
$arr = explode("\n",$str);

$listOfValues = $exclude = "";
for ($i = 0; $i < sizeof($arr); $i++){
	$exclude = trim($arr[$i]);
	 
 	if ($exclude  != "") $listOfValues.= "'".$exclude."',";
}
 
	//it is necesarry to return '' in case the list is empty. Otherwise the sql queries cause errors
	if (strlen($listOfValues) == 0){
		return $listOfValues = "''";
	}else{
		return $listOfValues= substr($listOfValues, 0, -1);
	}
}


function filterProfessors(){
//liest ein komplettes File in einen String und trennt die Werte mit einem Leerzeichen
$str = implode(" ",file(realpath(__DIR__."/../config/filterProfessors.txt")));

//Splittet den String in ein Array
$arr = explode("\n",$str);
$listOfValues = "";
$exclude = "";
for ($i = 0; $i < sizeof($arr); $i++){
	$exclude = trim($arr[$i]);
 	if ($exclude  != "") $listOfValues.= "'".$exclude."',";
}

	//it is necesarry to return '' in case the list is empty. Otherwise the sql queries cause errors
	if (strlen($listOfValues) == 0){
		return $listOfValues = "''";
	}else{
		return $listOfValues= substr($listOfValues, 0, -1);
	}
}

function filterRooms(){
//liest ein komplettes File in einen String und trennt die Werte mit einem Leerzeichen
$str = implode(" ",file(realpath(__DIR__."/../config/filterRooms.txt")));
  
//Splittet den String in ein Array
$arr = explode("\n",$str);

$listOfValues = $exclude = "";
for ($i = 0; $i < sizeof($arr); $i++){
	$exclude = trim($arr[$i]);
 	if ($exclude  != "") $listOfValues.= "'".$exclude."',";
}
 
	//it is necesarry to return '' in case the list is empty. Otherwise the sql queries cause errors
	if (strlen($listOfValues) == 0){
		return $listOfValues = "''";
	}else{
		return $listOfValues= substr($listOfValues, 0, -1);
	}	
}
?>