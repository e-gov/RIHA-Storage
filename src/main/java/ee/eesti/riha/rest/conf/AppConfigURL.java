package ee.eesti.riha.rest.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfigURL {

  @Value("${riharest.base.url}")
  private String restApiBaseUrl;

  public String getRestApiBaseUrl() {
    return restApiBaseUrl;
  }

  public void setRestApiBaseUrl(String restApiBaseUrl) {
    this.restApiBaseUrl = restApiBaseUrl;
  }

}
