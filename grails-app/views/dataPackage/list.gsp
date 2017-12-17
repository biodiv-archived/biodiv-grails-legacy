<%@ page import="species.dataset.DataPackage"%>

<%@page import="species.utils.Utils"%>
<html>
<head>
<g:set var="title" value="${g.message(code:'button.dataPackages')} "/>
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
    def dataPackage=g.message(code:'button.dataPackages')
    %>
		<uGroup:showSubmenuTemplate   model="['entityName':dataPackage]"/>
		
		
		<div class="">
            <g:render template="/dataPackage/showDataPackageListWrapperTemplate"/>
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
