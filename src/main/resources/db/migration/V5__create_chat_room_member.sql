CREATE TABLE chat_room_member
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    member_id    BIGINT NULL,
    chat_room_id BIGINT NULL,
    CONSTRAINT pk_chatroommember PRIMARY KEY (id)
);

ALTER TABLE chat_room_member
    ADD CONSTRAINT FK_CHATROOMMEMBER_ON_CHATROOM FOREIGN KEY (chat_room_id) REFERENCES chat_room (id);

ALTER TABLE chat_room_member
    ADD CONSTRAINT FK_CHATROOMMEMBER_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE chat_room
DROP
COLUMN lower_member_id;

ALTER TABLE chat_room
DROP
COLUMN upper_member_id;
