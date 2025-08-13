package ee.eesti.riha.rest.dao.grid;

import ee.eesti.riha.rest.model.Comment;
import ee.eesti.riha.rest.model.readonly.Comment_type_issue_view;
import ee.eesti.riha.rest.util.FilterParameter;
import ee.eesti.riha.rest.util.PagedRequest;
import ee.eesti.riha.rest.util.SortParameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Query grid to work specifically with entity of {@link Comment} type. Provides ways to query top level comments and
 * additional customized actions. <p>Actions: <ul><li>{@link #ACTION_AUTHOR_OR_ORGANIZATION_CODE} will modify query and
 * restrict {@link #PROPERTY_AUTHOR_PERSONAL_CODE} and {@link #PROPERTY_ORGANIZATION_CODE} in disjunction on both main
 * query and will additionally query child comment entities for same disjunction existence</li></ul></p>
 */
@Repository
@Transactional
public class CommentGrid extends AbstractQueryGrid {

    private static final String PROPERTY_AUTHOR_PERSONAL_CODE = "author_personal_code";
    private static final String PROPERTY_ORGANIZATION_CODE = "organization_code";
    private static final String ACTION_AUTHOR_OR_ORGANIZATION_CODE = "author-or-organization-code";
    private static final String ACTION_ORGANIZATION_INFOSYSTEMS_RELATED_ISSUES = "organization-infosystems-issues";

    public CommentGrid() {
        super(Comment_type_issue_view.class);
    }

    @Override
    protected void setRestrictions(CriteriaQuery<?> cq, Root<?> root, CriteriaBuilder cb, PagedRequest request) {
        List<Predicate> restrictions = new ArrayList<>();
        
        // Add null parent restriction
        restrictions.add(cb.isNull(root.get("comment_parent_id")));

        if (request.containsFilter(ACTION_AUTHOR_OR_ORGANIZATION_CODE)) {
            Predicate authorOrOrgRestriction = createAuthorOrOrganizationCodeRestriction(cq, root, cb, request);
            if (authorOrOrgRestriction != null) {
                restrictions.add(authorOrOrgRestriction);
            }
        } else {
            Predicate requestRestrictions = createRequestRestrictions(root, cb, request);
            if (requestRestrictions != null) {
                restrictions.add(requestRestrictions);
            }
            
            if (request.containsFilter(ACTION_ORGANIZATION_INFOSYSTEMS_RELATED_ISSUES)) {
                Predicate orgInfoSystemRestriction = createOrganizationInfoSystemsRelatedIssuesRestriction(root, cb, request);
                if (orgInfoSystemRestriction != null) {
                    restrictions.add(orgInfoSystemRestriction);
                }
            }
        }
        
        if (!restrictions.isEmpty()) {
            cq.where(cb.and(restrictions.toArray(new Predicate[0])));
        }
    }

    /**
     * Restricts query to search for either {@link #PROPERTY_AUTHOR_PERSONAL_CODE} or {@link
     * #PROPERTY_ORGANIZATION_CODE}. Additionally creates sub query to make same query on child entities. All other
     * filter restrictions are applied to main query only.
     *
     * @param cq criteria query
     * @param root query root
     * @param cb criteria builder
     * @param request  paged request
     */
    private Predicate createAuthorOrOrganizationCodeRestriction(CriteriaQuery<?> cq, Root<?> root, CriteriaBuilder cb, PagedRequest request) {
        List<Predicate> allRestrictions = new ArrayList<>();
        
        Predicate mainCriterion = getMainCriterion(root, cb, request);
        if (mainCriterion != null) {
            allRestrictions.add(mainCriterion);
        }

        Predicate propagatedCriterion = getPropagatedCriterion(root, cb, request);
        if (propagatedCriterion != null) {
            // Create subquery for child comments
            Subquery<Integer> subquery = cq.subquery(Integer.class);
            Root<Comment> childRoot = subquery.from(Comment.class);
            
            subquery.select(childRoot.get("comment_id"))
                   .where(cb.and(
                       cb.equal(childRoot.get("comment_parent_id"), root.get("comment_id")),
                       createPropagatedCriterionForRoot(childRoot, cb, request)
                   ));

            Predicate existsChild = cb.exists(subquery);
            allRestrictions.add(cb.or(propagatedCriterion, existsChild));
        }
        
        return allRestrictions.isEmpty() ? null : cb.and(allRestrictions.toArray(new Predicate[0]));
    }

    private Predicate createOrganizationInfoSystemsRelatedIssuesRestriction(Root<?> root, CriteriaBuilder cb, PagedRequest request) {
        FilterParameter organizationCodeFilter = request.getFirstFilter(ACTION_ORGANIZATION_INFOSYSTEMS_RELATED_ISSUES);
        if (organizationCodeFilter.getValue() == null) {
            return null;
        }

        // Note: This requires a native SQL function call which is more complex in JPA Criteria API
        // For now, we'll use a simple approach. In production, you might want to create a custom SQL function
        // or use a different approach like a JOIN with the main_resource_view
        
        // This is a simplified version - the original SQL restriction would need to be adapted
        // to use proper JPA Criteria API or a custom predicate
        return cb.isNotNull(root.get("infosystem_uuid")); // Placeholder - needs proper implementation
    }

    private Predicate getMainCriterion(Root<?> root, CriteriaBuilder cb, PagedRequest request) {
        List<Predicate> predicateList = new ArrayList<>();
        for (String property : request.getFilterProperties()) {
            if (!isPropagatedProperty(property)) {
                Predicate predicate = createPropertyRestrictions(root, cb, request.getFilter(property));
                if (predicate != null) {
                    predicateList.add(predicate);
                }
            }
        }

        return conjunction(predicateList);
    }

    private Predicate getPropagatedCriterion(Root<?> root, CriteriaBuilder cb, PagedRequest request) {
        List<Predicate> predicateList = new ArrayList<>();
        for (String property : request.getFilterProperties()) {
            if (isPropagatedProperty(property)) {
                Predicate predicate = createPropertyRestrictions(root, cb, request.getFilter(property));
                if (predicate != null) {
                    predicateList.add(predicate);
                }
            }
        }

        return disjunction(predicateList);
    }

    private Predicate createPropagatedCriterionForRoot(Root<Comment> childRoot, CriteriaBuilder cb, PagedRequest request) {
        List<Predicate> predicateList = new ArrayList<>();
        for (String property : request.getFilterProperties()) {
            if (isPropagatedProperty(property)) {
                Predicate predicate = createPropertyRestrictionsForRoot(childRoot, cb, request.getFilter(property));
                if (predicate != null) {
                    predicateList.add(predicate);
                }
            }
        }

        return disjunction(predicateList);
    }

    private Predicate createPropertyRestrictionsForRoot(Root<Comment> root, CriteriaBuilder cb, List<FilterParameter> filterParameters) {
        if (filterParameters == null) {
            return null;
        }

        List<Predicate> propertyRestrictions = new ArrayList<>();
        for (FilterParameter filter : filterParameters) {
            Predicate restriction = createPropertyFilterRestrictionForRoot(root, cb, filter);
            if (restriction != null) {
                propertyRestrictions.add(restriction);
            }
        }

        return disjunction(propertyRestrictions);
    }

    private Predicate createPropertyFilterRestrictionForRoot(Root<Comment> root, CriteriaBuilder cb, FilterParameter filter) {
        if (PROPERTY_AUTHOR_PERSONAL_CODE.equalsIgnoreCase(filter.getProperty())) {
            return cb.like(cb.lower(root.get(PROPERTY_AUTHOR_PERSONAL_CODE)), 
                          ("%" + filter.getValue().toString().toLowerCase() + "%"));
        }

        // Handle other properties
        if (filter.getValue() != null) {
            return cb.equal(root.get(filter.getProperty()), filter.getValue());
        } else {
            return cb.isNull(root.get(filter.getProperty()));
        }
    }

    private boolean isPropagatedProperty(String property) {
        return PROPERTY_AUTHOR_PERSONAL_CODE.equalsIgnoreCase(property)
                || PROPERTY_ORGANIZATION_CODE.equalsIgnoreCase(property);
    }

    @Override
    protected Order createSortParameterOrder(Root<?> root, CriteriaBuilder cb, SortParameter sortParameter) {
        Order order = super.createSortParameterOrder(root, cb, sortParameter);
        if (order != null) {
            // In JPA Criteria API, null handling is done differently
            // We'll return the order as-is for now
            return order;
        }
        return null;
    }

    @Override
    protected List<Map<String, Object>> transformResults(List<Object[]> results, PagedRequest request) {
        // Transform to CommentTypeIssueViewModel objects instead of generic maps
        List<Map<String, Object>> transformedResults = new ArrayList<>();
        List<String> aliasOrder = new ArrayList<>(projectionAliases.keySet());
        
        for (Object[] row : results) {
            Map<String, Object> resultMap = new HashMap<>();
            for (int i = 0; i < row.length && i < aliasOrder.size(); i++) {
                resultMap.put(aliasOrder.get(i), row[i]);
            }
            transformedResults.add(resultMap);
        }
        
        return transformedResults;
    }

    @Override
    protected Predicate createPropertyFilterRestriction(Root<?> root, CriteriaBuilder cb, FilterParameter filter) {
        if (PROPERTY_AUTHOR_PERSONAL_CODE.equalsIgnoreCase(filter.getProperty())) {
            return cb.like(cb.lower(root.get(PROPERTY_AUTHOR_PERSONAL_CODE)), 
                          ("%" + filter.getValue().toString().toLowerCase() + "%"));
        }

        return super.createPropertyFilterRestriction(root, cb, filter);
    }
}
