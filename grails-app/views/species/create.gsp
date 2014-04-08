<%@ page import="species.Species"%>
<%@ page import="species.TaxonomyDefinition"%>
<%@ page import="species.TaxonomyDefinition.TaxonomyRank"%>
<html>
    <head>
        <g:set var="title" value="Species"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
        <r:require modules="observations_create"/>
    </head>
    <body>

        <div class="observation_create">
            <h1> Add Species </h1>
            <g:hasErrors bean="${speciesInstance}">
            <i class="icon-warning-sign"></i>
            <span class="label label-important"> <g:message
                code="fix.errors.before.proceeding" default="Fix errors" /> </span>
            <%--<g:renderErrors bean="${speciesInstance}" as="list" />--%>
            </g:hasErrors>

            <form id="addSpecies" action="${uGroup.createLink(action:'save', controller:'species', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)}" method="POST" class="form-horizontal">

                <div class="span12 super-section" style="clear:both;">

                    <div class="control-group">
                        <label class="control-label span3" for="name">Add page for
                                                </label> 
                        <div class="span8">

                            <div class="input-prepend input-block-level">
                            <select id="rank" name="rank" class="add-on" style="height:auto;">
                                <g:each in="${TaxonomyRank.list()}" var="rank">
                                    <option value="${rank.ordinal()}">${rank.value()}</option>
                                </g:each>
                            </select>

                            <input id="page" 
                            data-provide="typeahead" type="text" class="input-block-level"
                            name="page" value="${page}"
                            placeholder="Add Page" />
                            <input type="hidden" name="canName" id="canName" value=""/>
                            <div id="nameSuggestions" style="display: block;position:relative;"></div>

                        </div>
                            <div id="errorMsg" class="alert hide"></div>
                        </div>
                    </div>  
                    <g:render template="/common/createTaxonRegistryTemplate"/>


                </div>   
                <div class="span12 submitButtons">

                    <g:if test="${speciesInstance?.id}">
                    <a href="${uGroup.createLink(controller:params.controller, action:'show', id:speciesInstance.id)}" class="btn"
                        style="float: right; margin-right: 30px;"> Cancel </a>
                    </g:if>
                    <g:else>
                    <a href="${uGroup.createLink(controller:params.controller, action:'list')}" class="btn"
                        style="float: right; margin-right: 30px;"> Cancel </a>
                    </g:else>
                    <a id="validateSpeciesSubmit" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> Validate</a>


                    <a id="addSpeciesSubmit" class="btn btn-primary"
                        style="float: right; margin-right: 5px;display:none;"> Add Page</a>

                </div>

            </form>
        </div>

    </body>
    <r:script>
    $(document).ready(function() {
        /*$("#species").autofillNames({
            'appendTo' : '#nameSuggestions',
            'nameFilter':'scientificNames',
            focus: function( event, ui ) {
                $("#canName").val("");
                $("#species").val( ui.item.label.replace(/<.*?>/g,"") );
                $("#nameSuggestions li a").css('border', 0);
                return false;
            },
            select: function( event, ui ) {
                $("#species").val( ui.item.label.replace(/<.*?>/g,"") );
                $("#canName").val( ui.item.value );
                $("#mappedRecoNameForcanName").val(ui.item.label.replace(/<.*?>/g,""));
                return false;
            },open: function(event, ui) {
                $("#nameSuggestions ul").removeAttr('style').css({'display': 'block','width':'300px'}); 
            }
        });*/

        var taxonRanks = [];
        <g:each in="${TaxonomyRank.list()}" var="t">
        taxonRanks.push({value:"${t.ordinal()}", text:"${t.value()}"});
        </g:each>


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
                        data.msg += "Did you mean <a href='/species/show/"+data.id+"'>"+data.name+"</a>?"
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
                            $('<div class="input-prepend input-block-level"><span class="add-on">'+taxonRanks[i].text+'</span><input data-provide="typeahead" data-rank ="'+taxonRanks[i].value+'" type="text" class="input-block-level taxonRank" name="taxonRegistry.'+taxonRanks[i].value+'" value="" placeholder="Add '+taxonRanks[i].text+'" /></div>').appendTo($hier);
                        }
                        if(data.rank > 0)
                        $('#taxonHierarchyInputForm').show();

                        $(".taxonRank").autofillNames();


                        $('#addSpeciesSubmit').show();
                    } else {
                         $('#errorMsg').removeClass('alert-info hide').addClass('alert-error').text(data.msg);
                    }
                }, error: function(xhr, status, error) {
                    handleError(xhr, status, error, this.success, function() {
                    var msg = $.parseJSON(xhr.responseText);
                    $(".alertMsg").html(msg.msg).removeClass('alert-success').addClass('alert-error');
                    }
                }
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
