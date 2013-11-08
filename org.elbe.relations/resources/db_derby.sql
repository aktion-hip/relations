<?xml version="1.0" encoding="UTF-8"?>
<SQLPage nameConnection="default_db">
<Statement><![CDATA[-- C:\Data\eclipse\Relations-RCP\data\default_db:

DROP SCHEMA newrelations restrict

CREATE TABLE tblPerson (

  PersonID	BIGINT generated always as identity,

  sName		VARCHAR(99) not null,

  sFirstname	VARCHAR(50),

  sText		CLOB,

  sFrom		VARCHAR(30),

  sTo		VARCHAR(30),

  dtCreation	TIMESTAMP not null,

  dtMutation	TIMESTAMP not null,

  PRIMARY KEY (PersonID)

);

CREATE INDEX idxPerson_01 ON tblPerson(sName, sFirstname);

CREATE INDEX idxPerson_02 ON tblPerson(sFrom, sTo);


CREATE TABLE tblRelation (

  RelationID	BIGINT generated always as identity,

  nType1	SMALLINT not null,

  nItem1	BIGINT not null,

  nType2	SMALLINT not null,

  nItem2	BIGINT not null,

  PRIMARY KEY (RelationID)

);

CREATE INDEX idxRelation_01 ON tblRelation(nType1, nItem1);
CREATE INDEX idxRelation_02 ON tblRelation(nType2, nItem2);

CREATE TABLE tblTerm (

  TermID	BIGINT generated always as identity,

  sTitle	VARCHAR(99) not null,

  sText		CLOB,

  dtCreation	TIMESTAMP not null,

  dtMutation	TIMESTAMP not null,

  PRIMARY KEY (TermID)

);

CREATE INDEX idxTerm_01 ON tblTerm(sTitle);


CREATE TABLE tblText (

  TextID	BIGINT generated always as identity,

  sTitle	VARCHAR(150) not null,

  sText		CLOB,

  sAuthor	VARCHAR(100),

  sCoAuthors	VARCHAR(150),

  sSubtitle	VARCHAR(200),

  sYear		VARCHAR(15),

  sPublication	VARCHAR(100),

  sPages	VARCHAR(20),

  nVolume	INT,

  nNumber	INT,

  sPublisher	VARCHAR(99),

  sPlace	VARCHAR(99),

  nType		INT,

  dtCreation	TIMESTAMP not null,

  dtMutation	TIMESTAMP not null,

  PRIMARY KEY (TextID)

);


CREATE INDEX idxText_01 ON tblText(sTitle);

CREATE INDEX idxText_02 ON tblText(sAuthor, sCoAuthors);


drop index idxPerson_01;
drop index idxPerson_02;
drop index idxTerm_01;
drop index idxText_01;
drop index idxText_02;

drop table tblPerson;
drop table tblTerm;
drop table tblText;
drop table tblRelation;]]></Statement>
</SQLPage>
