object MyPredef {
//    implicit def girl2Ordered(g: Girl) = new Ordered[Girl] {
//      override def compare(that: Girl): Int = {
//        g.faceValue - that.faceValue
//      }
//    }
  trait  girl2Ordering extends Ordering[Girl]{
    override def compare(x: Girl, y: Girl): Int = {
       x.faceValue-y.faceValue
    }
  }
    implicit  object Girl extends girl2Ordering
}
