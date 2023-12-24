/*
 * Copyright 2022 https://github.com/openapi-processor/openapi-processor-core
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.core.converter.mapping

open class AnnotationNameMappingDefault(
    /**
     * The parameter name of this mapping. Must match 1:1 with what is written in the api.
     */
    override val parameterName: String,

    /**
     * additional annotation of the type.
     */
    override val annotation: Annotation

): Mapping, AnnotationNameMapping
