<div id="ibp-header" class="header gradient-bg" style="display:none;">
    <!-- Logo -->
      <div class="span3">
        <a href="/">
          <img class="logo" alt="western ghats" src="/sites/all/themes/ibp/images/map-logo.gif">
        </a>
      </div>
    <!-- Logo ends -->

    <div id="mainMenu" class="span10">
        <ul class="links primary-links">
            <!--li class="menu-449 first"><a href="/" title="">Home</a></li-->
            <li class="menu-450"><a href="${createLink("controller":"species")}" title="Species">Species</a></li>
            <li class="menu-450"><a href="${createLink("controller":"observation")}" title="Observations">Observations</a></li>
            <li class="menu-450"><a href="${createLink("controller":"userGroup")}" title="Groups">Groups</a></li>
            <li class="menu-450"><a href="/maps" title="Maps">Maps</a></li>
            <li class="menu-451"><a href="/checklists" title="Checklists">Checklists</a></li>
            <li class="menu-452"><a href="/calendar" title="Events">Events</a></li>
            <li class="menu-453 last"><a href="/biodiversity_news" title="News">News</a></li>
        </ul>                                     
    </div>
    <div class="header_userInfo span2" style="float:right;">
        <sUser:userLoginBox/>
    </div>
</div>

