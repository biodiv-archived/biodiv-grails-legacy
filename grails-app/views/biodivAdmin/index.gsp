<%@page import="species.UtilsService"%>
<%@page import="java.util.*"%>
<%@page import="java.lang.*"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'default.pagetitle.admin.console')}"/>
        <meta name="layout" content="main" />
        <title>${title}</title>
    
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
   
        <g:set var="utilsService" bean="utilsService"/>
        <h5>Banner Message</h5> 
        <form class="form-horizontal" id="frmcontent" method="post" action="${uGroup.createLink(controller:'biodivAdmin', action:'contentupdate')}"> 
            <textarea name="content" id=description>
                    <g:each in="${utilsService.getBannerMessages()}">
                    ${it.key} - ${it.value} </br>
                    </g:each>
            </textarea>
            <p class="muted">Message Format Group Name - Banner Message(eg:spiderindia - Spider India Banner Message)</p>
            <input type="submit" value="Save" class="btn btn-success" id="savebtn" />  
        </form>
</div>
</div>

<asset:script>
CKEDITOR.plugins.addExternal( 'confighelper', "${assetPath(src:'ckeditor/confighelper/plugin.js')}" );
var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic','Link','Unlink' ]],

        on :
        {
            instanceReady : function( ev )
            {
                // Output paragraphs as <p>Text</p>.
                this.dataProcessor.writer.setRules( 'p',
                    {
                        indent : false,
                        breakBeforeOpen : true,
                        breakAfterOpen : false,
                        breakBeforeClose : false,
                        breakAfterClose : true
                    });
            }
        }
    };
CKEDITOR.replace( 'content',config);
</asset:script>
</body>
</html>
