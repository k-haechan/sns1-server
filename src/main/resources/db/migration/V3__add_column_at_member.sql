ALTER TABLE member
    ADD follower_count BIGINT NULL;

ALTER TABLE member
    ADD following_count BIGINT NULL;

ALTER TABLE member
    ADD is_secret BIT(1) NULL;

ALTER TABLE member
    MODIFY follower_count BIGINT NOT NULL;

ALTER TABLE member
    MODIFY following_count BIGINT NOT NULL;
