package com.mysite.sns1_server.domain.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sns1_server.domain.comment.entity.Comment;
import com.mysite.sns1_server.domain.post.entity.Post;

public interface CommentRepository extends JpaRepository<Comment,Long> {
	@EntityGraph(attributePaths = {"author", "post"}, type = EntityGraph.EntityGraphType.FETCH)
	Slice<Comment> findByPostOrderByIdDesc(Post post, Pageable pageable);
}
