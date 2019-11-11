package io.github.wtog.utils

import com.google.common.base.CaseFormat

/**
  * @author : tong.wang
  * @since : 10/31/19 12:25 AM
  * @version : 1.0.0
  */
object StringUtils {

  lazy val underscoreConverter      = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE)
  lazy val lowerunderscoreConverter = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL)

  implicit class StringWrapper(s: String) {

    def toUnderscore = underscoreConverter.convert(s)

    def toLowercamel = lowerunderscoreConverter.convert(s)

    def placeholderReplacedBy(placeholder: String, replacement: Any*) = {
      val list   = s.split(placeholder).zip(replacement.toSeq)
      val buffer = new StringBuilder()
      list.foreach {
        case (s, p: String) =>
          buffer.append(s).append(s"'$p'")
        case (s, p) =>
          buffer.append(s).append(p)
      }
      buffer.toString()
    }
  }

}
