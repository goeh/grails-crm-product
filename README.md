# GR8 CRM - Product Plugin

CRM = [Customer Relationship Management](http://en.wikipedia.org/wiki/Customer_relationship_management)

GR8 CRM is a set of [Grails Web Application Framework](http://www.grails.org/)
plugins that makes it easy to develop web application with CRM functionality.
With CRM we mean features like:

- Contact Management
- Task/Todo Lists
- Project Management


## Product Plugin
This plugin provides product domain classes and services.

### Related Plugins
The [crm-product-ui](https://github.com/technipelago/grails-crm-product-ui) plugin provides
a Twitter Bootstrap user interface for managing products.

## Examples

    def pc = crmProductService.createProductGroup(name: 'PC', true)
    def mac = crmProductService.createProductGroup(name: 'Mac', true)

    def soho = crmProductService.createPriceList(param: 'soho', name: 'SOHO', true)

    def mpb = crmProductService.createProduct(number: 'mbp15', name: 'MacBook Pro 15"', group: mac, true)
    def dell = crmProductService.createProduct(number: 'dellxps15', name: 'Dell XPS 15"', group: pc, true)

    dell.addToPrices(priceList: soho, unit: 'pcs', fromAmount: 1, inPrice: 649, outPrice: 1299.99, vat: 0.15)
    dell.addToPrices(priceList: soho, unit: 'pcs', fromAmount: 10, inPrice: 649, outPrice: 1199.99, vat: 0.15)
    dell.addToPrices(priceList: soho, unit: 'pcs', fromAmount: 100, inPrice: 649, outPrice: 999.99, vat: 0.15)

### Documentation

Complete documentation for this plugin can be found at [gr8crm.github.io](http://gr8crm.github.io/plugins/crm-product/)
