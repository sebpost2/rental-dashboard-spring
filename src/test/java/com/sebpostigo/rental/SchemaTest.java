package com.sebpostigo.rental;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SchemaTest extends IntegrationTest {

	@Test
	void migrationsCreateAllTables() {
		List<String> tables = jdbc.sql("""
				select table_name from information_schema.tables
				where table_schema = 'public' and table_name in ('users', 'properties', 'incomes', 'expenses')
				order by table_name""").query(String.class).list();

		assertThat(tables).containsExactly("expenses", "incomes", "properties", "users");
	}

	@Test
	void childRowsCascadeWhenTheirParentIsDeleted() {
		List<String> rules = jdbc.sql("""
				select tc.table_name || ':' || rc.delete_rule
				from information_schema.referential_constraints rc
				join information_schema.table_constraints tc on tc.constraint_name = rc.constraint_name
				where tc.table_name in ('properties', 'incomes', 'expenses')
				order by tc.table_name""").query(String.class).list();

		assertThat(rules).containsExactly("expenses:CASCADE", "incomes:CASCADE", "properties:CASCADE");
	}

}
