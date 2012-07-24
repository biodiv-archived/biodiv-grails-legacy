
<%@page import="species.utils.Utils"%>
<%@page import="species.utils.ImageType"%>
<div class="observation_story tablet">
        
        <h5>${userGroupInstance.name}</h5>
        ${userGroupInstance.getMembersCount()} members
       <uGroup:showFooter model="['userGroupInstance':userGroupInstance, 'showDetails':showDetails]"/>
</div>
