package common.zookeeper

import common.zookeeper.ZkUtil.setPersistent
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.Watcher.Event.EventType
import org.apache.zookeeper.data.Stat
import org.apache.zookeeper.{KeeperException, WatchedEvent, Watcher}

import java.nio.charset.StandardCharsets

object ZkWatcher {

  def onZkChange(cur: CuratorFramework, path: String)(handler: String => Unit): Unit = {

    println("[  watchNodeOrChidlrenChange ] == zknode : " + path)

    def watcher = new Watcher {
      def process(event: WatchedEvent): Unit = {
        println("[ watchNodeOrChidlrenChange ] == callback invoked " + path + "\ttype: " + event.getType)
        event.getType match {
          case EventType.NodeDataChanged | EventType.NodeChildrenChanged => updated()
          case _ => ()
        }
      }
    }

    def updated(): Unit = {
      try {
        val stat = new Stat()
        val msg = cur.getData.storingStatIn(stat).forPath(path)

        setPersistent(cur, path, "")

        val str = new String(msg, StandardCharsets.UTF_8)

        if (str.nonEmpty) {
          println("[ Watching ] == i got your command : " + new String(msg, StandardCharsets.UTF_8))
          handler(str)
        }

        if (str.startsWith("stop zkctrl")) {
          println("[ Watching ] == stopped by 'stop zkctrl' message : path =" + path)
        } else {
          /// create and attach next msg watcher
          cur.checkExists.usingWatcher(watcher).forPath(path)
        }

      } catch {
        case e: KeeperException =>
          println("[ Watching ] == found exception, i'll restart watching. node: " + path + "\te: " + e)
          reset()
      }
    }

    def reset(): Unit = {
      setPersistent(cur, path, "")
      updated()
    }

    reset()
  }

}
