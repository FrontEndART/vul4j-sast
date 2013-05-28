/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.rest.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import lombok.Cleanup;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.filterwriter.TMXWriter;

import org.zanata.common.LocaleId;
import org.zanata.model.NamedDocument;
import org.zanata.model.SourceContents;
import org.zanata.util.OkapiUtil;
import org.zanata.util.VersionUtility;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TMXStreamingOutput implements StreamingOutput
{
   private static final String creationTool = "Zanata " + TMXStreamingOutput.class.getSimpleName();
   private static final String creationToolVersion =
         VersionUtility.getVersionInfo(TMXStreamingOutput.class).getVersionNo();
   private final Iterable<NamedDocument> documents;
   private final LocaleId sourceLocale;
   private final LocaleId targetLocale;
   private ExportTUStrategy exportTUStrategy;

   public TMXStreamingOutput(Iterable<NamedDocument> documents,
         LocaleId sourceLocale, LocaleId targetLocale, Long targetLocaleId)
   {
      this.documents = documents;
      this.sourceLocale = sourceLocale;
      this.targetLocale = targetLocale;
      if (targetLocaleId == null)
      {
         this.exportTUStrategy = new ExportAllLocalesStrategy();
      }
      else
      {
         this.exportTUStrategy = new ExportSingleLocaleStrategy(targetLocaleId);
      }
   }

   net.sf.okapi.common.LocaleId toOkapiLocaleOrEmpty(LocaleId locale)
   {
      net.sf.okapi.common.LocaleId okapiLocale = OkapiUtil.toOkapiLocale(locale);
      if (okapiLocale == null)
      {
         return net.sf.okapi.common.LocaleId.EMPTY;
      }
      return okapiLocale;
   }

   @Override
   public void write(OutputStream output) throws IOException, WebApplicationException
   {
      @Cleanup
      Writer writer = new PrintWriter(output);
      @Cleanup
      XMLWriter xmlWriter = new XMLWriter(writer);
      @Cleanup
      TMXWriter tmxWriter = new TMXWriter(xmlWriter);
      String segType = "block"; // TODO other segmentation types
      String dataType = "unknown"; // TODO track data type metadata throughout the system
      tmxWriter.writeStartDocument(
            toOkapiLocaleOrEmpty(sourceLocale),
            toOkapiLocaleOrEmpty(targetLocale),
            creationTool, creationToolVersion,
            segType, null, dataType);

      // TODO option to export obsolete docs to TMX?
      for (NamedDocument doc : documents)
      {
         exportDocument(tmxWriter, doc);
      }
      tmxWriter.writeEndDocument();
      tmxWriter.close();
   }

   private void exportDocument(TMXWriter tmxWriter, NamedDocument doc)
   {
      // TODO option to export obsolete TFs to TMX?
      for (SourceContents tf : doc)
      {
         exportTUStrategy.exportTranslationUnit(tmxWriter, doc, tf);
      }
   }

}
