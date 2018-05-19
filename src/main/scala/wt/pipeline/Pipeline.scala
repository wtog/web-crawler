package wt.pipeline

import java.util.concurrent.atomic.AtomicInteger


/**
  * @author : tong.wang
  * @since : 5/16/18 9:09 PM
  * @version : 1.0.0
  */
trait Pipeline {
  def process(pageResultItem: (String, Any))
}


case class ConsolePipeline() extends Pipeline {
  val pageSize: AtomicInteger = new AtomicInteger(0)
  override def process(pageResultItem: (String, Any)): Unit = {
    pageSize.incrementAndGet()
  }
}
