CREATE TABLE chat_room
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime NOT NULL,
    updated_at      datetime NOT NULL,
    lower_member_id BIGINT NULL,
    upper_member_id BIGINT NULL,
    CONSTRAINT pk_chatroom PRIMARY KEY (id)
);
