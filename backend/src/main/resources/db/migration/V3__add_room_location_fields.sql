ALTER TABLE rooms ADD COLUMN building_name VARCHAR(255);
ALTER TABLE rooms ADD COLUMN floor INTEGER;

UPDATE rooms
SET building_name = 'Main Building', floor = 1
WHERE number = '101';

UPDATE rooms
SET building_name = 'Main Building', floor = 2
WHERE number = '204';

UPDATE rooms
SET building_name = 'Science Building', floor = 3
WHERE number = '305';

UPDATE rooms
SET building_name = 'Science Building', floor = 4
WHERE number = '410';
