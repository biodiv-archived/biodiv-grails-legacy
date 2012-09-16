<div id="wgp-header" class="header" style="display:none;">
    <!-- Logo -->
      <div class="span3">
        <a href="/">
          <img class="logo" alt="western ghats" src="/sites/all/themes/wg/images/map-logo.gif">
        </a>
      </div>
    <!-- Logo ends -->

    <div class="span7 top_nav_bar">
    <ul>
    <li onclick="location.href='${createLink("controller":"activityFeed")}'" title="Activity" id="activity_nav_link">Activity</li>
    <li onclick="location.href='${createLink("controller":"species")}'" title="Species" id="species_nav_link">Species</li>
    <li onclick="location.href='${createLink("controller":"observation")}'" title="Observations" id="species_nav_link">Observations</li>
    <li onclick="location.href='${createLink("controller":"userGroup")}'" title="Groups" id="species_nav_link">Groups</li>
    <li onclick="location.href='/map'" title="Maps" id="maps_nav_link">Maps</li>
    <li onclick="location.href='/browsechecklists'" title="Checklists" id="checklists_nav_link">Checklists</li>
    <!--li onclick="location.href='/collaborate-wg'" title="Collaborate" id="collaborate_nav_link">Collaborate</li-->
    <li onclick="location.href='/themepages/list'" title="Themes" id="themes_nav_link">Themes</li>
    <li onclick="location.href='/about/western-ghats'" title="About" id="about_nav_link">About</li>
    </ul>
    </div>
		<div class="header_userInfo">
        	<sUser:userLoginBox/>
        </div>

</div>
