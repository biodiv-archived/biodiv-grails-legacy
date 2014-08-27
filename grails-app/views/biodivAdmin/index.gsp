<html>
    <head>
        <g:set var="title" value="${g.message(code:'default.pagetitle.admin.console')}"/>
        <meta name="layout" content="main" />
        <title>${title}</title>
        <r:require modules="core" />
    </head>
    <body>
        <div class="span12">
            <div>
                <h5><g:message code="biodivadmin.index.taxon.concept" /></h5>
                <ul>
                    <li><a href="${uGroup.createLink('controller':'biodivAdmin', 'action':'updateGroups')}"><g:message code="biodivadmin.index.update.species.groups" /></a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'updateExternalLinks')}"><g:message code="biodivadmin.index.update.external.links" /></a></li>
                </ul> 
            </div>

            <div>
                <h5><g:message code="link.recommendations" /></h5>
                <ul>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadNames')}"> <g:message code="biodivadmin.index.sync.reco.names" /></a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadNamesIndex')}"> <g:message code="biodivadmin.index.recreate.names.index" /></a></li>
                </ul> 
            </div>

            <div>
                <h5><g:message code="default.search" /></h5>
                <ul>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadSpeciesSearchIndex')}"><g:message code="biodivadmin.index.reload.species.search.index" /></a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadObservationsSearchIndex')}"><g:message code="biodivadmin.index.reload.observations.search.index" /></a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadUsersSearchIndex')}"><g:message code="biodivadmin.index.reload.users.search.index" /></a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadDocumentSearchIndex')}"><g:message code="biodivadmin.index.reload.documents.search.index" /></a></li>
                </ul>
            </div>
        </div>
    </body>
</html>
