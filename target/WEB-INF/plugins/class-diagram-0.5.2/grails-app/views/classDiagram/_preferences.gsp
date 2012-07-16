
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${prefs}">
        <div class="errors">
            <g:renderErrors bean="${prefs}" as="list" />
        </div>
    </g:hasErrors>

    <div class="dialog">
        <g:form name="preferencesForm" method="post" action="model">

            <fieldset id="misc_fields" class="">
                <ol>
                    <li title="Changes the appearance of the class diagram. You may override or configure your own in Config.groovy!">
                        <label for="skin">Skin:</label>
                        <g:select name="skin" value="${prefs?.skin}" from="${skins}" optionKey="key" optionValue="value"></g:select>
                    </li>
                </ol>
            </fieldset>
            
            <fieldset id="show_fields" class="">
                <label for="show_fields_inner">Show:</label>
                <fieldset id="show_fields_inner" class="">
                    <ol>
                        <li>
                            <label for="showProperties" title="Show or hide properties. This includes declared properties together with implicit properties declared with getters and/or setter methods.">
                                <g:checkBox name="showProperties" value="${prefs?.showProperties}" ></g:checkBox>
                                Properties
                            </label>
                        </li>
                        <li>
                            <fieldset id="showPropertiesSelected" class="">
                                <label for="showPropertyType" title="Show or hide property types">
                                    <g:checkBox name="showPropertyType" value="${prefs?.showPropertyType}" ></g:checkBox>
                                    Property Type
                                </label>
                            </fieldset>
                        </li>
                        <li>
                            <label for="showMethods" title="Show or hide methods. Note that property setters and getters are not included!">
                                <g:checkBox name="showMethods" value="${prefs?.showMethods}" ></g:checkBox>
                                Methods
                            </label>
                        </li>
                        <li>
                            <fieldset id="showMethodsSelected" class="">
                                <ol>
                                    <li>
                                        <label for="showMethodReturnType" title="Show or hide method return types">
                                            <g:checkBox name="showMethodReturnType" value="${prefs?.showMethodReturnType}" ></g:checkBox>
                                            Method Return Type
                                        </label>
                                    </li>
                                    <li>
                                        <label for="showMethodSignature" title="Show or hide method signature (parameter list)">
                                            <g:checkBox name="showMethodSignature" value="${prefs?.showMethodSignature}" ></g:checkBox>
                                            Method Signature
                                        </label>
                                    </li>
                                </ol>
                            </fieldset>
                        </li>
                        <li>
                            <label for="showAssociationNames" title="Show or hide the names of the associations">
                                <g:checkBox name="showAssociationNames" value="${prefs?.showAssociationNames}" ></g:checkBox>
                                Association Names
                            </label>
                        </li>
                        <li>
                            <label for="showEmbeddedAsProperty" title="Show embedded references (aka Value Objects) as properties in the containing class. Uncheck to see Embedded references as first class objects. (Embedded references is declared with static embedded = [], see gorm doc.) ">
                                <g:checkBox name="showEmbeddedAsProperty" value="${prefs?.showEmbeddedAsProperty}" ></g:checkBox>
                                Embedded Objects as Property
                            </label>
                        </li>
                        <li>
                            <label for="showEnumAsProperty" title="Show enums as properties in the containing class. Uncheck to see enums as first class objects.">
                                <g:checkBox name="showEnumAsProperty" value="${prefs?.showEnumAsProperty}" ></g:checkBox>
                                Enums as Property
                            </label>
                        </li>
                        <li>
                            <label for="showPackages" title="Show classes grouped in packages.">
                                <g:checkBox name="showPackages" value="${prefs?.showPackages}" ></g:checkBox>
                                Packages
                            </label>
                        </li>
                    </ol>
                </fieldset>
            </fieldset>
            
            <fieldset id="class_selection" class="">
                <ol>
                    <li title="Select a subset of classes by entering part of a class and/or package name. Ex. 'com.my.subdomain', '[Oo]rder', 'Foo|Bar'. Note: leading an trailing wildcards is not needed. Works on domain classes only. See http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html for regexp spec. ">
                        <label for="classSelection">Classes:</label>
                        <input type="text" id="classSelection" name="classSelection" value="${fieldValue(bean:prefs,field:'classSelection')}"/>
                        <g:checkBox name="classSelectionIsRegexp" value="${prefs.classSelectionIsRegexp}" ></g:checkBox>
                        Regexp
                    </li>
                </ol>
            </fieldset>
            
            <fieldset id="misc_fields" class="">
                <ol>
                    <li title="Changes the size of the underlying model image. Note: Will not have any effect if your skin has hardcoded fontsize.">
                        <input id="fontsize" name="fontsize" type="hidden" value="${prefs?.fontsize}"/>
                        <label for="fontsize_slider">Size:</label>
                        <div id="fontsize_slider"></div>
                    </li>
                    <li title="Image format. Try png, gif, img, pdf eps, dot, or any other format described in http://graphviz.org/doc/info/output.html. Warning: Some of these formats may not work, and you may not get a sensible error message!">
                        <label for="outputFormat">Output Format:</label>
                        <input type="text" size="5" id="outputFormat" name="outputFormat" value="${fieldValue(bean:prefs,field:'outputFormat')}"/>
                    </li>
                    <li title="Graph orientation, or, in graphviz terminology: rankdir">
                        <label for="graphOrientation">Orientation:</label>
                        <g:select name="graphOrientation" from="${graphOrientations}" optionKey="key" optionValue="value"></g:select>
                    </li>
                    <li title="Re-order the classes in the diagram. May not have any effect if no alternative ordering is found, such as for diagrams with few classes. Note: Default ordering will be restored when the diagram is changed.">
                        <input id="randomizeOrder" class="autoUpdate" name="randomizeOrder" type="hidden" value="${prefs?.randomizeOrder}"/>
                        <label for="randomize-class-order">&nbsp;</label>
                        <button id="randomize-class-order" type="button">Re-order layout</button>
                    </li>
                </ol>
            </fieldset>

            </div>
                <div id="autoUpdate-selection" title="Update the model image instantly when you change any preferences. Uncheck if you want to delay updating until you hit 'Update'">
                    <label for="autoUpdate">
                        <g:checkBox name="autoUpdate" value="${prefs?.autoUpdate}"></g:checkBox>
                        Auto Update
                    </label>
                </div>
            </div>
        </g:form>
    </div>
    



