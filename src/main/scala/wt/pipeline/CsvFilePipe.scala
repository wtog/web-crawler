package wt.pipeline

/**
  * @author : tong.wang
  * @since : 5/20/18 11:01 PM
  * @version : 1.0.0
  */
object CsvFilePipe extends Pipeline {
  override def process(pageResultItem: (String, Map[String, Any])): Unit = {
    println(pageResultItem._1 + " - " + pageResultItem._2)
  }
}
