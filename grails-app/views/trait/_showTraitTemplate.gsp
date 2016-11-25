<style>
.dropdown-menu{position:relative;float:none;width:auto;}
</style>
<div class="trait" data-toggle="buttons-radio">
<%
	def traitValue
	def displayAny
		if(fromSpeciesShow){
			traitValue = factInstance[trait]
			displayAny = true
		}
		else{
			traitValue = trait.values()
			displayAny = false
		}
 %>

 <g:if test="${observationCreate}">
	<a class="dropdown-toggle btn" 
		data-toggle="dropdown">${trait.name} <b class="caret"></b>
	</a>
	<ul class="dropdown-menu" style="width:auto;overflow-x:hidden;overflow-y:auto;">
	<li style="text-align:center;overflow-x:hidden; overflow-y:auto;">
	    <g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':traitValue,'displayAny':displayAny, 'observationCreate':true, 'displayAny':displayAny]"/>
	</li>
	</ul>	
  </g:if>
  <g:else>
	<%= observationCreate %>  
	<a href="${uGroup.createLink(action:'show', controller:'trait', id:trait.id)}"><h6>${trait.name}</h6></a>
  <g:render template="/trait/showTraitValuesListTemplate" model="['traitValues':traitValue,'displayAny':displayAny, 'displayAny':displayAny]"/>
  </g:else>	
</div>
