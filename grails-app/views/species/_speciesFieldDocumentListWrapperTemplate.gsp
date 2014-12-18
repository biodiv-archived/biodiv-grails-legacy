<%@page import="species.participation.DocSciName"%>
 




 <ol  class='references' style='list-style:disc;list-style-type:decimal; padding: 0px 30px;'>

<%
            def link = ""
            
            List result = DocSciName.findAllByScientificName(speciesInstance.taxonConcept.canonicalForm)
            if(result){
                result.each { 
                        def documentId = it.document.id
                        String docTitle = it.document.title
                        link = uGroup.createLink(controller:'document', action:'show', id:documentId, 'userGroupWebaddress':params?.webaddress, absolute:true) 
                        %>
                       <li> <a class="species-page-link" style="font-style: normal;" href= "${link}">${docTitle}</a></li>
                    <%
                    }
                    
                    
            }
                                
%>
</ol>
     

   