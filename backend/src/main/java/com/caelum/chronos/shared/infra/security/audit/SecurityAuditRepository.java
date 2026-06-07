package com.caelum.chronos.shared.infra.security.audit;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.caelum.chronos.shared.domain.SecurityAuditLog;

@Repository
public interface SecurityAuditRepository extends JpaRepository<SecurityAuditLog, UUID> {
}
