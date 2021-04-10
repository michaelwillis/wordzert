(ns wordzert.server.core
  (:gen-class)
  (:require
   [hiccup.core :as hiccup]
   [org.httpkit.server :refer (run-server)]
   [compojure.core :refer (defroutes GET POST)]
   [compojure.route :as route]
   [ring.middleware.keyword-params]
   [ring.middleware.params]
   [ring.middleware.anti-forgery]
   [ring.middleware.session]))

(defn landing-page-handler [req]
  (hiccup/html
   [:html
    [:head
     [:meta {:charset "UTF-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
     [:link {:href "css/style.css" :rel "stylesheet" :type "text/css"}]]
    [:body
     (let [csrf-token (:anti-forgery-token req)]
       [:div#sente-csrf-token {:data-csrf-token csrf-token}])
     [:div#app]
     [:script {:src "cljs-out/dev-main.js" :type "text/javascript"}]]]))

(defroutes routes
  (GET "/" req (landing-page-handler req))
  (route/resources "/"))

(def handler 
  (-> routes
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params
      ring.middleware.anti-forgery/wrap-anti-forgery
      ring.middleware.session/wrap-session
      ))

(defn -main [& args]
  (run-server handler {:port 8000})
  (println (str "Server started, listening to port 8000")))
