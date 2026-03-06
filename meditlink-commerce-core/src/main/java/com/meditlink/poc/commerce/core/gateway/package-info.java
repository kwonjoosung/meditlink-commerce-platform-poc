@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
                "shared :: domain", "shared :: rule",
                "product :: api", "product :: api-dto",
                "product :: command", "product :: app-dto", "product :: query",
                "product :: domain-productgroup", "product :: domain-product", "product :: domain-price"
        }
)
package com.meditlink.poc.commerce.core.gateway;
