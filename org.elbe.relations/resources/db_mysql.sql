CREATE DATABASE relations

GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP ON relations.* TO RelationClient@localhost IDENTIFIED BY 'Client4Relations';

DROP TABLE IF EXISTS tblPerson;

CREATE TABLE tblPerson (
  PersonID	BIGINT unsigned not null auto_increment,
  sName		VARCHAR(99) not null,
  sFirstname	VARCHAR(50),
  sText		LONGTEXT,
  sFrom		VARCHAR(30),
  sTo		VARCHAR(30),
  dtCreation	timestamp not null,
  dtMutation	timestamp not null,
  PRIMARY KEY (PersonID),
  INDEX idxPerson_01 (sName, sFirstname),
  INDEX idxPerson_02 (sFrom, sTo)
);


DROP TABLE IF EXISTS tblRelation;

CREATE TABLE tblRelation (
  RelationID	BIGINT unsigned not null auto_increment,
  nType1	TINYINT not null,
  nItem1	BIGINT not null,
  nType2	TINYINT not null,
  nItem2	BIGINT not null,
  PRIMARY KEY (RelationID),
  INDEX idxRelation_01 (nType1, nItem1),
  INDEX idxRelation_02 (nType2, nItem2)
);

DROP TABLE IF EXISTS tblTerm;

CREATE TABLE tblTerm (
  TermID	BIGINT unsigned not null auto_increment,
  sTitle	VARCHAR(99) not null,
  sText		LONGTEXT,
  dtCreation	timestamp not null,
  dtMutation	timestamp not null,
  PRIMARY KEY (TermID),
  INDEX idxTerm_01 (sTitle)
);

DROP TABLE IF EXISTS tblText;

CREATE TABLE tblText (
  TextID	BIGINT unsigned not null auto_increment,
  sTitle	VARCHAR(150) not null,
  sText		LONGTEXT,
  sAuthor	VARCHAR(100),
  sCoAuthors	VARCHAR(150),
  sSubtitle	VARCHAR(300),
  sYear		VARCHAR(15),
  sPublication	VARCHAR(200),
  sPages	VARCHAR(20),
  nVolume	INT,
  nNumber	INT,
  sPublisher	VARCHAR(99),
  sPlace	VARCHAR(99),
  nType		INT,
  dtCreation	timestamp not null,
  dtMutation	timestamp not null,
  PRIMARY KEY (TextID),
  INDEX idxText_01 (sTitle),
  INDEX idxText_02 (sAuthor, sCoAuthors)
);