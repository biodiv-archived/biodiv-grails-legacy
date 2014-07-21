<%@page import="species.TaxonomyDefinition.TaxonomyRank"%>
<%@ page import="species.Species"%>
<%@ page import="species.Classification"%>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
<meta name="layout" content="main" />

<g:set var="entityName"
	value="${message(code: 'species.label', default: 'Species')}" />
<title>Taxonomy Browser</title>

<r:require modules="species_show"/>

</head>
<body>
        <div class="span12">
        <s:showSubmenuTemplate model="['entityName':'Taxonomy Browser']"/>

            <div class="taxonomyBrowser sidebar_section" style="position: relative;" data-name="classification" data-speciesid="${speciesInstance?.id}">
                <h5>Classifications</h5>	
                <div class="section help-block"> 
                    <ul>
                        <li>

                        Species page content is sourced from researchers and experts who have been authenticated by curators. If you are a researcher and would like to contribute, please expand the taxon browser, select the taxa for which you would like to be a contributor and request permission. Please also follow up by sending an email to  <span class="mailme">${grailsApplication.config.speciesPortal.ibp.supportEmail}</span> with a brief curriculum vitae (CV) or summary of your expertise so that curators can evaluate your application. You will be able to contribute once you have been allotted create/edit rights for the taxa.
                        </li>
                    </ul>
                </div>
 
                <div id="taxaHierarchy">

                    <%
                    def classifications = [];
                    Classification.list().each {
                    classifications.add([it.id, it, null]);
                    }
                    classifications = classifications.sort {return it[1].name};
                    %>

                    <g:render template="/common/taxonBrowserTemplate" model="['classifications':classifications, 'expandAll':false]"/>
                </div>
            </div>
            <g:render template="/species/inviteForContribution"/>
        </div>
        <!--div>
            <form id="searchPermission" action="${uGroup.createLink(action:'search', controller:'species', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" method="POST" class="form-horizontal">

                <div class="span12 super-section" style="clear:both;">
                    <div class="section">
                        <div class="control-group">
                            <label class="control-label span3" for="name">Revoke</label> 
                            <div class="pull-left" style="width:700px;margin-left:20px;">

                                <div class="input-prepend">
                                    <select id="rank" name="rank" class="add-on">
                                        <g:each in="${TaxonomyRank.list().reverse()}" var="rank">
                                        <option value="${rank.ordinal()}" ${(requestParams?requestParams.rank:-1) == rank?'selected':''}>${rank.value()}</option>
                                        </g:each>
                                    </select>

                                    <input id="page" 
                                    data-provide="typeahead" type="text" class="taxonRank" style=""
                                    name="page" value="" data-rank="${TaxonomyRank.SPECIES.ordinal()}"
                                    placeholder="Select taxon or" />
                                    <div id="nameSuggestions" style="display: block;position:relative;"></div>
                                </div>
                                    <sUser:selectUsers model="['id':'user']" /> 
                                <div id="errorMsg" class="alert hide"></div>
                            </div>
                        </div>  
                    </div>
                </div>   
                <div class="span12 submitButtons">
                    <a id="searchPermissions" class="btn btn-primary"
                        style="float: right; margin-right: 5px;">Search</a>

                    <a id="revokeSpeciesPermission" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> Revoke</a>
                </div>

            </form>
        </div-->

        <script type="text/javascript">
        var taxonRanks = [];
            <g:each in="${TaxonomyRank.list()}" var="t">
            taxonRanks.push({value:"${t.ordinal()}", text:"${t.value()}"});
            </g:each>

            </script>	

        <r:script>
        $(document).ready(function() {
            var taxonBrowser = $('.taxonomyBrowser').taxonhierarchy({
                expandAll:false
            });	
            /*$("#searchPermission").autofillNames({
                'appendTo' : '#nameSuggestions',
                'nameFilter':'scientificNames',
                focus: function( event, ui ) {
                    $("#canName").val("");
                    $("#page").val( ui.item.label.replace(/<.*?>/g,"") );
                    $("#nameSuggestions li a").css('border', 0);
                    return false;
                },
                select: function( event, ui ) {
                    $("#page").val( ui.item.label.replace(/<.*?>/g,"") );
                    $("#canName").val( ui.item.value );
                    $("#mappedRecoNameForcanName").val(ui.item.label.replace(/<.*?>/g,""));
                    return false;
                },open: function(event, ui) {
                    //$("#nameSuggestions ul").removeAttr('style').css({'display': 'block','width':'300px'}); 
                }
            });

            var users_autofillUsersComp = $("#userAndEmailList_user").autofillUsers({
                usersUrl : window.params.userTermsUrl
            });

            $('#searchPermission').click(function() {
                var params = {};
                $("#addSpecies input").each(function(index, ele) {
                    if($(ele).val().trim()) params[$(ele).attr('name')] = $(ele).val().trim();
                });
                params['rank'] = $('#rank').find(":selected").val(); 
                $.ajax({
                    url:'/species/searchPermission',
                    data:params,
                    method:'POST',
                    dataType:'json',
                    success:function(data) {
                    console.log(data);
                        if(data.success == true) {

                        } else {
                            $('#errorMsg').removeClass('alert-info hide').addClass('alert-error').text(data.msg);
                        }
                    }, error: function(xhr, status, error) {
                        handleError(xhr, status, error, this.success, function() {
                            var msg = $.parseJSON(xhr.responseText);
                            $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
                        });
                    }
                });
            });*/
        });
        </r:script>
    </body>
</html>
