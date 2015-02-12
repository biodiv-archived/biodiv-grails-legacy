<%@ page import="species.participation.Flag.FlagType"%>
<%@ page import="species.participation.Observation"%>
<%@page import="species.utils.ImageType"%>
<div class = "flag-list-users">
<g:each var="flagInstance" in="${observationInstance.fetchAllFlags()}">
						<li style="padding: 0 5px; clear: both;">
							<span class="flagInstanceClass">
							<a href="${uGroup.createLink(controller:'user', action:'show', id:flagInstance.author?.id)}">
							<img class="small_profile_pic"
								src="${flagInstance.author?.profilePicture(ImageType.VERY_SMALL)}"
								title="${flagInstance.author.name}"/></a> : ${flagInstance.flag.value()} ${flagInstance.notes ? raw(": " + flagInstance.notes) : ""}</span>
							<sUser:ifOwns model="['user':flagInstance.author]">
								<a href="#" onclick="removeFlag(${flagInstance.id}, $(this).parent()); return false;"><span class="deleteFlagIcon" data-original-title="${g.message(code:'flaglistusers.remove.flag')}" ><i class="icon-trash"></i></span></a>
							</sUser:ifOwns>
							
						</li>
					</g:each>
                                    </div>

