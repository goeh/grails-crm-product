<%@ page import="grails.plugins.crm.product.CrmPriceList; grails.plugins.crm.core.TenantUtils" defaultCodec="html" %>
<tr>
    <td>
        <g:select name="prices[${row}].priceList.id" from="${CrmPriceList.findAllByTenantId(TenantUtils.tenant)}"
                  value="${bean.priceList?.id}" optionKey="id" class="span2"/>
    </td>

    <td><input type="text" name="prices[${row}].fromAmount" value="${formatNumber(number:bean.fromAmount)}" class="span2" required=""/></td>
    <td><g:textField name="prices[${row}].unit" value="${bean.unit}" required="" class="span2"/></td>
    <td><input type="text" name="prices[${row}].inPrice" value="${formatNumber(number:bean.inPrice, minFractionDigits: 2)}" required="" class="span2"/></td>
    <td><input type="text" name="prices[${row}].outPrice" value="${formatNumber(number:bean.outPrice, minFractionDigits: 2)}" required="" class="span2"/></td>
    <td><g:select name="prices[${row}].vat" from="${vatList}" value="${formatNumber(number:bean.vat, minFractionDigits: 2)}"
                  optionKey="${{formatNumber(number:it.value, minFractionDigits: 2)}}" optionValue="label" class="span2"/></td>
    <td>
        <button type="button" class="btn btn-danger btn-small btn-delete" tabindex="-1" onclick="deleteTableRow(this)"><i class="icon-trash icon-white"></i></button>
    </td>

</tr>