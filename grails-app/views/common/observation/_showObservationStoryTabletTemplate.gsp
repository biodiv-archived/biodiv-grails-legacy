<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<b  class="species_title_wrapper" style="${styleviewcheck?'display:none;': ''}">
    <obv:showSpeciesName model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress,isListView:true]" />
</b>
<obv:showFooter model="['observationInstance':observationInstance, 'showDetails':false, 'showLike':false, 'hidePost':true]"/>

