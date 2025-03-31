-- add audio_file_id column to tracks table to link tracks with audio files.

ALTER TABLE tracks
ADD COLUMN audio_file_id BIGINT;

ALTER TABLE tracks
ADD CONSTRAINT fk_audio_file
FOREIGN KEY (audio_file_id)
REFERENCES audio_files(id)
ON DELETE SET NULL;
