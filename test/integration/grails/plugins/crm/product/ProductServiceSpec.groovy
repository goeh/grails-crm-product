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
    @Shared foo
    @Shared car
    @Shared priceList

    def setup() {
        pc = crmProductService.createProductGroup(name: "PC", true)
        mac = crmProductService.createProductGroup(name: "Mac", true)
        foo = crmProductService.createProductGroup(name: "Foo", true)
        car = crmProductService.createProductGroup(name: "Car", true)

        priceList = crmProductService.createPriceList(param: 'b2b', name: 'Small Businesses', true)
    }

    def "create product and make sure it got created"() {

        when: "create a product, but don't set group yet"
        def p = crmProductService.createProduct(number: "mbp13", name: "MacBook Pro 13\"")

        then: "product group is missing"
        p.validate() == false
        p.errors.getFieldError("group").code == "nullable"

        when: "set group"
        p.group = mac

        then: "validate passes"
        p.validate()

        when:
        p.save(failOnError: true, flush: true)

        then:
        p.ident() != null
        p.prices == null || p.prices.isEmpty()

        when:
        crmProductService.createProduct(number: "mbp15", name: "MacBook Pro 15\"", group: mac).save(failOnError: true, flush: true)
        crmProductService.createProduct(number: "dellxps15", name: "Dell XPS 15\"", group: pc).save(failOnError: true, flush: true)

        then:
        crmProductService.list().size() == 3
        crmProductService.listProducts([number: "mbp"], [:]).size() == 2

        when:
        p = crmProductService.listProducts([number: "mbp"], [sort: 'number', order: 'desc']).find { it }
        crmProductService.deleteProduct(p)

        then:
        crmProductService.listProducts([number: "mbp"], [:]).size() == 1
        crmProductService.listProducts([number: "mbp"], [:]).find { it }.name == "MacBook Pro 13\""
    }

    def "test multiple prices"() {

        given: "create a product"
        def p = crmProductService.createProduct(number: "dellxps15", name: "Dell XPS 15\"", group: pc)

        when: "create a product with staggered prices"
        p.addToPrices(priceList: priceList, unit: 'pcs', fromAmount: 1, inPrice: 0, outPrice: 1299.99, vat: 0.25)
        p.addToPrices(priceList: priceList, unit: 'pcs', fromAmount: 10, inPrice: 0, outPrice: 1199.99, vat: 0.25)
        p.addToPrices(priceList: priceList, unit: 'pcs', fromAmount: 100, inPrice: 0, outPrice: 999.99, vat: 0.25)

        then: "check that we get correct prices"
        p.save(failOnError: true, flush: true)
        crmProductService.getPrice("dellxps15") == 1299.99f
        crmProductService.getPrice("dellxps15", 1) == 1299.99f
        crmProductService.getPrice("dellxps15", 5) == 1299.99f
        crmProductService.getPrice("dellxps15", 15) == 1199.99f
        crmProductService.getPrice("dellxps15", 50) == 1199.99f
        crmProductService.getPrice("dellxps15", 150) == 999.99f
    }

    def "test multiple price lists"() {

        given: "create one product and multiple price lists"
        def priceList1 = crmProductService.createPriceList(param: 'a', name: 'A prices', true)
        def priceList2 = crmProductService.createPriceList(param: 'b', name: 'B prices', true)
        def priceList3 = crmProductService.createPriceList(param: 'c', name: 'C prices', true)
        def p = crmProductService.createProduct(number: "v240", name: "Volvo 240 GL 1987", group: car)

        when: "create a product with different price lists"
        p.addToPrices(priceList: priceList1, unit: 'pcs', fromAmount: 1, inPrice: 0, outPrice: 9000, vat: 0.25)
        p.addToPrices(priceList: priceList2, unit: 'pcs', fromAmount: 1, inPrice: 0, outPrice: 5000, vat: 0.25)
        p.addToPrices(priceList: priceList3, unit: 'pcs', fromAmount: 1, inPrice: 0, outPrice: 2000, vat: 0.25)

        then: "check that we get correct prices"
        p.save(failOnError: true, flush: true)
        crmProductService.getPrice("v240") == 9000f
        crmProductService.getPrice("v240", 1, priceList1) == 9000f
        crmProductService.getPrice("v240", 1, priceList2) == 5000f
        crmProductService.getPrice("v240", 1, priceList3) == 2000f
        p.getPrice(1, priceList1) == 9000f
        p.getPrice(2, priceList2) == 5000f
        p.getPrice(3, priceList3) == 2000f
    }

    def "test different prices by quantity and unit"() {

            given: "create one product and multiple price lists"
            def priceList1 = crmProductService.createPriceList(param: 'p', name: 'Private', true)
            def priceList2 = crmProductService.createPriceList(param: 'biz', name: 'Business', true)
            def p = crmProductService.createProduct(number: "meat", name: "Meat", group: foo)

            when: "create a product with different price lists"
            p.addToPrices(priceList: priceList1, unit: 'box', fromAmount: 1, inPrice: 0, outPrice: 499, vat: 0.25)
            p.addToPrices(priceList: priceList1, unit: 'kg', fromAmount: 1, inPrice: 0, outPrice: 99, vat: 0.25)
            p.addToPrices(priceList: priceList2, unit: 'box', fromAmount: 1, inPrice: 0, outPrice: 399, vat: 0.25)
            p.addToPrices(priceList: priceList2, unit: 'kg', fromAmount: 1, inPrice: 0, outPrice: 79, vat: 0.25)

            then: "check that we get correct prices"
            p.save(failOnError: true, flush: true)
            crmProductService.getPrice("meat") == 499
            crmProductService.getPrice("meat", 1, priceList1) == 499
            crmProductService.getPrice("meat", 1, priceList2) == 399
            crmProductService.getPrice("meat", 1, priceList1, "kg") == 99
            crmProductService.getPrice("meat", 1, priceList2, "kg") == 79
        }

    def "composition includes"() {
        given:
        def wheel = crmProductService.createProduct(number: "wheel", name: "Wheel", group: foo, true)
        def engine = crmProductService.createProduct(number: "engine", name: "Engine", group: foo, true)
        def car = crmProductService.createProduct(number: "car", name: "Car", group: foo)
        car.addIncludes(wheel, 4).addIncludes(engine, 1)

        expect:
        car.getIncludes().contains(wheel)
        car.getIncludes().contains(engine)
    }

    def "composition excludes"() {
        given:
        def gold = crmProductService.createProduct(number: "gold", name: "Gold level", group: foo, true)
        def silver = crmProductService.createProduct(number: "silver", name: "Silver level", group: foo, true)
        def bronse = crmProductService.createProduct(number: "bronse", name: "Bronse level", group: foo, true)
        gold.addExcludes(silver).addExcludes(bronse)
        silver.addExcludes(gold).addExcludes(bronse)
        bronse.addExcludes(gold).addExcludes(silver)

        expect:
        gold.getExcludes().contains(silver)
        gold.getExcludes().contains(bronse)
        silver.getExcludes().contains(gold)
        silver.getExcludes().contains(bronse)
        bronse.getExcludes().contains(gold)
        bronse.getExcludes().contains(silver)
    }
}
