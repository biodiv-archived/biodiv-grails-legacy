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
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadBiodivSearchIndex')}">Reload Biodiv Search Index</a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadSpeciesSearchIndex')}">Reload Species Search Index</a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadObservationsSearchIndex')}">Reload Observations Search Index</a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadUsersSearchIndex')}">Reload Users Search Index</a></li>
                    <li><a href="${uGroup.createLink(controller:'biodivAdmin', action:'reloadDocumentSearchIndex')}">Reload Documents Search Index</a></li>
                </ul>
            </div>
        </div>
    </body>
</html>
