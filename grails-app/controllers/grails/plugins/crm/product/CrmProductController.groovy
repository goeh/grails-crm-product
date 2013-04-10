/*
 * Copyright (c) 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.product

import org.springframework.dao.DataIntegrityViolationException
import grails.converters.JSON
import grails.plugins.crm.core.WebUtils
import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.contact.CrmContact

class CrmProductController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def crmSecurityService
    def selectionService
    def crmProductService
    def crmContactService

    def index() {
        // If any query parameters are specified in the URL, let them override the last query stored in session.
        def cmd = new CrmProductQueryCommand()
        def query = params.getSelectionQuery()
        bindData(cmd, query ?: WebUtils.getTenantData(request, 'crmProductQuery'))
        [cmd: cmd]
    }

    def list() {
        def baseURI = new URI('bean://crmProductService/list')
        def query = params.getSelectionQuery()
        def uri

        switch (request.method) {
            case 'GET':
                uri = params.getSelectionURI() ?: selectionService.addQuery(baseURI, query)
                break
            case 'POST':
                uri = selectionService.addQuery(baseURI, query)
                WebUtils.setTenantData(request, 'crmProductQuery', query)
                break
        }

        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def result
        try {
            result = selectionService.select(uri, params)
            if (result.size() == 1) {
                redirect action: "show", id: result.head().ident()
            } else {
                [crmProductList: result, crmProductTotal: result.totalCount, selection: uri, currency: "SEK"]
            }
        } catch (Exception e) {
            flash.error = e.message
            [crmProductList: [], crmProductTotal: 0, selection: uri, currency: "SEK"] // TODO SEK!!!
        }
    }

    def clearQuery() {
        WebUtils.setTenantData(request, 'crmProductQuery', null)
        redirect(action: 'index')
    }

    def print() {
        def user = crmSecurityService.currentUser
        def tempFile = event(for: "crmProduct", topic: "print", data: params + [report: 'list', user: user, tenant: TenantUtils.tenant]).waitFor(20000)?.value
        if (tempFile instanceof File) {
            try {
                def filename = message(code: 'crmProduct.label', default: 'Product') + '.pdf'
                WebUtils.inlineHeaders(response, "application/pdf", filename)
                WebUtils.renderFile(response, tempFile)
            } finally {
                tempFile.delete()
            }
            return null // Success
        } else if (tempFile) {
            log.error("Print event returned an unexpected value: $tempFile (${tempFile.class.name})")
            flash.error = message(code: 'crmProduct.print.error.message', default: 'Printing failed due to an error', args: [tempFile.class.name])
        } else {
            flash.warning = message(code: 'crmProduct.print.nothing.message', default: 'Nothing was printed')
        }
        redirect(action: "index") // error condition, return to search form.
    }

    def create() {
        def crmProduct = crmProductService.createProduct(params)
        def groups = crmProductService.listProductGroups()
        switch (request.method) {
            case "GET":
                return [crmProduct: crmProduct, productGroups: groups]
            case "POST":
                crmProduct.supplier = getCompany(params.remove('supplier'))

                bindData(crmProduct, params, [include: CrmProduct.BIND_WHITELIST])

                if (crmProduct.hasErrors() || !crmProduct.save()) {
                    render(view: "create", model: [crmProduct: crmProduct, productGroups: groups])
                    return
                }

                flash.success = message(code: 'crmProduct.created.message', args: [message(code: 'crmProduct.label', default: 'Product'), crmProduct.toString()])
                redirect(action: "show", id: crmProduct.id)
                break
        }
    }

    def show(Long id) {
        def crmProduct = CrmProduct.get(id)
        if (!crmProduct) {
            flash.error = message(code: 'crmProduct.not.found.message', args: [message(code: 'crmProduct.label', default: 'Product'), id])
            redirect(action: "index")
            return
        }

        def prices = CrmProductPrice.createCriteria().list() {
            eq('product', crmProduct)
            priceList {
                order 'orderIndex', 'asc'
            }
            order 'unit'
            order 'fromAmount'
        }
        [crmProduct: crmProduct, prices: prices, currency: "SEK"] // TODO SEK!!!
    }

    // TODO !!!!!!
    private List getVatList() {
        [0, 6, 12, 25].collect {
            [label: "${it}%", value: (it / 100).doubleValue()]
        }
    }

    def edit(Long id) {
        def crmProduct = CrmProduct.get(id)
        if (!crmProduct) {
            flash.error = message(code: 'crmProduct.not.found.message', args: [message(code: 'crmProduct.label', default: 'Product'), id])
            redirect(action: "index")
            return
        }
        def groups = crmProductService.listProductGroups()
        switch (request.method) {
            case "GET":
                return [crmProduct: crmProduct, productGroups: groups, vatList: getVatList()]
            case "POST":
                if (params.int('version') != null) {
                    if (crmProduct.version > params.int('version')) {
                        crmProduct.errors.rejectValue("version", "crmProduct.optimistic.locking.failure",
                                [message(code: 'crmProduct.label', default: 'Product')] as Object[],
                                "Another user has updated this Product while you were editing")
                        render(view: "edit", model: [crmProduct: crmProduct, productGroups: groups, vatList: getVatList()])
                        return
                    }
                }

                crmProduct.supplier = getCompany(params.remove('supplier'))

                bindData(crmProduct, params, [include: CrmProduct.BIND_WHITELIST])

                if (!crmProduct.save(flush: true)) {
                    render(view: "edit", model: [crmProduct: crmProduct, productGroups: groups, vatList: getVatList()])
                    return
                }

                flash.success = message(code: 'crmProduct.updated.message', args: [message(code: 'crmProduct.label', default: 'Product'), crmProduct.toString()])
                redirect(action: "show", id: crmProduct.id)
                break
        }
    }

    private CrmContact getCompany(String name) {
        if (!name) {
            return null
        }
        def company = crmContactService.findByName(name)

        // A company name is specified but it's not an existing company, create a new company.
        if (!company) {
            company = crmContactService.createCompany(name: name).save(failOnError: true, flush: true)
        }

        return company
    }

    def delete(Long id) {
        def crmProduct = CrmProduct.get(id)
        if (!crmProduct) {
            flash.error = message(code: 'crmProduct.not.found.message', args: [message(code: 'crmProduct.label', default: 'Product'), id])
            redirect(action: "list")
            return
        }

        try {
            def tombstone = crmProduct.toString()
            crmProduct.delete(flush: true)
            flash.warning = message(code: 'crmProduct.deleted.message', args: [message(code: 'crmProduct.label', default: 'Product'), tombstone])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'crmProduct.not.deleted.message', args: [message(code: 'crmProduct.label', default: 'Product'), id])
            redirect(action: "show", id: id)
        }
    }

    def addPrice() {
        def crmProduct = params.id ? CrmProduct.get(params.id) : null
        render template: 'price', model: [row: 0, bean: new CrmProductPrice(product: crmProduct, fromAmount: 1, inPrice: 0, outPrice: 0, vat: 0.25), vatList: getVatList()]
    }

    def autocompleteSupplier() {
        def result = crmContactService.list([name: params.q], [max: 100]).collect { [it.name, it.id] }
        WebUtils.noCache(response)
        render result as JSON
    }

}
