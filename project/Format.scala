import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import sbt._
import scalariform.formatter.preferences.FormattingPreferences

import scala.collection.immutable.Seq

/**
  * @author : tong.wang
  * @since : 8/26/18 1:13 AM
  * @version : 1.0.0
  */
object Format {
    lazy val formatSettings: Seq[Setting[_]] = Seq(
      ScalariformKeys.autoformat in Test := true,
      ScalariformKeys.autoformat in Compile := true,
      ScalariformKeys.preferences in Compile := formattingPreferences,
      ScalariformKeys.preferences in Test := formattingPreferences)

    lazy val docFormatSettings: Seq[Setting[_]] = Seq(
      ScalariformKeys.autoformat in Test := true,
      ScalariformKeys.autoformat in Compile := true,
      ScalariformKeys.preferences in Compile := docFormattingPreferences,
      ScalariformKeys.preferences in Test := docFormattingPreferences)

    def formattingPreferences: FormattingPreferences = {
      import scalariform.formatter.preferences._
      FormattingPreferences()
        .setPreference(AlignParameters, true)
        .setPreference(NewlineAtEndOfFile, true)
        .setPreference(RewriteArrowSymbols, true)
        .setPreference(AllowParamGroupsOnNewlines, true)
        .setPreference(AlignSingleLineCaseStatements, true)
        .setPreference(IndentPackageBlocks, true)
        .setPreference(SpacesAroundMultiImports, true)
        .setPreference(DoubleIndentConstructorArguments, true)
    }

    def docFormattingPreferences: FormattingPreferences = formattingPreferences

}
