package ee.eesti.riha.rest.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ee.eesti.riha.rest.auth.AuthInfo;
import ee.eesti.riha.rest.auth.AuthInfo.SimpleRoleRight;
import ee.eesti.riha.rest.auth.AuthInfo3rdParty;
import ee.eesti.riha.rest.dao.KindRepository;
import ee.eesti.riha.rest.dao.Role_rightRepository;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.logic.util.QueryHolder;
import ee.eesti.riha.rest.model.readonly.Kind;
import ee.eesti.riha.rest.model.readonly.Role_right;

@Component
public class AuthInfoCreator {

  @Autowired
  Role_rightRepository roleRepository;

  @Autowired
  KindRepository kindRepository;

  @Autowired
  ChangeLogic changeLogic;

  public AuthInfo convert(AuthInfo3rdParty authInfo3rdParty) throws RihaRestException {

    System.out.println(JsonHelper.GSON.toJson(authInfo3rdParty));

    AuthInfo authInfo = new AuthInfo(authInfo3rdParty);

    // no role given means not authenticated
    if (StringUtils.isEmpty(authInfo3rdParty.getRoll()) || authInfo.getRole_code().equals("null")) {
      authInfo.setRole_code("DEFAULT");
    }
    authInfo.setRole_name(authInfo.getRole_code());

    // add names to authInfo
    findNamesForCodes(authInfo3rdParty, authInfo);

    List<SimpleRoleRight> simpleRoles = findRoles(authInfo);

    authInfo.setRole_right(simpleRoles);

    return authInfo;
  }

  private void findNamesForCodes(AuthInfo3rdParty authInfo3rdParty, AuthInfo authInfo) throws RihaRestException {
    String personCode = StringUtils.defaultString(authInfo3rdParty.getIsikuKood());
    String organizationCode = StringUtils.defaultString(authInfo3rdParty.getAsutus());

    String json = "{\"persons\":[\"" + personCode + "\"], "
        + "\"organizations\":[\"" + organizationCode + "\"]}";

    QueryHolder queryHolder = QueryHolder.create(JsonHelper.GSON, json);
    Map<String, Map<String, String>> names = changeLogic.doGetNames(queryHolder);

    authInfo.setOrg_name(names.get("organizations").get(authInfo3rdParty.getAsutus()));
    authInfo.setUser_name(names.get("persons").get(authInfo3rdParty.getIsikuKood()));
  }

  public List<SimpleRoleRight> findRoles(AuthInfo authInfo) {
    List<Role_right> roles = roleRepository.getByName(authInfo.getRole_code());

    // unknown role, then grant authenticated role
    if (roles.isEmpty()) {
      roles = roleRepository.getByName("AUTHENTICATED");
    }

    List<SimpleRoleRight> simpleRoles = new ArrayList<>();

    for (Role_right role : roles) {
      Kind kind = kindRepository.getById(role.getKind_id());
      SimpleRoleRight simpleRole = new SimpleRoleRight(role, kind.getName());
      simpleRoles.add(simpleRole);
    }
    return simpleRoles;
  }

}
