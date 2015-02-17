<%@ page contentType="text/html"%>
<html><head><title>Bienvenue</title>

<meta content="text/html; charset=utf-8" http-equiv="Content-Type">
<style type="text/css">
/* Mobile-specific Styles */
@media only screen and (max-device-width: 480px) { table[class=w0], td[class=w0] { width: 0 !important; }
table[class=w10], td[class=w10], img[class=w10] { width:10px !important; }
table[class=w15], td[class=w15], img[class=w15] { width:5px !important; }
table[class=w30], td[class=w30], img[class=w30] { width:10px !important; }
table[class=w60], td[class=w60], img[class=w60] { width:10px !important; }
table[class=w125], td[class=w125], img[class=w125] { width:80px !important; }
table[class=w130], td[class=w130], img[class=w130] { width:55px !important; }
table[class=w140], td[class=w140], img[class=w140] { width:90px !important; }
table[class=w160], td[class=w160], img[class=w160] { width:180px !important; }
table[class=w170], td[class=w170], img[class=w170] { width:100px !important; }
table[class=w180], td[class=w180], img[class=w180] { width:80px !important; }
table[class=w195], td[class=w195], img[class=w195] { width:80px !important; }
table[class=w220], td[class=w220], img[class=w220] { width:80px !important; }
table[class=w240], td[class=w240], img[class=w240] { width:180px !important; }
table[class=w255], td[class=w255], img[class=w255] { width:185px !important; }
table[class=w275], td[class=w275], img[class=w275] { width:135px !important; }
table[class=w280], td[class=w280], img[class=w280] { width:135px !important; }
table[class=w300], td[class=w300], img[class=w300] { width:140px !important; }
table[class=w325], td[class=w325], img[class=w325] { width:95px !important; }
table[class=w360], td[class=w360], img[class=w360] { width:140px !important; }
table[class=w410], td[class=w410], img[class=w410] { width:180px !important; }
table[class=w470], td[class=w470], img[class=w470] { width:200px !important; }
table[class=w580], td[class=w580], img[class=w580] { width:280px !important; }
table[class=w640], td[class=w640], img[class=w640] { width:300px !important; }
table[class*=hide], td[class*=hide], img[class*=hide], p[class*=hide], span[class*=hide] { display:none !important; }
table[class=h0], td[class=h0] { height: 0 !important; }
p[class=footer-content-left] { text-align: center !important; }
#headline p { font-size: 30px !important; }
.article-content, #left-sidebar{ -webkit-text-size-adjust: 90% !important; -ms-text-size-adjust: 90% !important; }
.header-content, .footer-content-left {-webkit-text-size-adjust: 80% !important; -ms-text-size-adjust: 80% !important;}
img { height: auto; line-height: 100%;}
} /* Client-specific Styles */
#outlook a { padding: 0; } /* Force Outlook to provide a "view in browser" button. */
body { width: 100% !important; }
.ReadMsgBody { width: 100%; }
.ExternalClass { width: 100%; display:block !important; } /* Force Hotmail to display emails at full width */
/* Reset Styles */
/* Add 100px so mobile switch bar doesn't cover street address. */
body { background-color: #ececec; margin: 0; padding: 0; }
img { outline: none; text-decoration: none; display: block;}
br, strong br, b br, em br, i br { line-height:100%; }
h1, h2, h3, h4, h5, h6 { line-height: 100% !important; -webkit-font-smoothing: antialiased; }
h1 a, h2 a, h3 a, h4 a, h5 a, h6 a { color: blue !important; }
h1 a:active, h2 a:active, h3 a:active, h4 a:active, h5 a:active, h6 a:active { color: red !important; }
/* Preferably not the same color as the normal header link color. There is limited support for psuedo classes in email clients, this was added just for good measure. */
h1 a:visited, h2 a:visited, h3 a:visited, h4 a:visited, h5 a:visited, h6 a:visited { color: purple !important; }
/* Preferably not the same color as the normal header link color. There is limited support for psuedo classes in email clients, this was added just for good measure. */ table td, table tr { border-collapse: collapse; }
.yshortcuts, .yshortcuts a, .yshortcuts a:link,.yshortcuts a:visited, .yshortcuts a:hover, .yshortcuts a span {
color: black; text-decoration: none !important; border-bottom: none !important; background: none !important;
} /* Body text color for the New Yahoo. This example sets the font of Yahoo's Shortcuts to black. */
/* This most probably won't work in all email clients. Don't include <code _tmplitem="68" > blocks in email. */
code {
white-space: normal;
word-break: break-all;
}
#background-table { background-color: #ececec; }
/* Webkit Elements */
#top-bar { border-radius:6px 6px 0px 0px; -moz-border-radius: 6px 6px 0px 0px; -webkit-border-radius:6px 6px 0px 0px; -webkit-font-smoothing: antialiased; background-color: #851608; color: #ede899; }
#top-bar a { font-weight: bold; color: #ede899; text-decoration: none;}
#footer { border-radius:0px 0px 6px 6px; -moz-border-radius: 0px 0px 6px 6px; -webkit-border-radius:0px 0px 6px 6px; -webkit-font-smoothing: antialiased; }
/* Fonts and Content */
body, td { font-family: 'Helvetica Neue', Arial, Helvetica, Geneva, sans-serif; }
.header-content, .footer-content-left, .footer-content-right { -webkit-text-size-adjust: none; -ms-text-size-adjust: none; }
/* Prevent Webkit and Windows Mobile platforms from changing default font sizes on header and footer. */
.header-content { font-size: 12px; color: #ede899; }
.header-content a { font-weight: bold; color: #ede899; text-decoration: none; }
#headline p { color: #ede899; font-family: 'Helvetica Neue', Arial, Helvetica, Geneva, sans-serif; font-size: 36px; text-align: center; margin-top:0px; margin-bottom:30px; }
#headline p a { color: #ede899; text-decoration: none; }
.article-title { font-size: 18px; line-height:24px; color: #e97900; font-weight:bold; margin-top:0px; margin-bottom:18px; font-family: 'Helvetica Neue', Arial, Helvetica, Geneva, sans-serif; }
.article-title a { color: #e97900; text-decoration: none; }
.article-title.with-meta {margin-bottom: 0;}
.article-meta { font-size: 13px; line-height: 20px; color: #ccc; font-weight: bold; margin-top: 0;}
.article-content { font-size: 13px; line-height: 18px; color: #444444; margin-top: 0px; margin-bottom: 18px; font-family: 'Helvetica Neue', Arial, Helvetica, Geneva, sans-serif; }
.article-content a { color: #009fe9; font-weight:bold; text-decoration:none; }
.article-content img { max-width: 100% }
.article-content ol, .article-content ul { margin-top:0px; margin-bottom:18px; margin-left:19px; padding:0; }
.article-content li { font-size: 13px; line-height: 18px; color: #444444; }
.article-content li a { color: #009fe9; text-decoration:underline; }
.article-content p {margin-bottom: 15px;}
.footer-content-left { font-size: 12px; line-height: 15px; color: #f0e4f0; margin-top: 0px; margin-bottom: 15px; }
.footer-content-left a { color: #ede899; font-weight: bold; text-decoration: none; }
.footer-content-right { font-size: 11px; line-height: 16px; color: #f0e4f0; margin-top: 0px; margin-bottom: 15px; }
.footer-content-right a { color: #ede899; font-weight: bold; text-decoration: none; }
#footer { background-color: #851608; color: #f0e4f0; }
#footer a { color: #ede899; text-decoration: none; font-weight: bold; }
#permission-reminder { white-space: normal; }
#street-address { color: #ede899; white-space: normal; }
</style><!--[if gte mso 9]> <style _tmplitem="68" > .article-content ol, .article-content ul { margin: 0 0 0 24px; padding: 0; list-style-position: inside; } </style> <![endif]-->
</head><body>
<table class="w255" border="0" cellpadding="0" cellspacing="0" width="255">
<tbody>
<tr>
<td class="w255" height="8" width="255"></td>
</tr>
</tbody>
</table>
</td>
<td class="w15" width="15"></td>
</tr>
</tbody>
</table>
</td>
</tr>
<tr>
    <td id="header" class="w640" align="center" bgcolor="#ffffff" width="640"><a href="${grailsApplication.config.grails.serverURL}/"><img src="${resource(dir:'images', file:'whatsnewbanner_3.gif',absolute:'true' )}" alt="${grailsApplication.config.speciesPortal.app.siteName}" style="border: 0px solid ; width: 639px; height: 53px;"></a></td>
</tr>
<tr>
<td class="w580" style="width: 580px; height: 10px; background-color: white;"></td>
</tr>
<tr align="center">
    <td class="w640" bgcolor="#ffffff" height="30" width="640"><a href="${uGroup.createLink(controller:'observation', action:'list', absolute:true)}"><img src="${resource(dir:'images', file:'picturebanner.png',absolute:'true')}" alt="" style="border: 0px solid ; width: 600px; height: 101px;"></a></td>
</tr>
<tr id="simple-content-row">
<td class="w640" bgcolor="#ffffff" width="640">
<table class="w640" border="0" cellpadding="0" cellspacing="0" width="640">
<tbody>
<tr>
<td class="w30" width="30"></td>
<td class="w580" width="580"> <repeater>
<layout label="Text only"> </layout></repeater>
<table style="width: 580px; height: 151px;" class="w580" border="0" cellpadding="0" cellspacing="0">
<tbody>
<tr>
<td class="w580" width="580">
<p class="article-title" align="left"><singleline label="Title"><br>
</singleline></p>
<div class="article-content" align="left"><big style="font-weight: bold;"> <small>Cher</small> <small>&nbsp;${username}</small></big><span style="font-weight: bold;">,</span><br><br>
    <g:message code="welcomeEmail.intro" />
</div>
</td>
</tr>
<tr>
<td class="w580" height="10" width="580"></td>
</tr>
</tbody>
</table>
<layout label="Text with full-width image"></layout><layout label="Text with right-aligned image"></layout>
<table style="width: 580px; height: 404px;" class="w580" border="0" cellpadding="0" cellspacing="0">
<tbody>
<tr>
<td class="w580" style="width: 580px; vertical-align: bottom;">
    <p class="article-title" align="left"><big><a href="${uGroup.createLink(controller:'observation', action:'list', absolute:true)}"><span style="font-weight: bold;">Observations</span></a></big></p>
<table align="left" border="0" cellpadding="0" cellspacing="0">
<tbody>
<tr>
<td class="w30" width="15"><a href="${uGroup.createLink(controller:'observation', action:'list', absolute:true)}"><img src="${resource(dir:'images', file:'observations_cr.png',absolute:'true' )}" style="border: 0px solid ; width: 150px; height: 150px;" alt="Observations" hspace="5"></a></td>
</tr>
</tbody>
</table>
<div class="article-content" align="left">
    <g:message code="welcomeEmail.observation" />
</div>
</td>
</tr>
<tr>
<td class="w580" height="10" width="580"></td>
</tr>
<tr>
<td class="w580" style="width: 580px; vertical-align: bottom;">
    <p class="article-title" align="left"><a style="font-weight: bold;" href="${uGroup.createLink(controller:'map', absolute:true)}"><big>Cartes</big></a></p>
<table align="right" border="0" cellpadding="0" cellspacing="0">
<tbody>
<tr>
    <td class="w30" height="5" width="15"><a href="${uGroup.createLink(controller:'map', absolute:true)}"><img src="${resource(dir:'images', file:'maps_cr.png',absolute:'true')}" style="border: 0px solid ; width: 150px; height: 150px;" alt="Maps" hspace="5"></a></td>
</tr>
</tbody>
</table>
<div class="article-content" align="left"> 
    
    <g:message code="welcomeEmail.map" />
</div>
</td>
</tr>
<tr>
<td class="w580" style="width: 580px; height: 10px;"></td>
</tr>
</tbody>
</table>
<layout label="Text with left-aligned image">
</layout>
<table style="width: 580px; height: 801px;" class="w580" border="0" cellpadding="0" cellspacing="0">
<tbody>
<tr>
<td class="w580" width="580">
    <p class="article-title" align="left"><a href="${uGroup.createLink(controller:'document', action:'list', absolute:true)}"><span style="font-weight: bold;"><big>Documents</big></span></a></p>
<table align="left" border="0" cellpadding="0" cellspacing="0">
<tbody>
<tr>
    <td class="w30" width="15"><a href="${uGroup.createLink(controller:'document', action:'list', absolute:true)}"><img src="${resource(dir:'images', file:'documents_cr.png',absolute:'true')}" style="border: 0px solid ; width: 150px; height: 150px;" alt="Documents" hspace="5"></a></td>
</tr>
</tbody>
</table>
<div class="article-content" align="left"> 
    <g:message code="welcomeEmail.documents" />
</div>
</td>
</tr>
<tr>
<td class="w580" height="10" width="580"></td>
</tr>
<tr>
<td class="w580" width="580">
    <p class="article-title" align="left"><small><big><a style="font-weight: bold;" href="${uGroup.createLink(controller:'observation', action:'list', absolute:true)}"><big>Espèce</big></a></big></small></p>
<table align="right" border="0" cellpadding="0" cellspacing="0">
<tbody>
<tr>
    <td class="w30" height="5" width="15"><a href="${uGroup.createLink(controller:'species', action:'list', absolute:true)}"><img src="${resource(dir:'images', file:'species_cr.png',absolute:'true')}" style="border: 0px solid ; width: 150px; height: 150px;" alt="Species" hspace="5"></a></td>
</tr>
</tbody>
</table>
<div class="article-content" align="left"> 
    
    <g:message code="welcomeEmail.species" />
</div>
</td>
</tr>
<tr>
<td class="w580" height="10" width="580"></td>
</tr>
<tr>
<td class="w580" width="580">
    <p class="article-title" align="left"><small><big><a href="${uGroup.createLink(controller:'group', action:'list', absolute:true)}"><span style="font-weight: bold;"><big>groupes</big></span></a></big></small></p>
<table align="left" border="0" cellpadding="0" cellspacing="0">
<tbody>
<tr>
    <td class="w30" width="15"><a href="${uGroup.createLink(controller:'group', action:'list', absolute:true)}"><img src="${resource(dir:'images', file:'groups_cr.png',absolute:'true')}" style="border: 0px solid ; width: 150px; height: 150px;" alt="Groups" hspace="5"></a></td>
</tr>
</tbody>
</table>
<div class="article-content" align="left">
    <g:message code="welcomeEmail.groups" />
</div>
</td>
</tr>

<tr>
<td class="w580" width="580">
    <p class="article-title" align="left"><small><big><a style="font-weight: bold;" href="http://www.wikwio.org/idao"><big>IDAO</big></a></big></small></p>
<table align="right" border="0" cellpadding="0" cellspacing="0">
<tbody>
<tr>
    <td class="w30" height="5" width="15"><a href="http://www.wikwio.org/idao"><img src="${resource(dir:'images', file:'IDAO_cr.png',absolute:'true')}" style="border: 0px solid ; width: 150px; height: 150px;" alt="IDAO" hspace="5"></a></td>
</tr>
</tbody>
</table>
<div class="article-content" align="left"> 
    
    <g:message code="welcomeEmail.idao" />
</div>
</td>
</tr>


<tr>
<td class="w580" height="10" width="580"></td>
</tr>
<tr>
<td class="w580" width="580">
<div class="article-content" align="left">Le portail est dans un état d'évolution permanente, avec de nouvelles
fonctionnalités et des améliorations constamment ajoutées. Alors, suivez-nous sur <a href="${grailsApplication.config.speciesPortal.app.facebookUrl}">Facebook
</a>ou <a href="${grailsApplication.config.speciesPortal.app.twitterUrl}">Twitter
</a>pour rester à jour.Vous pouvez également laisser des  <a href="${grailsApplication.config.speciesPortal.app.feedbackFormUrl}">commentaires</a>, suggestions et demandes de fonctionnalités. <br><br>
Nous sommes ravis de vous avoir en tant que membre. Merci de mettre à jour votre  <a href="${userProfileUrl}">profil d'utilisateur.</a>.
Merci de vous rejoindre et nous espérons que vous apprécierez l'utilisation du portail. <br><br>
Merci d'ajouter <a href="mailto:${grailsApplication.config.grails.mail.default.from}">${grailsApplication.config.grails.mail.default.from}</a>
à votre carnet d'adresses pour vous assurer que vous continuez à recevoir des emails de notre part dans votre boîte de réception. <br>
<br>
Cordialement,<br>
<br><span style="font-weight: bold;">
L'équipe de ${grailsApplication.config.speciesPortal.app.siteName}</span> </div>
</td>
</tr>

</tbody>
</table><layout label="Two columns"></layout><layout label="Image gallery"></layout></td>
<td class="w30" width="30"></td>
</tr>
</tbody>
</table>
</td>
</tr>
<tr>
<td class="w640" bgcolor="#ffffff" height="15" width="640"></td>
</tr>
<tr>
<td class="w640" width="640">
<table id="footer" class="w640" bgcolor="#851608" border="0" cellpadding="0" cellspacing="0" width="640">
<tbody>

<tr>
<td class="w30" width="30"></td>
<td class="w580" valign="top" width="360">
</td>
<td class="hide w0" width="60"></td>
<td class="hide w0" valign="top" width="160">

</td>
<td class="w30" width="30"></td>
</tr>
<tr>
<td class="w30" width="30"></td>
<td class="w580 h0" height="15" width="360"></td>
<td class="w0" width="60"></td>
<td class="w0" width="160"></td>
<td class="w30" width="30"></td>
</tr>
</tbody>
</table>
</td>
</tr>
<tr>
<td class="w640" height="60" width="640"></td>
</tr>
</tbody>
</table>
</td>
</tr>
</tbody>
</table>
</body></html>
