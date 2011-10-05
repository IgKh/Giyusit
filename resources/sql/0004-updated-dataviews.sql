--
-- Upgraded data views (Revision 4)
--

-- Patch the data views table
ALTER TABLE DataViews ADD SeqNo 		INTEGER;
ALTER TABLE DataViews ADD Description	VARCHAR;
ALTER TABLE DataViews ADD CreateTime 	VARCHAR;


UPDATE DataViews SET SeqNo = 1 WHERE ROWID = 1;
UPDATE DataViews SET Description = 'רשימת המועמדים הנמצאים בססטוס פעיל' 
	WHERE ROWID = 1;

UPDATE DataViews SET SeqNo = 5 WHERE ROWID = 2;
UPDATE DataViews SET Description = 'רשימת המועמדים שטרם עודכן הסטטוס שלהם' 
	WHERE ROWID = 2;

UPDATE DataViews SET SeqNo = 10 WHERE ROWID = 3;
UPDATE DataViews SET Description = 'רשימת המועמדים הפעילים שפרטיהם סומנו כשגויים' 
	WHERE ROWID = 3;

UPDATE DataViews SET SeqNo = 15 WHERE ROWID = 4;
UPDATE DataViews SET Description = 'רשימת המועמדים הפעילים שלא הוזנו עבורם פרטי יצירת קשר' 
	WHERE ROWID = 4;

UPDATE DataViews SET SeqNo = 20 WHERE ROWID = 5;
UPDATE DataViews SET Description = 'רשימת המועמדים הפעילים שלא הוגדר עבורם גורם סגל אחראי' 
	WHERE ROWID = 5;

UPDATE DataViews SET SeqNo = 25 WHERE ROWID = 6;
UPDATE DataViews SET Description = 'רשימת המועמדים הפעילים שהופעלה עבורם אינדיקציית חתם דח"ש' 
	WHERE ROWID = 6;

UPDATE DataViews SET SeqNo = 30 WHERE ROWID = 7;
UPDATE DataViews SET Description = 'רשימת המועמדים הפעילים שהופעלה עבורם אינדיקציית ביטל דח"ש' 
	WHERE ROWID = 7;

UPDATE DataViews SET SeqNo = 35 WHERE ROWID = 8;
UPDATE DataViews SET Description = 'רשימת המועמדים שהשתתפו בפועל באירוע מסוג שבת פתוחה' 
	WHERE ROWID = 8;

UPDATE DataViews SET SeqNo = 40 WHERE ROWID = 9;
UPDATE DataViews SET Description = 'רשימת המועמדים שהשתתפו בפועל באירוע מסוג אורחות' 
	WHERE ROWID = 9;

UPDATE DataViews SET SeqNo = 45 WHERE ROWID = 10;
UPDATE DataViews SET Description = 'רשימת כל המועמדים בקובץ, ללא קשר לסטטוס שלהם' 
	WHERE ROWID = 10;


UPDATE DataViews SET SeqNo = 100 WHERE ROWID = 11;
UPDATE DataViews SET Description = 'רשימת האירועים שתאריך התחילה שלהם הוא עתידי' 
	WHERE ROWID = 11;
	
UPDATE DataViews SET SeqNo = 105 WHERE ROWID = 12;
UPDATE DataViews SET Description = 'רשימת האירועים שתאריך הסיום שלהם עבר' 
	WHERE ROWID = 12;
	
UPDATE DataViews SET SeqNo = 110 WHERE ROWID = 13;
UPDATE DataViews SET Description = 'רשימת כל האירועים שנפתחו בקובץ' 
	WHERE ROWID = 13;

INSERT INTO DataViews(CategoryID, SeqNo, Title, Description, Query, Ruler) VALUES
(
	1, 24, 
	'לא חתמו דח"ש', 'רשימת המועמדים הפעילים שלא הופעלה עבורם אינדיקציית חתם דח"ש', 
	'select * from AllCandidates where ActiveInd = "true" and (SignedDahashInd isnull or SignedDahashInd = "false")', 
	'@StdCandidatesRuler'
);
