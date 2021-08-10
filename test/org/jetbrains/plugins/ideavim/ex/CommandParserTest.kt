/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2021 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.jetbrains.plugins.ideavim.ex

import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.vimscript.model.commands.LetCommand
import com.maddyhome.idea.vim.vimscript.parser.VimscriptParser
import com.maddyhome.idea.vim.vimscript.parser.errors.IdeavimErrorListener
import org.jetbrains.plugins.ideavim.SkipNeovimReason
import org.jetbrains.plugins.ideavim.TestWithoutNeovim
import org.jetbrains.plugins.ideavim.VimTestCase

/**
 * @author Alex Plate
 */
class CommandParserTest : VimTestCase() {

  @TestWithoutNeovim(SkipNeovimReason.UNCLEAR, "Caret different position")
  fun `test simple ex command execution`() {
    val keys = ">>"
    val before = "I ${c}found it in a legendary land"
    val after = "    ${c}I found it in a legendary land"
    doTest(keys, before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE)
  }

  @TestWithoutNeovim(reason = SkipNeovimReason.EDITOR_MODIFICATION)
  fun `test execute in disabled state`() {
    setupChecks {
      caretShape = false
    }
    val keys = commandToKeys(">>")
    val before = "I ${c}found it in a legendary land"
    val after = "I ${c}found it in a legendary land"
    doTest(keys, before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE) {
      VimPlugin.setEnabled(false)
    }
  }

  @TestWithoutNeovim(reason = SkipNeovimReason.EDITOR_MODIFICATION)
  fun `test turn off and on`() {
    val keys = commandToKeys(">>")
    val before = "I ${c}found it in a legendary land"
    val after = "        ${c}I found it in a legendary land"
    doTest(keys, before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE) {
      VimPlugin.setEnabled(false)
      VimPlugin.setEnabled(true)
    }
  }

  @TestWithoutNeovim(reason = SkipNeovimReason.EDITOR_MODIFICATION)
  fun `test turn off and on twice`() {
    val keys = commandToKeys(">>")
    val before = "I ${c}found it in a legendary land"
    val after = "        ${c}I found it in a legendary land"
    doTest(keys, before, after, CommandState.Mode.COMMAND, CommandState.SubMode.NONE) {
      VimPlugin.setEnabled(false)
      VimPlugin.setEnabled(true)
      VimPlugin.setEnabled(true)
    }
  }

  fun `test multiline command input`() {
    val script1 = VimscriptParser.parse(
      """
     let s:patBR = substitute(match_words.',',
      \ s:notslash.'\zs[,:]*,[,:]*', ',', 'g') 
      """.trimIndent()
    )
    val script2 = VimscriptParser.parse(
      """
     let s:patBR = substitute(match_words.',',s:notslash.'\zs[,:]*,[,:]*', ',', 'g')
      """.trimIndent()
    )
    assertEquals(1, script1.units.size)
    assertTrue(script1.units[0] is LetCommand)
    assertEquals(script1, script2)
  }

  fun `test multiline expression input`() {
    configureByText("\n")
    val script1 = VimscriptParser.parse(
      """
      let dict = {'one': 1,
      \ 'two': 2}
      """.trimIndent()
    )
    val script2 = VimscriptParser.parse("let dict = {'one': 1, 'two': 2}")
    assertEquals(1, script1.units.size)
    assertTrue(script1.units[0] is LetCommand)
    assertEquals(script1, script2)
  }

  fun `test errors`() {
    configureByText("\n")
    VimscriptParser.parse(
      """
        echo 4
        let x = 3
        echo ^523
        echo 6
      """.trimIndent()
    )
    assertTrue(IdeavimErrorListener.testLogger.any { it.startsWith("line 3:5") })
  }

  fun `test errors 2`() {
    VimscriptParser.parse(
      """
        delfunction F1()
        echo 4
        echo 6
        *(
        let x = 5
      """.trimIndent()
    )
    assertTrue(IdeavimErrorListener.testLogger.any { it.startsWith("line 1:14") })
    assertTrue(IdeavimErrorListener.testLogger.any { it.startsWith("line 4:0") })
  }
}
