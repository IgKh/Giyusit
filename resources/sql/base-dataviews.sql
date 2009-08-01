--
-- Factory data views and stored rulers for Giyusit
--

-- Rulers
INSERT INTO RulerLibrary (Name, Ruler) VALUES
(
	"StdCandidatesRuler",
	"ID,FirstName,LastName,Gender,City,Status,Owner,Notes"
);

INSERT INTO RulerLibrary (Name, Ruler) VALUES
(
	"StdEventsRuler",
	"ID,Name,Type,StartDate,EndDate,ActiveAttendants,Location,Owner,Notes"
);

-- Candidate DataViews

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
	1, 'מועמדים פעילים', 
	'select * from AllCandidates where ActiveInd = "true"', 
	'@StdCandidatesRuler'
);

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
		1, 'מועמדים בססטוס ראשוני', 
	'select * from AllCandidates where StatusID = (select Value from FileParams where Key = "DefaultCandidateStatus")', 
	'@StdCandidatesRuler'
);

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
		1, 'בעלי פרטים שגויים', 
	'select * from AllCandidates where ActiveInd = "true" and WrongDetailsInd = "true"', 
	'@StdCandidatesRuler'
);

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
	1, 'ללא פרטי קשר', 
	'select * from AllCandidates where ActiveInd = "true" and HomePhone isnull and CellPhone isnull and EMail isnull', 
	'@StdCandidatesRuler'
);

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
	1, 'ללא אחראי', 
	'select * from AllCandidates where ActiveInd = "true" and OwnerID isnull', 
	'@StdCandidatesRuler'
);

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
	1, 'חתומי דח"ש', 
	'select * from AllCandidates where ActiveInd = "true" and SignedDahashInd = "true" and CanceledDahashInd <> "true"', 
	'@StdCandidatesRuler'
);

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
	1, 'ביטלו דח"ש', 
	'select * from AllCandidates where ActiveInd = "true" and CanceledDahashInd = "true"', 
	'@StdCandidatesRuler'
);

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
	1, 'היו בשבת פתוחה', 
	'select * from AllCandidates AC where
	 	(select count(*) from EventAttendance EA, Events E
	 	 where EA.EventID = E.ID and E.TypeID = 1 and EA.AttTypeID = 4 and EA.CandidateID = AC.ID)', 
	'@StdCandidatesRuler'
);

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
	1, 'היו באורחות', 
	'select * from AllCandidates AC where
	 	(select count(*) from EventAttendance EA, Events E
	 	 where EA.EventID = E.ID and E.TypeID = 2 and EA.AttTypeID = 4 and EA.CandidateID = AC.ID)', 
	'@StdCandidatesRuler'
);

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
		1, 'כל המועמדים', 
	'select * from AllCandidates', 
	'@StdCandidatesRuler'
);

-- Event DataViews

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
		2, 'אירועים עתידיים', 
	'select * from AllEvents where ISOStartDate > date("now")', 
	'@StdEventsRuler'
);

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
		2, 'אירועים שהסתיימו', 
	'select * from AllEvents where ISOEndDate < date("now")', 
	'@StdEventsRuler'
);

INSERT INTO DataViews(CategoryID, Title, Query, Ruler) VALUES
(
		2, 'כל האירועים', 
	'select * from AllEvents', 
	'@StdEventsRuler'
);

