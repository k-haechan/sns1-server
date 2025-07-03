CREATE TABLE comment
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime     NOT NULL,
    updated_at       datetime     NOT NULL,
    content          VARCHAR(255) NOT NULL,
    like_count       BIGINT       NOT NULL,
    re_comment_count BIGINT       NOT NULL,
    post_id          BIGINT       NOT NULL,
    author_id        BIGINT       NOT NULL,
    CONSTRAINT pk_comment PRIMARY KEY (id)
);

CREATE TABLE comment_like
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NOT NULL,
    liker_id   BIGINT   NOT NULL,
    comment_id BIGINT   NOT NULL,
    CONSTRAINT pk_commentlike PRIMARY KEY (id)
);

CREATE TABLE follow
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   datetime NOT NULL,
    updated_at   datetime NOT NULL,
    requester_id BIGINT   NOT NULL,
    target_id    BIGINT   NOT NULL,
    status       VARCHAR(255) NULL,
    CONSTRAINT pk_follow PRIMARY KEY (id)
);

CREATE TABLE image
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime     NOT NULL,
    updated_at datetime     NOT NULL,
    url        VARCHAR(255) NOT NULL,
    post_id    BIGINT       NOT NULL,
    CONSTRAINT pk_image PRIMARY KEY (id)
);

CREATE TABLE member
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    created_at        datetime     NOT NULL,
    updated_at        datetime     NOT NULL,
    username          VARCHAR(50)  NOT NULL,
    password          VARCHAR(100) NOT NULL,
    real_name         VARCHAR(50)  NOT NULL,
    email             VARCHAR(100) NOT NULL,
    phone             VARCHAR(15)  NOT NULL,
    profile_image_url VARCHAR(255) NULL,
    introduction      VARCHAR(255) NULL,
    CONSTRAINT pk_member PRIMARY KEY (id)
);

CREATE TABLE post
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime      NOT NULL,
    updated_at datetime      NOT NULL,
    title      VARCHAR(500)  NOT NULL,
    content    VARCHAR(2000) NOT NULL,
    like_count BIGINT        NOT NULL,
    author_id  BIGINT        NOT NULL,
    CONSTRAINT pk_post PRIMARY KEY (id)
);

CREATE TABLE post_like
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NOT NULL,
    liker_id   BIGINT   NOT NULL,
    post_id    BIGINT   NOT NULL,
    CONSTRAINT pk_postlike PRIMARY KEY (id)
);

CREATE TABLE post_save
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NOT NULL,
    member_id  BIGINT   NOT NULL,
    post_id    BIGINT   NOT NULL,
    CONSTRAINT pk_postsave PRIMARY KEY (id)
);

CREATE TABLE re_comment
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime     NOT NULL,
    updated_at datetime     NOT NULL,
    content    VARCHAR(255) NOT NULL,
    like_count BIGINT       NOT NULL,
    author_id  BIGINT       NOT NULL,
    comment_id BIGINT       NOT NULL,
    CONSTRAINT pk_recomment PRIMARY KEY (id)
);

CREATE TABLE re_comment_like
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    created_at   datetime NOT NULL,
    updated_at   datetime NOT NULL,
    liker_id     BIGINT   NOT NULL,
    recomment_id BIGINT   NOT NULL,
    CONSTRAINT pk_recommentlike PRIMARY KEY (id)
);

ALTER TABLE member
    ADD CONSTRAINT uc_member_email UNIQUE (email);

ALTER TABLE member
    ADD CONSTRAINT uc_member_phone UNIQUE (phone);

ALTER TABLE member
    ADD CONSTRAINT uc_member_username UNIQUE (username);

ALTER TABLE comment_like
    ADD CONSTRAINT FK_COMMENTLIKE_ON_COMMENT FOREIGN KEY (comment_id) REFERENCES comment (id);

ALTER TABLE comment_like
    ADD CONSTRAINT FK_COMMENTLIKE_ON_LIKER FOREIGN KEY (liker_id) REFERENCES member (id);

ALTER TABLE comment
    ADD CONSTRAINT FK_COMMENT_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES member (id);

ALTER TABLE comment
    ADD CONSTRAINT FK_COMMENT_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE follow
    ADD CONSTRAINT FK_FOLLOW_ON_REQUESTER FOREIGN KEY (requester_id) REFERENCES member (id);

ALTER TABLE follow
    ADD CONSTRAINT FK_FOLLOW_ON_TARGET FOREIGN KEY (target_id) REFERENCES member (id);

ALTER TABLE image
    ADD CONSTRAINT FK_IMAGE_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE post_like
    ADD CONSTRAINT FK_POSTLIKE_ON_LIKER FOREIGN KEY (liker_id) REFERENCES member (id);

ALTER TABLE post_like
    ADD CONSTRAINT FK_POSTLIKE_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE post_save
    ADD CONSTRAINT FK_POSTSAVE_ON_MEMBER FOREIGN KEY (member_id) REFERENCES member (id);

ALTER TABLE post_save
    ADD CONSTRAINT FK_POSTSAVE_ON_POST FOREIGN KEY (post_id) REFERENCES post (id);

ALTER TABLE post
    ADD CONSTRAINT FK_POST_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES member (id);

ALTER TABLE re_comment_like
    ADD CONSTRAINT FK_RECOMMENTLIKE_ON_LIKER FOREIGN KEY (liker_id) REFERENCES member (id);

ALTER TABLE re_comment_like
    ADD CONSTRAINT FK_RECOMMENTLIKE_ON_RECOMMENT FOREIGN KEY (recomment_id) REFERENCES re_comment (id);

ALTER TABLE re_comment
    ADD CONSTRAINT FK_RECOMMENT_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES member (id);

ALTER TABLE re_comment
    ADD CONSTRAINT FK_RECOMMENT_ON_COMMENT FOREIGN KEY (comment_id) REFERENCES comment (id);
