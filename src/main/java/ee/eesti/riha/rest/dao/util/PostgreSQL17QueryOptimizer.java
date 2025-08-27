package ee.eesti.riha.rest.dao.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PostgreSQL 17 optimization utilities for RIHA queries.
 * 
 * This class provides helper methods to optimize database queries
 * for PostgreSQL 17's improved performance features.
 */
public class PostgreSQL17QueryOptimizer {

    /**
     * Optimizes multiple OR conditions into a single IN clause.
     * 
     * PostgreSQL 17 can execute IN clauses with a single index scan
     * instead of multiple OR conditions that require separate scans.
     * 
     * @param fieldName the database field name
     * @param values list of values to check against
     * @param paramPrefix prefix for parameter names
     * @return optimized SQL condition
     */
    public static String optimizeOrConditionsToIn(String fieldName, List<String> values, String paramPrefix) {
        if (values == null || values.isEmpty()) {
            return "1=0"; // FALSE condition
        }
        
        if (values.size() == 1) {
            return fieldName + " = :" + paramPrefix;
        }
        
        return fieldName + " IN (:" + paramPrefix + "List)";
    }

    /**
     * Creates optimized JSON path queries for PostgreSQL 17.
     * 
     * Uses GIN indexes more efficiently with proper JSON operators.
     * 
     * @param jsonColumn the JSON column name (e.g., "json_content")
     * @param jsonPath the JSON path (e.g., "meta.update_timestamp")
     * @param operator comparison operator
     * @param paramName parameter name for the value
     * @return optimized JSON query condition
     */
    public static String optimizeJsonPathQuery(String jsonColumn, String jsonPath, String operator, String paramName) {
        // Convert dot notation to PostgreSQL JSON path notation
        String pgJsonPath = "{" + jsonPath.replace(".", ",") + "}";
        
        // Use #>> operator for text extraction with proper casting
        return String.format("(%s #>> '%s')::text %s :%s", 
                            jsonColumn, pgJsonPath, operator, paramName);
    }

    /**
     * Suggests whether to use BRIN or B-tree index based on data characteristics.
     * 
     * PostgreSQL 17 has improved BRIN index performance, especially for
     * time-series and append-only data.
     * 
     * @param isTimeSeriesData true if data is primarily time-ordered
     * @param isAppendOnly true if data is rarely updated after insert
     * @param estimatedTableSize estimated table size in MB
     * @return recommended index type
     */
    public static String recommendIndexType(boolean isTimeSeriesData, boolean isAppendOnly, long estimatedTableSize) {
        if (isTimeSeriesData && isAppendOnly && estimatedTableSize > 1000) {
            return "BRIN (recommended for large time-series data in PG17)";
        } else if (estimatedTableSize > 10000) {
            return "Consider BRIN for very large tables, otherwise B-tree";
        } else {
            return "B-tree (standard choice for smaller tables)";
        }
    }

    /**
     * Generates optimized ORDER BY clause for PostgreSQL 17's incremental sort.
     * 
     * PostgreSQL 17 can use incremental sort when data is partially ordered,
     * which can be much faster than full sorting.
     * 
     * @param primarySort the main sorting field
     * @param secondarySort optional secondary sorting field
     * @param direction "ASC" or "DESC"
     * @return optimized ORDER BY clause
     */
    public static String optimizeOrderByForIncrementalSort(String primarySort, String secondarySort, String direction) {
        StringBuilder orderBy = new StringBuilder("ORDER BY ");
        orderBy.append(primarySort).append(" ").append(direction);
        
        if (secondarySort != null && !secondarySort.trim().isEmpty()) {
            orderBy.append(", ").append(secondarySort).append(" ").append(direction);
            orderBy.append(" NULLS LAST"); // PG17 handles NULLS more efficiently
        }
        
        return orderBy.toString();
    }

    /**
     * Creates parameter map optimized for IN clause queries.
     * 
     * @param values list of values for IN clause
     * @param paramName parameter name
     * @return parameter map for Hibernate/JPA
     */
    public static Map<String, Object> createInClauseParams(List<String> values, String paramName) {
        if (values.size() == 1) {
            return Map.of(paramName, values.get(0));
        } else {
            return Map.of(paramName + "List", values);
        }
    }

    /**
     * Validates that query parameters are optimized for PostgreSQL 17.
     * 
     * @param query the SQL query to validate
     * @return list of optimization suggestions
     */
    public static List<String> validateQueryOptimization(String query) {
        return List.of(
            checkForOrChains(query),
            checkForJsonQueries(query),
            checkForSortingOptimizations(query)
        ).stream()
         .filter(suggestion -> !suggestion.isEmpty())
         .collect(Collectors.toList());
    }

    private static String checkForOrChains(String query) {
        if (query.matches(".*\\bOR\\s+\\w+\\s*=\\s*[^\\s]+\\s+OR\\s+\\w+\\s*=.*")) {
            return "Consider converting OR chains to IN clauses for better PG17 performance";
        }
        return "";
    }

    private static String checkForJsonQueries(String query) {
        if (query.contains("#>>") && !query.contains("GIN")) {
            return "Consider adding GIN indexes for JSON path queries in PG17";
        }
        return "";
    }

    private static String checkForSortingOptimizations(String query) {
        if (query.contains("ORDER BY") && !query.contains("NULLS")) {
            return "Consider adding NULLS LAST/FIRST for better PG17 sorting performance";
        }
        return "";
    }
}
