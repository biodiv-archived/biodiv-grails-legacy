import content.*

def projectservice = ctx.getBean("migrationService");

projectservice.migrateProjects()
