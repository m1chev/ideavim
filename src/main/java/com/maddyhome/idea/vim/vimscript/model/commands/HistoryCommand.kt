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

package com.maddyhome.idea.vim.vimscript.model.commands

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.VimPlugin
import com.maddyhome.idea.vim.ex.ExOutputModel
import com.maddyhome.idea.vim.ex.ranges.Ranges
import com.maddyhome.idea.vim.history.HistoryConstants.COMMAND
import com.maddyhome.idea.vim.history.HistoryConstants.EXPRESSION
import com.maddyhome.idea.vim.history.HistoryConstants.INPUT
import com.maddyhome.idea.vim.history.HistoryConstants.SEARCH
import com.maddyhome.idea.vim.vimscript.model.ExecutionResult

/**
 * see "h :history"
 */
data class HistoryCommand(val ranges: Ranges, val argument: String) : Command.SingleExecution(ranges, argument) {
  override val argFlags = flags(RangeFlag.RANGE_FORBIDDEN, ArgumentFlag.ARGUMENT_OPTIONAL, Access.READ_ONLY)
  override fun processCommand(editor: Editor, context: DataContext): ExecutionResult {
    logger.debug("execute")

    var arg = argument.trim().ifEmpty { "cmd" }

    var key: String
    val spos = arg.indexOf(' ')
    if (spos >= 0) {
      key = arg.take(spos).trim()
      arg = arg.substring(spos + 1)
    } else {
      key = arg
      arg = ""
    }

    logger.debug { "key='$key'" }

    if (key.length == 1 && key[0] in ":/=@") {
      when (key[0]) {
        ':' -> key = "cmd"
        '/' -> key = "search"
        '=' -> key = "expr"
        '@' -> key = "input"
      }
    } else if (key[0].isLetter()) {
      if (!"cmd".startsWith(key) &&
        !"search".startsWith(key) &&
        !"expr".startsWith(key) &&
        !"input".startsWith(key) &&
        !"all".startsWith(key)
      ) {
        // Invalid command
        logger.debug { "invalid command $key" }
        return ExecutionResult.Error
      }
    } else {
      arg = "$key $arg"
      key = "cmd"
    }

    val first: String
    val last: String
    val cpos = arg.indexOf(',')
    if (cpos >= 0) {
      first = arg.substring(0, cpos).trim()
      last = arg.substring(cpos + 1).trim()
    } else {
      first = arg
      last = ""
    }

    val f = if (first.isNotEmpty()) {
      first.toIntOrNull() ?: run {
        logger.debug("bad number")
        return ExecutionResult.Error
      }
    } else 0
    val l = if (last.isNotEmpty()) {
      last.toIntOrNull() ?: run {
        logger.debug("bad number")
        return ExecutionResult.Error
      }
    } else 0

    val p = processKey(f, l)
    val res = when (key[0]) {
      'c' -> p(COMMAND)
      's' -> p(SEARCH)
      'e' -> p(EXPRESSION)
      'i' -> p(INPUT)
      'a' -> "${p(COMMAND)}${p(SEARCH)}${p(EXPRESSION)}${p(INPUT)}"
      else -> ""
    }

    ExOutputModel.getInstance(editor).output(res)

    return ExecutionResult.Success
  }

  private fun processKey(start: Int, end: Int) = { key: String ->
    logger.debug { "process $key $start,$end" }

    VimPlugin.getHistory().getEntries(key, start, end).joinToString("\n", prefix = "      #  $key history\n") { entry ->
      val num = entry.number.toString().padStart(7)
      "$num  ${entry.entry}"
    }
  }

  companion object {
    private val logger = Logger.getInstance(HistoryCommand::class.java.name)
  }
}
