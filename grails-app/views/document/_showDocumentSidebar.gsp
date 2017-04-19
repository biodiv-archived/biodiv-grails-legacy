<%@ page import="content.Project"%>
<%@ page import="content.eml.Document"%>
<%@ page import="grails.converters.JSON"%>
<%@ page import="species.utils.Utils"%>
<%@ page import="species.TaxonomyDefinition"%>
<%@ page import="species.Species"%>
<%@ page import="species.UserGroupTagLib"%>
<%@ page import="content.eml.DocumentTokenUrl"%>
<%
Map sciNames = documentInstance.fetchSciNames();
Map nameValue =  sciNames.nameValues
Map nameParseValues = sciNames.nameparseValue
Map nameId = sciNames.nameDisplayValues
Map primaryName=sciNames.primaryName
def docId = DocumentTokenUrl.findByDoc(documentInstance)
String status = docId?.status

%>
<div  class="span4">
<g:if test="${documentInstance.longitude!=76.658279 && documentInstance.latitude!=0.0}">
<g:render template="/document/showDocumentLocationTemplate" model="['documentInstance':documentInstance]"></g:render>
</g:if>
    <div style = "clear:both; border:1px solid #CECECE;overflow:hidden"><h5> Taxa mentioned in this document </h5></div>
    <div id="gnrdscientificNamesList" class="sidebar_section pre-scrollable" style="clear:both; border:1px solid #CECECE;overflow:x">
        <table class="table table-bordered table-condensed table-striped ">
            <g:if test="${status == "Executing"}">
            <b><i style="color:blue"> Please wait for a moment while we are processing your document ... </i></b>
            </g:if>
            <g:elseif test="${status == "Failed"}">
            <b><i style="color:red"> Failed to get the Scientific names </i></b>
            </g:elseif>
            <g:else>
            <tbody >
                <g:each in="${nameValue}" var="sciName">
                <tr id= "docSciName_${nameId[sciName.key]}">
                    <% def primary=primaryName[nameId[sciName.key]] %>
                    <sUser:permToReorderDocNames model="['documentInstance':documentInstance]">
                        <td><i class="icon-remove pull-left " data-val="${nameId[sciName.key]}" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'docSciNamesDelete', 'userGroupWebaddress':webaddress)}","${nameId[sciName.key]}", "down", "docSciName", "${documentInstance.id}");'></i> </td>
                        <td><i class="icon-edit pull-left " data-val="${nameId[sciName.key]}"></i>
                            <i class="icon-ok" id="ok_${nameId[sciName.key]}" data-val="${nameId[sciName.key]}" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'docSciNamesEdit', 'userGroupWebaddress':webaddress)}","${nameId[sciName.key]}", document.getElementById("txtSciName_${nameId[sciName.key]}").value, "${documentInstance.id}");'></i>
                         </td>
                         <g:if test="${primary==1}">
                        <td><i class="primary icon-star pull-left" id="balckstar_${nameId[sciName.key]}" data-val="${nameId[sciName.key]}" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'docSciNamesPrimaraydelete', 'userGroupWebaddress':webaddress)}","${nameId[sciName.key]}", "docSciName", "${documentInstance.id}");'></i></td>
                        </g:if>
                        <g:else>
                        <td><i class="primary icon-star-empty pull-left" id="star_${nameId[sciName.key]}" data-val="${nameId[sciName.key]}" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'docSciNamesPrimaray', 'userGroupWebaddress':webaddress)}","${nameId[sciName.key]}", "docSciName", "${documentInstance.id}");'></i></td>
                        </g:else>
                        </sUser:permToReorderDocNames>
                    <td>
                        <%
                        def taxaObj = nameParseValues[sciName.key]?nameParseValues[sciName.key]:null
                        def speciesObjId = taxaObj?taxaObj.findSpeciesId():null;
                        def link = ""
                        if(speciesObjId) {
                        link = uGroup.createLink(controller:'species', action:'show', id:speciesObjId, 'userGroupWebaddress':webaddress, absolute:true)
                        link = link.replace("%5B", "");
                        link = link.replace("%5D", "");
                        }
                        %>
                        <div class="editNameClick_${nameId[sciName.key]}">
                        <g:if test="${speciesObjId}">
                        <a class="species-page-link" style="font-style: normal;" href= "${link}">${sciName.key}</a>
                       <!-- <sUser:permToReorderDocNames model="['documentInstance':documentInstance]">
                       // <i class="icon-circle-arrow-down pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'changeDisplayOrder', 'userGroupWebaddress':webaddress)}","${nameId[sciName.key]}", "down", "docSciName", "${documentInstance.id}")'></i>
                        //<i class="icon-circle-arrow-up pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'changeDisplayOrder', 'userGroupWebaddress':webaddress)}", "${nameId[sciName.key]}", "up", "docSciName", "${documentInstance.id}")'></i>
                        //</sUser:permToReorderDocNames> -->
                        </g:if>
                        <g:else>
                       <!-- //<sUser:permToReorderDocNames model="['documentInstance':documentInstance]">
                        //<i class="icon-circle-arrow-down pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'changeDisplayOrder', 'userGroupWebaddress':webaddress)}","${nameId[sciName.key]}", "down", "docSciName", "${documentInstance.id}")'></i>
                        //<i class="icon-circle-arrow-up pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'changeDisplayOrder', 'userGroupWebaddress':webaddress)}", "${nameId[sciName.key]}", "up", "docSciName", "${documentInstance.id}")'></i>
                        //</sUser:permToReorderDocNames> -->
                        ${sciName.key}
                        </g:else>
                        </div>
                        <div class="nameEdit"> 
                        <input type=text name="txtSciName" id="txtSciName_${nameId[sciName.key]}" class="txtSciName" value="${sciName.key}" />
                        </div>
                    </td>
                    <td>
                        ${sciName.value}
                    </td>
                </tr>
                </g:each>
            </tbody>
            </g:else>
        </table>
 <asset:script>
$(document).ready(function() {
                $('.txtSciName').hide();
                $('.icon-ok').hide();
                
});
        $(".icon-edit").click(function(){
             $('.txtSciName').hide();
             $(".icon-ok").hide();
             $(this).show(); 
            var txtName=$(this).data('val');
            $('.editNameClick_'+txtName).hide();
            $('#txtSciName_'+txtName).show();
            $(this).hide(); 
            $('#ok_'+txtName).show();
        });
        $(".icon-ok").click(function(){
            var txtName=$(this).data('val');
            $('.editNameClick_'+txtName).show();
            $('.editNameClick_'+txtName).text($('#txtSciName_'+txtName).val());
             $('#ok_'+txtName).hide();
             $(".icon-edit").show();
             $('#txtSciName_'+txtName).hide();
        });
        $(".icon-remove").click(function(){
            var docName=$(this).data('val');
           $('#docSciName_'+docName).hide();
        });
       
         $(".primary").click(function(){
            var that = $(this);
           if(that.hasClass( "icon-star" )){
                that.removeClass('icon-star').addClass('icon-star-empty');//$("#star_"+docName).attr('class','icon-star-empty pull-left')
           }
           else{
                that.removeClass('icon-star-empty').addClass('icon-star');
           }
        });
</asset:script>
    </div>
</div>
