/*
 * Copyright (c) 2014 T-Systems Multimedia Solutions GmbH
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

package de.tsystems.mms.apm.performancesignature.dynatrace.rest;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.List;

public class RESTStringArrayXMLHandler extends DefaultHandler {
    private final CharArrayWriter contents = new CharArrayWriter();
    private final List<String> objects = new ArrayList<>();

    public List<String> getObjects() {
        return this.objects;
    }

    public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes attr) {
        this.contents.reset();
        if (localName.equals("dashboard")) {
            this.objects.add(attr.getValue("id"));
        }
    }

    public void endElement(final String uri, final String localName, final String qName) {
        if (localName.equals("sessionid")) {
            this.objects.add(this.contents.toString());
        }
    }

    public void characters(final char[] ch, final int start, final int length) {
        this.contents.write(ch, start, length);
    }
}