/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2022 The IdeaVim authors
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

package org.jetbrains.plugins.ideavim.action.motion.text

import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.helper.VimBehaviorDiffers
import org.jetbrains.plugins.ideavim.VimTestCase

class MotionSectionForwardEndActionTest : VimTestCase() {
  @VimBehaviorDiffers(originalVimAfter = c, description = "Full text is deleted")
  fun `test remove full text`() {
    doTest(
      "d][",
      """
          ${c}I found it in a legendary land
          all rocks and lavender and tufted grass,
          where it was settled on some sodden sand
          hard by the torrent of a mountain pass.
      """.trimIndent(),
      "$c.",
      CommandState.Mode.COMMAND, CommandState.SubMode.NONE
    )
  }

  @VimBehaviorDiffers(originalVimAfter = c, description = "Full text is deleted")
  fun `test remove full text with new line at the end`() {
    doTest(
      "d][",
      """
          ${c}I found it in a legendary land
          all rocks and lavender and tufted grass,
          where it was settled on some sodden sand
          hard by the torrent of a mountain pass.
          
      """.trimIndent(),
      "$c.\n",
      CommandState.Mode.COMMAND, CommandState.SubMode.NONE
    )
  }

  fun `test move forward`() {
    doTest(
      "][",
      """
      {
        {
        
        }
      $c}
      {
        {
        }
      }
      {
        {
        }
      }
      """.trimIndent(),
      """
      {
        {
        
        }
      }
      {
        {
        }
      $c}
      {
        {
        }
      }
      """.trimIndent(),
      CommandState.Mode.COMMAND, CommandState.SubMode.NONE
    )
  }

  fun `test move forward twice`() {
    doTest(
      "][][",
      """
      {
        {
        $c
        }
      }
      {
        {
        }
      }
      {
        {
        }
      }
      """.trimIndent(),
      """
      {
        {
        
        }
      }
      {
        {
        }
      $c}
      {
        {
        }
      }
      """.trimIndent(),
      CommandState.Mode.COMMAND, CommandState.SubMode.NONE
    )
  }

  fun `test move forward till the end`() {
    doTest(
      "][][][",
      """
      {
        {
        $c
        }
      }
      {
        {
        }
      }
      {
        {
        }
      }
      """.trimIndent(),
      """
      {
        {
        
        }
      }
      {
        {
        }
      }
      {
        {
        }
      $c}
      """.trimIndent(),
      CommandState.Mode.COMMAND, CommandState.SubMode.NONE
    )
  }
}
