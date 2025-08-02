package com.mysite.sns1_server.domain.notification.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mysite.sns1_server.domain.member.entity.Member;
import com.mysite.sns1_server.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    Slice<Notification> findByMemberAndIdLessThanOrderByIdDesc(Member member, Long lastNotificationId, Pageable pageable);

    Slice<Notification> findByMemberOrderByIdDesc(Member member, Pageable pageable);
}
