/*
 * Copyright 2020 the original authors
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

package com.github.hauner.openapi.core.processor

import io.openapiprocessor.core.converter.mapping.*
import io.openapiprocessor.core.processor.MappingConverter
import io.openapiprocessor.core.processor.MappingReader
import io.openapiprocessor.core.processor.mapping.v2.ResultStyle
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class MappingConverterV2Spec extends Specification {

    def reader = new MappingReader()

    @Subject
    def converter = new MappingConverter()

    @Unroll
    void "reads global type mapping: (#input.source)" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  types:
    - type: ${input.source}
"""

        if (input.generics) {
            yaml += """\
      generics:
"""
        }

        input.generics?.each {
            yaml += """\
        - ${it} 
"""
        }

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size () == 1
        def type = mappings.first () as TypeMapping
        type.sourceTypeName == input.expected.sourceTypeName
        type.sourceTypeFormat == input.expected.sourceTypeFormat
        type.targetTypeName == input.expected.targetTypeName
        assertGenericTypes(type.genericTypes, input.expected.genericTypes)

        where:
        input << [
            [
                // normal
                source: 'array => java.util.Collection',
                expected: new TypeMapping (
                    'array',
                    null,
                    'java.util.Collection',
                    [],
                    false,
                    false)
            ], [
                // extra whitespaces
                source: '  array   =>    java.util.Collection   ',
                expected: new TypeMapping (
                    'array',
                    null,
                    'java.util.Collection',
                    [],
                    false,
                    false)
            ], [
                // with format
                source: 'string:date-time => java.time.ZonedDateTime',
                expected: new TypeMapping (
                    'string',
                    'date-time',
                    'java.time.ZonedDateTime',
                    [],
                    false,
                    false)
            ], [
                // extra whitespaces with format
                source  : '"  string  :  date-time   =>    java.time.ZonedDateTime   "',
                expected: new TypeMapping (
                    'string',
                    'date-time',
                    'java.time.ZonedDateTime',
                    [],
                    false,
                    false)
            ], [
                // with inline generics
                source: 'Foo => mapping.Bar<java.lang.String, java.lang.Boolean>',
                expected: new TypeMapping (
                    'Foo',
                    null,
                    'mapping.Bar',
                    [
                        new TargetType("java.lang.String", []),
                        new TargetType("java.lang.Boolean", [])
                    ],
                    false,
                    false)
            ], [
                // with extracted generics
                source: 'Foo => mapping.Bar',
                generics: ['java.lang.String', 'java.lang.Boolean'],
                expected: new TypeMapping (
                    'Foo',
                    null,
                    'mapping.Bar',
                    [
                        new TargetType("java.lang.String", []),
                        new TargetType("java.lang.Boolean", [])
                    ],
                    false,
                    false)
            ],  [
                // inline generics with extra whitespaces
                source: 'Foo => mapping.Bar  <   java.lang.String  ,   java.lang.Boolean   >   ',
                expected: new TypeMapping (
                    'Foo',
                    null,
                    'mapping.Bar',
                    [
                        new TargetType("java.lang.String", []),
                        new TargetType("java.lang.Boolean", [])
                    ],
                    false,
                    false)
            ], [
                // extracted generics with extra whitespaces
                source: 'Foo => mapping.Bar',
                generics: ['   java.lang.String   ', '   java.lang.Boolean   '],
                expected: new TypeMapping (
                    'Foo',
                    null,
                    'mapping.Bar',
                    [
                        new TargetType("java.lang.String", []),
                        new TargetType("java.lang.Boolean", [])
                    ],
                    false,
                    false)
            ], [
                // primitive
                source: 'Foo => byte',
                expected: new TypeMapping (
                    'Foo',
                    null,
                    'byte',
                    [],
                    true,
                    false)
            ], [
                // primitive array
                source: 'Foo => byte',
                expected: new TypeMapping (
                    'Foo',
                    null,
                    'byte',
                    [],
                    true,
                    true)
            ]
        ]
    }

    void "reads global response type mapping" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  responses:
    - content: application/vnd.array => java.util.List
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size() == 1

        def response = mappings.first () as ContentTypeMapping
        response.contentType == 'application/vnd.array'
        response.mapping.sourceTypeName == null
        response.mapping.sourceTypeFormat == null
        response.mapping.targetTypeName == 'java.util.List'
        response.mapping.genericTypes == []
    }

    void "reads global parameter type mapping" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  parameters:
    - name: foo => mapping.Foo
    - add: bar => mapping.Bar
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size() == 2

        def parameter = mappings.first () as NameTypeMapping
        parameter.parameterName == 'foo'
        parameter.mapping.sourceTypeName == null
        parameter.mapping.sourceTypeFormat == null
        parameter.mapping.targetTypeName == 'mapping.Foo'
        parameter.mapping.genericTypes == []

        def additional = mappings[1] as AddParameterTypeMapping
        additional.parameterName == 'bar'
        additional.mapping.sourceTypeName == null
        additional.mapping.sourceTypeFormat == null
        additional.mapping.targetTypeName == 'mapping.Bar'
        additional.mapping.genericTypes == []
    }

    void "reads endpoint exclude flag" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  paths:
    /foo:
      exclude: ${exclude.toString ()}
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size() == 1

        def endpoint = mappings.first () as EndpointTypeMapping
        endpoint.path == '/foo'
        endpoint.exclude == exclude
        endpoint.typeMappings.empty

        where:
        exclude << [true, false]
    }

    void "reads endpoint parameter type mapping" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  paths:
    /foo:
      parameters:
        - name: foo => mapping.Foo
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size() == 1

        def endpoint = mappings.first () as EndpointTypeMapping
        endpoint.path == '/foo'
        endpoint.typeMappings.size () == 1
        def parameter = endpoint.typeMappings.first () as NameTypeMapping
        parameter.parameterName == 'foo'
        parameter.mapping.sourceTypeName == null
        parameter.mapping.sourceTypeFormat == null
        parameter.mapping.targetTypeName == 'mapping.Foo'
        parameter.mapping.genericTypes == []
    }

    void "reads endpoint add mapping" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  paths:
    /foo:
      parameters:
        - add: request => javax.servlet.http.HttpServletRequest
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size () == 1

        def endpoint = mappings.first () as EndpointTypeMapping
        endpoint.path == '/foo'
        endpoint.typeMappings.size () == 1

        def parameter = endpoint.typeMappings.first () as AddParameterTypeMapping
        parameter.parameterName == 'request'
        parameter.mapping.sourceTypeName == null
        parameter.mapping.sourceTypeFormat == null
        parameter.mapping.targetTypeName == 'javax.servlet.http.HttpServletRequest'
        parameter.mapping.genericTypes == []
    }

    void "reads endpoint add mapping with annotation" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  paths:
    /foo:
      parameters:
        - add: foo => io.micronaut.http.annotation.RequestAttribute(ANY) java.lang.String
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size () == 1

        def endpoint = mappings.first () as EndpointTypeMapping
        endpoint.path == '/foo'
        endpoint.typeMappings.size () == 1

        def parameter = endpoint.typeMappings.first () as AddParameterTypeMapping
        parameter.parameterName == 'foo'
        parameter.mapping.sourceTypeName == null
        parameter.mapping.sourceTypeFormat == null
        parameter.mapping.targetTypeName == 'java.lang.String'
        parameter.mapping.genericTypes == []
        parameter.annotation.type == 'io.micronaut.http.annotation.RequestAttribute'
        parameter.annotation.parameters.size () == 1
        parameter.annotation.parameters[""].value == "ANY"
    }

    void "reads endpoint response type mapping" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  paths:
    /foo:
      responses:
        - content: application/vnd.array => java.util.List
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size() == 1

        def endpoint = mappings.first () as EndpointTypeMapping
        endpoint.path == '/foo'
        endpoint.typeMappings.size () == 1

        def response = endpoint.typeMappings.first () as ContentTypeMapping
        response.contentType == 'application/vnd.array'
        response.mapping.sourceTypeName == null
        response.mapping.sourceTypeFormat == null
        response.mapping.targetTypeName == 'java.util.List'
        response.mapping.genericTypes == []
    }

    void "reads global result mapping #result" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  result: $result
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size () == 1
        def type = mappings.first () as ResultTypeMapping
        type.targetTypeName == expected

        where:
        result                                    | expected
        'plain'                                   | 'plain'
        'org.springframework.http.ResponseEntity' | 'org.springframework.http.ResponseEntity'
    }

    void "reads endpoint result mapping #result" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  paths:
    /foo:
      result: $result
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size() == 1

        def endpoint = mappings.first () as EndpointTypeMapping
        endpoint.path == '/foo'
        endpoint.typeMappings.size () == 1

        def type = endpoint.typeMappings.first () as ResultTypeMapping
        type.targetTypeName == expected

        where:
        result                                    | expected
        'plain'                                   | 'plain'
        'org.springframework.http.ResponseEntity' | 'org.springframework.http.ResponseEntity'
    }

    void "reads global single & multi mapping" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  single: $single
  multi: $multi
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size () == 2
        def typeSingle = mappings.first () as TypeMapping
        typeSingle.sourceTypeName == 'single'
        typeSingle.targetTypeName == single
        def typeMulti = mappings[1] as TypeMapping
        typeMulti.sourceTypeName == 'multi'
        typeMulti.targetTypeName == multi

        where:
        single << ['reactor.core.publisher.Mono']
        multi << ['reactor.core.publisher.Flux']
    }

    void "reads endpoint single & multi mapping" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  paths:
    /foo:
      single: $single
      multi: $multi
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size() == 1

        def endpoint = mappings.first () as EndpointTypeMapping
        endpoint.path == '/foo'
        endpoint.typeMappings.size () == 2

        def typeSingle = endpoint.typeMappings.first () as TypeMapping
        typeSingle.sourceTypeName == 'single'
        typeSingle.targetTypeName == single
        def typeMulti = endpoint.typeMappings[1] as TypeMapping
        typeMulti.sourceTypeName == 'multi'
        typeMulti.targetTypeName == multi

        where:
        single << ['reactor.core.publisher.Mono']
        multi << ['reactor.core.publisher.Flux']
    }

    void "reads global result style mapping #resultStyle" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
  result-style: $resultStyle
"""

        when:
        def mapping = reader.read (yaml)
        def mappings = converter.convert (mapping)

        then:
        mappings.size () == 1
        def type = mappings.first () as OptionMapping
        type.name == 'resultStyle'
        type.value == expected

        where:
        resultStyle | expected
        'all'       | ResultStyle.ALL
        'success'   | ResultStyle.SUCCESS
    }

    void "does not fail on 'empty' options: key" () {
        String yaml = """\
openapi-processor-mapping: v2
options:
"""

        when:
        def mapping = reader.read (yaml)
        converter.convert (mapping)

        then:
        notThrown (Exception)
    }

    void "does not fail on 'empty' map: key" () {
        String yaml = """\
openapi-processor-mapping: v2
options: {}
map:
"""

        when:
        def mapping = reader.read (yaml)
        converter.convert (mapping)

        then:
        notThrown (Exception)
    }

    void "does not fail on 'empty' mapping.yaml" () {
        String yaml = """\
openapi-processor-mapping: v2
"""

        when:
        def mapping = reader.read (yaml)
        converter.convert (mapping)

        then:
        notThrown (Exception)
    }

    void assertGenericTypes (List<TargetType> actualTargetTypes, List<TargetType> expectedTargetTypes) {
        assert actualTargetTypes.size () == expectedTargetTypes.size()

        if (!expectedTargetTypes.isEmpty ()) {
            expectedTargetTypes.eachWithIndex { TargetType expected, int i ->
                assert actualTargetTypes[i].typeName == expected.typeName
            }
        }
    }
}
