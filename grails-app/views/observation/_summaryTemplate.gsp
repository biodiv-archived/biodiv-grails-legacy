<%@ page import="species.ScientificName.TaxonomyRank"%>
<%@ page import="species.NamesMetadata.NameStatus"%>
<div id="summary_section" class="sidebar_section">
    <h5><g:message code="taxonomic.summary" /></h5>
    <table class="table table-bordered table-condensed table-striped">
        <tr>
            <td>${TaxonomyRank.SPECIES.value()}</td>
            <td>
                <a class="taxonRank" href="${uGroup.createLink(
				    controller:params.controller, action:params.action?:'list',
					params:[taxonRank:TaxonomyRank.SPECIES.ordinal()])}" data-value="${TaxonomyRank.SPECIES.ordinal()}">
                    ${speciesCount?:0}</a>
            </td>
        </tr>
        <tr>
            <td>${TaxonomyRank.INFRA_SPECIFIC_TAXA.value()}</td>
            <td>
                <a class="taxonRank" href="${uGroup.createLink(
				    controller:params.controller, action:params.action?:'list',
					params:[taxonRank:TaxonomyRank.INFRA_SPECIFIC_TAXA.ordinal()])}" data-value="${TaxonomyRank.INFRA_SPECIFIC_TAXA.ordinal()}">
                    ${subSpeciesCount?:0}
                </a>
            </td>
        </tr>
        <tr>
            <td>${NameStatus.ACCEPTED.label()}</td>
            <td>
                <a class="status" href="${uGroup.createLink(
				    controller:params.controller, action:params.action?:'list',
					params:[status:NameStatus.ACCEPTED])}" data-value="${NameStatus.ACCEPTED}">
                    ${acceptedSpeciesCount?:0}
                </a>
            </td>
        </tr>
        <tr>
            <td>${NameStatus.SYNONYM.label()}</td>
            <td>
                <a class="status" href="${uGroup.createLink(
				    controller:params.controller, action:params.action?:'list',
                    params:[status:NameStatus.SYNONYM])}" data-value="${NameStatus.SYNONYM}">
                    ${synonymSpeciesCount?:0}
                </a>
            </td>
        </tr>
    </table>
    <input id="taxonRank" name="taxonRank" type="hidden" value="${(queryParam?.taxonRank)?:''}"></input>
    <input id="status" name="status" type="hidden" value="${(queryParam?.status)?:''}"></input>
</div>

<r:script>
$(document).ready(function() {
    $('#summary_section taxonRank').click(function(e) {
    $('#taxonRank').val($(e.target).data('value')); 
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, undefined, undefined, undefined, undefined, undefined);
        return false;
    });
    $('#summary_section status').click(function() {
    $('#status').val($(e.target).data('value')); 
        updateGallery(undefined, window.params.queryParamsMax, window.params.offset, undefined, undefined, undefined, undefined, undefined, undefined);
        return false;
    });
});

</r:script>
