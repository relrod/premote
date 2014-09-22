package me.elrod.premote

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.{Button, EditText}

import scalaz._, Scalaz._
import scalaz.effect._
import scalaz.concurrent._

class MainActivity extends Activity {
  override def onPostCreate(savedInstanceState: Bundle): Unit = {
    super.onPostCreate(savedInstanceState)
    setContentView(R.layout.main)

    // TODO: Holy crap.
    var sessionIdStr: Option[String] = None

    def iHateEverything(url: String): Task[String] =
      if (sessionIdStr.isDefined) {
        Task { sessionIdStr.get }
      } else {
        for {
          cookie <- PremoteClient.obtainCookie(url)
        } yield {
          sessionIdStr = cookie
          cookie.get
        }
      }

    val pgUp: Button = findViewById(R.id.pgup).asInstanceOf[Button]
    pgUp.setOnClickListener(new View.OnClickListener {
      def onClick(v: View) = {
        (for {
          url <- getUrl
          sessionId <- iHateEverything(url)
          str <- PremoteClient.pageUp(url, sessionId)
        } yield str).runAsync(_.fold(
          throwable => {
            throwable.printStackTrace
            ()
          },
          str => {
            Log.v("MainActivity", str)
            ()
          }
        ))
      }
    })

    val pgDown: Button = findViewById(R.id.pgdown).asInstanceOf[Button]
    pgDown.setOnClickListener(new View.OnClickListener {
      def onClick(v: View) = {
        (for {
          url <- getUrl
          sessionId <- iHateEverything(url)
          str <- PremoteClient.pageDown(url, sessionId)
        } yield str).runAsync(_.fold(
          throwable => {
            throwable.printStackTrace
            ()
          },
          str => {
            Log.v("MainActivity", str)
            ()
          }
        ))
      }
    })
  }

  private def getUrl = Task {
    findViewById(R.id.url).asInstanceOf[EditText].getText.toString
  }
}
