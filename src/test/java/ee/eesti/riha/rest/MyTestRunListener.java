package ee.eesti.riha.rest;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import ee.eesti.riha.rest.logic.Finals;
import ee.eesti.riha.rest.service.ApiCGIService;
import ee.eesti.riha.rest.service.ApiClassicService;

public class MyTestRunListener extends RunListener {

  static String serviceurl = System.getenv("serviceurl");
  static String url = serviceurl != null ? serviceurl : "http://localhost:8080/rest";
  static WebClient webClient = WebClient.create(url);
  static ApiClassicService service;
  static ApiCGIService cgiService;
  static int numOfTestRuns = 0;

  static {
    System.out.println("SERVICE URL: " + System.getenv("serviceurl"));
    System.out.println("URL: " + url);
    System.out.println("CREATE NEW SERVICES");
    webClient.header(Finals.X_AUTH_TOKEN, "TEST_TOKEN");
    service = JAXRSClientFactory.fromClient(webClient, ApiClassicService.class, true);
    cgiService = JAXRSClientFactory.fromClient(webClient, ApiCGIService.class);
  }

  @Override
  public void testRunStarted(Description description) throws Exception {
    if (numOfTestRuns == 0) {
      System.out.println("TESTING STARTING... ");

      System.out.println("TESTING STARTED!");
    }
    super.testRunStarted(description);
    numOfTestRuns++;
  }

  @Override
  public void testRunFinished(Result result) throws Exception {
    numOfTestRuns--;
    super.testRunFinished(result);
    if (numOfTestRuns == 0) {
      System.out.println("ALL TESTS ARE FINISHED!");
    }
  }

}
