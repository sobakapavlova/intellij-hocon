package org.jetbrains.plugins.hocon

import com.intellij.FileSetTestCase
import com.intellij.application.options.CodeStyle
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.psi.{PsiFile, PsiFileFactory}
import com.intellij.testFramework.EditorTestUtil
import com.intellij.util.LocalTimeCounter
import org.jetbrains.plugins.hocon.lang.HoconLanguage
import org.jetbrains.plugins.hocon.psi.HoconPsiFile

/**
 * @author ghik
 */
abstract class HoconFileSetTestCase(subpath: String)
  extends FileSetTestCase(s"${HoconTestUtils.TestdataPath}/$subpath") with HoconTestUtils {

  def rootPath: String = s"$testdataPath/$subpath"

  override final def transform(testName: String, data: Array[String]): String = {
    val settings = CodeStyle.getSettings(myProject).getCommonSettings(HoconLanguage)
    val indentOptions = settings.getIndentOptions
    indentOptions.INDENT_SIZE = 2
    indentOptions.CONTINUATION_INDENT_SIZE = 2
    indentOptions.TAB_SIZE = 2
    transform(data.map(_.stripLineEnd))
  }

  protected def transform(data: Seq[String]): String

  private[hocon] def createPseudoPhysicalFile(text: String, extension: String): PsiFile = {
    val project = myProject
    val tempFile = project.getBasePath + "/temp." + extension
    val fileType = FileTypeManager.getInstance.getFileTypeByFileName(tempFile)
    PsiFileFactory.getInstance(project)
      .createFileFromText(tempFile, fileType, text, LocalTimeCounter.currentTime(), true)
  }

  def createPseudoPhysicalHoconFile(text: String): HoconPsiFile =
    createPseudoPhysicalFile(text, "conf").asInstanceOf[HoconPsiFile]

  private[hocon] def inWriteCommandAction[T](body: => T): T =
    WriteCommandAction.writeCommandAction(myProject).compute(() => body)

  override def getName: String = getClass.getName
}

object HoconFileSetTestCase {

  System.setProperty("fileset.pattern", "(.*)\\.test")

  private[hocon] def extractCaret(fileText: String): (String, Int) = {
    import EditorTestUtil.CARET_TAG
    val caretOffset = fileText.indexOf(CARET_TAG)

    val newFileText =
      if (caretOffset >= 0) fileText.substring(0, caretOffset) + fileText.substring(caretOffset + CARET_TAG.length)
      else fileText

    (newFileText, caretOffset)
  }
}
