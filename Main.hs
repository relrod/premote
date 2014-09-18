module Main where

import Graphics.X11.Xlib
import Graphics.X11.Xlib.Extras

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
  d <- openDisplay ""
  rw <- rootWindow d $ defaultScreen d
  sendKey d rw 39845910 noModMask xK_g
  flush d
