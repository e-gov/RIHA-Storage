package ee.eesti.riha.rest.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * Allows to make REST calls from another server.
 * 
 * @see <a
 *      href="http://stackoverflow.com/questions/23450494/how-to-enable-cross-domain-requests-on-jax-rs-web-services">
 *      http://stackoverflow.com/questions/23450494/how-to-enable-cross-domain-requests-on-jax-rs-web-services
 </a>
 */
@Provider
public class CORSFilter implements ContainerResponseFilter {

  @Override
  public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext cres)
      throws IOException {
    // System2.out.println("filter works");
    cres.getHeaders().add("Access-Control-Allow-Origin", "*");
    cres.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
    cres.getHeaders().add("Access-Control-Allow-Credentials", "true");
    cres.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    cres.getHeaders().add("Access-Control-Max-Age", "1209600");
  }

}
