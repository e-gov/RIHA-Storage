package ee.eesti.riha.rest.util;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * The Class PropsReader.
 */
public final class PropsReader {
  private static final Logger LOG = LoggerFactory.getLogger(PropsReader.class);

  // this where external riharest.project.properties should be
  // so that properties could be changed without recompiling
  // if you change it you must change it in src/main/resources/persistence-context.xml as well
  private static final String EXTERNAL_CONF_LOCATION = "/opt/tomcat/conf/";
  
  private static final String FILE_NAME = "riharest.project.properties";

  private static Properties props;

  private static void load() throws IOException {
    try {
//      props = PropertiesLoaderUtils.loadProperties(new ClassPathResource(FILE_NAME));
      // try to find properties from environment first, if not found then 
      // use local properties
      props = PropertiesLoaderUtils.loadProperties(
          new FileSystemResource(EXTERNAL_CONF_LOCATION + FILE_NAME));
    } catch (IOException e) {
      props = PropertiesLoaderUtils.loadProperties(new ClassPathResource(FILE_NAME));
    }
  }

  public static Properties get() throws IOException {
    if (props == null) {
      load();
    }
    return props;
  }

  public static String get(String property) {
    try {
      return get().getProperty(property);
    } catch (IOException e) {
      LOG.error("Error reading property", e);
      throw new RuntimeException(e);
    }
  }
}
