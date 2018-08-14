package cn.itast.girl

class MissRight[T] {

  def choose(first :T,second:T)(implicit ord:T=>Ordered[T]):T={
    if(first>second) first else second
  }
  def select(first:T,second:T)(implicit ord:Ordering[T]):T={
    if(ord.gt(first,second)) first else second
  }
  def random(first:T,second:T)(implicit ordering: Ordering[T]):T={
    import Ordered.orderingToOrdered
    if (first>second) first else second
  }
}
object MissRight{
  def main(args: Array[String]): Unit = {
    import MyPredef._
    val m1=new MissRight[Girl]
    val g1=new Girl("hatanaoYui",98,28)
    val g2=new Girl("sora",98,33)
//    val g=m1.choose(g1,g2)
    val g=m1.select(g1,g2)
    println(g.name)

  }
}