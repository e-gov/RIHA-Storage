package ee.eesti.riha.rest.dao.grid;

import ee.eesti.riha.rest.model.readonly.RegisteredFileView;
import ee.eesti.riha.rest.util.FilterParameter;
import ee.eesti.riha.rest.util.FilterParameterExtractor;
import ee.eesti.riha.rest.util.PagedRequest;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class RegisteredFileGrid extends AbstractQueryGrid {

    public static final String PROPERTY_DATA = "data";

    public RegisteredFileGrid() {
        super(RegisteredFileView.class, "r");
    }

    @Override
    protected void setProjections() {
        addProjection("r.registeredFilePK.infoSystemUuid", "infosystem_uuid");
        addProjection("r.infoSystemName", "infosystem_name");
        addProjection("r.infoSystemShortName", "infosystem_short_name");
        addProjection("r.infoSystemOwnerName", "infosystem_owner_name");
        addProjection("r.infoSystemOwnerCode", "infosystem_owner_code");
        addProjection("r.registeredFilePK.fileResourceUuid", "file_resource_uuid");
        addProjection("r.fileResourceName", "file_resource_name");
    }

    @Override
    protected void setRestrictions(CriteriaQuery<?> criteriaQuery, Root<?> root, CriteriaBuilder criteriaBuilder, PagedRequest request) {
        if (request.containsFilter(PROPERTY_DATA)) {
            List<FilterParameter> dataFilterParameters = new ArrayList<>();
            for (FilterParameter parameter : request.getFilter(PROPERTY_DATA)) {
                dataFilterParameters.add(FilterParameterExtractor.extract(parameter.getValue(), ":"));
            }

            if (!dataFilterParameters.isEmpty()) {
                Subquery<Long> dataSubQuery = criteriaQuery.subquery(Long.class);
                Root<RegisteredFileView.LargeObjectRecord> csvRoot = dataSubQuery.from(RegisteredFileView.LargeObjectRecord.class);
                dataSubQuery.select(csvRoot.get("id"));
                
                Predicate dataPredicate = createFileDataSearchRestriction(csvRoot, criteriaBuilder, criteriaQuery, dataFilterParameters);
                if (dataPredicate != null) {
                    dataSubQuery.where(dataPredicate);
                    criteriaQuery.where(criteriaBuilder.exists(dataSubQuery));
                }
            }
        }

        super.setRestrictions(criteriaQuery, root, criteriaBuilder, request);
    }

    private Predicate createFileDataSearchRestriction(Root<?> root, CriteriaBuilder criteriaBuilder, CriteriaQuery<?> criteriaQuery, List<FilterParameter> filters) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        List<Predicate> restrictions = new ArrayList<>();
        for (FilterParameter filter : filters) {
            if (filter.getValue() == null) {
                // For null values, create a custom SQL restriction
                restrictions.add(criteriaBuilder.isNull(
                    criteriaBuilder.function("jsonb_extract_path_text", String.class,
                        root.get("value"), criteriaBuilder.literal(filter.getProperty()))));
            } else {
                // For non-null values, use ILIKE for case-insensitive matching
                Expression<String> jsonExtract = criteriaBuilder.function("jsonb_extract_path_text", String.class,
                    root.get("value"), criteriaBuilder.literal(filter.getProperty()));
                restrictions.add(criteriaBuilder.like(
                    criteriaBuilder.upper(jsonExtract),
                    criteriaBuilder.upper(criteriaBuilder.literal("%" + filter.getValue() + "%"))));
            }
        }

        if (restrictions.isEmpty()) {
            return null;
        }

        if (restrictions.size() == 1) {
            return restrictions.get(0);
        }

        return criteriaBuilder.or(restrictions.toArray(new Predicate[0]));
    }

    @Override
    protected Predicate createPropertyFilterRestriction(Root<?> root, CriteriaBuilder criteriaBuilder, FilterParameter filter) {
        if ("infoSystemUuid".equals(filter.getProperty())) {
            return criteriaBuilder.equal(root.get("registeredFilePK").get("infoSystemUuid"), filter.asUuid());
        } else if ("filResourceUuid".equals(filter.getProperty())) {
            return criteriaBuilder.equal(root.get("registeredFilePK").get("fileResourceUuid"), filter.asUuid());
        }

        return super.createPropertyFilterRestriction(root, criteriaBuilder, filter);
    }

}
