
<style>
#ibp-header {
border-bottom: 1px solid #E5E5E5;
box-shadow: 0 6px 6px -6px #5E5E5E;
color: #5E5E5E;
font-family: Verdana,Helvetica,Sans-Serif;
height: 80px;
width: 100%;
z-index: 2000;
}
.gradient-bg {
   background-color: #ECE9B7;
   background-image: -webkit-gradient(linear, 0% 0%, 0% 100%, from(#ECE9B7), to(#ebfefd));
   background-image: -webkit-linear-gradient(left, #ECE9B7, #ebfefd);
   background-image:    -moz-linear-gradient(left, #ECE9B7, #ebfefd);
   background-image:     -ms-linear-gradient(left, #ECE9B7, #ebfefd);
   background-image:      -o-linear-gradient(left, #ECE9B7, #ebfefd);
}
#userMenu {
font-size: 8pt;
font-weight: bold;
position: absolute;
margin: 5px 7px 5px 5px;      
line-height: 14px;        
top: 0;
right: 0;     
}
#mainMenu {
    margin-left: 10px;
    margin-top: 0;
    clear: both;
    font-size: 12pt;
    margin-left: 10px;
    font-weight: bold;
    position:absolute;
    top: 0px;        
    left: 200px;     
    padding: 5px;     
}
#mainMenu ul.links li a:link, #mainMenu ul li a:visited, #mainMenu ul li a:hover, #mainMenu ul li a:active {
    background-color: #726033;
    border-radius: 5px 5px 5px 5px;
    color: #ECE9B7;
    display: block;
    min-width: 100px;
    padding: 5px;
    text-align: center;
    text-decoration: none;
    line-height: 21px;         
}
#mainMenu ul.links li {
    float: left;
    list-style: none outside none;
    margin: 0 5px;
    padding: 0;
}
</style>

<div id="ibp-header" class="gradient-bg" style="display:none;">
    <!-- Logo -->
      <div id="logo">
        <a href="/">
          <img id="ibp_logo" alt="western ghats" src="/sites/all/themes/ibp/images/map-logo.gif">
        </a>
      </div>
    <!-- Logo ends -->

    <div id="mainMenu">
        <ul class="links primary-links">
            <li class="menu-449 first"><a href="/" title="">Home</a></li>
            <li class="menu-450"><a href="/biodiv/species/list" title="">Species</a></li>
            <li class="menu-450"><a href="/biodiv/observation/list" title="">Observations</a></li>
            <li class="menu-450"><a href="/maps" title="">Maps</a></li>
            <li class="menu-451"><a href="/checklists" title="">Checklists</a></li>
            <li class="menu-452"><a href="/calendar" title="">Events</a></li>
            <li class="menu-453 last"><a href="/biodiversity_news" title="">News</a></li>
        </ul>                                     
    </div>
        <sUser:userLoginBox/>
</div>

