package ee.eesti.riha.rest.logic;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.dao.KindRepository;
import ee.eesti.riha.rest.dao.Role_rightRepository;
import ee.eesti.riha.rest.error.ErrorCodes;
import ee.eesti.riha.rest.error.RihaRestError;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.model.readonly.Role_right;

@Component
public class Role_rightCalculator {

  // permission for all elements
  public static final int ALL = 2;
  // permission for elements with same owner
  public static final int SAME_OWNER = 1;
  public static final int NONE = 0;

  @Autowired
  KindRepository kindRepository;

  @Autowired
  Role_rightRepository roleRepository;

  private static final Logger LOG = LoggerFactory.getLogger(Role_rightCalculator.class);

  public Role_right calculate(AuthInfo authInfo, Integer kindId, Integer accessRestriction) {

    List<Role_right> defaultRights = roleRepository.getByName("DEFAULT");
    List<Role_right> authenticatedRights = roleRepository.getByName("AUTHENTICATED");
    List<Role_right> roleRights = roleRepository.getByName(authInfo.getRole_code());

    LOG.info("ACCESS RESTRICTION " + accessRestriction);
    LOG.info("" + defaultRights);
    LOG.info("" + authenticatedRights);
    LOG.info("" + roleRights);

    defaultRights = filterRoles(defaultRights, kindId, accessRestriction);
    authenticatedRights = filterRoles(authenticatedRights, kindId, accessRestriction);
    roleRights = filterRoles(roleRights, kindId, accessRestriction);

    LOG.info("" + defaultRights);
    LOG.info("" + authenticatedRights);
    LOG.info("" + roleRights);

    Role_right roleRight = calculateRoleRight(kindId, defaultRights, authenticatedRights, roleRights);
    LOG.info(JsonHelper.GSON.toJson(roleRight));
    return roleRight;
  }

  private Role_right calculateRoleRight(Integer kindId, List<Role_right> defaultRights,
      List<Role_right> authenticatedRights, List<Role_right> roleRights) {
    if (defaultRights.size() > 1 || authenticatedRights.size() > 1 || roleRights.size() > 1) {
      throw new IllegalArgumentException("Filter role_rights should have no more than 1 element");
    }
    return calculateRoleRight(kindId, getFirst(defaultRights), getFirst(authenticatedRights), getFirst(roleRights));
  }

  private static final Role_right NOTHING = new Role_right(Integer.MIN_VALUE, Integer.MIN_VALUE, "NOTHING",
      Integer.MIN_VALUE, NONE, NONE, NONE, NONE);

  private Role_right calculateRoleRight(Integer kindId, Role_right defaultRight, Role_right authenticatedRight,
      Role_right roleRight) {
    Role_right calculatedRight = null;
    // final int CALCULATED_ID = 999999;
    LOG.info("KIND_ID " + kindId);
    LOG.info("DEFAULT " + defaultRight);

    if (defaultRight == null) {
      defaultRight = NOTHING;
    }

    if (authenticatedRight == null) {
      authenticatedRight = NOTHING;
    }

    if (roleRight != null) {
      calculatedRight = new Role_right(roleRight.getRole_right_id(), kindId, roleRight.getRole_name(),
          max(defaultRight.getAccess_restriction(), authenticatedRight.getAccess_restriction(),
              roleRight.getAccess_restriction()),
          max(defaultRight.getRead(), authenticatedRight.getRead(), roleRight.getRead()),
          max(defaultRight.getCreate(), authenticatedRight.getCreate(), roleRight.getCreate()),
          max(defaultRight.getUpdate(), authenticatedRight.getUpdate(), roleRight.getUpdate()),
          max(defaultRight.getDelete(), authenticatedRight.getDelete(), roleRight.getDelete()));
    } else if (authenticatedRight != null) {
      calculatedRight = new Role_right(authenticatedRight.getRole_right_id(), kindId,
          authenticatedRight.getRole_name(),
          Math.max(defaultRight.getAccess_restriction(), authenticatedRight.getAccess_restriction()),
          Math.max(defaultRight.getRead(), authenticatedRight.getRead()),
          Math.max(defaultRight.getCreate(), authenticatedRight.getCreate()),
          Math.max(defaultRight.getUpdate(), authenticatedRight.getUpdate()),
          Math.max(defaultRight.getDelete(), authenticatedRight.getDelete()));
    } else {
      calculatedRight = defaultRight;
    }

    return calculatedRight;
  }

  private List<Role_right> filterRoles(List<Role_right> roles, Integer kindId, Integer accessRestriction) {
    List<Role_right> roleRights = new ArrayList<>();
    for (Role_right role : roles) {
      if (role.getKind_id().equals(kindId)
          && toInt(role.getAccess_restriction()) >= toInt(accessRestriction)) {
        roleRights.add(role);
      }
    }
    return roleRights;
  }

  // public <T> void canRead(T item, Role_right roleRight) throws RihaRestException {
  //
  // if (item == null || item.getClass() == Comment.class) {
  // // special case
  // return;
  // }
  // boolean canRead = false;
  // if (item instanceof BaseModel) {
  // BaseModel bm = (BaseModel) item;
  // if (toInt(bm.getAccess_restriction()) <= toInt(roleRight.getAccess_restriction())) {
  // canRead = true;
  // }
  // }
  //
  // if (!canRead) {
  // throw new RihaRestException(readDenied(roleRight.getRole_name(), null));
  // }
  // }

  public void canRead(Role_right roleRight, AuthInfo authInfo, String owner) throws RihaRestException {
    boolean canRead = false;
    if (roleRight.getRead() == ALL) {
      canRead = true;
    }

    if (roleRight.getRead() == SAME_OWNER && authInfo.getOrg_code().equals(owner)) {
      canRead = true;
    }

    if (!canRead) {
      throw new RihaRestException(readDenied(roleRight.getRole_name(), owner));
    }

  }

  public void canCreate(Role_right roleRight, AuthInfo authInfo, String owner) throws RihaRestException {
    boolean canCreate = false;
    if (roleRight.getCreate() == ALL) {
      canCreate = true;
    }

    if (roleRight.getCreate() == SAME_OWNER && authInfo.getOrg_code().equals(owner)) {
      canCreate = true;
    }

    if (!canCreate) {
      throw new RihaRestException(createDenied(roleRight.getRole_name(), owner));
    }

  }

  public void canUpdate(Role_right roleRight, AuthInfo authInfo, String owner) throws RihaRestException {
    boolean canUpdate = false;
    if (roleRight.getUpdate() == ALL) {
      canUpdate = true;
    }

    if (roleRight.getUpdate() == SAME_OWNER && authInfo.getOrg_code().equals(owner)) {
      canUpdate = true;
    }

    if (!canUpdate) {
      throw new RihaRestException(updateDenied(roleRight.getRole_name(), owner));
    }

  }

  public void canDelete(Role_right roleRight, AuthInfo authInfo, String owner) throws RihaRestException {
    boolean canDelete = false;
    if (roleRight.getDelete() == ALL) {
      canDelete = true;
    }

    if (roleRight.getDelete() == SAME_OWNER && authInfo.getOrg_code().equals(owner)) {
      canDelete = true;
    }

    if (!canDelete) {
      throw new RihaRestException(deleteDenied(roleRight.getRole_name(), owner));
    }

  }

  private static int max(int a, int b, int c) {
    return Math.max(a, Math.max(b, c));
  }

  private static int toInt(Integer num) {
    if (num == null) {
      return 0;
    }
    return num.intValue();
  }

  private static <T> T getFirst(List<T> elements) {
    if (elements != null && elements.size() > 0) {
      return elements.get(0);
    }
    return null;
  }

  private static RihaRestError readDenied(String role, String owner) {
    return accessDenied(ErrorCodes.NOT_AUTHORIZED_FOR_READ,
        ErrorCodes.NOT_AUTHORIZED_FOR_READ_MSG, role, owner);
  }

  private static RihaRestError createDenied(String role, String owner) {
    return accessDenied(ErrorCodes.NOT_AUTHORIZED_FOR_CREATE,
        ErrorCodes.NOT_AUTHORIZED_FOR_CREATE_MSG, role, owner);
  }

  private static RihaRestError updateDenied(String role, String owner) {
    return accessDenied(ErrorCodes.NOT_AUTHORIZED_FOR_UPDATE,
        ErrorCodes.NOT_AUTHORIZED_FOR_UPDATE_MSG, role, owner);
  }

  private static RihaRestError deleteDenied(String role, String owner) {
    return accessDenied(ErrorCodes.NOT_AUTHORIZED_FOR_DELETE,
        ErrorCodes.NOT_AUTHORIZED_FOR_DELETE_MSG, role, owner);
  }

  private static RihaRestError accessDenied(int errcode, String errmsg, String role, String owner) {
    RihaRestError error = new RihaRestError();
    error.setErrcode(errcode);
    error.setErrmsg(errmsg + role);
    error.setErrtrace("Asutus: " + owner);
    return error;
  }
}
