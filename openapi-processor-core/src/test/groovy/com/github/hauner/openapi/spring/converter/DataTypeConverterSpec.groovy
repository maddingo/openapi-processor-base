/*
 * Copyright 2019 the original authors
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

package com.github.hauner.openapi.spring.converter

import com.github.hauner.openapi.spring.generatr.ApiOptions
import com.github.hauner.openapi.spring.generatr.DefaultApiOptions
import com.github.hauner.openapi.spring.model.Api
import com.github.hauner.openapi.spring.model.DataTypes
import com.github.hauner.openapi.spring.model.datatypes.ArrayDataType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.Schema
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.hauner.openapi.spring.support.OpenApiParser.parse

class DataTypeConverterSpec extends Specification {
    def converter = new DataTypeConverter(new DefaultApiOptions())


    void "creates none data type" () {
        when:
        def type = converter.none ()

        then:
        type
    }

    @Unroll
    void "converts schema(#type, #format) to #javaType" () {
        Schema schema = new Schema(type: type, format: format)

        when:
        def datatype = converter.convert (new SchemaInfo (schema, javaType, true), new DataTypes())

        then:
        datatype.name == javaType

        where:
        type      | format   | javaType
        'string'  | null     | 'String'
        'string'  | 'date'   | 'LocalDate'
        'integer' | null     | 'Integer'
        'integer' | 'int32'  | 'Integer'
        'integer' | 'int64'  | 'Long'
        'number'  | null     | 'Float'
        'number'  | 'float'  | 'Float'
        'number'  | 'double' | 'Double'
        'boolean' | null     | 'Boolean'
    }

    void "throws when hitting an unknown data type" () {
        Schema schema = new Schema(type: type, format: format)

        when:
        converter.convert (new SchemaInfo(schema, null), new DataTypes())

        then:
        def e = thrown(UnknownDataTypeException)
        e.type == type
        e.format == format

        where:
        type | format
        'x'  | null
        'y'  | 'none'
    }

    void "converts object schema with ref" () {
        def dt = new DataTypes()

        Schema barSchema = new Schema(type: 'object', properties: [
            val: new Schema (type: 'string')
        ])
        Schema fooSchema = new Schema (type: 'object', properties: [
            bar: new ObjectSchema ($ref: "#/components/schemas/Bar")
        ])

        when:
        converter.convert (new SchemaInfo (barSchema, 'Bar', false), dt)
        converter.convert (new SchemaInfo (fooSchema, 'Foo', false), dt)

        then:
        assert dt.size () == 2
        def bar = dt.find ('Bar')
        bar.properties['val'].name == 'String'
        def foo = dt.find ('Foo')
        foo.properties['bar'] == bar
    }

    void "converts simple array schema to Array[]" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /array-string:
    get:
      responses:
        '200':
          content:
            application/vnd.array:
              schema:
                type: array
                items:
                  type: string
          description: none              
""")
        when:
        def options = new ApiOptions(packageName: 'pkg')
        Api api = new ApiConverter (options).convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        ep.response.responseType.name == 'String[]'
    }

    void "converts object schema to Map<> set via x-java-type" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /endpoint-map:
    get:
      parameters:
        - name: props
          description: query, map from single property
          in: query
          required: false
          schema:
            type: object
            x-java-type: java.util.Map
            properties:
              prop1:
                type: string
              prop2:
                type: string
      responses:
        '204':
          description: empty
""")

        when:
        def options = new ApiOptions(packageName: 'pkg')
        Api api = new ApiConverter (options).convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        ep.parameters.first ().dataType.name == 'Map'
    }

    @Ignore
    void "converts ref object schema to Map<> set via x-java-type" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /endpoint-map:
    get:
      parameters:
        - name: props
          description: query, map from single property
          in: query
          required: false
          schema:
            \$ref: '#/components/schemas/SinglePropMap'
      responses:
        '204':
          description: empty

components:
  schemas:
    SinglePropMap:
      type: object
      x-java-type: java.util.Map
      properties:
        prop1:
          type: string
        prop2:
          type: string
""")

        when:
        def options = new ApiOptions(packageName: 'pkg')
        Api api = new ApiConverter (options).convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        ep.parameters.first ().dataType.name == 'Map'
    }

    void "creates model for inline response object with name {path}Response{response code}"() {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: API
  version: 1.0.0

paths:
  /inline:
    get:
      responses:
        '200':
          description: none
          content:
            application/json:
              schema:
                type: object
                properties:
                  isbn:
                    type: string
                  title:
                    type: string                
""")
        when:
        def options = new ApiOptions(packageName: 'pkg')
        Api api = new ApiConverter (options).convert (openApi)

        then:
        def itf = api.interfaces.first ()
        def ep = itf.endpoints.first ()
        def props = ep.response.responseType.properties
        ep.response.responseType.name == 'InlineResponse200'
        ep.response.responseType.packageName == "${options.packageName}.model"
        props.size () == 2
        props.get ('isbn').name == 'String'
        props.get ('title').name == 'String'

        and:
        api.models.size () == 1
        api.models.find ('InlineResponse200') is ep.response.responseType
    }

    void "creates model for component schema object" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: component schema object
  version: 1.0.0

paths:
  /book:
    get:
      responses:
        '200':
          description: none
          content:
            application/json:
                schema:
                  \$ref: '#/components/schemas/Book'

components:
  schemas:
    Book:
      type: object
      properties:
        isbn:
          type: string
        title:
          type: string
""")
        when:
        def options = new ApiOptions(packageName: 'pkg')
        Api api = new ApiConverter (options).convert (openApi)

        then:
        api.models.size () == 1

        and:
        def dataTypes = api.models
        def book = dataTypes.find ('Book')
        assert book.name == 'Book'
        assert book.packageName == "${options.packageName}.model"
        assert book.properties.size () == 2
        def isbn = book.properties.get('isbn')
        assert isbn.name == 'String'
        def title = book.properties.get('title')
        assert title.name == 'String'
    }

    void "creates named simple data types from #/components/schemas" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: component simple schemas 
  version: 1.0.0

paths:
  /book:
    get:
      responses:
        '200':
          description: none
          content:
            application/json:
                schema:
                  \$ref: '#/components/schemas/Book'

components:
  schemas:
    Isbn:
      type: string
     
    Title:
      type: string

    Book:
      type: object
      properties:
        isbn:
          \$ref: '#/components/schemas/Isbn'
        title:
          \$ref: '#/components/schemas/Title'
          
""")
        when:
        def options = new ApiOptions(packageName: 'pkg')
        Api api = new ApiConverter (options).convert (openApi)

        then:
        api.models.size () == 3

        and:
        def dataTypes = api.models
        assert dataTypes.find ('Book')
        assert dataTypes.find ('Isbn')
        assert dataTypes.find ('Title')
    }

    void "creates named array data types from #/components/schemas" () {
        def openApi = parse ("""\
openapi: 3.0.2
info:
  title: component simple schemas 
  version: 1.0.0

paths:
  /book:
    get:
      responses:
        '200':
          description: none
          content:
            application/json:
                schema:
                  \$ref: '#/components/schemas/Book'

components:

  schemas:
    Authors:
      type: array
      items:
        \$ref: '#/components/schemas/Author'
        
    Author:      
      type: object
      properties:
        name:
          type: string
     
    Book:
      type: object
      properties:
        authors:
          \$ref: '#/components/schemas/Authors'
""")

        when:
        def options = new ApiOptions(packageName: 'pkg')
        Api api = new ApiConverter (options).convert (openApi)

        then:
        api.models.size () == 3

        and:
        def dataTypes = api.models
        def book = dataTypes.find ('Book')
        assert book != null
        def authors = dataTypes.find ('Authors')
        assert authors instanceof ArrayDataType
        assert authors.name == 'Author[]'
        def author = dataTypes.find ('Author')
        assert author != null
    }

}
