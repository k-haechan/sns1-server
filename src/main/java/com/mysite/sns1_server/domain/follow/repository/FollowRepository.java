package com.mysite.sns1_server.domain.follow.repository;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mysite.sns1_server.domain.follow.entity.Follow;
import com.mysite.sns1_server.domain.follow.type.FollowStatus;
import com.mysite.sns1_server.domain.member.entity.Member;

public interface FollowRepository extends JpaRepository<Follow,Long> {
	@EntityGraph(attributePaths = {"following", "follower"})
	Slice<Follow> findByFollowerAndStatus(Member follower, FollowStatus status, Pageable pageable);

	@EntityGraph(attributePaths = {"following", "follower"})
	Slice<Follow> findByFollowingAndStatus(Member following, FollowStatus status, Pageable pageable);

	Optional<Follow> findFollowByFollowerAndFollowing(Member follower, Member following);


	@Query("SELECT f.status FROM Follow f WHERE f.follower = :follower AND f.following = :following")
	FollowStatus findStatusByFollowerAndFollowing(
		@Param("follower") Member follower,
		@Param("following") Member following
	);
}
