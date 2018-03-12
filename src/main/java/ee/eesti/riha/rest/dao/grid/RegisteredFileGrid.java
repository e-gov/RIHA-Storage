package ee.eesti.riha.rest.dao.grid;

import ee.eesti.riha.rest.model.readonly.RegisteredFileView;
import ee.eesti.riha.rest.util.FilterParameter;
import ee.eesti.riha.rest.util.FilterParameterExtractor;
import ee.eesti.riha.rest.util.PagedRequest;
import org.hibernate.criterion.*;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
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
    protected void setRestrictions(DetachedCriteria criteria, PagedRequest request) {
        if (request.containsFilter(PROPERTY_DATA)) {
            List<FilterParameter> dataFilterParameters = new ArrayList<>();
            for (FilterParameter parameter : request.getFilter(PROPERTY_DATA)) {
                dataFilterParameters.add(FilterParameterExtractor.extract(parameter.getValue(), ":"));
            }

            Criterion dataCriterion = createFileDataSearchRestriction(dataFilterParameters);
            if (dataCriterion != null) {
                DetachedCriteria dataSubQueryCriteria = DetachedCriteria.forClass(
                        RegisteredFileView.LargeObjectCsvRecord.class, "csv")
                        .setProjection(Projections.id())
                        .add(dataCriterion);

                criteria.add(Subqueries.exists(dataSubQueryCriteria));
            }
        }

        super.setRestrictions(criteria, request);
    }

    private Criterion createFileDataSearchRestriction(List<FilterParameter> filters) {
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        List<Criterion> restrictions = new ArrayList<>();
        for (FilterParameter filter : filters) {
            if (filter.getValue() == null) {
                restrictions.add(Restrictions.sqlRestriction("{alias}.value ->> ? IS NULL",
                        filter.getProperty(),
                        StringType.INSTANCE));
            } else {
                restrictions.add(Restrictions.sqlRestriction("{alias}.value ->> ? ILIKE ?",
                        new Object[]{filter.getProperty(), filter.getValue()},
                        new Type[]{StringType.INSTANCE, StringType.INSTANCE}));
            }
        }

        if (restrictions.isEmpty()) {
            return null;
        }

        if (restrictions.size() == 1) {
            return restrictions.get(0);
        }

        return Restrictions.conjunction(restrictions.toArray(new Criterion[0]));
    }

    @Override
    protected Criterion createPropertyFilterRestriction(FilterParameter filter) {
        if ("infoSystemUuid".equals(filter.getProperty())) {
            return Restrictions.eq("r.registeredFilePK.infoSystemUuid", filter.asUuid());
        } else if ("filResourceUuid".equals(filter.getProperty())) {
            return Restrictions.eq("r.registeredFilePK.fileResourceUuid", filter.asUuid());
        }

        return super.createPropertyFilterRestriction(filter);
    }

}
