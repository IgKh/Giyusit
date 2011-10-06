--
-- New candidate fields (Revision 6)
--

-- Add new fields to candidate table
ALTER TABLE Candidates ADD COLUMN SubOrigin VARCHAR;
ALTER TABLE Candidates ADD COLUMN Page VARCHAR;

-- Patch AllCandidates view to expose city area function
DROP VIEW AllCandidates;
CREATE VIEW AllCandidates AS SELECT
				C.*,
				CSV.Name AS Status,
				CSV.ActiveInd AS ActiveInd,
				Staff.Name AS Owner,
				(C.FirstName || coalesce(' ' || C.LastName, '')) AS FullName,
				getCityArea(C.City) AS CityArea
			FROM Candidates C
			LEFT OUTER JOIN CandidateStatusValues CSV ON C.StatusID = CSV.ID
			LEFT OUTER JOIN Staff ON C.OwnerID = Staff.ID;

-- Update candidate ruler with new fields
UPDATE RulerLibrary SET Ruler = "ID*,FirstName,LastName,Gender,City,Status,Owner,Notes,NationalID+,Address+,ZipCode+,CityArea+,HomePhone+,CellPhone+,EMail+,Origin+,SubOrigin+,Page+,School+,ActiveInd+,SignedDahashInd+,CanceledDahashInd+,FullName+" WHERE Name = "StdCandidatesRuler";

