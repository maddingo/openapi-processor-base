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

package com.github.hauner.openapi.spring.generatr

/**
 * Options of the generatr.
 *
 * @author Martin Hauner
 */
class ApiOptions {

    /**
     * the path to the open api yaml file.
     */
    String apiPath

    /**
     * the destination folder for generating interfaces & models. This is the parent of the
     * {@link #packageName} folder tree.
     */
    String targetDir

    /**
     * the root package of the generated interfaces/model. The package folder tree will be created
     * inside {@link #targetDir}. Interfaces and models will be placed into the "api" and "model"
     * subpackages of packageName:
     * - interfaces => "${packageName}.api"
     * - models => "${packageName}.model"
     */
    String packageName

    /**
     * show warnings from the open api parser.
     */
    boolean showWarnings

    /**
     * provide additional type mapping information to map OpenAPI types to java types. The list can
     * contain the following mappings:
     *
     * {@link com.github.hauner.openapi.spring.generatr.mapping.ArrayTypeMapping}: used to globally
     * override the default mapping of the OpenAPI {@code array} from a simple java array to another
     * collection type.
     *
     * {@link com.github.hauner.openapi.spring.generatr.mapping.EndpointTypeMapping}: used to override
     * parameter, response type mappings or to add additional parameters on a single endpoint.
     */
    List<?> typeMappings

}
