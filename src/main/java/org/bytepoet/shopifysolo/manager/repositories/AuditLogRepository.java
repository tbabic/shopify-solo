package org.bytepoet.shopifysolo.manager.repositories;

import java.util.UUID;

import org.bytepoet.shopifysolo.manager.models.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

}
