/*
 * Copyright 2019-2020 the original authors
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

package com.github.hauner.openapi.spring.converter.mapping

import com.github.hauner.openapi.spring.converter.schema.MatchValues

/**
 * Used with {@link EndpointTypeMapping} to configure the java type that should represent the schema
 * of the given endpoint parameter.
 *
 * @author Martin Hauner
 */
class ParameterTypeMapping implements Mapping {

    /**
     * The parameter name of this mapping. Must match 1:1 with what is written in the api.
     */
    String parameterName

    /**
     * Type mapping valid only for requests with parameter {@link #parameterName}.
     */
    TypeMapping mapping

    /**
     * Checks if this is a mapping for the given parameter schema info
     *
     * @param match the match info
     * @return true if it is a mapping for info, else false
     */
    @Override
    boolean matches (MatchValues match) {
        parameterName == match.matchName
    }

    @Override
    boolean isLevel (MappingLevel level) {
        MappingLevel.IO == level
    }

    @Override
    List<Mapping> getChildMappings () {
        [mapping]
    }

}
