package species;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.util.ReflectHelper;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;



public class JSONType implements UserType {

  //  public Long aid;
  	// public String name;
  	// Long ro_id;
  	// String ro_type;
  	// String description;
  	// String is_migrated;
  	// String activity_performed;
  	// Boolean is_scientific_name;

    private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String JSON_TYPE = "json";

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException{

      // System.out.println("*****************y***********************************************");
      // System.out.println("*************************************************getting activityfeed json");
      // System.out.println("*************************************************getting activityfeed json");
      // System.out.println("*************************************************getting activityfeed json");
      // System.out.println("*************************************************getting activityfeed json");
      // System.out.println(rs.getString(names[0]));
      return rs.getString(names[0]);
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {


      // System.out.println("***********x*****************************************************");
      // System.out.println("*************************************************getting activityfeed json");
      // System.out.println("*************************************************getting activityfeed json");
      // System.out.println("*************************************************getting activityfeed json");
      // System.out.println("*************************************************getting activityfeed json");

      // final String json = value == null
      //       ? null
      //       : MAPPER.writeValueAsString(value);
      //       System.out.println(json);
      //       PGobject pgo = new PGobject();
      //     pgo.setType(JSON_TYPE);
      //     pgo.setValue(json);
      //     st.setObject(index, pgo);
      st.setObject(index, value, (value == null) ? Types.NULL : Types.OTHER);


  }
     @Override
     public int[] sqlTypes() {
         return new int[] { Types.OTHER };
     }

     //@SuppressWarnings("rawtypes")
     @Override
     public Class<Object> returnedClass() {
         return Object.class;
     }

     @Override
     public boolean equals(Object x, Object y) throws HibernateException {
         return (x != null) && x.equals(y);
     }

     @Override
     public int hashCode(Object x) throws HibernateException {
         return x.hashCode();
     }

//     @Override
//     public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor sessionImplementor, Object owner)
//         throws HibernateException, SQLException {
//         return rs.getString(names[0]);
//     }
//
//     @Override
//     public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor sessionImplementor)
//         throws HibernateException, SQLException {
//         st.setObject(index, value, (value == null) ? Types.NULL : Types.OTHER);
//     }
//
//     @Override
//     public Object deepCopy(Object value) throws HibernateException {
//         if (value == null) return null;
//         return new String((String)value);
//     }
//
     @Override
     public boolean isMutable() {
        return false;
     }
//
     @Override
     public Serializable disassemble(Object value) throws HibernateException {
         return (Serializable)value;
     }
//
     @Override
     public Object assemble(Serializable cached, Object owner)
        throws HibernateException {
         return cached;
     }

   @Override
    public Object replace(Object original, Object target, Object owner)
        throws HibernateException {
        return deepCopy(original);
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) return null;
        //return new String((String)value);
        return value;
    }

// }
  // @Override
  // public void setParameterValues(Properties parameters) {
  //   final String clazz = (String) parameters.get(CLASS);
  //   try {
  //     returnedClass = ReflectHelper.classForName(clazz);
  //   } catch (ClassNotFoundException e) {
  //     throw new IllegalArgumentException("Class: " + clazz
  //         + " is not a known class type.");
  //   }
  // }

}
