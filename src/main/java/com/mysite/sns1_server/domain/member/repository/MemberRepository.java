package com.mysite.sns1_server.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sns1_server.domain.member.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {

	// 사용자 이름으로 회원 조회
	Optional<Member> findByUsername(String username);

	// 이메일로 회원 조회
	Optional<Member> findByEmail(String email);

	// 사용자 이름 또는 이메일로 회원 조회
	Optional<Member> findByUsernameOrEmail(String username, String email);

}
