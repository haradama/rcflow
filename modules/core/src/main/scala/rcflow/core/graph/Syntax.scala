package rcflow.core.graph

import scala.language.implicitConversions

object Syntax:
  given Conversion[Node, Model] with
    def apply(n: Node): Model = Model(n)

  extension (a: Model)
    def >>(b: Model): Model = Model.link(a, b)
    def &(b: Model): Model = Model.link(a, b)
