package com.maddyhome.idea.vim.helper

import com.maddyhome.idea.vim.api.VimEditor
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.common.TextRange
import com.maddyhome.idea.vim.options.OptionConstants
import com.maddyhome.idea.vim.options.OptionScope
import com.maddyhome.idea.vim.vimscript.model.datatypes.VimString
import java.util.*

inline fun <reified T : Enum<T>> noneOfEnum(): EnumSet<T> = EnumSet.noneOf(T::class.java)

val TextRange.endOffsetInclusive
  get() = if (this.endOffset > 0 && this.endOffset > this.startOffset) this.endOffset - 1 else this.endOffset

val VimEditor.mode
  get() = this.commandState.mode

val VimEditor.inVisualMode
  get() = this.mode.inVisualMode

var VimEditor.subMode
  get() = this.commandState.subMode
  set(value) {
    this.commandState.subMode = value
  }

val VimEditor.commandState
  get() = CommandState.getInstance(this)

val CommandState.Mode.inVisualMode
  get() = this == CommandState.Mode.VISUAL || this == CommandState.Mode.INSERT_VISUAL

val VimEditor.inBlockSubMode
  get() = this.subMode == CommandState.SubMode.VISUAL_BLOCK

/**
 * Please use `isEndAllowed` based on `Editor` (another extension function)
 * It takes "single command" into account.
 */
val CommandState.Mode.isEndAllowed: Boolean
  get() = when (this) {
    CommandState.Mode.INSERT, CommandState.Mode.VISUAL, CommandState.Mode.SELECT -> true
    CommandState.Mode.COMMAND, CommandState.Mode.CMD_LINE, CommandState.Mode.REPLACE, CommandState.Mode.OP_PENDING -> usesVirtualSpace
    CommandState.Mode.INSERT_NORMAL -> usesVirtualSpace
    CommandState.Mode.INSERT_VISUAL -> usesVirtualSpace
    CommandState.Mode.INSERT_SELECT -> usesVirtualSpace
  }

val usesVirtualSpace
  get() = (injector.optionService.getOptionValue(
    OptionScope.GLOBAL,
    OptionConstants.virtualeditName
  ) as VimString).value == "onemore"

val VimEditor.isEndAllowed: Boolean
  get() = when (this.mode) {
    CommandState.Mode.INSERT, CommandState.Mode.VISUAL, CommandState.Mode.SELECT, CommandState.Mode.INSERT_VISUAL, CommandState.Mode.INSERT_SELECT -> true
    CommandState.Mode.COMMAND, CommandState.Mode.CMD_LINE, CommandState.Mode.REPLACE, CommandState.Mode.OP_PENDING, CommandState.Mode.INSERT_NORMAL -> {
      // One day we'll use a proper insert_normal mode
      if (this.mode.inSingleMode) true else usesVirtualSpace
    }
  }

val CommandState.Mode.inSingleMode: Boolean
  get() = when (this) {
    CommandState.Mode.INSERT_NORMAL, CommandState.Mode.INSERT_SELECT, CommandState.Mode.INSERT_VISUAL -> true
    else -> false
  }

val CommandState.Mode.inSingleNormalMode: Boolean
  get() = when (this) {
    CommandState.Mode.INSERT_NORMAL -> true
    else -> false
  }

val VimEditor.inNormalMode
  get() = this.mode.inNormalMode

val CommandState.Mode.inNormalMode
  get() = this == CommandState.Mode.COMMAND || this == CommandState.Mode.INSERT_NORMAL

val CommandState.Mode.isEndAllowedIgnoringOnemore: Boolean
  get() = when (this) {
    CommandState.Mode.INSERT, CommandState.Mode.VISUAL, CommandState.Mode.SELECT -> true
    CommandState.Mode.COMMAND, CommandState.Mode.CMD_LINE, CommandState.Mode.REPLACE, CommandState.Mode.OP_PENDING -> false
    CommandState.Mode.INSERT_NORMAL -> false
    CommandState.Mode.INSERT_VISUAL -> true
    CommandState.Mode.INSERT_SELECT -> true
  }

val VimEditor.inInsertMode
  get() = this.mode == CommandState.Mode.INSERT || this.mode == CommandState.Mode.REPLACE

val VimEditor.inSelectMode
  get() = this.mode == CommandState.Mode.SELECT || this.mode == CommandState.Mode.INSERT_SELECT

inline fun <reified T : Enum<T>> enumSetOf(vararg value: T): EnumSet<T> = when (value.size) {
  0 -> noneOfEnum()
  1 -> EnumSet.of(value[0])
  else -> EnumSet.of(value[0], *value.slice(1..value.lastIndex).toTypedArray())
}

fun CommandState.pushSelectMode(subMode: CommandState.SubMode, prevMode: CommandState.Mode = this.mode) {
  if (prevMode.inSingleMode) {
    popModes()
    pushModes(CommandState.Mode.INSERT_SELECT, subMode)
  } else {
    pushModes(CommandState.Mode.SELECT, subMode)
  }
}
