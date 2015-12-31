<%@ page import="species.dataset.Datasource"%>

<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'datasource.value.user')} "/>
<g:render template="/common/titleTemplate" model="['title':title]"/>
</head>
<body>
<style type="text/css">
.observations_list{
	overflow-y:scroll;
	height:600px;
}
</style>
	<div class="span12">
    <%
    def datasource=g.message(code:'button.datasources')
    %>
		<uGroup:showSubmenuTemplate   model="['entityName':datasource]"/>
		
		
		<div class="">
            <g:render template="/datasource/showDatasourceListWrapperTemplate"/>
		</div>
	</div>
	<asset:script>
$('.observations_list').bind('scroll', function() {
        if($(this).scrollTop() + $(this).innerHeight() >= this.scrollHeight) {
        	if($(".loadMore").is(":visible")){
	            $(".loadMore").trigger('click');
	            console.log("trigger");
        	}
        }
});

	</asset:script>

</body>
</html>
