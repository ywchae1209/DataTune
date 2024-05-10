package common.zookeeper

import common.May.maybe
import common.zookeeper.ZkUtil.{getSubStrings, readString, save2Zk, setPersistent}
import org.apache.curator.RetryPolicy
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

object ZkClient {

  private def retryPolicy( baseSleepTimeMs: Int, maxRetries: Int)
  : ExponentialBackoffRetry = new ExponentialBackoffRetry( baseSleepTimeMs, maxRetries)

  private val defaultRetry =  retryPolicy(1000, 3)

  ////////////////////////////////////////////////////////////////////////////////
  def apply( connectionString: String, retry : RetryPolicy =  defaultRetry)
  : ZkClient = {
    val f = () => {
      val cur = CuratorFrameworkFactory.newClient( connectionString, retry)
      cur.start()
      sys.addShutdownHook { cur.close() }
      cur
    }
    new ZkClient( f )
  }
}

/**
 * {{{ NOTE:
 * 1. ZkClient contains only connection-maker ( not connection itself )
 * 2. Zookeeper connection is made when, first call and use in trailing calls
 * 3. Read/write action retry policy :: ExponentialBackoffRetry( 1 seconds, 3 max-retry-count )
 * }}}
 *
 */
class ZkClient( connector: () => CuratorFramework ) extends Serializable {

  lazy val curator: Option[ CuratorFramework ] = maybe { connector() }

  def set(path: String, st: String)
  = curator.foreach( setPersistent(_, path, st))

  def get(path: String): Option[String]
  = curator.flatMap( readString(_, path))

  def save(path: String, lst: Seq[String])
  = save2Zk(curator)( path, Some(lst.toArray))

  def getChildPathAndString(path: String)
  = curator.flatMap( c => getSubStrings(c, path))

}
