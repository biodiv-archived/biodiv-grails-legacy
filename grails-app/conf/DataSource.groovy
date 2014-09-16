dataSource {
    pooled = true
    
    //Added bu hibernatespatial plugin
// //     driverClassName = "org.postgis.DriverWrapper"
    dialect = org.hibernatespatial.postgis.PostgisDialect
//    logSql = true
    properties {
        //TODO: following params to be enabled after testing for connection leak
        //maxActive = 50
        //maxIdle = 25
        //minIdle = 5
        //initialSize = 5
        validationQuery="SELECT 1"
        testOnBorrow=true
        testOnReturn=false
        testWhileIdle=true

        timeBetweenEvictionRunsMillis = 1000 * 60 * 15
		numTestsPerEvictionRun=3
		minEvictableIdleTimeMillis=1000 * 60 * 5
        //        maxWait = 30000
    }
}

hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory' // For Hibernate before 4.0
    //cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}

// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:log4jdbc:postgresql://127.0.0.1:5432/${appName}"
        }
    }
    test {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:postgresql://localhost:5432/${appName}"
        }
    }
    production {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:postgresql://localhost:5432/${appName}"
        }
    }
    saturn {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:postgresql://localhost:5432/${appName}"
        }
    }
    pamba {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:log4jdbc:postgresql://localhost:5432/${appName}"
        }
    }
    pambaTest {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:postgresql://localhost:5432/${appName}"
        }
    }
    kk {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:log4jdbc:postgresql://localhost:5432/${appName}"
        }
    }
}

/* Added by the Hibernate Spatial Plugin. */
dataSource {
   //driverClassName = "org.postgresql.Driver"
   //driverClassName = "net.sf.log4jdbc.DriverSpy"
   driverClassName = "net.sf.log4jdbc.sql.jdbcapi.DriverSpy"
}
