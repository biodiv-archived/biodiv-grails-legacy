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
                <h5><g:message code="msg.Taxon.Concept" /></h5>
                <ul>
                    <li><a href="${uGroup.createLink('controller':'biodivAdmin', 'action':'updateGroups')}"><g:message code="msg.Update.Species.Groups" /></a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'updateExternalLinks')}"><g:message code="msg.Update.External.Links" /></a></li>
                </ul> 
            </div>

            <div>
                <h5><g:message code="msg.Recommendations" /></h5>
                <ul>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadNames')}"> <g:message code="msg.Sync.reco.names" /></a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadNamesIndex')}"> <g:message code="msg.Recreate.Names.Index" /></a></li>
                </ul> 
            </div>

            <div>
                <h5><g:message code="msg.Search" /></h5>
                <ul>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadSpeciesSearchIndex')}"><g:message code="msg.Reload.Species.Search.Index" /></a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadObservationsSearchIndex')}"><g:message code="msg.Reload.Observations.Search.Index" /></a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadUsersSearchIndex')}"><g:message code="msg.Reload.Users.Search.Index" /></a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadDocumentSearchIndex')}"><g:message code="msg.Reload.Documents.Search.Index" /></a></li>
                </ul>
            </div>
        </div>
    </body>
</html>
