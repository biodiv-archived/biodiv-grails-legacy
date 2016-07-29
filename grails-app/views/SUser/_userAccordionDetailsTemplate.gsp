<style>
.icon-chevron-down{float:right;}
h5{background-clolor:white;}
.table td{padding:2px;}
.accordion-inner-obv {
    border-top: 1px solid #e5e5e5;
    padding: 25px 15px;
}
.accordion-inner {
    border-top: 1px solid #e5e5e5;
    padding: 50px 15px;
}
.sidebar_section{margin-bottom:0px;}
.sidebar_section h5 {
    background: rgba(106, 201, 162, 0.2) none repeat scroll 0 0;
    cursor: pointer;
    display: block;
    font-size: 16px;
    font-weight: bold;
    line-height: 20px;
    margin: 0;
    padding: 5px;
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
}
#totalscore {
    align-self: center;
    font-weight: bold;
    padding: 5px;
}
.content_score,.curation_score{
    padding:0px 65px 0px 30px;
    font-size: 12px;
    
}
.badge{position:relative;background-color:#3a87ad;padding-left:9px;padding-right:9px;}
.activity_score{padding:2px;}
.badge{background-color:#1a941d;}
</style>
<div id=activityscore></div>
<hr style="margin: 5px 0;">
 <div id=totalcontributedscore></div>
  <div id=totalactivityscore></div>
<div class="accordion" id="accordion2">
<div class="accordion-group">
  <div class="accordion-heading">
    
    <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion2" href="#collapseObservation">
    <span class="acc_label">Observations</span>
    <span class="icon-chevron-down"></span>
    </a>
  </div>
  <div id="collapseObservation" class="accordion-body collapse in">
    <div class="accordion-inner-obv">
        <table class="table table-bordered" style="font-size:12px;">
        <thead>
        <th>Category</th>
        <th>Count</th>
        
        </thead>
        <tbody>
            <tr><td>Uploaded</td><obv:showNoOfObservationsCreated model="['user':user, 'userGroup':userGroupInstance]" /></tr>
            <tr><td>Identified</td><obv:showNoOfRecommendationsSuggested model="['user':user, 'userGroup':userGroupInstance]" /></tr>
            <!-- <tr><td>ID agreed upon</td><obv:showNoOfAgreedUponOfUser model="['user':user, 'activityHolderType':"species.participation.RecommendationVote", 'activityType':"Agreed on species name"]" /></tr> -->
            <tr><td>Organized</td><obv:showNoOfOrganizedUponOfUser model="['user':user,'objectType':"species.participation.Observation"]" /></tr>
            <tr><td>Downloads</td><obv:showNoOfDownloadUponOfUser model="['user':user,'sourceType':"Observations"]" /></tr>
            <tr><td>Comments</td><obv:showNoOfCommentUponOfUser model="['user':user,'rootHolderType':"species.participation.Observation"]" /></tr>
            </tbody>
            </table>
        </ul>     
    </div>
  </div>
</div>
<div class="accordion-group">
  <div class="accordion-heading">  
    <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion2" href="#collapseSpecies">
    <span class="acc_label">Species</span>
    <span class="icon-chevron-down"></span>
    </a>
  </div>
  <div id="collapseSpecies" class="accordion-body collapse" style="height: 0px;">
    <div class="accordion-inner">
          <table class="table table-bordered" style="font-size:12px;">
        <thead>
        <th>Category</th>
        <th>Count</th>
       
        </thead>
            <tr><td>Contributed</td><s:noOfContributedSpecies model="['user':user, 'permissionType':"ROLE_CONTRIBUTOR"]" /></tr>
            <tr><td>Organized</td><s:showNoOfOrganizedSpecies model="['user':user,rootHolderType:'species.Species']"/></tr>
            <tr><td>Comments</td><obv:showNoOfCommentUponOfUser model="['user':user,'rootHolderType':"species.Species"]" /></tr>
        </table>
    </div>
  </div>
</div>
<div class="accordion-group">
  <div class="accordion-heading">
    <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion2" href="#collapseDocument">
      <span class="acc_label">Documents</span>
     <span class="icon-chevron-down"></span>
    </a>
  </div>
  <div id="collapseDocument" class="accordion-body collapse" style="height: 0px;">
    <div class="accordion-inner">
       <table class="table table-bordered" style="font-size:12px;">
        <thead>
        <th>Category</th>
        <th>Count</th>
        
        </thead>
            <tr><td>Uploaded</td><obv:showNoOfDocsUploaded model="['user':user,'rootHolderType':"content.eml.Document",'activityType':"Document created"]" /></tr>
            <tr><td>Organized</td><obv:showNoofOrganizedDocs model="['user':user,'rootHolderType':"content.eml.Document"]" /></tr>
            <tr><td>Comments</td><obv:showNoOfCommentUponOfUser model="['user':user,'rootHolderType':"content.eml.Document"]" /></tr>
        </table>
    </div>
  </div>
</div>
<div class="accordion-group">
  <div class="accordion-heading">
    <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion2" href="#collapseDiscussion">
      <span class="acc_label">Discussions</span>
     <span class="icon-chevron-down"></span>
    </a>
  </div>
  <div id="collapseDiscussion" class="accordion-body collapse" style="height: 0px;">
    <div class="accordion-inner">
           <table class="table table-bordered" style="font-size:12px;">
        <thead>
        <th>Category</th>
        <th>Count</th>
        </thead>
            <tr><td>Created</td><obv:showNoOfDiscussionCreated model="['user':user,'activityHolderType':"species.participation.Discussion",'activityType':"Discussion created"]" /></tr>
            <tr><td>Participated</td><obv:showNoofParticipationDiscussion model="['user':user,'rootHolderType':"species.participation.Discussion"]" /></tr>
            <tr><td>Organized</td><obv:showNoOfCommentUponOfUser model="['user':user,'activityHolderType':"species.participation.Discussion"]" /></tr>
        </table>  
    </div>
  </div>
</div>
<div class="accordion-group">
  <div class="accordion-heading">    
    <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion2" href="#collapseMap">
      <span class="acc_label">Maps</span>
     
    </a>
  </div>
  <div id="collapseMap" class="accordion-body collapse" style="height: 0px;">
    <div class="accordion-inner">        
    </div>
  </div>
</div>
<div class="accordion-group">
  <div class="accordion-heading">    
    <a class="accordion-toggle collapsed" data-toggle="collapse" data-parent="#accordion2" href="#collapseGroup">
      <span class="acc_label">Groups</span>
     <span class="icon-chevron-down"></span>
    </a>
  </div>
  <div id="collapseGroup" class="accordion-body collapse" style="height: 0px;">
    <div class="accordion-inner">
 <table class="table table-bordered" style="font-size:12px;">
        <thead>
        <th>Category</th>
        <th>Count</th>
        
        </thead>
            <tr><td>Founded</td><td class="countvaluecontributed"><uGroup:showNoOfFoundedUserGroups model="['userInstance':user]"></uGroup:showNoOfFoundedUserGroups></td></tr>
            <tr><td>Moderating</td><td class="countvalue"><uGroup:showNoOfExpertUserGroups model="['userInstance':user]"></uGroup:showNoOfExpertUserGroups></td></tr>
            <tr><td>Member of</td><td class="countvalue"><uGroup:showNoOfMemberUserGroups model="['userInstance':user]"></uGroup:showNoOfMemberUserGroups></td></tr>
        </table>
    </div>
  </div>
</div>
</div>
        <asset:script>
        $(document).ready(function() {
          var countarray=new Array();
           var all = $(".countvalue").map(function() {
            //sum += Number($(this.innerHTml));
                //return this.innerHTML;
                countarray.push(this.innerHTML)
            }).get();
        //alert (countarray.length);
        var total = 0;
        for (var i = 0; i < countarray.length; i++) {
          total += countarray[i] << 0;
        }
        var countcontributedarray=new Array()
          var allcontributed = $(".countvaluecontributed").map(function() {
                countcontributedarray.push(this.innerHTML)
            }).get();
            var totalcontributed = 0;
          for (var i = 0; i < countcontributedarray.length; i++) {
          totalcontributed += countcontributedarray[i] << 0;
          }
        $("#totalcontributedscore").html('<div class="content_score"><span class="icon-pencil"/><strong>Content Score</strong><span class="pull-right"><strong>'+Math.floor(Math.log10(totalcontributed)*10)+'</strong></span></div>');
        $("#totalactivityscore").html('<div class="curation_score"><span class="icon-tasks" /><strong>Curation Score</strong><span class="pull-right"><strong>'+Math.floor(Math.log10(total)*10)+'</strong></span></div>');
        $("#activityscore").html('<div class="activity_score"><strong>Activity Score</strong><span class="pull-right badge badge-success">'+(Math.floor(Math.log10(totalcontributed)*10)+Math.floor(Math.log10(total)*10))+'</span></div>');
        });

        $('.accordion-toggle').on('click',function(e){
    if($(this).parents('.accordion-group').children('.accordion-body').hasClass('in')){
        e.stopPropagation();
    }
    // You can also add preventDefault to remove the anchor behavior that makes
    // the page jump
     e.preventDefault();
    });
        </asset:script>