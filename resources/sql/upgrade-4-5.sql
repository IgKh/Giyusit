--
-- Upgrade script from schema revision 4 to revision 5
--

-- Update schema revision
UPDATE FileParams SET Value = '5' WHERE Key = 'SchemaRevision';

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
	'select ET.Name as Type, count(*) as Total from EventAttendance EA, Events E, EventTypes ET 
	 where EA.EventID = E.ID and ET.ID = E.TypeID and EA.AttTypeID = 4 group by Type order by ET.ID',
	'Type,Total'
);

INSERT INTO StatisticReports (Name, Description, Class, Query, Ruler) VALUES (
	'השתתפות באירועים (כולל צפי)',
	'סך כל המועמדים שהשתתפו או צפויים להשתתף באירועים מכל סוג',
	'negev.giyusit.statistics.BarReport',
	'select ET.Name as Type, count(*) as Total from EventAttendance EA, AttendanceTypes AT, Events E, EventTypes ET 
	 where EA.EventID = E.ID and AT.ID = EA.AttTypeId and ET.ID = E.TypeID and AT.ActiveInd = "true" 
	 group by Type order by ET.ID',
	'Type,Total'
);
