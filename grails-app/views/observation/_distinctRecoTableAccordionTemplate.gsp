<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.ScientificName.TaxonomyRank"%>
<style>
#habitatFilter{display:none;}
#observationMediaFilter{display:none;}
#speciesNameFilter{display:none;}
#observationFlagFilter{display:none;}
.panel {
  padding: 15px;
  margin-bottom: 20px;
  background-color: #CFEDE1;
  border: 1px solid #dddddd;
  border-radius: 4px;
  -webkit-box-shadow: 0 1px 1px rgba(0, 0, 0, 0.05);
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.05);
}
</style>
<div class="panel">
<h5>Life List</h5>
<table class="table table-bordered">
<tr><td colspan=2 ><div class="list"><obv:showGroupFilter model="['observationInstance':observationInstance, forObservations:true]" /></div></td></tr>
<tr>
<th>Observation Uploaded</th>
<th>Observation Identified</th>
</tr>
<tr>
<td width="50%">
<div class="pre-scrollable" style="overflow-y:auto;">
<g:render template="/observation/distinctRecoUserProfileTableTemplate" model="[distinctRecoList:distinctRecoList, totalCount:totalCount]"/>
</div>
</td>
<td width="50%">
<div class="pre-scrollable" style="overflow-y:auto;">
<g:render template="/observation/distinctRecoIdentifiedUserProfileTableTemplate" model="[distinctIdentifiedRecoList:distinctIdentifiedRecoList, totalCount:totalCount]"/>
</div>
</td>
</tr>
</table>
</div>
