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

import spock.lang.Shared

/**
 * Tests for CrmProductService.
 */
class ProductServiceSpec extends grails.plugin.spock.IntegrationSpec {

    def crmProductService

    @Shared pc
    @Shared mac

    def setup() {
        pc = crmProductService.createProductGroup(name: "PC", true)
        mac = crmProductService.createProductGroup(name: "Mac", true)
    }

    def "create product and make sure it got created"() {

        when: "create a product, but don't set group yet"
        def p = crmProductService.createProduct(number:"mbp13", name:"MacBook Pro 13\"")

        then: "product group is missing"
        p.validate() == false
        p.errors.getFieldError("group").code == "nullable"

        when: "set group"
        p.group = mac

        then: "validate passes"
        p.validate()

        when:
        p.save(failOnError:true, flush:true)

        then:
        p.ident() != null
        p.prices == null || p.prices.isEmpty()

        when:
        crmProductService.createProduct(number:"mbp15", name:"MacBook Pro 15\"", group:mac).save(failOnError: true, flush: true)
        crmProductService.createProduct(number:"dellxps15", name:"Dell XPS 15\"", group:pc).save(failOnError: true, flush: true)

        then:
        crmProductService.list().size() == 3
        crmProductService.listProducts([number:"mbp"], [:]).size() == 2

        when:
        p = crmProductService.listProducts([number:"mbp"], [sort:'number', order:'desc']).find{it}
        crmProductService.deleteProduct(p)

        then:
        crmProductService.listProducts([number:"mbp"], [:]).size() == 1
        crmProductService.listProducts([number:"mbp"], [:]).find{it}.name == "MacBook Pro 13\""
    }
}
