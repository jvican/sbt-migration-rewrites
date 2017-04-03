package sbt.rewrite

case class SettingInfo(name: String, `type`: String)
object SettingInfo {
  def apply(proxyValues: Array[String]): SettingInfo = {
    assert(proxyValues.length == 2)
    SettingInfo(proxyValues.head, proxyValues.tail.head)
  }
}

case class Interpreted(inputKeys: List[String],
                       keyOfTasks: List[String],
                       failedSignatures: List[String]) {
  def addInputKey(inputKey: String): Interpreted =
    this.copy(inputKeys = inputKey :: inputKeys)

  def addKeyOfTask(keyOfTask: String): Interpreted =
    this.copy(keyOfTasks = keyOfTask :: keyOfTasks)

  def addFailedSignature(failed: String): Interpreted =
    this.copy(failedSignatures = failed :: failedSignatures)

  def isEmpty: Boolean =
    inputKeys.isEmpty && keyOfTasks.isEmpty && failedSignatures.isEmpty

  def reportToUser(): Unit = {
    if (isEmpty) println("Sbt runtime analysis produced no results.")
    else {
      println("Sbt runtime analysis reports:")
      println(s"\tInput keys: ${inputKeys.mkString(", ")}.")
      println(s"\tKeys that required evaluated: ${inputKeys.mkString(", ")}.")
      println(s"\tErrors parsing ${failedSignatures.mkString(", ")}.")
    }
  }
}

object Interpreted {
  def empty: Interpreted = Interpreted(Nil, Nil, Nil)
}

case class SbtContext(settingInfos: Array[SettingInfo]) {
  def interpretContext: Interpreted = {
    import scala.meta._
    settingInfos.foldLeft(Interpreted.empty) {
      case (acc, SettingInfo(name, tpe)) =>
        val typeResult = TypeReader.readTypeSignature(tpe)
        typeResult match {
          case _: Parsed.Error => acc.addFailedSignature(tpe)
          case Parsed.Success(parsedType) =>
            if (TypeReader.isInputKey(parsedType)) acc.addInputKey(name)
            else if (TypeReader.isKeyOfTask(parsedType)) acc.addKeyOfTask(name)
            else acc
        }
    }
  }
}

object TypeReader {
  import scala.meta._

  def readTypeSignature(signature: String): Parsed[Type] =
    signature.parse[Type]

  def isInputKey(`type`: Type): Boolean = {
    import Type.{Apply, Select, Name}
    `type` match {
      case Apply(Select(Term.Name("sbt"), Name("InputTask")), _) => true
      case _ => false
    }
  }

  def isKeyOfTask(`type`: Type): Boolean = {
    import Type.{Apply, Select, Name}
    `type` match {
      case Apply(_, args) =>
        args
          .flatMap(_.collect {
            case Select(Term.Name("sbt"), Name("Task")) =>
          })
          .nonEmpty
      case _ => false
    }
  }
}
