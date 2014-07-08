<g:if test="${category.value.get('speciesFieldInstance')}">
<g:showSpeciesFieldToolbar model="model="['speciesFieldInstance':category.value.get('speciesFieldInstance')[0], 'isSpeciesContributor':isSpeciesContributor, 'isSpeciesFieldContributor':isSpeciesFieldContributor, 'isCurator':isCurator]" />
<%
def references = speciesInstance.fields.collect{it.references};
Map refs = new LinkedHashMap();
references.each(){
if(it) {
it.each() {
refs.put(it?.url?.trim()?:it?.title, it)
}
}
};

//printing only if references are not available.. using description
if(category.value.get('speciesFieldInstance')[0]?.description && !category.value.get('speciesFieldInstance')[0]?.references) {
category.value.get('speciesFieldInstance')[0]?.description?.replaceAll(/<.*?>/, '\n').split('\n').each() {
if(it) {
if(it.startsWith("http://")) {
refs.put(it, new Reference(url:it));
} else {
refs.put(it, new Reference(title:it));
}
}
}
}
references = refs.values();
%>
<g:if test="${references}">
<ol class="references" style="list-style:disc;list-style-type:decimal">
    <g:each in="${references}" var="r">

    <li class="linktext">
    <g:if test="${r.url}">

     ${r.title?r.title:r.url}
    </g:if> <g:else>
    ${r?.title}
    </g:else>
</a>
    </li>
    </g:each>

</ol>
</g:if>
</g:if>

