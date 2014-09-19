{-# LANGUAGE OverloadedStrings #-}
module Main where

import Control.Applicative ((<$>))
import Control.Monad.IO.Class
import Data.Monoid
import Data.String (fromString)
import Data.Word (Word64)
import Graphics.X11.Xlib
import Graphics.X11.Xlib.Extras
import Network.Info
import Network.Wai (Application)
import Network.Wai.Handler.Warp
import Network.Wai.Handler.WarpTLS (certFile, defaultTlsSettings,
                                    keyFile, runTLS)
import Web.Scotty

-- | Send a key to a given window (on a given rootwindow, on a given display).
sendKey :: Display -> Window -> Window -> KeyMask -> KeySym -> IO ()
sendKey display rootwindow window keymask keysym = allocaXEvent $ \event -> do
  kc <- keysymToKeycode display keysym
  setEventType event keyPress
  setKeyEvent event window rootwindow none keymask kc True
  sendEvent display window True keyPressMask event
  setEventType event keyRelease
  sendEvent display window True keyReleaseMask event

showInterfaces :: IO [(Int, NetworkInterface)]
showInterfaces = do
  interfaces <- zip [1..] <$> getNetworkInterfaces
  mapM_ (\(a, b) -> putStrLn $ showInterface a b) interfaces
  return interfaces
  where
    showInterface n i =
      show n <> ". " <> name i <> " (" <> show (ipv4 i) <> ")"

getSelection :: [(Int, NetworkInterface)] -> Int -> IO NetworkInterface
getSelection interfaces selection = do
  let selected = filter (\(a, _) -> a == selection) interfaces
  if length selected /= 1
    then do putStrLn "Invalid selection! Try again!"
            selection' <- read <$> getLine :: IO Int
            getSelection interfaces selection'
    else let selected' = snd . head $ selected
         in do
           putStrLn $ "Binding to " <> show (ipv4 selected')
           return selected'

main :: IO ()
main = do
  putStrLn "Please select an interface to bind to."
  interfaces <- showInterfaces
  selection <- read <$> getLine :: IO Int
  interface <- getSelection interfaces selection
  d <- liftIO $ openDisplay ""
  rw <- liftIO $ rootWindow d $ defaultScreen d
  let settings' = setHost (fromString (show (ipv4 interface))) defaultSettings
  app <- endpoints d rw 39845910 -- TODO
  runTLS
    (defaultTlsSettings { keyFile = "server.key" , certFile = "server.crt" })
    settings'
    app

endpoints :: Display -> Window -> Word64 -> IO Application
endpoints d w wid = scottyApp $ do
    get "/page-up" $ do
      liftIO $ sendKey d w wid noModMask xK_Page_Up
      liftIO $ flush d
      text "done"
