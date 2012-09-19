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
import javax.servlet.http.HttpServletResponse

class CrmProductGroupController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 610,
                    title: 'crmProductGroup.label',
                    action: 'index'
            ]
    ]

    def selectionService
    def crmProductService

    def domainClass = CrmProductGroup

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        def baseURI = new URI('gorm://crmProductGroup/list')
        def query = params.getSelectionQuery()
        def uri

        switch (request.method) {
            case 'GET':
                uri = params.getSelectionURI() ?: selectionService.addQuery(baseURI, query)
                break
            case 'POST':
                uri = selectionService.addQuery(baseURI, query)
                grails.plugins.crm.core.WebUtils.setTenantData(request, 'crmProductGroupQuery', query)
                break
        }

        params.max = Math.min(params.max ? params.int('max') : 20, 100)

        try {
            def result = selectionService.select(uri, params)
            [crmProductGroupList: result, crmProductGroupTotal: result.totalCount, selection: uri]
        } catch (Exception e) {
            flash.error = e.message
            [crmProductGroupList: [], crmProductGroupTotal: 0, selection: uri]
        }
    }

    def create() {
        def crmProductGroup = crmProductService.createProductGroup(params)
        switch (request.method) {
            case 'GET':
                return [crmProductGroup: crmProductGroup]
            case 'POST':
                if (!crmProductGroup.save(flush: true)) {
                    render view: 'create', model: [crmProductGroup: crmProductGroup]
                    return
                }

                flash.success = message(code: 'crmProductGroup.created.message', args: [message(code: 'crmProductGroup.label', default: 'Product Group'), crmProductGroup.toString()])
                redirect action: 'list'
                break
        }
    }

    def edit() {
        switch (request.method) {
            case 'GET':
                def crmProductGroup = domainClass.get(params.id)
                if (!crmProductGroup) {
                    flash.error = message(code: 'crmProductGroup.not.found.message', args: [message(code: 'crmProductGroup.label', default: 'Product Group'), params.id])
                    redirect action: 'list'
                    return
                }

                return [crmProductGroup: crmProductGroup]
            case 'POST':
                def crmProductGroup = domainClass.get(params.id)
                if (!crmProductGroup) {
                    flash.error = message(code: 'crmProductGroup.not.found.message', args: [message(code: 'crmProductGroup.label', default: 'Product Group'), params.id])
                    redirect action: 'list'
                    return
                }

                if (params.version) {
                    def version = params.version.toLong()
                    if (crmProductGroup.version > version) {
                        crmProductGroup.errors.rejectValue('version', 'crmProductGroup.optimistic.locking.failure',
                                [message(code: 'crmProductGroup.label', default: 'Product Group')] as Object[],
                                "Another user has updated this Type while you were editing")
                        render view: 'edit', model: [crmProductGroup: crmProductGroup]
                        return
                    }
                }

                crmProductGroup.properties = params

                if (!crmProductGroup.save(flush: true)) {
                    render view: 'edit', model: [crmProductGroup: crmProductGroup]
                    return
                }

                flash.success = message(code: 'crmProductGroup.updated.message', args: [message(code: 'crmProductGroup.label', default: 'Product Group'), crmProductGroup.toString()])
                redirect action: 'list'
                break
        }
    }

    def delete() {
        def crmProductGroup = domainClass.get(params.id)
        if (!crmProductGroup) {
            flash.error = message(code: 'crmProductGroup.not.found.message', args: [message(code: 'crmProductGroup.label', default: 'Product Group'), params.id])
            redirect action: 'list'
            return
        }

        if (isInUse(crmProductGroup)) {
            render view: 'edit', model: [crmProductGroup: crmProductGroup]
            return
        }

        try {
            def tombstone = crmProductGroup.toString()
            crmProductGroup.delete(flush: true)
            flash.warning = message(code: 'crmProductGroup.deleted.message', args: [message(code: 'crmProductGroup.label', default: 'Product Group'), tombstone])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'crmProductGroup.not.deleted.message', args: [message(code: 'crmProductGroup.label', default: 'Product Group'), params.id])
            redirect action: 'edit', id: params.id
        }
    }

    private boolean isInUse(CrmProductGroup group) {
        def count = CrmProduct.countByGroup(group)
        def rval = false
        if (count) {
            flash.error = message(code: "crmProductGroup.delete.error.reference", args:
                    [message(code: 'crmProductGroup.label', default: 'Product Group'),
                            message(code: 'crmProduct.label', default: 'Product'), count],
                    default: "This {0} is used by {1} {2}")
            rval = true
        }

        return rval
    }

    def moveUp(Long id) {
        def target = domainClass.get(id)
        if (target) {
            def sort = target.orderIndex
            def prev = domainClass.createCriteria().list([sort: 'orderIndex', order: 'desc']) {
                lt('orderIndex', sort)
                maxResults 1
            }?.find {it}
            if (prev) {
                domainClass.withTransaction {tx ->
                    target.orderIndex = prev.orderIndex
                    prev.orderIndex = sort
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
        redirect action: 'list'
    }

    def moveDown(Long id) {
        def target = domainClass.get(id)
        if (target) {
            def sort = target.orderIndex
            def next = domainClass.createCriteria().list([sort: 'orderIndex', order: 'asc']) {
                gt('orderIndex', sort)
                maxResults 1
            }?.find {it}
            if (next) {
                domainClass.withTransaction {tx ->
                    target.orderIndex = next.orderIndex
                    next.orderIndex = sort
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
        redirect action: 'list'
    }
}
