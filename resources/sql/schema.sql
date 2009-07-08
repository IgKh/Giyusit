--
-- schema.sql - Database schema for Giyusit
--
-- Revision 3
--
PRAGMA encoding = 'UTF-8';

--
-- System tables
--
CREATE TABLE FileParams
(
	Key		VARCHAR UNIQUE NOT NULL,
	Value	VARCHAR
);

INSERT INTO FileParams (Key, Value) VALUES ('SchemaRevision', '3');
INSERT INTO FileParams (Key, Value) VALUES ('DefaultCandidateStatus', '1');
INSERT INTO FileParams (Key, Value) VALUES ('DefaultAttendaceType', '2');

--
CREATE TABLE DataViews
(
	Title			VARCHAR NOT NULL,
	CategoryID		INTEGER NOT NULL,
	Query			VARCHAR NOT NULL,
	Ruler			VARCHAR NOT NULL,
	UserInd     	BOOLEAN DEFAULT 'false'
);

CREATE TABLE DataViewCategories
(
    ID          INTEGER PRIMARY KEY,
    Name        VARCHAR NOT NULL
);

INSERT INTO DataViewCategories (ID, Name) VALUES (1, 'מועמדים');
INSERT INTO DataViewCategories (ID, Name) VALUES (2, 'אירועים');

--
CREATE TABLE RulerLibrary
(
	Name	VARCHAR NOT NULL,
	Ruler	VARCHAR NOT NULL
);

--
CREATE TABLE BooleanValues 
(
	Key		VARCHAR,
	Value	VARCHAR
);

INSERT INTO BooleanValues (Key, Value) VALUES ('true', 'כן');
INSERT INTO BooleanValues (Key, Value) VALUES ('false', 'לא');

--
-- Candidate tables
--
--
CREATE TABLE Candidates
(
	ID				INTEGER PRIMARY KEY,
	NationalID 		INTEGER,
	FirstName 		VARCHAR NOT NULL ON CONFLICT IGNORE,
	LastName 		VARCHAR,
	Gender 			VARCHAR,
	Address 		VARCHAR,
	City 			VARCHAR,
	ZipCode 		VARCHAR,
	HomePhone		VARCHAR,
	CellPhone		VARCHAR,
	EMail 			VARCHAR,
	WrongDetailsInd	BOOLEAN,

	Origin 			VARCHAR,
	School			VARCHAR,
	Notes 			VARCHAR,
	OwnerID 		INTEGER,
	RecruiterID 	INTEGER,
	StatusID 		INTEGER,

	CanceledDahashInd	BOOLEAN,
	SignedDahashInd 	BOOLEAN
);

CREATE TRIGGER OnCandidateInsert AFTER INSERT ON Candidates
WHEN NEW.StatusID IS NULL
BEGIN
	UPDATE Candidates SET StatusID =
        	(SELECT Value FROM FileParams WHERE Key = 'DefaultCandidateStatus')
        	WHERE ID = NEW.ID;

	INSERT INTO CandidateStatuses (CandidateID, PriStatusID)
		VALUES (NEW.ID, (SELECT Value FROM FileParams WHERE Key = 'DefaultCandidateStatus'));
END;

CREATE TRIGGER OnCandidateDelete AFTER DELETE ON Candidates
BEGIN
	DELETE FROM CandidateStatuses WHERE CandidateID = OLD.ID;
	DELETE FROM EventAttendance WHERE CandidateID = OLD.ID;
END;

--
CREATE TABLE CandidateStatuses
(
	ID 				INTEGER PRIMARY KEY,
	CandidateID 	INTEGER NOT NULL,
	PriStatusID		INTEGER NOT NULL,
	SecStatusID		INTEGER,
	StartTime		TIMESTAMP DEFAULT CURRENT_DATE,
	
	UNIQUE(CandidateID, StartTime)
);

CREATE TRIGGER OnCStatusInsert AFTER INSERT ON CandidateStatuses
BEGIN
	UPDATE Candidates SET StatusID =
        	(SELECT PriStatusID FROM CandidateStatuses 
			WHERE CandidateID = NEW.CandidateID ORDER BY StartTime DESC)
        	WHERE ID = NEW.CandidateID;
END;

CREATE TRIGGER OnCStatusDelete AFTER DELETE ON CandidateStatuses
BEGIN
	UPDATE Candidates SET StatusID =
        	(SELECT PriStatusID FROM CandidateStatuses 
			WHERE CandidateID = OLD.CandidateID ORDER BY StartTime DESC)
        	WHERE ID = OLD.CandidateID;
END;

--
CREATE TABLE CandidateStatusValues
(
	ID 			INTEGER PRIMARY KEY,
	Name		VARCHAR NOT NULL,
	
	ActiveInd 	BOOLEAN DEFAULT 'true',
	EndDate		TIMESTAMP
);


INSERT INTO CandidateStatusValues (ID, Name) VALUES (1, 'חדש');

--
-- Event tables
--
--
CREATE TABLE Events
(
	ID 			INTEGER PRIMARY KEY,
	TypeID      INTEGER NOT NULL,
	OwnerID     INTEGER,

	Name 		VARCHAR NOT NULL,
	Location 	VARCHAR,
	StartDate 	DATE,
    EndDate     DATE,
    Notes       VARCHAR
);

CREATE TRIGGER OnEventDelete AFTER DELETE ON Events
BEGIN
	DELETE FROM EventAttendance WHERE EventID = OLD.ID;
END;

--
CREATE TABLE EventTypes
(
	ID		INTEGER PRIMARY KEY,
	Name	VARCHAR
);

INSERT INTO EventTypes (Name) VALUES ('שבת פתוחה');
INSERT INTO EventTypes (Name) VALUES ('אורחות');
INSERT INTO EventTypes (Name) VALUES ('כנס מחויבות');
INSERT INTO EventTypes (Name) VALUES ('טיול פסח');
INSERT INTO EventTypes (Name) VALUES ('שבת גיבוש');
INSERT INTO EventTypes (Name) VALUES ('אחר');

--
CREATE TABLE EventAttendance
(
	ID			INTEGER PRIMARY KEY,
	CandidateID	INTEGER NOT NULL,
	EventID		INTEGER NOT NULL,
	AttTypeID	INTEGER,
	Notes		VARCHAR,

	UNIQUE (CandidateID, EventID) ON CONFLICT IGNORE
);

CREATE TRIGGER OnEAttendanceInsert AFTER INSERT ON EventAttendance
WHEN NEW.AttTypeID IS NULL
BEGIN
	UPDATE EventAttendance SET AttTypeID =
        	(SELECT Value FROM FileParams WHERE Key = 'DefaultAttendaceType')
        	WHERE ID = NEW.ID;
END;

--
CREATE TABLE AttendanceTypes
(
	ID			INTEGER PRIMARY KEY,
	Name		VARCHAR NOT NULL,
	ActiveInd	BOOLEAN DEFAULT 'true'
);

INSERT INTO AttendanceTypes (ID, Name, ActiveInd) VALUES (1, 'פוטנציאל מחושב', 'true');
INSERT INTO AttendanceTypes (ID, Name, ActiveInd) VALUES (2, 'מעוניין להשתתף', 'true');
INSERT INTO AttendanceTypes (ID, Name, ActiveInd) VALUES (3, 'אישר השתתפות', 'true');
INSERT INTO AttendanceTypes (ID, Name, ActiveInd) VALUES (4, 'השתתף בפועל', 'true');
INSERT INTO AttendanceTypes (ID, Name, ActiveInd) VALUES (5, 'לא מעוניין להשתתף', 'false');
INSERT INTO AttendanceTypes (ID, Name, ActiveInd) VALUES (6, 'כנראה לא ישתתף', 'false');
INSERT INTO AttendanceTypes (ID, Name, ActiveInd) VALUES (7, 'לא השתתף בפועל', 'false');
INSERT INTO AttendanceTypes (ID, Name, ActiveInd) VALUES (8, 'ביטל השתתפות', 'false');

--
-- Staff tables
--
--
CREATE TABLE Staff
(
	ID 			INTEGER PRIMARY KEY,
	ParentID	INTEGER,
	Name 		VARCHAR NOT NULL,
	Role 		VARCHAR,
	RealInd		BOOLEAN DEFAULT 'true'
);

CREATE TRIGGER OnStaffDelete AFTER DELETE ON Staff 
BEGIN
	-- Set owner of owned candidates and events to null
	UPDATE Candidates SET OwnerID = NULL WHERE OwnerID = OLD.ID;
	UPDATE Candidates SET RecruiterID = NULL WHERE RecruiterID = OLD.ID;

	UPDATE Events SET OwnerID = NULL WHERE OwnerID = OLD.ID;

	-- Re-parent children
	UPDATE Staff SET ParentID = OLD.ParentID WHERE ParentID = OLD.ID;
END;

--
-- Helper views
--
CREATE VIEW AllCandidates AS SELECT 
				C.*, 
				CSV.Name AS Status, 
				CSV.ActiveInd AS ActiveInd,
				Staff.Name AS Owner,
				(C.FirstName || coalesce(' ' || C.LastName, '')) AS FullName
			FROM Candidates C
			LEFT OUTER JOIN CandidateStatusValues CSV ON C.StatusID = CSV.ID
			LEFT OUTER JOIN Staff ON C.OwnerID = Staff.ID;

CREATE VIEW StatusesView AS SELECT 
		CS.*, 
		strftime('%d/%m/%Y', CS.StartTime) AS StartDate,
		CSV.Name AS StatusName 
	FROM CandidateStatuses CS, CandidateStatusValues CSV 
	WHERE CS.PriStatusID = CSV.ID
	ORDER BY CS.StartTime DESC;

CREATE VIEW EventAttendanceCount AS SELECT
		EventID,
		count(*) AS Attendants
	FROM EventAttendance 
	GROUP BY EventID;
	
CREATE VIEW EventActiveAttendanceCount AS SELECT
		EventID,
		count(*) AS Attendants
	FROM EventAttendance 
	WHERE AttTypeID IN (SELECT ID FROM AttendanceTypes WHERE ActiveInd = 'true')
	GROUP BY EventID;

CREATE VIEW AllEvents AS SELECT
		E.ID AS ID,
		E.TypeID AS TypeID,
		E.OwnerID AS OwnerID,
		E.Name AS Name,
		E.Location AS Location,
		E.Notes AS Notes,
		E.StartDate AS ISOStartDate,
		E.EndDate AS ISOEndDate,
		--
		strftime('%d/%m/%Y', E.StartDate) AS StartDate,
		strftime('%d/%m/%Y', E.EndDate) AS EndDate,
		coalesce(EAAC.Attendants, 0) AS ActiveAttendants,
		EventTypes.Name AS Type,
		Staff.Name AS Owner
	FROM Events E
	LEFT OUTER JOIN EventActiveAttendanceCount EAAC ON E.ID = EAAC.EventID
	LEFT OUTER JOIN EventTypes ON E.TypeID = EventTypes.ID
	LEFT OUTER JOIN Staff ON E.OwnerID = Staff.ID;

CREATE VIEW EventCandidatesView AS SELECT
		EA.CandidateID AS ID,
		EA.EventID,
		EA.Notes,
		C.FirstName,
		C.LastName,
		C.Gender,
		C.Address,
		C.City,
		C.ZipCode,
		C.EMail,
		AT.Name AS AttType,
		AT.ActiveInd,
		BV.Value AS Active
	FROM EventAttendance EA
	LEFT OUTER JOIN Candidates C ON EA.CandidateID = C.ID
	LEFT OUTER JOIN AttendanceTypes AT ON EA.AttTypeID = AT.ID
	LEFT OUTER JOIN BooleanValues BV ON AT.ActiveInd = BV.Key;
	

