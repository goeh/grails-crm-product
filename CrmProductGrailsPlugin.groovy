class CrmProductGrailsPlugin {
    // Dependency group
    def groupId = "grails.crm"
    // the plugin version
    def version = "1.1-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/conf/ApplicationResources.groovy",
            "src/groovy/grails/plugins/crm/product/TestSecurityDelegate.groovy",
            "grails-app/views/error.gsp"
    ]

    def title = "Grails CRM Product Plugin"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
Provides product/item management for orders and sales projects, etc.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/crm-product"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-crm-product/issues"]
    def scm = [url: "https://github.com/goeh/grails-crm-product"]

}
