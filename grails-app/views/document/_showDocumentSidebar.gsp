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
    def docId = DocumentTokenUrl.findByDoc(documentInstance)
    String status = docId.status
%>
<r:require modules="pages" />
<div  class="span4">
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

       	      <td>
                <%
                  def taxaObj = nameParseValues[sciName.key]?TaxonomyDefinition.findByCanonicalForm(nameParseValues[sciName.key]):null
                  def speciesObjId = taxaObj?Species.findByTaxonConcept(taxaObj)?.id:null;
                  def link = ""
                  if(speciesObjId) {
                      link = uGroup.createLink(controller:'species', action:'show', id:speciesObjId, 'userGroupWebaddress':webaddress, absolute:true)
                      link = link.replace("%5B", "");
                      link = link.replace("%5D", "");
                  }
                %>
                <g:if test="${speciesObjId}">
                    <a class="species-page-link" style="font-style: normal;" href= "${link}">${sciName.key}</a>
                    <sUser:permToReorderDocNames model="['documentInstance':documentInstance]">
                       <i class="icon-circle-arrow-down pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'changeDisplayOrder', 'userGroup':webaddress)}","${nameId[sciName.key]}", "down", "docSciName", "${documentInstance.id}")'></i>
                       <i class="icon-circle-arrow-up pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'changeDisplayOrder', 'userGroup':webaddress)}", "${nameId[sciName.key]}", "up", "docSciName", "${documentInstance.id}")'></i>
                    </sUser:permToReorderDocNames>
                </g:if>
                <g:else>
                  <sUser:permToReorderDocNames model="['documentInstance':documentInstance]">
                       <i class="icon-circle-arrow-down pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'changeDisplayOrder', 'userGroup':webaddress)}","${nameId[sciName.key]}", "down", "docSciName", "${documentInstance.id}")'></i>
                       <i class="icon-circle-arrow-up pull-right" onclick='changeDisplayOrder("${uGroup.createLink(controller: 'document', action:'changeDisplayOrder', 'userGroup':webaddress)}", "${nameId[sciName.key]}", "up", "docSciName", "${documentInstance.id}")'></i>
                  </sUser:permToReorderDocNames>
                    ${sciName.key}
                </g:else>
              </td>
        	    <td>
             	    ${sciName.value}
 			        </td>
        
          </tr>
        </g:each>
      </tbody>
    </g:else>
</table>

</div>
</div>