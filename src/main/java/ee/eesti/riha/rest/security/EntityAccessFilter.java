package ee.eesti.riha.rest.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.auth.AuthInfo.SimpleRoleRight;
import ee.eesti.riha.rest.dao.ApiGenericDAO;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.Role_rightCalculator;
import ee.eesti.riha.rest.logic.util.JsonContentBasedTable;
import ee.eesti.riha.rest.model.Comment;
import ee.eesti.riha.rest.model.Data_object;
import ee.eesti.riha.rest.model.Document;
import ee.eesti.riha.rest.model.Main_resource;
import ee.eesti.riha.rest.model.readonly.Role_right;

@Component
public class EntityAccessFilter<T> {

  private static final Logger LOG = LoggerFactory.getLogger(EntityAccessFilter.class);

  @Autowired
  private Role_rightCalculator roleCalculator;

  @Autowired
  private ApiGenericDAO<T, Integer> genericDAO;

  @Autowired
  private ApiGenericDAO<Main_resource, Integer> mainResourceDAO;

  public List<T> filterItemsOnRead(List<T> items, Class<T> clazz, AuthInfo authInfo) {
    List<T> filteredItems = new ArrayList<>();

    if (JsonContentBasedTable.isJsonContentBasedTable(clazz)) {
      int skipCount = 0;
      for (T item : items) {
        try {
          canRead(item, authInfo);
          filteredItems.add(item);
        } catch (Exception e) {
          skipCount++;
        }
      }
      LOG.info("Secure Filter skipped: " + skipCount);
    } else {
      filteredItems = items;
    }
    return filteredItems;
  }

  public void canRead(T object, AuthInfo authInfo) throws RihaRestException {

    if (object.getClass() == Comment.class || !JsonContentBasedTable.isJsonContentBasedTable(object.getClass())) {
      // special case
      return;
    }

    Main_resource mr = queryMain_resource((Class<T>) object.getClass(), object);
    mr = queryMain_resourceThroughData_object(object, mr); // conditional override

    if (mr == null) { // mingi viga?
      throw new RihaRestException(noMain_resource(object, "READ"));
    } else {
      Role_right roleRight = canDoHelperMain_resource(authInfo, object, mr);
      roleCalculator.canRead(roleRight, authInfo, mr.getOwner());
      // roleCalculator.canRead(object, roleRight);
    }
  }

  private Main_resource queryMain_resource(Class<T> clazz, T item) {
    Main_resource mr = null;
    Integer mrId = null;
    if (clazz == Main_resource.class) {
      mr = (Main_resource) item;
    } else if (clazz == Document.class) {
      Document doc = (Document) item;
      mrId = doc.getMain_resource_id();
    } else if (clazz == Data_object.class) {
      Data_object data = (Data_object) item;
      mrId = data.getMain_resource_id();
    } else {
      LOG.info("Wrong clazz " + clazz + " item " + item);
    }
    if (mrId != null) {
      mr = mainResourceDAO.find(Main_resource.class, mrId);
    } else {
      LOG.info(clazz + " has no parent main_resource ");
    }
    return mr;
  }

  // special method needed for Document
  private Main_resource queryMain_resourceThroughData_object(T object, Main_resource mr) {
    if (mr == null) {
      Data_object data = null;
      if (object.getClass() == Document.class) {
        Document doc = (Document) object;
        if (doc.getData_object_id() == null) {
          return mr;
        }
        data = (Data_object) genericDAO.find((Class<T>) Data_object.class, doc.getData_object_id());
        if (data != null && data.getMain_resource_id() != null) {
          mr = mainResourceDAO.find(Main_resource.class, data.getMain_resource_id());
        }
      }
    }
    return mr;
  }

  private RihaRestError noMain_resource(T object, String errtrace) {
    LOG.info("No main_resource exists");
    RihaRestError error = new RihaRestError();
    error.setErrcode(ErrorCodes.NOT_AUTHORIZED_NO_REF_MAIN_RESOURECE);
    error.setErrmsg(ErrorCodes.NOT_AUTHORIZED_NO_REF_MAIN_RESOURECE_MSG + object.getClass().getSimpleName());
    error.setErrtrace(errtrace);
    return error;
  }

  private Role_right canDoHelperMain_resource(AuthInfo authInfo, T object, Main_resource mr) {

    Integer kindId = mr.getKind_id();
    Integer accessRestriction = queryAccessRestriction(object, mr);

    return roleCalculator.calculate(authInfo, kindId, accessRestriction);
  }

  private Integer queryAccessRestriction(T item, Main_resource mr) {
    Integer objAccessRestriction = getAccessRestriction(item, mr);
    Integer accessRestriction = mr.getAccess_restriction();
    List<Integer> parentAccessRestrictions = getParentMain_resourceAccessRestrictions(mr);

    accessRestriction = max(toInt(objAccessRestriction), toInt(accessRestriction),
        Collections.max(parentAccessRestrictions));
    return accessRestriction;
  }

  private Integer getAccessRestriction(T item, Main_resource mr) {
    Integer accessRestriction = null;
    Class<T> clazz = (Class<T>) item.getClass();
    if (clazz == Document.class) {
      Document doc = (Document) item;
      accessRestriction = doc.getAccess_restriction();
    } else if (clazz == Data_object.class) {
      Data_object data = (Data_object) item;
      accessRestriction = data.getAccess_restriction();
    } else if (clazz == Main_resource.class) {
      accessRestriction = mr.getAccess_restriction();
    }
    return accessRestriction;
  }

  private List<Integer> getParentMain_resourceAccessRestrictions(Main_resource mr) {
    List<Integer> accessRestrictions = new ArrayList<>();
    accessRestrictions.add(toInt(mr.getAccess_restriction()));
    int maxDepth = 10;
    int count = 0;
    if (mr.getMain_resource_parent_id() != null) {
      Main_resource parent = mainResourceDAO.find(Main_resource.class, mr.getMain_resource_parent_id());
      while (parent != null && parent.getMain_resource_parent_id() != null && count < maxDepth) {
        parent = mainResourceDAO.find(Main_resource.class, mr.getMain_resource_parent_id());
        accessRestrictions.add(parent.getAccess_restriction());
        count++;
      }
    }

    return accessRestrictions;
  }

  private static int toInt(Integer num) {
    if (num == null) {
      return 0;
    }
    return num.intValue();
  }

  private static int max(int a, int b, int c) {
    return Math.max(a, Math.max(b, c));
  }

  public void canCreate(T object, AuthInfo authInfo) throws RihaRestException {

    if (object.getClass() == Comment.class) {
      // special case
      return;
    }

    Main_resource mr = queryMain_resource((Class<T>) object.getClass(), object);
    mr = queryMain_resourceThroughData_object(object, mr);

    // Connected main_resource can't be null, otherwise can't know permissions
    if (mr == null) {
      throw new RihaRestException(noMain_resource(object, "CREATE"));
    } else {
      Role_right roleRight = canDoHelperMain_resource(authInfo, object, mr);
      roleCalculator.canCreate(roleRight, authInfo, mr.getOwner());
    }

  }

  public void canUpdate(T object, AuthInfo authInfo) throws RihaRestException {

    if (object.getClass() == Comment.class) {
      // special case
      return;
    }

    Main_resource mr = queryMain_resource((Class<T>) object.getClass(), object);
    mr = queryMain_resourceThroughData_object(object, mr);

    if (mr == null) {
      throw new RihaRestException(noMain_resource(object, "UPDATE"));
    } else {
      Role_right roleRight = canDoHelperMain_resource(authInfo, object, mr);
      roleCalculator.canUpdate(roleRight, authInfo, mr.getOwner());
    }

  }

  public void canDelete(T object, AuthInfo authInfo) throws RihaRestException {

    if (object.getClass() == Comment.class) {
      // special case
      return;
    }

    Main_resource mr = queryMain_resource((Class<T>) object.getClass(), object);
    mr = queryMain_resourceThroughData_object(object, mr);

    if (mr == null) {
      throw new RihaRestException(noMain_resource(object, "DELETE"));
    } else {
      Role_right roleRight = canDoHelperMain_resource(authInfo, object, mr);
      roleCalculator.canDelete(roleRight, authInfo, mr.getOwner());
    }
  }

  public int getMaxAccess(List<SimpleRoleRight> simpleRoles) {
    int max = 0;
    for (SimpleRoleRight role : simpleRoles) {
      if (toInt(role.getAccess_restriction()) > max) {
        max = toInt(role.getAccess_restriction());
      }
    }
    return max;
  }

}
