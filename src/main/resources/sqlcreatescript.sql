delimiter $$

CREATE TABLE `user` (
  `UserId` int(11) NOT NULL AUTO_INCREMENT,
  `UserFirstName` varchar(45) DEFAULT NULL,
  `UserLastName` varchar(45) DEFAULT NULL,
  `UserName` varchar(45) DEFAULT NULL,
  `UserPass` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`UserId`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=latin1$$

SELECT * FROM `userexample`.`user`;