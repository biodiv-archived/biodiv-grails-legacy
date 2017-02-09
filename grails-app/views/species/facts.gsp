<%@page import="species.utils.Utils"%>
<html>
    <head>
        <g:set var="title" value="${g.message(code:'facts.label')}"/>
        <g:render template="/common/titleTemplate" model="['title':title]"/>
    </head>
    <body>
        <div class="observation_create">
            <div class="span12">
                <uGroup:showSubmenuTemplate  model="['entityName':entityName]"/>
                
                <obv:showObservationFilterMessage
	                model="['instanceTotal':factsList.count, 'queryParams':[:], resultType:'fact']" />
                <div style="margin-bottom:10px;">
                    <form id="traitSearch" class="form-inline">
                        <input type="hidden" name="id" value="${params.id}">
                        <select id="traitName" name="trait" type="text" class="input-xlarge" placeholder="Trait">
                            <g:each in="${traitsList}" var="trait">
                            <option value="${trait}" ${(params.trait?.equalsIgnoreCase(trait))?"selected='selected'":''}>${trait}</option>
                            </g:each>
                        </select>
                        <input type="text" class="input-xlarge" name="traitValue" placeholder="Value" value="${params.traitValue?:''}">
                        <button id="traitSearchSubmit" type="submit" class="btn">Search</button>
                    </form>
                </div>
            </div>
                <g:render template="/species/factsTable" model="['factsList':factsList.factsList]"/>
           </div>
        </div>
        <asset:script>
        $(document).ready(function() {
            $('#traitSearchSubmit').ajaxSubmit({
                url:'${g.createLink(controller:'species', action:'listFacts')}',
                dataType : 'json',
                type: 'GET',
                success : function(response, statusText, xhr, form) {
                    console.log(response);
                }, 
                error : function (xhr, ajaxOptions, thrownError){
                    alert(xhr)
                }
            
            })

            $('#traitName').typeahead({
                source:${traitsList}
            });

        });
        </asset:script>
    </body>
</html>

