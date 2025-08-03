
CREATE TABLE notification
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NOT NULL,
    message    VARCHAR(255) NULL,
    member_id  BIGINT NULL,
    is_read     BIT(1)   NOT NULL,
    type       SMALLINT NULL,
    sub_id     BIGINT NULL,
    CONSTRAINT pk_notification PRIMARY KEY (id)
);


ALTER TABLE notification
    ADD CONSTRAINT FK_NOTIFICATION_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

