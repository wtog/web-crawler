package wt.exceptions

/**
  * @author : tong.wang
  * @since : 5/19/18 1:38 PM
  * @version : 1.0.0
  */
case class NonNullArgumentsException(arguments: String*) extends IllegalArgumentException {
  override def getLocalizedMessage: String = arguments.mkString(",") + " cant be null"
}

case class IllegalArgumentsException(arguments: String*) extends IllegalArgumentException {
  override def getLocalizedMessage: String = arguments.mkString(",") + " type is illeage"
}
