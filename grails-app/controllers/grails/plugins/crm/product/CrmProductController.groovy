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

import grails.plugins.crm.core.WebUtils
import org.springframework.dao.DataIntegrityViolationException
import grails.plugins.crm.core.TenantUtils

class CrmProductController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def crmProductService
    def crmContactService
    def selectionService
    def userTagService
    def crmSecurityService
    def shiroCrmSecurityService // TODO reference to Shiro Security!!!

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
            [crmProductList: result, crmProductTotal: result.totalCount, selection: uri]
        } catch (Exception e) {
            flash.error = e.message
            [crmProductList: [], crmProductTotal: 0, selection: uri]
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

        switch (request.method) {
            case "GET":
                return [crmProduct: crmProduct]
            case "POST":
                if (crmProduct.hasErrors() || !crmProduct.save()) {
                    render(view: "create", model: [crmProduct: crmProduct])
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

        [crmProduct: crmProduct]
    }

    private List getVatList() {
        [0, 6, 12, 25].collect {
            [label: "${it}%", value: (it / 100).floatValue()]
        }
    }

    def edit(Long id) {
        def crmProduct = CrmProduct.get(id)
        if (!crmProduct) {
            flash.error = message(code: 'crmProduct.not.found.message', args: [message(code: 'crmProduct.label', default: 'Product'), id])
            redirect(action: "index")
            return
        }
        switch (request.method) {
            case "GET":
                return [crmProduct: crmProduct, vatList: getVatList()]
            case "POST":
                if (params.int('version') != null) {
                    if (crmProduct.version > params.int('version')) {
                        crmProduct.errors.rejectValue("version", "crmProduct.optimistic.locking.failure",
                                [message(code: 'crmProduct.label', default: 'Product')] as Object[],
                                "Another user has updated this Product while you were editing")
                        render(view: "edit", model: [crmProduct: crmProduct, vatList: getVatList()])
                        return
                    }
                }

                println "before bindData: ${crmProduct.prices*.vat}"

                bindData(crmProduct, params, [include: CrmProduct.BIND_WHITELIST])

                println "after bindData: ${crmProduct.prices*.vat}"
                if (!crmProduct.save(flush: true)) {
                    render(view: "edit", model: [crmProduct: crmProduct, vatList: getVatList()])
                    return
                }

                flash.success = message(code: 'crmProduct.updated.message', args: [message(code: 'crmProduct.label', default: 'Product'), crmProduct.id])
                redirect(action: "show", id: crmProduct.id)
                break
        }
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
}
