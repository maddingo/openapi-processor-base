/*
 * Copyright 2019 https://github.com/openapi-processor/openapi-processor-core
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.core.converter

import io.openapiprocessor.core.converter.mapping.*
import io.openapiprocessor.core.processor.mapping.v2.ResultStyle
import io.openapiprocessor.core.support.Empty

/**
 * Options of the processor.
 */
class ApiOptions {

    /**
     * the destination folder for generating interfaces & models. This is the parent of the
     * {@link #packageName} folder tree.
     */
    var targetDir: String? = null

    /**
     * the root package of the generated interfaces/model. The package folder tree will be created
     * inside {@link #targetDir}. Interfaces and models will be placed into the "api" and "model"
     * subpackages of packageName:
     * - interfaces => "${packageName}.api"
     * - models => "${packageName}.model"
     */
    var packageName = "io.openapiprocessor.generated"

    /**
     * enable Bean Validation (JSR303) annotations. Default is false (disabled)
     */
    var beanValidation = false

    /**
     * Bean Validation format: javax (v2) or jakarta (v3)
     */
    var beanValidationFormat: String? = null

    /**
     * enable/disable generation of javadoc comments based on the `description` OpenAPI property.
     *
     * *experimental*
     */
    var javadoc = false

    /**
     * model type. pojo or record.
     */
    var modelType = "default"

    /**
     * enum type. default|string|supplier.
     */
    var enumType = "default"

    /**
     * suffix for model class names and enum names. Default is none, i.e. an empty string.
     */
    var modelNameSuffix = String.Empty

    /**
     * enable/disable generation of a common interface for an `oneOf` list of objects. All objects
     * implement that interface.
     */
    var oneOfInterface = false

    /**
     * enable/disable the code formatter (optional).
     */
    var formatCode = false

    /**
     *  enable/disable the @Generated date (optional).
     */
    var generatedDate = true

    /**
     * provide additional type mapping information to map OpenAPI types to java types. The list can
     * contain the following mappings:
     *
     * [io.openapiprocessor.core.converter.mapping.TypeMapping]: used to globally
     * override the mapping of an OpenAPI schema to a specific java type.
     *
     * [io.openapiprocessor.core.converter.mapping.EndpointTypeMapping]: used to
     * override parameter/response type mappings or to add additional parameters on a single
     * endpoint.
     */
    var typeMappings: List<Mapping> = emptyList()

    /**
     * validate that targetDir is set, throws if not.
     */
    fun validate() {
        if (targetDir == null) {
            throw InvalidOptionException("targetDir")
        }
    }

    // compatibility options

    /**
     * add @Valid on reactive type and not on the wrapped type
     */
    var beanValidationValidOnReactive = true

    /**
     * break identifier names from digits to letters.
     */
    var identifierWordBreakFromDigitToLetter = true
}

/**
 * get (global) result style option mapping value if set, otherwise [ResultStyle.SUCCESS].
 *
 * this is a shortcut to avoid the dependency on the
 * [io.openapiprocessor.core.converter.mapping.MappingFinder] for this *simple* case.
 */
val ApiOptions.resultStyle: ResultStyle
    get() {
        val matches = typeMappings
            .filterIsInstance<ResultStyleOptionMapping>()

        if (matches.isEmpty())
            return ResultStyle.SUCCESS

        return matches.first().value
    }
