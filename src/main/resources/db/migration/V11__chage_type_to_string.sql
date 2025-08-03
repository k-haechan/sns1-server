ALTER TABLE notification
DROP
COLUMN type;

ALTER TABLE notification
    ADD type VARCHAR(255) NULL;
