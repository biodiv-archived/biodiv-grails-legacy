package species
import org.hibernate.EmptyInterceptor ;
class MyEntityInterceptor extends org.hibernate.EmptyInterceptor {
    String onPrepareStatement(String sql) {
        sql = sql.replaceAll('##', ':');
        return sql;
    }
}
