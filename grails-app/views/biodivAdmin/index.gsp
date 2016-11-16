<%@page import="species.UtilsService"%>
<%@page import="java.util.*"%>
<%@page import="java.lang.*"%>
<%@page import="species.groups.UserGroup"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'default.pagetitle.admin.console')}"/>
        <meta name="layout" content="main" />
        <title>${title}</title>
    
      </head>
    <body>
    <% String cgroup=(params.webaddress)?params.webaddress:'ibp'; %>
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
        <% def userFilePath=grailsApplication.config.speciesPortal.userDetailsFilePath %>
        <div>
            <h5>User Profile Download</h5>
            <ul>
                <li><td><a class="btn btn-mini" href="${uGroup.createLink(action:'downloadUserFile', controller:'UFile', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress, 'params':[downloadFile:userFilePath])}"><g:message code="button.download.user" /></a></td></li>
            </ul>
        </div>
        <h5>Banner Message</h5>

        <form class="form-horizontal" id="frmgroup" method="post" action="${uGroup.createLink(controller:'biodivAdmin', action:'getMessage')}"> 
         <select name=groupId class=groupId>
         <option value="">Select Group</option>
         <option value="ibp">All Group</option>
            <g:each in="${UserGroup.list()}">
            <g:if test="${it.webaddress==getGroup}">
                <option value="${it.webaddress}" selected>${it.name}</option>
                </g:if>
            </g:each>
            <g:each in="${UserGroup.list()}">
                <option value="${it.webaddress}">${it.name}</option>
            </g:each>
        </select>
        </form>

        <div class="banner_content">
        <g:if test="${getMessage==null&&getGroup!=null}">
        <input type="button" value="Add New" name="addnew" class="btn-primary" id="addnew" />
        </g:if>
        <g:if test="${getMessage!=null&&getGroup!=null}">
        <div class="well">${getMessage}
        <input type="button" value="Edit" name="edit" id="edit" class="btn-primary" />
        </g:if>
        </div>
        </div>
        <div class="banner_editor">
        <form class="form-horizontal" id="frmcontent" class="frmcontent" method="post" action="${uGroup.createLink(controller:'biodivAdmin', action:'contentupdate')}"> 
        <input type="hidden" name="groupName" value="${getGroup}"/>
            <textarea name="content" class="content">
                   ${getMessage}
            </textarea>
         <!--   <p class="muted">Message Format Group Name - Banner Message(eg:spiderindia - Spider India Banner Message)</p> -->
            <input type="submit" value="Save" class="btn btn-success" id="savebtn" />  
        </form>
        </div>
</div>
</div>
       <asset:script>
$( document ).ready(function() {
    $(".banner_editor").hide();
    $(".groupId" ).change(function() {   
        this.form.submit();
    });

    $("#addnew" ).click(function() {   
    $(".banner_editor").show();
    });

    $("#edit" ).click(function() {   
    $(".banner_editor").show();
    });
               
}); 
</asset:script>
<asset:script>
CKEDITOR.plugins.addExternal( 'confighelper', "${assetPath(src:'ckeditor/confighelper/plugin.js')}" );
var config = { extraPlugins: 'confighelper', toolbar:'EditorToolbar', toolbar_EditorToolbar:[[ 'Bold', 'Italic','Link','Unlink', 'Styles','Format']],

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

