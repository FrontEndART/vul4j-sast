/*
 * Sonar C++ Plugin (Community)
 * Copyright (C) 2010-2017 SonarOpenCommunity
 * http://github.com/SonarOpenCommunity/sonar-cxx
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.cxx.api;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.sonar.sslr.api.AstNode;

public class CppPunctuatorTest {

  @Test
  public void test() {
    assertThat(CppPunctuator.values()).hasSize(70);

    AstNode astNode = mock(AstNode.class);
    for (CppPunctuator punctuator : CppPunctuator.values()) {
      assertThat(punctuator.hasToBeSkippedFromAst(astNode)).isFalse();
    }
  }

}