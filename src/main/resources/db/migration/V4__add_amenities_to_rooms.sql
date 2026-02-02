-- Add amenities_json column to rooms table
ALTER TABLE rooms ADD COLUMN amenities_json TEXT COMMENT '편의시설 정보 (JSON 형태)';

-- Update existing records with empty JSON array
UPDATE rooms SET amenities_json = '[]' WHERE amenities_json IS NULL;

