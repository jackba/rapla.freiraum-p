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
require_once(realpath(__DIR__."/constants.php"));
// $dbh = new PDO('mysql:host=localhost;dbname=rapla_db', "rapla", "mothball-10", array(PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8"));


try {
    $dbh = new PDO('mysql:host=localhost;dbname='.DB_LOGIN_DATABASE, DB_LOGIN_USER, DB_LOGIN_PW,array(PDO::MYSQL_ATTR_INIT_COMMAND => "SET NAMES utf8",PDO::ATTR_PERSISTENT => true,PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,PDO::MYSQL_ATTR_USE_BUFFERED_QUERY, TRUE));

} catch (PDOException $e) {
    echo 'Connection failed: ' . $e->getMessage();
}
?>