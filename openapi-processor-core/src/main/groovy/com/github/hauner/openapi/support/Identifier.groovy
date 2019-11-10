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

package com.github.hauner.openapi.support

/**
 * Identifier support to create valid java identifiers.
 *
 * @author Martin Hauner
 */
class Identifier {

    /**
     * converts a Json string as defined by http://www.json.org/ to a valid (camel case) java
     * identifier. One way, ie it is not reversible.
     *
     * conversion rules:
     * characters that are not valid java identifiers will be removed. The characters " ", "_",
     * "-" (valid or not) are interpreted as word separators and the next character will be
     * converted to upper case.
     *
     * @param json a valid json "string"
     *
     * @return a valid camel case java identifier
     */
    static String fromJson (String json) {
        def sb = new StringBuilder()

        def wordSplit = false
        json.toCharArray ().eachWithIndex { char c, int idx ->

            if (idx == 0) {
                if (isValidStart (c)) {
                    sb.append (c)
                }
            } else {
                if (isValidPart (c)) {
                    if (wordSplit) {
                        sb.append (c.toUpperCase ())
                        wordSplit = false
                    } else {
                        sb.append (c)
                    }
                } else {
                    wordSplit = true
                }
            }
        }

        sb.toString ()
    }

    private static boolean isValidStart (char c) {
        Character.isJavaIdentifierStart (c) && !isWordSplitPart (c)
    }

    private static boolean isValidPart (char c) {
        Character.isJavaIdentifierPart (c) && !isWordSplitPart (c)
    }

    private static boolean isWordSplitPart(char c) {
        c == '_' as char  // split at underscore
    }

}
