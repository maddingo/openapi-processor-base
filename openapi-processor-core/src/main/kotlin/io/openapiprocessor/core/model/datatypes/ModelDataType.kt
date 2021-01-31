/*
 * Copyright 2020 https://github.com/openapi-processor/openapi-processor-core
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.core.model.datatypes

interface ModelDataType: DataType {

    /**
     * loop object properties.
     */
    fun forEach(action: (property: String, dataType: DataType) -> Unit)

    @Deprecated(message = "use forEach() instead", replaceWith = ReplaceWith("forEach()"))
    fun getProperties(): Map<String, DataType>

    fun isRequired(prop: String): Boolean

}
