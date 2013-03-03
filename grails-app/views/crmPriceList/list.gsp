<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'crmPriceList.label', default: 'Product Group')}"/>
    <title><g:message code="crmPriceList.list.title" args="[entityName]"/></title>
    <r:script>
        $(document).ready(function() {
            $(".table-striped tr").hover(function() {
                $("i", $(this)).removeClass('hide');
            }, function() {
                $("i", $(this)).addClass('hide');
            });
        });
    </r:script>
</head>

<body>

<crm:header title="crmPriceList.list.title" args="[entityName]"/>

<div class="row-fluid">
    <div class="span9">

        <table class="table table-striped">
            <thead>
            <tr>

                <g:sortableColumn property="name"
                                  title="${message(code: 'crmPriceList.name.label', default: 'Name')}"/>

                <g:sortableColumn property="description"
                                  title="${message(code: 'crmPriceList.description.label', default: 'Description')}"/>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${crmPriceListList}" var="crmPriceList">
                <tr>

                    <td>
                        <g:link action="edit" id="${crmPriceList.id}">
                            ${fieldValue(bean: crmPriceList, field: "name")}
                        </g:link>
                    </td>

                    <td>

                        <g:decorate max="100" include="abbreviate" encode="HTML">${crmPriceList.description}</g:decorate>

                    </td>
                    <td style="text-align:right;width:50px;">
                        <g:link action="moveUp" id="${crmPriceList.id}"><i
                                class="icon icon-arrow-up hide"></i></g:link>
                        <g:link action="moveDown" id="${crmPriceList.id}"><i
                                class="icon icon-arrow-down hide"></i></g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <crm:paginate total="${crmPriceListTotal}"/>

        <div class="form-actions">
            <crm:button type="link" action="create" visual="success" icon="icon-file icon-white"
                        label="crmPriceList.button.create.label"
                        permission="crmPriceList:create"/>
        </div>
    </div>

    <div class="span3">
        <crm:submenu/>
    </div>
</div>

</body>
</html>
