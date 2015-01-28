\j skidefault def2pass jdbc:hsqldb:hsql://127.0.0.1:60000/skidummy
CREATE SCHEMA skidummy;
CREATE TABLE skidummy.eventinfo (
	id IDENTITY,
	first  VARCHAR(254) NOT NULL,
	last   VARCHAR(254),
	cell   VARCHAR(30),
	email  VARCHAR(254),
	skiday DATE,
	resort VARCHAR(254),
	pref   VARCHAR(254),
	skill  VARCHAR(254)
);

\j skidefault def2pass jdbc:hsqldb:hsql://127.0.0.1:60000/skiprod
CREATE SCHEMA skiprod;
CREATE TABLE skiprod.eventinfo (
	id IDENTITY,
	first  VARCHAR(254) NOT NULL,
	last   VARCHAR(254),
	cell   VARCHAR(30),
	email  VARCHAR(254),
	skiday DATE,
	resort VARCHAR(254),
	pref   VARCHAR(254),
	skill  VARCHAR(254)
);