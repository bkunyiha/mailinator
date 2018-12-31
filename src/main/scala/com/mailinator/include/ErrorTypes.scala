package com.mailinator.include

object ErrorTypes {

  trait EmailError {
    override def toString: String = this.getClass.getSimpleName.split("\\$").last

    val key: String = this.toString
    val message: String
  }

  case class Invalid(override val message: String) extends EmailError
  case class EmailExists(override val message: String = "Email Exists") extends EmailError
  case class EmailNotFound(override val message: String = "Email Not Found") extends EmailError
  case class EmailMessageNotFound(override val message: String = "Message Not Found") extends EmailError
}

