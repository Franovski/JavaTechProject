package com.csis231.api.auth.Otp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    // Latest OTP for this user/purpose
    Optional<OtpCode> findTopByUser_IdAndPurposeOrderByIdDesc(Long userId, String purpose);

    // All currently active codes for this user/purpose (used to invalidate older ones)
    @Query("""
           select c
           from OtpCode c
           where c.user.id = :userId
             and c.purpose = :purpose
             and c.consumedAt is null
             and c.expiresAt > :now
           """)
    List<OtpCode> findActiveByUserIdAndPurpose(@Param("userId") Long userId,
                                               @Param("purpose") String purpose,
                                               @Param("now") Instant now);

}
