package ee.eesti.riha.rest.model.hibernate;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.type.StandardBasicTypes;

import java.sql.Types;

public class CustomDialectH2 extends H2Dialect {

    public CustomDialectH2(){
        super();
        this.registerColumnType(Types.JAVA_OBJECT, "json");

    }
}
