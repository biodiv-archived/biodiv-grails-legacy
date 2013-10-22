<html>
    <head>
        <g:set var="title" value="Admin Console"/>
        <meta name="layout" content="main" />
        <title>${title}</title>
        <r:require modules="core" />
    </head>
    <body>
        <div class="span12">
            <div>
                <h5>Taxon Concept</h5>
                <ul>
                    <li><a href="${uGroup.createLink('controller':'biodivAdmin', 'action':'updateGroups')}">Update Species Groups</a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'updateExternalLinks')}">Update External Links</a></li>
                </ul> 
            </div>

            <div>
                <h5>Recommendations</h5>
                <ul>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadNames')}"> Sync reco names </a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadNamesIndex')}"> Recreate Names Index </a></li>
                </ul> 
            </div>

            <div>
                <h5>Search</h5>
                <ul>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadSpeciesSearchIndex')}">Reload Species Search Index</a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadObservationsSearchIndex')}">Reload Observations Search Index</a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadUsersSearchIndex')}">Reload Users Search Index</a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadDocumentSearchIndex')}">Reload Documents Search Index</a></li>
                </ul>
            </div>
        </div>
    </body>
</html>
