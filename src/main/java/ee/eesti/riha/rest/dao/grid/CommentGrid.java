package ee.eesti.riha.rest.dao.grid;

import ee.eesti.riha.rest.model.Comment;
import ee.eesti.riha.rest.model.CommentTypeIssueViewModel;
import ee.eesti.riha.rest.model.readonly.Comment_type_issue_view;
import ee.eesti.riha.rest.util.FilterParameter;
import ee.eesti.riha.rest.util.PagedRequest;
import ee.eesti.riha.rest.util.SortParameter;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.NullPrecedence;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.type.StringType;
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
        super(Comment_type_issue_view.class, "comment");
    }

    @Override
    protected void setRestrictions(DetachedCriteria criteria, PagedRequest request) {
        criteria.add(Restrictions.isNull("comment.comment_parent_id"));

        if (request.containsFilter(ACTION_AUTHOR_OR_ORGANIZATION_CODE)) {
            restrictForAuthorOrOrganizationCode(criteria, request);
        } else {
            super.setRestrictions(criteria, request);
            if (request.containsFilter(ACTION_ORGANIZATION_INFOSYSTEMS_RELATED_ISSUES)) {
                restrictForOrganizationInfoSystemsRelatedIssues(criteria, request);
            }
        }
    }

    /**
     * Restricts query to search for either {@link #PROPERTY_AUTHOR_PERSONAL_CODE} or {@link
     * #PROPERTY_ORGANIZATION_CODE}. Additionally creates sub query to make same query on child entities. All other
     * filter restrictions are applied to main query only.
     *
     * @param criteria criteria to add restrictions to
     * @param request  paged request
     */
    private void restrictForAuthorOrOrganizationCode(DetachedCriteria criteria, PagedRequest request) {
        Criterion mainCriterion = getMainCriterion(request);
        if (mainCriterion != null) {
            criteria.add(mainCriterion);
        }

        Criterion propagatedCriterion = getPropagatedCriterion(request);
        if (propagatedCriterion != null) {
            DetachedCriteria subCriteria = DetachedCriteria.forClass(Comment.class, "child")
                    .setProjection(Projections.id())
                    .add(Restrictions.eqProperty("child.comment_parent_id", "comment.comment_id"))
                    .add(propagatedCriterion);

            criteria.add(Restrictions.disjunction(propagatedCriterion, Subqueries.exists(subCriteria)));
        }
    }

    private void restrictForOrganizationInfoSystemsRelatedIssues(DetachedCriteria criteria, PagedRequest request) {
        FilterParameter organizationCodeFilter = request.getFirstFilter(ACTION_ORGANIZATION_INFOSYSTEMS_RELATED_ISSUES);
        if (organizationCodeFilter.getValue() == null) {
            return;
        }

        criteria.add(Restrictions.sqlRestriction("{alias}.infosystem_uuid :: TEXT IN (" +
                        "SELECT json_content ->> 'uuid' uuid" +
                        " FROM riha.main_resource_view" +
                        " WHERE json_content #>> '{owner,code}' = ?)",
                organizationCodeFilter.getValue(), StringType.INSTANCE));
    }

    private Criterion getMainCriterion(PagedRequest request) {
        List<Criterion> criterionList = new ArrayList<>();
        for (String property : request.getFilterProperties()) {
            if (!isPropagatedProperty(property)) {
                Criterion criterion = createPropertyRestrictions(request.getFilter(property));
                if (criterion != null) {
                    criterionList.add(criterion);
                }
            }
        }

        return conjunction(criterionList);
    }

    private Criterion getPropagatedCriterion(PagedRequest request) {
        List<Criterion> criterionList = new ArrayList<>();
        for (String property : request.getFilterProperties()) {
            if (isPropagatedProperty(property)) {
                Criterion criterion = createPropertyRestrictions(request.getFilter(property));
                if (criterion != null) {
                    criterionList.add(criterion);
                }
            }
        }

        return disjunction(criterionList);
    }

    private boolean isPropagatedProperty(String property) {
        return PROPERTY_AUTHOR_PERSONAL_CODE.equalsIgnoreCase(property)
                || PROPERTY_ORGANIZATION_CODE.equalsIgnoreCase(property);
    }

    @Override
    protected Order createSortParameterOrder(SortParameter sortParameter) {
        return super.createSortParameterOrder(sortParameter).nulls(NullPrecedence.LAST);
    }

    @Override
    protected void setTransformation(DetachedCriteria criteria, PagedRequest request) {
        criteria.setResultTransformer(new AliasToBeanResultTransformer(CommentTypeIssueViewModel.class));
    }

    @Override
    protected Criterion createPropertyFilterRestriction(FilterParameter filter) {
        if (PROPERTY_AUTHOR_PERSONAL_CODE.equalsIgnoreCase(filter.getProperty())) {
            return Restrictions.ilike(PROPERTY_AUTHOR_PERSONAL_CODE, filter.getValue());
        }

        return super.createPropertyFilterRestriction(filter);
    }
}
