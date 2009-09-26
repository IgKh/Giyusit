--
-- Upgrade script from schema revision 4 to revision 5
--

-- Update schema revision
UPDATE FileParams SET Value = '5' WHERE Key = 'SchemaRevision';

-- Patch standard rulers with marker characters
UPDATE RulerLibrary SET Ruler = "ID*,FirstName,LastName,Gender,City,Status,Owner,Notes" WHERE Name = "StdCandidatesRuler";
UPDATE RulerLibrary SET Ruler = "ID*,Name,Type,StartDate,EndDate,ActiveAttendants,Location,Owner,Notes" WHERE Name = "StdEventsRuler";

-- Helper view for event attendance
CREATE VIEW EventAttendanceHelper AS SELECT 
				EA.*, 
				E.TypeID AS EventTypeID, 
				ET.Name AS EventType,
				AT.Name AS AttType,
				AT.ActiveInd AS ActiveInd
			FROM EventAttendance EA
			JOIN AttendanceTypes AT ON EA.AttTypeID = AT.ID
			JOIN Events E ON EA.EventID = E.ID
			JOIN EventTypes ET ON E.TypeID = ET.ID;

-- Create the goals table
CREATE TABLE Goals
(
	ID			INTEGER PRIMARY KEY,
	Name		VARCHAR NOT NULL,
	Planning	INTEGER DEFAULT 0,
	ExecQuery	VARCHAR NOT NULL
);

INSERT INTO Goals (Name, ExecQuery) VALUES ('סה"כ מועמדים',
	'select count(*) from Candidates'
);

INSERT INTO Goals (Name, ExecQuery) VALUES ('מועמדים שהשתתפו בשבת',
	'select count(*) from EventAttendanceHelper where AttTypeID = 4 and EventTypeID = 1'
);

-- Create the Statistic Report table
CREATE TABLE StatisticReports
(
	ID			INTEGER PRIMARY KEY,
	Name		VARCHAR NOT NULL,
	Class		VARCHAR NOT NULL,
	Description	VARCHAR,
	Query		VARCHAR,
	Ruler		VARCHAR,
	CreateDate	VARCHAR DEFAULT CURRENT_DATE
);

INSERT INTO StatisticReports (Name, Description, Class, Ruler) VALUES (
	'עמידה ביעדים',
	'השוואת תכנון מול ביצוע של היעדים שהוגדרו בקובץ',
	'negev.giyusit.statistics.GoalsReport',
	'Name,Planning,Execution'
);

INSERT INTO StatisticReports (Name, Description, Class, Query, Ruler) VALUES (
	'מועמדים פעילים לפי ישוב',
	'מספר המועמדים הפעילים המתגוררים בכל ישוב',
	'negev.giyusit.statistics.PieReport',
	'select City, count(*) as Total from AllCandidates where ActiveInd = "true" group by City order by Total desc',
	'City,Total'
);

INSERT INTO StatisticReports (Name, Description, Class, Query, Ruler) VALUES (
	'מועמדים פעילים לפי מקור',
	'מספר המועמדים הפעילים שגויסו מכל מקור',
	'negev.giyusit.statistics.PieReport',
	'select Origin, count(*) as Total from AllCandidates where ActiveInd = "true" group by Origin order by Total desc',
	'Origin,Total'
);

INSERT INTO StatisticReports (Name, Description, Class, Query, Ruler) VALUES (
	'מועמדים לפי ישוב',
	'מספר המועמדים (ללא קשר לסטטוס) המתגוררים בכל ישוב',
	'negev.giyusit.statistics.PieReport',
	'select City, count(*) as Total from Candidates group by City order by Total desc',
	'City,Total'
);

INSERT INTO StatisticReports (Name, Description, Class, Query, Ruler) VALUES (
	'מועמדים לפי מקור',
	'מספר המועמדים (ללא קשר לסטטוס) שגויסו מכל מקור',
	'negev.giyusit.statistics.PieReport',
	'select Origin, count(*) as Total from Candidates group by Origin order by Total desc',
	'Origin,Total'
);

INSERT INTO StatisticReports (Name, Description, Class, Query, Ruler) VALUES (
	'השתתפות באירועים',
	'סך כל המועמדים שהשתתפו בפועל באירועים מכל סוג',
	'negev.giyusit.statistics.BarReport',
	'select EAH.EventType as Type, count(*) as Total from EventAttendanceHelper EAH 
	 where EAH.AttTypeID = 4 group by Type order by EAH.EventTypeID',
	'Type,Total'
);

INSERT INTO StatisticReports (Name, Description, Class, Query, Ruler) VALUES (
	'השתתפות באירועים (כולל צפי)',
	'סך כל המועמדים שהשתתפו או צפויים להשתתף באירועים מכל סוג',
	'negev.giyusit.statistics.BarReport',
	'select EAH.EventType as Type, count(*) as Total from EventAttendanceHelper EAH 
	 where EAH.ActiveInd = "true" group by Type order by EAH.EventTypeID',
	'Type,Total'
);
