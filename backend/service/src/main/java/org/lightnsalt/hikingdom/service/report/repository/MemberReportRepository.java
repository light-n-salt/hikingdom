package org.lightnsalt.hikingdom.service.report.repository;

import org.lightnsalt.hikingdom.domain.entity.report.MemberReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberReportRepository extends JpaRepository<MemberReport, Long> {
	boolean existsByReportTypeAndReporterIdAndReportedId(String reportType, Long reporterId, Long reportedId);
}
