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

package com.github.hauner.openapi.micronaut.writer

import com.github.hauner.openapi.core.writer.SimpleWriter

/**
 * Writer for a simple header of the generated interfaces & classes.
 *
 * @author Martin Hauner
 */
class HeaderWriter implements SimpleWriter {

    static String HEADER = """\
/*
 * This class is auto generated by https://github.com/hauner/openapi-processor-micronaut.
 * DO NOT EDIT.
 */

"""

    @Override
    void write(Writer target) {
        target.write (HEADER)
    }

}
