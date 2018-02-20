package ee.eesti.riha.rest.dao.grid;

import ee.eesti.riha.rest.util.FilterParameter;
import ee.eesti.riha.rest.util.PagedRequest;
import ee.eesti.riha.rest.util.PagedResponse;
import ee.eesti.riha.rest.util.SortParameter;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.*;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Abstract class providing convenient way of producing {@link PagedResponse} using {@link PagedRequest}. Implementors
 * have many ways to customize default behavior on all levels.
 */
@Transactional
public abstract class AbstractQueryGrid {

    private final Class entityType;
    private final String entityAlias;

    private Projection projection;
    private List<String> projectionAliases;

    @Autowired
    private SessionFactory sessionFactory;

    /**
     * Creates new instance of {@link AbstractQueryGrid} restricted to entity class with alias.
     *
     * @param entityType  class of entity
     * @param entityAlias entity alias used in queries
     */
    public AbstractQueryGrid(Class entityType, String entityAlias) {
        Assert.notNull(entityType, "entity type must be provided");
        Assert.hasText(entityAlias, "entity alias must not be empty");

        this.entityType = entityType;
        this.entityAlias = entityAlias;
    }

    /**
     * Creates conjunction from collection of criterion. Returns <ul><li>null in case of parameter list is null or
     * empty</li> <li>first element in collection in case collection size is 1</li> <li>conjunction of criterion
     * otherwise</li> </ul>
     *
     * @param criterion collection of criterion
     * @return null, first collection element or conjunction criterion of all collection elements
     */
    public static Criterion conjunction(Collection<? extends Criterion> criterion) {
        if (criterion == null || criterion.isEmpty()) {
            return null;
        }

        if (criterion.size() == 1) {
            return criterion.iterator().next();
        }

        return Restrictions.conjunction(criterion.toArray(new Criterion[0]));
    }

    /**
     * Creates disjunction from collection of criterion. Returns <ul><li>null in case of parameter list is null or
     * empty</li> <li>first element in collection in case collection size is 1</li> <li>disjunction of criterion
     * otherwise</li> </ul>
     *
     * @param criterion collection of criterion
     * @return null, first collection element or disjunction criterion of all collection elements
     */
    public static Criterion disjunction(Collection<? extends Criterion> criterion) {
        if (criterion == null || criterion.isEmpty()) {
            return null;
        }

        if (criterion.size() == 1) {
            return criterion.iterator().next();
        }

        return Restrictions.disjunction(criterion.toArray(new Criterion[0]));
    }

    /**
     * Initialization method. By default sets default projections using entity metadata property names.
     */
    @PostConstruct
    protected void init() {
        setProjection(getDefaultProjection());
    }

    /**
     * Defines projection that will be used when populating criteria. Called with default projection list after
     * dependency injection is complete.
     *
     * @param projection projection
     */
    protected void setProjection(Projection projection) {
        Assert.notNull(projection, "projection must be provided");

        this.projection = projection;
        this.projectionAliases = Arrays.asList(projection.getAliases());
    }

    private Projection getDefaultProjection() {
        ProjectionList projectionList = Projections.projectionList();

        ClassMetadata metadata = sessionFactory.getClassMetadata(entityType);

        String idPropertyName = metadata.getIdentifierPropertyName();
        if (idPropertyName != null) {
            projectionList.add(Projections.property(idPropertyName).as(idPropertyName));
        }

        for (String property : metadata.getPropertyNames()) {
            projectionList.add(Projections.property(property).as(property));
        }

        return projectionList;
    }

    /**
     * Query for paged list of elements. Retrieves total number of elements and elements restricted by request.
     *
     * @param request paged request
     * @return paged list of elements
     */
    public PagedResponse query(PagedRequest request) {
        Long totalElements = getTotalElementCount(request);
        List content = getContent(request);

        return new PagedResponse(content, totalElements, request.getPageSize(), request.getPageNumber());
    }

    /**
     * Retrieves list of elements for given request. Number and offset of returned elements is restricted by request
     * properties.
     *
     * @param request paged request
     * @return list of elements
     */
    public List getContent(PagedRequest request) {
        DetachedCriteria criteria = createCriteria();
        setProjections(criteria);
        setRestrictions(criteria, request);
        setOrder(criteria, request);
        setTransformation(criteria, request);

        Criteria executableCriteria = criteria.getExecutableCriteria(sessionFactory.getCurrentSession());
        setLimits(executableCriteria, request);

        return executableCriteria.list();
    }

    /**
     * Sets result transformers. By default {@link DetachedCriteria#ALIAS_TO_ENTITY_MAP} transformer is used.
     *
     * @param criteria criteria for setting result transformer
     * @param request  paged request
     */
    protected void setTransformation(DetachedCriteria criteria, PagedRequest request) {
        criteria.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
    }

    /**
     * Method for calculating total element count for given request.
     *
     * @param request paged request
     * @return total number of elements
     */
    public Long getTotalElementCount(PagedRequest request) {
        DetachedCriteria criteria = createCriteria();
        criteria.setProjection(Projections.rowCount());
        setRestrictions(criteria, request);

        return (Long) criteria.getExecutableCriteria(sessionFactory.getCurrentSession()).uniqueResult();
    }

    private void setLimits(Criteria contentExecutableCriteria, PagedRequest request) {
        if (request.getPageSize() >= 0) {
            contentExecutableCriteria.setMaxResults(request.getPageSize());

            if (request.getPageSize() >= 0) {
                contentExecutableCriteria.setFirstResult(request.getPageNumber() * request.getPageSize());
            }
        }
    }

    private DetachedCriteria createCriteria() {
        return DetachedCriteria.forClass(entityType, entityAlias);
    }

    /**
     * Default order setting method. Iterates through request sort definitions creates and adds {@link Order} to
     * criteria. By default only projected property can be sorted.
     *
     * @param criteria criteria for adding of ordering
     * @param request  paged request
     */
    protected void setOrder(DetachedCriteria criteria, PagedRequest request) {
        for (SortParameter sortParameter : request.getSort()) {
            if (projectionAliases.contains(sortParameter.getProperty())) {
                criteria.addOrder(sortParameter.isAscending()
                        ? Order.asc(sortParameter.getProperty())
                        : Order.desc(sortParameter.getProperty()));
            }
        }
    }

    private void setProjections(DetachedCriteria criteria) {
        criteria.setProjection(projection);
    }

    /**
     * Default restrictions setting method. Sets restrictions from request to criteria. Relies on {@link
     * #createRequestRestrictions(PagedRequest)} for instantiation of restriction criterion of each property. Override
     * this method in order to fully customize restriction handling.
     *
     * @param criteria criteria for setting of restrictions
     * @param request  paged request
     */
    protected void setRestrictions(DetachedCriteria criteria, PagedRequest request) {
        Criterion requestRestrictions = createRequestRestrictions(request);
        if (requestRestrictions != null) {
            criteria.add(requestRestrictions);
        }
    }

    /**
     * Iterates all request filters and creates conjunction of produced restriction criterion. Criterion is created
     * using {@link #createPropertyRestrictions(List)} method.
     *
     * @param request paged request
     * @return criterion conjunction, single criterion or null
     */
    protected Criterion createRequestRestrictions(PagedRequest request) {
        List<Criterion> requestRestrictions = new ArrayList<>();
        for (String property : request.getFilterProperties()) {
            Criterion propertyRestrictions = createPropertyRestrictions(request.getFilter(property));
            if (propertyRestrictions != null) {
                requestRestrictions.add(propertyRestrictions);
            }
        }

        return conjunction(requestRestrictions);
    }

    /**
     * Iterates request filters of single property and creates disjunction of restriction criterion. Criterion is
     * created using {@link #createPropertyFilterRestriction(FilterParameter)}.
     *
     * @param filterParameters list of filter parameters for single property
     * @return criterion disjunction, single criterion or null
     */
    protected Criterion createPropertyRestrictions(List<FilterParameter> filterParameters) {
        if (filterParameters == null) {
            return null;
        }

        List<Criterion> propertyRestrictions = new ArrayList<>();
        for (FilterParameter filter : filterParameters) {
            Criterion restriction = createPropertyFilterRestriction(filter);
            if (restriction != null) {
                propertyRestrictions.add(restriction);
            }
        }

        return disjunction(propertyRestrictions);
    }

    /**
     * Creates restriction criterion for single {@link FilterParameter}. By default produces equals {@link
     * SimpleExpression} with property and value from filter parameter or null if property alias not defined. Override
     * this method in order order to provide custom property handling and/or criterion creation.
     *
     * @param filter single filter
     * @return created criterion
     */
    protected Criterion createPropertyFilterRestriction(FilterParameter filter) {
        return projectionAliases.contains(filter.getProperty())
                ? Restrictions.eq(filter.getProperty(), filter.getValue())
                : null;
    }

}
