{-# LANGUAGE OverloadedStrings #-}
module Main where

import Control.Monad.IO.Class
import Graphics.X11.Xlib
import Graphics.X11.Xlib.Extras
import Web.Scotty
import Web.Scotty.TLS

-- | Send a key to a given window (on a given rootwindow, on a given display).
sendKey :: Display -> Window -> Window -> KeyMask -> KeySym -> IO ()
sendKey display rootwindow window keymask keysym = allocaXEvent $ \event -> do
  kc <- keysymToKeycode display keysym
  setEventType event keyPress
  setKeyEvent event window rootwindow none keymask kc True
  sendEvent display window True keyPressMask event
  setEventType event keyRelease
  sendEvent display window True keyReleaseMask event

main :: IO ()
main = do
  
  scottyTLS 3000 "server.key" "server.crt" $ do
    d <- liftIO $ openDisplay ""
    rw <- liftIO $ rootWindow d $ defaultScreen d
    get "/page-up" $ do
      liftIO $ sendKey d rw 39845910 noModMask xK_g
      liftIO $ flush d
      text "done"
