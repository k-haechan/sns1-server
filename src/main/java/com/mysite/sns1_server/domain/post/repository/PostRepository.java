package com.mysite.sns1_server.domain.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post,Long> {
	Slice<Post> findByAuthorOrderByIdDesc(Member author, Pageable pageable);

	Slice<Post> findByAuthorAndIdLessThanOrderByIdDesc(Member author, Long id, Pageable pageable);
}
