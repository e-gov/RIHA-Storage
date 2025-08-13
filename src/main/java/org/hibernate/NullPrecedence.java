package org.hibernate;

/**
 * Compatibility stub for legacy Hibernate NullPrecedence API.
 * This is a temporary solution to allow compilation after Spring Boot 3.0 migration.
 * TODO: Migrate to JPA Criteria API or JPQL queries for proper functionality.
 */
@Deprecated
public enum NullPrecedence {
    NONE, FIRST, LAST
}
