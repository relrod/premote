package me.elrod.premote

import android.app.Activity
import android.os.Bundle
import android.util.Log

import scalaz._, Scalaz._
import scalaz.effect._
import scalaz.concurrent._

class MainActivity extends Activity {
  override def onPostCreate(savedInstanceState: Bundle): Unit = {
    super.onPostCreate(savedInstanceState)
    setContentView(R.layout.main)

    PremoteClient.pageUp.runAsync(_.fold(
      throwable => {
        Log.v("MainActivity", throwable.toString)
        ()
      },
      result    => {
        Log.v("MainActivity", result)
      }
    ))
  }
}

