package me.elrod.premote

import android.webkit.CookieManager
import java.io.{ InputStreamReader }
import java.net.{ HttpURLConnection, URL, URLEncoder }
import javax.net.ssl._
import scala.io.{ Codec, Source }
import scalaz._, Scalaz._
import scalaz.effect._
import scalaz.concurrent._

object PremoteClient {
  def connection(url: String, path: String): Task[HttpURLConnection] = Task {
    val fullUrl = url |+| path
    val c = new URL(fullUrl).openConnection.asInstanceOf[HttpURLConnection]
    c.setRequestMethod("GET")
    c
  }

  def connectionWithCookie(url: String, path: String, sessionId: String): Task[HttpURLConnection] = {
    def setCookie(c: HttpURLConnection) = Task {
      c.setRequestProperty("Cookie", sessionId)
      c
    }

    for {
      c <- connection(url, path)
      withCookie <- setCookie(c)
    } yield withCookie
  }

  def getCookie(connection: HttpURLConnection) =
    Option(connection.getHeaderField("Set-Cookie"))

  def obtainCookie(url: String): Task[Option[String]] = for {
    c <- connection(url, "/noop")
  } yield getCookie(c)

  def pageUp(url: String, sid: String): Task[String] = for {
    v <- connectionWithCookie(url, "/page-up", sid)
  } yield Source.fromInputStream(v.getInputStream)(Codec.UTF8).mkString

  def pageDown(url: String, sid: String): Task[String] = for {
    v <- connectionWithCookie(url, "/page-down", sid)
  } yield Source.fromInputStream(v.getInputStream)(Codec.UTF8).mkString
}
