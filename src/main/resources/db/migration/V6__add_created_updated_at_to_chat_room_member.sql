ALTER TABLE chat_room_member
    ADD created_at datetime NULL;

ALTER TABLE chat_room_member
    ADD updated_at datetime NULL;

ALTER TABLE chat_room_member
    MODIFY created_at datetime NOT NULL;

ALTER TABLE chat_room_member
    MODIFY updated_at datetime NOT NULL;
