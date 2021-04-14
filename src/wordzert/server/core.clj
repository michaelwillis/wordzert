(ns wordzert.server.core
  (:gen-class)
  (:require
   [hiccup.core :as hiccup]
   [org.httpkit.server :refer (run-server)]
   [compojure.core :refer (defroutes GET POST)]
   [compojure.route :as route]
   [ring.middleware.defaults]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]))

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

(defn get-user-id [req]
  (get-in req [:cookies "ring-session" :value]))

(let [chsk-server (sente/make-channel-socket-server!
                   (get-sch-adapter)
                   {:user-id-fn get-user-id})
      {:keys [ch-recv send-fn connected-uids
              ajax-post-fn ajax-get-or-ws-handshake-fn]}
      chsk-server]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids)) ; Watchable, read-only atom)

;; We can watch this atom for changes if we like
(add-watch connected-uids :connected-uids
           (fn [_ _ old new]
             (when (not= old new)
               (println "Connected uids change: %s" new))))

(defmulti handle-event :id)

(defmethod handle-event :default [event]
  (println "Unhandled event: %s" (:id event)))

(defmethod handle-event :wordzert/join-game [event]
  (println "Join game! %s" (:?data event)))

(defmethod handle-event :wordzert/start-game [event]
  (println "Start game! %s" (:?data event)))

(defonce stop-event-handler-fn (atom nil))

(defn  stop-event-handler! []
  (when-let [stop-fn @stop-event-handler-fn] (stop-fn)))

(defn start-event-handler! []
  (stop-event-handler!)
  (reset! stop-event-handler-fn (sente/start-server-chsk-router! ch-chsk handle-event)))

(defroutes routes
  (GET "/" req (landing-page-handler req))
  (GET  "/chsk"  ring-req (ring-ajax-get-or-ws-handshake ring-req))
  (POST "/chsk"  ring-req (ring-ajax-post                ring-req))
  (route/resources "/"))

(def handler
  (-> routes
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params
      ring.middleware.anti-forgery/wrap-anti-forgery
      ring.middleware.session/wrap-session))


(defonce stop-server-fn (atom nil))

(defn stop-server! []
  (when-let [stop-fn @stop-server-fn] (stop-fn)))

(defn start-server! []
  (stop-server!)
  (reset! stop-server-fn (run-server handler {:port 8000}))
  (println (str "Server started, listening to port 8000")))


(defn -main [& args]
  (start-event-handler!)
  (start-server!))
