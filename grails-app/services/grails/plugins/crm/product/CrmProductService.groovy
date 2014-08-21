/*
 * Copyright (c) 2014 Goran Ehrsson.
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

import grails.events.Listener
import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.core.SearchUtils
import grails.plugins.crm.core.CrmValidationException
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.web.metaclass.BindDynamicMethod
import grails.plugins.selection.Selectable

class CrmProductService {

    static transactional = true

    def crmTagService
    def crmSecurityService
    def messageSource

    @Listener(namespace = "crmProduct", topic = "enableFeature")
    def enableFeature(event) {
        // event = [feature: feature, tenant: tenant, role:role, expires:expires]
        def tenant = crmSecurityService.getTenantInfo(event.tenant)
        if (!tenant) {
            throw new IllegalArgumentException("Cannot find tenant info for tenant [${event.tenant}], event=$event")
        }
        TenantUtils.withTenant(tenant.id) {
            crmTagService.createTag(name: CrmProduct.name, multiple: true)
            def s = messageSource.getMessage("crmProduct.name.standard", null, "Standard", tenant.locale)
            createPriceList(name: s, param: "standard", true)
        }
    }

    @Listener(namespace = "crmTenant", topic = "requestDelete")
    def requestDeleteTenant(event) {
        def tenant = event.id
        def count = 0
        count += CrmProduct.countByTenantId(tenant)
        count += CrmProductGroup.countByTenantId(tenant)
        count += CrmPriceList.countByTenantId(tenant)
        count ? [namespace: 'crmProduct', topic: 'deleteTenant'] : null
    }

    @Listener(namespace = "crmProduct", topic = "deleteTenant")
    def deleteTenant(event) {
        def tenant = event.id
        CrmProductComposition.createCriteria().list() {
            mainProduct {
                eq('tenantId', tenant)
            }
        }*.delete()
        def result = CrmProduct.findAllByTenantId(tenant)
        result*.delete()
        CrmProductGroup.findAllByTenantId(tenant)*.delete()
        CrmPriceList.findAllByTenantId(tenant)*.delete()
        log.warn("Deleted ${result.size()} products in tenant $tenant")
    }

    @CompileStatic
    private String paramify(final String name, Integer maxSize = 20) {
        String param = name.toLowerCase().replace(' ', '-')
        if (param.length() > maxSize) {
            param = param[0..(maxSize - 1)]
            if (param[-1] == '-') {
                param = param[0..-2]
            }
        }
        param
    }

    /**
     * Empty query = search all records.
     *
     * @param params pagination parameters
     * @return List of CrmProduct domain instances
     */
    @Selectable
    def list(Map params = [:]) {
        listProducts([:], params)
    }

    /**
     * Find CrmProduct instances filtered by query.
     *
     * @param query filter parameters
     * @param params pagination parameters
     * @return List of CrmProduct domain instances
     */
    @Selectable
    def list(Map query, Map params) {
        listProducts(query, params)
    }

    /**
     * Find CrmProduct instances filtered by query.
     *
     * @param query filter parameters
     * @param params pagination parameters
     * @return List of CrmProduct domain instances
     */
    private List<CrmProduct> listProducts(Map query, Map params) {
        def tagged
        if (query.tags) {
            tagged = crmTagService.findAllIdByTag(CrmProduct, query.tags) ?: [0L]
        }

        CrmProduct.createCriteria().list(params) {
            eq('tenantId', TenantUtils.tenant)
            if (tagged) {
                inList('id', tagged)
            }
            if (query.number) {
                or {
                    ilike('number', SearchUtils.wildcard(query.number))
                    ilike('displayNumber', SearchUtils.wildcard(query.number))
                }
            }
            if (query.name) {
                or {
                    ilike('name', SearchUtils.wildcard(query.name))
                    ilike('displayName', SearchUtils.wildcard(query.name))
                }
            }
            if (query.supplier) {
                ilike('supplierName', SearchUtils.wildcard(query.supplier))
            }
            if (query.suppliersNumber) {
                ilike('suppliersNumber', SearchUtils.wildcard(query.suppliersNumber))
            }
            if (query.barcode) {
                ilike('barcode', SearchUtils.wildcard(query.barcode))
            }
            if (query.customsCode) {
                ilike('customsCode', SearchUtils.wildcard(query.customsCode))
            }
            if (query.group || query.productGroup) {
                group {
                    or {
                        ilike('name', SearchUtils.wildcard(query.group ?: query.productGroup))
                        eq('param', query.group ?: query.productGroup)
                    }
                }
            }
            if (query.enabled != null) {
                eq('enabled', query.enabled)
            }
            if (query.weight != null) {
                eq('weight', query.weight)
            }
            if (query.price != null) {
                prices {
                    eq('outPrice', query.price)
                }
            }
        }
    }

    /**
     * Find a product based on primary key.
     *
     * @param id
     * @return
     */
    CrmProduct getProduct(Long id) {
        CrmProduct.findByIdAndTenantId(id, TenantUtils.tenant)
    }

    /**
     * Find a product based on product number
     *
     * @param number product/item number
     * @return
     */
    CrmProduct getProduct(String number) {
        CrmProduct.findByNumberAndTenantId(number, TenantUtils.tenant)
    }

    private CrmProduct useProductInstance(CrmProduct crmProduct = null) {
        def tenant = TenantUtils.tenant
        if (crmProduct == null) {
            crmProduct = new CrmProduct()
        }
        if (crmProduct.tenantId) {
            if (crmProduct.tenantId != tenant) {
                throw new IllegalStateException("The current tenant is [$tenant] and the specified domain instance belongs to another tenant [${crmProduct.tenantId}]")
            }
        } else {
            crmProduct.tenantId = tenant
        }
        crmProduct
    }

    /**
     * Initialize a product with default parameters.
     *
     * @param crmProduct the product instance to initialize, or null to have it created for you
     * @param params property values
     * @return
     */
    CrmProduct initProduct(CrmProduct crmProduct, Map params, Locale locale = null) {
        crmProduct = useProductInstance(crmProduct)
        def args = [crmProduct, params, [include: CrmProduct.BIND_WHITELIST]]
        new BindDynamicMethod().invoke(crmProduct, 'bind', args.toArray())
        crmProduct.tenantId = TenantUtils.tenant
        if (params.enabled == null) {
            crmProduct.enabled = true
        }
        crmProduct.validate() // to trigger beforeValidate() setters
        crmProduct.clearErrors()
        return crmProduct
    }

    /**
     * Save a product instance product.
     *
     * @param crmProduct the product instance to save, or null to have it created for you
     * @param params property values
     * @return
     */
    CrmProduct saveProduct(CrmProduct crmProduct, Map params) {
        crmProduct = useProductInstance(crmProduct)
        if (params.group instanceof String) {
            params.group = getProductGroup(params.group)
        }
        def args = [crmProduct, params, [include: CrmProduct.BIND_WHITELIST]]
        new BindDynamicMethod().invoke(crmProduct, 'bind', args.toArray())
        if (params.enabled == null) {
            crmProduct.enabled = true
        }
        if (crmProduct.save()) {
            return crmProduct
        } else {
            // Eager fetch associations to avoid LazyInitializationException
            crmProduct.prices?.size()
            crmProduct.compositions?.size()
        }
        throw new CrmValidationException('crmProduct.validation.error', crmProduct)
    }

    /**
     * Create a new product.
     *
     * @param params property values
     * @param save true if the product should be saved immediately
     * @return
     */
    CrmProduct createProduct(Map params, boolean save = false) {
        def tenant = TenantUtils.tenant
        def m = CrmProduct.findByNumberAndTenantId(params.number, tenant)
        if (!m) {
            m = new CrmProduct()
            def args = [m, params, [include: CrmProduct.BIND_WHITELIST]]
            new BindDynamicMethod().invoke(m, 'bind', args.toArray())
            m.tenantId = tenant
            if (params.enabled == null) {
                m.enabled = true
            }
            if (save) {
                m.save()
            } else {
            }
        }
        return m
    }

    /**
     * Update existing product.
     *
     * @param crmProduct
     * @param params
     * @return
     */
    boolean updateProduct(CrmProduct crmProduct, Map params) {
        def args = [crmProduct, params, [include: CrmProduct.BIND_WHITELIST]]
        new BindDynamicMethod().invoke(crmProduct, 'bind', args.toArray())
        return crmProduct.validate()
    }

    /**
     * Delete a product.
     *
     * @param crmProduct
     * @return name of deleted product
     */
    String deleteProduct(CrmProduct crmProduct) {
        def tombstone = crmProduct.toString()
        def id = crmProduct.id
        def tenant = crmProduct.tenantId
        def username = crmSecurityService.currentUser?.username

        event(for: "crmProduct", topic: "delete", fork: false, data: [id: id, tenant: tenant, user: username, name: tombstone])

        CrmProductComposition.findAllByProduct(crmProduct)*.delete()
        crmProduct.delete(flush: true)

        log.debug "Deleted product #$id in tenant $tenant \"${tombstone}\""

        event(for: "crmProduct", topic: "deleted", data: [id: id, tenant: tenant, user: username, name: tombstone])

        return tombstone
    }

    def listProductGroups(Map params) {
        CrmProductGroup.findAllByTenantId(TenantUtils.tenant, params)
    }

    CrmProductGroup getProductGroup(String param) {
        CrmProductGroup.findByParamAndTenantId(param, TenantUtils.tenant)
    }

    CrmProductGroup createProductGroup(Map params, boolean save = false) {
        def tenant = TenantUtils.tenant
        if (!params.param && params.name) {
            params.param = paramify(params.name, new CrmProductGroup().constraints.param.maxSize)
        }
        def m = CrmProductGroup.findByParamAndTenantId(params.param, tenant)
        if (!m) {
            m = new CrmProductGroup()
            def args = [m, params, [include: CrmProductGroup.BIND_WHITELIST]]
            new BindDynamicMethod().invoke(m, 'bind', args.toArray())
            m.tenantId = tenant
            if (params.enabled == null) {
                m.enabled = true
            }
            if (save) {
                m.save()
            } else {
                m.validate()
                m.clearErrors()
            }
        }
        return m
    }

    boolean updateProductGroup(CrmProductGroup productGroup, Map params) {
        def args = [productGroup, params]
        new BindDynamicMethod().invoke(productGroup, 'bind', args.toArray())
        return productGroup.validate()
    }

    def deleteProductGroup(CrmProductGroup group) {
        group.delete(flush: true)
    }

    CrmPriceList getPriceList(String param) {
        CrmPriceList.findByParamAndTenantId(param, TenantUtils.tenant)
    }

    def listPriceLists(Map params) {
        CrmPriceList.findAllByTenantId(TenantUtils.tenant, params)
    }

    CrmPriceList createPriceList(Map params, boolean save = false) {
        def tenant = TenantUtils.tenant
        if (!params.param && params.name) {
            params.param = paramify(params.name, new CrmPriceList().constraints.param.maxSize)
        }
        def m = CrmPriceList.findByParamAndTenantId(params.param, tenant)
        if (!m) {
            m = new CrmPriceList()
            def args = [m, params, [include: CrmPriceList.BIND_WHITELIST]]
            new BindDynamicMethod().invoke(m, 'bind', args.toArray())
            m.tenantId = tenant
            if (params.enabled == null) {
                m.enabled = true
            }
            if (save) {
                m.save()
            } else {
                m.validate()
                m.clearErrors()
            }
        }
        return m
    }

    boolean updatePriceList(CrmPriceList priceList, Map params) {
        def args = [priceList, params]
        new BindDynamicMethod().invoke(priceList, 'bind', args.toArray())
        return priceList.validate()
    }

    def deletePriceList(CrmPriceList priceList) {
        priceList.delete()
    }

    /**
     * Get all product prices sorted by price list and unit/amount.
     *
     * @param crmProduct
     * @return
     */
    List<CrmProductPrice> findProductPrices(CrmProduct crmProduct) {
        CrmProductPrice.createCriteria().list() {
            eq('product', crmProduct)
            priceList {
                order 'orderIndex', 'asc'
            }
            order 'unit'
            order 'fromAmount'
        }
    }

    /**
     * Return price for a product.
     *
     * @param productNumber CrmProduct.number
     * @param amount amount to base price on
     * @param priceList CrmPriceList instance or CrmPriceList.param string
     * @param unit unit to get price for
     * @return outPrice for the product, or null if no price are found
     */
    Double getPrice(String productNumber, Integer amount = null, Object priceList = null, String unit = null) {
        CrmProduct.findByNumberAndTenantId(productNumber, TenantUtils.tenant, [cache: true])?.getPrice(amount, priceList, unit)
    }

    /**
     * Add a price to a product instance.
     *
     * @param crmProduct
     * @param priceList CrmPriceList instance or a price list's param value
     * @param fromAmount
     * @param unit
     * @param inPrice
     * @param outPrice
     * @param vat
     * @return
     */
    CrmProductPrice addPrice(CrmProduct crmProduct, Object priceList, Double fromAmount, String unit, Double inPrice, Double outPrice, Double vat) {
        def tenant = TenantUtils.tenant
        if (crmProduct?.tenantId != null && crmProduct.tenantId != tenant) {
            throw new IllegalStateException("The current tenant is [$tenant] and the specified domain instance belongs to another tenant [${crmProduct.tenantId}]")
        }
        if (!(priceList instanceof CrmPriceList)) {
            CrmPriceList tmp = getPriceList(priceList.toString())
            if (!tmp) {
                throw new IllegalArgumentException("CrmPriceList with param [$priceList] not found in tenant [$tenant]")
            }
            priceList = tmp
        }
        def price = new CrmProductPrice(product: crmProduct, priceList: priceList, unit: unit, fromAmount: fromAmount, inPrice: inPrice, outPrice: outPrice, vat: vat)
        if (!price.hasErrors()) {
            crmProduct.addToPrices(price)
        }
        return price
    }
}
