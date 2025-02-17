package com.maddyhome.idea.vim.api

import com.maddyhome.idea.vim.handler.Motion
import com.maddyhome.idea.vim.handler.toMotionOrError
import com.maddyhome.idea.vim.helper.isEndAllowed
import com.maddyhome.idea.vim.helper.isEndAllowedIgnoringOnemore
import com.maddyhome.idea.vim.helper.mode
import kotlin.math.abs
import kotlin.math.sign

abstract class VimMotionGroupBase : VimMotionGroup {
  override fun getVerticalMotionOffset(editor: VimEditor, caret: VimCaret, count: Int): Int {
    val pos = caret.getVisualPosition()
    if ((pos.line == 0 && count < 0) || (pos.line >= injector.engineEditorHelper.getVisualLineCount(editor) - 1 && count > 0)) {
      return -1
    } else {
      var col = caret.vimLastColumn
      val line = injector.engineEditorHelper.normalizeVisualLine(editor, pos.line + count)

      if (col == LAST_COLUMN) {
        col = injector.engineEditorHelper.normalizeVisualColumn(
          editor, line, col,
          editor.mode.isEndAllowedIgnoringOnemore
        )
      } else {
        if (line < 0) {
          // https://web.ea.pages.jetbrains.team/#/issue/266279
          // There is a weird exception for line < 0, but I don't understand how this may happen
          throw RuntimeException("Line is " + line + " , pos.line=" + pos.line + ", count=" + count)
        }
        val newInlineElements = injector.engineEditorHelper
          .amountOfInlaysBeforeVisualPosition(editor, VimVisualPosition(line, col, false))

        col = injector.engineEditorHelper
          .normalizeVisualColumn(editor, line, col, (editor).isEndAllowed)
        col += newInlineElements
      }

      val newPos = VimVisualPosition(line, col, false)
      return editor.visualPositionToOffset(newPos).point
    }
  }

  override fun moveCaretToLineEnd(editor: VimEditor, line: Int, allowPastEnd: Boolean): Int {
    return injector.engineEditorHelper.normalizeOffset(
      editor,
      line,
      injector.engineEditorHelper.getLineEndOffset(editor, line, allowPastEnd),
      allowPastEnd
    )
  }

  override fun moveCaretToLineStart(editor: VimEditor, line: Int): Int {
    if (line >= editor.lineCount()) {
      return editor.fileSize().toInt()
    }
    return injector.engineEditorHelper.getLineStartOffset(editor, line)
  }

  /**
   * This moves the caret to the start of the next/previous word/WORD.
   *
   * @param editor  The editor to move in
   * @param count   The number of words to skip
   * @param bigWord If true then find WORD, if false then find word
   * @return position
   */
  override fun findOffsetOfNextWord(editor: VimEditor, searchFrom: Int, count: Int, bigWord: Boolean): Motion {
    val size = editor.fileSize().toInt()
    if ((searchFrom == 0 && count < 0) || (searchFrom >= size - 1 && count > 0)) {
      return Motion.Error
    }
    return (injector.searchHelper.findNextWord(editor, searchFrom, count, bigWord)).toMotionOrError()
  }

  override fun getOffsetOfHorizontalMotion(
    editor: VimEditor,
    caret: VimCaret,
    count: Int,
    allowPastEnd: Boolean
  ): Int {
    val oldOffset = caret.offset.point
    var diff = 0
    val text = editor.text()
    val sign = sign(count.toFloat()).toInt()
    for (pointer in IntProgression.fromClosedRange(0, count -sign, sign)) {
      val textPointer = oldOffset + pointer
      diff += if (textPointer < text.length && textPointer >= 0) {
          // Actual char size can differ from 1 if unicode characters are used (like 🐔)
        Character.charCount(Character.codePointAt(text, textPointer))
      } else {
        1
      }
    }
    val offset = injector.engineEditorHelper
      .normalizeOffset(editor, caret.getLine().line, oldOffset + (sign * diff), allowPastEnd)

    return if (offset == oldOffset) -1 else offset
  }

  override fun moveCaretToLineStartSkipLeadingOffset(
    editor: VimEditor,
    caret: VimCaret,
    linesOffset: Int
  ): Int {
    val line = injector.engineEditorHelper.normalizeVisualLine(editor, caret.getVisualPosition().line + linesOffset)
    return moveCaretToLineStartSkipLeading(editor, injector.engineEditorHelper.visualLineToLogicalLine(editor, line))
  }

  override fun scrollFullPage(editor: VimEditor, caret: VimCaret, pages: Int): Boolean {
    assert(pages != 0)
    return if (pages > 0) scrollFullPageDown(editor, caret, pages) else scrollFullPageUp(editor, caret, abs(pages))
  }

  companion object {
    const val LAST_COLUMN = 9999
  }
}