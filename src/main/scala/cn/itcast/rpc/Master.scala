package cn.itcast.rpc

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
import scala.collection.mutable

class Master(val Host: String, val Port: Int) extends Actor {
  //workedId->WorkerInfo
  val idToWorker = new mutable.HashMap[String, WorkerInfo]()
  //workerInfo
  val workers = new mutable.HashSet[WorkerInfo]()
  //超时检查的间隔
  val CHECK_INTERVAL = 15000

  override def preStart(): Unit = {
    println("preStart invoked")
    import context.dispatcher
    context.system.scheduler.schedule(0 millis, CHECK_INTERVAL millis, self, CheckTimeOutWorker)
  }

  //用于接收消息
  override def receive: Receive = {
    case RegisterWorker(id, memory, cores) => {
      //判断，是否注册
      if (!idToWorker.contains(id)) {
        //把Worker的信息封装起来保存到内存当中
        val workerInfo = new WorkerInfo(id, memory, cores)
        idToWorker(id) = workerInfo
        workers += workerInfo
        //"reply，你小子注册成功了，以后定期给我发送心跳"
        sender ! RegisteredWorker(s"akka.tcp://MaterSystem@$Host:$Port/user/Master")
      }
      println("a client connected!")

    }
    case Heartbeat(id) => {
      if (idToWorker.contains(id)) {
        val workerInfo = idToWorker(id)
        //报活
        val concurrentTime = System.currentTimeMillis()
        workerInfo.lastHeartbeatTime = concurrentTime
      }
    }
    case CheckTimeOutWorker => {
      val concurrentTime = System.currentTimeMillis()
      val toRemove = workers.filter(x => concurrentTime - x.lastHeartbeatTime > CHECK_INTERVAL)
      for (w <- toRemove) {
        workers -= w
        idToWorker -= w.id
      }
      println(workers.size)
    }
  }
}

object Master {
  def main(args: Array[String]) {
    val host = args(0)
    val port = args(1).toInt
    //准备配置
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    //ActorSystem老大，负责创建和监控下面的Actor，他是单例的
    val actorSystem = ActorSystem("MaterSystem", config)
    //创建Actor
    val master = actorSystem.actorOf(Props(new Master(host, port)), "Master")
    actorSystem.awaitTermination()

  }
}