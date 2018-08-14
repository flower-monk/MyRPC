package cn.itcast.rpc

import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._
class Worker(val masterHost: String, val masterPort: Int, val memory: Int, val cores: Int) extends Actor {
  var master: ActorSelection = _
  val workerId = UUID.randomUUID().toString
  val CHECK_INTERVAL=10000

  //建立连接
  override def preStart(): Unit = {
    master = context.actorSelection(s"akka.tcp://MaterSystem@$masterHost:$masterPort/user/Master")
    //向Master发送注册信息
    master ! RegisterWorker(workerId, memory, cores)


  }

  override def receive: Receive = {
    case RegisteredWorker(masterUrl)=> {
      println("reply，你小子注册成功了，以后定期给我发送心跳")
      println(masterUrl)
      println("a reply from master")
      //启动定时器发送心跳
      import context.dispatcher
      context.system.scheduler.schedule(0 millis,CHECK_INTERVAL millis,self,SendHeartbeat)
  }
    case  SendHeartbeat=>{
      println("send heartbeat to master")
      master ! Heartbeat(workerId)
    }
}

}

object Worker {
  def main(args: Array[String]): Unit = {
    val host = args(0)
    val port = args(1).toInt
    val masterHost = args(2)
    val masterPort = args(3).toInt
    val memory = args(4).toInt
    val cores = args(5).toInt
    //准备配置
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
       """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    //ActorSystem老大，负责创建和监控下面的Actor，他是单例的
    val actorSystem = ActorSystem("WokerSystem", config)
    //创建Actor
    actorSystem.actorOf(Props(new Worker(masterHost, masterPort, memory, cores)), "Worker")
    actorSystem.awaitTermination()
  }

}
