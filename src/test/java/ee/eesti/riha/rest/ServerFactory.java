package ee.eesti.riha.rest;

import java.util.List;

import javax.annotation.PreDestroy;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartFactoryBean;

public class ServerFactory implements SmartFactoryBean<Server>, DisposableBean {

  private Server mServer;

  private Object provider;

  private List<Object> resourceClasses;

  private String endpoint;

  @Override
  public Server getObject() throws Exception {

    if (mServer != null) {
      return mServer;
    } else {
      return buildServer();
    }

  } // -getObject

  private Server buildServer() {

    JAXRSServerFactoryBean sf = new JAXRSServerFactoryBean();

    // FIXME
    for (Object o : resourceClasses) {
      sf.setResourceClasses(o.getClass());
    }

    sf.setProvider(provider);

    // sf.setResourceProvider(JoumaailmImpl.class,
    // new SingletonResourceProvider(new JoumaailmImpl(), true));

    sf.setAddress(endpoint);

    return sf.create();

  } // -buildServer

  @Override
  public Class<?> getObjectType() {
    return Server.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public <T extends Object> void setProvider(T provider) {
    this.provider = provider;
  }

  public void setResourceClasses(List<Object> resourceClasses) {
    this.resourceClasses = resourceClasses;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public boolean isPrototype() {
    return false;
  }

  @Override
  public boolean isEagerInit() {
    return true;
  }

  // FIXME doesn't work
  @PreDestroy
  @Override
  public void destroy() throws Exception {
    mServer.stop();
    mServer.destroy();
    System.err.println("........ DESTROY");
  }
}
