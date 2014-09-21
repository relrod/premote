package me.elrod.premote

import android.webkit.CookieManager
import java.io.{ InputStreamReader }
import java.net.{ HttpURLConnection, URL, URLEncoder }
import javax.net.ssl.HttpsURLConnection
import scala.io.{ Codec, Source }
import scalaz._, Scalaz._
import scalaz.effect._
import scalaz.concurrent._

object PremoteClient {
  def connection(path: String): Task[HttpsURLConnection] = Task {
    val url = "https://10.10.10.125:3000/" |+| path.dropWhile(_ == '/')
    val c = new URL(url).openConnection.asInstanceOf[HttpsURLConnection]
    c.setRequestMethod("GET")
    c
  }

  def connectionWithCookie(path: String, sessionId: String): Task[HttpsURLConnection] = {
    def setCookie(c: HttpsURLConnection) = Task {
      c.setRequestProperty("Cookie", "sessionid=" |+| sessionId)
      c
    }

    for {
      c <- connection(path)
      withCookie <- setCookie(c)
    } yield withCookie
  }

  def getCookie(connection: HttpsURLConnection) =
    Option(connection.getHeaderField("Set-Cookie"))

  def obtainCookie: Task[Option[String]] = for {
    c <- connection("/noop")
  } yield getCookie(c)

  def pageUp: Task[String] = for {
    cookieOpt <- obtainCookie
    v <- connectionWithCookie("/page-up", cookieOpt | "")
  } yield Source.fromInputStream(v.getInputStream)(Codec.UTF8).mkString

  def pageDown: Task[String] = for {
    cookieOpt <- obtainCookie
    v <- connectionWithCookie("/page-down", cookieOpt | "")
  } yield Source.fromInputStream(v.getInputStream)(Codec.UTF8).mkString
}
