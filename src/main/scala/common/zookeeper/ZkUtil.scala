package common.zookeeper

import common.May.{maybeInfo, maybeWarn2, state, warn}
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.shaded.com.google.common.primitives.Longs
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.KeeperException.{NoNodeException, NodeExistsException}

import java.nio.charset.StandardCharsets
import scala.collection.immutable.ArraySeq.unsafeWrapArray
import scala.collection.immutable.{HashMap, HashSet}
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.reflect.ClassTag

////////////////////////////////////////////////////////////////////////////////
// todo :: clean up later...
////////////////////////////////////////////////////////////////////////////////
object ZkUtil {

  case object Conf {

    val zk_nodebyte = 524255 // 512 KByte
    val zk_sepLine = "\n"
    val zk_sepItem = "\t"
  }


  def save2Zk( curator: Option[CuratorFramework])
             ( path: String, arr: Option[Array[String]])
  : Boolean = {

    val ret =
      for {
        cur <- warn (curator)("save2ZK ===============> curator is not set")
        ar <- warn(arr)("save2ZK ===============> empty array")
        src = ar.mkString( Conf.zk_sepLine ).getBytes( StandardCharsets.UTF_8 )

      } yield setBigPersistent( cur, path, src, Conf.zk_nodebyte)

    ret.isDefined
  }

  ///////////////////////////////////////////////////////
  def readLines(cur: CuratorFramework,
                path2: String,
                sep: String = Conf.zk_sepLine)
  : Option[ Seq[String ] ] = {

    val ret =
      for {
        ab <- readChildren2Bytes( cur, path2 )
        st = new String( ab, StandardCharsets.UTF_8 )
      } yield unsafeWrapArray(st.split( sep))

    ret.filter(_.nonEmpty)
  }

  def readZKLinesWithFunction2[ A: ClassTag ]( curator: Option[CuratorFramework] )
                                             ( path2: String )
                                             ( f: Seq[String] => A ): A = {
    (for {
      cur  <- curator
      rows <- readLines( cur, path2)
      ret  <- maybeInfo( f(rows))(s"[ readZKLinesWithFunction2 ] fail to convert.. check : $path2")
    } yield {
      ret
    }) getOrElse (throw new Exception(s"[ readZKLinesWithFunction2 ] fail to convert.. check : $path2") )

  }

  def readZKLinesWithFunction[ A: ClassTag ]( curator: Option[CuratorFramework] )
                                            ( path2: String, limit:Int=0, sep: String = Conf.zk_sepItem )
                                            ( f: Array[ String ] => A ): Option[Seq[A]] = {
    for {
      cur <- curator
      rows <- readLines( cur, path2)
    } yield {
      val ret =
        rows.flatMap { row =>
          maybeInfo{ f( row.split( sep, limit )) }(s"[ readZKLinesWithFunction ] fail to convert : $row")
        }
      ret
    }
  }


  def readZKArray[ A: ClassTag ]( curator: Option[CuratorFramework] )
                                ( path: String, limit:Int=0, sep: String = Conf.zk_sepItem )
                                ( f: Array[ String ] => A )
  : Seq[ A ] = {

    val ar = readZKLinesWithFunction(curator )(path, limit, sep)(f)
    ar getOrElse {
      throw new Exception(s"[ readZkArray ] check $path")
    }
  }

  def readZKMap[ A, B ]( curator: Option[CuratorFramework] )
                       ( path: String, sep: String = Conf.zk_sepItem, limit:Int=0)
                       ( f: Array[ String ] => (A, B) )
  : HashMap[ A, B ] = {

    val ar = readZKLinesWithFunction( curator )(path, limit, sep)(f)

    val itms = ar.getOrElse {
      throw new Exception (s"[ readZKMap ] check $path")
    }
    HashMap[ A, B ]( itms: _* )
  }

  def readZKSet(curator: Option[CuratorFramework])
               (path: String)
  : HashSet[ String ] = {

    val ar = curator.flatMap( readLines( _, path)) getOrElse {
      throw new Exception (s"[ readZKSet ] check $path")
    }
    HashSet( ar: _* )
  }

  ////////////////////////////////////////////////////////////////////////////////
  def setPersistent ( cur: CuratorFramework, path: String, str: String)
  : Option[String ] = {

    warn {
      setPersistent( cur, path, str.getBytes( StandardCharsets.UTF_8 ) )
    }(s"[ createPersistent ] fails: $path, ${str.take(50)}")

  }

  // NOTE : large ar[Byte] split and save to child nodes
  def setBigPersistent( cur: CuratorFramework,
                        path: String,
                        ar: Array[Byte],
                        size: Int )
  : Array[ Option[String ] ] = {

    state (s"setPersistent: == $path : ${ar.take(40).toString}")
    val bulks = ar.grouped( size).zipWithIndex.toArray  // (data0, 0) ..
    bulks.map { case ( array, idx ) => setPersistent( cur, path + "/" + idx, array) }
  }

  def setPersistent(cur: CuratorFramework, path: String, ar: Array[Byte] )
  : Option[String ] = {

    try {
      Some( cur.setData().forPath( path, ar).toString )
    }
    catch {

      case _: NoNodeException =>
        try {
          createPersistent( cur, path, ar )
        }
        catch {
          case _: NodeExistsException => Some( cur.setData().forPath( path, ar).toString )
          case _: Throwable => None // todo : swallow exception
        }
      case _: Throwable => None // todo : swallow exception
    }
  }

  def createPersistent(cur: CuratorFramework, path: String)
  : Option[String ] = {

    maybeInfo{
      cur
        .create()
        .creatingParentsIfNeeded()
        .withMode(CreateMode.PERSISTENT)
        .forPath(path)
    }(s"[ createPersistent ] fails: $path")

  }

  def createPersistent(cur: CuratorFramework, path: String, str: String)
  : Option[String ] = {
    createPersistent( cur, path, str.getBytes( StandardCharsets.UTF_8))
  }

  def createPersistent(cur: CuratorFramework, path: String, data: Array[Byte])
  : Option[String ] = {

    maybeInfo {
      cur
        .create()
        .creatingParentsIfNeeded()
        .withMode( CreateMode.PERSISTENT )
        .forPath( path, data )
    }(s"[ createPersistent ] fails: $path ${data.take(50).toString}")

  }

  def readLong (cur: CuratorFramework, path: String)
  : Option[ Long ] =
    maybeInfo { Longs.fromByteArray( cur.getData.forPath( path ) ) }(s"[ readLong ] fails: $path")

  def readBytes (cur: CuratorFramework, path: String)
  : Option[ Array[ Byte ] ] =
    maybeInfo { cur.getData.forPath(path) }(s"[ readBytes ] fails: $path")

  // NOTE : read large data from child nodes
  def readChildren2Bytes(cur: CuratorFramework, path: String)
  : Option[Array[ Byte ] ] = {

    getChildren( cur, path).flatMap { children =>
      maybeWarn2 {
        children
          .map(_.toInt)
          .sorted
          .map { child => child -> readBytes(cur, path + "/" + child) }
          .foldLeft(Array[Byte]()) { case (b, (p, od)) => if (od.isDefined) b ++ od.get else b } // todo
      }(s"[ readChildren2Byte ] check zk path = $path")
    }
  }

  def readString (cur: CuratorFramework, path: String)
  : Option[ String ] = maybeInfo {
      new String( cur.getData.forPath(path), StandardCharsets.UTF_8)
    } ( s" ZK readString fails: $path" )

  def getChildren (cur: CuratorFramework, path: String)
  : Option[ Seq[String ] ] = {
    maybeInfo {
      cur.getChildren.forPath( path ).asScala.toSeq
    } ( s" ZK children not exists: $path" )
  }

  def getSubStrings (cur: CuratorFramework, path: String)
  = getChildren(cur, path).map( _.flatMap{ c =>
    val full = s"$path/$c"
    readString(cur, full).map(c -> _)  } )

  // recursive deleting
  def deletePersistent (cur: CuratorFramework, path: String)
  : Boolean = {
    maybeInfo {
      cur.delete().deletingChildrenIfNeeded().forPath( path)
    }( s" ZK delete fails : $path" ).isDefined
  }
}
