package ee.eesti.riha.rest.dao.grid;

import ee.eesti.riha.rest.util.FilterParameter;
import ee.eesti.riha.rest.util.PagedRequest;
import ee.eesti.riha.rest.util.PagedResponse;
import ee.eesti.riha.rest.util.SortParameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Abstract class providing convenient way of producing {@link PagedResponse} using {@link PagedRequest}. Implementors
 * have many ways to customize default behavior on all levels.
 */
@Transactional
public abstract class AbstractQueryGrid {

    private final Class<?> entityType;

    protected Map<String, String> projectionAliases = new HashMap<>();

    private boolean initialized = false;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Creates new instance of {@link AbstractQueryGrid} restricted to entity class.
     *
     * @param entityType  class of entity
     */
    public AbstractQueryGrid(Class<?> entityType) {
        Assert.notNull(entityType, "entity type must be provided");
        this.entityType = entityType;
    }

    /**
     * Legacy constructor for backward compatibility
     * @param entityType class of entity
     * @param entityAlias entity alias (ignored in JPA Criteria API)
     * @deprecated Use {@link #AbstractQueryGrid(Class)} instead
     */
    @Deprecated
    public AbstractQueryGrid(Class<?> entityType, String entityAlias) {
        this(entityType);
    }

    /**
     * Creates conjunction from collection of predicates. Returns <ul><li>null in case of parameter list is null or
     * empty</li> <li>first element in collection in case collection size is 1</li> <li>conjunction of predicates
     * otherwise</li> </ul>
     *
     * @param predicates collection of predicates
     * @return null, first collection element or conjunction predicate of all collection elements
     */
    protected Predicate conjunction(Collection<? extends Predicate> predicates) {
        if (predicates == null || predicates.isEmpty()) {
            return null;
        }

        if (predicates.size() == 1) {
            return predicates.iterator().next();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    /**
     * Creates disjunction from collection of predicates. Returns <ul><li>null in case of parameter list is null or
     * empty</li> <li>first element in collection in case collection size is 1</li> <li>disjunction of predicates
     * otherwise</li> </ul>
     *
     * @param predicates collection of predicates
     * @return null, first collection element or disjunction predicate of all collection elements
     */
    protected Predicate disjunction(Collection<? extends Predicate> predicates) {
        if (predicates == null || predicates.isEmpty()) {
            return null;
        }

        if (predicates.size() == 1) {
            return predicates.iterator().next();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        return cb.or(predicates.toArray(new Predicate[0]));
    }

    /**
     * Initialization method. By default sets default projections using entity metadata property names.
     */
    @PostConstruct
    private void init() {
        setProjections();
        this.initialized = true;
    }

    /**
     * Defines projection that will be used when populating criteria. Called with default projection list after
     * dependency injection is complete.
     */
    protected void setProjections() {
        Metamodel metamodel = entityManager.getMetamodel();
        EntityType<?> entityMetadata = metamodel.entity(entityType);

        // Add ID attribute
        if (entityMetadata.hasSingleIdAttribute()) {
            String idPropertyName = entityMetadata.getId(entityMetadata.getIdType().getJavaType()).getName();
            addProjection(idPropertyName, idPropertyName);
        }

        // Add all other attributes
        entityMetadata.getAttributes().forEach(attribute -> {
            if (attribute instanceof SingularAttribute) {
                String propertyName = attribute.getName();
                addProjection(propertyName, propertyName);
            }
        });
    }

    public void addProjection(String propertyName, String alias) {
        Assert.isTrue(!initialized,
                "Already initialized. Override setProjections() method in order to set projections");

        projectionAliases.put(alias, propertyName);
    }

    /**
     * Query for paged list of elements. Retrieves total number of elements and elements restricted by request.
     *
     * @param request paged request
     * @return paged list of elements
     */
    public PagedResponse query(PagedRequest request) {
        Long totalElements = getTotalElementCount(request);
        List<?> content = getContent(request);

        return new PagedResponse(content, totalElements, request.getPageSize(), request.getPageNumber());
    }

    /**
     * Retrieves list of elements for given request. Number and offset of returned elements is restricted by request
     * properties.
     *
     * @param request paged request
     * @return list of elements
     */
    public List<?> getContent(PagedRequest request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<?> root = cq.from(entityType);
        
        // Set projections
        setProjections(cq, root, cb);
        
        // Set restrictions
        setRestrictions(cq, root, cb, request);
        
        // Set order
        setOrder(cq, root, cb, request);
        
        TypedQuery<Object[]> query = entityManager.createQuery(cq);
        
        // Set limits
        setLimits(query, request);
        
        List<Object[]> results = query.getResultList();
        
        // Transform to maps (similar to ALIAS_TO_ENTITY_MAP)
        return transformResults(results, request);
    }

    /**
     * Transform results from Object[] to Maps with aliases as keys.
     *
     * @param results raw query results
     * @param request paged request
     * @return transformed results
     */
    protected List<Map<String, Object>> transformResults(List<Object[]> results, PagedRequest request) {
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

    /**
     * Method for calculating total element count for given request.
     *
     * @param request paged request
     * @return total number of elements
     */
    public Long getTotalElementCount(PagedRequest request) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<?> root = cq.from(entityType);
        
        cq.select(cb.count(root));
        setRestrictions(cq, root, cb, request);
        
        TypedQuery<Long> query = entityManager.createQuery(cq);
        return query.getSingleResult();
    }

    private void setLimits(TypedQuery<?> query, PagedRequest request) {
        if (request.getPageSize() >= 0) {
            query.setMaxResults(request.getPageSize());

            if (request.getPageNumber() >= 0) {
                query.setFirstResult(request.getPageNumber() * request.getPageSize());
            }
        }
    }

    /**
     * Default order setting method. Iterates through request sort definitions creates and adds {@link Order} to
     * criteria. By default only projected property can be sorted.
     *
     * @param cq criteria query for adding of ordering
     * @param root query root
     * @param cb criteria builder
     * @param request  paged request
     */
    protected void setOrder(CriteriaQuery<?> cq, Root<?> root, CriteriaBuilder cb, PagedRequest request) {
        List<Order> orders = new ArrayList<>();
        for (SortParameter sortParameter : request.getSort()) {
            Order order = createSortParameterOrder(root, cb, sortParameter);
            if (order != null) {
                orders.add(order);
            }
        }
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        }
    }

    /**
     * Creates single {@link Order} for specified {@link SortParameter}.
     *
     * @param root query root
     * @param cb criteria builder
     * @param sortParameter sort parameter
     * @return order or null if property is not within alias list
     */
    protected Order createSortParameterOrder(Root<?> root, CriteriaBuilder cb, SortParameter sortParameter) {
        if (!projectionAliases.containsKey(sortParameter.getProperty())) {
            return null;
        }

        String propertyName = projectionAliases.get(sortParameter.getProperty());
        return sortParameter.isAscending()
                ? cb.asc(root.get(propertyName))
                : cb.desc(root.get(propertyName));
    }

    private void setProjections(CriteriaQuery<Object[]> cq, Root<?> root, CriteriaBuilder cb) {
        if (projectionAliases.isEmpty()) {
            return;
        }

        List<Selection<?>> selections = new ArrayList<>();
        for (Map.Entry<String, String> projectionAliasEntry : projectionAliases.entrySet()) {
            selections.add(root.get(projectionAliasEntry.getValue()).alias(projectionAliasEntry.getKey()));
        }
        cq.multiselect(selections);
    }

    /**
     * Default restrictions setting method. Sets restrictions from request to criteria. Relies on {@link
     * #createRequestRestrictions(Root, CriteriaBuilder, PagedRequest)} for instantiation of restriction predicates of each property. Override
     * this method in order to fully customize restriction handling.
     *
     * @param cq criteria query for setting of restrictions
     * @param root query root
     * @param cb criteria builder
     * @param request  paged request
     */
    protected void setRestrictions(CriteriaQuery<?> cq, Root<?> root, CriteriaBuilder cb, PagedRequest request) {
        Predicate requestRestrictions = createRequestRestrictions(root, cb, request);
        if (requestRestrictions != null) {
            cq.where(requestRestrictions);
        }
    }

    /**
     * Iterates all request filters and creates conjunction of produced restriction predicates. Predicates are created
     * using {@link #createPropertyRestrictions(Root, CriteriaBuilder, List)} method.
     *
     * @param root query root
     * @param cb criteria builder
     * @param request paged request
     * @return predicate conjunction, single predicate or null
     */
    protected Predicate createRequestRestrictions(Root<?> root, CriteriaBuilder cb, PagedRequest request) {
        List<Predicate> requestRestrictions = new ArrayList<>();
        for (String property : request.getFilterProperties()) {
            Predicate propertyRestrictions = createPropertyRestrictions(root, cb, request.getFilter(property));
            if (propertyRestrictions != null) {
                requestRestrictions.add(propertyRestrictions);
            }
        }

        return conjunction(requestRestrictions);
    }

    /**
     * Iterates request filters of single property and creates disjunction of restriction predicates. Predicates are
     * created using {@link #createPropertyFilterRestriction(Root, CriteriaBuilder, FilterParameter)}.
     *
     * @param root query root
     * @param cb criteria builder
     * @param filterParameters list of filter parameters for single property
     * @return predicate disjunction, single predicate or null
     */
    protected Predicate createPropertyRestrictions(Root<?> root, CriteriaBuilder cb, List<FilterParameter> filterParameters) {
        if (filterParameters == null) {
            return null;
        }

        List<Predicate> propertyRestrictions = new ArrayList<>();
        for (FilterParameter filter : filterParameters) {
            Predicate restriction = createPropertyFilterRestriction(root, cb, filter);
            if (restriction != null) {
                propertyRestrictions.add(restriction);
            }
        }

        return disjunction(propertyRestrictions);
    }

    /**
     * Creates restriction predicate for single {@link FilterParameter}. By default produces equals predicate
     * with property and value from filter parameter or null if property alias not defined. Override
     * this method in order to provide custom property handling and/or predicate creation.
     *
     * @param root query root
     * @param cb criteria builder
     * @param filter single filter
     * @return created predicate
     */
    protected Predicate createPropertyFilterRestriction(Root<?> root, CriteriaBuilder cb, FilterParameter filter) {
        if (!projectionAliases.containsKey(filter.getProperty())) {
            return null;
        }

        String propertyName = projectionAliases.get(filter.getProperty());
        if (filter.getValue() != null) {
            return cb.equal(root.get(propertyName), filter.getValue());
        } else {
            return cb.isNull(root.get(propertyName));
        }
    }

}
