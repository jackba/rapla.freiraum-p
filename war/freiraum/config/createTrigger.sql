use rapla_db;

DROP TABLE IF EXISTS `z_meta`;
CREATE TABLE IF NOT EXISTS `z_meta` (
  `DB_LAST_CHANGE` int(11) NOT NULL,
  `RESOURCE_ID` int(11) NOT NULL
) ENGINE=MEMORY DEFAULT CHARSET=latin1;


--
-- Trigger `appointment`
--
DROP TRIGGER IF EXISTS `last_delete_appointment`;
DELIMITER //
CREATE TRIGGER `last_delete_appointment` BEFORE DELETE ON `appointment`
 FOR EACH ROW BEGIN
    insert into z_meta (DB_LAST_CHANGE,RESOURCE_ID) 
    select unix_timestamp(now()),resource_id 
    from allocation where appointment_id = old.id ;
END
//
DELIMITER ;
DROP TRIGGER IF EXISTS `last_insert_appointment`;
DELIMITER //
CREATE TRIGGER `last_insert_appointment` AFTER INSERT ON `appointment`
 FOR EACH ROW BEGIN
    insert into z_meta (DB_LAST_CHANGE,RESOURCE_ID) 
    select unix_timestamp(now()),resource_id 
    from allocation where appointment_id = new.id ;
END
//
DELIMITER ;
DROP TRIGGER IF EXISTS `last_update_appointment`;
DELIMITER //
CREATE TRIGGER `last_update_appointment` AFTER UPDATE ON `appointment`
 FOR EACH ROW BEGIN
    insert into z_meta (DB_LAST_CHANGE,RESOURCE_ID) 
    select unix_timestamp(now()),resource_id 
    from allocation where appointment_id = new.id ;
END
//
DELIMITER ;
