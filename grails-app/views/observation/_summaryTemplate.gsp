<%@ page import="species.ScientificName.TaxonomyRank"%>
<%@ page import="species.NamesMetadata.NameStatus"%>
<div class="sidebar_section">
    <h5><g:message code="project.show.Summary" /></h5>
    <table class="table table-bordered table-condensed table-striped">
        <tr>
            <td>${TaxonomyRank.SPECIES.value()}</td>
            <td>${speciesCount?:0}</td>
        </tr>
        <tr>
            <td>${TaxonomyRank.INFRA_SPECIFIC_TAXA.value()}</td>
            <td>${subSpeciesCount?:0}</td>
        </tr>
        <tr>
            <td>${NameStatus.ACCEPTED}</td>
            <td>${acceptedSpeciesCount?:0}</td>
        </tr>
        <tr>
            <td>${NameStatus.SYNONYM}</td>
            <td>${synonymSpeciesCount?:0}</td>
        </tr>
    </table>                                                      
</div>


