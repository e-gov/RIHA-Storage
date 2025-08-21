package ee.eesti.riha.rest.model.hibernate;

import com.google.gson.JsonObject;
import ee.eesti.riha.rest.logic.util.JsonHelper;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This class allows to map {@link com.google.gson.JsonObject} to postgresql datatype jsonp. It is based on
 * {@link StringJsonUserType}
 *
 */
public class JsonObjectUserType implements UserType<JsonObject> {

  /**
   * Return the SQL type codes for the columns mapped by this type. The codes are defined on <tt>java.sql.Types</tt>.
   *
   * @return int the typecode
   * @see java.sql.Types
   */
  @Override
  public int getSqlType() {
    return Types.OTHER;
  }

  /**
   * The class returned by <tt>nullSafeGet()</tt>.
   *
   * @return Class
   */
  @Override
  public Class<JsonObject> returnedClass() {
    return JsonObject.class;
  }

  /**
   * Compare two instances of the class mapped by this type for persistence "equality". Equality of the persistent
   * state.
   *
   * @param x
   * @param y
   * @return boolean
   */
  @Override
  public boolean equals(JsonObject x, JsonObject y) {
    if (x == null) {
      return y == null;
    }
    return x.equals(y);
  }

  /**
   * Get a hashcode for the instance, consistent with persistence "equality"
   */
  @Override
  public int hashCode(JsonObject x) {
    return x == null ? 0 : x.hashCode();
  }

  /**
   * Retrieve an instance of the mapped class from a JDBC resultset. Implementors should handle possibility of null
   * values.
   *
   * @param rs a JDBC result set
   * @param position the column position
   * @param session the session
   * @param owner the containing entity
   * @return Object
   * @throws org.hibernate.HibernateException
   * @throws java.sql.SQLException
   */
  @Override
  public JsonObject nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
      throws HibernateException, SQLException {
    String value = rs.getString(position);
    if (value == null) {
      return null;
    }
    return JsonHelper.GSON.fromJson(value, JsonObject.class);
  }

  /**
   * Write an instance of the mapped class to a prepared statement. Implementors should handle possibility of null
   * values. A multi-column type should be written to parameters starting from <tt>index</tt>.
   *
   * @param st a JDBC prepared statement
   * @param value the object to write
   * @param index statement parameter index
   * @param session the session
   * @throws org.hibernate.HibernateException
   * @throws java.sql.SQLException
   */
  @Override
  public void nullSafeSet(PreparedStatement st, JsonObject value, int index, SharedSessionContractImplementor session)
      throws HibernateException, SQLException {
    if (value == null) {
      st.setNull(index, Types.OTHER);
      return;
    }
    st.setObject(index, value.toString(), Types.OTHER);
  }

  /**
   * Return a deep copy of the persistent state, stopping at entities and at collections. It is not necessary to copy
   * immutable objects, or null values, in which case it is safe to simply return the argument.
   *
   * @param value the object to be cloned, which may be null
   * @return Object a copy
   */
  @Override
  public JsonObject deepCopy(JsonObject value) {
    if (value == null) {
      return null;
    }
    // JsonObject is mutable, so we need to create a copy
    return JsonHelper.GSON.fromJson(value.toString(), JsonObject.class);
  }

  /**
   * Are objects of this type mutable?
   *
   * @return boolean
   */
  @Override
  public boolean isMutable() {
    return true;
  }

  /**
   * Transform the object into its cacheable representation. At the very least this method should perform a deep copy if
   * the type is mutable. That may not be enough for some implementations, however; for example, associations must be
   * cached as identifier values. (optional operation)
   *
   * @param value the object to be cached
   * @return a cachable representation of the object
   * @throws org.hibernate.HibernateException
   */
  @Override
  public Serializable disassemble(JsonObject value) {
    return value == null ? null : value.toString();
  }

  /**
   * Reconstruct an object from the cacheable representation. At the very least this method should perform a deep copy
   * if the type is mutable. (optional operation)
   *
   * @param cached the object to be cached
   * @param owner the owner of the cached object
   * @return a reconstructed object from the cachable representation
   * @throws org.hibernate.HibernateException
   */
  @Override
  public JsonObject assemble(Serializable cached, Object owner) {
    if (cached == null) {
      return null;
    }
    return JsonHelper.GSON.fromJson((String) cached, JsonObject.class);
  }

  /**
   * During merge, replace the existing (target) value in the entity we are merging to with a new (original) value from
   * the detached entity we are merging. For immutable objects, or null values, it is safe to simply return the first
   * parameter. For mutable objects, it is safe to return a copy of the first parameter. For objects with component
   * values, it might make sense to recursively replace component values.
   *
   * @param original the value from the detached entity being merged
   * @param target the value in the managed entity
   * @param owner the owner
   * @return the value to be merged
   */
  @Override
  public JsonObject replace(JsonObject original, JsonObject target, Object owner) {
    return deepCopy(original);
  }
}
