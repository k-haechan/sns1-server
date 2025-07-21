package com.mysite.sns1_server.domain.image.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sns1_server.domain.image.entity.Image;
import com.mysite.sns1_server.domain.post.entity.Post;

public interface ImageRepository extends JpaRepository<Image,Long> {
	List<Image> findByPost(Post post);
}
