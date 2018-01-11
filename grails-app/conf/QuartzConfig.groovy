
quartz {
    autoStartup = true
    jdbcStore = false
    waitForJobsToCompleteOnShutdown = true
    exposeSchedulerInRepository = false

    props {
        scheduler.skipUpdateCheck = true
    }
}

environments {
    development {
        quartz {
            autoStartup = false
        }
    }
}
