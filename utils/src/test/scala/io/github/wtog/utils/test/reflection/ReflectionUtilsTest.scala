package io.github.wtog.utils.test.reflection

import io.github.wtog.utils.ReflectionUtils
import io.github.wtog.utils.test.BaseTest

/**
  * @author : tong.wang
  * @since : 3/7/20 9:26 AM
  * @version : 1.0.0
  */
class ReflectionUtilsTest extends BaseTest {

  test("get implementation classes") {
    val implementationClasses = ReflectionUtils
      .implementationClasses(
        classOf[BaseTest],
        "io.github.wtog.utils.BaseTest"
      )
    assert(!implementationClasses.contains(classOf[BaseTest]))
  }

}

