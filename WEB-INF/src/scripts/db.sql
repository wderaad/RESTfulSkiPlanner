\j dummy dp4hsdb jdbc:hsqldb:hsql://127.0.0.1:60000/dummy
CREATE SCHEMA dummy;
CREATE TABLE dummy.contact (
	id IDENTITY,
	first VARCHAR(254) NOT NULL,
	last VARCHAR(254),
	cell VARCHAR(30),
	email VARCHAR(254),
	birthday date
);

\j dummy dp4hsdb jdbc:hsqldb:hsql://127.0.0.1:60000/prod
CREATE SCHEMA prod;
CREATE TABLE prod.contact (
	id IDENTITY,
	first VARCHAR(254) NOT NULL,
	last VARCHAR(254),
	cell VARCHAR(30),
	email VARCHAR(254),
	birthday date
);