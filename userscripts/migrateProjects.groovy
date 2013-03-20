import content.*

def projectservice = ctx.getBean("projectService");

projectservice.migrateProjects()
