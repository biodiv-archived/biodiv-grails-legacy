<table>
    <g:each in="${userCountList}" var="uc">
    <tr align="left">
        <td height="10" width="120" style=" border: 1px solid lightblue; text-align: center;">
                <a href="${uGroup.createLink(action:'show', controller:'SUser', id:uc.key.id)}">
                    <p>${uc.key.name}</p> 
                </a>
        </td>
        <td height="10" width="60" style=" border: 1px solid lightblue; text-align: center;">
            <p>${uc.value}</p>
        </td>
    </tr>
    </g:each>
</table>

