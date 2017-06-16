package ee.eesti.riha.rest.service.impl;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import ee.eesti.riha.rest.auth.TokenStore;
import ee.eesti.riha.rest.error.RihaRestException;
import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.logic.ImportLogic;
import ee.eesti.riha.rest.logic.MyExceptionHandler;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import ee.eesti.riha.rest.service.ApiImportService;

@Component
public class ApiImportServiceImpl implements ApiImportService {

  @Autowired
  ImportLogic importLogic;

  @Autowired
  TokenStore tokenStore;

  @Override
  public Response doImport(String json, String token) {
    System.out.println("Import");
    System.out.println("JSON:" + json);
    JsonObject jsonObject = JsonHelper.getFromJson(json);

    try {
      importLogic.logic(jsonObject);
    } catch (RihaRestException | ReflectiveOperationException | IOException e) {
      return Response.status(Status.BAD_REQUEST).entity(MyExceptionHandler.unmapped(e, " ImportService ")).build();
    }
    // placeholder, don't know what the response should be
    return Response.ok(Finals.OK).build();
  }

}
