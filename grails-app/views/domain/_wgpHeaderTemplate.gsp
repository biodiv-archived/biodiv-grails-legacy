<style>
#wgp-header {
background-color: #f9f9f9;
box-shadow: 0 6px 6px -6px #5E5E5E;
color: #5E5E5E;
font-family: Verdana,Helvetica,Sans-Serif;
height: 80px;
width: 100%;
z-index: 2000;
}
</style>

<div id="wgp-header" style="display:none;">
    <!-- Logo -->
      <div id="logo" class="span3">
        <a href="/">
          <img id="wg_logo" alt="western ghats" src="/sites/all/themes/wg/images/map-logo.gif">
        </a>
      </div>
    <!-- Logo ends -->

    <div id="top_nav_bar">
    <ul>
    <li onclick="location.href='/biodiv/species/list'" title="Species" id="species_nav_link">Species</li>
    <li onclick="location.href='/biodiv/observation/list'" title="Observations" id="species_nav_link">Observations</li>
    <li onclick="location.href='/map'" title="Maps" id="maps_nav_link">Maps</li>
    <li onclick="location.href='/browsechecklists'" title="Checklists" id="checklists_nav_link">Checklists</li>
    <li onclick="location.href='/collaborate-wg'" title="Collaborate" id="collaborate_nav_link">Collaborate</li>
    <li onclick="location.href='/themepages/list'" title="Themes" id="themes_nav_link">Themes</li>
    <li onclick="location.href='/about/western-ghats'" title="About" id="about_nav_link">About</li>
    </ul>
    </div>
        <div>
                <span id='loginLink'
                        style='position: relative; margin-right: 30px; float: right'>
                        <sec:ifLoggedIn>
                                <sUser:renderProfileLink /> (<a id="logout"
                                        href="${createLink(controller:'logout')}">Logout</a>)
        </sec:ifLoggedIn> <sec:ifNotLoggedIn>
                                <g:link controller='login'>Login</g:link> | <g:link
                                        controller='register'>Register</g:link>
                        </sec:ifNotLoggedIn> </span>
                <!-- g:render template='/common/ajaxLogin' /-->
                <div id="fb-root"></div>
                <br />
        </div>
</div>

