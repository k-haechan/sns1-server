package com.mysite.sns1_server.domain.follow.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sns1_server.domain.follow.entity.Follow;
import com.mysite.sns1_server.domain.follow.type.FollowStatus;
import com.mysite.sns1_server.domain.member.entity.Member;

public interface FollowRepository extends JpaRepository<Follow,Long> {
	@EntityGraph(attributePaths = {"following", "follower"})
	Slice<Follow> findByFollowerAndStatus(Member follower, FollowStatus status, Pageable pageable);
}
