package org.bytepoet.shopifysolo.manager.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.bytepoet.shopifysolo.manager.models.AuditLog;
import org.bytepoet.shopifysolo.manager.models.Order;
import org.bytepoet.shopifysolo.manager.models.OrderStatus;
import org.bytepoet.shopifysolo.manager.models.OrderType;
import org.bytepoet.shopifysolo.manager.models.ShippingSearchStatus;
import org.bytepoet.shopifysolo.manager.repositories.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/audit-logs")
public class AuditLogsController {

	@Autowired
	private AuditLogRepository auditLogRepository;
	
	
	@RequestMapping(method=RequestMethod.GET)
	public Page<AuditLog> getAuditLogs(
			@RequestParam(name="changedBy", required=false) String changedBy,
			@RequestParam(name="shopifyOrderId", required=false) String shopifyOrderId,
			@RequestParam(name="id", required=false) Long id,
			@RequestParam(name="previousStatus", required=false) OrderStatus previousStatus,
			@RequestParam(name="nextStatus", required=false) OrderStatus nextStatus,
			@DateTimeFormat(iso = ISO.DATE_TIME) @RequestParam(name="start", required=false) Date start, 
			@DateTimeFormat(iso = ISO.DATE_TIME) @RequestParam(name="end", required=false) Date end,
			@RequestParam(name="page", required=false, defaultValue = "0") int page,
			@RequestParam(name="size", required=false, defaultValue = "20") int size,
			@RequestParam(name="sortBy", required=false, defaultValue ="logTime") String sortBy, 
			@RequestParam(name="sortDirection", required=false, defaultValue ="ASC") Direction direction) throws Exception {
		
		
		Specification<AuditLog> spec = new Specification<AuditLog>() {

			private static final long serialVersionUID = 3028280894396494017L;

			@Override
			public Predicate toPredicate(Root<AuditLog> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StringUtils.isNotBlank(changedBy)) {
					predicates.add(criteriaBuilder.equal(root.get("changedBy"), changedBy));
				}
				if (StringUtils.isNotBlank(shopifyOrderId)) {
					predicates.add(criteriaBuilder.like(root.get("previousState"), "%\"shopifyOrderNumber\":\""+shopifyOrderId+"\"%"));
				}
				if (id != null) {
					predicates.add(criteriaBuilder.equal(root.get("id"), id));
				}
				if (start != null) {
					predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("logTime"), start));
				}
				if (end != null) {
					predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("logTime"), end));
				}
				
				if (previousStatus != null) {
					predicates.add(criteriaBuilder.like(root.get("previousState"), "%\"status\":\""+previousStatus+"\"%"));
				}
				
				if (nextStatus != null) {
					predicates.add(criteriaBuilder.like(root.get("nextState"), "%\"status\":\""+nextStatus+"\"%"));
				}
				
				return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
			}
		};
		
		if(direction == null) {
			direction = Direction.ASC;
		}
		if(sortBy == null) {
			sortBy = "logTime";
		}
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
		Page<AuditLog> auditLogs = auditLogRepository.findAll(spec, pageable);
		return auditLogs;
		
	}
	
	@RequestMapping(path="/cleanup", method=RequestMethod.DELETE)
	public void cleanup() {
		Specification<AuditLog> spec = new Specification<AuditLog>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<AuditLog> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -7);
				
				return criteriaBuilder.lessThanOrEqualTo(root.get("logTime"), cal.getTime());
			}
		};
		

		List<AuditLog> auditLogs = auditLogRepository.findAll(spec);
		auditLogRepository.deleteInBatch(auditLogs);
	}
}
