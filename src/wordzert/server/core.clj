(ns wordzert.server.core
  (:gen-class)
  (:use [org.httpkit.server :only [run-server]]))

(defn handler [req]
  {:status 200
   :header {"Content-Type" "text/plain"}
   :body "Monkey Junk"})

(defn -main [& args]
  (run-server handler {:port 8000})
  (println (str "Server started, listening to port 8000")))
