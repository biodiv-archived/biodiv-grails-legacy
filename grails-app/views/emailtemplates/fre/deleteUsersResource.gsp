<%@ page contentType="text/html"%>

Cher ${username},<br/><br/>
Les images que vous avez téléchargées sur votre «Mes images ajouts" "zone" sur le portail Wikwio le ${uploadedDate} arrivent à échéance demain. Si vous n'avez pas utilisé ces images celles-ci seront effacées de votre zone de téléchargement le​​ ${toDeleteDate}.<br/>
Vous pouvez consulter vos ajouts d'images <a href="${uGroup.createLink(controller:'SUser', action:'myuploads', absolute:true)}">ici</a>.
<br/><br/>
Merci,<br/>
L'équipe du portail "
