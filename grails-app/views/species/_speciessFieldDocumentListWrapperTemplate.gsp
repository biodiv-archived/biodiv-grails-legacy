<%@page import="species.participation.DocSciName"%>


<%
def link = ""
            def titleLink = "<ol type='disc'>"
            List result = DocSciName.findAllByScientificName(speciesInstance.taxonConcept.canonicalForm)
            if(result){
                result.each { 
                        def documentId = it.document.id
                        String docTitle = it.document.title
                        link = uGroup.createLink(controller:'document', action:'show', id:documentId, 'userGroupWebaddress':params?.webaddress, absolute:true)
                      titleLink += '<a class="species-page-link" style="font-style: normal;" href= "'+link+'">'+"<li>${docTitle}</li>"+"</a><br>"
                    
                    }
                    titleLink += "</ol>"
                    
            }
                                
%>
