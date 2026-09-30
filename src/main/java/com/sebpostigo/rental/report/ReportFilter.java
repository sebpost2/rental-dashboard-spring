package com.sebpostigo.rental.report;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * The owner scope plus optional filters. Conditions are added only when a filter is present,
 * so no untyped null parameters ever reach Postgres.
 */
record ReportFilter(long ownerId, Long propertyId, LocalDate start, LocalDate end) {

	/** SQL condition for a ledger table aliased {@code alias}, joined to properties as {@code p}. */
	String where(String alias) {
		StringBuilder sql = new StringBuilder("p.owner_id = :ownerId");
		if (propertyId != null) {
			sql.append(" and ").append(alias).append(".property_id = :propertyId");
		}
		if (start != null) {
			sql.append(" and ").append(alias).append(".date >= :start");
		}
		if (end != null) {
			sql.append(" and ").append(alias).append(".date <= :end");
		}
		return sql.toString();
	}

	Map<String, Object> params() {
		Map<String, Object> params = new HashMap<>();
		params.put("ownerId", ownerId);
		if (propertyId != null) {
			params.put("propertyId", propertyId);
		}
		if (start != null) {
			params.put("start", start);
		}
		if (end != null) {
			params.put("end", end);
		}
		return params;
	}

}
