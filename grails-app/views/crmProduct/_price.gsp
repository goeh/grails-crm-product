<%@ page import="grails.plugins.crm.product.CrmPriceList; grails.plugins.crm.core.TenantUtils" defaultCodec="html" %>
<tr>
    <td>
        <g:select name="prices[${row}].priceList.id" from="${CrmPriceList.findAllByTenantId(TenantUtils.tenant)}"
                  optionKey="id" class="span2"/>
    </td>

    <td><input type="number" name="prices[${row}].fromAmount" value="${bean.fromAmount}" class="span2"
               min="-999999" max="999999" step="0.01" required=""/></td>
    <td><g:textField name="prices[${row}].unit" value="${bean.unit}" required="" class="span2"/></td>
    <td><input type="number" name="prices[${row}].inPrice" value="${bean.inPrice}" required="" class="span2"
               min="-999999" max="999999" step="0.01"/></td>
    <td><input type="number" name="prices[${row}].outPrice" value="${bean.outPrice}" required="" class="span2"
               min="-999999" max="999999" step="0.01"/></td>
    <td><g:select name="prices[${row}].vat" from="${vatList}" value="${bean.vat}" optionKey="value"
                  optionValue="label" class="span2"/></td>
    <td>
        <button type="button" class="btn btn-danger btn-small btn-delete" tabindex="-1" onclick="deleteTableRow(this)"><i class="icon-trash icon-white"></i></button>
    </td>

</tr>