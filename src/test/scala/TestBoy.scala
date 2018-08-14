object TestBoy {
  def main(args: Array[String]): Unit = {
    val b1=new Boy("laoduan",99)
    val b2=new Boy("laozhao",999)

    val arr=Array(b1,b2)
    println(arr.toBuffer)
    val sorted=arr.sortBy(x=>x).reverse
    println(sorted)
    for (b<-sorted){
      println(b.name)
    }
  }

}
