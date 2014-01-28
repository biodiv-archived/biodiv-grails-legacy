<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<b>    <obv:showSpeciesName
    model="['observationInstance':observationInstance, 'userGroup':userGroup, 'userGroupWebaddress':userGroupWebaddress,isListView:true]" />
</b>
<!--div class="user-icon pull-right">
<a href="${uGroup.createLink(controller:'SUser', action:'show', id:observationInstance.author.id, 'userGroupWebaddress':userGroup?userGroup.webaddress:userGroupWebaddress)}"> <img
    src="${observationInstance.author.profilePicture()}" class="small_profile_pic"
    title="${observationInstance.author.name}" /> </a>

</div-->

<obv:showFooter model="['observationInstance':observationInstance, 'showDetails':false, 'showLike':false, 'hidePost':true]"/>

