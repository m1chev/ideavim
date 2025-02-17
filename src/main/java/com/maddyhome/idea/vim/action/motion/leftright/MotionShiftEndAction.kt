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

package com.maddyhome.idea.vim.action.motion.leftright

import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.api.ExecutionContext
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.VimMotionGroupBase
import com.maddyhome.idea.vim.command.Command
import com.maddyhome.idea.vim.group.MotionGroup
import com.maddyhome.idea.vim.handler.ShiftedSpecialKeyHandler
import com.maddyhome.idea.vim.helper.inInsertMode
import com.maddyhome.idea.vim.helper.inSelectMode
import com.maddyhome.idea.vim.helper.inVisualMode
import com.maddyhome.idea.vim.helper.vimForEachCaret
import com.maddyhome.idea.vim.helper.vimLastColumn
import com.maddyhome.idea.vim.newapi.ij
import com.maddyhome.idea.vim.options.OptionConstants
import com.maddyhome.idea.vim.options.OptionScope
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimString

class MotionShiftEndAction : ShiftedSpecialKeyHandler() {

  override val type: Command.Type = Command.Type.OTHER_READONLY

  override fun motion(editor: VimEditor, context: ExecutionContext, cmd: Command) {
    editor.ij.vimForEachCaret { caret ->
      var allow = false
      if (editor.inInsertMode) {
        allow = true
      } else if (editor.inVisualMode || editor.inSelectMode) {
        val opt = (VimPlugin.getOptionService().getOptionValue(OptionScope.LOCAL(editor), OptionConstants.selectionName) as VimString).value
        if (opt != "old") {
          allow = true
        }
      }

      val newOffset = VimPlugin.getMotion().moveCaretToLineEndOffset(editor.ij, caret, cmd.count - 1, allow)
      caret.vimLastColumn = VimMotionGroupBase.LAST_COLUMN
      MotionGroup.moveCaret(editor.ij, caret, newOffset)
      caret.vimLastColumn = VimMotionGroupBase.LAST_COLUMN
    }
  }
}
