<%@ page import="species.Species"%>
<%@ page import="species.TaxonomyDefinition"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'default.species.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="observations_create"/>
        <style>
            #addSpecies .add-on {
            height:20px;
            width:98px;
            }
            #addSpecies select.add-on {
            height:30px;
            width:110px;
            }


            #addSpecies .taxonRank {
            min-height:20px;
            width:575px;   
            }
        </style>
    </head>
    <body>

        <div class="observation_create">
            <h1> <g:message code="species.create.add.species" /> </h1>
            <g:hasErrors bean="${speciesInstance}">
            <i class="icon-warning-sign"></i>
            <span class="label label-important"> <g:message
                code="fix.errors.before.proceeding" default="Fix errors" /> </span>
            <%--<g:renderErrors bean="${speciesInstance}" as="list" />--%>
            </g:hasErrors>

            <form id="addSpecies" action="${uGroup.createLink(action:'save', controller:'species', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" method="POST" class="form-horizontal">

                <div class="span12 super-section" style="clear:both;">

            <div class="section help-block"> 
                <ul>
                    <li>
                    <g:message code="species.create.contributors.create.rights" /> <a href="${uGroup.createLink('controller':'species', 'action':'taxonBrowser')}"><g:message code="msg.here" /></a>.
                    </li>
                    <li>
                   <g:message code="species.create.validate.species.name" /> 
                    </li>
                </ul>
           </div>

                    <div class="section">
                    <div class="control-group">
                        <label class="control-label span3" for="name"><g:message code="default.add.page.label" /> </label> 
                        <div class="pull-left" style="width:700px;margin-left:20px;">
 
                            <div class="input-prepend">
                                <select id="rank" name="rank" class="add-on">
                                <g:each in="${TaxonomyRank.list().reverse()}" var="rank">
                                    <option value="${rank.ordinal()}" ${(requestParams?requestParams.rank:-1) == rank?'selected':''}><g:message error="${rank}"/></option>
                                </g:each>
                            </select>

                            <input id="page" 
                            data-provide="typeahead" type="text" class="taxonRank" style=""
                            name="page" value="${requestParams?requestParams.speciesName:''}" data-rank="${requestParams?requestParams.rank:TaxonomyRank.SPECIES.ordinal()}"
                            placeholder="${g.message(code:'placeholder.create.add')}" />
                            <input type="hidden" name="canName" id="canName" value=""/>
                            <div id="nameSuggestions" style="display: block;position:relative;"></div>
                            <input type="hidden" name="lang" value="${lang?:params.lang}"/>

                        </div>
                            <div id="errorMsg" class="alert hide"></div>
                        </div>
                    </div>  
                    <g:render template="/common/createTaxonRegistryTemplate" model='[requestParams:requestParams, errors:errors]'/>
                    </div>

                </div>   
                <div class="span12 submitButtons">

                    <g:if test="${speciesInstance?.id}">
                    <a href="${uGroup.createLink(controller:params.controller, action:'show', id:speciesInstance.id)}" class="btn"
                        style="float: right; margin-right: 30px;"> <g:message code="button.cancel" /> </a>
                    </g:if>
                    <g:else>
                    <a href="${uGroup.createLink(controller:params.controller, action:'list')}" class="btn"
                        style="float: right; margin-right: 30px;"> <g:message code="button.cancel" /> </a>
                    </g:else>
                    <a id="validateSpeciesSubmit" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> <g:message code="button.validate" /></a>


                    <a id="addSpeciesSubmit" class="btn btn-primary"
                        style="float: right; margin-right: 5px;display:none;"> <g:message code="default.add.page.label" /></a>

                </div>

            </form>
        </div>

    </body>
    <r:script>
    $(document).ready(function() {
        $("#page").autofillNames({
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

        var taxonRanks = [];

        <g:each in="${TaxonomyRank.list()}" var="t">
        <g:if test="${t == TaxonomyRank.SUB_GENUS || t == TaxonomyRank.SUB_FAMILY}">
        taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}", mandatory:false, taxonValue:"${requestParams?requestParams.taxonRegistryNames[t.ordinal()]:''}"});
        </g:if>
        <g:else>
        taxonRanks.push({value:"${t.ordinal()}", text:"${g.message(error:t)}", mandatory:true, taxonValue:"${requestParams?requestParams.taxonRegistryNames[t.ordinal()]:''}"});
        </g:else>
        </g:each>

        var text1 = $('#page').data('rank');
        $('#rank option').filter(function() {
            return $(this).val() == text1; 
        }).prop('selected', true);

        $('#rank').change(function() {
            $('#page').attr('data-rank', $('#rank').find(':selected').val());
        });

        <g:if test="${requestParams}">
            var $hier = $('#taxonHierachyInput');
            $hier.empty();
            var rank = <%=requestParams?requestParams.rank:0%> ;
            for (var i=0; i<rank; i++) {
                $('<div class="input-prepend"><span class="add-on">'+taxonRanks[i].text+(taxonRanks[i].mandatory?'*':'')+'</span><input data-provide="typeahead" data-rank ="'+taxonRanks[i].value+'" type="text" class="taxonRank" name="taxonRegistry.'+taxonRanks[i].value+'" value="'+taxonRanks[i].taxonValue+'" placeholder="Add '+taxonRanks[i].text+'" /></div>').appendTo($hier);
            }
            if(rank > 0) $('#taxonHierarchyInputForm').show();
            $('#addSpeciesSubmit').show();
        </g:if>

        if($(".taxonRank:not(#page)").length > 0)
            $(".taxonRank:not(#page)").autofillNames();

        $('#validateSpeciesSubmit').click(function() {
            var params = {};
            $("#addSpecies input").each(function(index, ele) {
                if($(ele).val().trim()) params[$(ele).attr('name')] = $(ele).val().trim();
            });
            params['rank'] = $('#rank').find(":selected").val(); 
            //Did u mean species 
            $.ajax({
                url:'/species/validate',
                data:params,
                method:'POST',
                dataType:'json',
                success:function(data) {
                    if(data.success == true) {
                        if(data.id) {
                            window.location.href = '/species/show/'+data.id+'?editMode=true'
                            return;
                            //data.msg += "Did you mean <a href='/species/show/"+data.id+"'>"+data.name+"</a>?"
                        }
                        $('#errorMsg').removeClass('alert-error hide').addClass('alert-info').html(data.msg);
                        //$('#validateSpeciesSubmit').hide()
                        var $ul = $('<ul></ul>');
                        $('#existingHierarchies').empty().append($ul);
                        if(data.taxonRegistry) {
                            $.each(data.taxonRegistry, function(index, value) {
                                var $c = $('<li></li>');
                                $ul.append($c);
                                var $u = $('<ul><b>'+index+'</b></ul>');
                                $c.append($u);
                                $.each(value[0], function(i, v) {
                                    $u.append('<li>'+v.rank+' : '+v.name+'</li>');
                                });
                            });
                        }
                        
                        $('#existingHierarchies').append('<div>If you have a new or a different classification please provide it below.</div>');
                        var $hier = $('#taxonHierachyInput');
                        $hier.empty()
                        for (var i=0; i<data.rank; i++) {
                            var taxonRegistry = data.requestParams? data.requestParams.taxonRegistry:undefined;
                            var taxonValue = (taxonRegistry && taxonRegistry[i]) ?taxonRegistry[i]:taxonRanks[i].taxonValue;
                            $('<div class="input-prepend"><span class="add-on">'+taxonRanks[i].text+(taxonRanks[i].mandatory?'*':'')+'</span><input data-provide="typeahead" data-rank ="'+taxonRanks[i].value+'" type="text" class="taxonRank" name="taxonRegistry.'+taxonRanks[i].value+'" value="'+taxonValue+'" placeholder="Add '+taxonRanks[i].text+'" /></div>').appendTo($hier);
                        }
                        if(data.rank > 0) $('#taxonHierarchyInputForm').show();

                        if($(".taxonRank:not(#page)").length > 0)
                            $(".taxonRank:not(#page)").autofillNames();


                        $('#addSpeciesSubmit').show();
                    } else {
                        if(data.status == 'requirePermission') 
                            window.location.href = '/species/contribute'
                        else 
                            $('#errorMsg').removeClass('alert-info hide').addClass('alert-error').text(data.msg);
                    }
                }, error: function(xhr, status, error) {
                    handleError(xhr, status, error, this.success, function() {
                        var msg = $.parseJSON(xhr.responseText);
                        $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
                    });
                }
            });
            //get COL hierarchy 
            // get and autofill author contrib hierarchy
        });

        $('#addSpeciesSubmit').click(function() {
            $('#addSpecies').submit();
        });
    });
    </r:script>
</html>
