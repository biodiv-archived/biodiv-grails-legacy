dataSource {
    pooled = true
    driverClassName = "org.postgresql.Driver"
    username = "postgres"
    password = "postgres123"
//	logSql = true
}

hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}

// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:postgresql://localhost:5432/${appName}"
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
			url = "jdbc:postgresql://localhost:5432/${appName}"
		}
	}
}
