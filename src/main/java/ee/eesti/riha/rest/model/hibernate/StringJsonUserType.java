package ee.eesti.riha.rest.model.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

// TODO: Auto-generated Javadoc
/**
 * The Class StringJsonUserType.
 */
public class StringJsonUserType implements UserType<String> {

  /**
   * Return the SQL type code for the column mapped by this type.
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
  public Class<String> returnedClass() {
    return String.class;
  }

  /**
   * Compare two instances of the class mapped by this type for persistence "equality". Equality of the persistent
   * state.
   *
   * @param x the x
   * @param y the y
   * @return boolean
   */
  @Override
  public boolean equals(String x, String y) {
    if (x == null) {
      return y == null;
    }
    return x.equals(y);
  }

  /**
   * Get a hashcode for the instance, consistent with persistence "equality".
   *
   * @param x the x
   * @return the int
   */
  @Override
  public int hashCode(String x) {
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
   * @return String
   * @throws org.hibernate.HibernateException the hibernate exception
   * @throws java.sql.SQLException the SQL exception
   */
  @Override
  public String nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner)
      throws HibernateException, SQLException {
    return rs.getString(position);
  }

  /**
   * Write an instance of the mapped class to a prepared statement. Implementors should handle possibility of null
   * values. A multi-column type should be written to parameters starting from <tt>index</tt>.
   *
   * @param st a JDBC prepared statement
   * @param value the object to write
   * @param index statement parameter index
   * @param session the session
   * @throws org.hibernate.HibernateException the hibernate exception
   * @throws java.sql.SQLException the SQL exception
   */
  @Override
  public void nullSafeSet(PreparedStatement st, String value, int index, SharedSessionContractImplementor session)
      throws HibernateException, SQLException {
    if (value == null) {
      st.setNull(index, Types.OTHER);
      return;
    }
    st.setObject(index, value, Types.OTHER);
  }

  /**
   * Return a deep copy of the persistent state, stopping at entities and at collections. It is not necessary to copy
   * immutable objects, or null values, in which case it is safe to simply return the argument.
   *
   * @param value the object to be cloned, which may be null
   * @return String a copy
   */
  @Override
  public String deepCopy(String value) {
    return value; // Strings are immutable
  }

  /**
   * Are objects of this type mutable?.
   *
   * @return boolean
   */
  @Override
  public boolean isMutable() {
    return false; // Strings are immutable
  }

  /**
   * Transform the object into its cacheable representation. At the very least this method should perform a deep copy if
   * the type is mutable. That may not be enough for some implementations, however; for example, associations must be
   * cached as identifier values. (optional operation)
   *
   * @param value the object to be cached
   * @return a cachable representation of the object
   * @throws org.hibernate.HibernateException the hibernate exception
   */
  @Override
  public Serializable disassemble(String value) {
    return value; // Strings are already serializable
  }

  /**
   * Reconstruct an object from the cacheable representation. At the very least this method should perform a deep copy
   * if the type is mutable. (optional operation)
   *
   * @param cached the object to be cached
   * @param owner the owner of the cached object
   * @return a reconstructed object from the cachable representation
   * @throws org.hibernate.HibernateException the hibernate exception
   */
  @Override
  public String assemble(Serializable cached, Object owner) {
    return (String) cached;
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
  public String replace(String original, String target, Object owner) {
    return original; // Strings are immutable
  }
}
