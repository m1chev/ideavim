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
import com.maddyhome.idea.vim.api.VimCaret
import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.VimMotionGroupBase
import com.maddyhome.idea.vim.command.Argument
import com.maddyhome.idea.vim.command.Command
import com.maddyhome.idea.vim.command.MotionType
import com.maddyhome.idea.vim.handler.NonShiftedSpecialKeyHandler
import com.maddyhome.idea.vim.helper.inInsertMode
import com.maddyhome.idea.vim.helper.inSelectMode
import com.maddyhome.idea.vim.helper.inVisualMode
import com.maddyhome.idea.vim.helper.vimLastColumn
import com.maddyhome.idea.vim.newapi.ij
import com.maddyhome.idea.vim.options.OptionConstants
import com.maddyhome.idea.vim.options.OptionScope
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimString

class MotionEndAction : NonShiftedSpecialKeyHandler() {
  override val motionType: MotionType = MotionType.INCLUSIVE

  override fun offset(
    editor: VimEditor,
    caret: VimCaret,
    context: ExecutionContext,
    count: Int,
    rawCount: Int,
    argument: Argument?,
  ): Int {
    var allow = false
    if (editor.inInsertMode) {
      allow = true
    } else if (editor.inVisualMode || editor.inSelectMode) {
      val opt = (VimPlugin.getOptionService().getOptionValue(OptionScope.LOCAL(editor), OptionConstants.selectionName) as VimString).value
      if (opt != "old") {
        allow = true
      }
    }

    return VimPlugin.getMotion().moveCaretToLineEndOffset(editor.ij, caret.ij, count - 1, allow)
  }

  override fun preMove(editor: VimEditor, caret: VimCaret, context: ExecutionContext, cmd: Command) {
    caret.ij.vimLastColumn = VimMotionGroupBase.LAST_COLUMN
  }

  override fun postMove(editor: VimEditor, caret: VimCaret, context: ExecutionContext, cmd: Command) {
    caret.ij.vimLastColumn = VimMotionGroupBase.LAST_COLUMN
  }
}
