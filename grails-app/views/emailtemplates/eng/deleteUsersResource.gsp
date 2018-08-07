<%@ page contentType="text/html"%>

Dear ${username},<br/><br/>
The images that you have uploaded to your "My image uploads" area on the India Biodiversity Portal on ${uploadedDate} are due to expire tomorrow. If you do not utilize them these images will be cleared from your upload area on ${toDeleteDate}.<br/>
You may view your image uploads <a href="${uGroup.createLink(controller:'SUser', action:'myuploads', absolute:true)}">here</a>.
<br/><br/>
Thank you,<br/>
The Portal Team
