class CrmProductGrailsPlugin {
    def groupId = ""
    def version = "2.0.0"
    def grailsVersion = "2.2 > *"
    def dependsOn = [:]
    def loadAfter = ['crmCore']
    def pluginExcludes = [
            "grails-app/conf/ApplicationResources.groovy",
            "src/groovy/grails/plugins/crm/product/ProductTestSecurityDelegate.groovy",
            "grails-app/views/error.gsp"
    ]
    def title = "GR8 CRM Product Services"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
A GR8 CRM plugin that provides product/item management for orders and sales projects, etc.
This is a "headless" plugin. Use interface for product management is provided by the crm-product-ui plugin.
'''
    def documentation = "http://gr8crm.github.io/plugins/crm-product/"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-crm-product/issues"]
    def scm = [url: "https://github.com/goeh/grails-crm-product"]

    def features = {
        crmProduct {
            description "Product Management"
            link controller: "crmProduct", action: "index"
            permissions {
                guest "crmProduct:index,list,show"
                partner "crmProduct:index,list,show"
                user "crmProduct:*"
                admin "crmProduct,crmProductGroup,crmPriceList:*"
            }
            hidden true
        }
    }
}
