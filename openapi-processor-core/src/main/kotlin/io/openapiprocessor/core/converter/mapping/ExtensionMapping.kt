/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-core
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.core.converter.mapping

class ExtensionMapping(
    /**
     * name of the extension.
     */
    val extension: String,

    /**
     * provides mappings for the extension.
     */
    val typeMappings: List<Mapping> = emptyList()

): Mapping {
    override fun getChildMappings(): List<Mapping> {
        return typeMappings
    }
}

