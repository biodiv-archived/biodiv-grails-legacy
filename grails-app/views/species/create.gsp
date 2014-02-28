<%@ page import="species.Species"%>
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
                    <label class="control-label span3" for="name">Species</label> 
                    <div class="span8">
                        <input id="species" 
                        data-provide="typeahead" type="text" class="input-block-level"
                        name="species" value="${species}"
                        placeholder="Add species name" />
                    </div>
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
                    <a id="addSpeciesSubmit" class="btn btn-primary"
                        style="float: right; margin-right: 5px;"> Add Species </a>

                </div>

            </form>
        </div>

    </body>
    <r:script>
    $(document).ready(function() {
        $('#addSpeciesSubmit').click(function() {
            $('#addSpecies').submit();        
        });
    });
    </r:script>
</html>
