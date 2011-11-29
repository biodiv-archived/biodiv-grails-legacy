

<%@ page import="species.participation.Recommendation"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'recommendation.label', default: 'Recommendation')}" />
<title><g:message code="default.create.label"
		args="[entityName]" />
</title>
<style>
	.ui-autocomplete-category {
		font-weight: bold;
		padding: .2em .4em;
		margin: .8em 0 .2em;
		line-height: 1.5;
	}
	</style>

<g:javascript>

$(document).ready(function() {

	$.widget( "custom.catcomplete", $.ui.autocomplete, {
		_renderMenu: function( ul, items ) {
			var self = this,
				currentCategory = "";
			$.each( items, function( index, item ) {
				if ( item.category != currentCategory ) {
					ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
					currentCategory = item.category;
				}
				self._renderItem( ul, item );
			});
		}
	});
	
	
	var cache = {},
		lastXhr;
	$("#name").catcomplete({
		 	source:function( request, response ) {
				var term = request.term;
				if ( term in cache ) {
					response( cache[ term ] );
					return;
				}

				lastXhr = $.getJSON( "${createLink(action: 'suggest')}", request, function( data, status, xhr ) {
					cache[ term ] = data;
					if ( xhr === lastXhr ) {
						response( data );
					}
				});
			},focus: function( event, ui ) {
				$( "#name" ).val( ui.item.label.replace(/<.*?>/g,"") );
				return false;
			},
			select: function( event, ui ) {
			console.log("select")
			console.log(ui)
				$( "#name" ).val( ui.item.label );
				$( "#name-id" ).val( ui.item.value );
				$( "#name-description" ).html( ui.item.desc ? ui.item.desc : "" );
				ui.item.icon ? $( "#name-icon" ).attr( "src",  ui.item.icon).show() : $( "#name-icon" ).hide();
				return false;
			}
	}).data( "catcomplete" )._renderItem = function( ul, item ) {
			if(item.category == "General") {
				return $( "<li></li>" )
					.data( "item.autocomplete", item )
					.append( "<a>" + item.label + "</a>" )
					.appendTo( ul );
			} else {
				if(!item.icon) {
					item.icon =  "${createLinkTo(dir: 'images/', file:"no-image.jpg", base:grailsApplication.config.speciesPortal.images.serverURL)}"
				}  
				return $( "<li></li>" )
					.data( "item.autocomplete", item )
					.append( "<img src='" + item.icon+"' class='ui-state-default icon' style='float:left' /><a>" + item.label + "<br>(" + item.desc + ")</a>" )
					.appendTo( ul );
			}
		};
	
});

	
</g:javascript>
</head>
<body>

	<div class="container_16">
		<h1>
			<g:message code="default.create.label" args="[entityName]" />
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${recommendationInstance}">
			<div class="errors">
				<g:renderErrors bean="${recommendationInstance}" as="list" />
			</div>
		</g:hasErrors>
		<g:form action="save">
			<div class="dialog">
				<table>
					<tbody>

						<tr class="prop">
							<td valign="top" class="name"><label for="name"><g:message
										code="recommendation.name.label"
										default="Suggested Name" /> </label></td>
							<td valign="top"
								class="value ${hasErrors(bean: recommendationInstance, field: 'name', 'errors')}">
								<div>
									<input type="text" name="query" name="name"
										id="name"
										value="${recommendationInstance?.name}" size="40"
										class="text ui-widget-content ui-corner-all" /> <input
										type="hidden" id="name-id" />
									<div>
											<img id="name-icon" src="" class="ui-state-default figure" style="float:left" />										
											<p id="name-description"></p>
									</div>
								</div>
							</td>
						</tr>

						

					</tbody>
				</table>
			</div>
			<div class="buttons">
				<span class="button"><g:submitButton name="create"
						class="save" acction="save"
						value="${message(code: 'default.button.create.label', default: 'Create')}" />
				</span>
			</div>
		</g:form>
	</div>
</body>
</html>
